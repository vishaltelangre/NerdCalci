package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import java.math.BigDecimal
import java.math.MathContext as JavaMathContext
import java.util.Locale

/** A user-defined function local to a specific file. */
data class LocalFunction(val name: String, val params: List<String>, val body: List<Statement>)

/** Container for evaluated state. */
data class MathContext(
    val variables: MutableMap<String, EvaluationResult> = mutableMapOf(),
    val localFunctions: MutableMap<String, LocalFunction> = mutableMapOf(),
    val lineResults: MutableList<EvaluationResult?> = mutableListOf(),
    val userAssignedDynamicVariables: MutableSet<String> = mutableSetOf(),
    val fileVariables: MutableMap<String, String> = mutableMapOf(), // varName -> fileName
    val injectionErrors: MutableMap<String, Exception> = mutableMapOf()
)

object MathEngine {

    /**
     * Registry of dynamic variable keywords.
     *
     * Each entry maps a keyword name to a function that computes its value based on
     * the current block's line results.
     */
    private val DYNAMIC_VARIABLES: Map<String, (List<EvaluationResult?>, Map<String, EvaluationResult>) -> EvaluationResult> = mapOf(
        "sum"               to { results, vars -> computeBlockSum(results, vars) },
        "total"             to { results, vars -> computeBlockSum(results, vars) },

        "avg"               to { results, vars -> computeBlockAverage(results, vars) },
        "average"           to { results, vars -> computeBlockAverage(results, vars) },

        "last"              to { results, _ -> computePreviousLineResult(results) },
        "prev"              to { results, _ -> computePreviousLineResult(results) },
        "previous"          to { results, _ -> computePreviousLineResult(results) },
        "above"             to { results, _ -> computePreviousLineResult(results) },
        "_"                 to { results, _ -> computePreviousLineResult(results) },

        "lineno"            to { results, _ -> computeCurrentLineNumber(results) },
        "linenumber"        to { results, _ -> computeCurrentLineNumber(results) },
        "currentLineNumber" to { results, _ -> computeCurrentLineNumber(results) }
    )

    val EXCLUDED_DOT_NOTATION_VARIABLES = setOf("lineno", "linenumber", "currentLineNumber")

    private val RESERVED_DYNAMIC_VARIABLES = TokenKind.entries
        .filter { it.isPreviousLineAlias || it.isLineNumberAlias }
        .map { it.display }
        .toSet()

    val dynamicVariableNames: Set<String> get() = DYNAMIC_VARIABLES.keys
    val reservedVariableNames: Set<String> get() = RESERVED_DYNAMIC_VARIABLES

    /**
     * Calculate results for all lines in a file, maintaining variable state across lines.
     *
     * ## Architecture
     * ```text
     *            ┌──────────┐   List<Token>   ┌──────────┐    AST [Expr]    ┌───────────┐
     * String ───►│  Lexer   ├────────────────►│  Parser  ├─────────────────►│ Evaluator │──► Double
     *            └──────────┘                 └──────────┘                  └───────────┘
     *             Handles:                     Handles:                       Handles:
     *             - whitespace                 - precedence/grouping          - var resolution
     *             - unicode norm               - percentage forms             - builtin calls
     *             - comments                   - assignments                  - math operations
     * ```
     *
     * Per-line pipeline:
     * - Lexer tokenizes the raw expression (handles comments, unicode, operators)
     * - Parser produces an AST (handles percentages, assignments, compounds natively)
     * - Evaluator walks the AST and returns a numeric result
     *
     * Variables persist across lines in order, so later lines can reference earlier variables:
     *   Line 1: "price = 100"        → result: "100", variables: {price: 100.0}
     *   Line 2: "tax = 10% of price" → result: "10", variables: {price: 100.0, tax: 10.0}
     *   Line 3: "price + tax"        → result: "110", uses both variables
     *
     * Result formatting:
     * - Whole numbers: "100" (not "100.00")
     * - Decimals: "3.33" (2 decimal places)
     * - Very large: "1.23e+15" (scientific notation)
     * - Errors: "Err" (for any invalid expression)
     *
     * @param lines List of line entities to calculate
     * @return List of line entities with populated results
     */
    suspend fun calculate(lines: List<LineEntity>, loader: FileContextLoader? = null): List<LineEntity> {
        return calculateWithContext(lines, MathContext(), loader)
    }

