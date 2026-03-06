package com.vishaltelangre.nerdcalci.utils

enum class TokenType {
    Number, Variable, Keyword, Operator, Percent, Comment, Function, Default
}

data class SyntaxToken(val start: Int, val end: Int, val type: TokenType)

object SyntaxUtils {

    /** Identifiers that are highlighted as keywords rather than variables. */
    private val KEYWORD_NAMES = setOf("sum", "total", "avg", "average")

    /**
     * Parses the given text and returns a sequence of syntax tokens indicating the type of each
     * segment. This parser can be reused across different rendering systems (Compose, Canvas, etc).
     */
    fun parseSyntaxTokens(text: String): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        var i = 0
        while (i < text.length) {
            val char = text[i]
            when {
                // Comments: everything from # to end of string
                char == '#' -> {
                    tokens.add(SyntaxToken(i, text.length, TokenType.Comment))
                    break
                }

                char.isDigit() || (char == '.' && i + 1 < text.length && text[i + 1].isDigit()) -> {
                    val start = i
                    while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
                    tokens.add(SyntaxToken(start, i, TokenType.Number))
                    continue
                }

                char.isLetter() || char == '_' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                    val word = text.substring(start, i)

                    // Peek ahead for Function call parsing
                    var j = i
                    while (j < text.length && text[j].isWhitespace()) j++
                    val isFunction = j < text.length && text[j] == '('

                    val type = if (word in KEYWORD_NAMES) {
                        TokenType.Keyword
                    } else if (isFunction) {
                        TokenType.Function
                    } else {
                        TokenType.Variable
                    }
                    tokens.add(SyntaxToken(start, i, type))
                    continue
                }

                char == '%' -> {
                    tokens.add(SyntaxToken(i, i + 1, TokenType.Percent))
                }
                char in "+-*/^()=×÷" -> {
                    tokens.add(SyntaxToken(i, i + 1, TokenType.Operator))
                }
                else -> {
                    tokens.add(SyntaxToken(i, i + 1, TokenType.Default))
                }
            }
            i++
        }
        return tokens
    }
}
