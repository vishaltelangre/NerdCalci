package com.vishaltelangre.nerdcalci.utils

import android.net.Uri
import android.os.Environment
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

import java.security.MessageDigest

data class FileMetadata(
    val version: Int = 1,
    val id: String? = null,
    val isPinned: Boolean = false,
    val lastModified: Long = -1L,
    val createdAt: Long = -1L,
    val contentHash: String? = null
)

data class ParsedFileContent(
    val expressions: List<String>,
    val metadata: FileMetadata
)

object FileUtils {
    /**
     * Calculates a SHA-256 hash of the given content.
     */
    fun calculateHash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Reads only the metadata header from a [reader] relative to the current position.
     * Consumes one line from the reader. This is much faster than parsing the entire file.
     */
    fun readMetadataHeader(reader: BufferedReader): FileMetadata? {
        val firstLine = reader.readLine() ?: return null
        return parseMetadataHeader(firstLine)
    }

    private fun parseMetadataHeader(line: String): FileMetadata? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("# @metadata ")) return null
        return try {
            val jsonStr = trimmed.removePrefix("# @metadata ").trim()
            val json = JSONObject(jsonStr)
            FileMetadata(
                version = json.optInt("version", 1),
                id = if (json.has("id")) json.getString("id") else null,
                isPinned = json.optBoolean("isPinned", false),
                lastModified = json.optLong("lastModified", -1L),
                createdAt = json.optLong("createdAt", -1L),
                contentHash = if (json.has("contentHash")) json.getString("contentHash") else null
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats lines into a plain text string, appending results as comments and inserting metadata headers if provided.
     */
    fun formatFileContent(lines: List<LineEntity>, precision: Int, metadata: FileMetadata? = null): String {
        val sb = StringBuilder()
        val body = formatFileBody(lines, precision)

        metadata?.let { meta ->
            val json = JSONObject().apply {
                put("version", meta.version)
                meta.id?.let { put("id", it) }
                if (meta.isPinned) put("isPinned", true)
                if (meta.lastModified != -1L) put("lastModified", meta.lastModified)
                if (meta.createdAt != -1L) put("createdAt", meta.createdAt)
                meta.contentHash?.let { put("contentHash", it) }
            }
            sb.append("# @metadata ").append(json.toString()).append("\n")
        }

        sb.append(body)
        return sb.toString()
    }

    fun formatFileBody(lines: List<LineEntity>, precision: Int): String {
        return lines.joinToString("\n") { line ->
            val expr = line.expression.trim()
            val rawResult = line.result.trim()
            val displayResult = MathEngine.formatDisplayResult(rawResult, precision, java.util.Locale.ROOT)

            when {
                expr.isEmpty() || rawResult.isBlank() || rawResult == "Err" -> expr
                expr.trimStart().startsWith("#") -> expr // Full comment line
                shouldShowResult(expr) -> "$expr # $displayResult"
                else -> expr
            }
        }
    }

    /**
     * Parses plain text content into expressions list and metadata.
     */
    fun parseFileContent(content: String): ParsedFileContent {
        val lines = content.lines()
        var currentMetadata = FileMetadata()
        var metadataSet = false
        val dataLines = mutableListOf<String>()

        for (line in lines) {
            if (line.trim().startsWith("# @metadata ")) {
                if (!metadataSet) {
                    parseMetadataHeader(line)?.let {
                        currentMetadata = it
                        metadataSet = true
                    }
                }
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

        return ParsedFileContent(expressions, currentMetadata)
    }

    fun shouldShowResult(expression: String): Boolean {
        val hasOperators = expression.any { it in "+-*/%^" }
        val simpleAssignmentRegex = Regex("""^\s*[a-zA-Z][a-zA-Z0-9\s]*\s*=\s*[\d.]+\s*$""")
        if (simpleAssignmentRegex.matches(expression)) return false
        return hasOperators || !expression.contains("=")
    }

    /**
     * Translates SAF-specific path prefixes (e.g., "primary:") into human-readable absolute paths.
     *
     * IMPORTANT: This is intended for DISPLAY PURPOSES ONLY (UI, logs).
     * Do NOT use the returned path for actual file I/O logic, as direct file access
     * is restricted on modern Android versions (Scoped Storage). Always use [Uri]
     * and [android.content.ContentResolver] for file operations.
     */
    fun formatPathForDisplay(path: String?): String {
        return formatPathForDisplayInternal(path, Environment.getExternalStorageDirectory().absolutePath)
    }

    internal fun formatPathForDisplayInternal(path: String?, rootPath: String): String {
        if (path == null || path.isBlank()) return ""

        return if (path.startsWith("primary:")) {
            "$rootPath/${path.removePrefix("primary:")}"
        } else {
            val parts = path.split(":", limit = 2)
            if (parts.size == 2) {
                // Secondary storage (SD card)
                "/storage/${parts[0]}/${parts[1]}"
            } else {
                path
            }
        }
    }
}
