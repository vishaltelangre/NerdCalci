package com.vishaltelangre.nerdcalci.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import com.vishaltelangre.nerdcalci.utils.RegionUtils

object DateStringParser {

    private val systemZone: ZoneId get() = ZoneId.systemDefault()

    /**
     * Parses a string into a DateTimeResult.Date.
     * Month names are case-insensitive.
     *
     * Accepted formats:
     *   "June 10"          → current/inferred year, month-day order
     *   "10 June"          → same, day-month order
     *   "April 1, 2019"    → full US-style, year included
     *   "1 April 2019"     → full day-month-year
     *   "2019-04-01"       → ISO 8601
     *   "2019/04/01"       → YYYY/MM/DD
     *   "10/05/2024"       → Numeric format based on [dateFormat] setting
     *
     * @throws EvalException with actionable message on parse failure.
     */
    fun parse(input: String, dateFormat: String = Constants.DATE_FORMAT_AUTO): DateTimeResult {
        val s = input.trim()

        val resolvedFormat = if (dateFormat == Constants.DATE_FORMAT_AUTO) {
            RegionUtils.getDefaultDateFormat(Locale.getDefault())
        } else {
            dateFormat
        }

        // ISO 8601 (or similar): YYYY-MM-DD or YYYY/MM/DD, optionally with T and time and offset
        val isoPattern = Regex("""(\d{4}[-/]\d{2}[-/]\d{2})(?:[T ](\d{2}:\d{2}(?::\d{2})?(?:\.\d+)?))?(Z|[+-]\d{2}(?::?\d{2})?)?""")
        isoPattern.matchEntire(s)?.let { m ->
            val datePart = m.groupValues[1].replace('/', '-')
            val timePart = m.groupValues[2]
            val offsetPart = m.groupValues[3]
            
            return try {
                val date = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
                if (timePart.isEmpty()) {
                    if (offsetPart.isEmpty()) {
                        DateTimeResult.Date(date)
                    } else {
                        // Offset present but no time: treat as midnight in that zone.
                        val zone = if (offsetPart == "Z") ZoneOffset.UTC else ZoneId.of(offsetPart)
                        DateTimeResult.DateTime(date.atTime(LocalTime.MIDNIGHT).atZone(zone))
                    }
                } else {
                    val time = LocalTime.parse(timePart)
                    val ldt = date.atTime(time)
                    if (offsetPart.isEmpty()) {
                        DateTimeResult.DateTime(ldt.atZone(systemZone))
                    } else {
                        val zone = if (offsetPart == "Z") ZoneOffset.UTC else ZoneId.of(offsetPart)
                        DateTimeResult.DateTime(ldt.atZone(zone))
                    }
                }
            } catch (e: java.time.DateTimeException) {
                throw EvalException("Invalid date/time \"$s\": ${e.message}")
            }
        }

        // Numeric-only formats (e.g. 10/05/2024, 2024-10-05, 10.05.2024)
        // We support /, -, and . as separators. Year must be 4 digits.
        val numericPattern = Regex("""^(\d{1,4})[/\.-](\d{1,2})[/\.-](\d{1,4})$""")
        numericPattern.matchEntire(s)?.let { m ->
            val p1 = m.groupValues[1]
            val p2 = m.groupValues[2]
            val p3 = m.groupValues[3]

            val formatHint = when (resolvedFormat) {
                Constants.DATE_FORMAT_DMY -> "DD/MM/YYYY"
                Constants.DATE_FORMAT_MDY -> "MM/DD/YYYY"
                Constants.DATE_FORMAT_YMD -> "YYYY/MM/DD"
                else -> ""
            }

            try {
                return when (resolvedFormat) {
                    Constants.DATE_FORMAT_DMY -> {
                        if (p3.length != 4) throw EvalException("Year must be 4 digits in numeric date \"$s\" (expected $formatHint).")
                        buildDate(p3.toInt(), p2.toInt(), p1.toInt(), s)
                    }
                    Constants.DATE_FORMAT_MDY -> {
                        if (p3.length != 4) throw EvalException("Year must be 4 digits in numeric date \"$s\" (expected $formatHint).")
                        buildDate(p3.toInt(), p1.toInt(), p2.toInt(), s)
                    }
                    Constants.DATE_FORMAT_YMD -> {
                        if (p1.length != 4) throw EvalException("Year must be 4 digits in numeric date \"$s\" (expected $formatHint).")
                        buildDate(p1.toInt(), p2.toInt(), p3.toInt(), s)
                    }
                    else -> throw EvalException("Unknown date format setting.")
                }
            } catch (e: EvalException) {
                if (formatHint.isNotEmpty() && e.message?.contains("expected") == false) {
                    throw EvalException("${e.message} (expected $formatHint)")
                }
                throw e
            }
        }

        // Named-month patterns. Month can be short or full, case-insensitive.
        // Format: "Month D" or "Month D, YYYY"
        val monthFirstPattern = Regex(
            """^([A-Za-z]+)\s+(\d{1,2})(?:,?\s+(\d{4}))?$"""
        )
        monthFirstPattern.matchEntire(s)?.let { m ->
            val monthNum = DateKeywords.monthNumber(m.groupValues[1])
                ?: throw EvalException("Unknown month name \"${m.groupValues[1]}\".")
            val day = m.groupValues[2].toInt()
            val year = m.groupValues[3].toIntOrNull()
            return buildDate(year, monthNum, day, s)
        }

        // Format: "D Month" or "D Month YYYY"
        val dayFirstPattern = Regex(
            """^(\d{1,2})\s+([A-Za-z]+)(?:\s+(\d{4}))?$"""
        )
        dayFirstPattern.matchEntire(s)?.let { m ->
            val day = m.groupValues[1].toInt()
            val monthNum = DateKeywords.monthNumber(m.groupValues[2])
                ?: throw EvalException("Unknown month name \"${m.groupValues[2]}\".")
            val year = m.groupValues[3].toIntOrNull()
            return buildDate(year, monthNum, day, s)
        }

        throw EvalException(
            "Cannot parse date \"$s\". Use a named month (e.g. \"April 1, 2019\") or ISO 8601 (e.g. \"2019-04-01\")."
        )
    }

