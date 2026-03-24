package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

object FileUtils {
    /**
     * Formats lines into a plain text string, appending results as comments where appropriate.
     */
    fun formatFileContent(lines: List<LineEntity>, precision: Int): String {
        return lines.joinToString("\n") { line ->
            val expr = line.expression.trim()
            val rawResult = line.result.trim()
            val displayResult = MathEngine.formatDisplayResult(rawResult, precision)

            when {
                expr.isEmpty() || rawResult.isBlank() || rawResult == "Err" -> expr
                expr.trimStart().startsWith("#") -> expr // Full comment line
                shouldShowResult(expr) -> "$expr # $displayResult"
                else -> expr
            }
        }
    }

    fun shouldShowResult(expression: String): Boolean {
        val hasOperators = expression.any { it in "+-*/%^" }
        val simpleAssignmentRegex = Regex("""^\s*[a-zA-Z][a-zA-Z0-9\s]*\s*=\s*[\d.]+\s*$""")
        if (simpleAssignmentRegex.matches(expression)) return false
        return hasOperators || !expression.contains("=")
    }
}
