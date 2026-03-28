package com.vishaltelangre.nerdcalci.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.vishaltelangre.nerdcalci.core.UnitCategory

class SyntaxUtilsTest {
    private fun tokens(text: String) = SyntaxUtils.parseSyntaxTokens(text)

    private fun assertSingleToken(text: String, expectedType: TokenType) {
        val result = tokens(text)
        assertEquals("Expected exactly one token for \"$text\"", 1, result.size)
        assertEquals(expectedType, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(text.length, result[0].end)
    }

    @Test
    fun `empty string produces no tokens`() {
        assertTrue(tokens("").isEmpty())
    }

    @Test
    fun `whitespace-only string produces Default tokens`() {
        val result = tokens("   ")
        assertTrue(result.all { it.type == TokenType.Default })
    }

    @Test
    fun `integer literal tokenizes as Number`() {
        assertSingleToken("42", TokenType.Number)
    }

    @Test
    fun `decimal literal tokenizes as Number`() {
        assertSingleToken("3.14", TokenType.Number)
    }

    @Test
    fun `leading-dot decimal tokenizes as Number`() {
        val result = tokens(".5")
        assertEquals(1, result.size)
        assertEquals(TokenType.Number, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(2, result[0].end)
    }

    @Test
    fun `lone dot is not a Number token`() {
        val result = tokens(".")
        assertTrue(result.none { it.type == TokenType.Number })
    }

    @Test
    fun `number token spans entire integer`() {
        val result = tokens("1234")
        assertEquals(1, result.size)
        assertEquals(TokenType.Number, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(4, result[0].end)
    }

    @Test
    fun `number token spans entire decimal`() {
        val result = tokens("99.99")
        assertEquals(1, result.size)
        assertEquals(TokenType.Number, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(5, result[0].end)
    }

    @Test
    fun `simple identifier tokenizes as Variable`() {
        assertSingleToken("foo", TokenType.Variable)
    }

    @Test
    fun `single letter tokenizes as Variable`() {
        assertSingleToken("x", TokenType.Variable)
    }

    @Test
    fun `identifier with digits tokenizes as Variable`() {
        assertSingleToken("myVar2", TokenType.Variable)
    }

    @Test
    fun `identifier with underscores tokenizes as Variable`() {
        assertSingleToken("my_var", TokenType.Variable)
    }

    @Test
    fun `identifier with trailing underscores tokenizes as Variable`() {
        assertSingleToken("value_", TokenType.Variable)
    }

    @Test
    fun `leading underscore tokenizes as single Variable token`() {
        val result = tokens("_foo")
        assertEquals(1, result.size)
        assertEquals(TokenType.Variable, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(4, result[0].end)
    }

    @Test
    fun `double leading underscore tokenizes as single Variable token`() {
        val result = tokens("__internal__")
        assertEquals(1, result.size)
        assertEquals(TokenType.Variable, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(12, result[0].end)
    }

    @Test
    fun `private2 style variable tokenizes as single Variable token`() {
        val result = tokens("_private2")
        assertEquals(1, result.size)
        assertEquals(TokenType.Variable, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(9, result[0].end)
    }

    @Test
    fun `single underscore tokenizes as Keyword`() {
        val result = tokens("_")
        assertEquals(1, result.size)
        assertEquals(TokenType.Keyword, result[0].type)
    }

    @Test
    fun `variable token spans correct range in expression`() {
        // "10 + foo" — Variable starts at 5
        val result = tokens("10 + foo")
        val varToken = result.find { it.type == TokenType.Variable }
        assertEquals(5, varToken?.start)
        assertEquals(8, varToken?.end)
    }

    @Test
    fun `plus operator tokenizes as Operator`() {
        val result = tokens("+")
        assertEquals(1, result.size)
        assertEquals(TokenType.Operator, result[0].type)
    }

    @Test
    fun `minus operator tokenizes as Operator`() {
        assertSingleToken("-", TokenType.Operator)
    }

    @Test
    fun `asterisk operator tokenizes as Operator`() {
        assertSingleToken("*", TokenType.Operator)
    }

    @Test
    fun `slash operator tokenizes as Operator`() {
        assertSingleToken("/", TokenType.Operator)
    }

    @Test
    fun `caret operator tokenizes as Operator`() {
        assertSingleToken("^", TokenType.Operator)
    }

    @Test
    fun `equals operator tokenizes as Operator`() {
        assertSingleToken("=", TokenType.Operator)
    }

    @Test
    fun `open paren tokenizes as Operator`() {
        assertSingleToken("(", TokenType.Operator)
    }

    @Test
    fun `close paren tokenizes as Operator`() {
        assertSingleToken(")", TokenType.Operator)
    }

    @Test
    fun `unicode multiplication sign tokenizes as Operator`() {
        assertSingleToken("×", TokenType.Operator)
    }

    @Test
    fun `unicode division sign tokenizes as Operator`() {
        assertSingleToken("÷", TokenType.Operator)
    }

    @Test
    fun `percent sign tokenizes as Percent`() {
        assertSingleToken("%", TokenType.Percent)
    }

    @Test
    fun `double quoted string tokenizes as StringLiteral`() {
        assertSingleToken("\"hello\"", TokenType.StringLiteral)
    }

    @Test
    fun `unclosed string tokenizes as StringLiteral`() {
        val result = tokens("\"hello")
        assertEquals(1, result.size)
        assertEquals(TokenType.StringLiteral, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(6, result[0].end)
    }

    @Test
    fun `string literal token spans entire quotes`() {
        val result = tokens("\"test string\"")
        assertEquals(1, result.size)
        assertEquals(TokenType.StringLiteral, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(13, result[0].end)
    }

    @Test
    fun `percent token in expression has correct span`() {
        // "20%" — Percent is at index 2
        val result = tokens("20%")
        val pct = result.find { it.type == TokenType.Percent }
        assertEquals(2, pct?.start)
        assertEquals(3, pct?.end)
    }

    @Test
    fun `hash at start tokenizes full string as Comment`() {
        val text = "# this is a comment"
        val result = tokens(text)
        assertEquals(1, result.size)
        assertEquals(TokenType.Comment, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(text.length, result[0].end)
    }

    @Test
    fun `inline comment starts at hash and extends to end`() {
        val text = "10 + 5 # inline"
        val result = tokens(text)
        val comment = result.find { it.type == TokenType.Comment }
        assertEquals(7, comment?.start)
        assertEquals(text.length, comment?.end)
    }

    @Test
    fun `no tokens produced after comment`() {
        val result = tokens("# comment")
        assertEquals(1, result.size)
        assertEquals(TokenType.Comment, result[0].type)
    }

    @Test
    fun `hash-only string is a single Comment token`() {
        val result = tokens("#")
        assertEquals(1, result.size)
        assertEquals(TokenType.Comment, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(1, result[0].end)
    }

    @Test
    fun `space character tokenizes as Default`() {
        val result = tokens(" ")
        assertEquals(1, result.size)
        assertEquals(TokenType.Default, result[0].type)
    }

    @Test
    fun `unknown special char tokenizes as Default`() {
        val result = tokens("@")
        assertEquals(1, result.size)
        assertEquals(TokenType.Default, result[0].type)
    }

    @Test
    fun `simple addition expression produces correct token sequence`() {
        // "2 + 3" → Number, Default(space), Operator, Default(space), Number
        val result = tokens("2 + 3")
        val types = result.map { it.type }
        assertEquals(
            listOf(TokenType.Number, TokenType.Default, TokenType.Operator, TokenType.Default, TokenType.Number),
            types
        )
    }

    @Test
    fun `assignment expression produces Variable Operator Number`() {
        // "x = 10" → Variable, Default, Operator, Default, Number
        val result = tokens("x = 10")
        val types = result.map { it.type }
        assertEquals(
            listOf(TokenType.Variable, TokenType.Default, TokenType.Operator, TokenType.Default, TokenType.Number),
            types
        )
    }

    @Test
    fun `percentage expression produces correct token types`() {
        // "20% of 100" → Number, Percent, Default, Variable(of), Default, Number
        val result = tokens("20% of 100")
        assertEquals(TokenType.Number, result[0].type)
        assertEquals(TokenType.Percent, result[1].type)
        assertEquals(TokenType.Number, result.last().type)
    }

    @Test
    fun `expression with comment strips trailing part`() {
        val result = tokens("5 + 5 # note")
        val comment = result.find { it.type == TokenType.Comment }!!
        // Nothing after the comment token
        assertTrue(result.indexOf(comment) == result.size - 1)
    }

    @Test
    fun `decimal number followed by operator`() {
        // "3.14 * 2" → Number, Default, Operator, Default, Number
        val result = tokens("3.14 * 2")
        assertEquals(TokenType.Number, result[0].type)
        assertEquals(4, result[0].end)
    }

    @Test
    fun `underscore variable in assignment has correct span`() {
        // "_val = 7" → Variable(0..4), spaces & operator, Number
        val result = tokens("_val = 7")
        val varToken = result[0]
        assertEquals(TokenType.Variable, varToken.type)
        assertEquals(0, varToken.start)
        assertEquals(4, varToken.end)
    }

    @Test
    fun `double underscore variable in full expression`() {
        // "__x + 1"
        val result = tokens("__x + 1")
        assertEquals(TokenType.Variable, result[0].type)
        assertEquals(0, result[0].start)
        assertEquals(3, result[0].end)
    }

    @Test
    fun `consecutive operators each get their own token`() {
        val result = tokens("+-*/^")
        assertTrue(result.all { it.type == TokenType.Operator })
        assertEquals(5, result.size)
    }

    @Test
    fun `all token types appear in complex expression`() {
        // "price = 100 + 10% # tax"
        val result = tokens("price = 100 + 10% # tax")
        val types = result.map { it.type }.toSet()
        assertTrue(types.contains(TokenType.Variable))
        assertTrue(types.contains(TokenType.Number))
        assertTrue(types.contains(TokenType.Operator))
        assertTrue(types.contains(TokenType.Percent))
        assertTrue(types.contains(TokenType.Comment))
    }

    @Test
    fun `token ranges are contiguous and non-overlapping`() {
        val text = "_total = 200 + 50%"
        val result = tokens(text)
        var expectedStart = 0
        for (token in result) {
            assertEquals("Token starts where previous ended", expectedStart, token.start)
            assertTrue("Token end > start", token.end > token.start)
            expectedStart = token.end
        }
        assertEquals("Last token reaches end of string", text.length, expectedStart)
    }

    @Test
    fun `total keyword tokenizes as Keyword`() {
        assertSingleToken("total", TokenType.Keyword)
    }

    @Test
    fun `sum keyword tokenizes as Keyword`() {
        assertSingleToken("sum", TokenType.Keyword)
    }

    @Test
    fun `non-keyword starting with sum tokenizes as Variable`() {
        assertSingleToken("summary", TokenType.Variable)
    }

    @Test
    fun `avg keyword tokenizes as Keyword`() {
        assertSingleToken("avg", TokenType.Keyword)
    }

    @Test
    fun `average keyword tokenizes as Keyword`() {
        assertSingleToken("average", TokenType.Keyword)
    }

    @Test
    fun `last keyword tokenizes as Keyword`() {
        assertSingleToken("last", TokenType.Keyword)
    }

    @Test
    fun `prev keyword tokenizes as Keyword`() {
        assertSingleToken("prev", TokenType.Keyword)
    }

    @Test
    fun `previous keyword tokenizes as Keyword`() {
        assertSingleToken("previous", TokenType.Keyword)
    }

    @Test
    fun `above keyword tokenizes as Keyword`() {
        assertSingleToken("above", TokenType.Keyword)
    }

    @Test
    fun `lineno keyword tokenizes as Keyword`() {
        assertSingleToken("lineno", TokenType.Keyword)
    }

    @Test
    fun `linenumber keyword tokenizes as Keyword`() {
        assertSingleToken("linenumber", TokenType.Keyword)
    }

    @Test
    fun `currentLineNumber keyword tokenizes as Keyword`() {
        assertSingleToken("currentLineNumber", TokenType.Keyword)
    }

    @Test
    fun `identifier followed by parenthesis tokenizes as Function`() {
        val result = tokens("f(")
        assertEquals(TokenType.Function, result[0].type)
    }

    @Test
    fun `identifier followed by spaces and parenthesis tokenizes as Function`() {
        val result = tokens("myFunc   (")
        assertEquals(TokenType.Function, result[0].type)
    }

    @Test
    fun `function token spans only the identifier name`() {
        val result = tokens("calculate(10)")
        val funcToken = result[0]
        assertEquals(TokenType.Function, funcToken.type)
        assertEquals(0, funcToken.start)
        assertEquals(9, funcToken.end)
    }

    @Test
    fun `keyword has precedence over function parsing`() {
        // "sum(" should still parse "sum" as a Keyword, not a Function
        val result = tokens("sum(1, 2)")
        assertEquals(TokenType.Keyword, result[0].type)
    }

    @Test
    fun `getIdentifierRangeAt finds full word from middle`() {
        val text = "  log(10)  "
        // Cursor at 'o' (index 3)
        val range = text.getIdentifierRangeAt(3)
        assertEquals(2 until 5, range)
        assertEquals("log", text.substring(range))
    }

    @Test
    fun `getIdentifierRangeAt finds word from start`() {
        val text = "log(10)"
        val range = text.getIdentifierRangeAt(0)
        assertEquals(0 until 3, range)
    }

    @Test
    fun `getIdentifierRangeAt finds word from end bound`() {
        val text = "log(10)"
        // index 3 is '(', which is not part of identifier but we should find 'log' if index is 3
        val range = text.getIdentifierRangeAt(3)
        assertEquals(0 until 3, range)
    }

    @Test
    fun `getIdentifierRangeAt handles empty or non-word correctly`() {
        val text = " + "
        val range = text.getIdentifierRangeAt(1)
        assertEquals(1 until 1, range)
    }

    @Test
    fun `findClosingParenthesis matches simple parens`() {
        val text = "log(10)"
        assertEquals(6, text.findClosingParenthesis(3))
    }

    @Test
    fun `findClosingParenthesis handles nested parens`() {
        val text = "pow(2, log(100))"
        assertEquals(15, text.findClosingParenthesis(3))
        assertEquals(14, text.findClosingParenthesis(10))
    }

    @Test
    fun `calculateFuzzyMatch finds exact matches`() {
        val result = "abc".calculateFuzzyMatch("abc")
        assertTrue(result != null)
        // 1000 (exact) + 100 (first char) + 70 (consecutive) - 3 (length) = 1167
        assertEquals(1167, result?.score)
    }

    @Test
    fun `calculateFuzzyMatch finds subsequence matches`() {
        val result = "lineno".calculateFuzzyMatch("ln")
        assertTrue(result != null)
        // 'l' at 0, 'n' at 2
        assertEquals(listOf(0, 2), result?.matchIndices)
    }

    @Test
    fun `calculateFuzzyMatch respects word boundaries`() {
        val target = "currentLineNumber"
        val result = target.calculateFuzzyMatch("cln")
        assertTrue(result != null)
        // 'c' at 0 (+100 first char)
        // 'L' at 7 (+50 boundary)
        // 'n' at 9 (NOT a boundary, greedily matched before 'N' at 11)
        // score = 100 + 50 - 17 = 133
        assertEquals(133, result?.score)
    }

    @Test
    fun `calculateFuzzyMatch returns null if no match`() {
        val result = "abc".calculateFuzzyMatch("xyz")
        assertTrue(result == null)
    }

    @Test
    fun `calculateFuzzyMatch applies type-based bonuses`() {
        val query = "s"

        // Local variable score
        val varScore = "split".calculateFuzzyMatch(query, SuggestionType.VARIABLE)?.score ?: 0
        // Global function score
        val funcScore = "sin".calculateFuzzyMatch(query, SuggestionType.GLOBAL_FUNCTION)?.score ?: 0

        // "split" is longer than "sin", so without bonus "sin" would win.
        // But with +200 bonus, "split" should win.
        assertTrue("Variable 'split' ($varScore) should outrank Function 'sin' ($funcScore)", varScore > funcScore)

        // Dynamic variable vs Global function
        val dynamicScore = "sum".calculateFuzzyMatch(query, SuggestionType.DYNAMIC_VARIABLE)?.score ?: 0
        assertTrue("Dynamic 'sum' ($dynamicScore) should outrank Global 'sin' ($funcScore)", dynamicScore > funcScore)
        assertTrue("Variable 'split' ($varScore) should outrank Dynamic 'sum' ($dynamicScore)", varScore > dynamicScore)
    }

    @Test
    fun `getSuggestionContext populates unitStart for quantity with space`() {
        val beforeCursor = "15 kilometers "
        val result = getSuggestionContext(beforeCursor, beforeCursor, beforeCursor.length, emptyMap())
        assertEquals(SuggestionType.KEYWORD, result.type)
        assertEquals(3, result.unitStart)
    }

    @Test
    fun `getSuggestionContext handles conversion keyword after unit`() {
        val beforeCursor = "15 kmph in "
        val result = getSuggestionContext(beforeCursor, beforeCursor, beforeCursor.length, emptyMap())
        assertEquals(SuggestionType.UNIT, result.type)
        assertEquals(UnitCategory.SPEED, result.unitCategory)
        assertEquals(11, result.replaceStart)
    }

    @Test
    fun `getSuggestionContext handles conversion keyword after long unit name`() {
        val beforeCursor = "15 kilometers per hour in "
        val result = getSuggestionContext(beforeCursor, beforeCursor, beforeCursor.length, emptyMap())
        assertEquals(SuggestionType.UNIT, result.type)
        assertEquals(UnitCategory.SPEED, result.unitCategory)
        assertEquals(26, result.replaceStart)
    }

    @Test
    fun `getSuggestionContext sets needsSpace when space is missing after keyword`() {
        val beforeCursor = "15 kmph to"
        val result = getSuggestionContext(beforeCursor, beforeCursor, beforeCursor.length, emptyMap())
        assertEquals(SuggestionType.UNIT, result.type)
        assertEquals(UnitCategory.SPEED, result.unitCategory)
        assertEquals(true, result.needsSpace)
    }

    @Test
    fun `getSuggestionContext does not set needsSpace when space is present after keyword`() {
        val beforeCursor = "15 kmph to "
        val result = getSuggestionContext(beforeCursor, beforeCursor, beforeCursor.length, emptyMap())
        assertEquals(SuggestionType.UNIT, result.type)
        assertEquals(UnitCategory.SPEED, result.unitCategory)
        assertEquals(false, result.needsSpace)
    }
}
