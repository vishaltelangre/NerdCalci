package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.json.JSONObject

data class FileMetadata(
    val isPinned: Boolean = false,
    val lastModified: Long = -1L
)

data class ParsedFileContent(
    val expressions: List<String>,
    val metadata: FileMetadata
)

object FileUtils {
    /**
     * Formats lines into a plain text string, appending results as comments and inserting metadata headers if provided.
     */
    fun formatFileContent(lines: List<LineEntity>, precision: Int, metadata: FileMetadata? = null): String {
        val sb = StringBuilder()

        metadata?.let {
            if (it.isPinned || it.lastModified != -1L) {
                val json = JSONObject()
                if (it.isPinned) json.put("isPinned", true)
                if (it.lastModified != -1L) json.put("lastModified", it.lastModified)
                sb.append("# @metadata ").append(json.toString()).append("\n")
            }
        }

        val body = lines.joinToString("\n") { line ->
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
        sb.append(body)
        return sb.toString()
    }

    /**
     * Parses plain text content into expressions list and metadata.
     */
    fun parseFileContent(content: String): ParsedFileContent {
        val lines = content.lines()
        var isPinned = false
        var lastModified = -1L
        val dataLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("# @metadata ")) {
                try {
                    val jsonStr = trimmed.removePrefix("# @metadata ").trim()
                    val json = JSONObject(jsonStr)
                    isPinned = json.optBoolean("isPinned", false)
                    lastModified = json.optLong("lastModified", -1L)
                } catch (_: Exception) {}
            } else {
                dataLines.add(line)
            }
        }

        val expressions = dataLines.map { line ->
            val lastHashIndex = line.lastIndexOf('#')
            if (lastHashIndex > 0) {
                val exprCandidate = line.substring(0, lastHashIndex).trim()
                val potentialResult = line.substring(lastHashIndex + 1).trim()
                val isResult = potentialResult == "Err" || potentialResult.toDoubleOrNull() != null

                if (isResult && shouldShowResult(exprCandidate)) {
                    exprCandidate
                } else {
                    line.trim()
                }
            } else {
                line.trim()
            }
        }

        return ParsedFileContent(expressions, FileMetadata(isPinned, lastModified))
    }

    fun shouldShowResult(expression: String): Boolean {
        val hasOperators = expression.any { it in "+-*/%^" }
        val simpleAssignmentRegex = Regex("""^\s*[a-zA-Z][a-zA-Z0-9\s]*\s*=\s*[\d.]+\s*$""")
        if (simpleAssignmentRegex.matches(expression)) return false
        return hasOperators || !expression.contains("=")
    }
}
