package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import java.math.BigDecimal
import java.math.MathContext as JavaMathContext
import java.util.Locale
import com.vishaltelangre.nerdcalci.core.Rational
import com.vishaltelangre.nerdcalci.utils.RegionUtils

/** A user-defined function local to a specific file. */
data class LocalFunction(val name: String, val params: List<String>, val body: List<Statement>)

/** Container for evaluated state. */
data class MathContext(
    val variables: MutableMap<String, EvaluationResult> = mutableMapOf(),
    val localFunctions: MutableMap<String, LocalFunction> = mutableMapOf(),
    val lineResults: MutableList<EvaluationResult?> = mutableListOf(),
    val userAssignedDynamicVariables: MutableSet<String> = mutableSetOf(),
    val fileVariables: MutableMap<String, String> = mutableMapOf(), // varName -> fileName
    val injectionErrors: MutableMap<String, Exception> = mutableMapOf(),
    val rationalMode: Boolean = false
)

object MathEngine {

    /**
     * Registry of dynamic variable keywords.
     *
     * Each entry maps a keyword name to a function that computes its value based on
     * the current block's line results.
     */
    private val DYNAMIC_VARIABLES: Map<String, (List<EvaluationResult?>, Map<String, EvaluationResult>) -> EvaluationResult> = mapOf(
        "sum"               to { results, _ -> computeBlockSum(results) },
        "total"             to { results, _ -> computeBlockSum(results) },

        "avg"               to { results, _ -> computeBlockAverage(results) },
        "average"           to { results, _ -> computeBlockAverage(results) },

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

    /**
     * Set of separators that are considered "safe" for Western-style calculation results.
     * Includes standard dot, comma, apostrophes, and various whitespace grouping separators.
     */
    internal val SAFE_SEPARATORS = setOf(
        '.',      // Dot (Decimal/Grouping)
        ',',      // Comma (Decimal/Grouping)
        ' ',      // Standard Space (Grouping)
        '\u00A0', // Non-Breaking Space (Grouping)
        '\u202F', // Narrow Non-Breaking Space (Grouping)
        '\'',     // Single Apostrophe (Grouping - e.g. Switzerland)
        '\u2019'  // Right Single Quotation Mark (Grouping - e.g. Switzerland)
    )

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
    suspend fun calculate(lines: List<LineEntity>, loader: FileContextLoader? = null, rationalMode: Boolean = false): List<LineEntity> {
        return calculateWithContext(lines, MathContext(rationalMode = rationalMode), loader)
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
        loadingStack: Set<String> = emptySet(),
        rationalMode: Boolean = false
    ): MathContext {
        val context = MathContext(rationalMode = rationalMode)
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
    suspend fun calculateFrom(allLines: List<LineEntity>, changedIndex: Int, loader: FileContextLoader? = null, loadingStack: Set<String> = emptySet(), rationalMode: Boolean = false): List<LineEntity> {
        // Determine which lines are affected by the edit
        val firstAffectedIndex = changedIndex.coerceIn(0, allLines.size)
        val precedingLines = allLines.subList(0, firstAffectedIndex)
        val affectedLines = allLines.subList(firstAffectedIndex, allLines.size)

        try {
            // Collect variable state and line results from preceding lines,
            // then fully recalculate affected lines
            val context = buildVariableState(precedingLines, loader, loadingStack, rationalMode)
            return calculateWithContext(affectedLines, context, loader, loadingStack)
        } catch (e: CircularReferenceException) {
            return calculateWithContext(allLines, MathContext(rationalMode = rationalMode), loader, loadingStack)
        }
    }

    /**
     * Re-evaluates a specific line to capture the exact exception message.
     * Used for on-demand error tooltips.
     */
    suspend fun getErrorDetails(allLines: List<LineEntity>, targetIndex: Int, loader: FileContextLoader? = null, loadingStack: Set<String> = emptySet(), rationalMode: Boolean = false): String? {
        if (targetIndex < 0 || targetIndex >= allLines.size) return null

        val precedingLines = allLines.subList(0, targetIndex)
        val targetLine = allLines[targetIndex]

        try {
            val context = buildVariableState(precedingLines, loader, loadingStack, rationalMode)
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
        val isolatedContext = MathContext(variables, localFunctions, lineResults, userAssignedDynamicVariables, fileVariables, rationalMode = context.rationalMode)

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
                        val displayValue = UnitConverter.fromBase(result.value, u, isolatedContext.variables).let { value ->
                            if (u.category == UnitCategory.TEMPERATURE) value.setScale(10, java.math.RoundingMode.HALF_UP) else value
                        }
                        val formattedValue = if (!result.forceFloat && (isolatedContext.rationalMode || result.explicitRational)) {
                            Rational.toRational(displayValue).toString()
                        } else {
                            formatBigDecimal(displayValue)
                        }
                        "$formattedValue ${result.unit}"
                    }
                } else if (result.explicitUnitless && result.value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    result.value.toLong().toString()
                } else if (!result.forceFloat && (isolatedContext.rationalMode || result.explicitRational) && result.rationalValue != null) {
                    result.rationalValue.toString()
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

    private fun computeBlockSum(lineResults: List<EvaluationResult?>): EvaluationResult {
        val blockResults = mutableListOf<EvaluationResult>()
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            blockResults.add(0, result)
        }

        if (blockResults.isEmpty()) return EvaluationResult(BigDecimal.ZERO)

        var expectedCategory: UnitCategory? = null
        var firstUnitSymbol: String? = null
        for (res in blockResults) {
            if (res.unit != null) {
                val u = UnitConverter.findUnit(res.unit)
                if (isPhysicalCategory(u?.category)) {
                    expectedCategory = u?.category
                    firstUnitSymbol = res.unit
                    break
                }
            }
        }

        // Target unit for final result is the unit of the LAST line with a physical unit
        var targetUnitSymbol: String? = null
        for (i in blockResults.indices.reversed()) {
            val u = blockResults[i].unit?.let { UnitConverter.findUnit(it) }
            if (isPhysicalCategory(u?.category)) {
                targetUnitSymbol = blockResults[i].unit
                break
            }
        }

        var sumValue = BigDecimal.ZERO
        for (result in blockResults) {
            val resultValue = result.value ?: BigDecimal.ZERO
            val resultUnit = result.unit?.let { UnitConverter.findUnit(it) }
            val resultCategory = resultUnit?.category
            
            if (expectedCategory != null) {
                // Block contains physical units: all lines must match that category
                if (!isPhysicalCategory(resultCategory) || resultCategory != expectedCategory) {
                    val expectedName = firstUnitSymbol?.let { UnitConverter.findUnit(it)?.name?.lowercase()?.replaceFirstChar { it.uppercase() } } ?: expectedCategory.name.lowercase().replaceFirstChar { it.uppercase() }
                    val resultName = resultUnit?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "unitless number"
                    throw EvalException("Summation of `$expectedName` and `$resultName` is not supported")
                }
                sumValue = sumValue.add(resultValue)
            } else {
                // Block contains no physical units: all lines must be non-physical
                if (isPhysicalCategory(resultCategory)) {
                    throw EvalException("Summation of physical and unitless values is not supported")
                }
                sumValue = sumValue.add(resultValue)
            }
        }

        return EvaluationResult(sumValue, targetUnitSymbol)
    }

    private fun computeBlockAverage(lineResults: List<EvaluationResult?>): EvaluationResult {
        val blockResults = mutableListOf<EvaluationResult>()
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            blockResults.add(0, result)
        }

        if (blockResults.isEmpty()) return EvaluationResult(BigDecimal.ZERO)
        
        // Use logic similar to sum for dimension checking
        var expectedCategory: UnitCategory? = null
        var firstUnitSymbol: String? = null
        for (res in blockResults) {
            val u = res.unit?.let { UnitConverter.findUnit(it) }
            val cat = u?.category
            if (isPhysicalCategory(cat)) {
                expectedCategory = cat
                firstUnitSymbol = res.unit
                break
            }
        }

        var targetUnitSymbol: String? = null
        for (i in blockResults.indices.reversed()) {
            val u = blockResults[i].unit?.let { UnitConverter.findUnit(it) }
            if (isPhysicalCategory(u?.category)) {
                targetUnitSymbol = blockResults[i].unit
                break
            }
        }

        var sumValue = BigDecimal.ZERO
        var count = 0
        for (result in blockResults) {
            val resultValue = result.value ?: BigDecimal.ZERO
            val resultUnit = result.unit?.let { UnitConverter.findUnit(it) }
            val resultCategory = resultUnit?.category

            if (expectedCategory != null) {
                if (!isPhysicalCategory(resultCategory) || resultCategory != expectedCategory) {
                    val expectedName = firstUnitSymbol?.let { UnitConverter.findUnit(it)?.name?.lowercase()?.replaceFirstChar { it.uppercase() } } ?: expectedCategory.name.lowercase().replaceFirstChar { it.uppercase() }
                    val resultName = resultUnit?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "unitless number"
                    throw EvalException("Average of `$expectedName` and `$resultName` is not supported")
                }
            } else {
                if (isPhysicalCategory(resultCategory)) {
                    throw EvalException("Average of physical and unitless values is not supported")
                }
            }
            
            sumValue = sumValue.add(resultValue)
            count++
        }

        val avgValue = if (count > 0) sumValue.divide(BigDecimal(count), JavaMathContext.DECIMAL128) else BigDecimal.ZERO
        return EvaluationResult(avgValue, targetUnitSymbol)
    }

    private fun isPhysicalCategory(category: UnitCategory?): Boolean {
        return category != null && category != UnitCategory.SCALAR && category != UnitCategory.NUMERAL_SYSTEM
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
            loadingStack = loadingStack,
            rationalMode = context.rationalMode
        ).evaluateStatement(statement, context)
    }

