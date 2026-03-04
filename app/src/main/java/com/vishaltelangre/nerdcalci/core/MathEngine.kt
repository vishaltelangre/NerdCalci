package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

object MathEngine {

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
        return calculateWithVariables(lines, mutableMapOf())
    }

    /**
     * Collect variable state from preceding lines (those before the changed line) without
     * storing results. Used to seed partial recalculation with the correct variable context.
     */
    private fun buildVariableState(lines: List<LineEntity>): MutableMap<String, Double> {
        val variables = mutableMapOf<String, Double>()
        for (line in lines) {
            try {
                evaluateLine(line.expression, variables)
            } catch (_: Exception) {
                // Skip lines that can't be evaluated during the pre-pass
            }
        }
        return variables
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

        // Collect variable state from preceding lines, then fully recalculate affected lines
        val inheritedVariables = buildVariableState(precedingLines)
        return calculateWithVariables(affectedLines, inheritedVariables)
    }

    /**
     * Core evaluation loop: processes [lines] in order, building variable state from
     * [initialVariables] as assignments are encountered.
     */
    private fun calculateWithVariables(
        lines: List<LineEntity>,
        initialVariables: Map<String, Double>
    ): List<LineEntity> {
        val variables = initialVariables.toMutableMap()

        return lines.map { line ->
            if (line.expression.isBlank()) return@map line.copy(result = "")

            try {
                // Evaluate the line, updating `variables` if it's an assignment
                val result = evaluateLine(line.expression, variables)
                    ?: return@map line.copy(result = "")

                // Format result for display
                line.copy(result = formatResult(result))
            } catch (_: Exception) {
                line.copy(result = "Err")
            }
        }
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
