package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.Token
import com.vishaltelangre.nerdcalci.core.TokenKind
import com.vishaltelangre.nerdcalci.core.Lexer
import com.vishaltelangre.nerdcalci.core.UnitConverter
import com.vishaltelangre.nerdcalci.core.Unit
import com.vishaltelangre.nerdcalci.core.UnitCategory

enum class TokenType {
    Number, Variable, Keyword, Conversion, Operator, Percent, Comment, Function, StringLiteral, Default
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

                    val type = when {
                        word.lowercase() in setOf("to", "in", "as") -> TokenType.Conversion
                        word in KEYWORD_NAMES -> TokenType.Keyword
                        isFunction -> TokenType.Function
                        else -> TokenType.Variable
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
 * Matches the definition of an identifier in [Lexer].
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
            if (kwIndex >= 0) {
                var unit: Unit? = null
                var currentUnitStr = ""
                for (i in (kwIndex - 1) downTo 0) {
                    val t = cleanTokens[i]
                    if (t.kind == TokenKind.IDENTIFIER || t.kind == TokenKind.KW_OF || t.kind == TokenKind.KW_TO || t.kind == TokenKind.KW_IN || t.kind == TokenKind.KW_AS) {
                        currentUnitStr = if (currentUnitStr.isEmpty()) t.lexeme else "${t.lexeme} $currentUnitStr"
                        val found = UnitConverter.findUnit(currentUnitStr)
                        if (found != null) {
                            unit = found
                        }
                    } else {
                        break
                    }
                }

                val kwToken = cleanTokens[kwIndex]
                val endPos = kwToken.position + kwToken.lexeme.length
                var replaceStart = endPos
                while (replaceStart < beforeCursor.length && beforeCursor[replaceStart].isWhitespace()) {
                    replaceStart++
                }
                val currentWord = if (replaceStart < beforeCursor.length) {
                    beforeCursor.substring(replaceStart)
                } else ""
                val isExplicit = kwIndex == cleanTokens.size - 1
                val needsSpace = replaceStart == endPos && (replaceStart == beforeCursor.length || !beforeCursor[replaceStart].isWhitespace())
                return SuggestionContextInfo(currentWord, SuggestionType.UNIT, isExplicit, unit?.category, replaceStart = replaceStart, needsSpace = needsSpace)
            }

            // Check for convert() function arguments context (e.g. convert(10, "km", "m"))
            val convertContext = getConvertSuggestionContext(cleanTokens, beforeCursor.length)
            if (convertContext != null) {
                return convertContext
            }


            // Check if last tokens form a quantity (Number + [Unit])

            // 1. Full composite unit detection (e.g. "kilometers per hour ")
            val (compositeUnitStart, category) = detectCompositeUnit(beforeCursor)
            val lastToken = cleanTokens.last()
            
            if (compositeUnitStart != -1) {
                // If cursor is right after the unit and there's a space, suggest keywords
                if (beforeCursor.endsWith(" ")) {
                    return SuggestionContextInfo(
                        word = "",
                        type = SuggestionType.CONVERSION,
                        isExplicitTrigger = true,
                        unitStart = compositeUnitStart,
                        unitCategory = category,
                        replaceStart = cursorPos
                    )
                }

                // If typing an identifier after a unit (e.g. "10 kg t"), it's still a conversion context
                if (lastToken.kind == TokenKind.IDENTIFIER && lastToken.position > compositeUnitStart) {
                    return SuggestionContextInfo(
                        word = lastToken.lexeme,
                        type = SuggestionType.CONVERSION,
                        isExplicitTrigger = false,
                        unitStart = compositeUnitStart,
                        unitCategory = category,
                        replaceStart = lastToken.position
                    )
                }
            }

            val tokenBeforeLast = if (cleanTokens.size >= 2) cleanTokens[cleanTokens.size - 2] else null

            // 2. Typing unit after quantity (Number or Variable): "15 new" or "a new"
            if (lastToken.kind == TokenKind.IDENTIFIER && (tokenBeforeLast?.kind == TokenKind.NUMBER || tokenBeforeLast?.kind == TokenKind.IDENTIFIER)) {
                val found = UnitConverter.findUnit(lastToken.lexeme)
                return SuggestionContextInfo(
                    word = lastToken.lexeme,
                    type = SuggestionType.CONVERSION,
                    isExplicitTrigger = false,
                    unitStart = lastToken.position,
                    unitCategory = found?.category,
                    replaceStart = lastToken.position
                )
            }

            // 3. Simple quantity (Number or Variable) followed by space: "15 " or "a "
            if (lastToken.kind == TokenKind.NUMBER || lastToken.kind == TokenKind.IDENTIFIER) {
                if (beforeCursor.endsWith(" ")) {
                    return SuggestionContextInfo(
                        word = "",
                        type = SuggestionType.CONVERSION,
                        isExplicitTrigger = true,
                        unitStart = lastToken.position + lastToken.lexeme.length + 1,
                        replaceStart = cursorPos
                    )
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

private fun detectCompositeUnit(text: String): Pair<Int, UnitCategory?> {
    val tokens = try { Lexer(text).tokenize() } catch (e: Exception) { return -1 to null }
    val cleanTokens = tokens.filter { it.kind != TokenKind.EOF && it.kind != TokenKind.SEMICOLON }
    if (cleanTokens.isEmpty()) return -1 to null

    // Find the last token that is NOT an identifier or a known unit keyword.
    // This token is the boundary before the potential quantity (Number or Variable).
    val lastBoundaryIndex = cleanTokens.indexOfLast {
        it.kind != TokenKind.IDENTIFIER &&
        it.kind !in setOf(TokenKind.KW_OF, TokenKind.KW_TO, TokenKind.KW_IN, TokenKind.KW_AS)
    }

    // If the boundary token is a NUMBER, the unit starts right after it.
    // Otherwise, we assume the first token after the boundary is a Variable quantity, 
    // and the unit starts after that.
    val startTokenIndex = if (lastBoundaryIndex != -1 && cleanTokens[lastBoundaryIndex].kind == TokenKind.NUMBER) {
        lastBoundaryIndex + 1
    } else {
        lastBoundaryIndex + 2
    }

    if (startTokenIndex >= cleanTokens.size && !text.endsWith(" ")) return -1 to null

    // We look for the LONGEST matching unit starting after the number.
    var bestStart = -1
    var bestCategory: UnitCategory? = null
    var maxLength = -1
    for (i in startTokenIndex until cleanTokens.size) {
        var currentUnitStr = ""
        for (j in i until cleanTokens.size) {
            val t = cleanTokens[j]
            currentUnitStr = if (currentUnitStr.isEmpty()) t.lexeme else "$currentUnitStr ${t.lexeme}"
            val normalizedUnitStr = currentUnitStr.trim().lowercase()

            val unit = UnitConverter.findUnit(currentUnitStr)
            val prefixMatch = if (unit == null) {
                UnitConverter.UNITS.firstOrNull { candidate ->
                    candidate.symbols.any { symbol ->
                        symbol.lowercase().startsWith(normalizedUnitStr)
                    }
                }
            } else null

            val matchedUnit = unit ?: prefixMatch
            if (matchedUnit != null) {
                val matchLength = normalizedUnitStr.length
                val suffixFromStart = text.substring(cleanTokens[i].position).trim().lowercase()
                if (matchLength > maxLength && suffixFromStart.startsWith(normalizedUnitStr)) {
                    bestStart = cleanTokens[i].position
                    bestCategory = matchedUnit.category
                    maxLength = matchLength
                }
            }
        }
    }

    return bestStart to bestCategory
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
