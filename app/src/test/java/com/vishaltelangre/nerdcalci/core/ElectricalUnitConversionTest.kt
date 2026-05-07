package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import org.junit.Test
import org.junit.Assert.*

class ElectricalUnitConversionTest {

    @Test
    fun `test volt to millivolt conversion`() {
        val volt = UnitConverter.findUnit("V")!!
        val millivolt = UnitConverter.findUnit("mV")!!
        val result = UnitConverter.convert(BigDecimal.ONE, volt, millivolt, emptyMap())
        assertEquals(0, BigDecimal("1000.0").compareTo(result))
    }

    @Test
    fun `test amp hour to coulomb conversion`() {
        val ampInHour = UnitConverter.findUnit("Ah")!!
        val coulomb = UnitConverter.findUnit("C")!!
        val result = UnitConverter.convert(BigDecimal.ONE, ampInHour, coulomb, emptyMap())
        assertEquals(0, BigDecimal("3600.0").compareTo(result))
    }

    @Test
    fun `test ohm to kilohm conversion`() {
        val ohm = UnitConverter.findUnit("ohm")!!
        val kilohm = UnitConverter.findUnit("kohm")!!
        val result = UnitConverter.convert(BigDecimal("1000.0"), ohm, kilohm, emptyMap())
        assertEquals(0, BigDecimal.ONE.compareTo(result))
    }

    @Test
    fun `test voltage current power derivation`() {
        val volt = UnitConverter.findUnit("V")!!
        val amp = UnitConverter.findUnit("A")!!
        val wattSym = UnitConverter.deriveUnit(volt, amp, TokenKind.STAR)
        assertEquals("W", wattSym)
    }

    @Test
    fun `test ohm law derivation`() {
        val volt = UnitConverter.findUnit("V")!!
        val amp = UnitConverter.findUnit("A")!!
        val ohmSym = UnitConverter.deriveUnit(volt, amp, TokenKind.SLASH)
        assertEquals("ohm", ohmSym)
    }

    @Test
    fun `test charge derivation`() {
        val amp = UnitConverter.findUnit("A")!!
        val second = UnitConverter.findUnit("s")!!
        val chargeSym = UnitConverter.deriveUnit(amp, second, TokenKind.STAR)
        assertEquals("As", chargeSym)
        
        val hour = UnitConverter.findUnit("h")!!
        val ahSym = UnitConverter.deriveUnit(amp, hour, TokenKind.STAR)
        assertEquals("Ah", ahSym)
    }

    @Test
    fun `test current derivation from charge`() {
        val coulomb = UnitConverter.findUnit("C")!!
        val second = UnitConverter.findUnit("s")!!
        val ampSym = UnitConverter.deriveUnit(coulomb, second, TokenKind.SLASH)
        assertEquals("A", ampSym)
    }
}
