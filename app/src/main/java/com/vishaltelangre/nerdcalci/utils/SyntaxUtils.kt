package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.Token
import com.vishaltelangre.nerdcalci.core.TokenKind
import com.vishaltelangre.nerdcalci.core.Lexer
import com.vishaltelangre.nerdcalci.core.UnitConverter

enum class TokenType {
    Number, Variable, Keyword, Operator, Percent, Comment, Function, StringLiteral, Default
}

data class SyntaxToken(val start: Int, val end: Int, val type: TokenType)

object SyntaxUtils {

    /** Identifiers that are highlighted as keywords rather than variables. */
    private val KEYWORD_NAMES = setOf(
        "sum", "total", "avg", "average", "last", "prev", "previous", "above", "_",
        "lineno", "linenumber", "currentLineNumber"
    )

    /**
     * Parses the text into syntax tokens.
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

                char == '"' -> {
                    val start = i
                    i++ // consume open "
                    while (i < text.length && text[i] != '"') i++
                    if (i < text.length) i++ // consume close "
                    tokens.add(SyntaxToken(start, i, TokenType.StringLiteral))
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

fun getSuggestionContext(
    beforeCursor: String,
    text: String,
    cursorPos: Int,
    fileVariables: Map<String, String>
): SuggestionContextInfo {
    if (cursorPos > 0) {
        // Check for comment context (inline instructions)
        val hashIndex = beforeCursor.indexOf('#')
        if (hashIndex >= 0) {
            return SuggestionContextInfo("", SuggestionType.VARIABLE, false)
        }

        // Check for file path suggestions (inside file("..."))
        val fileRegex = Regex("""file\(\s*"([^"]*)$""")
        val fileMatch = fileRegex.find(beforeCursor)
        if (fileMatch != null) {
            return SuggestionContextInfo(fileMatch.groupValues[1], SuggestionType.FILE, true)
        }

        // Check for dot notation suggestions (e.g. file("").)
        val dotRegex = Regex("""(\w+|\bfile\(\s*"[^"]*"\s*\))\s*\.\s*(\w*)$""")
        val dotMatch = dotRegex.find(beforeCursor)
        if (dotMatch != null) {
            val objectName = dotMatch.groupValues[1]
            val linkedFile = if (objectName.startsWith("file(")) {
                Regex("""file\(\s*"([^"]+)"\s*\)""").find(objectName)?.groupValues?.getOrNull(1)
            } else {
                fileVariables[objectName]
            }
            if (linkedFile != null) {
                return SuggestionContextInfo(dotMatch.groupValues[2], SuggestionType.VARIABLE, true)
            }
        }

        // Explicit trigger on keywords (to, in, as) for unit conversions
        val tokens = runCatching {
            Lexer(beforeCursor).tokenize()
        }.getOrElse {
            if (beforeCursor.count { it == '"' } % 2 != 0) {
                runCatching {
                    Lexer(beforeCursor + "\"").tokenize()
                }.getOrElse { emptyList() }
            } else {
                emptyList()
            }
        }
        val cleanTokens = tokens.filter { it.kind != TokenKind.EOF }
        if (cleanTokens.isNotEmpty()) {
            val kwIndex = cleanTokens.indexOfLast {
                it.kind in listOf(
                    TokenKind.KW_TO,
                    TokenKind.KW_IN,
                    TokenKind.KW_AS
                )
            }
            if (kwIndex >= 1) {
                val prevToken = cleanTokens[kwIndex - 1]
                val unit = if (prevToken.kind == TokenKind.IDENTIFIER) UnitConverter.findUnit(prevToken.lexeme) else null
                val isValidSource = true

                if (isValidSource) {
                    val kwToken = cleanTokens[kwIndex]
                    val unitStartPos = kwToken.position + kwToken.lexeme.length + 1
                    val currentWord = if (unitStartPos < beforeCursor.length) {
                        beforeCursor.substring(unitStartPos)
                    } else ""
                    val isExplicit = kwIndex == cleanTokens.size - 1
                    return SuggestionContextInfo(currentWord, SuggestionType.UNIT, isExplicit, unit?.category, replaceStart = unitStartPos)
                }
            }

            // Check for convert() function arguments context (e.g. convert(10, "km", "m"))
            val convertContext = getConvertSuggestionContext(cleanTokens, beforeCursor.length)
            if (convertContext != null) {
                return convertContext
            }

            val lastToken = cleanTokens.last()
            val prevToken = cleanTokens.getOrNull(cleanTokens.size - 2)
            val prevPrevToken = cleanTokens.getOrNull(cleanTokens.size - 3)

            if (lastToken.kind == TokenKind.IDENTIFIER &&
                prevToken?.kind == TokenKind.IDENTIFIER) {
                if (prevPrevToken?.kind == TokenKind.NUMBER &&
                    UnitConverter.findUnit(prevToken.lexeme) != null) {
                    return SuggestionContextInfo(lastToken.lexeme, SuggestionType.KEYWORD, true)
                }
            }
        }

        val range = text.getIdentifierRangeAt(cursorPos - 1)
        if (cursorPos <= range.last + 1) {
            var word = text.substring(range.first, cursorPos)
            var i = 0
            while (i < word.length && word[i].isDigit()) {
                i++
            }
            if (i > 0 && i < word.length) {
                word = word.substring(i)
            }
            return SuggestionContextInfo(word, SuggestionType.VARIABLE, false, replaceStart = range.first + i)
        }
    }
    return SuggestionContextInfo("", SuggestionType.VARIABLE, false)
}

/**
 * Resolves advice suggestions for the `convert()` function arguments list.
 * e.g., convert(value, "fromUnit", "toUnit")
 *
 * @param cleanTokens Sanitized Token stream up to cursor triggers layout.
 */
private fun getConvertSuggestionContext(cleanTokens: List<Token>, cursorPos: Int): SuggestionContextInfo? {
    var scanIdx = cleanTokens.size - 1
    var parenBalance = 0
    var commaCount = 0
    var fromUnitStr = ""

    while (scanIdx >= 0) {
        val t = cleanTokens[scanIdx]
        if (t.kind == TokenKind.RPAREN) {
            parenBalance++
        } else if (t.kind == TokenKind.LPAREN) {
            if (parenBalance > 0) {
                parenBalance--
            } else {
                if (scanIdx > 0) {
                    val funcName = cleanTokens[scanIdx - 1]
                    if (funcName.kind == TokenKind.IDENTIFIER && funcName.lexeme == "convert") {
                        val lastT = cleanTokens.last()
                        val currentWord = if (lastT.kind == TokenKind.STRING_LITERAL) {
                            lastT.lexeme
                        } else if (lastT.kind == TokenKind.IDENTIFIER) {
                            lastT.lexeme
                        } else ""

                        val replaceStart = when (lastT.kind) {
                            TokenKind.STRING_LITERAL -> lastT.position + 1
                            TokenKind.IDENTIFIER -> lastT.position
                            else -> cursorPos
                        }

                        if (commaCount == 1) { // 2nd arg: From Unit
                            return SuggestionContextInfo(currentWord, SuggestionType.UNIT, true, unitCategory = null, replaceStart = replaceStart, argumentIndex = 2)
                        } else if (commaCount == 2) { // 3rd arg: To Unit
                            // Resolve the From Unit's category by reading backwards capture
                            val unit = UnitConverter.findUnit(fromUnitStr.trim('"'))
                            return SuggestionContextInfo(currentWord, SuggestionType.UNIT, true, unitCategory = unit?.category, replaceStart = replaceStart, argumentIndex = 3)
                        }
                    }
                }
                break
            }
        } else if (t.kind == TokenKind.COMMA) {
            if (parenBalance == 0) {
                commaCount++
            }
        } else if (parenBalance == 0 && commaCount == 1 && (t.kind == TokenKind.STRING_LITERAL || t.kind == TokenKind.IDENTIFIER)) {
            // Traverse backward and save the 2nd argument!
            fromUnitStr = t.lexeme
        }
        scanIdx--
    }
    return null
}

/**
 * Result of a fuzzy match calculation.
 */
data class FuzzyMatch(
    val score: Int,
    val matchIndices: List<Int>
)

/**
 * Calculates a fuzzy match score and matched indices using common heuristics.
 *
 * Scoring Rules:
 * - Exact matches get the highest bonus (+1000).
 * - Prefix matches get a significant bonus (+500).
 * - Starting character match bonus (+100).
 * - Consecutive characters get exponentially increasing bonuses (20 + 10 * consecutiveCount)
 * - Matches on word boundaries (snake_case, camelCase) get a bonus (+50).
 * - Shorter overall strings are prioritized.
 * - Type-based bonuses are applied to favor local variables/functions.
 *
 * Returns null if the query is not a subsequence of the target.
 */
fun String.calculateFuzzyMatch(query: String, type: SuggestionType? = null): FuzzyMatch? {
    if (query.isEmpty()) {
        var score = 0
        if (type != null) {
            score += when (type) {
                SuggestionType.VARIABLE, SuggestionType.LOCAL_FUNCTION -> 200
                SuggestionType.DYNAMIC_VARIABLE -> 100
                else -> 0
            }
        }
        score -= this.length
        return FuzzyMatch(score, emptyList())
    }

    val queryLower = query.lowercase()
    val targetLower = this.lowercase()

    // Find all characters of the query in the target string in order
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

    // If we didn't find all characters, it's not a match
    if (queryIdx != queryLower.length) return null

    var score = 0

    // Exact matches are top priority
    if (queryLower == targetLower) score += 1000
    // Prefix matches are high priority
    else if (targetLower.startsWith(queryLower)) score += 500

    // Bonus for matching the very first character of the target
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

    // Word boundary bonuses. Helps matching snake_case or camelCase variables
    for (idx in matchIndices) {
        if (idx > 0) {
            val prevChar = this[idx - 1]
            val currChar = this[idx]
            // Boundary if preceded by underscore, space, or if it transitions from lower to upper case
            if (prevChar == '_' || prevChar == ' ' || (prevChar.isLowerCase() && currChar.isUpperCase())) {
                score += 50
            }
        }
    }

    // Shorter targets are better when multiple targets have the same match quality
    score -= this.length

    // Apply prioritization bonus based on type
    if (type != null) {
        score += when (type) {
            SuggestionType.VARIABLE, SuggestionType.LOCAL_FUNCTION -> 200
            SuggestionType.DYNAMIC_VARIABLE -> 100
            else -> 0
        }
    }

    return FuzzyMatch(score, matchIndices)
}
