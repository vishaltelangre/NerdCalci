package com.vishaltelangre.nerdcalci.core

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class PrecisionEllipsisTest {

    private val engine = MathEngine

    @Test
    fun `formatDisplayResult supports precision ellipsis`() = runBlocking {
        val locale = Locale.US
        val value = "1.2345678"

        // Without ellipsis (should round HALF_UP)
        assertEquals("1.23", engine.formatDisplayResult(value, 2, locale, "US", showEllipsis = false))
        assertEquals("1.235", engine.formatDisplayResult(value, 3, locale, "US", showEllipsis = false))

        // With ellipsis (should round DOWN and append …)
        assertEquals("1.23…", engine.formatDisplayResult(value, 2, locale, "US", showEllipsis = true))
        assertEquals("1.234…", engine.formatDisplayResult(value, 3, locale, "US", showEllipsis = true))

        // With unit
        assertEquals("1.23… kg", engine.formatDisplayResult(value + " kg", 2, locale, "US", showEllipsis = true))

        // No truncation = no ellipsis
        assertEquals("1.23", engine.formatDisplayResult("1.23", 2, locale, "US", showEllipsis = true))
        assertEquals("1.23 kg", engine.formatDisplayResult("1.23 kg", 2, locale, "US", showEllipsis = true))
    }

    @Test
    fun `standard notation ellipsis - truncated`() {
        val result = engine.formatDisplayResult(
            rawResult = "1.23456",
            precision = 2,
            showEllipsis = true
        )
        // 1.23456 truncated to 2 decimals with DOWN rounding (because showEllipsis=true) -> 1.23
        assertEquals("1.23…", result)
    }

    @Test
    fun `standard notation ellipsis - truncated negative`() {
        val result = engine.formatDisplayResult(
            rawResult = "-1.23456",
            precision = 2,
            showEllipsis = true
        )
        // -1.23456 truncated to 2 decimals with DOWN rounding (towards zero) -> -1.23
        assertEquals("-1.23…", result)
    }

    @Test
    fun `standard notation ellipsis - exact`() {
        val result = engine.formatDisplayResult(
            rawResult = "1.23",
            precision = 2,
            showEllipsis = true
        )
        assertEquals("1.23", result)
    }

    @Test
    fun `scientific notation ellipsis - truncated large number`() {
        // 1.23456E15 with precision 2
        // Mantissa 1.23456 truncated to 1.23
        val result = engine.formatDisplayResult(
            rawResult = "1234560000000000",
            precision = 2,
            showEllipsis = true
        )
        // 1.23456E15 -> 1.23E15...
        assertEquals("1.23E15…", result)
    }

    @Test
    fun `scientific notation ellipsis - exact large number`() {
        val result = engine.formatDisplayResult(
            rawResult = "1230000000000000",
            precision = 2,
            showEllipsis = true
        )
        assertEquals("1.23E15", result)
    }

    @Test
    fun `scientific notation ellipsis - truncated small number`() {
        // 0.000123456 -> 1.23456E-4
        val result = engine.formatDisplayResult(
            rawResult = "0.000123456",
            precision = 2,
            showEllipsis = true
        )
        assertEquals("1.23E-4…", result)
    }

    @Test
    fun `scientific notation ellipsis - rounding up mantissa to 10`() {
        // 9.999E15 with precision 1
        // Mantissa 9.999 rounded HALF_UP to 1 decimal is 10.0
        // isScientificTruncated uses HALF_UP to detect truncation.
        // 9.999 != 10.0 -> truncated = true
        // Then roundingMode = DOWN (because showEllipsis = true)
        // formatScientific(9.999E15, precision 1, DOWN) -> 9.9E15
        val result = engine.formatDisplayResult(
            rawResult = "9999000000000000",
            precision = 1,
            showEllipsis = true
        )
        assertEquals("9.9E15…", result)
    }

    @Test
    fun `scientific notation ellipsis - forced by E notation in input`() {
        val result = engine.formatDisplayResult(
            rawResult = "1.23456E2",
            precision = 2,
            showEllipsis = true
        )
        // 1.23456E2 -> 1.23E2...
        assertEquals("1.23E2…", result)
    }

    @Test
    fun `scientific notation ellipsis - truncated negative`() {
        val result = engine.formatDisplayResult(
            rawResult = "-1.23456E2",
            precision = 2,
            showEllipsis = true
        )
        // -1.23456E2 -> -1.23E2...
        assertEquals("-1.23E2…", result)
    }
}
