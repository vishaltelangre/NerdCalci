package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaMathContext
import kotlin.math.*

/**
 * Registry of built-in mathematical functions and constants.
 *
 * Functions are dispatched by name and arity.
 * Constants (`PI`, `e`) are exposed via [constantValue] for bare-identifier access.
 */
object Builtins {

    private val mc = JavaMathContext.DECIMAL128
    private const val MAX_FACTORIAL_INPUT = 1000

    sealed interface UnitInputSpec {
        fun accepts(arg: EvaluationResult): Boolean
        fun normalize(arg: EvaluationResult, variables: Map<String, EvaluationResult>): BigDecimal
        fun description(): String
    }

    data object RequireUnitlessInput : UnitInputSpec {
        override fun accepts(arg: EvaluationResult): Boolean = arg.value != null && (arg.unit == null || arg.explicitUnitless)
        override fun normalize(arg: EvaluationResult, variables: Map<String, EvaluationResult>): BigDecimal {
            if (!accepts(arg)) throw EvalException("This function requires unitless inputs")
            return arg.value ?: BigDecimal.ZERO
        }
        override fun description(): String = "a unitless value"
    }

    data object RequireAngleInput : UnitInputSpec {
        override fun accepts(arg: EvaluationResult): Boolean {
            val unit = arg.unit?.let { UnitConverter.findUnit(it) }
            return arg.value != null && unit?.category == UnitCategory.ANGLE
        }

        override fun normalize(arg: EvaluationResult, variables: Map<String, EvaluationResult>): BigDecimal {
            if (!accepts(arg)) throw EvalException("This function accepts only angle inputs")
            return arg.value ?: BigDecimal.ZERO
        }
        override fun description(): String = "an angle value"
    }

    data object AnyNumericInput : UnitInputSpec {
        override fun accepts(arg: EvaluationResult): Boolean = arg.value != null
        override fun normalize(arg: EvaluationResult, variables: Map<String, EvaluationResult>): BigDecimal = arg.value ?: BigDecimal.ZERO
        override fun description(): String = "a numeric value"
    }

    data class OneOf(val options: List<UnitInputSpec>) : UnitInputSpec {
        override fun accepts(arg: EvaluationResult): Boolean = options.any { it.accepts(arg) }
        override fun normalize(arg: EvaluationResult, variables: Map<String, EvaluationResult>): BigDecimal {
            val matched = options.firstOrNull { it.accepts(arg) }
                ?: throw EvalException("Function does not accept the given input type")
            return matched.normalize(arg, variables)
        }
        override fun description(): String = options.joinToString(" or ") { it.description() }
    }

    sealed interface UnitPolicy {
        fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult
    }

