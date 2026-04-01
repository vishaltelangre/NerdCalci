package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaMathContext

/**
 * Represents a rational number (fraction) with an exact numerator and denominator.
 * Automatically simplifies the fraction by dividing both by their greatest common divisor (GCD).
 */
data class Rational(private val initialNumerator: BigInteger, private val initialDenominator: BigInteger) {
    val num: BigInteger
    val den: BigInteger

    init {
        require(initialDenominator != BigInteger.ZERO) { "Denominator cannot be zero" }
        val gcd = initialNumerator.abs().gcd(initialDenominator.abs())
        val sign = if (initialDenominator.signum() < 0) -1 else 1
        num = (initialNumerator / gcd) * BigInteger.valueOf(sign.toLong())
        den = (initialDenominator / gcd) * BigInteger.valueOf(sign.toLong())
    }

    operator fun plus(other: Rational): Rational {
        return Rational(
            num * other.den + other.num * den,
            den * other.den
        )
    }

    operator fun minus(other: Rational): Rational {
        return Rational(
            num * other.den - other.num * den,
            den * other.den
        )
    }

    operator fun times(other: Rational): Rational {
        return Rational(num * other.num, den * other.den)
    }

    operator fun div(other: Rational): Rational {
        if (other.num == BigInteger.ZERO) {
            throw DivisionByZeroException()
        }
        return Rational(num * other.den, den * other.num)
    }

    fun negate(): Rational = Rational(num.negate(), den)

    fun toBigDecimal(mc: JavaMathContext): BigDecimal {
        return BigDecimal(num).divide(BigDecimal(den), mc)
    }

    fun isWhole(): Boolean = den == BigInteger.ONE

    override fun toString(): String {
        return if (isWhole()) num.toString() else "$num/$den"
    }

    companion object {
        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Rational(BigInteger.ONE, BigInteger.ONE)

        fun toRational(value: BigDecimal): Rational {
            val scale = value.scale()
            return if (scale <= 0) {
                Rational(value.toBigIntegerExact(), BigInteger.ONE)
            } else {
                val numerator = value.unscaledValue()
                val denominator = BigInteger.TEN.pow(scale)
                Rational(numerator, denominator)
            }
        }

        fun fromLong(value: Long): Rational = Rational(BigInteger.valueOf(value), BigInteger.ONE)
    }
}