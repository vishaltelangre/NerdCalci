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
    fun `variable with underscores in name`() {
        val lines = listOf(
            createLine("monthly_salary = 5000", sortOrder = 0),
            createLine("monthly_salary * 12", sortOrder = 1),
            createLine("monthly_salary", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5000", result[0].result)
        assertEquals("60000", result[1].result)
        assertEquals("5000", result[2].result)
    }

    @Test
    fun `variable with underscores in percentage expressions`() {
        val lines =
                listOf(
                        createLine("rate = 10", sortOrder = 0),
                        createLine("rate_with_disc = 10% off rate", sortOrder = 1),
                        createLine("rate_with_disc", sortOrder = 2)
                )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("9", result[1].result)
        assertEquals("9", result[2].result)
    }

    @Test
    fun `undefined variable returns error not implicit multiplication`() {
        // rate2 (without underscore) is not defined, should error out instead of being parsed as rate * 2
        val lines = listOf(
            createLine("rate = 10", sortOrder = 0),
            createLine("rate2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("Err", result[1].result)
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

    @Test
    fun `invalid variable name with spaces returns error`() {
        val lines = listOf(createLine("rate with disc = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `invalid variable name starting with digit returns error`() {
        val lines = listOf(createLine("2rate = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `invalid variable name with special characters returns error`() {
        val lines = listOf(createLine("rate-disc = 10"))
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
    }

    @Test
    fun `valid variable names with underscores work`() {
        val lines = listOf(
            createLine("rate_with_disc = 10", sortOrder = 0),
            createLine("rate_2 = 11", sortOrder = 1),
            createLine("_private2 = 5", sortOrder = 2),
            createLine("__internal__ = 3", sortOrder = 3),
            createLine("_private2 + __internal__", sortOrder = 4)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("11", result[1].result)
        assertEquals("5", result[2].result)
        assertEquals("3", result[3].result)
        assertEquals("8", result[4].result)
    }

    // Built-in Functions Tests

    @Test
    fun `trigonometric functions work`() {
        val lines = listOf(
            createLine("sin(0)", sortOrder = 0),
            createLine("cos(0)", sortOrder = 1),
            createLine("tan(0)", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0", result[0].result)
        assertEquals("1", result[1].result)
        assertEquals("0", result[2].result)
    }

    @Test
    fun `inverse trigonometric functions work`() {
        val lines = listOf(
            createLine("asin(0)", sortOrder = 0),
            createLine("acos(1)", sortOrder = 1),
            createLine("atan(0)", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0", result[0].result)
        assertEquals("0", result[1].result)
        assertEquals("0", result[2].result)
    }

    @Test
    fun `logarithm functions work`() {
        val lines = listOf(
            createLine("log10(1000)", sortOrder = 0),
            createLine("log2(8)", sortOrder = 1),
            createLine("log(E)", sortOrder = 2)  // Natural log of E should be 1
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3", result[0].result)
        assertEquals("3", result[1].result)
        assertEquals("1", result[2].result)
    }

    @Test
    fun `power and root functions work`() {
        val lines = listOf(
            createLine("sqrt(16)", sortOrder = 0),
            createLine("cbrt(27)", sortOrder = 1),
            createLine("pow(2, 8)", sortOrder = 2),
            createLine("exp(0)", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("4", result[0].result)
        assertEquals("3", result[1].result)
        assertEquals("256", result[2].result)
        assertEquals("1", result[3].result)
    }

    @Test
    fun `rounding functions work`() {
        val lines = listOf(
            createLine("abs(-42)", sortOrder = 0),
            createLine("floor(3.7)", sortOrder = 1),
            createLine("ceil(3.2)", sortOrder = 2),
            createLine("signum(-5)", sortOrder = 3),
            createLine("signum(0)", sortOrder = 4),
            createLine("signum(5)", sortOrder = 5)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("42", result[0].result)
        assertEquals("3", result[1].result)
        assertEquals("4", result[2].result)
        assertEquals("-1", result[3].result)
        assertEquals("0", result[4].result)
        assertEquals("1", result[5].result)
    }

    @Test
    fun `constants work`() {
        val lines = listOf(
            createLine("PI", sortOrder = 0),
            createLine("E", sortOrder = 1),
            createLine("PI * 2", sortOrder = 2),
            createLine("E + 1", sortOrder = 3)
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
    }

    @Test
    fun `functions can be used with variables`() {
        val lines = listOf(
            createLine("radius = 5", sortOrder = 0),
            createLine("area = PI * pow(radius, 2)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
        // Area should be approximately 78.54
        assertTrue(result[1].result.toDouble() > 78 && result[1].result.toDouble() < 79)
    }

    @Test
    fun `nested functions work`() {
        val lines = listOf(
            createLine("sqrt(pow(3, 2) + pow(4, 2))", sortOrder = 0),
            createLine("abs(sin(0) - 1)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result) // Pythagorean theorem: sqrt(9 + 16) = 5
        assertEquals("1", result[1].result) // abs(0 - 1) = 1
    }

    @Test
    fun `functions are case sensitive`() {
        val lines = listOf(
            createLine("SQRT(16)", sortOrder = 0),
            createLine("SIN(0)", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("Err", result[0].result)
        assertEquals("Err", result[1].result)
    }

    @Test
    fun `functions require parentheses`() {
        val lines = listOf(
            createLine("floor(3.7)", sortOrder = 0),
            createLine("floor 3.7", sortOrder = 1),
            createLine("sqrt(16)", sortOrder = 2),
            createLine("sqrt 16", sortOrder = 3),
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3", result[0].result)
        assertEquals("Err", result[1].result)
        assertEquals("4", result[2].result)
        assertEquals("Err", result[3].result)
    }

    @Test
    fun `increment operator increases variable by 1`() {
        val lines = listOf(
            createLine("count = 5", sortOrder = 0),
            createLine("count++", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
        assertEquals("6", result[1].result)
    }

    @Test
    fun `decrement operator decreases variable by 1`() {
        val lines = listOf(
            createLine("count = 5", sortOrder = 0),
            createLine("count--", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("5", result[0].result)
        assertEquals("4", result[1].result)
    }

    @Test
    fun `compound addition assignment`() {
        val lines = listOf(
            createLine("total = 10", sortOrder = 0),
            createLine("total += 5 + 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("17", result[1].result) // 10 + (5 + 2)
    }

    @Test
    fun `compound subtraction assignment`() {
        val lines = listOf(
            createLine("total = 20", sortOrder = 0),
            createLine("total -= 5 * 2", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("20", result[0].result)
        assertEquals("10", result[1].result) // 20 - (5 * 2)
    }

    @Test
    fun `compound multiplication assignment`() {
        val lines = listOf(
            createLine("factor = 3", sortOrder = 0),
            createLine("factor *= 2 + 1", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("3", result[0].result)
        assertEquals("9", result[1].result) // 3 * (2 + 1)
    }

    @Test
    fun `compound division assignment`() {
        val lines = listOf(
            createLine("value = 100", sortOrder = 0),
            createLine("value /= 5", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("100", result[0].result)
        assertEquals("20", result[1].result)
    }

    @Test
    fun `compound modulo (remainder) assignment`() {
        val lines = listOf(
            createLine("value = 10", sortOrder = 0),
            createLine("value %= 3", sortOrder = 1)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("1", result[1].result)
    }

    @Test
    fun `calculateFrom returns only the affected lines starting at changedIndex`() {
        val lines =
                listOf(
                        createLine("1 + 1", sortOrder = 0),
                        createLine("2 + 2", sortOrder = 1),
                        createLine("3 + 3", sortOrder = 2)
                )
        val result = MathEngine.calculateFrom(lines, changedIndex = 1)
        // Should return lines[1..2] only (2 lines)
        assertEquals(2, result.size)
        assertEquals("4", result[0].result)
        assertEquals("6", result[1].result)
    }

    @Test
    fun `calculateFrom respects variables defined in preceding lines`() {
        val lines =
                listOf(
                        createLine("price = 100", sortOrder = 0), // preceding (not recalculated)
                        createLine("tax = 10", sortOrder = 1), // preceding (not recalculated)
                        createLine("price + tax", sortOrder = 2) // affected — must see both vars
                )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("110", result[0].result)
    }

    @Test
    fun `calculateFrom with changedIndex 0 is equivalent to full calculate`() {
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
    fun `calculateFrom propagates variable reassignment from preceding lines to affected lines`() {
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
        assertEquals("10", result[0].result) // x = 10
        assertEquals("20", result[1].result) // x * 2 = 20
    }

    @Test
    fun `calculateFrom clamps out-of-bounds changedIndex gracefully`() {
        val lines = listOf(
                            createLine("5 + 5", sortOrder = 0),
                           createLine("2 * 3", sortOrder = 1)
                    )
        // changedIndex beyond list size — should return empty (nothing to recalculate)
        val result = MathEngine.calculateFrom(lines, changedIndex = 100)
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateFrom clamps negative changedIndex to full recalculation`() {
        val lines = listOf(
            createLine("a = 5", sortOrder = 0),
            createLine("a * 2", sortOrder = 1)
        )
        // Negative index should clamp to 0 and recalculate everything
        val result = MathEngine.calculateFrom(lines, changedIndex = -99)
        assertEquals(2, result.size)
        assertEquals("5", result[0].result)
        assertEquals("10", result[1].result)
    }

    @Test
    fun `total sums all results above in same block`() {
        val lines = listOf(
            createLine("4 / 2", sortOrder = 0),
            createLine("b = 2", sortOrder = 1),
            createLine("a = 4", sortOrder = 2),
            createLine("total", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("2", result[0].result)
        assertEquals("2", result[1].result)
        assertEquals("4", result[2].result)
        assertEquals("8", result[3].result)
    }

    @Test
    fun `sum is an alias for total`() {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("sum", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("30", result[2].result)
    }

    @Test
    fun `blank line resets the block for total`() {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("total", sortOrder = 2),
            createLine("", sortOrder = 3),
            createLine("c = 5", sortOrder = 4),
            createLine("total", sortOrder = 5)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("30", result[2].result)  // 10 + 20
        assertEquals("", result[3].result)
        assertEquals("5", result[4].result)
        assertEquals("5", result[5].result)   // only c = 5 in this block
    }

    @Test
    fun `comment-only lines do not contribute to total`() {
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
        assertEquals("20", result[3].result)
    }

    @Test
    fun `total used in an expression`() {
        val lines = listOf(
            createLine("item1 = 25", sortOrder = 0),
            createLine("item2 = 75", sortOrder = 1),
            createLine("tax = total * 0.10", sortOrder = 2)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("25", result[0].result)
        assertEquals("75", result[1].result)
        assertEquals("10", result[2].result)
    }

    @Test
    fun `total assignment overrides aggregate meaning`() {
        val lines = listOf(
            createLine("10", sortOrder = 0),
            createLine("20", sortOrder = 1),
            createLine("total = 4", sortOrder = 2),
            createLine("total / 2", sortOrder = 3)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("10", result[0].result)
        assertEquals("20", result[1].result)
        assertEquals("4", result[2].result)
        assertEquals("2", result[3].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total with no preceding results is 0`() {
        val lines = listOf(
            createLine("total", sortOrder = 0)
        )
        val result = MathEngine.calculate(lines)
        assertEquals("0", result[0].result)
    }

    @Test
    fun `calculateFrom correctly handles total in affected lines`() {
        val lines = listOf(
            createLine("a = 10", sortOrder = 0),
            createLine("b = 20", sortOrder = 1),
            createLine("total", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("30", result[0].result)
    }

    @Test
    fun `total includes its own block results across calculateFrom boundary`() {
        val lines = listOf(
            createLine("5", sortOrder = 0),
            createLine("15", sortOrder = 1),
            createLine("total * 2", sortOrder = 2)
        )
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("40", result[0].result)
    }
}
