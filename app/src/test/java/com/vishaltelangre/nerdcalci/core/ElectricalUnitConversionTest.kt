package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import org.junit.Test
import org.junit.Assert.*

class ElectricalUnitConversionTest {

    @Test
    fun `volt to millivolt`() {
        val volt = UnitConverter.findUnit("V")!!
        val millivolt = UnitConverter.findUnit("mV")!!
        val result = UnitConverter.convert(BigDecimal.ONE, volt, millivolt, emptyMap())
        assertEquals(1000.0, result.toDouble(), 0.001)
    }

    @Test
    fun `ampere hour to coulomb`() {
        val ah = UnitConverter.findUnit("Ah")!!
        val coulomb = UnitConverter.findUnit("coulomb")!!
        val result = UnitConverter.convert(BigDecimal.ONE, ah, coulomb, emptyMap())
        assertEquals(3600.0, result.toDouble(), 0.001)
    }

    @Test
    fun `milliampere hour to coulomb`() {
        val mAh = UnitConverter.findUnit("mAh")!!
        val coulomb = UnitConverter.findUnit("coulomb")!!
        val result = UnitConverter.convert(BigDecimal.ONE, mAh, coulomb, emptyMap())
        assertEquals(3.6, result.toDouble(), 0.001)
    }

    @Test
    fun `ohm to kilohm`() {
        val ohm = UnitConverter.findUnit("ohm")!!
        val kilohm = UnitConverter.findUnit("kohm")!!
        val result = UnitConverter.convert(BigDecimal("1000.0"), ohm, kilohm, emptyMap())
        assertEquals(1.0, result.toDouble(), 0.001)
    }

    @Test
    fun `V times A results in W`() {
        val volt = UnitConverter.findUnit("V")!!
        val amp = UnitConverter.findUnit("A")!!
        assertEquals("W", UnitConverter.deriveUnit(volt, amp, TokenKind.STAR))
        
        val v = BigDecimal("10")
        val a = BigDecimal("2")
        val resultBase = UnitConverter.toBase(v, volt, emptyMap()) * UnitConverter.toBase(a, amp, emptyMap())
        val watt = UnitConverter.findUnit("W")!!
        val resultValue = UnitConverter.fromBase(resultBase, watt, emptyMap())
        assertEquals(20.0, resultValue.toDouble(), 0.001)
    }

    @Test
    fun `V divided by A results in ohm`() {
        val volt = UnitConverter.findUnit("V")!!
        val amp = UnitConverter.findUnit("A")!!
        assertEquals("ohm", UnitConverter.deriveUnit(volt, amp, TokenKind.SLASH))
        
        val v = BigDecimal("10")
        val a = BigDecimal("2")
        val resultBase = UnitConverter.toBase(v, volt, emptyMap()).divide(UnitConverter.toBase(a, amp, emptyMap()), java.math.MathContext.DECIMAL128)
        val ohm = UnitConverter.findUnit("ohm")!!
        val resultValue = UnitConverter.fromBase(resultBase, ohm, emptyMap())
        assertEquals(5.0, resultValue.toDouble(), 0.001)
    }

    @Test
    fun `mA times min results in mAmin`() {
        val mA = UnitConverter.findUnit("mA")!!
        val min = UnitConverter.findUnit("min")!!
        assertEquals("mAmin", UnitConverter.deriveUnit(mA, min, TokenKind.STAR))
        
        val valMA = BigDecimal("10")
        val valMin = BigDecimal("1")
        val resultBase = UnitConverter.toBase(valMA, mA, emptyMap()) * UnitConverter.toBase(valMin, min, emptyMap())
        val mAmin = UnitConverter.findUnit("mAmin")!!
        val resultValue = UnitConverter.fromBase(resultBase, mAmin, emptyMap())
        assertEquals(10.0, resultValue.toDouble(), 0.001)
    }

    @Test
    fun `mAh divided by h results in mA`() {
        val mAh = UnitConverter.findUnit("mAh")!!
        val h = UnitConverter.findUnit("h")!!
        assertEquals("mA", UnitConverter.deriveUnit(mAh, h, TokenKind.SLASH))
        
        val valMAh = BigDecimal("10")
        val valH = BigDecimal("1")
        val resultBase = UnitConverter.toBase(valMAh, mAh, emptyMap()).divide(UnitConverter.toBase(valH, h, emptyMap()), java.math.MathContext.DECIMAL128)
        val mA = UnitConverter.findUnit("mA")!!
        val resultValue = UnitConverter.fromBase(resultBase, mA, emptyMap())
        assertEquals(10.0, resultValue.toDouble(), 0.001)
    }

    @Test
    fun `battery usage calculation flows`() {
        val s = UnitConverter.findUnit("s")!!
        val mA = UnitConverter.findUnit("mA")!!
        val mAs = UnitConverter.findUnit("mAs")!!
        val min = UnitConverter.findUnit("min")!!
        val mAh = UnitConverter.findUnit("mAh")!!
        val mAmin = UnitConverter.findUnit("mAmin")!!

        // 60s * 35mA = 2,100 mAs
        assertEquals("mAs", UnitConverter.deriveUnit(mA, s, TokenKind.STAR))
        val flow1Base = UnitConverter.toBase(BigDecimal("60"), s, emptyMap()) * UnitConverter.toBase(BigDecimal("35"), mA, emptyMap())
        assertEquals(2100.0, UnitConverter.fromBase(flow1Base, mAs, emptyMap()).toDouble(), 0.001)

        // 2100 mAs / 1 min = 35 mA
        val flow2Base = UnitConverter.toBase(BigDecimal("2100"), mAs, emptyMap()).divide(UnitConverter.toBase(BigDecimal.ONE, min, emptyMap()), java.math.MathContext.DECIMAL128)
        assertEquals(35.0, UnitConverter.fromBase(flow2Base, mA, emptyMap()).toDouble(), 0.001)

        // 60 mAmin = 1 mAh
        val flow3Base = UnitConverter.toBase(BigDecimal("60"), mAmin, emptyMap())
        assertEquals(1.0, UnitConverter.fromBase(flow3Base, mAh, emptyMap()).toDouble(), 0.001)
    }
}
