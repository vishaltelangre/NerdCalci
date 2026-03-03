package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import net.objecthunter.exp4j.ExpressionBuilder

object MathEngine {
    // exp4j built-in functions and constants — excluded from the undefined-variable check.
    // https://www.objecthunter.net/exp4j/
    private val BUILT_IN_IDENTIFIERS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "log", "log10", "log2", "log1p",
        "sqrt", "cbrt", "abs", "floor", "ceil", "signum",
        "exp", "expm1", "pow", "e", "pi"
    )

    // Matches valid variable/function name tokens in an expression.
    private val VARIABLE_PATTERN = Regex("""[a-zA-Z_][a-zA-Z0-9_]*""")

    // Strip comments (anything after #)
    private fun stripComments(expr: String): String {
        val hashIndex = expr.indexOf('#')
        return if (hashIndex >= 0) {
            expr.substring(0, hashIndex).trim()
        } else {
            expr
        }
    }

    // Normalize Unicode operators to standard ASCII
    private fun normalizeOperators(expr: String): String {
        return expr
            .replace("×", "*")  // Multiplication sign → asterisk
            .replace("÷", "/")  // Division sign → slash
    }

    private fun preprocessPercentages(expr: String): String {
        var result = expr
        // Order matters: more specific patterns first

        // "20% off 100" → "(100 - 100 * 20 / 100)" = 80
        // "15% off price" → "(price - price * 15 / 100)"
        result = result.replace(Regex("""(\d+(?:\.\d+)?)\s*%\s+off\s+(\d+(?:\.\d+)?|\w+)"""), "($2 - $2 * $1 / 100)")

        // "20% of 100" → "(100 * 20 / 100)" = 20
        // "10% of salary" → "(salary * 10 / 100)"
        result = result.replace(Regex("""(\d+(?:\.\d+)?)\s*%\s+of\s+(\d+(?:\.\d+)?|\w+)"""), "($2 * $1 / 100)")

        // "100 + 20%" → "(100 * (1 + 20 / 100))" = 120
        // "salary + 10%" → "(salary * (1 + 10 / 100))"
        result = result.replace(Regex("""(\d+(?:\.\d+)?|\w+)\s*\+\s*(\d+(?:\.\d+)?)\s*%"""), "($1 * (1 + $2 / 100))")

        // "100 - 15%" → "(100 * (1 - 15 / 100))" = 85
        // "budget - 25%" → "(budget * (1 - 25 / 100))"
        result = result.replace(Regex("""(\d+(?:\.\d+)?|\w+)\s*-\s*(\d+(?:\.\d+)?)\s*%"""), "($1 * (1 - $2 / 100))")

        return result
    }

    private fun preprocessCompositeOperations(expr: String): String {
        var result = expr.trim()

        // Match increment and decrement first (e.g. "a++", "b--")
        val incDecPattern = Regex("""^([a-zA-Z_][a-zA-Z0-9_]*)\s*(\+\+|--)$""")
        val incDecMatch = incDecPattern.find(result)
        if (incDecMatch != null) {
            val varName = incDecMatch.groupValues[1]
            val op = if (incDecMatch.groupValues[2] == "++") "+" else "-"
            return "$varName = $varName $op 1"
        }

        // Match compound assignments (e.g. "a += 5", "b -= 2", "c %= 3")
        val compoundPattern = Regex("""^([a-zA-Z_][a-zA-Z0-9_]*)\s*(\+=|-=|\*=|/=|%=)\s*(.+)$""")
        val compoundMatch = compoundPattern.find(result)
        if (compoundMatch != null) {
            val varName = compoundMatch.groupValues[1]
            val op = compoundMatch.groupValues[2].substring(0, 1) // +, -, *, /, %
            val rightSide = compoundMatch.groupValues[3]
            return "$varName = $varName $op ($rightSide)"
        }

        return result
    }


    /**
     * Calculate results for all lines in a file, maintaining variable state across lines.
     *
     * Processing pipeline:
     * - Strip comments (anything after #)
     * - Normalize Unicode operators (× → *, ÷ → /)
     * - Preprocess percentage expressions (% of, % off, +%, -%)
     * - Parse variable assignment (if present)
     * - Evaluate expression using exp4j
     * - Format result based on type
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
                val exprWithoutComments = stripComments(line.expression)
                if (exprWithoutComments.isBlank()) continue

                var processed = normalizeOperators(exprWithoutComments)
                processed = preprocessPercentages(processed)
                processed = preprocessCompositeOperations(processed)

                val parts = processed.split("=")
                if (parts.size != 2) continue
                val varName = parts[0].trim()
                val exprToEval = parts[1].trim()

                if (!varName.matches(Regex(Constants.VARIABLE_NAME_PATTERN))) continue

                val hasUndefined = VARIABLE_PATTERN.findAll(exprToEval).any { match ->
                    !variables.containsKey(match.value) && !BUILT_IN_IDENTIFIERS.contains(match.value.lowercase())
                }
                if (hasUndefined) continue

                val builder = ExpressionBuilder(exprToEval).implicitMultiplication(false)
                if (variables.isNotEmpty()) builder.variables(variables.keys.toSet())
                val expression = builder.build()
                if (variables.isNotEmpty()) variables.forEach { (k, v) -> expression.setVariable(k, v) }

                variables[varName] = expression.evaluate()
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
        val firstAffectedIndex = changedIndex.coerceIn(0, allLines.size)
        val precedingLines = allLines.subList(0, firstAffectedIndex)
        val affectedLines = allLines.subList(firstAffectedIndex, allLines.size)
        // Collect variable state from preceding lines, then fully recalculate affected lines
        val inheritedVariables = buildVariableState(precedingLines)
        return calculateWithVariables(affectedLines, inheritedVariables)
    }

    /**
     * Core evaluation loop: processes [lines] in order, building variable state from
     * [initialVariables] as assignments are encountered. A copy of [initialVariables] is made
     * on entry so the caller's map is never modified.
     */
    private fun calculateWithVariables(
        lines: List<LineEntity>,
        initialVariables: Map<String, Double>
    ): List<LineEntity> {
        val variables = initialVariables.toMutableMap()

        return lines.map { line ->
            if (line.expression.isBlank()) return@map line.copy(result = "")

            try {
                // Strip comments first
                val exprWithoutComments = stripComments(line.expression)
                if (exprWithoutComments.isBlank()) return@map line.copy(result = "")

                // Normalize and preprocess the expression
                var processed = normalizeOperators(exprWithoutComments)
                processed = preprocessPercentages(processed)
                processed = preprocessCompositeOperations(processed)

                // Parse variable assignment (e.g., price = 100)
                val parts = processed.split("=")
                val (varName, exprToEval) = if (parts.size == 2) {
                    parts[0].trim() to parts[1].trim()
                } else {
                    null to processed.trim()
                }

                // Validate variable name if this is an assignment
                if (varName != null && !varName.matches(Regex(Constants.VARIABLE_NAME_PATTERN))) {
                    return@map line.copy(result = "Err")
                }

                // Validate that expression doesn't contain undefined variables
                // This prevents issues like "rate2" being tokenized as "rate" + "2" by exp4j
                VARIABLE_PATTERN.findAll(exprToEval).forEach { match ->
                    val varRef = match.value
                    // Check if this looks like a variable but isn't defined or a built-in function
                    if (!variables.containsKey(varRef) && !BUILT_IN_IDENTIFIERS.contains(varRef.lowercase())) {
                        // It's an undefined variable - return error
                        return@map line.copy(result = "Err")
                    }
                }

                // Build expression with exp4j
                val builder = ExpressionBuilder(exprToEval)
                    .implicitMultiplication(false)  // Disable implicit multiplication

                // Add all existing variables to the expression context
                if (variables.isNotEmpty()) {
                    builder.variables(variables.keys.toSet())
                }

                val expression = builder.build()

                // Set variable values from previous lines
                if (variables.isNotEmpty()) {
                    variables.forEach { (key, value) ->
                        expression.setVariable(key, value)
                    }
                }

                val evalResult = expression.evaluate()

                // Store variable if this was an assignment
                if (varName != null) variables[varName] = evalResult

                // Format result for display
                val displayResult = if (evalResult % 1.0 == 0.0) {
                    // Whole number - choose format based on magnitude
                    if (evalResult >= Int.MIN_VALUE && evalResult <= Int.MAX_VALUE) {
                        evalResult.toInt().toString()  // e.g., "100"
                    } else if (evalResult >= Long.MIN_VALUE && evalResult <= Long.MAX_VALUE) {
                        evalResult.toLong().toString()  // e.g., "2000000000"
                    } else {
                        String.format("%.2e", evalResult)  // e.g., "1.23e+15"
                    }
                } else {
                    String.format("%.2f", evalResult)  // e.g., "3.33"
                }

                line.copy(result = displayResult)
            } catch (e: Exception) {
                line.copy(result = "Err")
            }
        }
    }
}
