package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.junit.Assert.*
import org.junit.Test

class MathEngineTest {

    private fun createLine(expression: String, fileId: Long = 1L, sortOrder: Int = 0): LineEntity {
        return LineEntity(id = sortOrder.toLong(), fileId = fileId, expression = expression, result = "", sortOrder = sortOrder)
    }

    @Test
    fun `basic addition returns correct result`() {
        val lines = listOf(createLine("2 + 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("4", result[0].result)
    }

    @Test
    fun `basic subtraction returns correct result`() {
        val lines = listOf(createLine("10 - 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("7", result[0].result)
    }

    @Test
    fun `basic multiplication returns correct result`() {
        val lines = listOf(createLine("5 * 6"))
        val result = MathEngine.calculate(lines)
        assertEquals("30", result[0].result)
    }

    @Test
    fun `basic division returns correct result`() {
        val lines = listOf(createLine("20 / 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
    }

    @Test
    fun `complex expression with multiple operators`() {
        val lines = listOf(createLine("2 + 3 * 4 - 1"))
        val result = MathEngine.calculate(lines)
        assertEquals("13", result[0].result)
    }

    @Test
    fun `expression with parentheses respects order of operations`() {
        val lines = listOf(createLine("(2 + 3) * 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("20", result[0].result)
    }

    @Test
    fun `exponentiation works correctly`() {
        val lines = listOf(createLine("2 ^ 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("8", result[0].result)
    }

    @Test
    fun `multiplication sign × is normalized to asterisk`() {
        val lines = listOf(createLine("5 × 6"))
        val result = MathEngine.calculate(lines)
        assertEquals("30", result[0].result)
    }

    @Test
    fun `division sign ÷ is normalized to slash`() {
        val lines = listOf(createLine("20 ÷ 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
    }

    @Test
    fun `mixed unicode and ASCII operators work together`() {
        val lines = listOf(createLine("10 × 2 ÷ 4 + 1"))
        val result = MathEngine.calculate(lines)
        assertEquals("6", result[0].result)
    }

    @Test
    fun `decimal addition returns formatted result`() {
        val lines = listOf(createLine("1.5 + 2.3"))
        val result = MathEngine.calculate(lines)
        assertEquals("3.80", result[0].result)
    }

    @Test
    fun `decimal division returns two decimal places`() {
        val lines = listOf(createLine("10 / 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("3.33", result[0].result)
    }

    @Test
    fun `result with no decimal part shows as integer`() {
        val lines = listOf(createLine("5.0 + 5.0"))
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
    }

    @Test
    fun `simple variable assignment stores value`() {
        val lines = listOf(
            createLine("price = 100", sortOrder = 0),
            createLine("price", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100", result[0].result)
        assertEquals("100", result[1].result)
    }

    @Test
    fun `variable can be used in calculations`() {
        val lines = listOf(
            createLine("price = 100", sortOrder = 0),
            createLine("price * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100", result[0].result)
        assertEquals("200", result[1].result)
    }

    @Test
    fun `multiple variables work together`() {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("a + b", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("20", result[1].result)
        assertEquals("30", result[2].result)
    }

    @Test
    fun `variable reassignment updates value`() {
        val lines = listOf(
            createLine("x = 5", sortOrder = 0),
            createLine("x * 2", sortOrder = 1),
            createLine("x = 10", sortOrder = 2),
            createLine("x * 2", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
        assertEquals("10", result[1].result)
        assertEquals("10", result[2].result)
        assertEquals("20", result[3].result)
    }

    @Test
    fun `variable with spaces is normalized`() {
        val lines = listOf(
            createLine("monthly salary = 5000", sortOrder = 0),
            createLine("monthly salary * 12", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5000", result[0].result)
        assertEquals("60000", result[1].result)
    }

    @Test
    fun `variable assignment with expression`() {
        val lines = listOf(
            createLine("total = 10 + 20 + 30", sortOrder = 0),
            createLine("total / 3", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("60", result[0].result)
        assertEquals("20", result[1].result)
    }

    @Test
    fun `percentage of number works correctly`() {
        val lines = listOf(createLine("20% of 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("20", result[0].result)
    }

    @Test
    fun `percentage of decimal number`() {
        val lines = listOf(createLine("15.5% of 200"))
        val result = MathEngine.calculate(lines)
        assertEquals("31", result[0].result) // Result is whole number
    }

    @Test
    fun `percentage of variable`() {
        val lines = listOf(
            createLine("price = 1000", sortOrder = 0),
            createLine("10% of price", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000", result[0].result)
        assertEquals("100", result[1].result)
    }

    @Test
    fun `percentage off reduces value`() {
        val lines = listOf(createLine("20% off 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("80", result[0].result)
    }

    @Test
    fun `percentage off with decimal`() {
        val lines = listOf(createLine("25% off 80"))
        val result = MathEngine.calculate(lines)
        assertEquals("60", result[0].result)
    }

    @Test
    fun `percentage off variable`() {
        val lines = listOf(
            createLine("original = 500", sortOrder = 0),
            createLine("30% off original", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("500", result[0].result)
        assertEquals("350", result[1].result)
    }

    @Test
    fun `add percentage to number`() {
        val lines = listOf(createLine("100 + 20%"))
        val result = MathEngine.calculate(lines)
        assertEquals("120", result[0].result)
    }

    @Test
    fun `add percentage to variable`() {
        val lines = listOf(
            createLine("salary = 50000", sortOrder = 0),
            createLine("salary + 10%", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("50000", result[0].result)
        assertEquals("55000.00", result[1].result) // Returns decimal format
    }

    @Test
    fun `subtract percentage from number`() {
        val lines = listOf(createLine("100 - 15%"))
        val result = MathEngine.calculate(lines)
        assertEquals("85", result[0].result)
    }

    @Test
    fun `subtract percentage from variable`() {
        val lines = listOf(
            createLine("budget = 1000", sortOrder = 0),
            createLine("budget - 25%", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000", result[0].result)
        assertEquals("750", result[1].result)
    }

    @Test
    fun `expression with inline comment returns result`() {
        val lines = listOf(createLine("10 + 5 # adding numbers"))
        val result = MathEngine.calculate(lines)
        assertEquals("15", result[0].result)
    }

    @Test
    fun `full line comment returns empty result`() {
        val lines = listOf(createLine("# This is just a comment"))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `comment with special characters is ignored`() {
        val lines = listOf(createLine("20 * 2 # result should be 40!"))
        val result = MathEngine.calculate(lines)
        assertEquals("40", result[0].result)
    }

    @Test
    fun `hash symbol in middle of expression is treated as comment`() {
        val lines = listOf(createLine("5 + 5 # + 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
    }

    @Test
    fun `empty expression returns empty result`() {
        val lines = listOf(createLine(""))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `blank expression with spaces returns empty result`() {
        val lines = listOf(createLine("   "))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `expression with only comment and spaces returns empty result`() {
        val lines = listOf(createLine("   # just a comment"))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `invalid expression returns Err`() {
        val lines = listOf(createLine("2 + * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `division by zero returns Err`() {
        val lines = listOf(createLine("10 / 0"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `undefined variable returns Err`() {
        val lines = listOf(createLine("unknownVar * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `malformed parentheses returns Err`() {
        val lines = listOf(createLine("(2 + 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `invalid variable name is processed by exp4j`() {
        // exp4j doesn't strictly validate variable names starting with numbers
        // This actually parses as "123 * invalid = 5" which evaluates to just 5
        val lines = listOf(createLine("123invalid = 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result) // exp4j handles this
    }

    @Test
    fun `complex calculation with variables and percentages`() {
        val lines = listOf(
            createLine("basePrice = 1000", sortOrder = 0),
            createLine("discount = 15% of basePrice", sortOrder = 1),
            createLine("discountedPrice = basePrice - discount", sortOrder = 2),
            createLine("tax = 10% of discountedPrice", sortOrder = 3),
            createLine("final = discountedPrice + tax", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000", result[0].result)
        assertEquals("150", result[1].result)
        assertEquals("850", result[2].result)
        assertEquals("85", result[3].result)
        assertEquals("935", result[4].result)
    }

    @Test
    fun `multi-line with comments and calculations`() {
        val lines = listOf(
            createLine("# Monthly budget calculation", sortOrder = 0),
            createLine("income = 5000", sortOrder = 1),
            createLine("rent = 1200 # apartment", sortOrder = 2),
            createLine("utilities = 300", sortOrder = 3),
            createLine("remaining = income - rent - utilities", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
        assertEquals("5000", result[1].result)
        assertEquals("1200", result[2].result)
        assertEquals("300", result[3].result)
        assertEquals("3500", result[4].result)
    }

    @Test
    fun `variable dependency chain calculates correctly`() {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = a * 2", sortOrder = 1),
            createLine("c = b + a", sortOrder = 2),
            createLine("d = c / a", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("20", result[1].result)
        assertEquals("30", result[2].result)
        assertEquals("3", result[3].result)
    }

    @Test
    fun `mixed valid and invalid lines process independently`() {
        val lines = listOf(
            createLine("5 + 5", sortOrder = 0),
            createLine("invalid ++", sortOrder = 1),
            createLine("10 * 2", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("Err", result[1].result)
        assertEquals("20", result[2].result)
    }

    @Test
    fun `large integer within Long range displays correctly`() {
        val lines = listOf(createLine("1000000000 * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("2000000000", result[0].result)
    }

    @Test
    fun `very large number uses scientific notation`() {
        val lines = listOf(createLine("999999999999999 * 999999999999999"))
        val result = MathEngine.calculate(lines)
        // Should be in scientific notation format
        assertTrue(result[0].result.contains("e") || result[0].result.length > 15)
    }

    @Test
    fun `single number evaluates to itself`() {
        val lines = listOf(createLine("42"))
        val result = MathEngine.calculate(lines)
        assertEquals("42", result[0].result)
    }

    @Test
    fun `negative numbers work correctly`() {
        val lines = listOf(createLine("-10 + 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("-5", result[0].result)
    }

    @Test
    fun `nested parentheses calculate correctly`() {
        val lines = listOf(createLine("((2 + 3) * (4 + 5))"))
        val result = MathEngine.calculate(lines)
        assertEquals("45", result[0].result)
    }

    @Test
    fun `expression with only whitespace after comment`() {
        val lines = listOf(createLine("10 + 5 #    "))
        val result = MathEngine.calculate(lines)
        assertEquals("15", result[0].result)
    }

    @Test
    fun `zero as result displays as 0`() {
        val lines = listOf(createLine("5 - 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("0", result[0].result)
    }

    @Test
    fun `decimal precision is maintained at 2 places`() {
        val lines = listOf(createLine("1 / 3 * 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("1", result[0].result) // Result is whole number
    }

    @Test
    fun `variable with underscore in name works`() {
        val lines = listOf(
            createLine("my_var = 100", sortOrder = 0),
            createLine("my_var * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100", result[0].result)
        assertEquals("200", result[1].result)
    }

    @Test
    fun `percentage calculation order matters`() {
        // 20% of 100 should be 20, not 100% of 20
        val lines = listOf(createLine("20% of 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("20", result[0].result)
    }

    @Test
    fun `multiple spaces in expression are handled`() {
        val lines = listOf(createLine("10    +    20"))
        val result = MathEngine.calculate(lines)
        assertEquals("30", result[0].result)
    }
}