    /**
     * Collect variable/function state and line results from preceding lines (those before the changed
     * line). Used to seed partial recalculation with the correct context.
     *
     * Returns a populated MathContext.
     */
    suspend fun buildVariableState(
        lines: List<LineEntity>,
        loader: FileContextLoader? = null,
        loadingStack: Set<String> = emptySet()
    ): MathContext {
        val context = MathContext()
        for (line in lines) {
            if (line.expression.isBlank()) {
                context.lineResults.add(null)
                continue
            }
            try {
                injectDynamicVariables(context)
                val result = evaluateLine(line.expression, context, loader, loadingStack)
                context.lineResults.add(result.takeIf { it.value != null })
                trackDynamicVariableAssignment(line.expression, context.userAssignedDynamicVariables)
            } catch (e: Exception) {
                if (e is CircularReferenceException && loadingStack.isNotEmpty()) throw e
                context.lineResults.add(null)
            }
        }
        injectDynamicVariables(context)
        return context
    }

    /**
     * Partially recalculate only lines from [changedIndex] onward.
     *
     * Preceding lines (those before [changedIndex]) are silently scanned to collect variable
     * assignments, so that affected lines (from [changedIndex] onward) see the correct variable
     * state without needing to re-evaluate the entire file.
     *
     * @param allLines  Complete ordered list of lines for the file.
     * @param changedIndex  Index of the first line that changed (0-based).
     * @return Recalculated affected lines: `allLines[changedIndex..end]` with updated results.
     */
    suspend fun calculateFrom(allLines: List<LineEntity>, changedIndex: Int, loader: FileContextLoader? = null, loadingStack: Set<String> = emptySet()): List<LineEntity> {
        // Determine which lines are affected by the edit
        val firstAffectedIndex = changedIndex.coerceIn(0, allLines.size)
        val precedingLines = allLines.subList(0, firstAffectedIndex)
        val affectedLines = allLines.subList(firstAffectedIndex, allLines.size)

        try {
            // Collect variable state and line results from preceding lines,
            // then fully recalculate affected lines
            val context = buildVariableState(precedingLines, loader, loadingStack)
            return calculateWithContext(affectedLines, context, loader, loadingStack)
        } catch (e: CircularReferenceException) {
            return calculateWithContext(allLines, MathContext(), loader, loadingStack)
        }
    }

    /**
     * Re-evaluates a specific line to capture the exact exception message.
     * Used for on-demand error tooltips.
     */
    suspend fun getErrorDetails(allLines: List<LineEntity>, targetIndex: Int, loader: FileContextLoader? = null, loadingStack: Set<String> = emptySet()): String? {
        if (targetIndex < 0 || targetIndex >= allLines.size) return null

        val precedingLines = allLines.subList(0, targetIndex)
        val targetLine = allLines[targetIndex]

        try {
            val context = buildVariableState(precedingLines, loader, loadingStack)
            injectDynamicVariables(context)
            evaluateLine(targetLine.expression, context, loader, loadingStack)
            return null // No error occurred during this evaluation
        } catch (e: Exception) {
            return e.message
        }
    }

    /**
     * Core evaluation loop: processes [lines] in order, building variable state from
     * [initialVariables] as assignments are encountered.
     *
     * [lineResults] tracks the numeric result of each line (null for blank/comment/error),
     * used to compute values for dynamic variables like `sum`/`total`.
     */
    private suspend fun calculateWithContext(
        lines: List<LineEntity>,
        context: MathContext,
        loader: FileContextLoader? = null,
        loadingStack: Set<String> = emptySet()
    ): List<LineEntity> {
        val variables = context.variables.toMutableMap()
        val localFunctions = context.localFunctions.toMutableMap()
        val lineResults = context.lineResults
        val userAssignedDynamicVariables = context.userAssignedDynamicVariables.toMutableSet()
        val fileVariables = context.fileVariables.toMutableMap()
        val isolatedContext = MathContext(variables, localFunctions, lineResults, userAssignedDynamicVariables, fileVariables)

        return lines.map { line ->
            if (line.expression.isBlank()) {
                lineResults.add(null)
                return@map line.copy(result = "")
            }

            try {
                // Inject dynamic variables before evaluating the line
                injectDynamicVariables(isolatedContext)

                // Evaluate the line, updating `isolatedContext` if it's an assignment/function definition
                val result = evaluateLine(line.expression, isolatedContext, loader, loadingStack)
                if (result.value == null) {
                    lineResults.add(null)
                    return@map line.copy(result = "")
                }

                lineResults.add(result)

                // Track if user explicitly assigned a dynamic variable name
                trackDynamicVariableAssignment(line.expression, userAssignedDynamicVariables)

                // Store raw numeric result for persistence
                val u = if (result.unit != null) UnitConverter.findUnit(result.unit) else null
                val resultString = if (u != null) {
                    if (u.category == UnitCategory.NUMERAL_SYSTEM) {
                        if (result.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) throw IllegalArgumentException(ERR_FRACTIONAL_NUMERAL_SYSTEM)
                        // Check if value is within Long range
                        if (result.value.compareTo(BigDecimal(Long.MIN_VALUE)) < 0 || result.value.compareTo(BigDecimal(Long.MAX_VALUE)) > 0) {
                            // Out of range, fall back to scientific notation
                            formatBigDecimal(result.value)
                        } else {
                            formatNumeralSystem(result.value.toLong(), u.factor.toInt())
                        }
                    } else {
                        val displayValue = UnitConverter.fromBase(result.value, u, isolatedContext.variables)
                        val formattedValue = formatBigDecimal(displayValue)
                        "$formattedValue ${result.unit}"
                    }
                } else if (result.explicitUnitless && result.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    result.value.toLong().toString()
                } else {
                    formatBigDecimal(result.value)
                }
                line.copy(result = resultString)
            } catch (e: Exception) {
                lineResults.add(null)
                line.copy(result = "Err")
            }
        }
    }

