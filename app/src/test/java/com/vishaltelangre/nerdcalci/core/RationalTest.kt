package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class RationalTest {

    @Test
    fun testSimplification() {
        assertEquals("1/2", Rational(2.toBigInteger(), 4.toBigInteger()).toString())
        assertEquals("1/3", Rational(10.toBigInteger(), 30.toBigInteger()).toString())
        assertEquals("-1/2", Rational((-2).toBigInteger(), 4.toBigInteger()).toString())
        assertEquals("-1/2", Rational(2.toBigInteger(), (-4).toBigInteger()).toString())
        assertEquals("1/2", Rational((-2).toBigInteger(), (-4).toBigInteger()).toString())
        assertEquals("5", Rational(5.toBigInteger(), 1.toBigInteger()).toString())
        assertEquals("0", Rational(0.toBigInteger(), 5.toBigInteger()).toString())
    }

    @Test
    fun testAddition() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(1.toBigInteger(), 3.toBigInteger())
        assertEquals("5/6", (r1 + r2).toString())

        val r3 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r4 = Rational(1.toBigInteger(), 2.toBigInteger())
        assertEquals("1", (r3 + r4).toString())
    }

    @Test
    fun testSubtraction() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(1.toBigInteger(), 3.toBigInteger())
        assertEquals("1/6", (r1 - r2).toString())
    }

    @Test
    fun testMultiplication() {
        val r1 = Rational(2.toBigInteger(), 3.toBigInteger())
        val r2 = Rational(3.toBigInteger(), 4.toBigInteger())
        assertEquals("1/2", (r1 * r2).toString())
    }

    @Test
    fun testDivision() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(3.toBigInteger(), 4.toBigInteger())
        assertEquals("2/3", (r1 / r2).toString())
    }

    @Test
    fun testToRational() {
        assertEquals("1/2", Rational.toRational(BigDecimal("0.5")).toString())
        assertEquals("1/4", Rational.toRational(BigDecimal("0.25")).toString())
        assertEquals("33/10", Rational.toRational(BigDecimal("3.3")).toString())
        assertEquals("7", Rational.toRational(BigDecimal("7")).toString())
    }
}
