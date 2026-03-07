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
    private val callStack: Set<String> = emptySet()
) {

    fun evaluate(expr: Expr): Double = when (expr) {
        is Expr.NumberLiteral  -> expr.value
        is Expr.PercentLiteral -> expr.value / 100.0
        is Expr.PercentOf      -> evaluate(expr.base) * expr.percent / 100.0
        is Expr.PercentOff     -> evaluate(expr.base) * (1.0 - expr.percent / 100.0)
        is Expr.UnaryMinus     -> -evaluate(expr.operand)
        is Expr.Variable       -> resolveVariable(expr.name)
        is Expr.FunctionCall   -> evaluateFunction(expr.name, expr.args)
        is Expr.BinaryOp       -> evaluateBinaryOp(expr)
    }

    private fun resolveVariable(name: String): Double {
        // Check user variables first, then built-in constants
        variables[name]?.let { return it }
        Builtins.constantValue(name)?.let { return it }
        throw UndefinedVariableException(name)
    }

    private fun evaluateFunction(name: String, argExprs: List<Expr>): Double {
        // Check if it's a user-defined local function
        val localFunc = localFunctions[name]
        if (localFunc != null) {
            if (argExprs.size != localFunc.params.size) {
                throw ArityMismatchException(name, localFunc.params.size, argExprs.size)
            }
            val args = argExprs.map { evaluate(it) }
            if (name in callStack) {
                throw EvalException("Infinite recursion detected in function '$name'")
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
                callStack = callStack + name
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
    fun evaluateStatement(statement: Statement, context: MathContext): Double? {
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
                validateVariableOrFunctionName(statement.name)
                val result = evaluate(statement.expr)
                context.variables[statement.name] = result
                result
            }

            is Statement.CompoundAssignment -> {
                validateVariableOrFunctionName(statement.name)
                val current = context.variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
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
                    else -> throw EvalException("Unknown compound operator: ${statement.op}")
                }
                context.variables[statement.name] = result
                result
            }

            is Statement.Increment -> {
                validateVariableOrFunctionName(statement.name)
                val current = context.variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
                val result = current + 1
                context.variables[statement.name] = result
                result
            }

            is Statement.Decrement -> {
                validateVariableOrFunctionName(statement.name)
                val current = context.variables[statement.name]
                    ?: throw UndefinedVariableException(statement.name)
                val result = current - 1
                context.variables[statement.name] = result
                result
            }
        }
    }

    private fun validateVariableOrFunctionName(name: String) {
        if (!name.matches(Regex(Constants.VAR_FUNC_NAME_PATTERN))) {
            throw EvalException("Invalid variable or function name '$name'")
        }
    }

    private fun evaluateBinaryOp(expr: Expr.BinaryOp): Double {
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
        else -> throw EvalException("Unknown operator: $op")
    }
}