    data object RequireUnitlessInputPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            if (args.any { it.unit != null && !it.explicitUnitless }) {
                throw EvalException("This function requires unitless inputs")
            }
            val resultValue = body(args.map { it.value ?: BigDecimal.ZERO })
            return EvaluationResult(resultValue, rationalValue = Rational.fromBigDecimalSmart(resultValue))
        }
    }

    data object PreserveInputUnitResultPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            val input = args.first()
            val inputUnit = input.unit?.let { UnitConverter.findUnit(it) }
            if (inputUnit == null) {
                val resultValue = body(args.map { it.value ?: BigDecimal.ZERO })
                return EvaluationResult(
                    resultValue,
                    explicitUnitless = input.explicitUnitless,
                    rationalValue = Rational.fromBigDecimalSmart(resultValue)
                )
            }
            val displayValue = UnitConverter.fromBase(input.value ?: BigDecimal.ZERO, inputUnit, variables)
            val transformedDisplayValue = body(listOf(displayValue))
            val baseValue = UnitConverter.toBase(transformedDisplayValue, inputUnit, variables)
            return EvaluationResult(
                baseValue,
                unit = input.unit,
                explicitUnitless = false,
                rationalValue = Rational.fromBigDecimalSmart(baseValue)
            )
        }
    }

    data object DropUnitResultPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            val input = args.first()
            val inputUnit = input.unit?.let { UnitConverter.findUnit(it) }
            val displayArgs = if (inputUnit == null) {
                args.map { it.value ?: BigDecimal.ZERO }
            } else {
                listOf(UnitConverter.fromBase(input.value ?: BigDecimal.ZERO, inputUnit, variables))
            }
            val resultValue = body(displayArgs)
            return EvaluationResult(
                resultValue,
                unit = null,
                explicitUnitless = true,
                rationalValue = Rational.fromBigDecimalSmart(resultValue)
            )
        }
    }

    data object RationalDisplayPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            val input = args.first()
            val value = input.value ?: BigDecimal.ZERO
            val rational = Rational.fromBigDecimalSmart(value) ?: return input.copy(value = value, explicitRational = false)
            return input.copy(value = value, rationalValue = rational, explicitRational = true)
        }
    }

    data object FloatDisplayPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            return args.first().copy(forceFloat = true)
        }
    }

    data object UnitlessNumericResultPolicy : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            val resultValue = body(args.map { it.value ?: BigDecimal.ZERO })
            return EvaluationResult(
                resultValue,
                unit = null,
                explicitUnitless = false,
                rationalValue = Rational.fromBigDecimalSmart(resultValue)
            )
        }
    }

    data class TransformUnitByPower(val exponent: Int) : UnitPolicy {
        override fun evaluate(
            args: List<EvaluationResult>,
            body: (List<BigDecimal>) -> BigDecimal,
            variables: Map<String, EvaluationResult>
        ): EvaluationResult {
            val input = args.first()
            val inputUnit = input.unit?.let { UnitConverter.findUnit(it) }
            if (inputUnit == null) {
                val resultValue = body(args.map { it.value ?: BigDecimal.ZERO })
                return EvaluationResult(resultValue, rationalValue = Rational.fromBigDecimalSmart(resultValue))
            }
            val derivedUnit = UnitConverter.deriveForRoot(inputUnit, exponent)
                ?: throw EvalException(
                    when (exponent) {
                        2 -> "`sqrt()` requires a unitless value or a squared unit."
                        3 -> "`cbrt()` requires a unitless value or a cubed unit."
                        else -> "Power is not supported for ${inputUnit.name}."
                    }
                )
            val displayValue = UnitConverter.fromBase(input.value ?: BigDecimal.ZERO, inputUnit, variables)
            val transformedDisplayValue = body(listOf(displayValue))
            if (derivedUnit == "unitless") {
                return EvaluationResult(
                    transformedDisplayValue,
                    explicitUnitless = false,
                    rationalValue = Rational.fromBigDecimalSmart(transformedDisplayValue)
                )
            }
            val derived = UnitConverter.findUnit(derivedUnit) ?: throw EvalException(
                when (exponent) {
                    2 -> "`sqrt()` requires a unitless value or a squared unit."
                    3 -> "`cbrt()` requires a unitless value or a cubed unit."
                    else -> "Power is not supported for ${inputUnit.name}."
                }
            )
            val baseValue = UnitConverter.toBase(transformedDisplayValue, derived, variables)
            return EvaluationResult(
                baseValue,
                unit = derived.symbols.first(),
                explicitUnitless = false,
                rationalValue = Rational.fromBigDecimalSmart(baseValue)
            )
        }
    }

    private data class BuiltinFn(
        val arity: Int,
        val inputSpec: UnitInputSpec,
        val unitPolicy: UnitPolicy,
        val body: (List<BigDecimal>) -> BigDecimal
    )

    private val functions: Map<String, BuiltinFn> = mapOf(
        // Unit-stripping placeholder helpers
        "value" to dropUnitFn(1) { it[0] },
        "dropUnit" to dropUnitFn(1) { it[0] },
        "raw" to dropUnitFn(1) { it[0] },

        // Display overrides
        "rational" to builtin(1, AnyNumericInput, RationalDisplayPolicy) { it[0] },
        "fraction" to builtin(1, AnyNumericInput, RationalDisplayPolicy) { it[0] },
        "float" to builtin(1, AnyNumericInput, FloatDisplayPolicy) { it[0] },

        // Trigonometric (Fallback to Double)
        "sin"    to angleFn(1) { BigDecimal(sin(it[0].toDouble()), mc) },
        "cos"    to angleFn(1) { BigDecimal(cos(it[0].toDouble()), mc) },
        "tan"    to angleFn(1) { BigDecimal(tan(it[0].toDouble()), mc) },
        "asin"   to angleFn(1) { BigDecimal(asin(it[0].toDouble()), mc) },
        "acos"   to angleFn(1) { BigDecimal(acos(it[0].toDouble()), mc) },
        "atan"   to angleFn(1) { BigDecimal(atan(it[0].toDouble()), mc) },
        "sinh"   to angleFn(1) { BigDecimal(sinh(it[0].toDouble()), mc) },
        "cosh"   to angleFn(1) { BigDecimal(cosh(it[0].toDouble()), mc) },
        "tanh"   to angleFn(1) { BigDecimal(tanh(it[0].toDouble()), mc) },

        // Logarithmic
        "log"    to unitlessFn(1) { BigDecimal(ln(it[0].toDouble()), mc) },
        "log10"  to unitlessFn(1) { BigDecimal(log10(it[0].toDouble()), mc) },
        "log2"   to unitlessFn(1) { BigDecimal(log(it[0].toDouble(), 2.0), mc) },
        "log1p"  to unitlessFn(1) { BigDecimal(ln(1.0 + it[0].toDouble()), mc) },

        // Power / roots
        "sqrt"   to powerFn(2) { BigDecimal(sqrt(it[0].toDouble()), mc) },
        "cbrt"   to powerFn(3) { BigDecimal(cbrt(it[0].toDouble()), mc) },
        "pow"    to builtin(2, RequireUnitlessInput, UnitlessNumericResultPolicy) {
            MathEngine.calculatePower(it[0], it[1], mc)
        },
        "exp"    to unitlessFn(1) { BigDecimal(exp(it[0].toDouble()), mc) },
        "expm1"  to unitlessFn(1) { BigDecimal(expm1(it[0].toDouble()), mc) },
        "factorial" to unitlessFn(1) { factorial(it[0]) },
        "fact"      to unitlessFn(1) { factorial(it[0]) },

        // Rounding / sign
        "abs"    to preserveUnitFn(1) { it[0].abs() },
        "floor"  to preserveUnitFn(1) { BigDecimal(floor(it[0].toDouble()), mc) },
        "ceil"   to preserveUnitFn(1) { BigDecimal(ceil(it[0].toDouble()), mc) },
        "signum" to preserveUnitFn(1) { BigDecimal(sign(it[0].toDouble()), mc) },
    )

    /** Built-in constants accessible as bare identifiers. */
    private const val PI_STR = "3.1415926535897932384626433832795028841971"
    private const val E_STR = "2.7182818284590452353602874713526624977572"
    private val constants: Map<String, BigDecimal> = mapOf(
        "PI" to BigDecimal(PI_STR),
        "pi" to BigDecimal(PI_STR),
        "π"  to BigDecimal(PI_STR),
        "e"  to BigDecimal(E_STR),
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

    fun execute(name: String, args: List<EvaluationResult>, variables: Map<String, EvaluationResult>): EvaluationResult {
        val fn = functions[name] ?: throw UnknownFunctionException(name)
        if (fn.arity != args.size) {
            throw ArityMismatchException(name, fn.arity, args.size)
        }
        if (!args.all { fn.inputSpec.accepts(it) }) {
            val received = args.firstOrNull()?.unit?.let { unit ->
                UnitConverter.findUnit(unit)?.name ?: unit
            } ?: "unitless"
            throw EvalException("`$name()` does not accept `$received` type, pass ${fn.inputSpec.description()}.")
        }
        val normalizedArgs = normalizeInputArgs(name, fn.inputSpec, args, variables)
        return fn.unitPolicy.evaluate(normalizedArgs, fn.body, variables)
    }

    /** Returns true if [name] is a registered function (any arity). */
    fun isFunction(name: String): Boolean = name in functions

    /** Return the expected arity of a built-in function, or null if it's not a function. */
    fun getArity(name: String): Int? = functions[name]?.arity

    fun requiresUnitlessInputs(name: String): Boolean = functions[name]?.inputSpec is RequireUnitlessInput

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

    private fun builtin(arity: Int, inputSpec: UnitInputSpec, policy: UnitPolicy, body: (List<BigDecimal>) -> BigDecimal) =
        BuiltinFn(arity, inputSpec, policy, body)

    private fun unitlessFn(arity: Int, body: (List<BigDecimal>) -> BigDecimal) =
        builtin(arity, RequireUnitlessInput, UnitlessNumericResultPolicy, body)

    private fun angleFn(arity: Int, body: (List<BigDecimal>) -> BigDecimal) =
        builtin(arity, OneOf(listOf(RequireUnitlessInput, RequireAngleInput)), UnitlessNumericResultPolicy, body)

    private fun preserveUnitFn(arity: Int, body: (List<BigDecimal>) -> BigDecimal) =
        builtin(arity, AnyNumericInput, PreserveInputUnitResultPolicy, body)

    private fun dropUnitFn(arity: Int, body: (List<BigDecimal>) -> BigDecimal) =
        builtin(arity, AnyNumericInput, DropUnitResultPolicy, body)

    private fun powerFn(exponent: Int, body: (List<BigDecimal>) -> BigDecimal) =
        builtin(1, AnyNumericInput, TransformUnitByPower(exponent), body)

    private fun normalizeInputArgs(
        name: String,
        spec: UnitInputSpec,
        args: List<EvaluationResult>,
        variables: Map<String, EvaluationResult>
    ): List<EvaluationResult> {
        return args.map { arg ->
            val normalized = spec.normalize(arg, variables)
            arg.copy(value = normalized)
        }
    }
}
