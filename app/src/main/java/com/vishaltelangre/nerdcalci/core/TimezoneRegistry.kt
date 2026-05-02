package com.vishaltelangre.nerdcalci.core

import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Registry of supported timezone identifiers.
 * Powers both evaluation (resolve a ZoneId from user input) and autocomplete.
 */
object TimezoneRegistry {

    /**
     * Short alias → canonical IANA zone ID.
     * Kept minimal and high-quality. Extend as needed.
     */
    private val ALIASES: Map<String, String> = mapOf(
        "UTC"  to "UTC",
        "GMT"  to "GMT",
        "PST"  to "America/Los_Angeles",
        "PDT"  to "America/Los_Angeles",
        "MST"  to "America/Denver",
        "MDT"  to "America/Denver",
        "CST"  to "America/Chicago",
        "CDT"  to "America/Chicago",
        "EST"  to "America/New_York",
        "EDT"  to "America/New_York",
        "IST"  to "Asia/Kolkata",
        "AEST" to "Australia/Sydney",
        "AEDT" to "Australia/Sydney",
        "CET"  to "Europe/Paris",
        "CEST" to "Europe/Paris",
        "JST"  to "Asia/Tokyo",
        "HKT"  to "Asia/Hong_Kong",
        "SGT"  to "Asia/Singapore",
        "WIB"  to "Asia/Jakarta",
        "NZST" to "Pacific/Auckland",
        "NZDT" to "Pacific/Auckland",
        "BST"  to "Europe/London",
        "BRT"  to "America/Sao_Paulo"
    )

    /** All IANA zone IDs available on this JVM. Sorted alphabetically. */
    private val IANA_IDS: List<String> = ZoneId.getAvailableZoneIds().sorted()

    /**
     * All suggestion strings for autocomplete.
     * Aliases come first (they are shorter and more commonly typed),
     * then full IANA IDs. Duplicates removed.
     */
    val allSuggestions: List<String> =
        (ALIASES.keys.sorted() + IANA_IDS).distinct()

    /**
     * Resolves a user-supplied timezone string to a ZoneId.
     *
     * Resolution order:
     * 1. Alias lookup (e.g. "PST" → America/Los_Angeles)
     * 2. Direct IANA ID (e.g. "America/Chicago")
     * 3. Offset string: "GMT+530", "+05:30", "UTC-5", "UTC+5:30"
     *
     * Returns null if the string cannot be resolved.
     */
    fun resolve(input: String): ZoneId? {
        val trimmed = input.trim()

        // 1. Alias
        ALIASES[trimmed]?.let { return ZoneId.of(it) }

        // 2. Direct IANA
        return try {
            ZoneId.of(trimmed)
        } catch (_: Exception) {
            // 3. Offset string parsing: normalise "GMT+530" → "+05:30"
            parseOffsetString(trimmed)
        }
    }

    /**
     * Returns a friendly short name for the timezone.
     * Prefers common abbreviations (IST, PST), falls back to GMT offset (GMT+05:30).
     */
    fun getFriendlyName(zdt: java.time.ZonedDateTime): String {
        val zone = zdt.zone

        // 1. Try reverse lookup in our alias registry for a preferred short name.
        val registryAlias = ALIASES.entries.filter { it.value == zone.id }.map { it.key }.firstOrNull { alias ->
            // Heuristic: if multiple aliases exist (e.g. PST/PDT), match the DST state.
            if (zone.rules.isDaylightSavings(zdt.toInstant())) {
                alias.endsWith("DT") || alias.endsWith("S") // Summer time
            } else {
                !alias.endsWith("DT")
            }
        } ?: ALIASES.entries.find { it.value == zone.id }?.key

        if (registryAlias != null) return registryAlias

        // 2. Fallback to system display name.
        val displayName = zone.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)

        // If the short name is the same as the ID and contains a slash, it's a long IANA ID.
        // Also check if it's a raw offset ID like "+05:30".
        val isLongId = displayName.contains('/') || displayName.matches(Regex("""[+-]\d{2}:?\d{2}"""))

        return if (isLongId || displayName == zone.id) {
            val offset = zdt.offset
            if (offset == java.time.ZoneOffset.UTC) "UTC" else "GMT$offset"
        } else {
            displayName
        }
    }

    /**
     * Parses offset strings in various formats to a ZoneOffset.
     * Accepted formats: "+05:30", "-05:00", "GMT+530", "GMT+5:30", "UTC+530", "UTC-05:30".
     * Returns null if format is not recognised.
     */
    private fun parseOffsetString(input: String): ZoneOffset? {
        // Strip GMT/UTC prefix
        val stripped = input.removePrefix("GMT").removePrefix("UTC").trim()
        if (stripped.isEmpty()) return ZoneOffset.UTC

        val sign = if (stripped.startsWith('-')) -1 else 1
        val digits = stripped.trimStart('+', '-').replace(":", "")
        if (digits.length !in 1..6 || !digits.all(Char::isDigit)) return null

        return try {
            when (digits.length) {
                1, 2 -> ZoneOffset.ofHours(sign * digits.toInt())
                3    -> ZoneOffset.ofHoursMinutes(sign * digits.substring(0, 1).toInt(), digits.substring(1).toInt())
                4    -> ZoneOffset.ofHoursMinutes(sign * digits.substring(0, 2).toInt(), digits.substring(2).toInt())
                5    -> ZoneOffset.ofHoursMinutesSeconds(sign * digits.substring(0, 1).toInt(), digits.substring(1, 3).toInt(), digits.substring(3).toInt())
                6    -> ZoneOffset.ofHoursMinutesSeconds(sign * digits.substring(0, 2).toInt(), digits.substring(2, 4).toInt(), digits.substring(4).toInt())
                else -> null
            }
        } catch (_: Exception) { null }
    }
}