    /**
     * Detect if a line contains an explicit assignment to a dynamic variable name.
     * If so, mark that name as user-assigned so we stop injecting it.
     */
    private fun trackDynamicVariableAssignment(expression: String, userAssigned: MutableSet<String>) {
        // Strip comments
        val hashIdx = expression.indexOf('#')
        val expr = if (hashIdx >= 0) expression.substring(0, hashIdx).trim() else expression.trim()

        for (name in DYNAMIC_VARIABLES.keys) {
            if (expr.startsWith(name) && expr.length > name.length) {
                val rest = expr.substring(name.length).trimStart()
                if (rest.startsWith("=") || rest.startsWith("+=") || rest.startsWith("-=") ||
                    rest.startsWith("*=") || rest.startsWith("/=") || rest.startsWith("%=") ||
                    rest.startsWith("++") || rest.startsWith("--") || rest.startsWith("(")) {
                    userAssigned.add(name)
                }
            }
        }
    }

    /**
     * Inject each registered dynamic variable into [variables] by calling its computation
     * function against the current [lineResults].  Skips any name the user has
     * explicitly assigned.
     */
    private fun injectDynamicVariables(context: MathContext) {
        for ((name, compute) in DYNAMIC_VARIABLES) {
            if (name !in context.userAssignedDynamicVariables) {
                context.variables.remove(name)
                context.injectionErrors.remove(name)
                try {
                    context.variables[name] = compute(context.lineResults, context.variables)
                } catch (e: Exception) {
                    context.injectionErrors[name] = e
                }
            }
        }
    }

    private fun computeBlockSum(lineResults: List<EvaluationResult?>, variables: Map<String, EvaluationResult>): EvaluationResult {
        val blockResults = mutableListOf<EvaluationResult>()
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            blockResults.add(0, result)
        }

        if (blockResults.isEmpty()) return EvaluationResult(BigDecimal.ZERO)

        // Identify the target unit for this block (the unit of the last line with a unit)
        var targetUnit: Unit? = null
        var targetUnitSymbol: String? = null
        for (i in blockResults.indices.reversed()) {
            val u = blockResults[i].unit?.let { UnitConverter.findUnit(it) }
            if (u != null && u.category != UnitCategory.SCALAR) {
                targetUnit = u
                targetUnitSymbol = blockResults[i].unit
                break
            }
        }

        var sumValue = BigDecimal.ZERO
        for (result in blockResults) {
            val resultValue = result.value ?: BigDecimal.ZERO
            val resultUnit = result.unit?.let { UnitConverter.findUnit(it) }
            if (targetUnit != null) {
                if (resultUnit != null) {
                    if (resultUnit.category != targetUnit.category && resultUnit.category != UnitCategory.SCALAR) {
                        throw EvalException("Cannot sum ${resultUnit.name} and ${targetUnit.name}: dimension mismatch")
                    }
                    sumValue = sumValue.add(resultValue)
                } else {
                    // Result is unitless. Treat as "targetUnit" same as '+' operator.
                    sumValue = sumValue.add(UnitConverter.toBase(resultValue, targetUnit, variables))
                }
            } else {
                if (resultUnit != null && resultUnit.category != UnitCategory.SCALAR) {
                    // This shouldn't happen given how we pick targetUnit, but for safety:
                    return EvaluationResult(null)
                }
                sumValue = sumValue.add(resultValue)
            }
        }

