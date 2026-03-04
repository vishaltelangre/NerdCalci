package com.vishaltelangre.nerdcalci.core

/**
 * Single-pass character scanner that converts a raw expression string into a
 * list of [Token]s.
 *
 * Handles:
 * - Decimal number literals (`42`, `3.14`, `.5`)
 * - Identifiers and keyword resolution (`price`, `of`, `off`)
 * - Single-char operators (`+`, `-`, `*`, `/`, `%`, `^`, `(`, `)`, `,`, `=`)
 * - Multi-char operators (`+=`, `-=`, `*=`, `/=`, `%=`, `++`, `--`)
 * - Unicode normalization (`×` → STAR, `÷` → SLASH)
 * - Comments (`#` stops scanning, rest is ignored)
 * - Whitespace (skipped silently)
 */
class Lexer(private val source: String) {

    private var pos = 0

    /** Keyword lookup: identifier text → keyword token kind. */
    private val keywords = mapOf(
        "of" to TokenKind.KW_OF,
        "off" to TokenKind.KW_OFF,
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (pos < source.length) {
            val start = pos
            val ch = source[pos]
            when {
                ch == '#' -> {
                    // Comment — stop scanning entirely
                    pos = source.length
                }
                ch.isWhitespace() -> {
                    pos++
                }
                ch.isDigit() || (ch == '.' && peekNext()?.isDigit() == true) -> {
                    tokens.add(scanNumber(start))
                }
                ch.isLetter() || ch == '_' -> {
                    tokens.add(scanIdentifier(start))
                }
                else -> {
                    tokens.add(scanOperator(start))
                }
            }
        }
        tokens.add(Token(TokenKind.EOF, "", position = pos))
        return tokens
    }

    private fun scanNumber(start: Int): Token {
        // Integer part
        while (pos < source.length && source[pos].isDigit()) pos++

        // Decimal part
        if (pos < source.length && source[pos] == '.' &&
            (pos + 1 < source.length && source[pos + 1].isDigit())) {
            pos++ // consume '.'
            while (pos < source.length && source[pos].isDigit()) pos++
        }

        val lexeme = source.substring(start, pos)
        val value = lexeme.toDouble()
        return Token(TokenKind.NUMBER, lexeme, value, start)
    }

    private fun scanIdentifier(start: Int): Token {
        while (pos < source.length && (source[pos].isLetterOrDigit() || source[pos] == '_')) pos++
        val lexeme = source.substring(start, pos)
        val kind = keywords[lexeme] ?: TokenKind.IDENTIFIER
        return Token(kind, lexeme, position = start)
    }

    private fun scanOperator(start: Int): Token {
        val ch = source[pos]
        pos++
        return when (ch) {
            '+' -> when {
                match('=') -> Token(TokenKind.PLUS_EQUALS, "+=", position = start)
                match('+') -> Token(TokenKind.PLUS_PLUS, "++", position = start)
                else       -> Token(TokenKind.PLUS, "+", position = start)
            }
            '-' -> when {
                match('=') -> Token(TokenKind.MINUS_EQUALS, "-=", position = start)
                match('-') -> Token(TokenKind.MINUS_MINUS, "--", position = start)
                else       -> Token(TokenKind.MINUS, "-", position = start)
            }
            '*', '×' -> when {
                match('=') -> Token(TokenKind.STAR_EQUALS, "*=", position = start)
                else       -> Token(TokenKind.STAR, "*", position = start)
            }
            '/', '÷' -> when {
                match('=') -> Token(TokenKind.SLASH_EQUALS, "/=", position = start)
                else       -> Token(TokenKind.SLASH, "/", position = start)
            }
            '%' -> when {
                match('=') -> Token(TokenKind.PERCENT_EQUALS, "%=", position = start)
                else       -> Token(TokenKind.PERCENT, "%", position = start)
            }
            '^' -> Token(TokenKind.CARET, "^", position = start)
            '(' -> Token(TokenKind.LPAREN, "(", position = start)
            ')' -> Token(TokenKind.RPAREN, ")", position = start)
            ',' -> Token(TokenKind.COMMA, ",", position = start)
            '=' -> Token(TokenKind.EQUALS, "=", position = start)
            else -> throw ParseException("Unexpected character '${ch}'", start)
        }
    }

    /** Consume the next character if it matches [expected], returning true. */
    private fun match(expected: Char): Boolean {
        if (pos < source.length && source[pos] == expected) {
            pos++
            return true
        }
        return false
    }

    /** Peek at the next character without consuming it. */
    private fun peekNext(): Char? = if (pos + 1 < source.length) source[pos + 1] else null
}
