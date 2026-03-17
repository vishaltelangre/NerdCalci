package com.vishaltelangre.nerdcalci.core

/**
 * Converts a list of [Token]s into an AST (Abstract Syntax Tree).
 * Implements Pratt (precedence climbing) parser.
 *
 * The parser reads tokens left-to-right and builds a tree that represents the
 * structure of the expression, respecting math precedence rules like
 * "multiply before add".
 *
 * ## How it works
 *
 * Each type of expression has its own parsing method, ordered by priority
 * (lowest-priority operations are parsed first, highest-priority last):
 *
 * ```
 * parseExpression()                         — entry point
 *   └─ parseAddSub()                        — handles + and - (lowest priority)
 *        └─ parseMulDivMod()                — handles *, /, % (higher priority)
 *             └─ parsePower()               — handles ^ (even higher)
 *                  └─ parseUnary()          — handles negative sign: -x
 *                       └─ parsePrimary()   — numbers, variables, function calls, parens
 * ```
 *
 * This layering ensures that `2 + 3 * 4` parses as `2 + (3 * 4)` naturally,
 * because `parseAddSub` calls `parseMulDivMod` for each operand, and
 * `parseMulDivMod` "binds tighter" since it's deeper in the call chain.
 *
 * ## Statement detection
 *
 * Before parsing an expression, we check if the line is one of these patterns:
 * - `price = 100`   → Assignment
 * - `total += 5`    → CompoundAssignment
 * - `count++`       → Increment
 * - `count--`       → Decrement
 *
 * If none match, the whole line is treated as an expression to evaluate.
 */
class Parser(private val tokens: List<Token>) {

    private var pos = 0

    private fun peek(): Token = tokens[pos]
    private fun peekKind(): TokenKind = tokens[pos].kind
    private fun isAtEnd(): Boolean = peekKind() == TokenKind.EOF

    private fun advance(): Token {
        val token = tokens[pos]
        if (!isAtEnd()) pos++
        return token
    }

    /** Consume the next token, but only if it's the expected kind. Otherwise, throw an error. */
    private fun expect(kind: TokenKind): Token {
        val token = peek()
        if (token.kind != kind) {
            throw ParseException(
                "Expected ${kind.display}, but found ${token.kind.display}", token.position
            )
        }
        return advance()
    }

    /** Parse the full token stream into a single [Statement]. */
    fun parse(): Statement {
        if (isAtEnd()) return Statement.Empty
        val stmt = parseStatement(allowFunctionDef = true)
        // Allow trailing EOF; anything else is a parse error
        if (!isAtEnd()) {
            val leftover = peek()
            throw ParseException(
                "Unexpected ${leftover.kind.display}", leftover.position
            )
        }
        return stmt
    }

