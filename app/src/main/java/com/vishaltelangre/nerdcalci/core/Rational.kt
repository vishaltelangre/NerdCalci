package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaMathContext

/**
 * Represents a rational number (fraction) with an exact numerator and denominator.
 * Automatically simplifies the fraction by dividing both by their greatest common divisor (GCD).
 */
class Rational(numerator: BigInteger, denominator: BigInteger) : Comparable<Rational> {
    val num: BigInteger
    val den: BigInteger

    init {
        require(denominator != BigInteger.ZERO) { "Denominator cannot be zero" }
        if (isTooComplex(numerator, denominator)) {
            throw ArithmeticException("Rational number is too complex")
        }
        val gcd = numerator.abs().gcd(denominator.abs())
        val sign = if (denominator.signum() < 0) -1 else 1
        num = (numerator / gcd) * BigInteger.valueOf(sign.toLong())
        den = (denominator / gcd) * BigInteger.valueOf(sign.toLong())
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

    fun toLongOrNull(): Long? {
        if (!isWhole()) return null
        if (num > BigInteger.valueOf(Long.MAX_VALUE) || num < BigInteger.valueOf(Long.MIN_VALUE)) return null
        return num.toLong()
    }

    override fun toString(): String {
        return if (isWhole()) num.toString() else "$num/$den"
    }

    override fun compareTo(other: Rational): Int {
        return (num * other.den).compareTo(other.num * den)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rational) return false
        return num == other.num && den == other.den
    }

    override fun hashCode(): Int {
        var result = num.hashCode()
        result = 31 * result + den.hashCode()
        return result
    }

    companion object {
        private const val MAX_RATIONAL_BIT_LENGTH = 16000 // approx 4.8k digits
        private const val MAX_EXACT_EXPONENT = 10000

        internal fun isTooComplex(numerator: BigInteger, denominator: BigInteger): Boolean {
            return numerator.bitLength() > MAX_RATIONAL_BIT_LENGTH ||
                    denominator.bitLength() > MAX_RATIONAL_BIT_LENGTH
        }

        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Rational(BigInteger.ONE, BigInteger.ONE)

        fun toRational(value: BigDecimal): Rational? {
            val scale = value.scale()
            // Quick check for extreme scales before any heavy calculation
            if (scale > MAX_EXACT_EXPONENT || scale < -MAX_EXACT_EXPONENT) {
                return null
            }

            return try {
                if (scale <= 0) {
                    val result = value.toBigIntegerExact()
                    if (isTooComplex(result, BigInteger.ONE)) null
                    else Rational(result, BigInteger.ONE)
                } else {
                    val numerator = value.unscaledValue()
                    val denominator = BigInteger.TEN.pow(scale)
                    if (isTooComplex(numerator, denominator)) null
                    else Rational(numerator, denominator)
                }
            } catch (_: Exception) {
                null
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
        ): Rational? {
            val sign = value.signum()
            val absoluteValue = value.abs()
            
            var n1 = BigInteger.ONE
            var d1 = BigInteger.ZERO
            var n2 = BigInteger.ZERO
            var d2 = BigInteger.ONE
            
            var currentVal = absoluteValue
            
            for (i in 0..100) { // Limit iterations to prevent infinite loops for truly irrational numbers
                val a = try { currentVal.toBigInteger() } catch (_: Exception) { break }
                val nextN = a * n1 + n2
                val nextD = a * d1 + d2
                
                if (isTooComplex(nextN, nextD)) break

                // Update for next iteration
                n2 = n1
                d2 = d1
                n1 = nextN
                d1 = nextD
                
                val currentRational = try { Rational(n1, d1) } catch (_: Exception) { break }
                val diff = (currentRational.toBigDecimal(JavaMathContext.DECIMAL128) - absoluteValue).abs()
                if (diff <= tolerance && !(n1 == BigInteger.ZERO && absoluteValue.signum() > 0)) break
                
                val fractionalPart = currentVal - a.toBigDecimal()
                if (fractionalPart.signum() == 0) break
                currentVal = try {
                    BigDecimal.ONE.divide(fractionalPart, JavaMathContext.DECIMAL128)
                } catch (_: Exception) {
                    break
                }
            }
            
            if (d1 == BigInteger.ZERO) return null 
            if (isTooComplex(n1, d1)) return null
            
            val result = if (sign >= 0) Rational(n1, d1) else Rational(n1.negate(), d1)
            // If the approximation resulted in zero but the original value was not zero,
            // it means the number is too small to be represented as a "simple" rational 
            // within our complexity limits. Fall back to BigDecimal.
            if (result.num.signum() == 0 && value.signum() != 0) return null
            
            return result
        }
    }
}