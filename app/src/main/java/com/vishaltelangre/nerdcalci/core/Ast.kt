package com.vishaltelangre.nerdcalci.core

/**
 * Top-level AST node: what a single calculator line parses into.
 */
sealed class Statement {
    /** A line that evaluates an expression and displays its result. */
    data class ExprStatement(val expr: Expr) : Statement()

    /** Variable assignment: `price = 100` */
    data class Assignment(val target: Expr, val expr: Expr) : Statement()

    /** Compound assignment: `total += 5`, `x *= 2` */
    data class CompoundAssignment(val target: Expr, val op: TokenKind, val expr: Expr) : Statement()

    /** Increment: `count++` */
    data class Increment(val target: Expr) : Statement()

    /** Decrement: `count--` */
    data class Decrement(val target: Expr) : Statement()

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

    /** String literal: `"hello"` */
    data class StringLiteral(val value: String) : Expr()

    /** Member access: `obj.name` */
    data class MemberAccess(val obj: Expr, val name: String) : Expr()

    /** Member function call: `obj.func(args)` */
    data class MemberFunctionCall(val obj: Expr, val name: String, val args: List<Expr>) : Expr()

    /** A value tagged with a unit: `10 km`, `5 hours` */
    data class Quantity(val value: Expr, val unit: String) : Expr()

    /** Unit conversion: `<expr> [to|in|as] <unit>` */
    data class UnitConversion(val expr: Expr, val toUnit: String) : Expr()
}

