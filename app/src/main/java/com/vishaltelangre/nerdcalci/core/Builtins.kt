package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaMathContext
import kotlin.math.*

/**
 * Registry of built-in mathematical functions and constants.
 *
 * Functions are dispatched by name and arity.
 * Constants (`PI`, `E`) are exposed via [constantValue] for bare-identifier access.
 */
object Builtins {

    private val mc = JavaMathContext.DECIMAL128
    private const val MAX_FACTORIAL_INPUT = 1000

    private data class BuiltinFn(val arity: Int, val body: (List<BigDecimal>) -> BigDecimal)

    private val functions: Map<String, BuiltinFn> = mapOf(
        // Unit-stripping placeholder helpers
        "value" to BuiltinFn(1) { it[0] },
        "dropUnit" to BuiltinFn(1) { it[0] },
        "raw" to BuiltinFn(1) { it[0] },

        // Display overrides
        "rational" to BuiltinFn(1) { it[0] },
        "fraction" to BuiltinFn(1) { it[0] },
        "float" to BuiltinFn(1) { it[0] },

        // Trigonometric (Fallback to Double)
        "sin"    to BuiltinFn(1) { BigDecimal(sin(it[0].toDouble()), mc) },
        "cos"    to BuiltinFn(1) { BigDecimal(cos(it[0].toDouble()), mc) },
        "tan"    to BuiltinFn(1) { BigDecimal(tan(it[0].toDouble()), mc) },
        "asin"   to BuiltinFn(1) { BigDecimal(asin(it[0].toDouble()), mc) },
        "acos"   to BuiltinFn(1) { BigDecimal(acos(it[0].toDouble()), mc) },
        "atan"   to BuiltinFn(1) { BigDecimal(atan(it[0].toDouble()), mc) },
        "sinh"   to BuiltinFn(1) { BigDecimal(sinh(it[0].toDouble()), mc) },
        "cosh"   to BuiltinFn(1) { BigDecimal(cosh(it[0].toDouble()), mc) },
        "tanh"   to BuiltinFn(1) { BigDecimal(tanh(it[0].toDouble()), mc) },

        // Logarithmic
        "log"    to BuiltinFn(1) { BigDecimal(ln(it[0].toDouble()), mc) },
        "log10"  to BuiltinFn(1) { BigDecimal(log10(it[0].toDouble()), mc) },
        "log2"   to BuiltinFn(1) { BigDecimal(log(it[0].toDouble(), 2.0), mc) },
        "log1p"  to BuiltinFn(1) { BigDecimal(ln(1.0 + it[0].toDouble()), mc) },

        // Power / roots
        "sqrt"   to BuiltinFn(1) { BigDecimal(sqrt(it[0].toDouble()), mc) },
        "cbrt"   to BuiltinFn(1) { BigDecimal(cbrt(it[0].toDouble()), mc) },
        "pow"    to BuiltinFn(2) {
            val base = it[0]
            val exponent = it[1]
            try {
                // Check if exponent is an exact integer
                val exponentInt = exponent.toBigIntegerExact().toInt()
                // Use BigDecimal.pow for exact integer exponents
                base.pow(exponentInt, mc)
            } catch (_: ArithmeticException) {
                // Exponent is not an exact integer, fall back to Double-based approach
                BigDecimal(base.toDouble().pow(exponent.toDouble()), mc)
            }
        },
        "exp"    to BuiltinFn(1) { BigDecimal(exp(it[0].toDouble()), mc) },
        "expm1"  to BuiltinFn(1) { BigDecimal(expm1(it[0].toDouble()), mc) },
        "factorial" to BuiltinFn(1) { factorial(it[0]) },
        "fact"      to BuiltinFn(1) { factorial(it[0]) },

        // Rounding / sign
        "abs"    to BuiltinFn(1) { it[0].abs() },
        "floor"  to BuiltinFn(1) { BigDecimal(floor(it[0].toDouble()), mc) },
        "ceil"   to BuiltinFn(1) { BigDecimal(ceil(it[0].toDouble()), mc) },
        "signum" to BuiltinFn(1) { BigDecimal(sign(it[0].toDouble()), mc) },
    )

    /** Built-in constants accessible as bare identifiers. */
    private val constants: Map<String, BigDecimal> = mapOf(
        "PI" to BigDecimal("3.1415926535897932384626433832795028841971"),
        "pi" to BigDecimal("3.1415926535897932384626433832795028841971"),
        "π"  to BigDecimal("3.1415926535897932384626433832795028841971"),
        "E"  to BigDecimal("2.7182818284590452353602874713526624977572"),
        "e"  to BigDecimal("2.7182818284590452353602874713526624977572"),
    )

    val functionNames: Set<String> get() = functions.keys

    val constantNames: Set<String> get() = constants.keys

    /**
     * Call a built-in function by [name] with the given [args].
     *
     * @throws UnknownFunctionException if no function with this name exists
     * @throws ArityMismatchException if the argument count doesn't match
     */
    fun call(name: String, args: List<BigDecimal>): BigDecimal {
        val fn = functions[name]
            ?: throw UnknownFunctionException(name)
        if (fn.arity != args.size) {
            throw ArityMismatchException(name, fn.arity, args.size)
        }
        return fn.body(args)
    }

    /** Returns true if [name] is a registered function (any arity). */
    fun isFunction(name: String): Boolean = name in functions

    /** Return the expected arity of a built-in function, or null if it's not a function. */
    fun getArity(name: String): Int? = functions[name]?.arity

    /** Returns true if [name] is a built-in constant usable as a bare identifier. */
    fun isConstant(name: String): Boolean = name in constants

    /** Get the value of a bare constant, or null if [name] isn't one. */
    fun constantValue(name: String): BigDecimal? = constants[name]

    /** Returns true if [name] is any built-in (function or constant). */
    fun isBuiltin(name: String): Boolean = isFunction(name) || isConstant(name)

    private fun factorial(value: BigDecimal): BigDecimal {
        val integerValue = try {
            value.toBigIntegerExact()
        } catch (_: ArithmeticException) {
            throw EvalException("Factorial is only defined for whole numbers")
        }

        if (integerValue.signum() < 0) {
            throw EvalException("Factorial is only defined for non-negative whole numbers")
        }

        if (integerValue > BigInteger.valueOf(MAX_FACTORIAL_INPUT.toLong())) {
            throw EvalException("Factorial is only supported up to $MAX_FACTORIAL_INPUT")
        }

        var result = BigInteger.ONE
        var current = BigInteger.valueOf(2)
        while (current <= integerValue) {
            result = result.multiply(current)
            current = current.add(BigInteger.ONE)
        }
        return BigDecimal(result)
    }
}
