package com.vishaltelangre.nerdcalci.core

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.*
import java.time.temporal.ChronoUnit

class DateTimeFeaturesTest {

    @Test
    fun `date and datetime constructors`() = testCalculate(
        "date(2024, 1, 1)",
        "datetime(2024, 1, 1, 14, 30, 0)",
        "datetimeZ(2024, 1, 1, 18, 0, 0, \"UTC\")",
        "datetimeZ(2024, 4, 1, 18, 0, 0, \"AEST\")",
        "date(2024, 2, 29)",                   // Leap year
        "date(2000, 2, 29)",                   // Millennial leap year
        "date(1900, 2, 29)"                    // Not a leap year (Err)
    ) { results ->
        assertEquals("Jan 1, 2024", results[0].result)
        assertTrue(results[1].result.contains("Jan 1, 2024, 2:30 PM"))
        assertEquals("Jan 1, 2024, 6:00 PM UTC", results[2].result)
        assertTrue(results[3].result.contains("Apr 1, 2024"))
        assertEquals("Feb 29, 2024", results[4].result)
        assertEquals("Feb 29, 2000", results[5].result)
        assertEquals("Err", results[6].result)
    }

    @Test
    fun `date parsing formats`() = testCalculate(
        "parseDate(\"2024-01-01\")",
        "parseDate(\"Jan 1, 2024\")",
        "parseDate(\"1 Jan 2024\")",
        "parseDate(\"2024/01/01\")",
        "parseDate(\"2025/03/17\")",
        "parseDate(1718400000)",
        "parseDate(\"june 10\")",               // Inferred year
        "parseDate(\"10 June\")"                // Inferred year
    ) { results ->
        assertEquals("Jan 1, 2024", results[0].result)
        assertEquals("Jan 1, 2024", results[1].result)
        assertEquals("Jan 1, 2024", results[2].result)
        assertEquals("Jan 1, 2024", results[3].result)
        assertEquals("Mar 17, 2025", results[4].result)
        assertNotEquals("Err", results[5].result)
        assertTrue(results[6].result.contains("Jun 10"))
        assertTrue(results[7].result.contains("Jun 10"))
    }

    @Test
    fun `regional numeric date parsing`() {
        // Test with DMY preference
        testCalculate(
            "parseDate(\"25/12/2024\")",
            dateFormat = Constants.DATE_FORMAT_DMY
        ) { results ->
            assertEquals("Dec 25, 2024", results[0].result)
        }

        // Test with MDY preference
        testCalculate(
            "parseDate(\"12/25/2024\")",
            dateFormat = Constants.DATE_FORMAT_MDY
        ) { results ->
            assertEquals("Dec 25, 2024", results[0].result)
        }

        // Test with YMD preference
        testCalculate(
            "parseDate(\"2024/12/25\")",
            dateFormat = Constants.DATE_FORMAT_YMD
        ) { results ->
            assertEquals("Dec 25, 2024", results[0].result)
        }
    }

    @Test
    fun `roundtrip parsing via iso8601 and timestamp`() = testCalculate(
        "d = date(2024, 1, 1)",
        "parseDate(d as iso8601)",
        "parseDate(d as timestamp)",
        "dt = datetime(2024, 1, 1, 12, 0, 0)",
        "parseDate(dt as iso8601)",
        "parseDate(dt as timestamp)",
        "dtz = datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\")",
        "parseDate(dtz as iso8601)",
        "parseDate(dtz as timestamp)",
        "parseDate(\"2025-05-02T15:11:42.838589+05:30\")"
    ) { results ->
        assertEquals("Jan 1, 2024", results[1].result)
        // Timestamps always include time, so they parse back as DateTime
        assertTrue(results[2].result.contains("Jan 1, 2024"))
        
        assertTrue(results[4].result.contains("Jan 1, 2024"))
        assertTrue(results[5].result.contains("Jan 1, 2024"))
        assertTrue(results[7].result.contains("Jan 1, 2024"))
        assertTrue(results[8].result.contains("Jan 1, 2024"))
        assertTrue(results[9].result.contains("May 2, 2025, 3:11 PM GMT+05:30"))
    }

