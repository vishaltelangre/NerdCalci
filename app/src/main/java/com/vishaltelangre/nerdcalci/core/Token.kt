package com.vishaltelangre.nerdcalci.core

/** Every distinct lexeme kind produced by [Lexer]. */
enum class TokenKind {
    // Literals
    NUMBER,

    // Identifiers (variables, function names, keywords)
    IDENTIFIER,

    // Arithmetic operators
    PLUS, MINUS, STAR, SLASH, PERCENT, CARET,

    // Compound assignment operators
    PLUS_EQUALS, MINUS_EQUALS, STAR_EQUALS, SLASH_EQUALS, PERCENT_EQUALS,

    // Increment / decrement
    PLUS_PLUS, MINUS_MINUS,

    // Grouping & punctuation
    LPAREN, RPAREN, COMMA, EQUALS,

    // Keywords — resolved from IDENTIFIER by the Lexer
    KW_OF, KW_OFF,

    // End-of-input
    EOF
}

/**
 * A single token produced by the [Lexer].
 *
 * @property kind    the type of this token
 * @property lexeme  the raw source text that was scanned
 * @property value   pre-parsed numeric value (meaningful only when [kind] == [TokenKind.NUMBER])
 * @property position character offset in the source string (for error reporting)
 */
data class Token(
    val kind: TokenKind,
    val lexeme: String,
    val value: Double = 0.0,
    val position: Int = 0
)
