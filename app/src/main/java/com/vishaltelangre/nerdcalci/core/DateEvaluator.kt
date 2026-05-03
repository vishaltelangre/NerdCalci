package com.vishaltelangre.nerdcalci.core

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.math.BigDecimal
import java.util.Locale

object DateEvaluator {

    private val systemZone: ZoneId get() = ZoneId.systemDefault()
    private val today: LocalDate get() = LocalDate.now(systemZone)
    private val now: ZonedDateTime get() = ZonedDateTime.now(systemZone)

    // ── Relative keyword resolution ──────────────────────────────────────────

    /**
     * Resolves a relative keyword to a DateTimeResult.
     * Called by Evaluator.resolveVariable() when variable name is a date keyword.
     *
     * "today"     → DateTimeResult.Date(LocalDate.now(systemZone))
     * "yesterday" → DateTimeResult.Date(LocalDate.now(systemZone).minusDays(1))
     * "tomorrow"  → DateTimeResult.Date(LocalDate.now(systemZone).plusDays(1))
     * "now"       → DateTimeResult.DateTime(ZonedDateTime.now(systemZone))
     */
    fun resolveRelativeKeyword(keyword: String): DateTimeResult = when (keyword) {
        "today"     -> DateTimeResult.Date(today)
        "yesterday" -> DateTimeResult.Date(today.minusDays(1))
        "tomorrow"  -> DateTimeResult.Date(today.plusDays(1))
        "now"       -> DateTimeResult.DateTime(now)
        else        -> throw EvalException("Unknown relative date keyword: $keyword")
    }

    // ── Date arithmetic ──────────────────────────────────────────────────────

    /**
     * Adds or subtracts a DateTimeDelta from a DateTimeResult.
     *
     * Rules:
     * - Date + delta with only date components (years/months/weeks/days) → DateTimeResult.Date
     * - Date + delta with time components (hours/minutes/seconds) → DateTimeResult.DateTime (midnight + time)
     * - DateTime + any delta → DateTimeResult.DateTime
     *
     * Month arithmetic: add months first via plusMonths(), then pin to the last valid day of the
     * resulting month. E.g., January 31 + 1 month → February 28/29 (last day of February).
     *
     * @param base The base DateTimeResult (Date or DateTime).
     * @param delta The DateTimeDelta to apply.
     * @param isAdd true for addition, false for subtraction.
     * @return A new DateTimeResult with the delta applied.
     */
    fun applyDelta(base: DateTimeResult, delta: DateTimeDelta, isAdd: Boolean): DateTimeResult {
        val sign = if (isAdd) 1L else -1L

        return when (base) {
            is DateTimeResult.Date -> {
                var d = base.date
                d = d.plusYears(sign * delta.years)
                d = d.plusMonths(sign * delta.months)
                // plusMonths already pins to last day of month in java.time
                d = d.plusWeeks(sign * delta.weeks)
                d = d.plusDays(sign * delta.days)

                val hasTime = delta.hours != 0L || delta.minutes != 0L || delta.seconds != 0L
                if (hasTime) {
                    // Convert date to midnight ZonedDateTime in system zone, then add time
                    var zdt = d.atStartOfDay(systemZone)
                    zdt = zdt.plusHours(sign * delta.hours)
                    zdt = zdt.plusMinutes(sign * delta.minutes)
                    zdt = zdt.plusSeconds(sign * delta.seconds)
                    DateTimeResult.DateTime(zdt)
                } else {
                    DateTimeResult.Date(d)
                }
            }
            is DateTimeResult.DateTime -> {
                var zdt = base.instant
                zdt = zdt.plusYears(sign * delta.years)
                zdt = zdt.plusMonths(sign * delta.months)
                zdt = zdt.plusWeeks(sign * delta.weeks)
                zdt = zdt.plusDays(sign * delta.days)
                zdt = zdt.plusHours(sign * delta.hours)
                zdt = zdt.plusMinutes(sign * delta.minutes)
                zdt = zdt.plusSeconds(sign * delta.seconds)
                DateTimeResult.DateTime(zdt)
            }
            else -> throw EvalException("Cannot apply a time delta to a duration or day-count result.")
        }
    }

    // ── Interval computation ─────────────────────────────────────────────────