    @Test
    fun `relative keywords`() = testCalculate(
        "today",
        "yesterday",
        "tomorrow",
        "now",
        "today to tomorrow",
        "now to tomorrow",
        "now to yesterday"
    ) { results ->
        results.forEach { assertNotEquals("Err", it.result) }
        assertEquals("1 d", results[4].result)
        assertNotEquals("Err", results[5].result)
        assertTrue(results[6].result.contains("-")) 
    }

    @Test
    fun `date arithmetic with durations`() = testCalculate(
        "date(2024, 1, 1) + 1 year",
        "date(2024, 1, 1) + 1 month",
        "date(2024, 1, 1) + 1 week",
        "date(2024, 1, 1) + 1 day",
        "date(2024, 1, 1) - 1 day",
        "date(2024, 1, 31) + 1 month", // Month pinning: Feb 29 2024 (leap year)
        "date(2024, 1, 1) + 1 hour",    // Promotes to DateTime
        "date(2024, 1, 1) - 2 hours",   // Promotes to DateTime
        "parseDate(\"2019-04-01\") + 3 weeks",
        "3 weeks before parseDate(\"2019-04-22\")",
        "3 weeks after parseDate(\"2019-04-01\")"
    ) { results ->
        assertEquals("Jan 1, 2025", results[0].result)
        assertEquals("Feb 1, 2024", results[1].result)
        assertEquals("Jan 8, 2024", results[2].result)
        assertEquals("Jan 2, 2024", results[3].result)
        assertEquals("Dec 31, 2023", results[4].result)
        assertEquals("Feb 29, 2024", results[5].result)
        assertTrue(results[6].result.contains("Jan 1, 2024, 1:00 AM"))
        assertTrue(results[7].result.contains("Dec 31, 2023, 10:00 PM"))
        assertEquals("Apr 22, 2019", results[8].result)
        assertEquals("Apr 1, 2019", results[9].result)
        assertEquals("Apr 22, 2019", results[10].result)
    }

    @Test
    fun `relative arithmetic shortcuts`() = testCalculate(
        "3 days ago",
        "2 weeks from now",
        "4 hours before today",
        "5 minutes after now",
        "4 days from",
        "4 days from 2 days ago",
        "4 days from 2 days before date(2024, 5, 1)"
    ) { results ->
        val now = LocalDate.now()
        assertTrue(results[0].result.contains("${now.minusDays(3).dayOfMonth}"))
        assertTrue(results[1].result.contains("${now.plusWeeks(2).dayOfMonth}"))
        assertTrue(results[2].result.contains("${now.minusDays(1).dayOfMonth}"))
        assertTrue(results[3].result.contains("${now.dayOfMonth}"))
        assertTrue(results[4].result.contains("${now.plusDays(4).dayOfMonth}"))
        assertTrue(results[5].result.contains("${now.plusDays(2).dayOfMonth}"))
        assertEquals("May 3, 2024", results[6].result)
    }

