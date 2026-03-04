package com.vishaltelangre.nerdcalci.core

import kotlin.math.*

/**
 * Registry of built-in mathematical functions and constants.
 *
 * Functions are dispatched by name and arity.
 * Constants (`PI`, `E`) are exposed via [constantValue] for bare-identifier access.
 */
object Builtins {

    private data class BuiltinFn(val arity: Int, val body: (List<Double>) -> Double)

    private val functions: Map<String, BuiltinFn> = mapOf(
        // Trigonometric
        "sin"    to BuiltinFn(1) { sin(it[0]) },
        "cos"    to BuiltinFn(1) { cos(it[0]) },
        "tan"    to BuiltinFn(1) { tan(it[0]) },
        "asin"   to BuiltinFn(1) { asin(it[0]) },
        "acos"   to BuiltinFn(1) { acos(it[0]) },
        "atan"   to BuiltinFn(1) { atan(it[0]) },
        "sinh"   to BuiltinFn(1) { sinh(it[0]) },
        "cosh"   to BuiltinFn(1) { cosh(it[0]) },
        "tanh"   to BuiltinFn(1) { tanh(it[0]) },

        // Logarithmic
        "log"    to BuiltinFn(1) { ln(it[0]) },
        "log10"  to BuiltinFn(1) { log10(it[0]) },
        "log2"   to BuiltinFn(1) { log(it[0], 2.0) },
        "log1p"  to BuiltinFn(1) { ln(1.0 + it[0]) },

        // Power / roots
        "sqrt"   to BuiltinFn(1) { sqrt(it[0]) },
        "cbrt"   to BuiltinFn(1) { cbrt(it[0]) },
        "pow"    to BuiltinFn(2) { it[0].pow(it[1]) },
        "exp"    to BuiltinFn(1) { exp(it[0]) },
        "expm1"  to BuiltinFn(1) { expm1(it[0]) },

        // Rounding / sign
        "abs"    to BuiltinFn(1) { abs(it[0]) },
        "floor"  to BuiltinFn(1) { floor(it[0]) },
        "ceil"   to BuiltinFn(1) { ceil(it[0]) },
        "signum" to BuiltinFn(1) { sign(it[0]) },
    )

    /** Built-in constants accessible as bare identifiers. */
    private val constants: Map<String, Double> = mapOf(
        "PI" to PI,
        "E"  to E,
    )

    /**
     * Call a built-in function by [name] with the given [args].
     *
     * @throws UnknownFunctionException if no function with this name exists
     * @throws ArityMismatchException if the argument count doesn't match
     */
    fun call(name: String, args: List<Double>): Double {
        val fn = functions[name]
            ?: throw UnknownFunctionException(name)
        if (fn.arity != args.size) {
            throw ArityMismatchException(name, fn.arity, args.size)
        }
        return fn.body(args)
    }

    /** Returns true if [name] is a registered function (any arity). */
    fun isFunction(name: String): Boolean = name in functions

    /** Returns true if [name] is a built-in constant usable as a bare identifier. */
    fun isConstant(name: String): Boolean = name in constants

    /** Get the value of a bare constant, or null if [name] isn't one. */
    fun constantValue(name: String): Double? = constants[name]

    /** Returns true if [name] is any built-in (function or constant). */
    fun isBuiltin(name: String): Boolean = isFunction(name) || isConstant(name)
}
