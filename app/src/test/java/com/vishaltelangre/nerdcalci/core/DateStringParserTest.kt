package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class DateStringParserTest {

    private var originalLocale: Locale? = null

    @Before
    fun setup() {
        originalLocale = Locale.getDefault()
        // Fix to US locale by default for consistent test behavior
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        originalLocale?.let { Locale.setDefault(it) }
    }

    @Test
    fun `resolves numeric format automatically based on locale`() {
        // MDY locale (e.g. US)
        Locale.setDefault(Locale.US)
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("12/25/2024") as DateTimeResult.Date).date)

        // DMY locale (e.g. UK)
        Locale.setDefault(Locale.UK)
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("25/12/2024") as DateTimeResult.Date).date)
        
        // Restore to US for other tests
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `parses ISO 8601 format`() {
        val result = DateStringParser.parse("2019-04-01")
        assertEquals(LocalDate.of(2019, 4, 1), (result as DateTimeResult.Date).date)
    }

    @Test
    fun `parses YYYY slash MM slash DD format`() {
        val result = DateStringParser.parse("2019/04/01")
        assertEquals(LocalDate.of(2019, 4, 1), (result as DateTimeResult.Date).date)
    }

    @Test
    fun `parses US style month-first format`() {
        val result = DateStringParser.parse("April 1, 2019")
        assertEquals(LocalDate.of(2019, 4, 1), (result as DateTimeResult.Date).date)
        
        val resultShort = DateStringParser.parse("Apr 1 2019")
        assertEquals(LocalDate.of(2019, 4, 1), (resultShort as DateTimeResult.Date).date)
    }

    @Test
    fun `parses day-first format`() {
        val result = DateStringParser.parse("1 April 2019")
        assertEquals(LocalDate.of(2019, 4, 1), (result as DateTimeResult.Date).date)

        val resultShort = DateStringParser.parse("1 Apr 2019")
        assertEquals(LocalDate.of(2019, 4, 1), (resultShort as DateTimeResult.Date).date)
    }

    @Test
    fun `infers year for month-day formats`() {
        // We can't easily test fixed inferred year because it depends on current date.
        // But we can check it doesn't throw.
        val result = DateStringParser.parse("June 10")
        assertNotNull((result as DateTimeResult.Date).date)
        
        val result2 = DateStringParser.parse("10 June")
        assertNotNull((result2 as DateTimeResult.Date).date)
    }

    @Test
    fun `parses regional numeric formats`() {
        // DMY
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("25/12/2024", Constants.DATE_FORMAT_DMY) as DateTimeResult.Date).date)
        // MDY
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("12/25/2024", Constants.DATE_FORMAT_MDY) as DateTimeResult.Date).date)
        // YMD
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("2024/12/25", Constants.DATE_FORMAT_YMD) as DateTimeResult.Date).date)
    }

    @Test
    fun `supports interchangeable separators in numeric formats`() {
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("25.12-2024", Constants.DATE_FORMAT_DMY) as DateTimeResult.Date).date)
        assertEquals(LocalDate.of(2024, 12, 25), (DateStringParser.parse("12-25.2024", Constants.DATE_FORMAT_MDY) as DateTimeResult.Date).date)
    }

    @Test
    fun `rejects 2-digit years in numeric formats`() {
        val e = assertThrows(EvalException::class.java) {
            DateStringParser.parse("11/11/11", Constants.DATE_FORMAT_DMY)
        }
        assertTrue(e.message!!.contains("Year must be 4 digits"))
    }

    @Test
    fun `rejects out of range components in numeric formats`() {
        val e1 = assertThrows(EvalException::class.java) {
            DateStringParser.parse("32/01/2024", Constants.DATE_FORMAT_DMY)
        }
        assertTrue(e1.message!!.contains("out of range"))

        val e2 = assertThrows(EvalException::class.java) {
            DateStringParser.parse("13/01/2024", Constants.DATE_FORMAT_MDY)
        }
        assertTrue(e2.message!!.contains("out of range"))
    }

    @Test
    fun `rejects invalid dates`() {
        val e = assertThrows(EvalException::class.java) {
            DateStringParser.parse("February 30, 2019")
        }
        assertTrue(e.message!!.contains("Invalid date"))
    }

    @Test
    fun `rejects regional (non-English) month names`() {
        val e = assertThrows(EvalException::class.java) {
            // French for April is "Avril"
            DateStringParser.parse("1 Avril 2024")
        }
        assertTrue(e.message!!.contains("Unknown month name"))
    }

    @Test
    fun `rejects unknown month names`() {
        val e = assertThrows(EvalException::class.java) {
            DateStringParser.parse("Blorg 1, 2019")
        }
        assertTrue(e.message!!.contains("Unknown month name"))
    }

    @Test
    fun `parses epoch seconds`() {
        // 1718400000 -> 2024-06-14T21:20:00Z
        val result = DateStringParser.parseEpoch(1718400000L)
        // Note: result depends on system timezone, so we just check it returns an instant.
        assertNotNull((result as DateTimeResult.DateTime).instant)
    }

    @Test
    fun `is case-insensitive for month names`() {
        assertEquals(LocalDate.of(2019, 6, 10), (DateStringParser.parse("june 10, 2019") as DateTimeResult.Date).date)
        assertEquals(LocalDate.of(2019, 6, 10), (DateStringParser.parse("JUNE 10, 2019") as DateTimeResult.Date).date)
        assertEquals(LocalDate.of(2019, 6, 10), (DateStringParser.parse("10 JUNE 2019") as DateTimeResult.Date).date)
    }

    @Test
    fun `rejects malformed zone offset in ISO string`() {
        // "+25:00" is not a valid offset; ZoneId.of() throws DateTimeException which must map to EvalException.
        val e = assertThrows(EvalException::class.java) {
            DateStringParser.parse("2024-01-01+25:00")
        }
        assertTrue(e.message!!.contains("Invalid date/time"))
    }

    @Test
    fun `rejects impossible month-day combinations`() {
        val e = assertThrows(EvalException::class.java) {
            DateStringParser.parse("June 31")
        }
        assertTrue(e.message!!.contains("out of range"))
    }
}