    /**
     * Computes the interval between two DateTimeResult values.
     * Returns DateTimeResult.Duration for general intervals,
     * or DateTimeResult.DayCount when `inDays = true` (for "through ... in days").
     *
     * @param inclusive If true, the end date is treated as +1 day (for "through" keyword).
     * @param inDays If true, returns DayCount instead of Duration.
     */
    fun interval(from: DateTimeResult, to: DateTimeResult, inclusive: Boolean = false, projectionUnit: String? = null): DateTimeResult {
        // When projecting to a time unit, use full instant precision if either operand has a time component.
        if (projectionUnit != null) {
            val unit = UnitConverter.findUnit(projectionUnit) ?: throw EvalException("Unknown unit `$projectionUnit`")
            if (unit.category != UnitCategory.TIME) throw EvalException("Cannot project date interval to non-time unit `${unit.name}`")

            val hasDateTime = from is DateTimeResult.DateTime || to is DateTimeResult.DateTime
            if (hasDateTime) {
                // Derive instants from DateTime operands; fall back to start-of-day for plain Date.
                val startInstant: java.time.Instant = when (from) {
                    is DateTimeResult.DateTime -> from.instant.toInstant()
                    is DateTimeResult.Date     -> from.date.atStartOfDay(systemZone).toInstant()
                    else -> throw EvalException("Expected a date or datetime operand.")
                }
                val rawEndInstant: java.time.Instant = when (to) {
                    is DateTimeResult.DateTime -> to.instant.toInstant()
                    is DateTimeResult.Date     -> to.date.atStartOfDay(systemZone).toInstant()
                    else -> throw EvalException("Expected a date or datetime operand.")
                }
                // Apply inclusive (+1 day) only when projecting to day-unit output
                // OR when the end operand is a plain date (so "through tomorrow"
                // includes all of tomorrow). For precise DateTime endpoints, we
                // do NOT pad the end instant.
                val endInstant = if (inclusive && (projectionUnit.lowercase() in setOf("d", "day", "days") || to is DateTimeResult.Date))
                    rawEndInstant.plusSeconds(86400L) else rawEndInstant

                val duration = java.time.Duration.between(startInstant, endInstant)
                val totalSeconds = duration.seconds

                if (projectionUnit.lowercase() in setOf("d", "day", "days")) {
                    return DateTimeResult.DayCount(totalSeconds / 86400L)
                }

                val value = UnitConverter.fromBase(BigDecimal.valueOf(totalSeconds), unit, emptyMap<String, EvaluationResult>()).toLong()
                return DateTimeResult.TimeCount(value, unit.symbols.first())
            } else {
                // Both operands are plain dates — use the original LocalDate-based path.
                val fromDate = toLocalDate(from)
                val rawToDate = toLocalDate(to)
                val toDate = if (inclusive) rawToDate.plusDays(1) else rawToDate

                val totalDays = ChronoUnit.DAYS.between(fromDate, toDate)
                if (projectionUnit.lowercase() in setOf("d", "day", "days")) {
                    return DateTimeResult.DayCount(totalDays)
                }

                val totalSeconds = totalDays * 24 * 3600L
                val value = UnitConverter.fromBase(BigDecimal.valueOf(totalSeconds), unit, emptyMap<String, EvaluationResult>()).toLong()
                return DateTimeResult.TimeCount(value, unit.symbols.first())
            }
        }

        // Non-projection path: return a DateTimeDelta (Duration or Duration with time components).
        val hasDateTime = from is DateTimeResult.DateTime || to is DateTimeResult.DateTime

        if (hasDateTime) {
            // Preserve full instant precision so datetime-to-datetime intervals include time.
            val fromZdt = toZonedDateTime(from)
            val rawToZdt = toZonedDateTime(to)
            // Apply inclusive (+1 day) only if the end operand was a plain date.
            val toZdt = if (inclusive && to is DateTimeResult.Date) rawToZdt.plusDays(1) else rawToZdt

            val isBackward = fromZdt.isAfter(toZdt)
            val (startZdt, endZdt) = if (isBackward) toZdt to fromZdt else fromZdt to toZdt

            val startOfEndDate = endZdt.toLocalDate().atStartOfDay(endZdt.zone)
            val timeDuration = java.time.Duration.between(startOfEndDate, endZdt)
            val extraSeconds = timeDuration.seconds  // seconds into the end day
            val startTimeSeconds = java.time.Duration.between(
                startZdt.toLocalDate().atStartOfDay(startZdt.zone), startZdt
            ).seconds

            // Net elapsed seconds = seconds into end day minus seconds into start day.
            val netSeconds = extraSeconds - startTimeSeconds

            var totalDays = ChronoUnit.DAYS.between(startZdt.toLocalDate(), endZdt.toLocalDate())
            var remainingSeconds = netSeconds

            // Adjust the calendar boundary if the time part makes the last day incomplete.
            val adjustedEndDate = if (remainingSeconds < 0) {
                totalDays--
                remainingSeconds += 86400L
                endZdt.toLocalDate().minusDays(1)
            } else {
                endZdt.toLocalDate()
            }

            // Recalculate the period based on the adjusted date boundary.
            val period = Period.between(startZdt.toLocalDate(), adjustedEndDate)

            val hours   = remainingSeconds / 3600L
            val minutes = (remainingSeconds % 3600L) / 60L
            val seconds = remainingSeconds % 60L

            val weeks = totalDays / 7L
            val days  = totalDays % 7L

            val delta = if (period.years > 0 || period.months >= 2) {
                DateTimeDelta(years = period.years.toLong(), months = period.months.toLong(),
                    days = period.days.toLong(), hours = hours, minutes = minutes, seconds = seconds)
            } else if (period.months == 1) {
                DateTimeDelta(months = 1, days = period.days.toLong(),
                    hours = hours, minutes = minutes, seconds = seconds)
            } else {
                DateTimeDelta(weeks = weeks, days = days,
                    hours = hours, minutes = minutes, seconds = seconds)
            }

            return DateTimeResult.Duration(if (isBackward) delta.negate() else delta)
        }

        val fromDate = toLocalDate(from)
        val rawToDate = toLocalDate(to)
        val toDate = if (inclusive) rawToDate.plusDays(1) else rawToDate

        val isBackward = fromDate.isAfter(toDate)
        val (start, end) = if (isBackward) toDate to fromDate else fromDate to toDate

        val period = Period.between(start, end)
        val totalDays = ChronoUnit.DAYS.between(start, end)
        val weeks = totalDays / 7
        val remainingDays = totalDays % 7

        val delta = if (period.years > 0 || period.months >= 2) {
            DateTimeDelta(
                years = period.years.toLong(),
                months = period.months.toLong(),
                days = period.days.toLong()
            )
        } else if (period.months == 1) {
            DateTimeDelta(months = 1, days = period.days.toLong())
        } else {
            DateTimeDelta(weeks = weeks, days = remainingDays)
        }

        return DateTimeResult.Duration(if (isBackward) delta.negate() else delta)
    }

