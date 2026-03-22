package com.vishaltelangre.nerdcalci.core

import org.junit.Test
import org.junit.Assert.*

class UnitConverterTest {

    @Test
    fun `test data unit case sensitivity for bits and bytes`() {
        val bitUnit = UnitConverter.findUnit("b")
        val byteUnit = UnitConverter.findUnit("B")

        assertNotNull("Should find 'b' (Bit)", bitUnit)
        assertNotNull("Should find 'B' (Byte)", byteUnit)

        assertEquals("Bit", bitUnit!!.name)
        assertEquals("Byte", byteUnit!!.name)
    }

    @Test
    fun `test all excludedDataSymbols preserve case sensitivity`() {
        val excludedDataSymbols = listOf("B", "kB", "KB", "MB", "GB", "TB", "PB", "EB", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
        for (symbol in excludedDataSymbols) {
            val unit = UnitConverter.findUnit(symbol)
            assertNotNull("Should find unit for $symbol", unit)
            val lowerUnit = UnitConverter.findUnit(symbol.lowercase())
            assertNotEquals("Lowercase for $symbol should NOT resolve to the same unit", unit, lowerUnit)
        }
    }

    @Test
    fun `test and lowercase for non-excluded symbols works`() {
        val kg1 = UnitConverter.findUnit("kg")
        val kg2 = UnitConverter.findUnit("KG")
        assertNotNull(kg1)
        assertNotNull(kg2)
        assertEquals(kg1!!.name, kg2!!.name)
    }
}
