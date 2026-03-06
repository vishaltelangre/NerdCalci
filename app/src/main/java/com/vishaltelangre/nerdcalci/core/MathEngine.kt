package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

/** A user-defined function local to a specific file. */
data class LocalFunction(val name: String, val params: List<String>, val body: List<Statement>)

/** Container for evaluated state. */
data class MathContext(
    val variables: MutableMap<String, Double> = mutableMapOf(),
    val localFunctions: MutableMap<String, LocalFunction> = mutableMapOf(),
    val lineResults: MutableList<Double?> = mutableListOf(),
    val userAssignedDynamicVariables: MutableSet<String> = mutableSetOf()
)

object MathEngine {

    /**
     * Registry of dynamic variable keywords.
     *
     * Each entry maps a keyword name to a function that computes its value based on
     * the current block's line results.
     */
    private val DYNAMIC_VARIABLES: Map<String, (List<Double?>) -> Double> = mapOf(
        "sum"     to ::computeBlockSum,
        "total"   to ::computeBlockSum,
        "avg"     to ::computeBlockAverage,
        "average" to ::computeBlockAverage
    )

    val dynamicVariableNames: Set<String> get() = DYNAMIC_VARIABLES.keys

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
    fun calculate(lines: List<LineEntity>): List<LineEntity> {
        return calculateWithContext(lines, MathContext())
    }

    /**
     * Collect variable/function state and line results from preceding lines (those before the changed
     * line). Used to seed partial recalculation with the correct context.
     *
     * Returns a populated MathContext.
     */
    private fun buildVariableState(
        lines: List<LineEntity>
    ): MathContext {
        val context = MathContext()
        for (line in lines) {
            if (line.expression.isBlank()) {
                context.lineResults.add(null)
                continue
            }
            try {
                injectDynamicVariables(context)
                val result = evaluateLine(line.expression, context)
                context.lineResults.add(result)
                trackDynamicVariableAssignment(line.expression, context.userAssignedDynamicVariables)
            } catch (_: Exception) {
                context.lineResults.add(null)
            }
        }
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
    fun calculateFrom(allLines: List<LineEntity>, changedIndex: Int): List<LineEntity> {
        // Determine which lines are affected by the edit
        val firstAffectedIndex = changedIndex.coerceIn(0, allLines.size)
        val precedingLines = allLines.subList(0, firstAffectedIndex)
        val affectedLines = allLines.subList(firstAffectedIndex, allLines.size)

        // Collect variable state and line results from preceding lines,
        // then fully recalculate affected lines
        val context = buildVariableState(precedingLines)
        return calculateWithContext(affectedLines, context)
    }

    /**
     * Core evaluation loop: processes [lines] in order, building variable state from
     * [initialVariables] as assignments are encountered.
     *
     * [lineResults] tracks the numeric result of each line (null for blank/comment/error),
     * used to compute values for dynamic variables like `sum`/`total`.
     */
    private fun calculateWithContext(
        lines: List<LineEntity>,
        context: MathContext
    ): List<LineEntity> {
        val variables = context.variables.toMutableMap()
        val localFunctions = context.localFunctions.toMutableMap()
        val lineResults = context.lineResults
        val userAssignedDynamicVariables = context.userAssignedDynamicVariables.toMutableSet()
        val isolatedContext = MathContext(variables, localFunctions, lineResults, userAssignedDynamicVariables)

        return lines.map { line ->
            if (line.expression.isBlank()) {
                lineResults.add(null)
                return@map line.copy(result = "")
            }

            try {
                // Inject dynamic variables before evaluating the line
                injectDynamicVariables(isolatedContext)

                // Evaluate the line, updating `isolatedContext` if it's an assignment/function definition
                val result = evaluateLine(line.expression, isolatedContext)
                    ?: run {
                        lineResults.add(null)
                        return@map line.copy(result = "")
                    }

                lineResults.add(result)

                // Track if user explicitly assigned a dynamic variable name
                trackDynamicVariableAssignment(line.expression, userAssignedDynamicVariables)

                // Store raw numeric result for persistence
                line.copy(result = result.toString())
            } catch (_: Exception) {
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
                context.variables[name] = compute(context.lineResults)
            }
        }
    }

    /**
     * Sum the current block's line results, scanning backward from the end of
     * [lineResults] until a null entry (blank line / comment / error) or the start.
     */
    private fun computeBlockSum(lineResults: List<Double?>): Double {
        var sum = 0.0
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            sum += result
        }
        return sum
    }

    /**
     * Average the current block's line results, scanning backward from the end of
     * [lineResults] until a null entry (blank line / comment / error) or the start.
     */
    private fun computeBlockAverage(lineResults: List<Double?>): Double {
        var sum = 0.0
        var count = 0
        for (i in lineResults.indices.reversed()) {
            val result = lineResults[i] ?: break
            sum += result
            count++
        }
        return if (count > 0) sum / count else 0.0
    }

    /**
     * Evaluate a single line expression within the given variable context.
     * Returns the numeric result.
     * Returns null for empty/comment-only lines or for function definitions.
     * Mutates [context] if the line is an assignment, compound assignment,
     * increment/decrement, or function definition.
     */
    private fun evaluateLine(expression: String, context: MathContext): Double? {
        val tokens = Lexer(expression).tokenize()

        // If the only tokens are EOF (blank line) or the lexer skipped everything
        // (pure comment), there's nothing to evaluate.
        if (tokens.size == 1 && tokens[0].kind == TokenKind.EOF) return null

        val statement = Parser(tokens).parse()

        return Evaluator(variables = context.variables, localFunctions = context.localFunctions)
            .evaluateStatement(statement, context)
    }

    /** Format a numeric result for display based on user-defined precision. */
    fun formatDisplayResult(rawResult: String, precision: Int): String {
        if (rawResult.isBlank() || rawResult == "Err") return rawResult
        val value = rawResult.toDoubleOrNull() ?: return rawResult

        return if (value % 1.0 == 0.0) {
            when {
                value >= Int.MIN_VALUE && value <= Int.MAX_VALUE ->
                    value.toInt().toString()
                value >= Long.MIN_VALUE && value <= Long.MAX_VALUE ->
                    value.toLong().toString()
                else ->
                    String.format("%.${precision}e", value)
            }
        } else {
            String.format("%.${precision}f", value)
        }
    }
}
