package com.vishaltelangre.nerdcalci.core

/** Every distinct lexeme kind produced by [Lexer]. */
enum class TokenKind(val display: String) {
    // Literals
    NUMBER("number"),

    // Identifiers (variables, function names, keywords)
    IDENTIFIER("identifier"),

    // Arithmetic operators
    PLUS("+"), MINUS("-"), STAR("*"), SLASH("/"), PERCENT("%"), CARET("^"),

    // Compound assignment operators
    PLUS_EQUALS("+="), MINUS_EQUALS("-="), STAR_EQUALS("*="), SLASH_EQUALS("/="), PERCENT_EQUALS("%="),

    // Increment / decrement
    PLUS_PLUS("++"), MINUS_MINUS("--"),

    // Grouping & punctuation
    LPAREN("("), RPAREN(")"), COMMA(","), EQUALS("="), SEMICOLON(";"),

    // Keywords — resolved from IDENTIFIER by the Lexer
    KW_OF("of"), KW_OFF("off"),
    KW_LAST("last"), KW_PREV("prev"), KW_PREVIOUS("previous"), KW_ABOVE("above"),
    KW_UNDERSCORE("_"),

    // End-of-input
    EOF("end of line");

    val isPreviousLineAlias: Boolean
        get() = this in PREVIOUS_LINE_ALIAS_KINDS
}

private val PREVIOUS_LINE_ALIAS_KINDS = setOf(
    TokenKind.KW_LAST,
    TokenKind.KW_PREV,
    TokenKind.KW_PREVIOUS,
    TokenKind.KW_ABOVE,
    TokenKind.KW_UNDERSCORE
)

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