    fun addDeltas(a: DateTimeDelta, b: DateTimeDelta): DateTimeDelta {
        return DateTimeDelta(
            years = a.years + b.years,
            months = a.months + b.months,
            weeks = a.weeks + b.weeks,
            days = a.days + b.days,
            hours = a.hours + b.hours,
            minutes = a.minutes + b.minutes,
            seconds = a.seconds + b.seconds
        ).normalize()
    }

    /**
     * Year-only interval: 1978 to 2021 → DateTimeResult.Duration(DateTimeDelta(years=43))
     */
    fun yearInterval(fromYear: Int, toYear: Int): DateTimeResult.Duration {
        val years = (toYear - fromYear).toLong()
        return DateTimeResult.Duration(DateTimeDelta(years = years))
    }

    // ── Day-count queries ────────────────────────────────────────────────────

    /** Returns the number of days from today to the given date (positive = future). */
    fun daysTill(date: DateTimeResult): DateTimeResult.DayCount {
        val days = ChronoUnit.DAYS.between(today, toLocalDate(date))
        return DateTimeResult.DayCount(days)
    }

    /** Returns the number of days since the given date until today (positive = past). */
    fun daysSince(date: DateTimeResult): DateTimeResult.DayCount {
        val days = ChronoUnit.DAYS.between(toLocalDate(date), today)
        return DateTimeResult.DayCount(days)
    }

    /** Returns the absolute number of days between two dates. */
    fun daysBetween(a: DateTimeResult, b: DateTimeResult): DateTimeResult.DayCount {
        val days = Math.abs(ChronoUnit.DAYS.between(toLocalDate(a), toLocalDate(b)))
        return DateTimeResult.DayCount(days)
    }

    // ── Conversion ───────────────────────────────────────────────────────────

