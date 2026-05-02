package com.vishaltelangre.nerdcalci.core

import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Represents the result of a date/time expression.
 * Carried in EvaluationResult.dateTimeResult when non-null.
 */
sealed class DateTimeResult {

    /**
     * A calendar date with no time, using the system timezone implicitly.
     * This is the result of: today, yesterday, tomorrow, date(), parseDate() (no time component).
     */
    data class Date(val date: LocalDate) : DateTimeResult()

    /**
     * A date + time value. Always stored as ZonedDateTime.
     * The zone may be ZoneId.systemDefault() (for now, datetime(), parseDate(epoch))
     * or an explicit zone (for datetimeZ() and after `in "<tz>"` conversion).
     */
    data class DateTime(val instant: ZonedDateTime) : DateTimeResult()

    /**
     * The duration between two dates, expressed as a calendar-aware DateTimeDelta.
     * This is the result of interval expressions:
     *   parseDate("Jan 10") to parseDate("Feb 5")  → Duration(delta = DateTimeDelta(weeks=3, days=5))
     *   1978 to 2021                                → Duration(delta = DateTimeDelta(years=43))
     */
    data class Duration(val delta: DateTimeDelta) : DateTimeResult()

    /**
     * The result of a day-count query: days since / days till / days between.
     * Displayed as "N d".
     */
    data class DayCount(val days: Long) : DateTimeResult()

    /**
     * The result of a specific unit count: through ... in hours/minutes/weeks etc.
     * Displayed exactly as "<value> <unit>".
     */
    data class TimeCount(val value: Long, val unit: String) : DateTimeResult()
}
