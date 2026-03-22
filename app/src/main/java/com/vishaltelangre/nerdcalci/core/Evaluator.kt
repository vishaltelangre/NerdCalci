package com.vishaltelangre.nerdcalci.core

import kotlin.math.pow

data class EvaluationResult(
    val value: Double?,
    val unit: String? = null
)

/**
 * Recursive tree-walk evaluator that computes a [Double] result from an [Expr] AST.
 *
 * Variables are resolved from the provided [variables] map.
 * User-defined functions are resolved from [localFunctions].
 * If a function is not local, it falls back to [Builtins].
 * [callStack] is used to detect and prevent infinite recursion.
 * Typed exceptions provide specific error messages.
 */
class Evaluator(
    private val variables: Map<String, EvaluationResult>,
    private val localFunctions: Map<String, LocalFunction> = emptyMap(),
    private val callStack: Set<String> = emptySet(),
    private val fileVariables: Map<String, String> = emptyMap(),
    private val fileContextLoader: FileContextLoader? = null,
    private val loadingStack: Set<String> = emptySet()
) {

    suspend fun evaluate(expr: Expr): EvaluationResult = when (expr) {
        is Expr.NumberLiteral  -> EvaluationResult(expr.value)
        is Expr.PercentLiteral -> EvaluationResult(expr.value / 100.0)
        is Expr.PercentOf      -> {
            val base = evaluate(expr.base).value ?: throw EvalException("Cannot apply percentage to a non-numeric value")
            EvaluationResult(base * expr.percent / 100.0)
        }
        is Expr.PercentOff     -> {
            val base = evaluate(expr.base).value ?: throw EvalException("Cannot apply percentage to a non-numeric value")
            EvaluationResult(base * (1.0 - expr.percent / 100.0))
        }
        is Expr.UnaryMinus     -> {
            val operand = evaluate(expr.operand).value ?: throw EvalException("Cannot negate a non-numeric value")
            EvaluationResult(-operand)
        }
        is Expr.Variable       -> resolveVariable(expr.name)
        is Expr.FunctionCall   -> evaluateFunction(expr.name, expr.args)
        is Expr.BinaryOp       -> evaluateBinaryOp(expr)
        is Expr.StringLiteral  -> throw EvalException("Quotes are only allowed when specifying file names in `file(\"...\")`")
        is Expr.MemberAccess   -> resolveMemberAccess(expr)
        is Expr.MemberFunctionCall -> resolveMemberFunctionCall(expr)
        is Expr.Quantity -> {
            val rawValue = evaluate(expr.value).value ?: 0.0
            val unit = UnitConverter.findUnit(expr.unit)
                ?: throw EvalException("Unknown unit `${expr.unit}`")
            EvaluationResult(UnitConverter.toBase(rawValue, unit, variables), unit.symbols.first())
        }
        is Expr.UnitConversion -> {
            val toUnit = UnitConverter.findUnit(expr.toUnit)
                ?: throw EvalException("Unknown unit `${expr.toUnit}`")

            val evaluatedExpr = evaluate(expr.expr)
            val baseValue = evaluatedExpr.value ?: 0.0
            val fromUnit = evaluatedExpr.unit?.let { UnitConverter.findUnit(it) }

            if (fromUnit != null) {
                val isCompatible = fromUnit.category == toUnit.category ||
                        (fromUnit.category == UnitCategory.SCALAR && toUnit.category == UnitCategory.NUMERAL_SYSTEM) ||
                        (fromUnit.category == UnitCategory.NUMERAL_SYSTEM && toUnit.category == UnitCategory.SCALAR)
                if (!isCompatible) {
                    throw EvalException("Cannot convert `${fromUnit.name}` to `${toUnit.name}`: dimension mismatch")
                }
            } else {
                if (toUnit.category != UnitCategory.SCALAR && toUnit.category != UnitCategory.NUMERAL_SYSTEM) {
                    throw EvalException("Cannot convert unitless number to `${toUnit.name}`")
                }
            }

            EvaluationResult(baseValue, toUnit.symbols.first())
        }
    }


    private fun resolveVariable(name: String): EvaluationResult {
        // Check user variables first, then built-in constants
        variables[name]?.let { return it }
        Builtins.constantValue(name)?.let { return EvaluationResult(it) }
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
                loadingStack = loadingStack
            )
            val localContext = MathContext(variables = localVars)
            var lastResult = EvaluationResult(0.0)
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
            val value = evaluate(argExprs[0]).value ?: 0.0
            val from = (argExprs[1] as? Expr.StringLiteral)?.value
                ?: throw EvalException("`convert()` second argument must be a unit string, e.g., \"km\"")
            val to = (argExprs[2] as? Expr.StringLiteral)?.value
                ?: throw EvalException("`convert()` third argument must be a unit string, e.g., \"m\"")

            val fromUnit = UnitConverter.findUnit(from) ?: throw EvalException("Unknown unit `$from`")
            val toUnit = UnitConverter.findUnit(to) ?: throw EvalException("Unknown unit `$to`")

            if (fromUnit.category != toUnit.category) {
                throw EvalException("Cannot convert `${fromUnit.name}` to `${toUnit.name}`: dimension mismatch")
            }

            return EvaluationResult(UnitConverter.toBase(value, fromUnit, variables), toUnit.symbols.first())
        }

        // Fallback to built-ins
        val builtinArity = Builtins.getArity(name)
        if (builtinArity != null && argExprs.size != builtinArity) {
            throw ArityMismatchException(name, builtinArity, argExprs.size)
        }

        val args = argExprs.map { evaluate(it).value ?: 0.0 }
        return EvaluationResult(Builtins.call(name, args))
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
                val res = evaluate(statement.expr)
                val unit = if (statement.expr is Expr.Quantity) statement.expr.unit else null
                EvaluationResult(res.value, res.unit ?: unit)
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
                val current = context.variables[name]
                    ?: throw UndefinedVariableException(name)
                val rhs = evaluate(statement.expr)

                val curVal = current.value ?: 0.0
                val rhsVal = rhs.value ?: 0.0

                val result = when (statement.op) {
                    TokenKind.PLUS_EQUALS    -> EvaluationResult(curVal + rhsVal, current.unit)
                    TokenKind.MINUS_EQUALS   -> EvaluationResult(curVal - rhsVal, current.unit)
                    TokenKind.STAR_EQUALS    -> EvaluationResult(curVal * rhsVal, current.unit)
                    TokenKind.SLASH_EQUALS   -> {
                        if (rhsVal == 0.0) throw DivisionByZeroException()
                        EvaluationResult(curVal / rhsVal, current.unit)
                    }
                    TokenKind.PERCENT_EQUALS -> {
                        if (rhsVal == 0.0) throw DivisionByZeroException()
                        EvaluationResult(curVal % rhsVal, current.unit)
                    }
                    else -> throw EvalException("Unknown compound operator: `${statement.op}`")
                }
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }

            is Statement.Increment -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)
                val current = context.variables[name]
                    ?: throw UndefinedVariableException(name)
                val result = EvaluationResult((current.value ?: 0.0) + 1.0, current.unit)
                context.fileVariables.remove(name)
                context.variables[name] = result
                result
            }

            is Statement.Decrement -> {
                val target = statement.target
                if (target !is Expr.Variable) throw EvalException("Invalid assignment target")
                val name = target.name
                validateVariableOrFunctionName(name)
                val current = context.variables[name]
                    ?: throw UndefinedVariableException(name)
                val result = EvaluationResult((current.value ?: 0.0) - 1.0, current.unit)
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

    private suspend fun evaluateBinaryOp(expr: Expr.BinaryOp): EvaluationResult {
        val leftEval = evaluate(expr.left)
        val rightEval = evaluate(expr.right)

        // Percentage addition/subtraction
        if (expr.right is Expr.PercentLiteral) {
            val pct = expr.right.value
            val leftVal = leftEval.value ?: 0.0
            return EvaluationResult(when (expr.op) {
                TokenKind.PLUS  -> leftVal * (1.0 + pct / 100.0)
                TokenKind.MINUS -> leftVal * (1.0 - pct / 100.0)
                else -> applyOp(leftVal, expr.op, rightEval.value ?: 0.0)
            }, leftEval.unit)
        }

        val leftVal = leftEval.value ?: 0.0
        val rightVal = rightEval.value ?: 0.0

        val leftUnit = leftEval.unit?.let { UnitConverter.findUnit(it) }
        val rightUnit = rightEval.unit?.let { UnitConverter.findUnit(it) }

        if (expr.op == TokenKind.PLUS || expr.op == TokenKind.MINUS) {
            if (leftUnit != null && rightUnit != null) {
                if (leftUnit.category != rightUnit.category) {
                    throw EvalException("Cannot calculate ${leftUnit.name} and ${rightUnit.name}: dimension mismatch")
                }

                val rightDelta = if (leftUnit.category == UnitCategory.TEMPERATURE) {
                    val rawRight = UnitConverter.fromBase(rightVal, rightUnit, variables)
                    val slope = UnitConverter.toBase(1.0, rightUnit, variables) - UnitConverter.toBase(0.0, rightUnit, variables)
                    rawRight * slope
                } else {
                    // Pick the smaller unit (smaller factor values translate to smaller absolute unit
                    // definitions)
                    rightVal
                }

                val pickedUnit = if (leftUnit.factor < rightUnit.factor) leftUnit else rightUnit
                return EvaluationResult(applyOp(leftVal, expr.op, rightDelta), pickedUnit.symbols.first())
            } else if (leftUnit != null && rightUnit == null) {
                val scaledRight = UnitConverter.toBase(rightVal, leftUnit, variables)
                return EvaluationResult(applyOp(leftVal, expr.op, scaledRight), leftUnit.symbols.first())
            } else if (leftUnit == null && rightUnit != null) {
                val scaledLeft = UnitConverter.toBase(leftVal, rightUnit, variables)
                return EvaluationResult(applyOp(scaledLeft, expr.op, rightVal), rightUnit.symbols.first())
            }
        }

        val resultVal = applyOp(leftVal, expr.op, rightVal)

        // Handle scaling multiplication/division inheritance
        val resultUnit = if (expr.op == TokenKind.STAR || expr.op == TokenKind.SLASH) {
            if (leftUnit != null && rightUnit == null) {
                leftEval.unit
            } else if (leftUnit == null && rightUnit != null && expr.op == TokenKind.STAR) {
                rightEval.unit
            } else {
                null
            }
        } else {
            leftEval.unit ?: rightEval.unit
        }

        return EvaluationResult(resultVal, resultUnit)
    }

    private fun applyOp(left: Double, op: TokenKind, right: Double): Double = when (op) {
        TokenKind.PLUS    -> left + right
        TokenKind.MINUS   -> left - right
        TokenKind.STAR    -> left * right
        TokenKind.SLASH   -> {
            if (right == 0.0) throw DivisionByZeroException()
            left / right
        }
        TokenKind.PERCENT -> {
            if (right == 0.0) throw DivisionByZeroException()
            left % right
        }
        TokenKind.CARET   -> left.pow(right)
        else -> throw EvalException("Unknown operator: `$op`")
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
        val evaluatedArgs = args.map { Expr.NumberLiteral(it.value ?: 0.0) }

        val remoteEvaluator = Evaluator(
            variables = remoteContext.variables,
            localFunctions = remoteContext.localFunctions,
            fileVariables = remoteContext.fileVariables,
            fileContextLoader = fileContextLoader,
            loadingStack = loadingStack + fileName
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
}