        return EvaluationResult(sumValue, targetUnitSymbol)
    }

    private fun computeBlockAverage(lineResults: List<EvaluationResult?>, variables: Map<String, EvaluationResult>): EvaluationResult {
        val blockResults = mutableListOf<EvaluationResult>()
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            blockResults.add(0, result)
        }

        if (blockResults.isEmpty()) return EvaluationResult(BigDecimal.ZERO)

        // Identify the target unit for this block (the unit of the last line with a unit)
        var targetUnit: Unit? = null
        var targetUnitSymbol: String? = null
        for (i in blockResults.indices.reversed()) {
            val u = blockResults[i].unit?.let { UnitConverter.findUnit(it) }
            if (u != null && u.category != UnitCategory.SCALAR) {
                targetUnit = u
                targetUnitSymbol = blockResults[i].unit
                break
            }
        }

        var sumValue = BigDecimal.ZERO
        var count = 0
        for (result in blockResults) {
            val resultValue = result.value ?: BigDecimal.ZERO
            val resultUnit = result.unit?.let { UnitConverter.findUnit(it) }
            if (targetUnit != null) {
                if (resultUnit != null) {
                    if (resultUnit.category != targetUnit.category && resultUnit.category != UnitCategory.SCALAR) {
                        throw EvalException("Cannot average ${resultUnit.name} and ${targetUnit.name}: dimension mismatch")
                    }
                    sumValue = sumValue.add(resultValue)
                } else {
                    sumValue = sumValue.add(UnitConverter.toBase(resultValue, targetUnit, variables))
                }
            } else {
                sumValue = sumValue.add(resultValue)
            }
            count++
        }

        val avgValue = if (count > 0) sumValue.divide(BigDecimal(count), JavaMathContext.DECIMAL128) else BigDecimal.ZERO
        return EvaluationResult(avgValue, targetUnitSymbol)
    }

    /**
     * Returns the result of the immediately preceding line, or 0.0 if that line
     * was blank, a comment, or an error.
     */
    private fun computePreviousLineResult(lineResults: List<EvaluationResult?>): EvaluationResult {
        return lineResults.lastOrNull() ?: EvaluationResult(BigDecimal.ZERO)
    }

    /**
     * Returns the 1-based index of the line currently being evaluated.
     */
    private fun computeCurrentLineNumber(lineResults: List<EvaluationResult?>): EvaluationResult {
        return EvaluationResult(BigDecimal(lineResults.size + 1))
    }

    /**
     * Evaluate a single line expression within the given variable context.
     * Returns the numeric result.
     * Returns null for empty/comment-only lines or for function definitions.
     * Mutates [context] if the line is an assignment, compound assignment,
     * increment/decrement, or function definition.
     */
    private suspend fun evaluateLine(
        expression: String,
        context: MathContext,
        loader: FileContextLoader? = null,
        loadingStack: Set<String> = emptySet()
    ): EvaluationResult {
        val tokens = Lexer(expression).tokenize()

        // If the only tokens are EOF (blank line) or the lexer skipped everything
        // (pure comment), there's nothing to evaluate.
        if (tokens.size == 1 && tokens[0].kind == TokenKind.EOF) return EvaluationResult(null)

        val statement = Parser(tokens).parse()

        return Evaluator(
            variables = context.variables,
            injectionErrors = context.injectionErrors,
            localFunctions = context.localFunctions,
            fileVariables = context.fileVariables,
            fileContextLoader = loader,
            loadingStack = loadingStack
        ).evaluateStatement(statement, context)
    }

    /** Format a numeric result for display based on user-defined precision. */
    fun formatDisplayResult(
        rawResult: String,
        precision: Int,
        locale: Locale = Locale.getDefault(),
        settings: NumberFormatSettings = NumberFormatSettings()
    ): String {
        if (rawResult.isBlank() || rawResult == "Err") return rawResult

        val spaceIndex = rawResult.indexOf(' ')
        val (numStr, unitStr) = if (spaceIndex > 0) {
            rawResult.substring(0, spaceIndex) to rawResult.substring(spaceIndex)
        } else {
            rawResult to ""
        }

        val value = numStr.toBigDecimalOrNull() ?: return rawResult
        val safePrecision = precision.coerceIn(Constants.MIN_PRECISION, Constants.MAX_PRECISION)

        val trimmedUnit = unitStr.trim().lowercase()
        val isNumeralSystem = UnitConverter.isNumeralSystemSymbol(trimmedUnit)

        val formattedResult = if (isNumeralSystem) {
            if (value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) return "Err"
            val radix = when (trimmedUnit) {
                "bin" -> 2
                "hex" -> 16
                "oct" -> 8
                else -> 10
            }
            formatNumeralSystem(value.toLong(), radix)
        } else if (numStr.contains('E', ignoreCase = true)) {
            formatScientific(value, safePrecision, locale)
        } else if (value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            val longVal = value.toLong()
            when {
                value.compareTo(BigDecimal(Int.MIN_VALUE)) >= 0 && value.compareTo(BigDecimal(Int.MAX_VALUE)) <= 0 ->
                    formatInteger(longVal.toInt().toLong(), locale, settings)
                value.compareTo(BigDecimal(Long.MIN_VALUE)) >= 0 && value.compareTo(BigDecimal(Long.MAX_VALUE)) <= 0 ->
                    formatInteger(longVal, locale, settings)
                else ->
                    formatScientific(value, safePrecision, locale)
            }
        } else {
            formatDecimal(value, safePrecision, locale, settings)
        }

        return if (isNumeralSystem) formattedResult else formattedResult + unitStr
    }

    /** Helper to format BigDecimal like Double for integers but with BigDecimal's precision for decimals. */
    internal fun formatBigDecimal(value: BigDecimal): String {
        val stz = value.stripTrailingZeros()
        val absoluteValue = stz.abs()

        if (absoluteValue >= BigDecimal("10000000") || (absoluteValue < BigDecimal("0.001") && absoluteValue > BigDecimal.ZERO)) {
            // High-precision scientific notation
            val scale = stz.precision() - stz.scale() - 1
            val unscaled = stz.movePointLeft(scale).stripTrailingZeros()
            var s = unscaled.toPlainString()
            if (!s.contains('.')) s += ".0"
            return s + "E" + scale
        }

        var s = stz.toPlainString()
        if (!s.contains('.') && !s.contains('E')) {
            s += ".0"
        }
        return s
    }

    private fun formatInteger(value: Long, locale: Locale, settings: NumberFormatSettings): String {
        return formatDecimalParts(value.toString(), null, locale, settings, alwaysDecimal = false)
    }

    private fun formatDecimal(value: BigDecimal, precision: Int, locale: Locale, settings: NumberFormatSettings): String {
        // Use BigDecimal.setScale to avoid precision loss from toDouble()
        val rounded = value.setScale(precision, java.math.RoundingMode.HALF_UP)
        val plainString = rounded.toPlainString()
        val parts = plainString.split('.', limit = 2)
        return formatDecimalParts(parts[0], parts.getOrNull(1), locale, settings, alwaysDecimal = false)
    }

    private fun formatDecimalParts(
        integerPart: String,
        decimalPart: String?,
        locale: Locale,
        settings: NumberFormatSettings,
        alwaysDecimal: Boolean
    ): String {
        val useGrouping = settings.separators != NumberSeparatorPreset.OFF

        val groupingSeparator = when (settings.separators) {
            NumberSeparatorPreset.LOCALE -> java.text.DecimalFormatSymbols.getInstance(locale).groupingSeparator
            NumberSeparatorPreset.COMMA -> ','
            NumberSeparatorPreset.DOT -> '.'
            NumberSeparatorPreset.SPACE -> ' '
            NumberSeparatorPreset.OFF -> java.text.DecimalFormatSymbols.getInstance(locale).groupingSeparator
        }
        val decimalSeparator = when (settings.decimal) {
            NumberDecimalPreset.LOCALE -> java.text.DecimalFormatSymbols.getInstance(locale).decimalSeparator
            NumberDecimalPreset.COMMA -> ','
            NumberDecimalPreset.DOT -> '.'
        }

        val sign = if (integerPart.startsWith('-')) "-" else ""
        val unsigned = integerPart.removePrefix("-")
        val groupedInteger = if (useGrouping) unsigned.reversed().chunked(3).joinToString(groupingSeparator.toString()).reversed() else unsigned
        val dec = decimalPart?.let { if (it.isNotEmpty()) "$decimalSeparator$it" else "" } ?: if (alwaysDecimal) "${decimalSeparator}0" else ""
        return sign + groupedInteger + dec
    }

    private fun formatScientific(value: BigDecimal, precision: Int, locale: Locale): String {
        return "%.${precision}e".format(locale, value.toDouble())
    }

    private fun formatNumeralSystem(value: Long, radix: Int): String {
        val prefix = when (radix) {
            16 -> "0x"
            2 -> "0b"
            8 -> "0o"
            else -> ""
        }
        return "$prefix${java.lang.Long.toString(value, radix).uppercase()}"
    }
}