    @Test
    fun `date intervals and day counts`() = testCalculate(
        "date(2024, 1, 1) to date(2024, 1, 8)", // [0]
        "date(2024, 1, 1) to date(2025, 1, 1)", // [1]
        "date(2024, 1, 8) to date(2024, 1, 1)", // [2]
        "date(2024, 1, 1) through date(2024, 1, 31) in days", // [3]
        "days between date(2024, 1, 1) and date(2024, 1, 8)", // [4]
        "days since date(2024, 1, 1)",          // [5]
        "days till date(2025, 1, 1)",           // [6]
        "1978 to 2021",                         // [7]
        "parseDate(\"2019-03-03\") to parseDate(\"2019-05-30\")", // [8]
        "days since date(2030, 1, 1)",          // [9] Since future (negative)
        "days till date(2020, 1, 1)",            // [10] Till past (negative)
        "date(2024, 1, 1) through date(2024, 1, 2) in hours", // [11]
        "date(2024, 1, 1) through date(2024, 1, 7) in weeks", // [12]
        "days between date(2024, 1, 8) and date(2024, 1, 1)", // [13]
        "between date(2024, 1, 8) and date(2024, 1, 1)"      // [14]
    ) { results ->
        assertEquals("1 wk", results[0].result)
        assertEquals("1 y", results[1].result)
        assertEquals("-1 wk", results[2].result)
        assertEquals("31 d", results[3].result)
        assertEquals("7 d", results[4].result)
        
        val today = LocalDate.now()
        val since = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), today)
        assertEquals("$since d", results[5].result)
        
        val till = java.time.temporal.ChronoUnit.DAYS.between(today, LocalDate.of(2025, 1, 1))
        assertEquals("$till d", results[6].result)
        
        assertEquals("43 y", results[7].result)
        assertEquals("2 mo 27 d", results[8].result)
        assertTrue(results[9].result.startsWith("-"))
        assertTrue(results[10].result.startsWith("-"))
        assertTrue(results[11].result.contains("48 h"))
        assertEquals("1 wk", results[12].result)
        assertEquals("7 d", results[13].result)
        assertEquals("1 wk", results[14].result)
    }

    @Test
    fun `datetime-to-datetime interval preserves time components`() = testCalculate(
        "datetimeZ(2024, 1, 1, 10, 0, 0, \"UTC\") to datetimeZ(2024, 1, 1, 12, 30, 0, \"UTC\")", // [0] same day, 2h 30min apart
        "datetimeZ(2024, 1, 1, 23, 0, 0, \"UTC\") to datetimeZ(2024, 1, 2, 1, 0, 0, \"UTC\")",  // [1] crosses midnight, 2h
        "datetimeZ(2024, 1, 3, 6, 0, 0, \"UTC\") to datetimeZ(2024, 1, 1, 6, 0, 0, \"UTC\")"   // [2] backward, -2 d
    ) { results ->
        // Same day: expect 2 h 30 min
        assertEquals("2 h 30 min", results[0].result)
        // Crosses midnight: expect 2 h
        assertEquals("2 h", results[1].result)
        // Backward by 2 days exactly
        assertEquals("-2 d", results[2].result)
    }

    @Test
    fun `compact numeric date interval`() = testCalculate(
        "20260413 to 20260420" // [0] compact numeric dates
    ) { results ->
        // 7 days apart → 1 wk
        assertEquals("1 wk", results[0].result)
    }

    @Test
    fun `date interval with time cross respects incomplete days`() = testCalculate(
        "datetime(1981, 2, 14, 18, 32, 0) to datetime(2026, 5, 3, 14, 49, 9)" // [0]
    ) { results ->
        // Feb 14 18:32 to May 3 14:49:09
        // 45y 2m 18d 20h 17min 9s
        assertEquals("45 y 2 mo 18 d 20 h 17 min 9 s", results[0].result)
    }

    @Test
    fun `inclusive logic for dates vs datetimes`() = testCalculate(
        "datetime(2024, 1, 1, 12, 0, 0) through datetime(2024, 1, 2, 12, 0, 0) in hours", // [0] datetimes, no padding -> 24h
        "date(2024, 1, 1) through date(2024, 1, 1) in hours",                           // [1] date through same date -> 24h (padding applied)
        "date(2024, 1, 1) through date(2024, 1, 2) in hours"                            // [2] date through tomorrow -> 48h (padding applied)
    ) { results ->
        assertEquals("24 h", results[0].result)
        assertEquals("24 h", results[1].result)
        assertEquals("48 h", results[2].result)
    }


    @Test
    fun `timezone conversion and formatting`() = testCalculate(
        "dtz = datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\")",
        "dtz in \"Asia/Tokyo\"",
        "dtz to \"Asia/Tokyo\"",
        "dtz as \"Asia/Tokyo\"",
        "dtz as iso8601",
        "dtz in timestamp",
        "datetimeZ(2024, 4, 1, 18, 0, 0, \"AEST\") in \"America/Chicago\"",
        "datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\") in \"PST\"",
        "datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\") in \"EST\""
    ) { results ->
        val tokyo = "Jan 1, 2024, 9:00 PM JST"
        assertEquals(tokyo, results[1].result)
        assertEquals("Err", results[2].result) // `to "string"` is not a timezone modifier; use `in` or `as`
        assertEquals(tokyo, results[3].result)

        assertEquals("2024-01-01T12:00:00Z", results[4].result)
        assertEquals("1704110400.0", results[5].result)
        
        assertTrue(results[6].result.contains("Apr 1, 2024"))
        assertTrue(results[7].result.contains("PST") || results[7].result.contains("PDT"))
        assertTrue(results[8].result.contains("EST") || results[8].result.contains("EDT"))
    }

    @Test
    fun `conversions with in and to operators`() = testCalculate(
        "d = date(2024, 1, 1)",
        "d in timestamp",
        "d to timestamp",
        "d as timestamp",
        "d in iso8601",
        "d to iso8601",
        "d as iso8601",
        "dt = datetime(2024, 1, 1, 12, 0, 0)",
        "dt in timestamp",
        "dt to iso8601"
    ) { results ->
        // date(2024, 1, 1) at system zone midnight
        val ts = results[1].result
        assertEquals(ts, results[2].result)
        assertEquals(ts, results[3].result)
        assertNotEquals("Err", ts)

        val iso = results[4].result
        assertEquals("2024-01-01", iso)
        assertEquals(iso, results[5].result)
        assertEquals(iso, results[6].result)

        // datetime
        assertNotEquals("Err", results[8].result)
        assertTrue(results[9].result.contains("2024-01-01T12:00:00"))
    }

    @Test
    fun `composite units in numeric arithmetic`() = testCalculate(
        "1h 30 min in s",
        "100 km / 2 hours 30 minutes",
        "1 day + 12 hours in hours",
        "2wk 3d + 1 week",
        "1mo 2wk in days"
    ) { results ->
        assertEquals("5400.0 s", results[0].result)
        assertEquals("40.0 kmh", results[1].result)
        assertEquals("36.0 h", results[2].result)
        assertEquals("3 wk 3 d", results[3].result)
        assertEquals("44.436875 d", results[4].result)
    }

    @Test
    fun `reserved keyword protection`() = testCalculate(
        "today = 10",
        "now = \"hello\"",
        "yesterday = date(2024, 1, 1)",
        "tomorrow = 5",
        "d = 10",
        "d = today"
    ) { results ->
        // Assignments to keywords should fail
        assertEquals("Err", results[0].result)
        assertEquals("Err", results[1].result)
        assertEquals("Err", results[2].result)
        assertEquals("Err", results[3].result)

        // Normal variable can hold a keyword value
        assertNotEquals("Err", results[5].result)
    }

    @Test
    fun `quantity divided by duration`() = testCalculate(
        "100 km / 2h 30min",
        "distance = 50 miles",
        "time = 1 hour",
        "distance / time"
    ) { results ->
        // 100 km / 2.5 h = 40 km/h
        assertEquals("40.0 kmh", results[0].result)
        assertEquals("50.0 mph", results[3].result)
    }

    @Test
    fun `variable resolution with dates`() = testCalculate(
        "d = date(2024, 1, 1)",
        "d + 1 week",
        "tz = \"Asia/Tokyo\"",
        "datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\") in tz"
    ) { results ->
        assertEquals("Jan 1, 2024", results[0].result)
        assertEquals("Jan 8, 2024", results[1].result)
        assertEquals("Jan 1, 2024, 9:00 PM JST", results[3].result)
    }

    @Test
    fun `date and time edge cases`() = testCalculate(
        "parseDate(\"\")",                      // Empty
        "parseDate(\"2024-02-30\")",            // Invalid calendar date
        "date(2024, 2, 30)",                   // Invalid constructor params
        "date(2024, 2, 29) + 1 year",          // Leap year arithmetic
        "date(2024, 1, 1) to \"not a date\"",  // Invalid interval target
        "days since \"not a date\"",           // Invalid day-count target
        "datetimeZ(2024, 1, 1, 12, 0, 0, \"UTC\") in \"Invalid/Zone\"",
        "1.5 days ago",                         // Fractional duration (truncates)
        "date(2023, 12, 31) + 1y 1mo 1d",      // Multi-component overflow
        "parseDate(\"12/02/1988\")",           // defaults to DMY
        dateFormat = Constants.DATE_FORMAT_DMY
    ) { results ->
        assertEquals("Err", results[0].result)
        assertEquals("Err", results[1].result)
        assertEquals("Err", results[2].result)
        assertEquals("Feb 28, 2025", results[3].result)
        assertEquals("Err", results[4].result)
        assertEquals("Err", results[5].result)
        assertEquals("Err", results[6].result)
        assertNotEquals("Err", results[7].result) // Truncates to 1 day
        assertEquals("Feb 1, 2025", results[8].result)
        assertEquals("Feb 12, 1988", results[9].result)
    }

    @Test
    fun `above correctly picks up date from preceding line during partial recalculation`() = runBlocking {
        val expressions = arrayOf(
            "date(2024, 2, 12)",
            "above"
        )
        val lines = createLines(*expressions)
        // Simulate partial recalculation starting at the second line
        val results = MathEngine.calculateFrom(lines, changedIndex = 1)
        assertEquals(1, results.size)
        assertEquals("Feb 12, 2024", results[0].result)
    }

    @Test
    fun `date values can be assigned and reused across lines`() = testCalculate(
        "d = parseDate(\"2019-04-01\")",
        "d + 3 weeks",
        "next = d + 3 weeks",
        "next in \"America/Chicago\""
    ) { results ->
        assertEquals("Apr 1, 2019", results[0].result)
        assertEquals("Apr 22, 2019", results[1].result)
        assertEquals("Apr 22, 2019", results[2].result)
        assertTrue(results[3].result.contains("Apr 21, 2019") || results[3].result.contains("Apr 22, 2019"))
    }
    @Test
    fun `compound assignment with dates`() = testCalculate(
        "d = date(2024, 1, 1)",
        "d += 1 week",
        "d",
        "d -= 2 days",
        "d"
    ) { results ->
        assertEquals("Jan 8, 2024", results[1].result)
        assertEquals("Jan 8, 2024", results[2].result)
        assertEquals("Jan 6, 2024", results[3].result)
        assertEquals("Jan 6, 2024", results[4].result)
    }

    @Test
    fun `duration scaling and ratios`() = testCalculate(
        "2h * 3",
        "1 day / 4",
        "1h / 15min",
        "1wk / 1 day"
    ) { results ->
        assertEquals("6.0 h", results[0].result)
        assertEquals("0.25 d", results[1].result)
        assertEquals("4.0", results[2].result)
        assertEquals("7.0", results[3].result)
    }

    @Test
    fun `invalid date arithmetic should err`() = testCalculate(
        "d = date(2024, 1, 1)",
        "d++",
        "d--",
        "d * 2",
        "d / 2",
        "d + 1",
        "d - 1"
    ) { results ->
        assertError("Unsupported date/duration arithmetic", results, 1)
        assertError("Unsupported date/duration arithmetic", results, 2)
        assertError("Multiplication is not supported for dates", results, 3)
        assertError("Division is not supported for dates", results, 4)
        assertError("Unsupported date/duration arithmetic", results, 5)
        assertError("Unsupported date/duration arithmetic", results, 6)
    }
    @Test
    fun `date interval and composite quantity consistency`() = testCalculate(
        "243 days 12.5 years 14 hours"
    ) { results ->
        assertError("Fractional components (like 2.5 years) are not supported in multi-unit durations. Please use whole numbers.", results, 0)
    }

    @Test
    fun `timezone interval and negative duration normalization`() = testCalculate(
        "now in \"IST\" through (now in \"JST\")", // [0]
        "now in \"JST\" through (now in \"IST\")"  // [1]
    ) { results ->
        assertEquals("3 h 30 min", results[0].result)
        assertEquals("-3 h 30 min", results[1].result)
    }

    @Test
    fun `date engine stabilization regression`() = testCalculate(
        "date(2024, 1, 1) through date(2024, 1, 31) in days", // [0]
        "_ + 1 day",                                   // [1]
        "raw(_)",                                      // [2]
        "243 days 14 hours",                           // [3]
        "raw(_)",                                      // [4]
        "14 hours 12 minutes 243 days",                // [5]
        "raw(_)",                                      // [6]
        "14 hours 2 years 12 weeks",                   // [7]
        "raw(_)",                                      // [8]
        "243 days 14 hours 12 minutes",                // [9]
        "raw(_)",                                      // [10]
        "2 years 12 weeks 14 hours",                   // [11]
        "raw(_)"                                       // [12]
    ) { results ->
        assertEquals("31 d", results[0].result)
        assertEquals("32 d", results[1].result) 
        assertEquals("32", results[2].result)
        assertEquals("243 d 14 h", results[3].result)
        assertTrue(results[4].result.startsWith("243.5833"))
        assertEquals("243 d 14 h 12 min", results[5].result)
        assertTrue(results[6].result.startsWith("243.5916"))
        assertEquals("2 y 12 wk 14 h", results[7].result)
        assertTrue(results[8].result.startsWith("2.2315"))
    }
    @Test
    fun `date interval conversion in time units`() = testCalculate(
        "date(2024,1,1) to date(2024,1,2) in seconds",    // [0] 86400 s
        "date(2024,1,1) to date(2024,1,2) in minutes",    // [1] 1440 min
        "date(2024,1,1) to date(2024,1,2) in hours",      // [2] 24 h
        "date(2024,1,1) to date(2024,1,2) in days",       // [3] 1 d
        "date(2024,1,1) to date(2024,1,8) in weeks",      // [4] 1 wk
        "(date(2024,1,1) to date(2024,1,2)) in seconds",  // [5] 86400 s (parenthesized)
        "date(2024,1,1) through date(2024,1,1) in hours", // [6] 24 h (inclusive)
        "between date(2024,1,1) and date(2024,1,2) in s", // [7] 86400 s (using in operator)
        "date(2024,1,1) to date(2024,1,1) in minutes",    // [8] 0 min
        "date(2024,1,2) to date(2024,1,1) in hours",      // [9] -24 h
        "today to today in s",                            // [10] 0 s
        "tomorrow through today in s",                    // [11] 0 s (inclusive)
        "date(2024,1,1) through date(2024,1,2) + 1d",     // [12] 3 d
        "date(2024,1,1) to date(2024,1,2) + 1d",          // [13] 2 d
        "days between date(2024,1,1) and date(2024,1,2) in hours", // [14] 24 h
        "days since (today - 1 day) in hours",            // [15] 24 h
        "days till (today + 1 day) in hours",             // [16] 24 h
        "days between date(2024,1,2) and date(2024,1,1) in hours"  // [17] -24 h?
    ) { results ->
        assertEquals("86400 s", results[0].result)
        assertEquals("1440 min", results[1].result)
        assertEquals("24 h", results[2].result)
        assertEquals("1 d", results[3].result)
        assertEquals("1 wk", results[4].result)
        assertEquals("86400.0 s", results[5].result)
        assertEquals("24 h", results[6].result)
        assertEquals("86400 s", results[7].result)
        assertEquals("0 min", results[8].result)
        assertEquals("-24 h", results[9].result)
        assertEquals("0 s", results[10].result)
        assertEquals("0 s", results[11].result)
        assertEquals("3 d", results[12].result)
        assertEquals("2 d", results[13].result)
        assertEquals("24 h", results[14].result)
        assertEquals("24 h", results[15].result)
        assertEquals("24 h", results[16].result)
        assertEquals("24 h", results[17].result)
    }
 
    @Test
    fun `date component extraction and arithmetic`() = testCalculate(
        "dt_z = datetimeZ(2024, 5, 20, 15, 30, 0, \"UTC\")", // [0]
        "getDay(dt_z)",                                      // [1] -> 20.0
        "getMonth(dt_z)",                                    // [2] -> 5.0
        "getYear(dt_z)",                                     // [3] -> 2024.0
        "daysInMonth(dt_z)",                                 // [4] -> 31.0
        
        "getDay(date(2024, 1, 5))",                          // [5] -> 5.0
        "getDay(parseDate(\"2026-05-20\"))",                 // [6] -> 20.0
        "getDay(today)",                                     // [7]
        "getDay(now)",                                       // [8]
        "getMonth(now)",                                     // [9]
        "getYear(now)",                                      // [10]
        
        "getMonth(date(2024, 10, 5))",                       // [11] -> 10.0
        "getMonth(parseDate(\"2026-05-20\"))",               // [12] -> 5.0
        
        "getYear(date(2024, 10, 5))",                        // [13] -> 2024.0
        "getYear(parseDate(\"2026-05-20\"))",                // [14] -> 2026.0
        
        "daysInMonth(date(2024, 2, 1))",                     // [15] -> 29.0
        "daysInMonth(date(2023, 2, 1))",                     // [16] -> 28.0
        "daysInMonth(date(2024, 4, 1))",                     // [17] -> 30.0
        
        "dt_arith = date(2024, 2, 1)",                       // [18]
        "days_left = daysInMonth(dt_arith) - getDay(dt_arith)", // [19] -> 28.0
        "days_left"                                          // [20] -> 28.0
    ) { results ->
        assertEquals("20.0", results[1].result)
        assertEquals("5.0", results[2].result)
        assertEquals("2024.0", results[3].result)
        assertEquals("31.0", results[4].result)
        
        assertEquals("5.0", results[5].result)
        assertEquals("20.0", results[6].result)
        
        val now = LocalDate.now()
        assertEquals("${now.dayOfMonth.toDouble()}", results[7].result)
        assertEquals("${now.dayOfMonth.toDouble()}", results[8].result)
        assertEquals("${now.monthValue.toDouble()}", results[9].result)
        assertEquals("${now.year.toDouble()}", results[10].result)
        
        assertEquals("10.0", results[11].result)
        assertEquals("5.0", results[12].result)
        
        assertEquals("2024.0", results[13].result)
        assertEquals("2026.0", results[14].result)
        
        assertEquals("29.0", results[15].result)
        assertEquals("28.0", results[16].result)
        assertEquals("30.0", results[17].result)
        
        assertEquals("28.0", results[19].result)
        assertEquals("28.0", results[20].result)
    }
    @Test
    fun `budgeting calculation using date functions`() = testCalculate(
        "money_left = 1000",
        "min_daily_expenses = 20",
        "current_day = getDay(now)",
        "days_in_month = daysInMonth(now)",
        "days_left = days_in_month - current_day",
        "money_left = money_left - (current_day * min_daily_expenses)",
        "allowed_daily_expense = money_left / days_left"
    ) { results ->
        val now = LocalDate.now()
        val d = now.dayOfMonth
        val totalDays = now.lengthOfMonth()
        val left = totalDays - d
        
        assertEquals("${d.toDouble()}", results[2].result)
        assertEquals("${totalDays.toDouble()}", results[3].result)
        assertEquals("${left.toDouble()}", results[4].result)
        
        if (left > 0) {
            val expectedMoneyLeft = 1000.0 - (d * 20.0)
            val expectedDaily = expectedMoneyLeft / left
            val actualDaily = results[6].result.toDoubleOrNull()
            assertNotNull("Result should not be Err and should be numeric", actualDaily)
            assertEquals(expectedDaily, actualDaily!!, 0.001)
        }
    }
}