    /**
     * Detect what kind of statement this line is by looking at the first two tokens.
     *
     * Examples:
     * - `price = 100`   → sees IDENT then EQUALS       → Assignment
     * - `total += 5`    → sees IDENT then PLUS_EQUALS  → CompoundAssignment
     * - `count++`       → sees IDENT then PLUS_PLUS    → Increment
     * - `count--`       → sees IDENT then MINUS_MINUS  → Decrement
     * - `2 + 3 * 4`     → none of the above            → ExprStatement
     */
    private fun parseStatement(allowFunctionDef: Boolean = true): Statement {
        val kind = peekKind()
        if (kind == TokenKind.IDENTIFIER || kind.isPreviousLineAlias || kind.isLineNumberAlias) {
            val name = peek().lexeme
            val position = peek().position
            val nextKind = peekAt(1)

            return when (nextKind) {
                // e.g. price = 100
                TokenKind.EQUALS -> {
                    requireAssignable(name, position)
                    advance() // skip past "price"
                    advance() // skip past "="
                    val expr = parseExpression()
                    Statement.Assignment(name, expr)
                }
                // e.g. f(x) = x * 2;
                TokenKind.LPAREN -> {
                    // It could be a function definition (if it's `f(x) = ...`)
                    // or a function call (if it's just `f(x)` without `=`)
                    if (isFunctionDefinition()) {
                        if (!allowFunctionDef) {
                            throw ParseException("Nested function definitions are not allowed", position)
                        }
                        requireAssignable(name, position)
                        advance() // skip past "f" (function name)
                        return parseFunctionDefinition(name)
                    } else {
                        return Statement.ExprStatement(parseExpression())
                    }
                }
                // e.g. total += 5, score -= 3, x *= 2, x /= 4, x %= 3
                TokenKind.PLUS_EQUALS, TokenKind.MINUS_EQUALS,
                TokenKind.STAR_EQUALS, TokenKind.SLASH_EQUALS,
                TokenKind.PERCENT_EQUALS -> {
                    requireAssignable(name, position)
                    advance() // skip past "total"
                    val opToken = advance() // skip past "+=" (and remember which operator)
                    val expr = parseExpression()
                    Statement.CompoundAssignment(name, opToken.kind, expr)
                }
                // e.g. count++
                TokenKind.PLUS_PLUS -> {
                    requireAssignable(name, position)
                    advance() // skip past "count"
                    advance() // skip past "++"
                    Statement.Increment(name)
                }
                // e.g. count--
                TokenKind.MINUS_MINUS -> {
                    requireAssignable(name, position)
                    advance() // skip past "count"
                    advance() // skip past "--"
                    Statement.Decrement(name)
                }
                // Everything else is an expression, e.g. 2 + 3, price * 1.1
                else -> Statement.ExprStatement(parseExpression())
            }
        }

        return Statement.ExprStatement(parseExpression())
    }

    /**
     * Look ahead to check if this is a function definition like `f(x, y) = ...`
     * We are currently *before* the function name, but we know peak() is IDENTIFIER.
     * We know peekAt(1) is LPAREN.
     * We need to find the RPAREN and see if the next token is EQUALS.
     */
    private fun isFunctionDefinition(): Boolean {
        var offset = 2
        while (peekAt(offset) != TokenKind.EOF && peekAt(offset) != TokenKind.RPAREN) {
            offset++
        }
        if (peekAt(offset) == TokenKind.RPAREN) {
            return peekAt(offset + 1) == TokenKind.EQUALS
        }
        return false
    }

    /**
     * Parses a function definition. We have already consumed the function name.
     * The next token is `(`.
     * e.g. `(x, y) = x + y;`
     * A function body can contain multiple statements separated by `;`.
     */
    private fun parseFunctionDefinition(name: String): Statement.FunctionDefinition {
        expect(TokenKind.LPAREN)
        val params = mutableListOf<String>()
        if (peekKind() == TokenKind.IDENTIFIER) {
            params.add(advance().lexeme)
            while (peekKind() == TokenKind.COMMA) {
                advance() // skip past ","
                params.add(expect(TokenKind.IDENTIFIER).lexeme)
            }
        }
        expect(TokenKind.RPAREN)
        expect(TokenKind.EQUALS)

        val body = mutableListOf<Statement>()

        while (!isAtEnd()) {
            body.add(parseStatement(allowFunctionDef = false)) // Prevent nested defs
            if (peekKind() == TokenKind.SEMICOLON) {
                advance() // skip past ";"
            } else {
                break
            }
        }

        return Statement.FunctionDefinition(name, params, body)
    }

    private fun parseExpression(): Expr = parseAddSub()

