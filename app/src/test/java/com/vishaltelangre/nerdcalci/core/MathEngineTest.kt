package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking

class MathEngineTest {

    private fun createLine(expression: String, fileId: Long = 1L, sortOrder: Int = 0): LineEntity {
        return LineEntity(id = sortOrder.toLong(), fileId = fileId, expression = expression, result = "", sortOrder = sortOrder)
    }

    @Test
    fun `basic addition returns correct result`() = runBlocking {
        val lines = listOf(createLine("2 + 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("4.0", result[0].result)
    }

    @Test
    fun `basic subtraction returns correct result`() = runBlocking {
        val lines = listOf(createLine("10 - 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("7.0", result[0].result)
    }

    @Test
    fun `basic multiplication returns correct result`() = runBlocking {
        val lines = listOf(createLine("5 * 6"))
        val result = MathEngine.calculate(lines)
        assertEquals("30.0", result[0].result)
    }

    @Test
    fun `basic division returns correct result`() = runBlocking {
        val lines = listOf(createLine("20 / 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
    }

    @Test
    fun `basic modulo returns correct result`() = runBlocking {
        val lines = listOf(createLine("10 % 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("1.0", result[0].result)
    }

    @Test
    fun `modulo without spaces returns correct result`() = runBlocking {
        val lines = listOf(createLine("10%3"))
        val result = MathEngine.calculate(lines)
        assertEquals("1.0", result[0].result)
    }

    @Test
    fun `total throws dimensional mismatch error when accessed`() = runBlocking {
        val lines = listOf(
            createLine("4kg", sortOrder = 0),
            createLine("5 hour", sortOrder = 1),
            createLine("3 kph", sortOrder = 2),
            createLine("total", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[3].result)

        val errorMsg = MathEngine.getErrorDetails(lines, 3)
        assertEquals("Cannot sum Kilogram and Kilometer per hour: dimension mismatch", errorMsg)
    }

    @Test
    fun `chained percentage expression works correctly`() = runBlocking {
        val lines = listOf(createLine("100 + 20% - 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("115.0", result[0].result)
    }

    @Test
    fun `complex expression with multiple operators`() = runBlocking {
        val lines = listOf(createLine("2 + 3 * 4 - 1"))
        val result = MathEngine.calculate(lines)
        assertEquals("13.0", result[0].result)
    }

    @Test
    fun `expression with parentheses respects order of operations`() = runBlocking {
        val lines = listOf(createLine("(2 + 3) * 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("20.0", result[0].result)
    }

    @Test
    fun `exponentiation works correctly`() = runBlocking {
        val lines = listOf(createLine("2 ^ 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("8.0", result[0].result)
    }

    @Test
    fun `multiplication sign × is normalized to asterisk`() = runBlocking {
        val lines = listOf(createLine("5 × 6"))
        val result = MathEngine.calculate(lines)
        assertEquals("30.0", result[0].result)
    }

    @Test
    fun `division sign ÷ is normalized to slash`() = runBlocking {
        val lines = listOf(createLine("20 ÷ 4"))
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
    }

    @Test
    fun `temperature addition is order independent`() = runBlocking {
        val lines = listOf(
            createLine("30 °F + 30 °C + 30 °C"),
            createLine("30 °C + 30 °F + 30 °C"),
            createLine("30 °C + 30 °C + 30 °F"),
        )
        val result = MathEngine.calculate(lines)

        // Both expressions should normalize to the same canonical temperature result.
        val resultString1 = result[0].result
        val spaceIndex1 = resultString1.indexOf(' ')
        assertTrue(spaceIndex1 > 0)
        val value1 = resultString1.substring(0, spaceIndex1).toDouble()
        val unit1 = resultString1.substring(spaceIndex1 + 1)

        val resultString2 = result[1].result
        val spaceIndex2 = resultString2.indexOf(' ')
        assertTrue(spaceIndex2 > 0)
        val value2 = resultString2.substring(0, spaceIndex2).toDouble()
        val unit2 = resultString2.substring(spaceIndex2 + 1)

        val resultString3 = result[2].result
        val spaceIndex3 = resultString3.indexOf(' ')
        assertTrue(spaceIndex3 > 0)
        val value3 = resultString3.substring(0, spaceIndex3).toDouble()
        val unit3 = resultString3.substring(spaceIndex3 + 1)

        assertEquals(58.888888888888886, value1, 1e-9)
        assertEquals("°C", unit1)
        assertEquals(58.888888888888886, value2, 1e-9)
        assertEquals("°C", unit2)
        assertEquals(58.888888888888886, value3, 1e-9)
        assertEquals("°C", unit3)
    }

    @Test
    fun `em conversion with custom variable`() = runBlocking {
        val lines = listOf(
            createLine("em = 20", sortOrder = 0),
            createLine("2 em in px", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertTrue(result[1].result.startsWith("40.0"))
    }

    @Test
    fun `pixel conversion with custom ppi variable`() = runBlocking {
        val lines = listOf(
            createLine("ppi = 120", sortOrder = 0),
            createLine("10 px in inches", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)

        val resultStr = result[1].result
        val spaceIndex = resultStr.indexOf(' ')
        assertTrue(spaceIndex > 0)

        val value = resultStr.substring(0, spaceIndex).toDouble()
        val unit = resultStr.substring(spaceIndex + 1)

        assertEquals(0.083333333333, value, 1e-6)
        assertEquals("inch", unit)
    }

    @Test
    fun `trigonometric functions support degree inputs`() = runBlocking {
        val lines = listOf(
            createLine("sin(90°)"),
            createLine("sin(30 deg)"),
            createLine("cos(60 degree)"),
            createLine("sin(90)") // defaults to radians
        )
        val result = MathEngine.calculate(lines)
        assertEquals(1.0, result[0].result.toDouble(), 1e-9)
        assertEquals(0.5, result[1].result.toDouble(), 1e-9)
        assertEquals(0.5, result[2].result.toDouble(), 1e-9)
        assertEquals(0.8939966636005579, result[3].result.toDouble(), 1e-9)
    }

    @Test
    fun `mixed unicode and ASCII operators work together`() = runBlocking {
        val lines = listOf(createLine("10 × 2 ÷ 4 + 1"))
        val result = MathEngine.calculate(lines)
        assertEquals("6.0", result[0].result)
    }

    @Test
    fun `numeral system multipliers evaluate correctly`() = runBlocking {
        val lines = listOf(
            createLine("5 thousand", sortOrder = 0),
            createLine("2.5 million", sortOrder = 1),
            createLine("1.5 crore", sortOrder = 2),
            createLine("2 lakh + 5 thousand", sortOrder = 3),
            createLine("50% of 1 lakh", sortOrder = 4),
            createLine("1.5 hundred", sortOrder = 5),
            createLine("1 billion", sortOrder = 6),
            createLine("1 trillion", sortOrder = 7),
            createLine("4500 million in crores", sortOrder = 8),
            createLine("2 lakh to thousand", sortOrder = 9),
            createLine("5 thousand meters to km", sortOrder = 10),
            createLine("10 thousand in hex", sortOrder = 11)
        )
        val result = MathEngine.calculate(lines)

        assertEquals("5000.0", result[0].result)
        assertEquals("2500000.0", result[1].result)
        assertEquals("1.5E7", result[2].result)
        assertEquals("205000.0", result[3].result)
        assertEquals("50000.0", result[4].result)
        assertEquals("150.0", result[5].result)
        assertEquals("1.0E9", result[6].result)
        assertEquals("1.0E12", result[7].result)
        assertEquals("450.0 crore", result[8].result)
        assertEquals("200.0 thousand", result[9].result)
        assertEquals("5.0 km", result[10].result)
        assertEquals("0x2710", result[11].result)
    }

    @Test
    fun `decimal addition returns formatted result`() = runBlocking {
        val lines = listOf(createLine("1.5 + 2.3"))
        val result = MathEngine.calculate(lines)
        assertEquals("3.8", result[0].result)
    }

    @Test
    fun `decimal division returns raw double precision`() = runBlocking {
        val lines = listOf(createLine("10 / 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("3.333333333333333333333333333333333", result[0].result)
    }

    @Test
    fun `result with no decimal part shows as double with trailing zero`() = runBlocking {
        val lines = listOf(createLine("5.0 + 5.0"))
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `simple variable assignment stores value`() = runBlocking {
        val lines = listOf(
            createLine("price = 100", sortOrder = 0),
            createLine("price", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100.0", result[0].result)
        assertEquals("100.0", result[1].result)
    }

    @Test
    fun `variable can be used in calculations`() = runBlocking {
        val lines = listOf(
            createLine("price = 100", sortOrder = 0),
            createLine("price * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100.0", result[0].result)
        assertEquals("200.0", result[1].result)
    }

    @Test
    fun `multiple variables work together`() = runBlocking {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("a + b", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("30.0", result[2].result)
    }

    @Test
    fun `variable reassignment updates value`() = runBlocking {
        val lines = listOf(
            createLine("x = 5", sortOrder = 0),
            createLine("x * 2", sortOrder = 1),
            createLine("x = 10", sortOrder = 2),
            createLine("x * 2", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
        assertEquals("10.0", result[1].result)
        assertEquals("10.0", result[2].result)
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `variable with underscores in name`() = runBlocking {
        val lines = listOf(
            createLine("monthly_salary = 5000", sortOrder = 0),
            createLine("monthly_salary * 12", sortOrder = 1),
            createLine("monthly_salary", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5000.0", result[0].result)
        assertEquals("60000.0", result[1].result)
        assertEquals("5000.0", result[2].result)
    }

    @Test
    fun `variable with underscores in percentage expressions`() = runBlocking {
        val lines =
                listOf(
                        createLine("rate = 10", sortOrder = 0),
                        createLine("rate_with_disc = 10% off rate", sortOrder = 1),
                        createLine("rate_with_disc", sortOrder = 2)
                )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("9.0", result[1].result)
        assertEquals("9.0", result[2].result)
    }

    @Test
    fun `undefined variable returns error not implicit multiplication`() = runBlocking {
        // rate2 (without underscore) is not defined, should error out instead of being parsed as rate * 2
        val lines = listOf(
            createLine("rate = 10", sortOrder = 0),
            createLine("rate2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("Err", result[1].result)
    }

    @Test
    fun `variable assignment with expression`() = runBlocking {
        val lines = listOf(
            createLine("total = 10 + 20 + 30", sortOrder = 0),
            createLine("total / 3", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("60.0", result[0].result)
        assertEquals("20.0", result[1].result)
    }

    @Test
    fun `percentage of number works correctly`() = runBlocking {
        val lines = listOf(createLine("20% of 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("20.0", result[0].result)
    }

    @Test
    fun `percentage of decimal number`() = runBlocking {
        val lines = listOf(createLine("15.5% of 200"))
        val result = MathEngine.calculate(lines)
        assertEquals("31.0", result[0].result) // Result is whole number
    }

    @Test
    fun `percentage of variable`() = runBlocking {
        val lines = listOf(
            createLine("price = 1000", sortOrder = 0),
            createLine("10% of price", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000.0", result[0].result)
        assertEquals("100.0", result[1].result)
    }

    @Test
    fun `percentage of quantity preserves unit`() = runBlocking {
        val lines = listOf(
            createLine("10000g", sortOrder = 0),
            createLine("10% of _", sortOrder = 1),
            createLine("_ + 9kg", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10000.0 g", result[0].result)
        assertEquals("1000.0 g", result[1].result)
        assertEquals("10000.0 g", result[2].result)
    }

    @Test
    fun `percentage off reduces value`() = runBlocking {
        val lines = listOf(createLine("20% off 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("80.0", result[0].result)
    }

    @Test
    fun `percentage off with decimal`() = runBlocking {
        val lines = listOf(createLine("25% off 80"))
        val result = MathEngine.calculate(lines)
        assertEquals("60.0", result[0].result)
    }

    @Test
    fun `percentage off variable`() = runBlocking {
        val lines = listOf(
            createLine("original = 500", sortOrder = 0),
            createLine("30% off original", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("500.0", result[0].result)
        assertEquals("350.0", result[1].result)
    }

    @Test
    fun `add percentage to number`() = runBlocking {
        val lines = listOf(createLine("100 + 20%"))
        val result = MathEngine.calculate(lines)
        assertEquals("120.0", result[0].result)
    }

    @Test
    fun `add percentage to variable`() = runBlocking {
        val lines = listOf(
            createLine("salary = 50000", sortOrder = 0),
            createLine("salary + 10%", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("50000.0", result[0].result)
        assertEquals("55000.0", result[1].result)
    }

    @Test
    fun `subtract percentage from number`() = runBlocking {
        val lines = listOf(createLine("100 - 15%"))
        val result = MathEngine.calculate(lines)
        assertEquals("85.0", result[0].result)
    }

    @Test
    fun `subtract percentage from variable`() = runBlocking {
        val lines = listOf(
            createLine("budget = 1000", sortOrder = 0),
            createLine("budget - 25%", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000.0", result[0].result)
        assertEquals("750.0", result[1].result)
    }

    @Test
    fun `expression with inline comment returns result`() = runBlocking {
        val lines = listOf(createLine("10 + 5 # adding numbers"))
        val result = MathEngine.calculate(lines)
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `full line comment returns empty result`() = runBlocking {
        val lines = listOf(createLine("# This is just a comment"))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `comment with special characters is ignored`() = runBlocking {
        val lines = listOf(createLine("20 * 2 # result should be 40!"))
        val result = MathEngine.calculate(lines)
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `hash symbol in middle of expression is treated as comment`() = runBlocking {
        val lines = listOf(createLine("5 + 5 # + 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `empty expression returns empty result`() = runBlocking {
        val lines = listOf(createLine(""))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `blank expression with spaces returns empty result`() = runBlocking {
        val lines = listOf(createLine("   "))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `expression with only comment and spaces returns empty result`() = runBlocking {
        val lines = listOf(createLine("   # just a comment"))
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
    }

    @Test
    fun `invalid expression returns Err`() = runBlocking {
        val lines = listOf(createLine("2 + * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `division by zero returns Err`() = runBlocking {
        val lines = listOf(createLine("10 / 0"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `undefined variable returns Err`() = runBlocking {
        val lines = listOf(createLine("unknownVar * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `malformed parentheses returns Err`() = runBlocking {
        val lines = listOf(createLine("(2 + 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `complex calculation with variables and percentages`() = runBlocking {
        val lines = listOf(
            createLine("basePrice = 1000", sortOrder = 0),
            createLine("discount = 15% of basePrice", sortOrder = 1),
            createLine("discountedPrice = basePrice - discount", sortOrder = 2),
            createLine("tax = 10% of discountedPrice", sortOrder = 3),
            createLine("final = discountedPrice + tax", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000.0", result[0].result)
        assertEquals("150.0", result[1].result)
        assertEquals("850.0", result[2].result)
        assertEquals("85.0", result[3].result)
        assertEquals("935.0", result[4].result)
    }

    @Test
    fun `multi-line with comments and calculations`() = runBlocking {
        val lines = listOf(
            createLine("# Monthly budget calculation", sortOrder = 0),
            createLine("income = 5000", sortOrder = 1),
            createLine("rent = 1200 # apartment", sortOrder = 2),
            createLine("utilities = 300", sortOrder = 3),
            createLine("remaining = income - rent - utilities", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
        assertEquals("5000.0", result[1].result)
        assertEquals("1200.0", result[2].result)
        assertEquals("300.0", result[3].result)
        assertEquals("3500.0", result[4].result)
    }

    @Test
    fun `variable dependency chain calculates correctly`() = runBlocking {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = a * 2", sortOrder = 1),
            createLine("c = b + a", sortOrder = 2),
            createLine("d = c / a", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("30.0", result[2].result)
        assertEquals("3.0", result[3].result)
    }

    @Test
    fun `mixed valid and invalid lines process independently`() = runBlocking {
        val lines = listOf(
            createLine("5 + 5", sortOrder = 0),
            createLine("invalid ++", sortOrder = 1),
            createLine("10 * 2", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("Err", result[1].result)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `large integer within Long range is returned correctly`() = runBlocking {
        val lines = listOf(createLine("1000000000 * 2"))
        val result = MathEngine.calculate(lines)
        assertEquals("2.0E9", result[0].result)
    }

    @Test
    fun `very large number uses scientific notation`() = runBlocking {
        val lines = listOf(createLine("999999999999999 * 999999999999999"))
        val result = MathEngine.calculate(lines)
        // Should be in scientific notation format
        assertTrue(result[0].result.contains("e") || result[0].result.length > 15)
    }

    @Test
    fun `single number evaluates to itself`() = runBlocking {
        val lines = listOf(createLine("42"))
        val result = MathEngine.calculate(lines)
        assertEquals("42.0", result[0].result)
    }

    @Test
    fun `negative numbers work correctly`() = runBlocking {
        val lines = listOf(createLine("-10 + 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("-5.0", result[0].result)
    }

    @Test
    fun `nested parentheses calculate correctly`() = runBlocking {
        val lines = listOf(createLine("((2 + 3) * (4 + 5))"))
        val result = MathEngine.calculate(lines)
        assertEquals("45.0", result[0].result)
    }

    @Test
    fun `expression with only whitespace after comment`() = runBlocking {
        val lines = listOf(createLine("10 + 5 #    "))
        val result = MathEngine.calculate(lines)
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `zero as result displays as 0`() = runBlocking {
        val lines = listOf(createLine("5 - 5"))
        val result = MathEngine.calculate(lines)
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `maintains decimal precision correctly`() = runBlocking {
        val lines = listOf(createLine("1 / 3 * 3"))
        val result = MathEngine.calculate(lines)
        assertEquals("0.9999999999999999999999999999999999", result[0].result) // Result is whole number
    }

    @Test
    fun `high precision arithmetic works for very large numbers`() = runBlocking {
        val lines = listOf(createLine("(10^100) + 1 - (10^100)"))
        val result = MathEngine.calculate(lines)
        assertEquals("1.0", result[0].result)
    }

    @Test
    fun `variable with underscore in name works`() = runBlocking {
        val lines = listOf(
            createLine("my_var = 100", sortOrder = 0),
            createLine("my_var * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100.0", result[0].result)
        assertEquals("200.0", result[1].result)
    }

    @Test
    fun `percentage calculation order matters`() = runBlocking {
        // 20% of 100 should be 20, not 100% of 20
        val lines = listOf(createLine("20% of 100"))
        val result = MathEngine.calculate(lines)
        assertEquals("20.0", result[0].result)
    }

    @Test
    fun `multiple spaces in expression are handled`() = runBlocking {
        val lines = listOf(createLine("10    +    20"))
        val result = MathEngine.calculate(lines)
        assertEquals("30.0", result[0].result)
    }

    @Test
    fun `invalid variable name with spaces returns error`() = runBlocking {
        val lines = listOf(createLine("rate with disc = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `invalid variable name starting with digit returns error`() = runBlocking {
        val lines = listOf(createLine("2rate = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `invalid variable name with special characters returns error`() = runBlocking {
        val lines = listOf(createLine("rate-disc = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `valid variable names with underscores work`() = runBlocking {
        val lines = listOf(
            createLine("rate_with_disc = 10", sortOrder = 0),
            createLine("rate_2 = 11", sortOrder = 1),
            createLine("_private2 = 5", sortOrder = 2),
            createLine("__internal__ = 3", sortOrder = 3),
            createLine("_private2 + __internal__", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("11.0", result[1].result)
        assertEquals("5.0", result[2].result)
        assertEquals("3.0", result[3].result)
        assertEquals("8.0", result[4].result)
    }

    @Test
    fun `trigonometric functions work`() = runBlocking {
        val lines = listOf(
            createLine("sin(0)", sortOrder = 0),
            createLine("cos(0)", sortOrder = 1),
            createLine("tan(0)", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0.0", result[0].result)
        assertEquals("1.0", result[1].result)
        assertEquals("0.0", result[2].result)
    }

    @Test
    fun `inverse trigonometric functions work`() = runBlocking {
        val lines = listOf(
            createLine("asin(0)", sortOrder = 0),
            createLine("acos(1)", sortOrder = 1),
            createLine("atan(0)", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0.0", result[0].result)
        assertEquals("0.0", result[1].result)
        assertEquals("0.0", result[2].result)
    }

    @Test
    fun `logarithm functions work`() = runBlocking {
        val lines = listOf(
            createLine("log10(1000)", sortOrder = 0),
            createLine("log2(8)", sortOrder = 1),
            createLine("log(E)", sortOrder = 2)  // Natural log of E should be 1
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("1.0", result[2].result)
    }

    @Test
    fun `power and root functions work`() = runBlocking {
        val lines = listOf(
            createLine("sqrt(16)", sortOrder = 0),
            createLine("cbrt(27)", sortOrder = 1),
            createLine("pow(2, 8)", sortOrder = 2),
            createLine("exp(0)", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("4.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("256.0", result[2].result)
        assertEquals("1.0", result[3].result)
    }

    @Test
    fun `factorial functions work`() = runBlocking {
        val lines = listOf(
            createLine("factorial(0)", sortOrder = 0),
            createLine("factorial(5)", sortOrder = 1),
            createLine("fact(6)", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1.0", result[0].result)
        assertEquals("120.0", result[1].result)
        assertEquals("720.0", result[2].result)
    }

    @Test
    fun `rounding functions work`() = runBlocking {
        val lines = listOf(
            createLine("abs(-42)", sortOrder = 0),
            createLine("floor(3.7)", sortOrder = 1),
            createLine("ceil(3.2)", sortOrder = 2),
            createLine("signum(-5)", sortOrder = 3),
            createLine("signum(0)", sortOrder = 4),
            createLine("signum(5)", sortOrder = 5)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("42.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("-1.0", result[3].result)
        assertEquals("0.0", result[4].result)
        assertEquals("1.0", result[5].result)
    }

    @Test
    fun `constants work`() = runBlocking {
        val lines = listOf(
            createLine("PI", sortOrder = 0),
            createLine("E", sortOrder = 1),
            createLine("PI * 2", sortOrder = 2),
            createLine("E + 1", sortOrder = 3),
            createLine("pi", sortOrder = 4),
            createLine("π", sortOrder = 5),
            createLine("e", sortOrder = 6)

        )
        val result = MathEngine.calculate(lines)

        val piValue = result[0].result.toDoubleOrNull()
        assertNotNull("PI should return a number, got: ${result[0].result}", piValue)
        assertTrue("PI should be ~3.14, got: $piValue", piValue!! >= 3.14 && piValue <= 3.15)

        val eValue = result[1].result.toDoubleOrNull()
        assertNotNull("E should return a number, got: ${result[1].result}", eValue)
        assertTrue("E should be ~2.72, got: $eValue", eValue!! >= 2.71 && eValue <= 2.73)

        val piTimesTwo = result[2].result.toDoubleOrNull()
        assertNotNull("PI * 2 should return a number, got: ${result[2].result}", piTimesTwo)
        assertTrue("PI * 2 should be ~6.28, got: $piTimesTwo", piTimesTwo!! >= 6.28 && piTimesTwo <= 6.29)

        val ePlusOne = result[3].result.toDoubleOrNull()
        assertNotNull("E + 1 should return a number, got: ${result[3].result}", ePlusOne)
        assertTrue("E + 1 should be ~3.71, got: $ePlusOne", ePlusOne!! >= 3.71 && ePlusOne <= 3.73)

        val piValueLower = result[4].result.toDoubleOrNull()
        assertNotNull("pi should return a number, got: ${result[4].result}", piValueLower)
        assertTrue("pi should be ~3.14, got: $piValueLower", piValueLower!! >= 3.14 && piValueLower <= 3.15)

        val piValueSymbol = result[5].result.toDoubleOrNull()
        assertNotNull("π should return a number, got: ${result[5].result}", piValueSymbol)
        assertTrue("π should be ~3.14, got: $piValueSymbol", piValueSymbol!! >= 3.14 && piValueSymbol <= 3.15)

        val eValueLower = result[6].result.toDoubleOrNull()
        assertNotNull("e should return a number, got: ${result[6].result}", eValueLower)
        assertTrue("e should be ~2.72, got: $eValueLower", eValueLower!! >= 2.71 && eValueLower <= 2.73)
    }


    @Test
    fun `functions can be used with variables`() = runBlocking {
        val lines = listOf(
            createLine("radius = 5", sortOrder = 0),
            createLine("area = PI * pow(radius, 2)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
        // Area should be approximately 78.54
        assertTrue(result[1].result.toDouble() > 78 && result[1].result.toDouble() < 79)
    }

    @Test
    fun `nested functions work`() = runBlocking {
        val lines = listOf(
            createLine("sqrt(pow(3, 2) + pow(4, 2))", sortOrder = 0),
            createLine("abs(sin(0) - 1)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result) // Pythagorean theorem: sqrt(9 + 16) = 5
        assertEquals("1.0", result[1].result) // abs(0 - 1) = 1
    }

    @Test
    fun `functions are case sensitive`() = runBlocking {
        val lines = listOf(
            createLine("SQRT(16)", sortOrder = 0),
            createLine("SIN(0)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        assertEquals("Err", result[1].result)
    }

    @Test
    fun `functions require parentheses`() = runBlocking {
        val lines = listOf(
            createLine("floor(3.7)", sortOrder = 0),
            createLine("floor 3.7", sortOrder = 1),
            createLine("sqrt(16)", sortOrder = 2),
            createLine("sqrt 16", sortOrder = 3),
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3.0", result[0].result)
        assertEquals("Err", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("Err", result[3].result)
    }

    @Test
    fun `increment operator increases variable by 1`() = runBlocking {
        val lines = listOf(
            createLine("count = 5", sortOrder = 0),
            createLine("count++", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
        assertEquals("6.0", result[1].result)
    }

    @Test
    fun `decrement operator decreases variable by 1`() = runBlocking {
        val lines = listOf(
            createLine("count = 5", sortOrder = 0),
            createLine("count--", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[0].result)
        assertEquals("4.0", result[1].result)
    }

    @Test
    fun `compound addition assignment`() = runBlocking {
        val lines = listOf(
            createLine("total = 10", sortOrder = 0),
            createLine("total += 5 + 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("17.0", result[1].result) // 10 + (5 + 2)
    }

    @Test
    fun `compound subtraction assignment`() = runBlocking {
        val lines = listOf(
            createLine("total = 20", sortOrder = 0),
            createLine("total -= 5 * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("20.0", result[0].result)
        assertEquals("10.0", result[1].result) // 20 - (5 * 2)
    }

    @Test
    fun `compound multiplication assignment`() = runBlocking {
        val lines = listOf(
            createLine("factor = 3", sortOrder = 0),
            createLine("factor *= 2 + 1", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3.0", result[0].result)
        assertEquals("9.0", result[1].result) // 3 * (2 + 1)
    }

    @Test
    fun `compound division assignment`() = runBlocking {
        val lines = listOf(
            createLine("amount = 100", sortOrder = 0),
            createLine("amount /= 5", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100.0", result[0].result)
        assertEquals("20.0", result[1].result)
    }

    @Test
    fun `compound modulo (remainder) assignment`() = runBlocking {
        val lines = listOf(
            createLine("amount = 10", sortOrder = 0),
            createLine("amount %= 3", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("1.0", result[1].result)
    }

    @Test
    fun `calculateFrom returns only the affected lines starting at changedIndex`() = runBlocking {
        val lines =
                listOf(
                        createLine("1 + 1", sortOrder = 0),
                        createLine("2 + 2", sortOrder = 1),
                        createLine("3 + 3", sortOrder = 2)
                )
        val result = MathEngine.calculateFrom(lines, changedIndex = 1)
        // Should return lines[1..2] only (2 lines)
        assertEquals(2, result.size)
        assertEquals("4.0", result[0].result)
        assertEquals("6.0", result[1].result)
    }

    @Test
    fun `calculateFrom respects variables defined in preceding lines`() = runBlocking {
        val lines =
                listOf(
                        createLine("price = 100", sortOrder = 0), // preceding (not recalculated)
                        createLine("tax = 10", sortOrder = 1), // preceding (not recalculated)
                        createLine("price + tax", sortOrder = 2) // affected — must see both vars
                )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("110.0", result[0].result)
    }

    @Test
    fun `local function basic definition and call`() = runBlocking {
        val lines = listOf(
            createLine("f(x) = x * 2", sortOrder = 0),
            createLine("f(5)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result) // Function definition produces no output
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `local function multiple parameters`() = runBlocking {
        val lines = listOf(
            createLine("calc(a, b) = a + b;", sortOrder = 0),
            createLine("calc(10, 20)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
        assertEquals("30.0", result[1].result)
    }

    @Test
    fun `local function multiple statements returns last expression`() = runBlocking {
        val lines = listOf(
            createLine("salary(workHours) = base = workHours * 1000; bonus = base * 0.20; tax = base * 0.10; base + bonus - tax", sortOrder = 0),
            createLine("salary(120)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
        assertEquals("132000.0", result[1].result)
    }

    @Test
    fun `local function strictly isolates scope`() = runBlocking {
        val lines = listOf(
            createLine("v = 10", sortOrder = 0),
            createLine("f(x) = v = x;", sortOrder = 1),
            createLine("f(5)", sortOrder = 2),
            createLine("v", sortOrder = 3) // Should still be 10, not overridden by 'v' inside f
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("", result[1].result)
        assertEquals("5.0", result[2].result)
        assertEquals("10.0", result[3].result) // Outer scope unchanged
    }

    @Test
    fun `local function prevents infinite recursion`() = runBlocking {
        val lines = listOf(
            createLine("calc(a) = calc(a);", sortOrder = 0),
            createLine("calc(2)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("", result[0].result)
        assertEquals("Err", result[1].result) // Throws recursive exception
    }

    @Test
    fun `local function with trailing comment does not return 0`() = runBlocking {
        val lines =
                listOf(
                        createLine("f(x) = x * 2; # comment", sortOrder = 0),
                        createLine("f(5)", sortOrder = 1)
                )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[1].result) // Should NOT be "0"
    }

    @Test
    fun `nested function definition in a local function body is not allowed`() = runBlocking {
        val lines =
                listOf(
                        createLine("f(x) = g(y) = y; x", sortOrder = 0),
                        createLine("f(5)", sortOrder = 1)
                )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[1].result) // Parser throws error for nested definition
    }

    @Test
    fun `semicolons outside function bodies fail parsing`() = runBlocking {
        val lines = listOf(
            createLine("x = 10; y = 20", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result) // ParseException
    }

    @Test
    fun `calculateFrom with changedIndex 0 is equivalent to full calculate`() = runBlocking {
        val lines =
                listOf(
                        createLine("a = 5", sortOrder = 0),
                        createLine("b = a * 2", sortOrder = 1),
                        createLine("a + b", sortOrder = 2)
                )
        val full = MathEngine.calculate(lines)
        val partial = MathEngine.calculateFrom(lines, changedIndex = 0)
        assertEquals(full.map { it.result }, partial.map { it.result })
    }

    @Test
    fun `calculateFrom propagates variable reassignment from preceding lines to affected lines`() = runBlocking {
        // Simulate the user having changed line at index 1 (x = 10), now recalculating affected lines
        val lines =
                listOf(
                        createLine("x = 5", sortOrder = 0), // preceding: x = 5
                        createLine(
                                "x = 10",
                                sortOrder = 1
                        ), // changed line (changedIndex = 1, first affected)
                        createLine("x * 2", sortOrder = 2) // should use x = 10 from the changed line
                )
        val result = MathEngine.calculateFrom(lines, changedIndex = 1)
        assertEquals(2, result.size)
        assertEquals("10.0", result[0].result) // x = 10
        assertEquals("20.0", result[1].result) // x * 2 = 20
    }

    @Test
    fun `calculateFrom clamps out-of-bounds changedIndex gracefully`() = runBlocking {
        val lines = listOf(
                            createLine("5 + 5", sortOrder = 0),
                           createLine("2 * 3", sortOrder = 1)
                    )
        // changedIndex beyond list size — should return empty (nothing to recalculate)
        val result = MathEngine.calculateFrom(lines, changedIndex = 100)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateFrom clamps negative changedIndex to full recalculation`() = runBlocking {
        val lines = listOf(
            createLine("a = 5", sortOrder = 0),
            createLine("a * 2", sortOrder = 1)
        )
        // Negative index should clamp to 0 and recalculate everything
        val result = MathEngine.calculateFrom(lines, changedIndex = -99)
        assertEquals(2, result.size)
        assertEquals("5.0", result[0].result)
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `total sums all results above in same block`() = runBlocking {
        val lines = listOf(
            createLine("4 / 2", sortOrder = 0),
            createLine("b = 2", sortOrder = 1),
            createLine("a = 4", sortOrder = 2),
            createLine("total", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("2.0", result[0].result)
        assertEquals("2.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("8.0", result[3].result)
    }

    @Test
    fun `sum is an alias for total`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("sum", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("30.0", result[2].result)
    }

    @Test
    fun `blank line resets the block for total`() = runBlocking {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("total", sortOrder = 2),
            createLine("", sortOrder = 3),
            createLine("c = 5", sortOrder = 4),
            createLine("total", sortOrder = 5)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("30.0", result[2].result)  // 10 + 20
        assertEquals("", result[3].result)
        assertEquals("5.0", result[4].result)
        assertEquals("5.0", result[5].result)   // only c = 5 in this block
    }

    @Test
    fun `comment-only lines do not contribute to total`() = runBlocking {
        val lines =
                listOf(
                        createLine("10", sortOrder = 0),
                        createLine("# just a comment", sortOrder = 1),
                        createLine("20", sortOrder = 2),
                        createLine("total", sortOrder = 3)
                )
        val result = MathEngine.calculate(lines)
        // comment-only line produces null → breaks the block
        // so total only sees 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `total used in an expression`() = runBlocking {
        val lines = listOf(
            createLine("item1 = 25", sortOrder = 0),
            createLine("item2 = 75", sortOrder = 1),
            createLine("tax = total * 0.10", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("25.0", result[0].result)
        assertEquals("75.0", result[1].result)
        assertEquals("10.0", result[2].result)
    }

    @Test
    fun `total assignment overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("total = 4", sortOrder = 2),
            createLine("total / 2", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("2.0", result[3].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total increment overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total++", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("11.0", result[1].result)  // total was 10, incremented and assigned to 11
        assertEquals("11.0", result[2].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total decrement overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total--", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("9.0", result[1].result)   // total was 10, decremented to 9
        assertEquals("9.0", result[2].result)   // uses assigned value, not aggregate
    }

    @Test
    fun `total += overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total += 5", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("15.0", result[1].result)
        assertEquals("15.0", result[2].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total -= overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total -= 3", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("7.0", result[1].result)
        assertEquals("7.0", result[2].result)
    }

    @Test
    fun `total multiply-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total *= 2", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `total divide-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("total /= 2", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("5.0", result[1].result)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `total modulo-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("total %= 7", sortOrder = 2),
            createLine("total", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("2.0", result[2].result)   // 30 % 7 = 2
        assertEquals("2.0", result[3].result)
    }

    @Test
    fun `total with no preceding results is 0`() = runBlocking {
        val lines = listOf(
            createLine("total", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `total preserves units and does not convert to base unit`() = runBlocking {
        val lines = listOf(
            createLine("1 acre to sqft", sortOrder = 0),
            createLine("_/2", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("43560.0 ft²", result[0].result)
        assertEquals("21780.0 ft²", result[1].result)
        assertEquals("65340.0 ft²", result[2].result)
    }

    @Test
    fun `total handles unitless numbers as target unit`() = runBlocking {
        val lines = listOf(
            createLine("4 kg", sortOrder = 0),
            createLine("5", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("4.0 kg", result[0].result)
        assertEquals("5.0", result[1].result)
        assertEquals("9.0 kg", result[2].result)
    }

    @Test
    fun `total with mixed categories returns error`() = runBlocking {
        val lines = listOf(
            createLine("10 m", sortOrder = 0),
            createLine("20 s", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[2].result)

        val errorMsg = MathEngine.getErrorDetails(lines, 2)
        assertEquals("Cannot sum Meter and Second: dimension mismatch", errorMsg)
    }

    @Test
    fun `calculateFrom correctly handles total in affected lines`() = runBlocking {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("30.0", result[0].result)
    }

    @Test
    fun `total includes its own block results across calculateFrom boundary`() = runBlocking {
        val lines = listOf(
            createLine("5", sortOrder = 0),
            createLine("15", sortOrder = 1),
            createLine("total * 2", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `avg averages all results above in same block`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("60", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("60.0", result[2].result)
        assertEquals("30.0", result[3].result)
    }

    @Test
    fun `average is an alias for avg`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("30", sortOrder = 1),
            createLine("average", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("30.0", result[1].result)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `avg with no preceding results is 0`() = runBlocking {
        val lines = listOf(
            createLine("avg", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `blank line resets the block for avg`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg", sortOrder = 2),
            createLine("", sortOrder = 3),
            createLine("5", sortOrder = 4),
            createLine("avg", sortOrder = 5)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("15.0", result[2].result)
        assertEquals("", result[3].result)
        assertEquals("5.0", result[5].result)
    }

    @Test
    fun `comment-only lines do not contribute to avg`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("# ignore this", sortOrder = 1), // Should be null in lineResults
            createLine("20", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        // Null breaks the block, so avg only sees 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `avg used in an expression`() = runBlocking {
        val lines = listOf(
            createLine("25", sortOrder = 0),
            createLine("75", sortOrder = 1),
            createLine("half_avg = avg / 2", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("25.0", result[2].result) // avg is 50, halved to 25
    }

    @Test
    fun `avg assignment overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg = 100", sortOrder = 2),
            createLine("avg / 2", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100.0", result[2].result)
        assertEquals("50.0", result[3].result)
    }

    @Test
    fun `avg increment overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg++", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("16.0", result[2].result) // avg is 15, gets incremented and assigned to 16
        assertEquals("16.0", result[3].result)
    }

    @Test
    fun `avg decrement overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg--", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("14.0", result[2].result) // avg is 15, gets decremented and assigned to 14
        assertEquals("14.0", result[3].result)
    }

    @Test
    fun `avg += overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg += 5", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[2].result) // 15 + 5 = 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `avg -= overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg -= 3", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("12.0", result[2].result) // 15 - 3 = 12
        assertEquals("12.0", result[3].result)
    }

    @Test
    fun `avg multiply-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg *= 2", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("30.0", result[2].result) // 15 * 2 = 30
        assertEquals("30.0", result[3].result)
    }

    @Test
    fun `avg divide-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg /= 3", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("5.0", result[2].result) // 15 / 3 = 5
        assertEquals("5.0", result[3].result)
    }

    @Test
    fun `avg modulo-assign overrides aggregate meaning`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("avg %= 4", sortOrder = 2),
            createLine("avg", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
        assertEquals("3.0", result[2].result) // 15 % 4 = 3
        assertEquals("3.0", result[3].result)
    }

    @Test
    fun `calculateFrom correctly handles avg in affected lines`() = runBlocking {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("avg", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `avg includes its own block results across calculateFrom boundary`() = runBlocking {
        val lines = listOf(
            createLine("5", sortOrder = 0),
            createLine("35", sortOrder = 1),
            createLine("avg * 2", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `last keyword refers to the previous line result`() = runBlocking {
        val lines = listOf(
            createLine("10 * 5", sortOrder = 0),
            createLine("last + 10", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("50.0", result[0].result)
        assertEquals("60.0", result[1].result)
    }

    @Test
    fun `prev keyword refers to the previous line result`() = runBlocking {
        val lines = listOf(
            createLine("100 / 4", sortOrder = 0),
            createLine("prev * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("25.0", result[0].result)
        assertEquals("50.0", result[1].result)
    }

    @Test
    fun `previous keyword refers to the previous line result`() = runBlocking {
        val lines = listOf(
            createLine("20 + 30", sortOrder = 0),
            createLine("previous - 10", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("50.0", result[0].result)
        assertEquals("40.0", result[1].result)
    }

    @Test
    fun `above keyword refers to the previous line result`() = runBlocking {
        val lines = listOf(
            createLine("5 ^ 2", sortOrder = 0),
            createLine("above / 5", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("25.0", result[0].result)
        assertEquals("5.0", result[1].result)
    }

    @Test
    fun `underscore keyword refers to the previous line result`() = runBlocking {
        val lines = listOf(
            createLine("42", sortOrder = 0),
            createLine("_ + 8", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("42.0", result[0].result)
        assertEquals("50.0", result[1].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line is blank`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("   ", sortOrder = 1),
            createLine("last + 5", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line is a comment`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("# comment", sortOrder = 1),
            createLine("last + 5", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line resulted in an error`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("{", sortOrder = 1), // Invalid expression
            createLine("last + 5", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[1].result)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 on the first line`() = runBlocking {
        val lines = listOf(
            createLine("last + 10", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `last keyword reassignment is blocked`() = runBlocking {
        val lines = listOf(createLine("last = 10", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`last` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `compound assignment to underscore is blocked`() = runBlocking {
        val lines = listOf(createLine("_ += 5", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`_` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `assignment to constant PI is not allowed`() = runBlocking {
        val lines = listOf(createLine("PI = 4", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`PI` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `assignment to constant pi is not allowed`() = runBlocking {
        val lines = listOf(createLine("pi = 4", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`pi` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `assignment to constant π is not allowed`() = runBlocking {
        val lines = listOf(createLine("π = 4", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`π` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `assignment to constant E is not allowed`() = runBlocking {
        val lines = listOf(createLine("E = 4", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`E` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `assignment to constant e is not allowed`() = runBlocking {
        val lines = listOf(createLine("e = 4", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`e` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `formatDisplayResult formats raw strings correctly`() = runBlocking {
        assertEquals("0.33", MathEngine.formatDisplayResult("0.3333333333333333", 2))
        assertEquals("0.3333", MathEngine.formatDisplayResult("0.3333333333333333", 4))
        assertEquals("10", MathEngine.formatDisplayResult("10.0", 2)) // whole numbers are displayed without decimal points
        assertEquals("4", MathEngine.formatDisplayResult("4", 6)) // whole numbers are displayed without decimal points
        assertEquals("Err", MathEngine.formatDisplayResult("Err", 2))
        assertEquals("1234.57", MathEngine.formatDisplayResult("1234.5678", 2))
        assertEquals("1234.5678000000", MathEngine.formatDisplayResult("1234.5678", 10))
    }

    @Test
    fun `formatDisplayResult handles out of bounds precision`() = runBlocking {
        // Should clamp value below MIN_PRECISION to MIN_PRECISION
        assertEquals("0", MathEngine.formatDisplayResult("0.333", Constants.MIN_PRECISION - 1))
        // Should clamp value above MAX_PRECISION to MAX_PRECISION
        assertEquals("0.3330000000", MathEngine.formatDisplayResult("0.333", Constants.MAX_PRECISION + 5))
    }

    @Test
    fun `formatDisplayResult handles scientific notation`() = runBlocking {
        assertEquals("1.00e+20", MathEngine.formatDisplayResult("1.0E20", 2, java.util.Locale.ROOT))
        assertEquals("1e+20", MathEngine.formatDisplayResult("1.0E20", 0, java.util.Locale.ROOT))
    }

    @Test
    fun `formatDisplayResult respects explicit locales`() = runBlocking {
        val raw = "1234.567"
        // Locale.ROOT uses dot
        assertEquals("1234.57", MathEngine.formatDisplayResult(raw, 2, java.util.Locale.ROOT))
        // Locale.GERMANY uses comma
        assertEquals("1234,57", MathEngine.formatDisplayResult(raw, 2, java.util.Locale.GERMANY))
        // Scientific notation with Locale.GERMANY
        val largeRaw = "1.23E30"
        assertEquals("1,23e+30", MathEngine.formatDisplayResult(largeRaw, 2, java.util.Locale.GERMANY))
    }

    @Test
    fun `getErrorDetails handles undefined variable`() = runBlocking {
        val lines = listOf(createLine("x + 5"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Unknown variable `x`", err)
    }

    @Test
    fun `getErrorDetails handles syntax error`() = runBlocking {
        val lines = listOf(createLine("1 + (2 * 3"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Expected `)`, but found `end of line`", err)
    }

    @Test
    fun `getErrorDetails returns null for blank line`() = runBlocking {
        val lines = listOf(createLine(""))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertNull(err)
    }

    @Test
    fun `getErrorDetails handles division by zero`() = runBlocking {
        val lines = listOf(createLine("10 / 0"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Cannot divide by zero", err)
    }

    @Test
    fun `getErrorDetails handles unknown function`() = runBlocking {
        val lines = listOf(createLine("unknown(5)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Unknown function `unknown()`", err)
    }

    @Test
    fun `getErrorDetails handles lexer error`() = runBlocking {
        val lines = listOf(createLine("1 @ 2"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Unexpected character `@`", err)
    }

    @Test
    fun `getErrorDetails handles multiple operators`() = runBlocking {
        val lines = listOf(createLine("1 + * 2"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Expected a value or `(`, but found `*`", err)
    }

    @Test
    fun `getErrorDetails handles missing operand`() = runBlocking {
        val lines = listOf(createLine("5 + "))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Expected a value or `(`, but found `end of line`", err)
    }

    @Test
    fun `getErrorDetails handles empty parentheses`() = runBlocking {
        val lines = listOf(createLine("()"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Expected a value or `(`, but found `)`", err)
    }

    @Test
    fun `getErrorDetails handles arity mismatch for user-defined function`() = runBlocking {
        val lines = listOf(
            createLine("f(x) = x * 2"),
            createLine("f(1, 2)")
        )
        val err = MathEngine.getErrorDetails(lines, 1)
        assertEquals("Function `f()` expects 1 argument, but got 2", err)
    }

    @Test
    fun `arity mismatch reported before undefined argument for built in function`() = runBlocking {
        val lines = listOf(createLine("sinh(2, 5)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Function `sinh()` expects 1 argument, but got 2", err)
    }

    @Test
    fun `arity mismatch reported before undefined argument for local function`() = runBlocking {
        val lines = listOf(
            createLine("f(x) = x * 2"),
            createLine("f(1, unknown_var)")
        )
        val err = MathEngine.getErrorDetails(lines, 1)
        assertEquals("Function `f()` expects 1 argument, but got 2", err)
    }

    @Test
    fun `arity mismatch reported before undefined argument for built-in function with invalid arg`() = runBlocking {
        val lines = listOf(createLine("sinh(1, unknown_var)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Function `sinh()` expects 1 argument, but got 2", err)
    }

    @Test
    fun `factorial rejects fractional input`() = runBlocking {
        val lines = listOf(createLine("factorial(4.5)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Factorial is only defined for whole numbers", err)
    }

    @Test
    fun `factorial rejects negative input`() = runBlocking {
        val lines = listOf(createLine("fact(-3)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Factorial is only defined for non-negative whole numbers", err)
    }

    @Test
    fun `factorial rejects inputs beyond supported limit`() = runBlocking {
        val lines = listOf(createLine("factorial(1001)"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Factorial is only supported up to 1000", err)
    }

    @Test
    fun `lineno, linenumber, and currentLineNumber returns correct current line number`() = runBlocking {
        val lines = listOf(
            createLine("lineno", sortOrder = 0),
            createLine("linenumber", sortOrder = 1),
            createLine("currentLineNumber", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1.0", result[0].result)
        assertEquals("2.0", result[1].result)
        assertEquals("3.0", result[2].result)
    }

    @Test
    fun `lineno works in expressions`() = runBlocking {
        val lines = listOf(
            createLine("10 + lineno"), // 10 + 1 = 11
            createLine("lineno * 5")   // 2 * 5 = 10
        )
        val result = MathEngine.calculate(lines)
        assertEquals("11.0", result[0].result)
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `lineno is reserved and cannot be reassigned`() = runBlocking {
        val lines = listOf(
            createLine("lineno = 10")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        var err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`lineno` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `compound assignment to lineno is not allowed`() = runBlocking {
        val lines = listOf(
            createLine("lineno += 5")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        var err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`lineno` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `increment on lineno is not allowed`() = runBlocking {
        val lines = listOf(
            createLine("lineno++")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        var err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`lineno` is a reserved name and cannot be changed", err)
    }

    @Test
    fun `lineno updates correctly after line insertion`() = runBlocking {
        // Initial state
        val lines = mutableListOf(
            createLine("x = 10", sortOrder = 0),
            createLine("lineno", sortOrder = 1) // Should be 2
        )
        val result1 = MathEngine.calculate(lines)
        assertEquals("2.0", result1[1].result)

        // Insert line at index 1
        lines.add(1, createLine("y = 20", sortOrder = 1))
        // Update sort orders
        val updatedLines = lines.mapIndexed { index, line -> line.copy(sortOrder = index) }

        val result2 = MathEngine.calculate(updatedLines)
        assertEquals("10.0", result2[0].result) // L1: x=10
        assertEquals("20.0", result2[1].result) // L2: y=20
        assertEquals("3.0", result2[2].result)  // L3: lineno (was 2, now 3)
    }

    @Test
    fun `lineno updates correctly after line deletion`() = runBlocking {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("lineno", sortOrder = 2) // Should be 3
        )
        val result1 = MathEngine.calculate(lines)
        assertEquals("3.0", result1[2].result)

        // Delete line at index 1
        val remainingLines = listOf(lines[0], lines[2]).mapIndexed { index, line -> line.copy(sortOrder = index) }
        val result2 = MathEngine.calculate(remainingLines)
        assertEquals("10.0", result2[0].result)
        assertEquals("2.0", result2[1].result) // lineno (was 3, now 2)
    }

    @Test
    fun `calculateFrom handles lineno correctly for partial recalculation`() = runBlocking {
        val lines = listOf(
            createLine("a = 5", sortOrder = 0),   // L1
            createLine("b = 10", sortOrder = 1),  // L2
            createLine("lineno + a", sortOrder = 2) // L3: 3 + 5 = 8
        )

        // Initial full calculation
        val fullResult = MathEngine.calculate(lines)
        assertEquals("8.0", fullResult[2].result)

        // Partial recalculation from line 2 (index 2)
        val partialResult = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, partialResult.size)
        assertEquals("8.0", partialResult[0].result)
    }

    private class FakeFileContextLoader(private val contexts: Map<String, MathContext>) : FileContextLoader {
        override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
            return contexts[fileName]
        }
    }

    @Test
    fun `evaluate resolves member access from linked file`() = runBlocking {
        val remoteContext = MathContext(variables = mutableMapOf("x" to EvaluationResult(BigDecimal("20.0"))))
        val loader = FakeFileContextLoader(mapOf("File B" to remoteContext))

        val lines = listOf(
            createLine("f = file(\"File B\")"),
            createLine("f.x + 5")
        )
        val result = MathEngine.calculate(lines, loader)
        assertEquals("25.0", result[1].result)
    }

    @Test
    fun `evaluate with direct file function access`() = runBlocking {
        val remoteContext = MathContext(variables = mutableMapOf("total" to EvaluationResult(BigDecimal("100.0"))))
        val loader = FakeFileContextLoader(mapOf("Summary" to remoteContext))

        val lines = listOf(createLine("file(\"Summary\").total * 0.1"))
        val result = MathEngine.calculate(lines, loader)
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `evaluate clears file linked state after reassignment to number`() = runBlocking {
        val remoteContext = MathContext(variables = mutableMapOf("x" to EvaluationResult(BigDecimal("20.0"))))
        val loader = FakeFileContextLoader(mapOf("File B" to remoteContext))

        val lines = listOf(
            createLine("f = file(\"File B\")"),
            createLine("f = 42"),
            createLine("f.x")
        )
        val result = MathEngine.calculate(lines, loader)
        assertEquals("Err", result[2].result)
        val err = MathEngine.getErrorDetails(lines, 2, loader)
        assertEquals("`f` is not linked to any file. Use `file(\"...\")` to link first", err)
    }

    @Test
    fun `evaluate remote function call self-reference loop triggers CircularReferenceException`() = runBlocking {
        val fileBContext = MathContext()

        fileBContext.localFunctions["loopback"] = LocalFunction(
            name = "loopback",
            params = emptyList(),
            body = listOf(
                Statement.ExprStatement(Expr.MemberAccess(Expr.FunctionCall("file", listOf(Expr.StringLiteral("File B"))), "x"))
            )
        )

        val loader = FakeFileContextLoader(mapOf(
            "File B" to fileBContext
        ))

        val lines = listOf(
            createLine("file(\"File B\").loopback()")
        )
        val result = MathEngine.calculate(lines, loader)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0, loader)
        assertEquals("File `File B` references itself, causing an endless loop", err)
    }

    @Test
    fun `evaluate resolves remote function call from linked file`() = runBlocking {
        val remoteContext = MathContext()
        remoteContext.localFunctions["double"] = LocalFunction(
            name = "double",
            params = listOf("x"),
            body = listOf(
                Statement.ExprStatement(
                    Expr.BinaryOp(
                        Expr.Variable("x"),
                        TokenKind.STAR,
                        Expr.NumberLiteral(BigDecimal("2.0"))
                    )
                )
            )
        )
        val loader = FakeFileContextLoader(mapOf("Summary" to remoteContext))

        val lines = listOf(createLine("file(\"Summary\").double(5) + 3"))
        val result = MathEngine.calculate(lines, loader)
        assertEquals("13.0", result[0].result)
    }

    @Test
    fun `cross-file function call keeps unit`() = runBlocking {
        val remoteContext = MathContext()
        remoteContext.localFunctions["addOne"] = LocalFunction(
            name = "addOne",
            params = listOf("x"),
            body = listOf(
                Statement.ExprStatement(
                    Expr.BinaryOp(
                        Expr.Variable("x"),
                        TokenKind.PLUS,
                        Expr.Quantity(Expr.NumberLiteral(BigDecimal("1.0")), "cm")
                    )
                )
            )
        )
        val loader = FakeFileContextLoader(mapOf("Summary" to remoteContext))

        val lines = listOf(createLine("file(\"Summary\").addOne(10 cm)"))
        val result = MathEngine.calculate(lines, loader)
        assertEquals("11.0 cm", result[0].result)
    }

    @Test
    fun `standalone string literal throws error`() = runBlocking {
        val lines = listOf(createLine("\"hello\""))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Quotes are only allowed when specifying file names in `file(\"...\")`", err)
    }

    @Test
    fun `writing to member access target is read-only`() = runBlocking {
        val lines = listOf(createLine("f = file(\"File B\")"), createLine("f.x = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[1].result)
        val err = MathEngine.getErrorDetails(lines, 1)
        assertEquals("Variables from other files are read-only and cannot be changed", err)
    }

    @Test
    fun `dot notation on global functions throws error`() = runBlocking {
        val remoteContext = MathContext()
        val loader = FakeFileContextLoader(mapOf("File B" to remoteContext))
        val lines = listOf(createLine("f = file(\"File B\")"), createLine("f.sin(90)"))
        val result = MathEngine.calculate(lines, loader)
        assertEquals("Err", result[1].result)
        val err = MathEngine.getErrorDetails(lines, 1, loader)
        assertEquals("`sin()` is a global function and should be called directly, not via dot notation", err)
    }

    @Test
    fun `file call missing closing parenthesis throws error`() = runBlocking {
        val lines = listOf(createLine("file(\"A\""))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0)
        assertTrue(err != null && err.contains("Expected `)`"))
    }

    @Test
    fun `dot notation missing identifier throws error`() = runBlocking {
        val lines = listOf(createLine("file(\"A\")."))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Missing variable or function name after `.`", err)
    }

    private class RecursiveFakeFileContextLoader(
        private val files: Map<String, List<LineEntity>>
    ) : FileContextLoader {
        override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
            val lines = files[fileName] ?: return null
            return MathEngine.buildVariableState(lines, this, loadingStack)
        }
    }

    @Test
    fun `circular dependency between files results in Err`() = runBlocking {
        val fileALines = listOf(
            createLine("b = file(\"File B\")", fileId = 1L, sortOrder = 0),
            createLine("b.y", fileId = 1L, sortOrder = 1)
        )
        val fileBLines = listOf(
            createLine("a = file(\"File A\")", fileId = 2L, sortOrder = 0),
            createLine("a.x", fileId = 2L, sortOrder = 1)
        )

        val loader = RecursiveFakeFileContextLoader(mapOf(
            "File A" to fileALines,
            "File B" to fileBLines
        ))

        val result = MathEngine.calculate(fileALines, loader)
        assertEquals("Err", result[1].result)
    }

    @Test
    fun `CircularReferenceException formats message intuitively`() {
        // Direct self-reference loop: A -> A
        val selfLoop = CircularReferenceException("Salary", setOf("Salary"))
        assertEquals("File `Salary` references itself, causing an endless loop", selfLoop.message)

        // Indirect double-hop loop: A -> B -> A
        val doubleHop = CircularReferenceException("Untitled", setOf("Untitled", "Salary"))
        assertEquals("File `Salary` also references file `Untitled`, causing an endless loop", doubleHop.message)

        // Deeper nested multi-hop loop: A -> B -> C -> D -> B (fallback to arrows)
        val genericChain = CircularReferenceException("D", setOf("A", "B", "C"))
        assertEquals("Endless loop: A -> B -> C -> D", genericChain.message)
    }

    @Test
    fun `getErrorDetails reports intuitive circular messages with seeded stack`() = runBlocking {
        val fileALines = listOf(
            createLine("b = file(\"File B\")", fileId = 1L, sortOrder = 0),
            createLine("b.y", fileId = 1L, sortOrder = 1)
        )
        val fileBLines = listOf(
            createLine("a = file(\"File A\")", fileId = 2L, sortOrder = 0),
            createLine("a.x", fileId = 2L, sortOrder = 1)
        )

        val loader = RecursiveFakeFileContextLoader(mapOf(
            "File A" to fileALines,
            "File B" to fileBLines
        ))

        // Trigger loading B.y from A -> triggers circular loop back to A
        val errMsg = MathEngine.getErrorDetails(
            allLines = fileALines,
            targetIndex = 1,
            loader = loader,
            loadingStack = setOf("File A")
        )

        assertEquals("File `File B` also references file `File A`, causing an endless loop", errMsg)
    }

    @Test
    fun `unit conversion simple natural language`() = runBlocking {
        val lines = listOf(
            createLine("10 km in m", sortOrder = 0),
            createLine("1000 m as kilometers", sortOrder = 1),
            createLine("2 hours in seconds", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10000.0 m", result[0].result)
        assertEquals("1.0 km", result[1].result)
        assertEquals("7200.0 s", result[2].result)
    }

    @Test
    fun `unit conversion mixed arithmetic`() = runBlocking {
        val lines = listOf(
            createLine("10 km + 5000 m in km", sortOrder = 0),
            createLine("1 m + 100 cm in m", sortOrder = 1),
            createLine("10 kg + 20 gram to kilograms", sortOrder = 2),
            createLine("53 weeks - 20 days", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("15.0 km", result[0].result)
        assertEquals("2.0 m", result[1].result)
        assertEquals("10.02 kg", result[2].result)
        assertEquals("351.0 d", result[3].result)
    }

    @Test
    fun `unit conversion temperature non linear`() = runBlocking {
        val lines = listOf(
            createLine("0 degC in F", sortOrder = 0),
            createLine("212 F in C", sortOrder = 1),
            createLine("0 C in K", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("32.0 °F", result[0].result)
        assertEquals("100.0 °C", result[1].result)
        assertEquals("273.15 K", result[2].result)
    }

    @Test
    fun `unit conversion data storage`() = runBlocking {
        val lines = listOf(
            createLine("1 GB in MB", sortOrder = 0),
            createLine("1 GiB in MiB", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("1000.0 MB", result[0].result)
        assertEquals("1024.0 MiB", result[1].result)
    }

    @Test
    fun `unit conversion css dynamic ppi`() = runBlocking {
        val lines = listOf(
            createLine("96 px in inch", sortOrder = 0),
            createLine("ppi = 300", sortOrder = 1),
            createLine("300 px in inch", sortOrder = 2),
            createLine("em = 21px", sortOrder = 3), // triggers em evaluation
            createLine("1.5 em in px", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals(1.0, result[0].result.split(" ")[0].toDouble(), 0.0001)
        assertEquals(1.0, result[2].result.split(" ")[0].toDouble(), 0.0001)
        // em = 21px. 1.5 em = 1.5 * 21px = 31.5px
        val resStr = result[4].result
        val spaceIdx = resStr.indexOf(' ')
        val valStr = if (spaceIdx > 0) resStr.substring(0, spaceIdx) else resStr
        assertEquals(31.5, valStr.toDouble(), 0.0001)
        assertEquals("px", resStr.substring(spaceIdx + 1))
    }

    @Test
    fun `unit conversion function syntax`() = runBlocking {
        val lines = listOf(
            createLine("convert(10, \"km\", \"m\")", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10000.0 m", result[0].result)
    }

    @Test
    fun `mixed unit addition picking smaller unit`() = runBlocking {
        val lines = listOf(
            createLine("53 weeks + 2 days", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("373.0 d", result[0].result)
    }

    @Test
    fun `add scalar inherits unit`() = runBlocking {
        val lines = listOf(
            createLine("53 weeks", sortOrder = 0),
            createLine("last + 3", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("53.0 wk", result[0].result)
        assertEquals("56.0 wk", result[1].result)
    }

    @Test
    fun `reassigning to unit symbol is disallowed`() = runBlocking {
        val lines = listOf(createLine("km = 5", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)

        val error = MathEngine.getErrorDetails(lines, 0)
        assertEquals("`km` is a unit symbol and cannot be used as a variable name", error)
    }

    @Test
    fun `convert function performs full conversion from base`() = runBlocking {
        val lines = listOf(createLine("convert(10, \"km\", \"cm\")", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("1000000.0 cm", result[0].result)
    }

    @Test
    fun `unit conversion expression performs full conversion from base`() = runBlocking {
        val lines = listOf(createLine("10 km in cm", sortOrder = 0))
        val result = MathEngine.calculate(lines)
        assertEquals("1000000.0 cm", result[0].result)
    }

    @Test
    fun `unit conversion error incompatible dimensions`() = runBlocking {
        val lines = listOf(
            createLine("10 km in kg", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0)
        assertTrue(err?.contains("dimension mismatch") == true)
    }

    @Test
    fun `unit conversion multi word alias`() = runBlocking {
        val lines = listOf(
            createLine("10 degree celsius in fahrenheit", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("50.0 °F", result[0].result)
    }

    @Test
    fun `standalone quantity preserves unit in result`() = runBlocking {
        val lines = listOf(
            createLine("10 kg", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10.0 kg", result[0].result)
    }

    @Test
    fun `quantity can be stripped to unitless value`() = runBlocking {
        val lines = listOf(
            createLine("area = 4.20", sortOrder = 0),
            createLine("cost = area * 4.2 crores", sortOrder = 1),
            createLine("saleable = area * (1 acre as sqft) * 70%", sortOrder = 2),
            createLine("revenue = saleable * 5000", sortOrder = 3),
            createLine("value(10 kg)", sortOrder = 4),
            createLine("dropUnit(10 kg)", sortOrder = 5),
            createLine("raw(10 kg)", sortOrder = 6),
            createLine("value(128066.6 sqft)", sortOrder = 7),
            createLine("value(12 million)", sortOrder = 8),
            createLine("value(12 kg)", sortOrder = 9),
            createLine("value(128066.6 sqft) - 1", sortOrder = 10),
            createLine("value(12 kg) - 1 kg", sortOrder = 11),
            createLine("value(revenue) - cost", sortOrder = 12),
        )
        val result = MathEngine.calculate(lines)
        assertEquals("4.2", result[0].result)
        assertTrue(result[1].result.startsWith("1.764E8"))
        assertTrue(result[2].result.startsWith("128066.4"))
        assertTrue(result[3].result.startsWith("6.40332"))
        assertTrue(result[3].result.endsWith(" ft²"))
        assertEquals("10", result[4].result)
        assertEquals("10", result[5].result)
        assertEquals("10", result[6].result)
        assertTrue(result[7].result.startsWith("128066.6"))
        assertEquals("12000000", result[8].result)
        assertEquals("12", result[9].result)
        assertTrue(result[10].result.startsWith("128065.6"))
        assertEquals("11.0", result[11].result)
    }

    @Test
    fun `unit conversion calculation chain works as expected between different units by dropping units`() = runBlocking {
        val lines = listOf(
            createLine("area = 4.20", sortOrder = 0),
            createLine("cost = area * 4.2 crores", sortOrder = 1),
            createLine("saleable = area * (1 acre as sqft) * 70%", sortOrder = 2),
            createLine("revenue = saleable * 5000", sortOrder = 3),
            createLine("value(revenue) - cost", sortOrder = 4),
        )
        val result = MathEngine.calculate(lines)
        assertEquals("4.2", result[0].result)
        assertTrue(result[1].result.startsWith("1.764E8"))
        assertTrue(result[2].result.startsWith("128066.4"))
        assertEquals(640332000.0, result[3].result.split(" ")[0].toDouble(), 0.1)
        assertTrue(result[3].result.endsWith(" ft²"))
        assertEquals(463932000.0, result[4].result.toDouble(), 0.01)
    }

    @Test
    fun `unit stripping helpers cannot be reassigned`() = runBlocking {
        val lines = listOf(
            createLine("value = 1"),
            createLine("dropUnit = 1"),
            createLine("raw = 1")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err0 = MathEngine.getErrorDetails(lines, 0)
        assertTrue(err0?.contains("`value` is a reserved name and cannot be changed") == true)
        assertEquals("Err", result[1].result)
        val err1 = MathEngine.getErrorDetails(lines, 1)
        assertTrue(err1?.contains("`dropUnit` is a reserved name and cannot be changed") == true)
        assertEquals("Err", result[2].result)
        val err2 = MathEngine.getErrorDetails(lines, 2)
        assertTrue(err2?.contains("`raw` is a reserved name and cannot be changed") == true)
    }

    @Test
    fun `unitless conversion to non-scalar returns Err`() = runBlocking {
        val lines = listOf(createLine("5 as cm"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        val err = MathEngine.getErrorDetails(lines, 0)
        assertTrue(err?.contains("Cannot convert unitless number to `Centimeter`") == true)
    }

    @Test
    fun `compound assignment is quantity aware`() = runBlocking {
        val lines = listOf(
            createLine("distance = 10 km"),
            createLine("distance += 1")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("11.0 km", result[1].result)
    }

    @Test
    fun `increment is quantity aware`() = runBlocking {
        val lines = listOf(
            createLine("distance = 10 km"),
            createLine("distance++")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("11.0 km", result[1].result)
    }

    @Test
    fun `fractional numeral system conversion throws error`() = runBlocking {
        val lines = listOf(createLine("10.4 in binary"))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertEquals("Fractional value cannot be converted to numeral system", err)
    }

    @Test
    fun `formatDisplayResult honors precision`() {
        val input = "0.33333333333333335"
        assertEquals("0.33", MathEngine.formatDisplayResult(input, 2))
        assertEquals("0.3333", MathEngine.formatDisplayResult(input, 4))
        assertEquals("0.333333", MathEngine.formatDisplayResult(input, 6))
    }

    @Test
    fun `formatDisplayResult handles quantities with precision`() {
        val input = "0.33333333333333335 km"
        assertEquals("0.33 km", MathEngine.formatDisplayResult(input, 2))
        assertEquals("0.3333 km", MathEngine.formatDisplayResult(input, 4))
    }

    @Test
    fun `formatDisplayResult handles large numbers with precision`() {
        val input = "12345678901234567890"
        // Should use scientific notation with precision
        val formatted = MathEngine.formatDisplayResult(input, 2)
        assertTrue(formatted.contains("e"))
        assertTrue(formatted.startsWith("1.23"))
    }

    @Test
    fun `integers should always include a trailing zero`() {
        assertEquals("0.0", MathEngine.formatBigDecimal(BigDecimal.ZERO))
        assertEquals("10.0", MathEngine.formatBigDecimal(java.math.BigDecimal("10")))
        assertEquals("10.0", MathEngine.formatBigDecimal(java.math.BigDecimal("10.000")))
        assertEquals("-5.0", MathEngine.formatBigDecimal(java.math.BigDecimal("-5")))
    }

    @Test
    fun `decimals should preserve precision without extra zeros`() {
        assertEquals("3.14", MathEngine.formatBigDecimal(java.math.BigDecimal("3.14")))
        assertEquals("0.123456789", MathEngine.formatBigDecimal(java.math.BigDecimal("0.123456789000")))
    }

    @Test
    fun `large numbers should use scientific notation`() {
        // Threshold is 10^7 (10,000,000)
        assertEquals("9999999.0", MathEngine.formatBigDecimal(java.math.BigDecimal("9999999")))
        assertEquals("1.0E7", MathEngine.formatBigDecimal(java.math.BigDecimal("10000000")))
        assertEquals("1.2345678E7", MathEngine.formatBigDecimal(java.math.BigDecimal("12345678")))
        assertEquals("1.0E100", MathEngine.formatBigDecimal(java.math.BigDecimal("10").pow(100)))
    }

    @Test
    fun `very small numbers should use scientific notation`() {
        // Threshold is 10^-3 (0.001)
        assertEquals("0.001", MathEngine.formatBigDecimal(java.math.BigDecimal("0.001")))
        assertEquals("1.0E-4", MathEngine.formatBigDecimal(java.math.BigDecimal("0.0001")))
        assertEquals("1.23E-7", MathEngine.formatBigDecimal(java.math.BigDecimal("0.000000123")))
    }

    @Test
    fun `scientific notation should preserve precision`() {
        val highPrec = java.math.BigDecimal("1.234567890123456789012345678901234")
        val large = highPrec.multiply(java.math.BigDecimal("10").pow(20))
        // 1.23456...E20
        assertEquals("1.234567890123456789012345678901234E20", MathEngine.formatBigDecimal(large))

        val small = highPrec.divide(java.math.BigDecimal("10").pow(20), java.math.MathContext.DECIMAL128)
        // 1.23456...E-20
        assertEquals("1.234567890123456789012345678901234E-20", MathEngine.formatBigDecimal(small))
    }

    @Test
    fun `negative numbers in scientific notation`() {
        assertEquals("-1.0E7", MathEngine.formatBigDecimal(java.math.BigDecimal("-10000000")))
        assertEquals("-1.0E-4", MathEngine.formatBigDecimal(java.math.BigDecimal("-0.0001")))
    }
}