    /**
     * Converts a DateTimeResult to a different timezone.
     * Date values are first converted to midnight ZonedDateTime in the system zone
     * before applying the timezone conversion.
     */
    fun convertTimezone(dt: DateTimeResult, targetZone: ZoneId): DateTimeResult.DateTime {
        val zdt: ZonedDateTime = when (dt) {
            is DateTimeResult.Date     -> dt.date.atStartOfDay(systemZone)
            is DateTimeResult.DateTime -> dt.instant
            else -> throw EvalException("Cannot apply timezone conversion to a duration or day-count result.")
        }
        return DateTimeResult.DateTime(zdt.withZoneSameInstant(targetZone))
    }

    /**
     * Returns ISO 8601 string representation, e.g. "2019-07-01T14:30:00+05:30".
     * Date (no time) uses start-of-day in system zone.
     */
    fun toIso8601(dt: DateTimeResult): String {
        return when (dt) {
            is DateTimeResult.Date -> dt.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            is DateTimeResult.DateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt.instant)
            else -> throw EvalException("Cannot convert a duration or day-count to ISO 8601.")
        }
    }

    /**
     * Returns Unix epoch seconds.
     * Date (no time) is treated as start-of-day in system zone.
     */
    fun toTimestamp(dt: DateTimeResult): Long {
        val zdt: ZonedDateTime = when (dt) {
            is DateTimeResult.Date     -> dt.date.atStartOfDay(systemZone)
            is DateTimeResult.DateTime -> dt.instant
            else -> throw EvalException("Cannot convert a duration or day-count to a timestamp.")
        }
        return zdt.toEpochSecond()
    }

    // ── Formatting ───────────────────────────────────────────────────────────

    /**
     * Formats a DateTimeResult for display in the calculator results column.
     *
     * Format table:
     *   Date (current year)   → "June 10"
     *   Date (other year)     → "July 1, 2019"
     *   DateTime (current yr) → "June 10, 12:30 PM"   (with system-zone timezone abbreviation)
     *   DateTime (other yr)   → "July 1, 2019, 2:30 PM CDT"
     *   Duration              → delta.format()  e.g. "3 w 5 d", "42 y", "2 mo"
     *   DayCount              → "N d"           e.g. "57 d", "30 d"
     */
    fun format(result: DateTimeResult): String {
        val currentYear = today.year
        return when (result) {
            is DateTimeResult.Date -> {
                if (result.date.year == currentYear) {
                    formatDate(result.date, withYear = false)
                } else {
                    formatDate(result.date, withYear = true)
                }
            }
            is DateTimeResult.DateTime -> {
                val sameYear = result.instant.year == currentYear
                val datePart = if (sameYear) {
                    formatDate(result.instant.toLocalDate(), withYear = false)
                } else {
                    formatDate(result.instant.toLocalDate(), withYear = true)
                }
                val timePart = formatTime(result.instant)
                val tzAbbr = TimezoneRegistry.getFriendlyName(result.instant)
                "$datePart, $timePart $tzAbbr"
            }
            is DateTimeResult.Duration -> result.delta.format()
            is DateTimeResult.DayCount -> "${result.days} d"
            is DateTimeResult.TimeCount -> "${result.value} ${result.unit}"
        }
    }

    // Formats: "July 1" or "July 1, 2019" — US month-first style, no leading zero on day.
    private fun formatDate(date: LocalDate, withYear: Boolean): String {
        val month = date.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
        val day = date.dayOfMonth
        return if (withYear) "$month $day, ${date.year}" else "$month $day"
    }

    // Formats: "2:30 PM" — 12-hour clock, no leading zero on hour.
    private fun formatTime(zdt: ZonedDateTime): String {
        val hour12 = zdt.hour % 12
        val displayHour = if (hour12 == 0) 12 else hour12
        val minute = zdt.minute.toString().padStart(2, '0')
        val amPm = if (zdt.hour < 12) "AM" else "PM"
        return "$displayHour:$minute $amPm"
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private fun toLocalDate(dt: DateTimeResult): LocalDate = when (dt) {
        is DateTimeResult.Date     -> dt.date
        is DateTimeResult.DateTime -> dt.instant.toLocalDate()
        else -> throw EvalException("Expected a date or datetime operand.")
    }

    private fun toZonedDateTime(dt: DateTimeResult): ZonedDateTime = when (dt) {
        is DateTimeResult.DateTime -> dt.instant
        is DateTimeResult.Date     -> dt.date.atStartOfDay(systemZone)
        else -> throw EvalException("Expected a date or datetime operand.")
    }
}
