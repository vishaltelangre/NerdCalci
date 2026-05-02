package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.*
import org.junit.Test

class DateTimeDeltaParserTest {

    @Test
    fun `parses single component durations`() {
        assertEquals(DateTimeDelta(years = 1), DateTimeDeltaParser.parse("1 year"))
        assertEquals(DateTimeDelta(months = 2), DateTimeDeltaParser.parse("2 months"))
        assertEquals(DateTimeDelta(weeks = 3), DateTimeDeltaParser.parse("3 weeks"))
        assertEquals(DateTimeDelta(days = 4), DateTimeDeltaParser.parse("4 days"))
        assertEquals(DateTimeDelta(hours = 5), DateTimeDeltaParser.parse("5 hours"))
        assertEquals(DateTimeDelta(minutes = 6), DateTimeDeltaParser.parse("6 minutes"))
        assertEquals(DateTimeDelta(seconds = 7), DateTimeDeltaParser.parse("7 seconds"))
    }

    @Test
    fun `parses shorthand unit symbols`() {
        assertEquals(DateTimeDelta(years = 1), DateTimeDeltaParser.parse("1y"))
        assertEquals(DateTimeDelta(months = 1), DateTimeDeltaParser.parse("1mo"))
        assertEquals(DateTimeDelta(weeks = 1), DateTimeDeltaParser.parse("1w"))
        assertEquals(DateTimeDelta(days = 1), DateTimeDeltaParser.parse("1d"))
        assertEquals(DateTimeDelta(hours = 1), DateTimeDeltaParser.parse("1h"))
        assertEquals(DateTimeDelta(minutes = 1), DateTimeDeltaParser.parse("1min"))
        assertEquals(DateTimeDelta(seconds = 1), DateTimeDeltaParser.parse("1s"))
    }

    @Test
    fun `parses compound durations with spaces`() {
        val expected = DateTimeDelta(weeks = 3, days = 5, hours = 2, minutes = 30)
        assertEquals(expected, DateTimeDeltaParser.parse("3 weeks 5 days 2 hours 30 minutes"))
    }

    @Test
    fun `parses compact compound durations`() {
        val expected = DateTimeDelta(weeks = 3, days = 5, hours = 2, minutes = 30)
        assertEquals(expected, DateTimeDeltaParser.parse("3w 5d 2h 30min"))
    }

    @Test
    fun `is case-insensitive`() {
        assertEquals(DateTimeDelta(days = 1), DateTimeDeltaParser.parse("1 DAY"))
        assertEquals(DateTimeDelta(weeks = 2), DateTimeDeltaParser.parse("2 Wks"))
    }

    @Test
    fun `handles fractional amounts by truncating`() {
        // Current implementation uses toLong() on the double value
        assertEquals(DateTimeDelta(days = 1), DateTimeDeltaParser.parse("1.5 days"))
        assertEquals(DateTimeDelta(hours = 2), DateTimeDeltaParser.parse("2.9 hours"))
    }

    @Test
    fun `returns null for non-duration strings`() {
        assertNull(DateTimeDeltaParser.parse("hello world"))
        assertNull(DateTimeDeltaParser.parse("100"))
        assertNull(DateTimeDeltaParser.parse("meters"))
    }

    @Test
    fun `ignores unrelated text around durations`() {
        assertEquals(DateTimeDelta(days = 3), DateTimeDeltaParser.parse("it was 3 days ago"))
        assertEquals(DateTimeDelta(weeks = 1, days = 2), DateTimeDeltaParser.parse("around 1w 2d roughly"))
    }

    @Test
    fun `sums repeated units`() {
        // "1 day 2 days" -> 3 days
        assertEquals(DateTimeDelta(days = 3), DateTimeDeltaParser.parse("1 day 2 days"))
    }
}
