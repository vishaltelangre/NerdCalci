package com.vishaltelangre.nerdcalci.core

import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Registry of supported timezone identifiers.
 * Powers both evaluation (resolve a ZoneId from user input) and autocomplete.
 */
object TimezoneRegistry {

    /**
     * Per-zone explicit standard/daylight abbreviation pair.
     * standard = abbreviation when DST is NOT in effect.
     * daylight = abbreviation when DST IS in effect.
     */
    private data class ZoneAbbr(val standard: String, val daylight: String)

    /**
     * IANA zone ID → explicit abbreviation pair.
     * Both aliases (PST/PDT, etc.) are derived from here for autocomplete and display.
     */
    private val ZONE_ABBR: Map<String, ZoneAbbr> = mapOf(
        "UTC"                  to ZoneAbbr("UTC",  "UTC"),
        "GMT"                  to ZoneAbbr("GMT",  "GMT"),
        "America/Los_Angeles"  to ZoneAbbr("PST",  "PDT"),
        "America/Denver"       to ZoneAbbr("MST",  "MDT"),
        "America/Chicago"      to ZoneAbbr("CST",  "CDT"),
        "America/New_York"     to ZoneAbbr("EST",  "EDT"),
        "Asia/Kolkata"         to ZoneAbbr("IST",  "IST"),
        "Australia/Sydney"     to ZoneAbbr("AEST", "AEDT"),
        "Europe/Paris"         to ZoneAbbr("CET",  "CEST"),
        "Asia/Tokyo"           to ZoneAbbr("JST",  "JST"),
        "Asia/Hong_Kong"       to ZoneAbbr("HKT",  "HKT"),
        "Asia/Singapore"       to ZoneAbbr("SGT",  "SGT"),
        "Asia/Jakarta"         to ZoneAbbr("WIB",  "WIB"),
        "Pacific/Auckland"     to ZoneAbbr("NZST", "NZDT"),
        "Europe/London"        to ZoneAbbr("GMT",  "BST"),
        "America/Sao_Paulo"    to ZoneAbbr("BRT",  "BRST")
    )

    /**
     * Short alias → canonical IANA zone ID.
     * Derived from ZONE_ABBR so there is a single source of truth.
     * When two zones share an abbreviation, the first entry in ZONE_ABBR wins.
     */
    private val ALIASES: Map<String, String> = buildMap {
        ZONE_ABBR.forEach { (zoneId, abbr) ->
            putIfAbsent(abbr.standard, zoneId)
            if (abbr.daylight != abbr.standard) putIfAbsent(abbr.daylight, zoneId)
        }
    }

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
        val isDst = zone.rules.isDaylightSavings(zdt.toInstant())

        // 1. Try explicit DST-aware lookup in ZONE_ABBR.
        val abbr = ZONE_ABBR[zone.id]
        if (abbr != null) {
            return if (isDst) abbr.daylight else abbr.standard
        }

        // 2. Fallback to JDK short display name.
        val displayName = zone.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())

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
