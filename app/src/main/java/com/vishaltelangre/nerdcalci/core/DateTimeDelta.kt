package com.vishaltelangre.nerdcalci.core

/**
 * A calendar-aware duration used exclusively in date arithmetic.
 * All fields default to 0. Values may be negative (e.g., for subtraction).
 *
 * Display format examples:
 *   DateTimeDelta(years=42) → "42 y"
 *   DateTimeDelta(weeks=3, days=5) → "3 wk 5 d"
 *   DateTimeDelta(months=2, days=5) → "2 mo 5 d"
 *   DateTimeDelta(hours=2, minutes=30) → "2 h 30 min"
 */
data class DateTimeDelta(
    val years: Long = 0,
    val months: Long = 0,
    val weeks: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
) {
    /** Returns true if all fields are zero. */
    fun isEmpty(): Boolean = years == 0L && months == 0L && weeks == 0L &&
            days == 0L && hours == 0L && minutes == 0L && seconds == 0L

    /** Returns a new delta with all values negated (for subtraction). */
    fun negate(): DateTimeDelta = copy(
        years = -years, months = -months, weeks = -weeks, days = -days,
        hours = -hours, minutes = -minutes, seconds = -seconds
    )

    operator fun unaryMinus(): DateTimeDelta = negate()

    operator fun plus(other: DateTimeDelta): DateTimeDelta = DateTimeDelta(
        years = years + other.years,
        months = months + other.months,
        weeks = weeks + other.weeks,
        days = days + other.days,
        hours = hours + other.hours,
        minutes = minutes + other.minutes,
        seconds = seconds + other.seconds
    )

    operator fun minus(other: DateTimeDelta): DateTimeDelta = plus(other.negate())

    /**
     * Formats in short-form cascade. Shows only non-zero fields from the
     * largest occupied unit downward.
     *
     * Examples:
     *   DateTimeDelta(years=2, months=3)   → "2 y 3 mo"
     *   DateTimeDelta(weeks=3, days=5)      → "3 w 5 d"
     *   DateTimeDelta(days=22)             → "22 d"
     *   DateTimeDelta(hours=2, minutes=30) → "2 h 30 min"
     *   DateTimeDelta()                    → "0 d"
     */
    fun format(): String {
        val n = normalize()
        if (n.isEmpty()) return "0 d"

        // Determine if the delta is negative overall.
        // Since we normalized D/H/M/S to have consistent signs, we can check any non-zero field.
        val isNegative = n.years < 0 || n.months < 0 || n.weeks < 0 || n.days < 0 || n.hours < 0 || n.minutes < 0 || n.seconds < 0
        val abs = if (isNegative) n.negate() else n

        val parts = mutableListOf<String>()
        if (abs.years != 0L)   parts += "${abs.years} y"
        if (abs.months != 0L)  parts += "${abs.months} mo"
        if (abs.weeks != 0L)   parts += "${abs.weeks} wk"
        if (abs.days != 0L)    parts += "${abs.days} d"
        if (abs.hours != 0L)   parts += "${abs.hours} h"
        if (abs.minutes != 0L) parts += "${abs.minutes} min"
        if (abs.seconds != 0L) parts += "${abs.seconds} s"
        
        val joined = parts.joinToString(" ")
        return if (isNegative) "-$joined" else joined
    }

    /**
     * Normalizes the delta by carrying over smaller units to larger ones.
     * Seconds -> Minutes -> Hours -> Days.
     * Years, months and weeks are kept as-is since their relationship to days varies by calendar.
     */
    fun normalize(): DateTimeDelta {
        // First, normalize the clock part (D/H/M/S) to ensure consistent signs.
        // We use total seconds for this part as their relationships are fixed.
        var totalS = days * 86400L + hours * 3600L + minutes * 60L + seconds
        
        val d = totalS / 86400L; totalS %= 86400L
        val h = totalS / 3600L;  totalS %= 3600L
        val m = totalS / 60L;    totalS %= 60L
        val s = totalS
        
        // Note: years, months and weeks are kept separate as they don't have fixed second counts.
        // However, if the overall delta is negative, we should ideally ensure these are also 
        // consistent. For now, we fix the most common issue: mixed signs in the D/H/M/S part.
        return copy(days = d, hours = h, minutes = m, seconds = s)
    }

    /**
     * Estimates the total duration in seconds.
     * Uses average year (365.2425 days) and average month (1/12 of year).
     */
    fun toSecondsEstimate(): Long {
        return years * 31556952L +
                months * 2629746L +
                weeks * 604800L +
                days * 86400L +
                hours * 3600L +
                minutes * 60L +
                seconds
    }
}

/** Maps time unit symbols (same as UnitCategory.TIME symbols) to DateTimeDelta fields. */
object DateTimeDeltaParser {
    /**
     * Parses a compound duration string like "3 weeks 5 days" or "2h 30min" into a DateTimeDelta.
     * Recognises the same short and long unit symbols as UnitConverter for UnitCategory.TIME.
     * Returns null if the string contains no recognisable duration components.
     *
     * Accepted unit tokens (case-insensitive):
     *   years:   "y", "yr", "yrs", "year", "years"
     *   months:  "mo", "month", "months"
     *   weeks:   "w", "wk", "wks", "week", "weeks"
     *   days:    "d", "day", "days"
     *   hours:   "h", "hr", "hrs", "hour", "hours"
     *   minutes: "min", "mins", "minute", "minutes"
     *   seconds: "s", "sec", "secs", "second", "seconds"
     */
    fun parse(input: String): DateTimeDelta? {
        // Regex: one or more "NUMBER UNIT" pairs, separated by optional spaces
        val componentPattern = Regex(
            """(\d+(?:\.\d+)?)\s*(years?|yrs?|y|months?|mo|weeks?|wks?|w|days?|d|hours?|hrs?|h|minutes?|mins?|min|seconds?|secs?|s)\b""",
            RegexOption.IGNORE_CASE
        )
        val matches = componentPattern.findAll(input).toList()
        if (matches.isEmpty()) return null

        var years = 0L; var months = 0L; var weeks = 0L; var days = 0L
        var hours = 0L; var minutes = 0L; var seconds = 0L

        for (m in matches) {
            val amount = m.groupValues[1].toDouble().toLong()
            when (m.groupValues[2].lowercase()) {
                "y", "yr", "yrs", "year", "years" -> years += amount
                "mo", "month", "months"            -> months += amount
                "w", "wk", "wks", "week", "weeks" -> weeks += amount
                "d", "day", "days"                 -> days += amount
                "h", "hr", "hrs", "hour", "hours"  -> hours += amount
                "min", "mins", "minute", "minutes" -> minutes += amount
                "s", "sec", "secs", "second", "seconds" -> seconds += amount
            }
        }
        return DateTimeDelta(years, months, weeks, days, hours, minutes, seconds)
    }
}