    /** Format a numeric result for display based on user-defined precision. */
    fun formatDisplayResult(
        rawResult: String,
        precision: Int,
        systemLocale: Locale = Locale.getDefault(),
        regionCode: String = RegionUtils.SYSTEM_DEFAULT,
        groupingSeparatorEnabled: Boolean = true
    ): String {
        if (rawResult.isBlank() || rawResult == "Err") return rawResult

        val locale = RegionUtils.getLocaleForRegion(regionCode, systemLocale)


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
        } else if (numStr.contains('E', ignoreCase = true) ||
                   value.abs() >= BigDecimal("1000000000000000") || // 10^15, reasonable threshold before Long range end
                   (value.abs() < BigDecimal("0.001") && value.abs() > BigDecimal.ZERO)) {
            formatScientific(value, safePrecision, locale)
        } else {
            val useIndianStyle = regionCode == "IN" ||
                    (regionCode == RegionUtils.SYSTEM_DEFAULT && locale.country == "IN")
            val isWholeNumber = value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0
            val forcePrecision = !isWholeNumber // Only force trailing zeros for actual decimals
            formatLocalized(value, safePrecision, locale, alwaysDecimal = false, forcePrecision = forcePrecision, useIndianStyle = useIndianStyle, groupingSeparatorEnabled = groupingSeparatorEnabled)
        }