    /**
     * Parses a Unix epoch seconds integer into a DateTimeResult.DateTime using the system timezone.
     */
    fun parseEpoch(epochSeconds: Long): DateTimeResult.DateTime {
        val instant = Instant.ofEpochSecond(epochSeconds)
        return DateTimeResult.DateTime(instant.atZone(systemZone))
    }

    /**
     * Infers the year when omitted.
     *
     * Rule:
     *   - Compute the candidate date for the current year.
     *   - If that date is within the past 6 months, use current year.
     *   - If that date is more than 6 months in the past, use next year.
     *   - If that date is in the future (any amount), use current year.
     *
     * This matches general Soulver behavior: "the occurrence closest to today;
     * prefer future when equidistant".
     */
    private fun buildDate(year: Int?, month: Int, day: Int, original: String): DateTimeResult {
        // Guard against impossible month/day combinations before year inference.
        if (month !in 1..12) {
            throw EvalException("Invalid date \"$original\": month $month is out of range.")
        }
        val maxDays = when (month) {
            2 -> 29 // Allow 29 for potential leap years
            4, 6, 9, 11 -> 30
            else -> 31
        }
        if (day < 1 || day > maxDays) {
            throw EvalException("Invalid date \"$original\": day $day is out of range for month $month.")
        }

        val resolvedYear = year ?: inferYear(month, day)
        return try {
            DateTimeResult.Date(LocalDate.of(resolvedYear, month, day))
        } catch (e: Exception) {
            throw EvalException("Invalid date \"$original\": day $day is out of range for month $month.")
        }
    }

    private fun inferYear(month: Int, day: Int): Int {
        val today = LocalDate.now(ZoneId.systemDefault())
        val thisYear = today.year

        // Find the first year >= thisYear for which the date is valid (handles Feb 29, etc.).
        val candidateDate = findFirstValidDate(thisYear, month, day)

        // If the first valid candidate is more than 6 months in the past, try the next valid year.
        return if (candidateDate.isBefore(today.minusMonths(6))) {
            findFirstValidDate(candidateDate.year + 1, month, day).year
        } else {
            candidateDate.year
        }
    }

    /** Returns the first LocalDate on or after the given year for which (year, month, day) is valid. */
    private fun findFirstValidDate(startYear: Int, month: Int, day: Int): LocalDate {
        var year = startYear
        // Defensive limit: don't search more than 10,000 years to prevent infinite loops
        // on impossible dates (e.g., June 31).
        val limit = startYear + 10000
        while (year < limit) {
            try {
                return LocalDate.of(year, month, day)
            } catch (_: java.time.DateTimeException) {
                year++
            }
        }
        throw EvalException("Invalid date: day $day is never valid for month $month.")
    }
}
