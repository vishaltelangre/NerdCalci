package com.vishaltelangre.nerdcalci.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun `single underscore tokenizes as Variable`() {
        val result = tokens("_")
        assertEquals(1, result.size)
        assertEquals(TokenType.Variable, result[0].type)
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
}
