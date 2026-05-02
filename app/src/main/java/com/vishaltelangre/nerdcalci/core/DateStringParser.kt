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

object DateStringParser {

    private val systemZone: ZoneId get() = ZoneId.systemDefault()

    /**
     * Parses a string into a DateTimeResult.Date using only unambiguous formats.
     * Month names are case-insensitive.
     *
     * Accepted formats:
     *   "June 10"          → current/inferred year, month-day order
     *   "10 June"          → same, day-month order
     *   "April 1, 2019"    → full US-style, year included
     *   "1 April 2019"     → full day-month-year
     *   "2019-04-01"       → ISO 8601
     *   "2019/04/01"       → YYYY/MM/DD
     *
     * Rejected formats (throw EvalException):
     *   "12/02/1988"       → ambiguous (DD/MM or MM/DD?)
     *   "01.05.2005"       → ambiguous
     *
     * @throws EvalException with actionable message on parse failure.
     */
    fun parse(input: String): DateTimeResult {
        val s = input.trim()

        // Reject ambiguous numeric-only formats explicitly before trying anything else
        if (s.matches(Regex("""\d{1,2}[/\.]\d{1,2}[/\.]\d{2,4}"""))) {
            throw EvalException(
                "Ambiguous date format \"$s\". Use a named month (e.g. \"April 1, 2019\") or ISO 8601 (e.g. \"2019-04-01\") instead."
            )
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
                    DateTimeResult.Date(date)
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
            } catch (e: DateTimeParseException) {
                throw EvalException("Invalid date/time \"$s\": ${e.message}")
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
     * Parses a Unix epoch seconds integer into a DateTimeResult.Date using the system timezone.
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
        val candidateThisYear = try { LocalDate.of(thisYear, month, day) } catch (_: Exception) { return thisYear + 1 }
        return if (candidateThisYear.isBefore(today.minusMonths(6))) thisYear + 1 else thisYear
    }
}
