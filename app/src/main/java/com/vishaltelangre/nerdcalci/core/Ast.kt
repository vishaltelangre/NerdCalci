package com.vishaltelangre.nerdcalci.core

/**
 * Top-level AST node: what a single calculator line parses into.
 */
sealed class Statement {
    /** A line that evaluates an expression and displays its result. */
    data class ExprStatement(val expr: Expr) : Statement()

    /** Variable assignment: `price = 100` */
    data class Assignment(val name: String, val expr: Expr) : Statement()

    /** Compound assignment: `total += 5`, `x *= 2` */
    data class CompoundAssignment(val name: String, val op: TokenKind, val expr: Expr) : Statement()

    /** Increment: `count++` */
    data class Increment(val name: String) : Statement()

    /** Decrement: `count--` */
    data class Decrement(val name: String) : Statement()

    /** Local custom function definition: `f(x) = x * 2;` */
    data class FunctionDefinition(val name: String, val params: List<String>, val body: List<Statement>) : Statement()

    /** Blank line or pure comment — produces no result. */
    data object Empty : Statement()
}

/**
 * Expression AST nodes — the recursive arithmetic/function tree.
 */
sealed class Expr {
    /** Numeric literal: `42`, `3.14` */
    data class NumberLiteral(val value: Double) : Expr()

    /** Percentage literal: `20%` — a bare percentage without `of`/`off` context. */
    data class PercentLiteral(val value: Double) : Expr()

    /** Percentage-of: `20% of price` → `price * 0.20` */
    data class PercentOf(val percent: Double, val base: Expr) : Expr()

    /** Percentage-off: `15% off price` → `price * (1 - 0.15)` */
    data class PercentOff(val percent: Double, val base: Expr) : Expr()

    /** Unary negation: `-expr` */
    data class UnaryMinus(val operand: Expr) : Expr()

    /** Binary operation: `left op right` */
    data class BinaryOp(val left: Expr, val op: TokenKind, val right: Expr) : Expr()

    /** Variable reference: `price` */
    data class Variable(val name: String) : Expr()

    /** Function call: `sqrt(16)`, `pow(2, 8)` */
    data class FunctionCall(val name: String, val args: List<Expr>) : Expr()
}