        return if (isNumeralSystem) formattedResult else formattedResult + unitStr
    }

    /** Helper to format BigDecimal like Double for integers but with BigDecimal's precision for decimals. */
    internal fun formatBigDecimal(value: BigDecimal): String {
        val stz = value.stripTrailingZeros()
        val absoluteValue = stz.abs()

        if (absoluteValue >= BigDecimal("1000000000000000") || (absoluteValue < BigDecimal("0.00001") && absoluteValue > BigDecimal.ZERO)) {
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

    private fun getSafeSymbols(locale: Locale, groupingSeparatorEnabled: Boolean = true): java.text.DecimalFormatSymbols {
        var symbols = java.text.DecimalFormatSymbols.getInstance(locale)

        if (!SAFE_SEPARATORS.contains(symbols.decimalSeparator) ||
            (groupingSeparatorEnabled && !SAFE_SEPARATORS.contains(symbols.groupingSeparator))
        ) {
            // Fallback to English version of the region's locale to ensure Western-style separators.
            symbols = java.text.DecimalFormatSymbols.getInstance(
                Locale.Builder().setLanguage("en").setRegion(locale.country).build()
            )
        }

        // Always force Western digits (0-9)
        symbols.zeroDigit = '0'
        return symbols
    }

    private fun formatLocalized(
        value: BigDecimal,
        precision: Int,
        locale: Locale,
        alwaysDecimal: Boolean,
        forcePrecision: Boolean = false,
        useIndianStyle: Boolean = false,
        groupingSeparatorEnabled: Boolean = true
    ): String {
        val symbols = getSafeSymbols(locale, groupingSeparatorEnabled)

        if (useIndianStyle) {
            val rounded = value.setScale(precision, java.math.RoundingMode.HALF_UP)
            val s = rounded.toPlainString()
            val parts = s.split('.', limit = 2)
            val integerPart = parts[0]
            val decimalPart = parts.getOrNull(1)

            val sign = if (integerPart.startsWith('-')) "-" else ""
            val unsigned = integerPart.removePrefix("-")
            val groupedInteger = if (groupingSeparatorEnabled) {
                formatIndianStyle(unsigned, symbols.groupingSeparator)
            } else {
                unsigned
            }

            val formattedDecimal = when {
                forcePrecision -> {
                    val dp = decimalPart ?: "0".repeat(precision.coerceAtLeast(1))
                    "${symbols.decimalSeparator}$dp"
                }
                alwaysDecimal -> {
                    val dp = if (decimalPart.isNullOrEmpty()) "0" else decimalPart
                    "${symbols.decimalSeparator}$dp"
                }
                !decimalPart.isNullOrEmpty() && decimalPart.any { it != '0' } -> {
                    "${symbols.decimalSeparator}$decimalPart"
                }
                else -> ""
            }

            return sign + groupedInteger + formattedDecimal
        }

        val formatter = java.text.DecimalFormat()
        formatter.decimalFormatSymbols = symbols
        formatter.isGroupingUsed = groupingSeparatorEnabled
        formatter.maximumFractionDigits = precision
        formatter.minimumFractionDigits = if (forcePrecision) precision else if (alwaysDecimal) 1 else 0

        return formatter.format(value)
    }

    private fun formatIndianStyle(unsigned: String, separator: Char): String {
        if (unsigned.length <= 3) return unsigned

        val reversed = unsigned.reversed()
        val first3 = reversed.substring(0, 3)
        val rest = reversed.substring(3)
        val groupedRest = rest.chunked(2).joinToString(separator.toString())

        return (first3 + separator + groupedRest).reversed()
    }


    private fun formatScientific(value: BigDecimal, precision: Int, locale: Locale): String {
        val exponent = value.precision() - value.scale() - 1
        val mantissaScale = precision.coerceAtLeast(0)
        var mantissa = value.movePointLeft(exponent).setScale(mantissaScale, java.math.RoundingMode.HALF_UP)
        var adjustedExponent = exponent

        if (mantissa.abs() >= BigDecimal.TEN) {
            mantissa = mantissa.movePointLeft(1).setScale(mantissaScale, java.math.RoundingMode.HALF_UP)
            adjustedExponent += 1
        }

        val mantissaString = mantissa.toPlainString()
        val symbols = getSafeSymbols(locale, groupingSeparatorEnabled = false)
        val decimalSeparator = symbols.decimalSeparator
        val localizedMantissa = if (decimalSeparator == '.') {
            mantissaString
        } else {
            mantissaString.replace('.', decimalSeparator)
        }

        return "${localizedMantissa}E$adjustedExponent"
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
