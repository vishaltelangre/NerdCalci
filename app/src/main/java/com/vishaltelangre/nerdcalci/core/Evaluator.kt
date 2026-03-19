package com.vishaltelangre.nerdcalci.core

import kotlin.math.pow

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
    private val variables: Map<String, Double>,
    private val localFunctions: Map<String, LocalFunction> = emptyMap(),
    private val callStack: Set<String> = emptySet(),
    private val fileVariables: Map<String, String> = emptyMap(),
    private val fileContextLoader: FileContextLoader? = null,
    private val loadingStack: Set<String> = emptySet()
) {

    suspend fun evaluate(expr: Expr): Double = when (expr) {
        is Expr.NumberLiteral  -> expr.value
        is Expr.PercentLiteral -> expr.value / 100.0
        is Expr.PercentOf      -> evaluate(expr.base) * expr.percent / 100.0
        is Expr.PercentOff     -> evaluate(expr.base) * (1.0 - expr.percent / 100.0)
        is Expr.UnaryMinus     -> -evaluate(expr.operand)
        is Expr.Variable       -> resolveVariable(expr.name)
        is Expr.FunctionCall   -> evaluateFunction(expr.name, expr.args)
        is Expr.BinaryOp       -> evaluateBinaryOp(expr)
        is Expr.StringLiteral  -> throw EvalException("Quotes are only allowed when specifying file names in `file(\"...\")`")
        is Expr.MemberAccess   -> resolveMemberAccess(expr)
        is Expr.MemberFunctionCall -> resolveMemberFunctionCall(expr)
    }

    private fun resolveVariable(name: String): Double {
        // Check user variables first, then built-in constants
        variables[name]?.let { return it }
        Builtins.constantValue(name)?.let { return it }
        throw UndefinedVariableException(name)
    }

    private suspend fun evaluateFunction(name: String, argExprs: List<Expr>): Double {
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
            val localVars = mutableMapOf<String, Double>()
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

            var lastResult: Double = 0.0
            val localContext = MathContext(
                variables = localVars,
                localFunctions = mutableMapOf() // Inner scope cannot define new functions
            )
            for (stmt in localFunc.body) {
                innerEvaluator.evaluateStatement(stmt, localContext)?.let { result ->
                    lastResult = result
                }
            }
            return lastResult
        }

        if (name == "file") {
            throw EvalException("The `file()` function either needs to be assigned like `f = file(\"FileName\")` or used in dot notation like `file(\"FileName\").variable`")
        }

        // Fallback to built-ins
        val builtinArity = Builtins.getArity(name)
        if (builtinArity != null && argExprs.size != builtinArity) {
            throw ArityMismatchException(name, builtinArity, argExprs.size)
        }

        val args = argExprs.map { evaluate(it) }
        return Builtins.call(name, args)
    }

    /**
     * Executes a single statement inside a scope represented by `context`,
     * mutating `context` (variables or localFunctions) as necessary.
     * Returns the numeric result of the statement, or null if it produces no result.
     */
    suspend fun evaluateStatement(statement: Statement, context: MathContext): Double? {
        return when (statement) {
            is Statement.Empty -> null

            is Statement.FunctionDefinition -> {
                validateVariableOrFunctionName(statement.name)
                context.localFunctions[statement.name] = LocalFunction(statement.name, statement.params, statement.body)
                null
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
                    return null
                }

                // Intercept File Variable Copy
                if (statement.expr is Expr.Variable && context.fileVariables.containsKey(statement.expr.name)) {
                    val sourceFile = context.fileVariables[statement.expr.name]!!
                    context.variables.remove(name)
                    context.fileVariables[name] = sourceFile
                    return null
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
                val result = when (statement.op) {
                    TokenKind.PLUS_EQUALS    -> current + rhs
                    TokenKind.MINUS_EQUALS   -> current - rhs
                    TokenKind.STAR_EQUALS    -> current * rhs
                    TokenKind.SLASH_EQUALS   -> {
                        if (rhs == 0.0) throw DivisionByZeroException()
                        current / rhs
                    }
                    TokenKind.PERCENT_EQUALS -> {
                        if (rhs == 0.0) throw DivisionByZeroException()
                        current % rhs
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
                val result = current + 1
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
                val result = current - 1
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
    }

    private suspend fun evaluateBinaryOp(expr: Expr.BinaryOp): Double {
        val left = evaluate(expr.left)

        // Percentage addition/subtraction
        // e.g. `100 + 20%` means `100 * 1.20`
        if (expr.right is Expr.PercentLiteral) {
            val pct = expr.right.value
            return when (expr.op) {
                TokenKind.PLUS  -> left * (1.0 + pct / 100.0)
                TokenKind.MINUS -> left * (1.0 - pct / 100.0)
                else -> applyOp(left, expr.op, evaluate(expr.right))
            }
        }

        val right = evaluate(expr.right)
        return applyOp(left, expr.op, right)
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
    private suspend fun resolveMemberAccess(expr: Expr.MemberAccess): Double {
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

    private suspend fun resolveMemberFunctionCall(expr: Expr.MemberFunctionCall): Double {
        val obj = expr.obj
        val name = expr.name
        val argExprs = expr.args
        val fileName = getFileNameFromObj(obj, true)

        if (Builtins.isFunction(name)) {
            throw EvalException("`$name()` is a global function and should be called directly, not via dot notation")
        }
        val remoteContext = loadRemoteContext(fileName)

        val args = argExprs.map { evaluate(it) }
        val evaluatedArgs = args.map { Expr.NumberLiteral(it) }

        val remoteEvaluator = Evaluator(
            variables = remoteContext.variables,
            localFunctions = remoteContext.localFunctions,
            fileVariables = remoteContext.fileVariables,
            fileContextLoader = fileContextLoader,
            loadingStack = loadingStack
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
