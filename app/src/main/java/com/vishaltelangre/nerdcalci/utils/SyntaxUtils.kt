package com.vishaltelangre.nerdcalci.utils

enum class TokenType {
    Number, Variable, Keyword, Operator, Percent, Comment, Function, Default
}

data class SyntaxToken(val start: Int, val end: Int, val type: TokenType)

object SyntaxUtils {

    /** Identifiers that are highlighted as keywords rather than variables. */
    private val KEYWORD_NAMES = setOf(
        "sum", "total", "avg", "average", "last", "prev", "previous", "above", "_",
        "lineno", "linenumber", "currentLineNumber"
    )

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
/**
 * Returns the range of the identifier at the given index.
 * Matches the definition of an identifier in [com.vishaltelangre.nerdcalci.core.Lexer].
 */
fun String.getIdentifierRangeAt(index: Int): IntRange {
    var start = index
    // Walk back until we hit a non-identifier character
    while (start > 0 && (this[start - 1].isLetterOrDigit() || this[start - 1] == '_')) {
        start--
    }

    var end = index
    // Walk forward until we hit a non-identifier character
    while (end < this.length && (this[end].isLetterOrDigit() || this[end] == '_')) {
        end++
    }
    return start until end
}

/**
 * If the character at [index] is '(', find the matching ')'.
 * Returns the index of ')' or the last character index if no match is found.
 */
fun String.findClosingParenthesis(index: Int): Int {
    if (index >= length || this[index] != '(') return index
    var balance = 0
    for (i in index until length) {
        if (this[i] == '(') balance++
        else if (this[i] == ')') balance--
        if (balance == 0) return i
    }
    return length - 1
}

/**
 * Result of a fuzzy match calculation.
 */
data class FuzzyMatch(
    val score: Int,
    val matchIndices: List<Int>
)

/**
 * Calculates a fuzzy match score and matched indices for a given query.
 * Returns null if the query is not a subsequence of the target.
 */
fun String.calculateFuzzyMatch(query: String): FuzzyMatch? {
    if (query.isEmpty()) return FuzzyMatch(0, emptyList())

    val queryLower = query.lowercase()
    val targetLower = this.lowercase()

    val matchIndices = mutableListOf<Int>()
    var queryIdx = 0
    var targetIdx = 0

    while (queryIdx < queryLower.length && targetIdx < targetLower.length) {
        if (queryLower[queryIdx] == targetLower[targetIdx]) {
            matchIndices.add(targetIdx)
            queryIdx++
        }
        targetIdx++
    }

    if (queryIdx != queryLower.length) return null

    // Scoring logic
    var score = 0

    // Exact match bonus
    if (queryLower == targetLower) score += 1000
    // Prefix match bonus
    else if (targetLower.startsWith(queryLower)) score += 500

    // First character match bonus
    if (matchIndices.isNotEmpty() && matchIndices.first() == 0) score += 100

    // Consecutive characters bonus
    var consecutiveCount = 0
    for (i in 1 until matchIndices.size) {
        if (matchIndices[i] == matchIndices[i - 1] + 1) {
            consecutiveCount++
            score += 20 + (consecutiveCount * 10)
        } else {
            consecutiveCount = 0
        }
    }

    // Word boundary bonuses (camelCase, snake_case, etc)
    for (idx in matchIndices) {
        if (idx > 0) {
            val prevChar = this[idx - 1]
            val currChar = this[idx]
            if (prevChar == '_' || prevChar == ' ' || (prevChar.isLowerCase() && currChar.isUpperCase())) {
                score += 50
            }
        }
    }

    // Shorter targets are better if everything else is equal
    score -= this.length

    return FuzzyMatch(score, matchIndices)
}
