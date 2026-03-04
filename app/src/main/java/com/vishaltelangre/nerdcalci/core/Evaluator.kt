package com.vishaltelangre.nerdcalci.core

import kotlin.math.pow

/**
 * Recursive tree-walk evaluator that computes a [Double] result from an [Expr] AST.
 *
 * Variables are resolved from the provided [variables] map.
 * Functions are dispatched to [Builtins].
 * Typed exceptions provide specific error messages.
 */
class Evaluator(private val variables: Map<String, Double>) {

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
        val args = argExprs.map { evaluate(it) }
        return Builtins.call(name, args)
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