    /**
     * Parse addition and subtraction: `a + b`, `a - b`, `a + b - c`.
     *
     * These are the lowest-priority arithmetic operations, so they're parsed
     * first (outermost). Each operand is parsed by [parseMulDivMod], which
     * handles higher-priority `*` and `/` before returning.
     *
     * Example: `2 + 3 * 4` → calls parseMulDivMod() twice:
     *   left  = parseMulDivMod() → just `2` (no * or / follows)
     *   right = parseMulDivMod() → `3 * 4 = 12`
     *   result: BinaryOp(2, +, BinaryOp(3, *, 4))
     *
     * **Percentage shorthand:** if the right side of `+` or `-` is a bare
     * percentage like `20%`, the evaluator treats it specially:
     *   `1000 + 20%`  →  `1000 * 1.20 = 1200` (add 20% of itself)
     *   `1000 - 5%`   →  `1000 * 0.95 = 950`  (subtract 5% of itself)
     */
    private fun parseAddSub(): Expr {
        var left = parseMulDivMod()
        while (peekKind() == TokenKind.PLUS || peekKind() == TokenKind.MINUS) {
            val op = advance()
            val right = parseMulDivMod()
            left = Expr.BinaryOp(left, op.kind, right)
        }
        return left
    }

    /**
     * Parse multiplication, division, and modulo: `a * b`, `a / b`, `a % b`.
     *
     * Higher priority than `+`/`-`, so `2 + 3 * 4` becomes `2 + (3 * 4)`.
     *
     * **`%` ambiguity:** the `%` sign can mean two things:
     *   - Modulo (remainder): `10 % 3` → `1`
     *   - Part of a percentage: `20%` in `price + 20%`
     *
     * This function only treats `%` as modulo if a number/expression follows it
     * (i.e. it has a right-hand operand like `10 % 3`). Otherwise, it stops and
     * lets the `%` be picked up as part of a percentage by the number that
     * already parsed before it (e.g. `20` sees `%` and becomes `20%`).
     */
    private fun parseMulDivMod(): Expr {
        var left = parsePower()
        while (peekKind() == TokenKind.STAR || peekKind() == TokenKind.SLASH ||
               peekKind() == TokenKind.PERCENT) {
            if (peekKind() == TokenKind.PERCENT) {
                // % as modulo only if followed by something that can start an expression
                if (!canStartExpression(peekAt(1))) break
            }
            val op = advance()
            val right = parsePower()
            left = Expr.BinaryOp(left, op.kind, right)
        }
        return left
    }

    /**
     * Parse exponentiation: `2 ^ 3`, `2 ^ 3 ^ 4`.
     *
     * Higher priority than `*` and `/`, so `2 * 3 ^ 4` becomes `2 * (3 ^ 4)`.
     *
     * Unlike `+` and `*`, exponentiation groups from the **right**:
     *   `2 ^ 3 ^ 4` means `2 ^ (3 ^ 4)` = `2 ^ 81`, NOT `(2 ^ 3) ^ 4 = 8 ^ 4`.
     *
     * This is achieved by calling [parsePower] recursively for the right operand
     * instead of using a loop (which would group from the left).
     */
    private fun parsePower(): Expr {
        val base = parseUnary()
        if (peekKind() == TokenKind.CARET) {
            advance() // skip past "^"
            val exponent = parsePower() // recursive call → groups from the right
            return Expr.BinaryOp(base, TokenKind.CARET, exponent)
        }
        return base
    }

    /**
     * Parse unary minus: `-42`, `-x`, `-(a + b)`.
     *
     * This handles the negative sign before a value. It can be nested:
     * `--5` parses as `-(-5)` = `5`.
     */
    private fun parseUnary(): Expr {
        if (peekKind() == TokenKind.MINUS) {
            advance()
            val operand = parseUnary()
            return Expr.UnaryMinus(operand)
        }
        return parsePrimary()
    }

