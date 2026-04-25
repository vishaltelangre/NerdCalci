package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class RationalTest {

    @Test
    fun `simplification reduces fractions to lowest terms`() {
        assertEquals("1/2", Rational(2.toBigInteger(), 4.toBigInteger()).toString())
        assertEquals("1/3", Rational(10.toBigInteger(), 30.toBigInteger()).toString())
        assertEquals("-1/2", Rational((-2).toBigInteger(), 4.toBigInteger()).toString())
        assertEquals("-1/2", Rational(2.toBigInteger(), (-4).toBigInteger()).toString())
        assertEquals("1/2", Rational((-2).toBigInteger(), (-4).toBigInteger()).toString())
        assertEquals("5", Rational(5.toBigInteger(), 1.toBigInteger()).toString())
        assertEquals("0", Rational(0.toBigInteger(), 5.toBigInteger()).toString())
    }

    @Test
    fun `addition correctly calculates sum of fractions`() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(1.toBigInteger(), 3.toBigInteger())
        assertEquals("5/6", (r1 + r2).toString())

        val r3 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r4 = Rational(1.toBigInteger(), 2.toBigInteger())
        assertEquals("1", (r3 + r4).toString())
    }

    @Test
    fun `subtraction correctly calculates difference of fractions`() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(1.toBigInteger(), 3.toBigInteger())
        assertEquals("1/6", (r1 - r2).toString())
    }

    @Test
    fun `multiplication correctly calculates product of fractions`() {
        val r1 = Rational(2.toBigInteger(), 3.toBigInteger())
        val r2 = Rational(3.toBigInteger(), 4.toBigInteger())
        assertEquals("1/2", (r1 * r2).toString())
    }

    @Test
    fun `division correctly calculates quotient of fractions`() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val r2 = Rational(3.toBigInteger(), 4.toBigInteger())
        assertEquals("2/3", (r1 / r2).toString())
    }

    @Test
    fun `division by zero throws DivisionByZeroException`() {
        val r1 = Rational(1.toBigInteger(), 2.toBigInteger())
        val zero = Rational.ZERO

        try {
            r1 / zero
            throw AssertionError("Expected DivisionByZeroException")
        } catch (_: DivisionByZeroException) {
            // Expected
        }
    }

    @Test
    fun `toRational converts simple decimals to exact fractions`() {
        assertEquals("1/2", Rational.toRational(BigDecimal("0.5")).toString())
        assertEquals("1/4", Rational.toRational(BigDecimal("0.25")).toString())
        assertEquals("33/10", Rational.toRational(BigDecimal("3.3")).toString())
        assertEquals("7", Rational.toRational(BigDecimal("7")).toString())
    }

    @Test
    fun `toRational handles negative decimal values`() {
        assertEquals("-1/2", Rational.toRational(BigDecimal("-0.5")).toString())
        assertEquals("-3/4", Rational.toRational(BigDecimal("-0.75")).toString())
    }

    @Test
    fun `negate and isWhole provide correct rational properties`() {
        val whole = Rational(5.toBigInteger(), 1.toBigInteger())
        val fraction = Rational(3.toBigInteger(), 4.toBigInteger())

        assertEquals("-5", whole.negate().toString())
        assertEquals("-3/4", fraction.negate().toString())
        assertTrue(whole.isWhole())
        assertFalse(fraction.isWhole())
    }

    @Test
    fun `fromBigDecimalSmart approximates repeating decimals while respecting limits`() {
        // Simple fractions
        assertEquals("1/3", Rational.fromBigDecimalSmart(BigDecimal("0.3333333333333333")).toString())
        assertEquals("1/7", Rational.fromBigDecimalSmart(BigDecimal("0.14285714285714285")).toString())
        
        // Very small non-zero values (within limits)
        assertEquals("1/100000000000000000", Rational.fromBigDecimalSmart(BigDecimal("1e-17")).toString())
        
        // Extreme scientific notation (should return null due to complexity limits)
        assertTrue(Rational.fromBigDecimalSmart(BigDecimal("1.0E-10000")) == null)
        
        // Zero should still be zero
        assertEquals("0", Rational.fromBigDecimalSmart(BigDecimal.ZERO).toString())
    }

    @Test
    fun `toRational enforces exact exponent limits for conversions`() {
        // Within limits
        val withinLimit = BigDecimal("1.0").movePointLeft(1000)
        assertTrue(Rational.toRational(withinLimit) != null)

        // Beyond exact exponent limit (MAX_EXACT_EXPONENT = 10000)
        val beyondLimit = BigDecimal("1.0").movePointLeft(10001)
        assertTrue(Rational.toRational(beyondLimit) == null)
    }

    @Test
    fun `arithmetic operations throw exception if result exceeds complexity limits`() {
        // Create a large rational near the bit limit (16000 bits)
        val largeNum = BigInteger.valueOf(2).pow(15000)
        val r1 = Rational(largeNum, BigInteger.ONE)
        
        // r1 * r1 should exceed 16000 bits
        try {
            r1 * r1
            throw AssertionError("Expected ArithmeticException for too complex rational")
        } catch (_: ArithmeticException) {
            // Expected
        }
    }

    @Test
    fun `equality and hashcode handle simplified fractions correctly`() {
        val r1 = Rational(BigInteger.valueOf(1), BigInteger.valueOf(2))
        val r2 = Rational(BigInteger.valueOf(2), BigInteger.valueOf(4)) // Simplifies to 1/2
        val r3 = Rational(BigInteger.valueOf(1), BigInteger.valueOf(3))
        
        assertTrue(r1 == r2)
        assertEquals(r1.hashCode(), r2.hashCode())
        assertFalse(r1 == r3)
    }

    @Test
    fun `toLongOrNull validates integral values within long range`() {
        assertEquals(123L, Rational.fromLong(123).toLongOrNull())
        assertEquals(null, Rational(BigInteger.ONE, BigInteger.valueOf(2)).toLongOrNull())
        
        // Too large for Long
        val large = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)
        assertEquals(null, Rational(large, BigInteger.ONE).toLongOrNull())
    }

    @Test
    fun `compareTo provides correct ordering for rational fractions`() {
        val r1 = Rational(BigInteger.valueOf(1), BigInteger.valueOf(2))
        val r2 = Rational(BigInteger.valueOf(1), BigInteger.valueOf(3))
        
        assertTrue(r1 > r2)
        assertTrue(r2 < r1)
        assertTrue(r1.compareTo(Rational(BigInteger.valueOf(2), BigInteger.valueOf(4))) == 0)
    }
}
