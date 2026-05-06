package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal

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
                "Expected `${kind.display}`, but found `${token.kind.display}`", token.position
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
                "Unexpected `${leftover.kind.display}`", leftover.position
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
        if (kind == TokenKind.IDENTIFIER && peekAt(1) == TokenKind.LPAREN) {
            if (isFunctionDefinition()) {
                val position = peek().position
                if (!allowFunctionDef) {
                    throw ParseException("Functions cannot be created inside other functions", position)
                }
                val name = peek().lexeme
                requireAssignable(name, position)
                advance() // skip past name
                return parseFunctionDefinition(name)
            }
        }

        val checkpoint = pos
        val target: Expr = try {
            parsePostfix()
        } catch (e: Exception) {
            pos = checkpoint
            return Statement.ExprStatement(parseExpression())
        }

        val nextKind = peekKind()
        val isAssignment = nextKind == TokenKind.EQUALS ||
                nextKind == TokenKind.PLUS_EQUALS ||
                nextKind == TokenKind.MINUS_EQUALS ||
                nextKind == TokenKind.STAR_EQUALS ||
                nextKind == TokenKind.SLASH_EQUALS ||
                nextKind == TokenKind.PERCENT_EQUALS ||
                nextKind == TokenKind.PLUS_PLUS ||
                nextKind == TokenKind.MINUS_MINUS

        if (isAssignment) {
            requireAssignableExpr(target)
            return when (nextKind) {
                TokenKind.EQUALS -> {
                    advance()
                    Statement.Assignment(target, parseExpression())
                }
                TokenKind.PLUS_EQUALS, TokenKind.MINUS_EQUALS,
                TokenKind.STAR_EQUALS, TokenKind.SLASH_EQUALS,
                TokenKind.PERCENT_EQUALS -> {
                    val opToken = advance()
                    Statement.CompoundAssignment(target, opToken.kind, parseExpression())
                }
                // e.g. count++
                TokenKind.PLUS_PLUS -> {
                    advance()
                    Statement.Increment(target)
                }
                // e.g. count--
                TokenKind.MINUS_MINUS -> {
                    advance()
                    Statement.Decrement(target)
                }
                else -> throw ParseException("Something is wrong with this notation", peek().position)
            }
        } else {
            pos = checkpoint
            return Statement.ExprStatement(parseExpression())
        }
    }

    private fun requireAssignableExpr(target: Expr) {
        if (target is Expr.MemberAccess) {
            throw ParseException("Variables from other files are read-only and cannot be changed", peek().position)
        }
        if (target is Expr.Variable) {
            requireAssignable(target.name, peek().position)
            return
        }
        throw ParseException("Values can only be assigned to variables", peek().position)
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

    private fun parseExpression(): Expr {
        return parseAddSub()
    }

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
    private fun parseAddSub(allowUnitConversion: Boolean = true): Expr {
        var left = parseMulDivMod()
        while (peekKind() == TokenKind.PLUS || peekKind() == TokenKind.MINUS ||
            (allowUnitConversion && isUnitOperator(peekKind())) || isDatePreposition(peekKind())
        ) {
            val kind = peekKind()
            if (isDatePreposition(kind)) {
                val op = advance()
                // Special case for "ago" or "from": if it's at the end or not followed by an expression,
                // we treat it as "ago from now" or "from now".
                if ((op.kind == TokenKind.KW_AGO || op.kind == TokenKind.KW_FROM) && !canStartExpression(peekKind())) {
                    left = Expr.BinaryOp(left, op.kind, Expr.Variable("now"))
                } else {
                    // Right-associative for before/after/ago/from, but NOT for through
                    // because through has an optional 'in days' suffix that parseAddSub would consume.
                    val right = if (op.kind == TokenKind.KW_THROUGH) parseAddSub(allowUnitConversion = false) else parseAddSub()
                    if (op.kind == TokenKind.KW_THROUGH) {
                        var unit: String? = null
                        if (peekKind() == TokenKind.KW_IN && tokens.getOrNull(pos + 1)?.kind == TokenKind.IDENTIFIER) {
                            val nextLexeme = tokens[pos + 1].lexeme
                            if (UnitConverter.findUnit(nextLexeme)?.category == UnitCategory.TIME) {
                                advance() // consume "in"
                                advance() // consume unit
                                unit = nextLexeme
                            }
                        }
                        left = Expr.DateInterval(left, right, projectionUnit = unit, inclusive = true)
                    } else {
                        left = Expr.BinaryOp(left, op.kind, right)
                    }
                }
            } else if (allowUnitConversion && isUnitOperator(kind)) {
                if (kind == TokenKind.KW_TO && left is Expr.NumberLiteral && peekAt(1) == TokenKind.NUMBER) {
                    // Only treat as year interval if both sides look like 4-digit years (1000..9999).
                    val leftVal = left.value
                    val rightVal = tokens.getOrNull(pos + 1)?.value
                    val leftIsYear = leftVal.stripTrailingZeros().scale() <= 0 &&
                        leftVal.toLong() in 1000L..9999L
                    val rightIsYear = rightVal != null && rightVal.stripTrailingZeros().scale() <= 0 &&
                        rightVal.toLong() in 1000L..9999L
                    if (leftIsYear && rightIsYear) {
                        advance()
                        val right = parseMulDivMod()
                        left = Expr.YearInterval(left, right)
                        continue
                    }
                    // Fall through to date-interval / other handling below.
                }
                if (kind == TokenKind.KW_TO) {
                    val nextKind = peekAt(1)
                    if (nextKind in setOf(TokenKind.KW_TODAY, TokenKind.KW_YESTERDAY, TokenKind.KW_TOMORROW, TokenKind.KW_NOW) ||
                        (nextKind == TokenKind.IDENTIFIER && peekAt(2) == TokenKind.LPAREN) ||
                        nextKind == TokenKind.NUMBER) {
                        advance() // consume 'to'
                        val right = parseAddSub(allowUnitConversion = false)
                        var unit: String? = null
                        if (peekKind() == TokenKind.KW_IN && tokens.getOrNull(pos + 1)?.kind == TokenKind.IDENTIFIER) {
                            val nextLexeme = tokens[pos + 1].lexeme
                            if (UnitConverter.findUnit(nextLexeme)?.category == UnitCategory.TIME) {
                                advance() // consume "in"
                                advance() // consume unit
                                unit = nextLexeme
                            }
                        }
                        left = Expr.DateInterval(left, right, projectionUnit = unit, inclusive = false)
                        continue
                    }
                }
                advance() // consume to/in/as
                val target = peek()
                if (target.kind == TokenKind.STRING_LITERAL &&
                    (kind == TokenKind.KW_IN || kind == TokenKind.KW_AS)) {
                    advance()
                    left = Expr.DateModifier(left, kind.display, target.lexeme)
                } else if (target.kind == TokenKind.IDENTIFIER || isUnitOperator(target.kind) || target.kind == TokenKind.KW_OF ||
                    target.kind == TokenKind.KW_TODAY || target.kind == TokenKind.KW_YESTERDAY ||
                    target.kind == TokenKind.KW_TOMORROW || target.kind == TokenKind.KW_NOW) {
                    val firstLexeme = target.lexeme
                    advance() // consume first

                    val combinedUnit = parseCombinedUnit(firstLexeme)
                    left = Expr.UnitConversion(left, combinedUnit)
                } else {
                    throw ParseException("Expected unit name after conversion operator", target.position)
                }
            } else {
                val op = advance()
                val right = parseMulDivMod()
                left = Expr.BinaryOp(left, op.kind, right)
            }
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
        return parsePostfix()
    }

    /**
     * Parse postfix operations, specifically member access and function calls
     * using dot notation (e.g., `obj.member` or `obj.func()`).
     *
     * It iteratively chains dot access expressions onto the base expression.
     */
    private fun parsePostfix(): Expr {
        var expr = parsePrimary()
        while (true) {
            val kind = peekKind()
            if (kind == TokenKind.DOT) {
                advance() // consume "."
                val token = peek()

                if (token.kind != TokenKind.IDENTIFIER && !token.kind.name.startsWith("KW_")) {
                    val message = if (token.kind == TokenKind.EOF) {
                        "Missing variable or function name after `.`"
                    } else {
                        "Expected variable or function name after `.`, but found `${token.lexeme}`"
                    }
                    throw ParseException(message, token.position)
                }
                advance()
                val name = token.lexeme

                // Check if it's a member function call
                if (peekKind() == TokenKind.LPAREN) {
                    advance() // consume "("
                    val args = parseArgList()
                    expect(TokenKind.RPAREN)
                    expr = Expr.MemberFunctionCall(expr, name, args)
                } else {
                    // Otherwise treat it as a property/variable accessor
                    expr = Expr.MemberAccess(expr, name)
                }
            } else if (kind == TokenKind.PERCENT) {
                val nextKind = peekAt(1)
                // e.g. a% of b  OR  a% of what is b
                if (nextKind == TokenKind.KW_OF) {
                    advance() // skip past "%"
                    advance() // skip past "of"
                    if (peekKind() == TokenKind.KW_WHAT) {
                        advance() // skip past "what"
                        expect(TokenKind.KW_IS)
                        val value = parseExpression()
                        expr = Expr.ReversePercentOf(expr, value)
                    } else {
                        val base = parseExpression()
                        expr = Expr.PercentOf(expr, base)
                    }
                // e.g. a% off b
                } else if (nextKind == TokenKind.KW_OFF) {
                    advance() // skip past "%"
                    advance() // skip past "off"
                    val base = parseExpression()
                    expr = Expr.PercentOff(expr, base)
                } else if (canStartExpression(nextKind)) {
                    // % is followed by expression, so it's a MODULO operator.
                    // Do not consume % here; let parseMulDivMod handle it.
                    break
                // e.g. bare a% (used in "price + a%")
                } else {
                    advance() // skip past "%"
                    expr = Expr.PercentLiteral(expr)
                }
            } else {
                break
            }
        }
        return expr
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
            TokenKind.STRING_LITERAL -> {
                val token = advance()
                Expr.StringLiteral(token.lexeme)
            }

            TokenKind.KW_FILE -> {
                advance() // consume "file"
                expect(TokenKind.LPAREN)
                val args = parseArgList()
                expect(TokenKind.RPAREN)
                Expr.FunctionCall("file", args)
            }

            TokenKind.NUMBER -> {
                val numToken = advance()
                var numericValue = numToken.value

                // Extract scalable numeral systems (million, lakh, etc) immediately
                if (peekKind() == TokenKind.IDENTIFIER) {
                    val multiplier = getNumeralMultiplier(peek().lexeme)
                    if (multiplier != null) {
                        advance() // consume numeral word
                        numericValue *= multiplier
                    }
                }

                parseQuantityIfPossible(Expr.NumberLiteral(numericValue))
            }


            TokenKind.IDENTIFIER -> {
                val nameToken = advance()
                val name = nameToken.lexeme
                if (name.equals("days", ignoreCase = true) && peekKind() == TokenKind.KW_BETWEEN) {
                    advance()
                    val start = parseExpression()
                    expect(TokenKind.KW_AND)
                    val end = parseAddSub(allowUnitConversion = false)
                    var unit: String? = "days"
                    if (peekKind() == TokenKind.KW_IN && tokens.getOrNull(pos + 1)?.kind == TokenKind.IDENTIFIER) {
                        val nextLexeme = tokens[pos + 1].lexeme
                        if (UnitConverter.findUnit(nextLexeme)?.category == UnitCategory.TIME) {
                            advance() // consume "in"
                            advance() // consume unit
                            unit = nextLexeme
                        }
                    }
                    Expr.DateInterval(start, end, projectionUnit = unit, inclusive = false, absolute = true)
                }
 else if (name.equals("days", ignoreCase = true) &&
                    (peekKind() == TokenKind.KW_SINCE || peekKind() == TokenKind.KW_TILL || peekKind() == TokenKind.KW_UNTIL)
                ) {
                    val op = advance().kind
                    val base = parseAddSub(allowUnitConversion = false)
                    var unit: String? = "days"
                    if (peekKind() == TokenKind.KW_IN && tokens.getOrNull(pos + 1)?.kind == TokenKind.IDENTIFIER) {
                        val nextLexeme = tokens[pos + 1].lexeme
                        if (UnitConverter.findUnit(nextLexeme)?.category == UnitCategory.TIME) {
                            advance() // consume "in"
                            advance() // consume unit
                            unit = nextLexeme
                        }
                    }
                    if (unit != null && !unit.equals("days", ignoreCase = true)) {
                        // Return as a TimeCount if a specific unit is requested
                        Expr.DayCountQuery(op, base, projectionUnit = unit)
                    } else {
                        Expr.DayCountQuery(op, base)
                    }
                } else if (peekKind() == TokenKind.LPAREN) {
                    // e.g. sqrt(16), pow(2, 8)
                    advance() // skip past "("
                    val args = parseArgList()
                    expect(TokenKind.RPAREN) // expect closing ")"
                    Expr.FunctionCall(name, args)
                } else {
                    // e.g. price, total, PI
                    parseQuantityIfPossible(Expr.Variable(name))
                }
            }

            TokenKind.KW_TODAY, TokenKind.KW_YESTERDAY, TokenKind.KW_TOMORROW, TokenKind.KW_NOW -> {
                val token = advance()
                parseQuantityIfPossible(Expr.Variable(token.lexeme))
            }

            TokenKind.KW_BETWEEN -> {
                advance()
                val start = parseExpression()
                expect(TokenKind.KW_AND)
                val end = parseAddSub(allowUnitConversion = false)
                var unit: String? = null
                if (peekKind() == TokenKind.KW_IN && tokens.getOrNull(pos + 1)?.kind == TokenKind.IDENTIFIER) {
                    val nextLexeme = tokens[pos + 1].lexeme
                    if (UnitConverter.findUnit(nextLexeme)?.category == UnitCategory.TIME) {
                        advance() // consume "in"
                        advance() // consume unit
                        unit = nextLexeme
                    }
                }
                Expr.DateInterval(start, end, projectionUnit = unit, inclusive = false, absolute = true)
            }

            else -> {
                if (kind.isPreviousLineAlias || kind.isLineNumberAlias) {
                    val token = advance()
                    parseQuantityIfPossible(Expr.Variable(token.lexeme))
                } else if (kind == TokenKind.LPAREN) {
                    // e.g. (2 + 3), (price * 1.1)
                    advance() // skip past "("
                    val expr = parseExpression()
                    expect(TokenKind.RPAREN) // expect closing ")"
                    parseQuantityIfPossible(expr)
                } else {
                    val token = peek()
                    throw ParseException(
                        "Expected a value or `(`, but found `${token.kind.display}`", token.position
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
     * Built-in functions (sin, sqrt, pow, ...) and constants (PI, e) cannot be
     * used as variable names. For example, `sin = 5` is an error.
     */
    private fun requireAssignable(name: String, position: Int) {
        if (Builtins.isBuiltin(name) || MathEngine.reservedVariableNames.contains(name)) {
            throw ParseException("`$name` is a reserved name and cannot be changed", position)
        }
    }

    /** Peek at token kind at [offset] positions ahead without consuming anything. */
    private fun peekAt(offset: Int): TokenKind {
        val idx = pos + offset
        return if (idx < tokens.size) tokens[idx].kind else TokenKind.EOF
    }

    /** Check whether a token kind can start an expression (used for % disambiguation). */
    private fun canStartExpression(kind: TokenKind): Boolean =
        kind == TokenKind.NUMBER || kind == TokenKind.LPAREN ||
                kind == TokenKind.IDENTIFIER || kind.isPreviousLineAlias || kind.isLineNumberAlias ||
                kind == TokenKind.STRING_LITERAL || kind == TokenKind.KW_FILE ||
                kind == TokenKind.KW_TODAY || kind == TokenKind.KW_YESTERDAY ||
                kind == TokenKind.KW_TOMORROW || kind == TokenKind.KW_NOW ||
                kind == TokenKind.KW_BETWEEN

    private fun parseQuantityIfPossible(firstValue: Expr): Expr {
        val initialPos = pos  // save cursor before any quantity parsing
        var quantities = mutableListOf<Expr.Quantity>()

        fun inner(value: Expr): Expr.Quantity? {
            val nextKind = peekKind()
            if (nextKind == TokenKind.IDENTIFIER || isUnitOperator(nextKind) || nextKind == TokenKind.KW_OF) {
                val firstLexeme = peek().lexeme
                val savedPos = pos
                val t = advance() // consume first
                val combinedUnit = parseCombinedUnit(firstLexeme)

                var treatAsOperator = false
                if (combinedUnit == "in" && t.kind == TokenKind.KW_IN) {
                    val ahead = peekKind()
                    if (ahead == TokenKind.IDENTIFIER) {
                        treatAsOperator = true
                    }
                }

                if (!treatAsOperator && UnitConverter.findUnit(combinedUnit) != null) {
                    return Expr.Quantity(value, combinedUnit)
                }
                pos = savedPos // Backtrack!

                val firstUnit = UnitConverter.findUnit(firstLexeme)

                treatAsOperator = false
                if (firstLexeme == "in" && tokens[pos].kind == TokenKind.KW_IN) {
                    val aheadPos = pos + 1
                    val ahead = if (aheadPos < tokens.size) tokens[aheadPos].kind else TokenKind.EOF
                    if (ahead == TokenKind.IDENTIFIER) {
                        treatAsOperator = true
                    }
                }

                if (!treatAsOperator && firstUnit != null) {
                    advance() // consume first
                    return Expr.Quantity(value, firstLexeme)
                }
            }
            return null
        }

        val q1 = inner(firstValue)
        if (q1 != null) {
            quantities.add(q1)
            // Look for more: e.g. "3w 2d 5h"
            while (peekKind() == TokenKind.NUMBER) {
                val start = pos
                val numToken = advance()
                val nextQ = inner(Expr.NumberLiteral(numToken.value))
                if (nextQ != null) {
                    quantities.add(nextQ)
                } else {
                    pos = start // Backtrack the number
                    break
                }
            }
        }

        return when {
            quantities.size > 1 -> {
                // Reject composites where any part has a fractional value.
                val anyFractional = quantities.any { q ->
                    val numExpr = q.value
                    numExpr is Expr.NumberLiteral && numExpr.value.stripTrailingZeros().scale() > 0
                }
                if (anyFractional) {
                    error("Fractional components (like 2.5 years) are not supported in multi-unit durations. Please use whole numbers.")
                }
                Expr.CompositeQuantity(quantities)
            }
            quantities.size == 1 -> quantities[0]
            else -> firstValue
        }
    }

    private fun parseCombinedUnit(startLexeme: String): String {
        var bestCombined = startLexeme
        var currentCombined = startLexeme
        var tempPos = pos
        var consumeCount = 0

        while (tempPos < tokens.size) {
            val t = tokens[tempPos]
            if (t.kind == TokenKind.IDENTIFIER || isUnitOperator(t.kind) || t.kind == TokenKind.KW_OF) {
                currentCombined += " ${t.lexeme}"
                if (UnitConverter.findUnit(currentCombined) != null) {
                    bestCombined = currentCombined
                    consumeCount = (tempPos - pos) + 1
                }
                tempPos++
            } else if (t.kind == TokenKind.MINUS) {
                val nextIdx = tempPos + 1
                if (nextIdx < tokens.size && tokens[nextIdx].kind == TokenKind.IDENTIFIER) {
                    currentCombined += "-${tokens[nextIdx].lexeme}"
                    if (UnitConverter.findUnit(currentCombined) != null) {
                        bestCombined = currentCombined
                        consumeCount = (nextIdx - pos) + 1
                    }
                    tempPos = nextIdx + 1
                } else {
                    break
                }
            } else {
                break
            }
        }
        for (i in 0 until consumeCount) advance()
        return bestCombined
    }

    private fun isUnitOperator(kind: TokenKind): Boolean =
        kind == TokenKind.KW_TO || kind == TokenKind.KW_IN || kind == TokenKind.KW_AS

    private fun isDatePreposition(kind: TokenKind): Boolean =
        kind == TokenKind.KW_BEFORE || kind == TokenKind.KW_AFTER || kind == TokenKind.KW_AGO ||
                kind == TokenKind.KW_FROM || kind == TokenKind.KW_SINCE || kind == TokenKind.KW_TILL ||
                kind == TokenKind.KW_UNTIL || kind == TokenKind.KW_THROUGH

    private fun getNumeralMultiplier(word: String): BigDecimal? {
        return when (word.lowercase()) {
            "dozen", "dozens" -> BigDecimal("12")
            "hundred", "hundreds" -> BigDecimal("100")
            "thousand", "thousands" -> BigDecimal("1000")
            "lakh", "lakhs" -> BigDecimal("100000")
            "million", "millions" -> BigDecimal("1000000")
            "crore", "crores" -> BigDecimal("10000000")
            "billion", "billions" -> BigDecimal("1000000000")
            "trillion", "trillions" -> BigDecimal("1000000000000")
            "quadrillion", "quadrillions" -> BigDecimal("1e15")
            "quintillion", "quintillions" -> BigDecimal("1e18")
            else -> null
        }
    }
}

private class BacktrackException : Exception()
