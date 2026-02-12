package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import net.objecthunter.exp4j.ExpressionBuilder

object MathEngine {
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

    // Preprocess variable names
    // Note: exp4j doesn't support spaces in variable names, so we normalize them
    // Examples:
    //   "monthly salary = 5000" → "monthly_salary = 5000"
    //   "base price + 100" → "base_price + 100"
    private fun preprocessVariableNames(expr: String): String {
        return expr.replace(Regex("""([a-zA-Z][a-zA-Z0-9\s]+?)(\s*[=+\-*/^()])""")) { matchResult ->
            val varName = matchResult.groupValues[1].trim()
            val operator = matchResult.groupValues[2]
            varName.replace(" ", "_") + operator
        }
    }

    /**
     * Calculate results for all lines in a file, maintaining variable state across lines.
     *
     * Processing pipeline:
     * - Strip comments (anything after #)
     * - Normalize Unicode operators (× → *, ÷ → /)
     * - Preprocess variable names (spaces → underscores)
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
        val variables = mutableMapOf<String, Double>()

        return lines.map { line ->
            if (line.expression.isBlank()) return@map line.copy(result = "")

            try {
                // Strip comments first
                val exprWithoutComments = stripComments(line.expression)
                if (exprWithoutComments.isBlank()) return@map line.copy(result = "")

                // Normalize and preprocess the expression
                var processed = normalizeOperators(exprWithoutComments)
                processed = preprocessVariableNames(processed)
                processed = preprocessPercentages(processed)

                // Parse variable assignment (e.g., price = 100)
                val parts = processed.split("=")
                val (varName, exprToEval) = if (parts.size == 2) {
                    parts[0].trim() to parts[1].trim()
                } else {
                    null to processed.trim()
                }

                // Build expression with exp4j
                val builder = ExpressionBuilder(exprToEval)

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
