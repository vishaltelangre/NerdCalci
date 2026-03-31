package com.vishaltelangre.nerdcalci.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ExportUtilsUnitTest {

    @Test
    fun `formatResultText honors precision`() {
        val rawResult = "0.3333333333333333"

        val result2 = ExportUtils.formatResultText(rawResult, 2)
        assertEquals("0.33", result2)

        val result4 = ExportUtils.formatResultText(rawResult, 4)
        assertEquals("0.3333", result4)
    }

    @Test
    fun `formatResultText handles quantity with precision`() {
        val rawResult = "0.3333333333333333 km"

        val result2 = ExportUtils.formatResultText(rawResult, 2)
        assertEquals("0.33 km", result2)
    }

    @Test
    fun `formatResultText handles error`() {
        assertEquals("Err", ExportUtils.formatResultText("Err", 2))
    }

    @Test
    fun `formatResultText handles blank value`() {
        assertEquals("", ExportUtils.formatResultText("", 2))
    }

    @Test
    fun `formatResultText adds grouping separators for large integers`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("1,234,567", ExportUtils.formatResultText("1234567", 2))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `formatResultText adds grouping separators for decimals`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("1,234.50", ExportUtils.formatResultText("1234.5", 2))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
