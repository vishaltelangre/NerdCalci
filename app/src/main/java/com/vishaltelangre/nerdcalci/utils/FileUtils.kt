package com.vishaltelangre.nerdcalci.utils

import android.os.Environment
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
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
     * Calculates a SHA-256 hash by streaming the content from an [InputStream].
     */
    fun calculateHash(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = inputStream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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

            if (expr.isEmpty() || rawResult.isBlank() || rawResult == "Err" || expr.trimStart().startsWith("#")) {
                expr
            } else if (shouldShowResult(expr)) {
                val resultJson = JSONObject().apply { put("result", displayResult) }.toString()
                "$expr # $resultJson"
            } else {
                expr
            }
        }
    }

    /**
     * Parses plain text content into expressions list and metadata.
     */
    fun parseFileContent(content: String): ParsedFileContent {
        val lines = content.lines()
        var currentMetadata = FileMetadata()
        val dataLines = mutableListOf<String>()
        var metadataSet = false

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

        // Regex to handle: [expression with optional comment] # {"result":"..."}
        val structuredResultRegex = Regex("""^(.*)\s*#\s*(\{\s*"result"\s*:\s*".*?"\s*\})\s*$""")

        val expressions = dataLines.map { line ->
            val match = structuredResultRegex.matchEntire(line)
            if (match != null) {
                match.groupValues[1].trim()
            } else {
                // FALLBACK: Legacy parsing logic for older files
                val lastHashIndex = line.lastIndexOf('#')
                if (lastHashIndex >= 0) {
                    val exprCandidate = line.substring(0, lastHashIndex).trim()
                    val potentialResult = line.substring(lastHashIndex + 1).trim()

                    // Use looksLikeResult which now handles units with special characters
                    if (looksLikeResult(potentialResult) && shouldShowResult(exprCandidate)) {
                        exprCandidate
                    } else if (potentialResult.isEmpty() && lastHashIndex > 0 && shouldShowResult(exprCandidate)) {
                        // Handle "expr #" case
                        exprCandidate
                    } else {
                        line.trim()
                    }
                } else if (looksLikeResult(line.trim())) {
                    // Omit standalone result lines
                    null
                } else {
                    line.trim()
                }
            }
        }.filterNotNull()

        return ParsedFileContent(expressions, currentMetadata)
    }

    private fun looksLikeResult(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed == "Err") return true

        // Hex, Oct, Bin
        if (trimmed.startsWith("0x") || trimmed.startsWith("0b") || trimmed.startsWith("0o")) {
            val content = trimmed.substring(2)
            if (content.isNotEmpty() && content.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                return true
            }
        }

        // Sanitize all safe separators (including spaces, NBSP, etc.) from the whole text
        val sanitized = text.filter { it !in MathEngine.SAFE_SEPARATORS }
        if (sanitized.isEmpty()) return false

        // Check for fractions: digits/digits
        if (sanitized.count { it == '/' } == 1) {
            val parts = sanitized.split('/')
            if (parts.all { p -> p.all { it.isDigit() || it == '-' || it == '.' } }) {
                return true
            }
        }

        // Check for scientific notation: digitsEdigits
        if (sanitized.contains('E', ignoreCase = true)) {
            val s = sanitized.uppercase()
            if (s.count { it == 'E' } == 1) {
                val parts = s.split('E')
                if (parts[0].replace(Regex("[.-]"), "").all { it.isDigit() } &&
                    parts[1].replace(Regex("[+-]"), "").all { it.isDigit() }) {
                    return true
                }
            }
        }

        // Check for pure numeric or number + units
        // We split by whitespace to detect potential units
        val words = trimmed.split(Regex("\\s+"))
        if (words.isNotEmpty()) {
            val firstWordSanitized = words[0].filter { it !in MathEngine.SAFE_SEPARATORS }
            if (firstWordSanitized.all { it.isDigit() || it == '-' || it == '.' }) {
                // It's a result if it's just the number, or followed by a unit (all letters)
                // Note: if the number had internal spaces (Swiss style), words.size > 1.
                // We re-check the whole sanitized string for pure numeric case.
                if (sanitized.all { it.isDigit() || it == '-' || it == '.' }) return true

                // Units case - use UnitConverter to validate if there's a potential unit
                if (words.size == 2) {
                    val potentialUnit = words[1]
                    // Check if it's a valid unit or looks like one (letters, or contains common unit chars like °, ², ³)
                    if (com.vishaltelangre.nerdcalci.core.UnitConverter.findUnit(potentialUnit) != null) {
                        return true
                    }
                    // Fallback: accept if it looks like a unit (letters with optional superscripts/symbols)
                    if (potentialUnit.all { it.isLetter() || it in "°²³" }) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun shouldShowResult(expression: String): Boolean {
        return expression.isNotBlank()
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