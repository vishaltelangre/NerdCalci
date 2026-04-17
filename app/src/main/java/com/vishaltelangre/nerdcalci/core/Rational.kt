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

        /**
         * Converts a BigDecimal to a Rational using continued fractions to find a "simple" 
         * representation. Stops when the approximation is within [tolerance].
         * Standard tolerance is 1e-15 for finding "nice" fractions for constants/decimals.
         */
        fun fromBigDecimalSmart(
            value: BigDecimal, 
            tolerance: BigDecimal = BigDecimal("1e-15")
        ): Rational {
            val sign = value.signum()
            val absoluteValue = value.abs()
            
            var n1 = BigInteger.ONE
            var d1 = BigInteger.ZERO
            var n2 = BigInteger.ZERO
            var d2 = BigInteger.ONE
            
            var currentVal = absoluteValue
            
            for (i in 0..100) { // Limit iterations to prevent infinite loops for truly irrational numbers
                val a = currentVal.toBigInteger()
                val nextN = a * n1 + n2
                val nextD = a * d1 + d2
                
                // Update for next iteration
                n2 = n1
                d2 = d1
                n1 = nextN
                d1 = nextD
                
                val currentRational = Rational(n1, d1)
                val diff = (currentRational.toBigDecimal(JavaMathContext.DECIMAL128) - absoluteValue).abs()
                if (diff <= tolerance && !(n1 == BigInteger.ZERO && absoluteValue.signum() > 0)) break
                
                val fractionalPart = currentVal - a.toBigDecimal()
                if (fractionalPart.signum() == 0) break
                currentVal = BigDecimal.ONE.divide(fractionalPart, JavaMathContext.DECIMAL128)
            }
            
            return if (sign >= 0) Rational(n1, d1) else Rational(n1.negate(), d1)
        }
    }
}