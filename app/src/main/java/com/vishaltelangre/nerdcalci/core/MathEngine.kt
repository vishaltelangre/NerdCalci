package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

object MathEngine {

    /**
     * Registry of aggregate keywords.
     *
     * Each entry maps a keyword name to a function that computes its value from
     * the current block's line results.
     */
    private val AGGREGATES: Map<String, (List<Double?>) -> Double> = mapOf(
        "sum"   to ::computeBlockSum,
        "total" to ::computeBlockSum
    )

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
        return calculateWithVariables(lines, mutableMapOf(), mutableListOf())
    }

    /**
     * Collect variable state and line results from preceding lines (those before the changed
     * line). Used to seed partial recalculation with the correct
     * variable context and aggregate state.
     *
     * Returns a triple of (variables, lineResults, userAssignedAggregates).
    private fun buildVariableState(
        lines: List<LineEntity>
    ): Triple<MutableMap<String, Double>, MutableList<Double?>, MutableSet<String>> {
        val variables = mutableMapOf<String, Double>()
        val lineResults = mutableListOf<Double?>()
        val userAssignedAggregates = mutableSetOf<String>()
        for (line in lines) {
            if (line.expression.isBlank()) {
                lineResults.add(null)
                continue
            }
            try {
                injectAggregates(variables, lineResults, userAssignedAggregates)
                val result = evaluateLine(line.expression, variables)
                lineResults.add(result)
                // Track if user explicitly assigned an aggregate name
                trackAggregateAssignment(line.expression, userAssignedAggregates)
            } catch (_: Exception) {
                lineResults.add(null)
            }
        }
        return Triple(variables, lineResults, userAssignedAggregates)
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
        val (inheritedVariables, lineResults, userAssignedAggregates) = buildVariableState(precedingLines)
        return calculateWithVariables(affectedLines, inheritedVariables, lineResults, userAssignedAggregates)
    }

    /**
     * Core evaluation loop: processes [lines] in order, building variable state from
     * [initialVariables] as assignments are encountered.
     *
     * [lineResults] tracks the numeric result of each line (null for blank/comment/error),
     * used to compute values for aggregates like `sum`/`total`.
     */
    private fun calculateWithVariables(
        lines: List<LineEntity>,
        initialVariables: Map<String, Double>,
        lineResults: MutableList<Double?>,
        userAssignedAggregates: MutableSet<String> = mutableSetOf()
    ): List<LineEntity> {
        val variables = initialVariables.toMutableMap()

        return lines.map { line ->
            if (line.expression.isBlank()) {
                lineResults.add(null)
                return@map line.copy(result = "")
            }

            try {
                // Inject aggregate variables before evaluating the line
                injectAggregates(variables, lineResults, userAssignedAggregates)

                // Evaluate the line, updating `variables` if it's an assignment
                val result = evaluateLine(line.expression, variables)
                    ?: run {
                        lineResults.add(null)
                        return@map line.copy(result = "")
                    }

                lineResults.add(result)

                // Track if user explicitly assigned an aggregate name
                trackAggregateAssignment(line.expression, userAssignedAggregates)

                // Format result for display
                line.copy(result = formatResult(result))
            } catch (_: Exception) {
                lineResults.add(null)
                line.copy(result = "Err")
            }
        }
    }

    /**
     * Detect if a line contains an explicit assignment to an aggregate name.
     * If so, mark that name as user-assigned so we stop injecting it.
     */
    private fun trackAggregateAssignment(expression: String, userAssigned: MutableSet<String>) {
        // Strip comments
        val hashIdx = expression.indexOf('#')
        val expr = if (hashIdx >= 0) expression.substring(0, hashIdx).trim() else expression.trim()

        for (name in AGGREGATES.keys) {
            if (expr.startsWith(name) && expr.length > name.length) {
                val rest = expr.substring(name.length).trimStart()
                if (rest.startsWith("=") || rest.startsWith("+=") || rest.startsWith("-=") ||
                    rest.startsWith("*=") || rest.startsWith("/=") || rest.startsWith("%=") ||
                    rest.startsWith("++") || rest.startsWith("--")) {
                    userAssigned.add(name)
                }
            }
        }
    }

    /**
     * Inject each registered aggregate into [variables] by calling its computation
     * function against the current [lineResults].  Skips any name the user has
     * explicitly assigned.
     */
    private fun injectAggregates(
        variables: MutableMap<String, Double>,
        lineResults: List<Double?>,
        userAssigned: Set<String>
    ) {
        for ((name, compute) in AGGREGATES) {
            if (name !in userAssigned) {
                variables[name] = compute(lineResults)
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
     * Evaluate a single line expression within the given variable context.
     * Returns the numeric result, or null for empty/comment-only lines.
     * Mutates [variables] if the line is an assignment, compound assignment,
     * or increment/decrement.
     */
    private fun evaluateLine(expression: String, variables: MutableMap<String, Double>): Double? {
        val tokens = Lexer(expression).tokenize()

        // If the only tokens are EOF (blank line) or the lexer skipped everything
        // (pure comment), there's nothing to evaluate.
        if (tokens.size == 1 && tokens[0].kind == TokenKind.EOF) return null

        val statement = Parser(tokens).parse()

        return when (statement) {
            is Statement.Empty -> null

            is Statement.ExprStatement -> {
                Evaluator(variables).evaluate(statement.expr)
            }

            is Statement.Assignment -> {
                // Validate variable name if this is an assignment
                if (!statement.name.matches(Regex(Constants.VARIABLE_NAME_PATTERN))) {
                    throw EvalException("Invalid variable name '${statement.name}'")
                }

                // Evaluate the expression and store the result
                val result = Evaluator(variables).evaluate(statement.expr)
                variables[statement.name] = result
                result
            }

            is Statement.CompoundAssignment -> {
                val current = variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
                val rhs = Evaluator(variables).evaluate(statement.expr)
                val result = when (statement.op) {
                    TokenKind.PLUS_EQUALS    -> current + rhs
                    TokenKind.MINUS_EQUALS   -> current - rhs
                    TokenKind.STAR_EQUALS    -> current * rhs
                    TokenKind.SLASH_EQUALS   -> {
                        if (rhs == 0.0) throw DivisionByZeroException()
                        current / rhs
                    }
                    TokenKind.PERCENT_EQUALS -> {
                        if (rhs == 0.0) throw DivisionByZeroException()
                        current % rhs
                    }
                    else -> throw EvalException("Unknown compound operator: ${statement.op}")
                }
                variables[statement.name] = result
                result
            }

            is Statement.Increment -> {
                val current = variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
                val result = current + 1
                variables[statement.name] = result
                result
            }

            is Statement.Decrement -> {
                val current = variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
                val result = current - 1
                variables[statement.name] = result
                result
            }
        }
    }

    /** Format a numeric result for display. */
    private fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            when {
                value >= Int.MIN_VALUE && value <= Int.MAX_VALUE ->
                    value.toInt().toString()
                value >= Long.MIN_VALUE && value <= Long.MAX_VALUE ->
                    value.toLong().toString()
                else ->
                    String.format("%.2e", value)
            }
        } else {
            String.format("%.2f", value)
        }
    }
}
