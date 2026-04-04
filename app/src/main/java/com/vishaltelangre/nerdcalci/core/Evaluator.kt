package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaMathContext
import java.math.RoundingMode
import kotlin.math.pow
import com.vishaltelangre.nerdcalci.core.Unit as NerdUnit

const val ERR_FRACTIONAL_NUMERAL_SYSTEM = "Fractional value cannot be converted to numeral system"

data class EvaluationResult(
    val value: BigDecimal?,
    val unit: String? = null,
    val explicitUnitless: Boolean = false,
    val rationalValue: Rational? = null,
    val explicitRational: Boolean = false,
    val forceFloat: Boolean = false
)

/**
 * Recursive tree-walk evaluator that computes a [BigDecimal] result from an [Expr] AST.
 *
 * Variables are resolved from the provided [variables] map.
 * User-defined functions are resolved from [localFunctions].
 * If a function is not local, it falls back to [Builtins].
 * [callStack] is used to detect and prevent infinite recursion.
 * Typed exceptions provide specific error messages.
 */
class Evaluator(
    private val variables: Map<String, EvaluationResult>,
    private val injectionErrors: Map<String, Exception> = emptyMap(),
    private val localFunctions: Map<String, LocalFunction> = emptyMap(),
    private val callStack: Set<String> = emptySet(),
    private val fileVariables: Map<String, String> = emptyMap(),
    private val fileContextLoader: FileContextLoader? = null,
    private val loadingStack: Set<String> = emptySet(),
    private val rationalMode: Boolean = false
) {
    private val mc = JavaMathContext.DECIMAL128

    suspend fun evaluate(expr: Expr): EvaluationResult = when (expr) {
        is Expr.NumberLiteral  -> EvaluationResult(expr.value, rationalValue = Rational.toRational(expr.value))
        is Expr.PercentLiteral -> {
            val res = expr.value.divide(BigDecimal("100"), mc)
            EvaluationResult(res, rationalValue = Rational.toRational(res))
        }
        is Expr.PercentOf      -> {
            val baseEval = evaluate(expr.base)
            val base = baseEval.value ?: throw EvalException("Cannot apply percentage to a non-numeric value")
            val baseUnit = baseEval.unit?.let { UnitConverter.findUnit(it) }
            val resultUnit = if (baseUnit != null && baseUnit.category != UnitCategory.SCALAR) baseEval.unit else null
            val resultValue = base.multiply(expr.percent).divide(BigDecimal("100"), mc)

            val baseRational = baseEval.rationalValue ?: Rational.toRational(base)
            val percentRational = Rational.toRational(expr.percent.divide(BigDecimal("100"), mc))
            EvaluationResult(resultValue, resultUnit, rationalValue = baseRational * percentRational)
        }
        is Expr.PercentOff     -> {
            val baseEval = evaluate(expr.base)
            val base = baseEval.value ?: throw EvalException("Cannot apply percentage to a non-numeric value")
            val baseUnit = baseEval.unit?.let { UnitConverter.findUnit(it) }
            val resultUnit = if (baseUnit != null && baseUnit.category != UnitCategory.SCALAR) baseEval.unit else null
            val factor = BigDecimal.ONE.subtract(expr.percent.divide(BigDecimal("100"), mc))
            val resultValue = base.multiply(factor)

            val baseRational = baseEval.rationalValue ?: Rational.toRational(base)
            val factorRational = Rational.ONE - Rational.toRational(expr.percent.divide(BigDecimal("100"), mc))
            EvaluationResult(resultValue, resultUnit, rationalValue = baseRational * factorRational)
        }
        is Expr.UnaryMinus     -> {
            val eval = evaluate(expr.operand)
            val operand = eval.value ?: throw EvalException("Cannot negate a non-numeric value")
            val operandRational = eval.rationalValue ?: Rational.toRational(operand)
            eval.copy(value = operand.negate(), rationalValue = operandRational.negate())
        }
        is Expr.Variable       -> resolveVariable(expr.name)
        is Expr.FunctionCall   -> evaluateFunction(expr.name, expr.args)
        is Expr.BinaryOp       -> evaluateBinaryOp(expr)
        is Expr.StringLiteral  -> throw EvalException("Quotes are only allowed when specifying file names in `file(\"...\")`")
        is Expr.MemberAccess   -> resolveMemberAccess(expr)
        is Expr.MemberFunctionCall -> resolveMemberFunctionCall(expr)
        is Expr.Quantity -> {
            val eval = evaluate(expr.value)
            val rawValue = eval.value ?: BigDecimal.ZERO
            val unit = UnitConverter.findUnit(expr.unit)
                ?: throw EvalException("Unknown unit `${expr.unit}`")
            if (unit.category == UnitCategory.NUMERAL_SYSTEM && rawValue.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                throw EvalException(ERR_FRACTIONAL_NUMERAL_SYSTEM)
            }
            // Note: UnitConverter currently works with BigDecimal.
            // We'll keep using BigDecimal for result.value but update rationalValue.
            val resultValue = UnitConverter.toBase(rawValue, unit, variables)
            val resultRationalValue = Rational.toRational(resultValue)
            EvaluationResult(resultValue, unit.symbols.first(), rationalValue = resultRationalValue)
        }
        is Expr.UnitConversion -> {
            val toUnit = UnitConverter.findUnit(expr.toUnit)
                ?: throw EvalException("Unknown unit `${expr.toUnit}`")

            val evaluatedExpr = evaluate(expr.expr)
            val baseValue = evaluatedExpr.value ?: BigDecimal.ZERO
            val baseRational = evaluatedExpr.rationalValue ?: Rational.toRational(baseValue)
            val fromUnit = evaluatedExpr.unit?.let { UnitConverter.findUnit(it) }

            if (fromUnit != null) {
                val isCompatible = fromUnit.category == toUnit.category ||
                        (fromUnit.category == UnitCategory.SCALAR && toUnit.category == UnitCategory.NUMERAL_SYSTEM) ||
                        (fromUnit.category == UnitCategory.NUMERAL_SYSTEM && toUnit.category == UnitCategory.SCALAR)
                if (!isCompatible) {
                    throw EvalException("Conversion of `${fromUnit.name}` to `${toUnit.name}` is not supported")
                }
            } else {
                if (toUnit.category != UnitCategory.SCALAR && toUnit.category != UnitCategory.NUMERAL_SYSTEM) {
                    throw EvalException("Conversion of unitless number to `${toUnit.name}` is not supported")
                }
            }

            if (toUnit.category == UnitCategory.NUMERAL_SYSTEM && baseValue.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                throw EvalException(ERR_FRACTIONAL_NUMERAL_SYSTEM)
            }

            EvaluationResult(baseValue, toUnit.symbols.first(), rationalValue = baseRational)
        }
    }


    private fun resolveVariable(name: String): EvaluationResult {
        // Check user variables first, then injection errors, then built-in constants
        variables[name]?.let { return it }
        injectionErrors[name]?.let { throw it }
        Builtins.constantValue(name)?.let { return EvaluationResult(it, rationalValue = Rational.toRational(it)) }
        throw UndefinedVariableException(name)
    }

    private suspend fun evaluateFunction(name: String, argExprs: List<Expr>): EvaluationResult {
        // Check if it's a user-defined local function
        val localFunc = localFunctions[name]
        if (localFunc != null) {
            if (argExprs.size != localFunc.params.size) {
                throw ArityMismatchException(name, localFunc.params.size, argExprs.size)
            }
            val args = argExprs.map { evaluate(it) }
            if (callStack.contains(name)) {
                throw EvalException("Function `$name()` calls itself too many times which is not allowed")
            }

            // Create a strictly isolated scope
            val localVars = mutableMapOf<String, EvaluationResult>()
            for (i in args.indices) {
                localVars[localFunc.params[i]] = args[i]
            }

            // A new Evaluator handles the inner scope execution
            val innerEvaluator = Evaluator(
                variables = localVars,
                localFunctions = localFunctions,
                callStack = callStack + name,
                fileVariables = fileVariables,
                fileContextLoader = fileContextLoader,
                loadingStack = loadingStack,
                rationalMode = rationalMode
            )
            val localContext = MathContext(variables = localVars)
            var lastResult = EvaluationResult(BigDecimal.ZERO)
            for (stmt in localFunc.body) {
                val res = innerEvaluator.evaluateStatement(stmt, localContext)
                if (res.value != null) {
                    lastResult = res
                }
            }
            return lastResult
        }

        if (name == "file") {
            throw EvalException("The `file()` function either needs to be assigned like `f = file(\"FileName\")` or used in dot notation like `file(\"FileName\").variable`")
        }

        if (name == "convert") {
            if (argExprs.size != 3) throw ArityMismatchException("convert", 3, argExprs.size)
            val eval = evaluate(argExprs[0])
            val value = eval.value ?: BigDecimal.ZERO
            val from = (argExprs[1] as? Expr.StringLiteral)?.value
                ?: throw EvalException("`convert()` second argument must be a unit string, e.g., \"km\"")
            val to = (argExprs[2] as? Expr.StringLiteral)?.value
                ?: throw EvalException("`convert()` third argument must be a unit string, e.g., \"m\"")

            val fromUnit = UnitConverter.findUnit(from) ?: throw EvalException("Unknown unit `$from`")
            val toUnit = UnitConverter.findUnit(to) ?: throw EvalException("Unknown unit `$to`")

            if (fromUnit.category != toUnit.category) {
                    throw EvalException("Conversion of `${fromUnit.name}` to `${toUnit.name}` is not supported")
            }

            val resultValue = UnitConverter.toBase(value, fromUnit, variables)
            val resultRationalValue = Rational.toRational(resultValue)
            return EvaluationResult(resultValue, toUnit.symbols.first(), rationalValue = resultRationalValue)
        }

        // Fallback to built-ins
        val builtinArity = Builtins.getArity(name)
        if (builtinArity != null && argExprs.size != builtinArity) {
            throw ArityMismatchException(name, builtinArity, argExprs.size)
        }

        val evaluatedArgs = argExprs.map { evaluate(it) }
        return Builtins.execute(name, evaluatedArgs, variables)
    }

    /**
     * Executes a single statement inside a scope represented by `context`,
     * mutating `context` (variables or localFunctions) as necessary.
     * Returns the numeric result of the statement, or null if it produces no result.
     */
    suspend fun evaluateStatement(statement: Statement, context: MathContext): EvaluationResult {
        return when (statement) {
            is Statement.Empty -> EvaluationResult(null)

            is Statement.FunctionDefinition -> {
                validateVariableOrFunctionName(statement.name)
                context.localFunctions[statement.name] = LocalFunction(statement.name, statement.params, statement.body)
                EvaluationResult(null)
            }

            is Statement.ExprStatement -> {
                evaluate(statement.expr)
            }

            is Statement.Assignment -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)

                // Intercept file("...") FunctionCall
                if (statement.expr is Expr.FunctionCall && statement.expr.name == "file") {
                    val args = statement.expr.args
                    if (args.size != 1) {
                        throw EvalException("The `file()` function requires exactly one file name in quotes, e.g., `file(\"FileName\")`")
                    }
                    val arg = args[0]
                    if (arg !is Expr.StringLiteral) {
                        throw EvalException("The `file()` function requires exactly one file name in quotes, e.g., `file(\"FileName\")`")
                    }
                    val fileName = arg.value
                    context.variables.remove(name)
                    context.fileVariables[name] = fileName
                    return EvaluationResult(null)
                }

                // Intercept File Variable Copy
                if (statement.expr is Expr.Variable && context.fileVariables.containsKey(statement.expr.name)) {
                    val sourceFile = context.fileVariables[statement.expr.name]!!
                    context.variables.remove(name)
                    context.fileVariables[name] = sourceFile
                    return EvaluationResult(null)
                }

                val result = evaluate(statement.expr)
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }

            is Statement.CompoundAssignment -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)

                val standardOp = when (statement.op) {
                    TokenKind.PLUS_EQUALS    -> TokenKind.PLUS
                    TokenKind.MINUS_EQUALS   -> TokenKind.MINUS
                    TokenKind.STAR_EQUALS    -> TokenKind.STAR
                    TokenKind.SLASH_EQUALS   -> TokenKind.SLASH
                    TokenKind.PERCENT_EQUALS -> TokenKind.PERCENT
                    else -> throw EvalException("Unknown compound operator: `${statement.op}`")
                }
                val result = evaluateBinaryOp(Expr.BinaryOp(target, standardOp, statement.expr))
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }

            is Statement.Increment -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)

                val currentValue = evaluate(target)
                val currentUnit = currentValue.unit?.let { UnitConverter.findUnit(it) }
                if (currentUnit?.category == UnitCategory.TEMPERATURE) {
                    val displayValue = UnitConverter.fromBase(currentValue.value ?: BigDecimal.ZERO, currentUnit, variables)
                    val updatedDisplayValue = displayValue.add(BigDecimal.ONE)
                    val updatedBaseValue = UnitConverter.toBase(updatedDisplayValue, currentUnit, variables)
                    val result = EvaluationResult(updatedBaseValue, currentValue.unit, rationalValue = Rational.toRational(updatedBaseValue))
                    context.fileVariables.remove(name)
                    context.variables[name] = result
                    return result
                }

                val increment = if (currentValue.unit != null) {
                    Expr.Quantity(Expr.NumberLiteral(BigDecimal.ONE), currentValue.unit)
                } else {
                    Expr.NumberLiteral(BigDecimal.ONE)
                }

                val result = evaluateBinaryOp(Expr.BinaryOp(target, TokenKind.PLUS, increment))
                // Return the updated value for calculator UX; prefix semantics are not supported.
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }

            is Statement.Decrement -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)

                val currentValue = evaluate(target)
                val currentUnit = currentValue.unit?.let { UnitConverter.findUnit(it) }
                if (currentUnit?.category == UnitCategory.TEMPERATURE) {
                    val displayValue = UnitConverter.fromBase(currentValue.value ?: BigDecimal.ZERO, currentUnit, variables)
                    val updatedDisplayValue = displayValue.subtract(BigDecimal.ONE)
                    val updatedBaseValue = UnitConverter.toBase(updatedDisplayValue, currentUnit, variables)
                    val result = EvaluationResult(updatedBaseValue, currentValue.unit, rationalValue = Rational.toRational(updatedBaseValue))
                    context.fileVariables.remove(name)
                    context.variables[name] = result
                    return result
                }

                val decrement = if (currentValue.unit != null) {
                    Expr.Quantity(Expr.NumberLiteral(BigDecimal.ONE), currentValue.unit)
                } else {
                    Expr.NumberLiteral(BigDecimal.ONE)
                }

                val result = evaluateBinaryOp(Expr.BinaryOp(target, TokenKind.MINUS, decrement))
                // Return the updated value for calculator UX; prefix semantics are not supported.
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }
        }
    }

    private fun validateVariableOrFunctionName(name: String) {
        if (!name.matches(Regex(Constants.VAR_FUNC_NAME_PATTERN))) {
            throw EvalException("Invalid variable or function name `$name`")
        }
        if (name in UnitConverter.RESERVED_UNIT_SYMBOLS) {
            throw EvalException("`$name` is a unit symbol and cannot be used as a variable name")
        }
    }

    private fun scalarValue(
        result: EvaluationResult,
        unit: NerdUnit?,
        forceDisplayValue: Boolean
    ): BigDecimal {
        if (unit == null) return result.value ?: BigDecimal.ZERO
        return when {
            unit.category == UnitCategory.SCALAR -> result.value ?: BigDecimal.ZERO
            forceDisplayValue || result.explicitUnitless -> UnitConverter.fromBase(result.value ?: BigDecimal.ZERO, unit, variables)
            else -> result.value ?: BigDecimal.ZERO
        }
    }

    private suspend fun evaluateBinaryOp(expr: Expr.BinaryOp): EvaluationResult {
        val leftEval = evaluate(expr.left)
        val rightEval = evaluate(expr.right)

        // Percentage addition/subtraction
        if (expr.right is Expr.PercentLiteral) {
            val pct = expr.right.value
            val leftVal = leftEval.value ?: BigDecimal.ZERO
            val leftRational = leftEval.rationalValue ?: Rational.toRational(leftVal)
            val pctRational = Rational.toRational(pct.divide(BigDecimal("100"), mc))

            val (resultVal, resultRational) = when (expr.op) {
                TokenKind.PLUS  -> {
                    val factor = BigDecimal.ONE.add(pct.divide(BigDecimal("100"), mc))
                    leftVal.multiply(factor) to leftRational * (Rational.ONE + pctRational)
                }
                TokenKind.MINUS -> {
                    val factor = BigDecimal.ONE.subtract(pct.divide(BigDecimal("100"), mc))
                    leftVal.multiply(factor) to leftRational * (Rational.ONE - pctRational)
                }
                else -> {
                    val rightVal = rightEval.value ?: BigDecimal.ZERO
                    val rightRational = rightEval.rationalValue ?: Rational.toRational(rightVal)
                    applyOp(leftVal, expr.op, rightVal) to applyRationalOp(leftRational, expr.op, rightRational)
                }
            }
            return EvaluationResult(resultVal, leftEval.unit, rationalValue = resultRational)
        }

        val leftVal = leftEval.value ?: BigDecimal.ZERO
        val rightVal = rightEval.value ?: BigDecimal.ZERO

        val leftRational = leftEval.rationalValue ?: Rational.toRational(leftVal)
        val rightRational = rightEval.rationalValue ?: Rational.toRational(rightVal)

        val leftUnit = leftEval.unit?.let { UnitConverter.findUnit(it) }
        val rightUnit = rightEval.unit?.let { UnitConverter.findUnit(it) }
        val forceDisplayValue = leftEval.explicitUnitless || rightEval.explicitUnitless
        val leftUsesDisplayValue = forceDisplayValue ||
            ((expr.op == TokenKind.STAR || (expr.op == TokenKind.SLASH && leftUnit?.category == UnitCategory.TEMPERATURE)) &&
                leftUnit?.category == UnitCategory.TEMPERATURE &&
                (rightUnit == null || rightUnit.category == UnitCategory.SCALAR))
        val rightUsesDisplayValue = forceDisplayValue ||
            (expr.op == TokenKind.STAR && rightUnit?.category == UnitCategory.TEMPERATURE &&
                (leftUnit == null || leftUnit.category == UnitCategory.SCALAR))
        val leftScalar = scalarValue(leftEval, leftUnit, leftUsesDisplayValue)
        val rightScalar = scalarValue(rightEval, rightUnit, rightUsesDisplayValue)

        if ((expr.op == TokenKind.STAR || expr.op == TokenKind.SLASH) &&
            ((leftUnit?.category == UnitCategory.TEMPERATURE && (rightUnit == null || rightUnit.category == UnitCategory.SCALAR)) ||
                (rightUnit?.category == UnitCategory.TEMPERATURE && (leftUnit == null || leftUnit.category == UnitCategory.SCALAR)))) {
            val tempUnit = leftUnit?.takeIf { it.category == UnitCategory.TEMPERATURE }
                ?: rightUnit?.takeIf { it.category == UnitCategory.TEMPERATURE }
                ?: throw EvalException("Temperature unit is unavailable")
            val resultDisplay = applyOp(leftScalar, expr.op, rightScalar)
            val resultBase = UnitConverter.toBase(resultDisplay, tempUnit, variables)
            return EvaluationResult(
                resultBase,
                tempUnit.symbols.first(),
                rationalValue = Rational.toRational(resultBase)
            )
        }

        if (expr.op == TokenKind.PERCENT && (leftUnit != null || rightUnit != null)) {
            val leftDesc = getDimensionDescription(leftUnit)
            val rightDesc = getDimensionDescription(rightUnit)
            throw EvalException("Modulo of $leftDesc and $rightDesc is not supported")
        }

        // General addition/subtraction
        if (expr.op == TokenKind.PLUS || expr.op == TokenKind.MINUS) {
            val leftIsPhysical = isPhysicalUnit(leftUnit)
            val rightIsPhysical = isPhysicalUnit(rightUnit)

            // Strict unit check: if one is physical, the other must be physical of the same category
            if (leftIsPhysical || rightIsPhysical) {
                if (!leftIsPhysical || !rightIsPhysical || leftUnit!!.category != rightUnit!!.category) {
                    val leftDesc = getDimensionDescription(leftUnit)
                    val rightDesc = getDimensionDescription(rightUnit)
                    val opName = if (expr.op == TokenKind.PLUS) "Addition" else "Subtraction"

                    throw EvalException("$opName of $leftDesc and $rightDesc is not supported")
                }
            }

            if (leftUnit != null && rightUnit != null) {
                if (leftUnit.category == UnitCategory.SCALAR || rightUnit.category == UnitCategory.SCALAR ||
                    leftUnit.category == UnitCategory.NUMERAL_SYSTEM || rightUnit.category == UnitCategory.NUMERAL_SYSTEM) {
                    val resultUnit = when {
                        leftUnit.category == UnitCategory.NUMERAL_SYSTEM -> leftEval.unit
                        rightUnit.category == UnitCategory.NUMERAL_SYSTEM -> rightEval.unit
                        else -> null
                    }
                    return EvaluationResult(
                        applyOp(leftScalar, expr.op, rightScalar),
                        resultUnit,
                        rationalValue = applyRationalOp(leftRational, expr.op, rightRational)
                    )
                }

                if (leftUnit.category == UnitCategory.TEMPERATURE) {
                    val canonicalUnit = UnitConverter.findUnit("degC")
                        ?: throw EvalException("Temperature conversion is unavailable")
                    val leftCanonical = UnitConverter.fromBase(leftVal, canonicalUnit, variables)
                    val rightCanonical = UnitConverter.fromBase(rightVal, canonicalUnit, variables)
                    val resultCanonical = applyOp(leftCanonical, expr.op, rightCanonical)
                    // Rational for temperature is tricky due to offsets, thus we fall back to BigDecimal scaling
                    return EvaluationResult(
                        UnitConverter.toBase(resultCanonical, canonicalUnit, variables),
                        canonicalUnit.symbols.first(),
                        rationalValue = Rational.toRational(UnitConverter.toBase(resultCanonical, canonicalUnit, variables))
                    )
                }

                val pickedUnit = if (leftUnit.factor.compareTo(rightUnit.factor) < 0) leftUnit else rightUnit
                return EvaluationResult(
                    applyOp(leftVal, expr.op, rightVal),
                    pickedUnit.symbols.first(),
                    rationalValue = applyRationalOp(leftRational, expr.op, rightRational)
                )
            } else if (leftUnit != null) {
                // At this point it must be non-physical (SCALAR or NUMERAL)
                return EvaluationResult(
                    applyOp(leftScalar, expr.op, rightScalar),
                    leftEval.unit,
                    rationalValue = applyRationalOp(leftRational, expr.op, rightRational)
                )
            } else if (rightUnit != null) {
                // At this point it must be non-physical
                return EvaluationResult(
                    applyOp(leftScalar, expr.op, rightScalar),
                    rightEval.unit,
                    rationalValue = applyRationalOp(leftRational, expr.op, rightRational)
                )
            }
        }

        val resultVal = applyOp(leftScalar, expr.op, rightScalar)
        val resultRational = applyRationalOp(leftRational, expr.op, rightRational)

        val resultScale = when (expr.op) {
            TokenKind.STAR, TokenKind.SLASH -> UnitConverter.deriveUnitScale(leftUnit, rightUnit, expr.op)
            TokenKind.CARET -> if (leftUnit != null) {
                val exponent = rightVal.toIntOrNullExact()
                if (exponent == 3 && leftUnit?.category == UnitCategory.LENGTH) {
                    BigDecimal("1000.0")
                } else {
                    BigDecimal.ONE
                }
            } else {
                BigDecimal.ONE
            }
            else -> BigDecimal.ONE
        }

        // Handle scaling multiplication/division inheritance
        val resultUnit = if (expr.op == TokenKind.STAR || expr.op == TokenKind.SLASH) {
            if (leftEval.explicitUnitless || rightEval.explicitUnitless ||
                leftUnit?.category == UnitCategory.SCALAR || rightUnit?.category == UnitCategory.SCALAR) {
                null
            } else {
                val derivedUnit = UnitConverter.deriveUnit(leftUnit, rightUnit, expr.op)
                when {
                    derivedUnit == "unitless" -> null  // Units canceled out
                    derivedUnit == null && leftUnit != null && rightUnit != null -> {
                        val operationName = when (expr.op) {
                            TokenKind.STAR -> "Multiplication"
                            TokenKind.SLASH -> "Division"
                            else -> expr.op.display
                        }
                        throw EvalException(
                            "$operationName of ${leftUnit.name} and ${rightUnit.name} is not supported"
                        )
                    }
                    derivedUnit != null -> derivedUnit
                    leftUnit != null && rightUnit == null -> leftEval.unit
                    leftUnit == null && rightUnit != null && expr.op == TokenKind.STAR -> rightEval.unit
                    else -> null
                }
            }
        } else if (
            expr.op == TokenKind.CARET &&
            leftUnit != null &&
            leftUnit.category != UnitCategory.SCALAR &&
            leftUnit.category != UnitCategory.NUMERAL_SYSTEM
        ) {
            if (rightEval.unit != null) {
                throw EvalException("Exponentiation requires a unitless exponent")
            }
            val exponent = rightVal.toIntOrNullExact()
            if (exponent == null) {
                throw EvalException("Exponentiation requires an integer exponent")
            }
            val derivedUnit = UnitConverter.deriveForPower(leftUnit, exponent)
            when {
                derivedUnit == "unitless" -> null
                derivedUnit == null -> throw EvalException(
                    "Exponentiation of ${leftUnit.name} by ${rightVal.stripTrailingZeros().toPlainString()} is not supported"
                )
                else -> derivedUnit
            }
        } else {
            when {
                leftEval.explicitUnitless || rightEval.explicitUnitless -> null
                leftUnit?.category == UnitCategory.SCALAR || rightUnit?.category == UnitCategory.SCALAR -> null
                else -> leftEval.unit ?: rightEval.unit
            }
        }

        return EvaluationResult(resultVal * resultScale, resultUnit, rationalValue = resultRational * Rational.toRational(resultScale))
    }

    private fun applyOp(left: BigDecimal, op: TokenKind, right: BigDecimal): BigDecimal = when (op) {
        TokenKind.PLUS    -> left.add(right)
        TokenKind.MINUS   -> left.subtract(right)
        TokenKind.STAR    -> left.multiply(right, mc)
        TokenKind.SLASH   -> {
            if (right.compareTo(BigDecimal.ZERO) == 0) throw DivisionByZeroException()
            if (rationalMode) {
                // In rational mode, we use the rational value for higher precision when possible.
                // applyRationalOp will be called separately to get the exact value.
                // Still need a BigDecimal for result.value.
                (Rational.toRational(left) / Rational.toRational(right)).toBigDecimal(mc)
            } else {
                left.divide(right, mc)
            }
        }
        TokenKind.PERCENT -> {
            if (right.compareTo(BigDecimal.ZERO) == 0) throw DivisionByZeroException()
            left.remainder(right, mc)
        }
        TokenKind.CARET   -> {
            try {
                left.pow(right.intValueExact(), mc)
            } catch (e: Exception) {
                BigDecimal(left.toDouble().pow(right.toDouble()), mc)
            }
        }
        else -> throw EvalException("Unknown operator: `$op`")
    }

    private fun applyRationalOp(left: Rational, op: TokenKind, right: Rational): Rational = when (op) {
        TokenKind.PLUS    -> left + right
        TokenKind.MINUS   -> left - right
        TokenKind.STAR    -> left * right
        TokenKind.SLASH   -> if (right.num == BigInteger.ZERO) throw DivisionByZeroException() else left / right
        TokenKind.PERCENT -> {
            // Remainder for rationals: a - b * floor(a/b)
            val div = (left / right).toBigDecimal(mc).setScale(0, RoundingMode.FLOOR)
            left - (right * Rational.toRational(div))
        }
        TokenKind.CARET   -> {
            try {
                val exponent = right.toBigDecimal(mc).toBigIntegerExact().toInt()
                if (exponent >= 0) {
                    Rational(left.num.pow(exponent), left.den.pow(exponent))
                } else {
                    Rational(left.den.pow(-exponent), left.num.pow(-exponent))
                }
            } catch (e: Exception) {
                // Fallback to double-based approximation for non-integer exponents
                println("Evaluator: falling back to double approximation for power: ${e.message}")
                val leftDouble = left.toBigDecimal(mc).toDouble()
                val rightDouble = right.toBigDecimal(mc).toDouble()
                val resultDouble = leftDouble.pow(rightDouble)
                Rational.toRational(BigDecimal(resultDouble, mc))
            }
        }
        else -> throw EvalException("Unknown operator: `$op`")
    }

    private fun BigDecimal.toIntOrNullExact(): Int? {
        return try {
            toBigIntegerExact().intValueExact()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolves a member access expression (e.g., `obj.name`).
     *
     * It extracts the file reference link, loads that file's evaluated context,
     * and fetches the variable value from it.
     */
    private suspend fun resolveMemberAccess(expr: Expr.MemberAccess): EvaluationResult {
        val obj = expr.obj
        val name = expr.name
        val fileName = getFileNameFromObj(obj, false)
        val remoteContext = loadRemoteContext(fileName)
        if (name in MathEngine.EXCLUDED_DOT_NOTATION_VARIABLES) {
            throw EvalException("Line number variables (like `$name`) are relative to the file being viewed and cannot be accessed from other files")
        }
        if (Builtins.isConstant(name)) {
            throw EvalException("`$name` is a global constant and should be accessed directly, not via dot notation")
        }
        return remoteContext.variables[name] ?: throw UndefinedVariableException(name)
    }

    /**
     * Loads the MathContext of another file, strictly guarding against circular references.
     *
     * [loadingStack] tracks the chain of files currently being loaded to detect infinite loops
     * (e.g., FileA refers to FileB, which refers back to FileA).
     */
    private suspend fun loadRemoteContext(fileName: String): MathContext {
        if (loadingStack.contains(fileName)) {
            throw CircularReferenceException(fileName, loadingStack)
        }
        val loader = fileContextLoader ?: throw EvalException("File loading is not supported in this context")
        return loader.loadContext(fileName, loadingStack + fileName) ?: throw EvalException("Failed to load file `$fileName`")
    }

    private suspend fun resolveMemberFunctionCall(expr: Expr.MemberFunctionCall): EvaluationResult {
        val obj = expr.obj
        val name = expr.name
        val argExprs = expr.args
        val fileName = getFileNameFromObj(obj, true)

        if (Builtins.isFunction(name)) {
            throw EvalException("`$name()` is a global function and should be called directly, not via dot notation")
        }
        val remoteContext = loadRemoteContext(fileName)

        val args = argExprs.map { evaluate(it) }
        val evaluatedArgs = args.map {
            val baseValue = it.value ?: BigDecimal.ZERO
            if (it.unit != null) {
                val unit = UnitConverter.findUnit(it.unit)
                if (unit != null) {
                    val visualValue = UnitConverter.fromBase(baseValue, unit, variables)
                    Expr.Quantity(Expr.NumberLiteral(visualValue), it.unit)
                } else {
                    Expr.NumberLiteral(baseValue)
                }
            } else {
                Expr.NumberLiteral(baseValue)
            }
        }

        val remoteEvaluator = Evaluator(
            variables = remoteContext.variables,
            localFunctions = remoteContext.localFunctions,
            fileVariables = remoteContext.fileVariables,
            fileContextLoader = fileContextLoader,
            loadingStack = loadingStack + fileName,
            rationalMode = rationalMode
        )
        return remoteEvaluator.evaluateFunction(name, evaluatedArgs)
    }

    /**
     * Extracts the file name from a dot notation expression.
     */
    private fun getFileNameFromObj(obj: Expr, isFunctionCall: Boolean): String {
        val operation = if (isFunctionCall) "call functions from" else "access items from"
        return if (obj is Expr.Variable) {
            fileVariables[obj.name] ?: throw EvalException("`${obj.name}` is not linked to any file. Use `file(\"...\")` to link first")
        } else if (obj is Expr.FunctionCall && obj.name == "file") {
            if (obj.args.size != 1 || obj.args[0] !is Expr.StringLiteral) {
                throw EvalException("`file()` expects exactly one file name in quotes, e.g., `file(\"FileName\")`")
            }
            (obj.args[0] as Expr.StringLiteral).value
        } else {
            throw EvalException("You can only $operation other files using dot notation")
        }
    }

    private fun isPhysicalUnit(unit: NerdUnit?): Boolean {
        return unit != null && unit.category != UnitCategory.SCALAR && unit.category != UnitCategory.NUMERAL_SYSTEM
    }

    private fun getDimensionName(unit: NerdUnit?): String {
        return unit?.category?.name?.lowercase() ?: "unitless"
    }

    private fun getDimensionDescription(unit: NerdUnit?): String {
        return unit?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "unitless number"
    }
}