    /**
     * Parse the most basic building blocks of an expression.
     *
     * This handles (in order):
     *
     * 1. **Numbers** — `42`, `3.14`
     *    - If followed by `% of expr`  → percentage-of:  `20% of price`
     *    - If followed by `% off expr` → percentage-off: `15% off price`
     *    - If followed by bare `%`     → percentage literal for use with +/-
     *
     * 2. **Identifiers** — variable references or function calls:
     *    - If followed by `(` → function call: `sqrt(16)`, `pow(2, 8)`
     *    - Otherwise → variable reference: `price`, `total`
     *
     * 3. **Parenthesized groups** — `(2 + 3)` → parse the inner expression
     */
    private fun parsePrimary(): Expr {
        val kind = peekKind()
        return when (kind) {
            TokenKind.NUMBER -> {
                val numToken = advance()
                // Check for percentage syntax: 20% of X, 15% off X, or bare 20%
                if (peekKind() == TokenKind.PERCENT) {
                    val nextKind = peekAt(1)
                    // e.g. 20% of price
                    if (nextKind == TokenKind.KW_OF) {
                        advance() // skip past "%"
                        advance() // skip past "of"
                        val base = parseExpression()
                        return Expr.PercentOf(numToken.value, base)
                    // e.g. 15% off price
                    } else if (nextKind == TokenKind.KW_OFF) {
                        advance() // skip past "%"
                        advance() // skip past "off"
                        val base = parseExpression()
                        return Expr.PercentOff(numToken.value, base)
                    } else if (canStartExpression(nextKind)) {
                        // % is followed by expression, so it's a MODULO operator.
                        // Do not consume % here; let parseMulDivMod handle it.
                    // e.g. bare 20% (used in "price + 20%")
                    } else {
                        advance() // skip past "%"
                        return Expr.PercentLiteral(numToken.value)
                    }
                }

                Expr.NumberLiteral(numToken.value)
            }

            TokenKind.IDENTIFIER -> {
                val nameToken = advance()
                val name = nameToken.lexeme
                if (peekKind() == TokenKind.LPAREN) {
                    // e.g. sqrt(16), pow(2, 8)
                    advance() // skip past "("
                    val args = parseArgList()
                    expect(TokenKind.RPAREN) // expect closing ")"
                    Expr.FunctionCall(name, args)
                } else {
                    // e.g. price, total, PI
                    Expr.Variable(name)
                }
            }

            else -> {
                if (kind.isPreviousLineAlias || kind.isLineNumberAlias) {
                    val token = advance()
                    Expr.Variable(token.lexeme)
                } else if (kind == TokenKind.LPAREN) {
                    // e.g. (2 + 3), (price * 1.1)
                    advance() // skip past "("
                    val expr = parseExpression()
                    expect(TokenKind.RPAREN) // expect closing ")"
                    expr
                } else {
                    val token = peek()
                    throw ParseException(
                        "Expected a value or '(', but found ${token.kind.display}", token.position
                    )
                }
            }
        }
    }

    /** Parse a comma-separated list of function arguments, e.g. `2, 8` in `pow(2, 8)` */
    private fun parseArgList(): List<Expr> {
        if (peekKind() == TokenKind.RPAREN) return emptyList()
        val args = mutableListOf(parseExpression())
        while (peekKind() == TokenKind.COMMA) {
            advance() // skip past ","
            args.add(parseExpression())
        }
        return args
    }

    /**
     * Reject assignment to reserved names.
     *
     * Built-in functions (sin, sqrt, pow, ...) and constants (PI, E) cannot be
     * used as variable names. For example, `sin = 5` is an error.
     */
    private fun requireAssignable(name: String, position: Int) {
        if (Builtins.isBuiltin(name) || MathEngine.reservedVariableNames.contains(name)) {
            throw ParseException("'$name' is reserved and cannot be reassigned", position)
        }
    }

    /** Peek at token kind at [offset] positions ahead without consuming anything. */
    private fun peekAt(offset: Int): TokenKind {
        val idx = pos + offset
        return if (idx < tokens.size) tokens[idx].kind else TokenKind.EOF
    }

    /** Check whether a token kind can start an expression (used for % disambiguation). */
    private fun canStartExpression(kind: TokenKind): Boolean =
        kind == TokenKind.MINUS || kind == TokenKind.NUMBER || kind == TokenKind.LPAREN ||
                kind == TokenKind.IDENTIFIER || kind.isPreviousLineAlias || kind.isLineNumberAlias
}
