package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Locale

class MathEngineTest {

    private val indianSettings = "IN"
    private val defaultSettings = "US" // Explicitly test generic grouping

    @Test
    fun `formatDisplayResult supports Indian style grouping`() {
        val locale = Locale.US

        // 1 hundred
        assertEquals("100", MathEngine.formatDisplayResult("100", 2, locale, indianSettings))

        // 1 thousand
        assertEquals("1,000", MathEngine.formatDisplayResult("1000", 2, locale, indianSettings))

        // 10 thousand
        assertEquals("10,000", MathEngine.formatDisplayResult("10000", 2, locale, indianSettings))

        // 1 lakh
        assertEquals("1,00,000", MathEngine.formatDisplayResult("100000", 2, locale, indianSettings))

        // 10 lakh
        assertEquals("10,00,000", MathEngine.formatDisplayResult("1000000", 2, locale, indianSettings))

        // 1 crore
        assertEquals("1,00,00,000", MathEngine.formatDisplayResult("10000000", 2, locale, indianSettings))

        // 10 crore
        assertEquals("10,00,00,000", MathEngine.formatDisplayResult("100000000", 2, locale, indianSettings))

        // 100 crore
        assertEquals("1,00,00,00,000", MathEngine.formatDisplayResult("1000000000", 2, locale, indianSettings))

        // 1,000 crore
        assertEquals("10,00,00,00,000", MathEngine.formatDisplayResult("10000000000", 2, locale, indianSettings))

        // 1 lakh crore
        assertEquals("10,00,00,00,00,000", MathEngine.formatDisplayResult("1000000000000", 2, locale, indianSettings))

        // Mixed digits
        assertEquals("12,34,567.89", MathEngine.formatDisplayResult("1234567.89", 2, locale, indianSettings))
    }

    @Test
    fun `formatDisplayResult respects useIndianStyle flag`() {
        val locale = Locale.US
        val value = "1000000"
        assertEquals("1,000,000", MathEngine.formatDisplayResult(value, 2, locale, defaultSettings))
        assertEquals("10,00,000", MathEngine.formatDisplayResult(value, 2, locale, indianSettings))
    }

    @Test
    fun `basic addition returns correct result`() {
        testCalculate("2 + 2") { result ->
            assertEquals("4.0", result[0].result)
        }
    }

    @Test
    fun `basic subtraction returns correct result`() {
        testCalculate("10 - 3") { result ->
            assertEquals("7.0", result[0].result)
        }
    }

    @Test
    fun `basic multiplication returns correct result`() {
        testCalculate("5 * 6") { result ->
            assertEquals("30.0", result[0].result)
        }
    }

    @Test
    fun `basic division returns correct result`() {
        testCalculate("20 / 4") { result ->
            assertEquals("5.0", result[0].result)
        }
    }

    @Test
    fun `basic modulo returns correct result`() {
        testCalculate("10 % 3") { result ->
            assertEquals("1.0", result[0].result)
        }
    }

    @Test
    fun `modulo without spaces returns correct result`() {
        testCalculate("10%3") { result ->
            assertEquals("1.0", result[0].result)
        }
    }

    @Test
    fun `total throws dimension mismatch error for mixed units`() {
        testCalculate("4kg", "5 hour", "3 kph", "total") { result ->
            assertError("Cannot sum Kilogram and Hour: dimension mismatch", result, 3)
        }
    }

    @Test
    fun `chained percentage expression works correctly`() {
        testCalculate("100 + 20% - 5") { result ->
            assertEquals("115.0", result[0].result)
        }
    }

    @Test
    fun `complex expression with multiple operators`() {
        testCalculate("2 + 3 * 4 - 1") { result ->
            assertEquals("13.0", result[0].result)
        }
    }

    @Test
    fun `expression with parentheses respects order of operations`() {
        testCalculate("(2 + 3) * 4") { result ->
            assertEquals("20.0", result[0].result)
        }
    }

    @Test
    fun `exponentiation works correctly`() {
        testCalculate("2 ^ 3") { result ->
            assertEquals("8.0", result[0].result)
        }
    }

    @Test
    fun `multiplication sign × is normalized to asterisk`() {
        testCalculate("5 × 6") { result ->
            assertEquals("30.0", result[0].result)
        }
    }

    @Test
    fun `division sign ÷ is normalized to slash`() {
        testCalculate("20 ÷ 4") { result ->
            assertEquals("5.0", result[0].result)
        }
    }

    @Test
    fun `temperature multiplication uses displayed unit value`() {
        testCalculate("30 °C * 2", "2 * 30 °C", "30 °F * 2", "2 * 30 K") { result ->
            assertEquals("60.0 °C", result[0].result)
            assertEquals("60.0 °C", result[1].result)
            val fahrenheitValue = result[2].result.substringBefore(' ').toDouble()
            assertEquals(60.0, fahrenheitValue, 1e-9)
            assertTrue(result[2].result.endsWith(" °F"))
            assertEquals("60.0 K", result[3].result)
        }
    }

    @Test
    fun `temperature division uses displayed unit value`() {
        testCalculate("30 °C / 2", "60 °C / 2", "30 °F / 2", "30 K / 2") { result ->
            assertEquals("15.0 °C", result[0].result)
            assertEquals("30.0 °C", result[1].result)
            assertEquals("15.0 °F", result[2].result)
            assertEquals("15.0 K", result[3].result)
        }
    }

    @Test
    fun `temperature addition is order independent`() {
        testCalculate(
            "30 °F + 30 °C + 30 °C",
            "30 °C + 30 °F + 30 °C",
            "30 °C + 30 °C + 30 °F"
        ) { result ->
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
    }

    @Test
    fun `em conversion with custom variable`() {
        testCalculate("em = 20", "2 em in px") { result ->
            assertTrue(result[1].result.startsWith("40.0"))
        }
    }

    @Test
    fun `pixel conversion with custom ppi variable`() {
        testCalculate("ppi = 120", "10 px in inches") { result ->
            val resultStr = result[1].result
            val spaceIndex = resultStr.indexOf(' ')
            assertTrue(spaceIndex > 0)

            val value = resultStr.substring(0, spaceIndex).toDouble()
            val unit = resultStr.substring(spaceIndex + 1)

            assertEquals(0.083333333333, value, 1e-6)
            assertEquals("inch", unit)
        }
    }

    @Test
    fun `trigonometric functions support degree inputs`() = testCalculate(
        "sin(90°)",
        "sin(30 deg)",
        "cos(60 degree)",
        "sin(90)" // defaults to radians
    ) { result ->
        assertEquals(1.0, result[0].result.toDouble(), 1e-9)
        assertEquals(0.5, result[1].result.toDouble(), 1e-9)
        assertEquals(0.5, result[2].result.toDouble(), 1e-9)
        assertEquals(0.8939966636005579, result[3].result.toDouble(), 1e-9)
    }

    @Test
    fun `mixed unicode and ASCII operators work together`() = testCalculate("10 × 2 ÷ 4 + 1") { result ->
        assertEquals("6.0", result[0].result)
    }

    @Test
    fun `numeral system multipliers evaluate correctly`() = testCalculate(
        "5 thousand",
        "2.5 million",
        "1.5 crore",
        "2 lakh + 5 thousand",
        "50% of 1 lakh",
        "1.5 hundred",
        "1 billion",
        "1 trillion",
        "4500 million in crores",
        "2 lakh to thousand",
        "5 thousand meters to km",
        "10 thousand in hex",
        "10 hex + 10 hex",
        "20 hex - 10 hex"
    ) { result ->
        assertEquals("5000.0", result[0].result)
        assertEquals("2500000.0", result[1].result)
        assertEquals("15000000.0", result[2].result)
        assertEquals("205000.0", result[3].result)
        assertEquals("50000.0", result[4].result)
        assertEquals("150.0", result[5].result)
        assertEquals("1000000000.0", result[6].result)
        assertEquals("1000000000000.0", result[7].result)
        assertEquals("450.0 crore", result[8].result)
        assertEquals("200.0 thousand", result[9].result)
        assertEquals("5.0 km", result[10].result)
        assertEquals("0x2710", result[11].result)
        assertEquals("0x140", result[12].result)
        assertEquals("0xA0", result[13].result)
    }

    @Test
    fun `decimal addition returns formatted result`() = testCalculate("1.5 + 2.3") { result ->
        assertEquals("3.8", result[0].result)
    }

    @Test
    fun `decimal division returns raw double precision`() = testCalculate("10 / 3") { result ->
        assertEquals("3.333333333333333333333333333333333", result[0].result)
    }

    @Test
    fun `result with no decimal part shows as double with trailing zero`() = testCalculate("5.0 + 5.0") { result ->
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `simple variable assignment stores value`() = testCalculate("price = 100", "price") { result ->
        assertEquals("100.0", result[0].result)
        assertEquals("100.0", result[1].result)
    }

    @Test
    fun `variable can be used in calculations`() = testCalculate("price = 100", "price * 2") { result ->
        assertEquals("100.0", result[0].result)
        assertEquals("200.0", result[1].result)
    }

    @Test
    fun `multiple variables work together`() = testCalculate("a = 10", "b = 20", "a + b") { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("30.0", result[2].result)
    }

    @Test
    fun `variable reassignment updates value`() = testCalculate("x = 5", "x * 2", "x = 10", "x * 2") { result ->
        assertEquals("5.0", result[0].result)
        assertEquals("10.0", result[1].result)
        assertEquals("10.0", result[2].result)
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `variable with underscores in name`() = testCalculate("monthly_salary = 5000", "monthly_salary * 12", "monthly_salary") { result ->
        assertEquals("5000.0", result[0].result)
        assertEquals("60000.0", result[1].result)
        assertEquals("5000.0", result[2].result)
    }

    @Test
    fun `variable with underscores in percentage expressions`() = testCalculate("rate = 10", "rate_with_disc = 10% off rate", "rate_with_disc") { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("9.0", result[1].result)
        assertEquals("9.0", result[2].result)
    }

    @Test
    fun `undefined variable returns error not implicit multiplication`() = testCalculate("rate = 10", "rate2") { result ->
        // rate2 (without underscore) is not defined, should error out instead of being parsed as rate * 2
        assertEquals("10.0", result[0].result)
        assertError("Unknown variable `rate2`", result, 1)
    }

    @Test
    fun `variable assignment with expression`() = testCalculate("total = 10 + 20 + 30", "total / 3") { result ->
        assertEquals("60.0", result[0].result)
        assertEquals("20.0", result[1].result)
    }

    @Test
    fun `percentage of number works correctly`() = testCalculate("20% of 100") { result ->
        assertEquals("20.0", result[0].result)
    }

    @Test
    fun `percentage of decimal number`() = testCalculate("15.5% of 200") { result ->
        assertEquals("31.0", result[0].result) // Result is whole number
    }

    @Test
    fun `percentage of variable`() = testCalculate("price = 1000", "10% of price") { result ->
        assertEquals("1000.0", result[0].result)
        assertEquals("100.0", result[1].result)
    }

    @Test
    fun `percentage of quantity preserves unit`() = testCalculate("10000g", "10% of _", "_ + 9kg") { result ->
        assertEquals("10000.0 g", result[0].result)
        assertEquals("1000.0 g", result[1].result)
        assertEquals("10000.0 g", result[2].result)
    }

    @Test
    fun `unit cancellation in division returns unitless result`() = testCalculate(
        "10km / 100m",
        "(10km * 10km) / 50sqkm",
        "x = 10km / 100m",
        "x * 2",
        "100kg / 10g",
        "10kg / 2kg",
        "1h / 60min"
    ) { result ->
        assertEquals("100.0", result[0].result)
        assertEquals("2.0", result[1].result)
        assertEquals("100.0", result[2].result)
        assertEquals("200.0", result[3].result)
        assertEquals("10000.0", result[4].result)
        assertEquals("5.0", result[5].result)
        assertEquals("1.0", result[6].result)
    }

    @Test
    fun `non linear same category division returns error`() = testCalculate("20 C / 10 C", "6 l100km / 2 l100km") { result ->
        assertError("unsupported multiplicative unit: Celsius / Celsius", result, 0)
        assertError("unsupported multiplicative unit: Liters per 100 km / Liters per 100 km", result, 1)
    }

    @Test
    fun `percentage off reduces value`() = testCalculate("20% off 100") { result ->
        assertEquals("80.0", result[0].result)
    }

    @Test
    fun `percentage off with decimal`() = testCalculate("25% off 80") { result ->
        assertEquals("60.0", result[0].result)
    }

    @Test
    fun `percentage off variable`() = testCalculate("original = 500", "30% off original") { result ->
        assertEquals("500.0", result[0].result)
        assertEquals("350.0", result[1].result)
    }

    @Test
    fun `add percentage to number`() = testCalculate("100 + 20%") { result ->
        assertEquals("120.0", result[0].result)
    }

    @Test
    fun `add percentage to variable`() = testCalculate("salary = 50000", "salary + 10%") { result ->
        assertEquals("50000.0", result[0].result)
        assertEquals("55000.0", result[1].result)
    }

    @Test
    fun `subtract percentage from number`() = testCalculate("100 - 15%") { result ->
        assertEquals("85.0", result[0].result)
    }

    @Test
    fun `subtract percentage from variable`() = testCalculate("budget = 1000", "budget - 25%") { result ->
        assertEquals("1000.0", result[0].result)
        assertEquals("750.0", result[1].result)
    }

    @Test
    fun `expression with inline comment returns result`() = testCalculate("10 + 5 # adding numbers") { result ->
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `full line comment returns empty result`() = testCalculate("# This is just a comment") { result ->
        assertEquals("", result[0].result)
    }

    @Test
    fun `comment with special characters is ignored`() = testCalculate("20 * 2 # result should be 40!") { result ->
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `hash symbol in middle of expression is treated as comment`() = testCalculate("5 + 5 # + 10") { result ->
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `empty expression returns empty result`() = testCalculate("") { result ->
        assertEquals("", result[0].result)
    }

    @Test
    fun `blank expression with spaces returns empty result`() = testCalculate("   ") { result ->
        assertEquals("", result[0].result)
    }

    @Test
    fun `expression with only comment and spaces returns empty result`() = testCalculate("   # just a comment") { result ->
        assertEquals("", result[0].result)
    }

    @Test
    fun `invalid expression returns Err`() = testCalculate("2 + * 2") { result ->
        assertError("Expected a value or `(`, but found `*`", result, 0)
    }

    @Test
    fun `division by zero returns Err`() = testCalculate("10 / 0") { result ->
        assertError("Cannot divide by zero", result, 0)
    }

    @Test
    fun `undefined variable returns Err`() = testCalculate("unknownVar * 2") { result ->
        assertError("Unknown variable `unknownVar`", result, 0)
    }

    @Test
    fun `malformed parentheses returns Err`() = testCalculate("(2 + 3") { result ->
        assertError("Expected `)`, but found `end of line`", result, 0)
    }

    @Test
    fun `complex calculation with variables and percentages`() = testCalculate(
        "basePrice = 1000",
        "discount = 15% of basePrice",
        "discountedPrice = basePrice - discount",
        "tax = 10% of discountedPrice",
        "final = discountedPrice + tax"
    ) { result ->
        assertEquals("1000.0", result[0].result)
        assertEquals("150.0", result[1].result)
        assertEquals("850.0", result[2].result)
        assertEquals("85.0", result[3].result)
        assertEquals("935.0", result[4].result)
    }

    @Test
    fun `multi-line with comments and calculations`() = testCalculate(
        "# Monthly budget calculation",
        "income = 5000",
        "rent = 1200 # apartment",
        "utilities = 300",
        "remaining = income - rent - utilities"
    ) { result ->
        assertEquals("", result[0].result)
        assertEquals("5000.0", result[1].result)
        assertEquals("1200.0", result[2].result)
        assertEquals("300.0", result[3].result)
        assertEquals("3500.0", result[4].result)
    }

    @Test
    fun `variable dependency chain calculates correctly`() = testCalculate(
        "a = 10",
        "b = a * 2",
        "c = b + a",
        "d = c / a"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("30.0", result[2].result)
        assertEquals("3.0", result[3].result)
    }

    @Test
    fun `mixed valid and invalid lines process independently`() = testCalculate(
        "5 + 5",
        "invalid ++",
        "10 * 2"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertError("Unknown variable `invalid`", result[1], result, 1)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `large integer within Long range is returned correctly`() = testCalculate("1000000000 * 2") { result ->
        assertEquals("2000000000.0", result[0].result)
    }

    @Test
    fun `very large number uses scientific notation`() = testCalculate("999999999999999 * 999999999999999") { result ->
        // Should be in scientific notation format
        assertTrue(result[0].result.contains("e") || result[0].result.length > 15)
    }

    @Test
    fun `single number evaluates to itself`() = testCalculate("42") { result ->
        assertEquals("42.0", result[0].result)
    }

    @Test
    fun `negative numbers work correctly`() = testCalculate("-10 + 5") { result ->
        assertEquals("-5.0", result[0].result)
    }

    @Test
    fun `nested parentheses calculate correctly`() = testCalculate("((2 + 3) * (4 + 5))") { result ->
        assertEquals("45.0", result[0].result)
    }

    @Test
    fun `expression with only whitespace after comment`() = testCalculate("10 + 5 #    ") { result ->
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `zero as result displays as 0`() = testCalculate("5 - 5") { result ->
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `maintains decimal precision correctly`() = testCalculate("1 / 3 * 3") { result ->
        assertEquals("0.9999999999999999999999999999999999", result[0].result) // Result is whole number
    }

    @Test
    fun `high precision arithmetic works for very large numbers`() = testCalculate("(10^100) + 1 - (10^100)") { result ->
        assertEquals("1.0", result[0].result)
    }

    @Test
    fun `variable with underscore in name works`() = testCalculate(
        "my_var = 100",
        "my_var * 2"
    ) { result ->
        assertEquals("100.0", result[0].result)
        assertEquals("200.0", result[1].result)
    }

    @Test
    fun `percentage calculation order matters`() = testCalculate("20% of 100") { result ->
        // 20% of 100 should be 20, not 100% of 20
        assertEquals("20.0", result[0].result)
    }

    @Test
    fun `multiple spaces in expression are handled`() = testCalculate("10    +    20") { result ->
        assertEquals("30.0", result[0].result)
    }

    @Test
    fun `invalid variable name with spaces returns error`() = testCalculate("rate with disc = 10") { result ->
        assertError("Unexpected `identifier`", result, 0)
    }

    @Test
    fun `invalid variable name starting with digit returns error`() = testCalculate("2rate = 10") { result ->
        assertError("Unexpected `identifier`", result, 0)
    }

    @Test
    fun `invalid variable name with special characters returns error`() = testCalculate("rate-disc = 10") {
        assertError("Unexpected `=`", it, 0)
    }

    @Test
    fun `valid variable names with underscores work`() = testCalculate(
        "rate_with_disc = 10",
        "rate_2 = 11",
        "_private2 = 5",
        "__internal__ = 3",
        "1 + 1", // Dummy line for later subtraction testing if needed
        "_private2 + __internal__"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("11.0", result[1].result)
        assertEquals("5.0", result[2].result)
        assertEquals("3.0", result[3].result)
        assertEquals("8.0", result[5].result)
    }

    @Test
    fun `trigonometric functions work`() = testCalculate(
        "sin(0)",
        "cos(0)",
        "tan(0)"
    ) { result ->
        assertEquals("0.0", result[0].result)
        assertEquals("1.0", result[1].result)
        assertEquals("0.0", result[2].result)
    }

    @Test
    fun `inverse trigonometric functions work`() = testCalculate(
        "asin(0)",
        "acos(1)",
        "atan(0)"
    ) { result ->
        assertEquals("0.0", result[0].result)
        assertEquals("0.0", result[1].result)
        assertEquals("0.0", result[2].result)
    }

    @Test
    fun `logarithm functions work`() = testCalculate(
        "log10(1000)",
        "log2(8)",
        "log(E)" // Natural log of E should be 1
    ) { result ->
        assertEquals("3.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("1.0", result[2].result)
    }

    @Test
    fun `power and root functions work`() = testCalculate(
        "sqrt(16)",
        "cbrt(27)",
        "pow(2, 8)",
        "exp(0)"
    ) { result ->
        assertEquals("4.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("256.0", result[2].result)
        assertEquals("1.0", result[3].result)
    }

    @Test
    fun `factorial functions work`() = testCalculate(
        "factorial(0)",
        "factorial(5)",
        "fact(6)"
    ) { result ->
        assertEquals("1.0", result[0].result)
        assertEquals("120.0", result[1].result)
        assertEquals("720.0", result[2].result)
    }

    @Test
    fun `rounding functions work`() = testCalculate(
        "abs(-42)",
        "floor(3.7)",
        "ceil(3.2)",
        "signum(-5)",
        "signum(0)",
        "signum(5)"
    ) { result ->
        assertEquals("42.0", result[0].result)
        assertEquals("3.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("-1.0", result[3].result)
        assertEquals("0.0", result[4].result)
        assertEquals("1.0", result[5].result)
    }

    @Test
    fun `constants work`() = testCalculate(
        "PI",
        "E",
        "PI * 2",
        "E + 1",
        "pi",
        "π",
        "e"
    ) { result ->
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
    fun `functions can be used with variables`() = testCalculate(
        "radius = 5",
        "area = PI * pow(radius, 2)"
    ) { result ->
        assertEquals("5.0", result[0].result)
        // Area should be approximately 78.54
        assertTrue(result[1].result.toDouble() > 78 && result[1].result.toDouble() < 79)
    }

    @Test
    fun `nested functions work`() = testCalculate(
        "sqrt(pow(3, 2) + pow(4, 2))",
        "abs(sin(0) - 1)"
    ) { result ->
        assertEquals("5.0", result[0].result) // Pythagorean theorem: sqrt(9 + 16) = 5
        assertEquals("1.0", result[1].result) // abs(0 - 1) = 1
    }

    @Test
    fun `functions are case sensitive`() = testCalculate(
        "SQRT(16)",
        "SIN(0)"
    ) { result ->
        assertError("Unknown function `SQRT()`", result, 0)
        assertError("Unknown function `SIN()`", result, 1)
    }

    @Test
    fun `functions require parentheses`() = testCalculate(
        "floor(3.7)",
        "floor 3.7",
        "sqrt(16)",
        "sqrt 16"
    ) { result ->
        assertEquals("3.0", result[0].result)
        assertError("Unexpected `number`", result, 1) // floor 3.7
        assertEquals("4.0", result[2].result)
        assertError("Unexpected `number`", result, 3) // sqrt 16
    }

    @Test
    fun `increment operator increases variable by 1`() = testCalculate(
        "count = 5",
        "count++"
    ) { result ->
        assertEquals("5.0", result[0].result)
        assertEquals("6.0", result[1].result)
    }

    @Test
    fun `decrement operator decreases variable by 1`() = testCalculate(
        "count = 5",
        "count--"
    ) { result ->
        assertEquals("5.0", result[0].result)
        assertEquals("4.0", result[1].result)
    }

    @Test
    fun `temperature increment increases displayed value by one degree`() = testCalculate(
        "temp = 30 °C",
        "temp++",
        "temp = 30 °F",
        "temp++",
        "temp = 30 K",
        "temp++"
    ) { result ->
        assertEquals("30.0 °C", result[0].result)
        assertEquals("31.0 °C", result[1].result)
        assertEquals("30.0 °F", result[2].result)
        assertEquals("31.0 °F", result[3].result)
        assertEquals("30.0 K", result[4].result)
        assertEquals("31.0 K", result[5].result)
    }

    @Test
    fun `temperature decrement decreases displayed value by one degree`() = testCalculate(
        "temp = 30 °C",
        "temp--",
        "temp = 30 °F",
        "temp--",
        "temp = 30 K",
        "temp--"
    ) { result ->
        assertEquals("30.0 °C", result[0].result)
        assertEquals("29.0 °C", result[1].result)
        assertEquals("30.0 °F", result[2].result)
        assertEquals("29.0 °F", result[3].result)
        assertEquals("30.0 K", result[4].result)
        assertEquals("29.0 K", result[5].result)
    }

    @Test
    fun `compound addition assignment`() = testCalculate(
        "total = 10",
        "total += 5 + 2"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("17.0", result[1].result) // 10 + (5 + 2)
    }

    @Test
    fun `compound subtraction assignment`() = testCalculate(
        "total = 20",
        "total -= 5 * 2"
    ) { result ->
        assertEquals("20.0", result[0].result)
        assertEquals("10.0", result[1].result) // 20 - (5 * 2)
    }

    @Test
    fun `compound multiplication assignment`() = testCalculate(
        "factor = 3",
        "factor *= 2 + 1"
    ) { result ->
        assertEquals("3.0", result[0].result)
        assertEquals("9.0", result[1].result) // 3 * (2 + 1)
    }

    @Test
    fun `compound division assignment`() = testCalculate(
        "amount = 100",
        "amount /= 5"
    ) { result ->
        assertEquals("100.0", result[0].result)
        assertEquals("20.0", result[1].result)
    }

    @Test
    fun `compound modulo (remainder) assignment`() = testCalculate(
        "amount = 10",
        "amount %= 3"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("1.0", result[1].result)
    }

    @Test
    fun `calculateFrom returns only the affected lines starting at changedIndex`() = testCalculateFrom(
        "1 + 1",
        "2 + 2",
        "3 + 3",
        changedIndex = 1
    ) { result ->
        // Should return lines[1..2] only (2 lines)
        assertEquals(2, result.size)
        assertEquals("4.0", result[0].result)
        assertEquals("6.0", result[1].result)
    }

    @Test
    fun `calculateFrom respects variables defined in preceding lines`() = testCalculateFrom(
        "price = 100",
        "tax = 10",
        "price + tax",
        changedIndex = 2
    ) { result ->
        assertEquals(1, result.size)
        assertEquals("110.0", result[0].result)
    }

    @Test
    fun `local function basic definition and call`() = testCalculate(
        "f(x) = x * 2",
        "f(5)"
    ) { result ->
        assertEquals("", result[0].result) // Function definition produces no output
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `local function multiple parameters`() = testCalculate(
        "calc(a, b) = a + b;",
        "calc(10, 20)"
    ) { result ->
        assertEquals("", result[0].result)
        assertEquals("30.0", result[1].result)
    }

    @Test
    fun `local function multiple statements returns last expression`() = testCalculate(
        "salary(workHours) = base = workHours * 1000; bonus = base * 0.20; tax = base * 0.10; base + bonus - tax",
        "salary(120)"
    ) { result ->
        assertEquals("", result[0].result)
        assertEquals("132000.0", result[1].result)
    }

    @Test
    fun `local function strictly isolates scope`() = testCalculate(
        "v = 10",
        "f(x) = v = x;",
        "f(5)",
        "v" // Should still be 10, not overridden by 'v' inside f
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("", result[1].result)
        assertEquals("5.0", result[2].result)
        assertEquals("10.0", result[3].result) // Outer scope unchanged
    }

    @Test
    fun `local function prevents infinite recursion`() = testCalculate(
        "calc(a) = calc(a);",
        "calc(2)"
    ) { result ->
        assertEquals("", result[0].result)
        assertError("Function `calc()` calls itself too many times which is not allowed", result, 1) // Throws recursive exception
    }

    @Test
    fun `local function with trailing comment does not return 0`() = testCalculate(
        "f(x) = x * 2; # comment",
        "f(5)"
    ) { result ->
        assertEquals("10.0", result[1].result) // Should NOT be "0"
    }

    @Test
    fun `nested function definition in a local function body is not allowed`() = testCalculate(
        "f(x) = g(y) = y; x",
        "f(5)"
    ) { result ->
        assertError("Functions cannot be created inside other functions", result, 0)
        assertError("Unknown function `f()`", result, 1)
    }

    @Test
    fun `semicolons outside function bodies fail parsing`() = testCalculate("x = 10; y = 20") { result ->
        assertError("Unexpected `;`", result, 0) // ParseException
    }

    @Test
    fun `calculateFrom with changedIndex 0 is equivalent to full calculate`() = runBlocking {
        val expressions = listOf("a = 5", "b = a * 2", "a + b")
        val lines = createLines(*expressions.toTypedArray())
        val full = MathEngine.calculate(lines)
        val partial = MathEngine.calculateFrom(lines, changedIndex = 0)
        assertEquals(full.map { it.result }, partial.map { it.result })
    }

    @Test
    fun `calculateFrom propagates variable reassignment from preceding lines to affected lines`() = testCalculateFrom(
        "x = 5",
        "x = 10",
        "x * 2",
        changedIndex = 1
    ) { result ->
        assertEquals(2, result.size)
        assertEquals("10.0", result[0].result) // x = 10
        assertEquals("20.0", result[1].result) // x * 2 = 20
    }

    @Test
    fun `calculateFrom clamps out-of-bounds changedIndex gracefully`() = testCalculateFrom(
        "5 + 5",
        "2 * 3",
        changedIndex = 100
    ) { result ->
        assertEquals(0, result.size)
    }

    @Test
    fun `calculateFrom clamps negative changedIndex to full recalculation`() = testCalculateFrom(
        "a = 5",
        "a * 2",
        changedIndex = -99
    ) { result ->
        assertEquals(2, result.size)
        assertEquals("5.0", result[0].result)
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `total sums all results above in same block`() = testCalculate(
        "4 / 2",
        "b = 2",
        "a = 4",
        "total"
    ) { result ->
        assertEquals("2.0", result[0].result)
        assertEquals("2.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("8.0", result[3].result)
    }

    @Test
    fun `sum is an alias for total`() = testCalculate(
        "10",
        "20",
        "sum"
    ) { result ->
        assertEquals("30.0", result[2].result)
    }

    @Test
    fun `blank line resets the block for total`() = testCalculate(
        "a = 10",
        "b = 20",
        "total",
        "",
        "c = 5",
        "total"
    ) { result ->
        assertEquals("30.0", result[2].result)  // 10 + 20
        assertEquals("", result[3].result)
        assertEquals("5.0", result[4].result)
        assertEquals("5.0", result[5].result)   // only c = 5 in this block
    }

    @Test
    fun `comment-only lines do not contribute to total`() = testCalculate(
        "10",
        "# just a comment",
        "20",
        "total"
    ) { result ->
        // comment-only line produces null → breaks the block
        // so total only sees 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `total used in an expression`() = testCalculate(
        "item1 = 25",
        "item2 = 75",
        "tax = total * 0.10"
    ) { result ->
        assertEquals("25.0", result[0].result)
        assertEquals("75.0", result[1].result)
        assertEquals("10.0", result[2].result)
    }

    @Test
    fun `total assignment overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "total = 4",
        "total / 2"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("4.0", result[2].result)
        assertEquals("2.0", result[3].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total increment overrides aggregate meaning`() = testCalculate(
        "10",
        "total++",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("11.0", result[1].result)  // total was 10, incremented and assigned to 11
        assertEquals("11.0", result[2].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total decrement overrides aggregate meaning`() = testCalculate(
        "10",
        "total--",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("9.0", result[1].result)   // total was 10, decremented to 9
        assertEquals("9.0", result[2].result)   // uses assigned value, not aggregate
    }

    @Test
    fun `total += overrides aggregate meaning`() = testCalculate(
        "10",
        "total += 5",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("15.0", result[1].result)
        assertEquals("15.0", result[2].result)  // uses assigned value, not aggregate
    }

    @Test
    fun `total -= overrides aggregate meaning`() = testCalculate(
        "10",
        "total -= 3",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("7.0", result[1].result)
        assertEquals("7.0", result[2].result)
    }

    @Test
    fun `total multiply-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "total *= 2",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `total divide-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "total /= 2",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("5.0", result[1].result)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `total modulo-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "total %= 7",
        "total"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("2.0", result[2].result)   // 30 % 7 = 2
        assertEquals("2.0", result[3].result)
    }

    @Test
    fun `total with no preceding results is 0`() = testCalculate("total") { result ->
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `total preserves units and does not convert to base unit`() = testCalculate(
        "1 acre to sqft",
        "_/2",
        "total"
    ) { result ->
        assertEquals("43560.0 ft²", result[0].result)
        assertEquals("21780.0 ft²", result[1].result)
        assertEquals("65340.0 ft²", result[2].result)
    }

    @Test
    fun `total with physical and unitless values returns error`() = testCalculate(
        "4 kg",
        "5",
        "total"
    ) { result ->
        assertEquals("4.0 kg", result[0].result)
        assertEquals("5.0", result[1].result)
        assertError("Cannot sum Kilogram and unitless number: dimension mismatch", result, 2)
    }

    @Test
    fun `total with mixed categories returns error`() = testCalculate(
        "10 m",
        "20 s",
        "total"
    ) { result ->
        assertError("Cannot sum Meter and Second: dimension mismatch", result, 2)
    }

    @Test
    fun `total with same categories returns sum`() = testCalculate(
        "10 m",
        "200 cm",
        "total"
    ) { result ->
        assertEquals("1200.0 cm", result[2].result)
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
        val lines = createLines("5", "15", "total * 2")
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `avg averages all results above in same block`() = testCalculate(
        "10",
        "20",
        "60",
        "avg",
        "10m",
        "20",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[1].result)
        assertEquals("60.0", result[2].result)
        assertEquals("30.0", result[3].result)
        assertEquals("10.0 m", result[4].result)
        assertEquals("20.0", result[5].result)
        assertError("Cannot average Meter and unitless number: dimension mismatch", result, 6)
    }

    @Test
    fun `average is an alias for avg`() = testCalculate(
        "10",
        "30",
        "average"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("30.0", result[1].result)
        assertEquals("20.0", result[2].result)
    }

    @Test
    fun `avg with no preceding results is 0`() = testCalculate("avg") { result ->
        assertEquals("0.0", result[0].result)
    }

    @Test
    fun `blank line resets the block for avg`() = testCalculate(
        "10",
        "20",
        "avg",
        "",
        "5",
        "avg"
    ) { result ->
        assertEquals("15.0", result[2].result)
        assertEquals("", result[3].result)
        assertEquals("5.0", result[5].result)
    }

    @Test
    fun `comment-only lines do not contribute to avg`() = testCalculate(
        "10",
        "# ignore this",
        "20",
        "avg"
    ) { result ->
        // Null breaks the block, so avg only sees 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `avg used in an expression`() = testCalculate(
        "25",
        "75",
        "half_avg = avg / 2"
    ) { result ->
        assertEquals("25.0", result[2].result) // avg is 50, halved to 25
    }

    @Test
    fun `avg assignment overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg = 100",
        "avg / 2"
    ) { result ->
        assertEquals("100.0", result[2].result)
        assertEquals("50.0", result[3].result)
    }

    @Test
    fun `avg increment overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg++",
        "avg"
    ) { result ->
        assertEquals("16.0", result[2].result) // avg is 15, gets incremented and assigned to 16
        assertEquals("16.0", result[3].result)
    }

    @Test
    fun `avg decrement overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg--",
        "avg"
    ) { result ->
        assertEquals("14.0", result[2].result) // avg is 15, gets decremented and assigned to 14
        assertEquals("14.0", result[3].result)
    }

    @Test
    fun `avg += overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg += 5",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("20.0", result[2].result) // 15 + 5 = 20
        assertEquals("20.0", result[3].result)
    }

    @Test
    fun `avg -= overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg -= 3",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("12.0", result[2].result) // 15 - 3 = 12
        assertEquals("12.0", result[3].result)
    }

    @Test
    fun `avg multiply-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg *= 2",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("30.0", result[2].result) // 15 * 2 = 30
        assertEquals("30.0", result[3].result)
    }

    @Test
    fun `avg divide-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg /= 3",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("5.0", result[2].result) // 15 / 3 = 5
        assertEquals("5.0", result[3].result)
    }

    @Test
    fun `avg modulo-assign overrides aggregate meaning`() = testCalculate(
        "10",
        "20",
        "avg %= 4",
        "avg"
    ) { result ->
        assertEquals("10.0", result[0].result)
        assertEquals("3.0", result[2].result) // 15 % 4 = 3
        assertEquals("3.0", result[3].result)
    }

    @Test
    fun `calculateFrom correctly handles avg in affected lines`() = runBlocking {
        val lines = createLines("a = 10", "b = 20", "avg")
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("15.0", result[0].result)
    }

    @Test
    fun `avg includes its own block results across calculateFrom boundary`() = runBlocking {
        val lines = createLines("5", "35", "avg * 2")
        val result = MathEngine.calculateFrom(lines, changedIndex = 2)
        assertEquals(1, result.size)
        assertEquals("40.0", result[0].result)
    }

    @Test
    fun `last keyword refers to the previous line result`() = testCalculate(
        "10 * 5",
        "last + 10"
    ) { result ->
        assertEquals("50.0", result[0].result)
        assertEquals("60.0", result[1].result)
    }

    @Test
    fun `prev keyword refers to the previous line result`() = testCalculate(
        "100 / 4",
        "prev * 2m"
    ) { result ->
        assertEquals("25.0", result[0].result)
        assertEquals("50.0 m", result[1].result)
    }

    @Test
    fun `previous keyword refers to the previous line result`() = testCalculate(
        "20 + 30",
        "previous - 10"
    ) { result ->
        assertEquals("50.0", result[0].result)
        assertEquals("40.0", result[1].result)
    }

    @Test
    fun `above keyword refers to the previous line result`() = testCalculate(
        "5 ^ 2",
        "above / 5"
    ) { result ->
        assertEquals("25.0", result[0].result)
        assertEquals("5.0", result[1].result)
    }

    @Test
    fun `underscore keyword refers to the previous line result`() = testCalculate(
        "42",
        "_ + 8"
    ) { result ->
        assertEquals("42.0", result[0].result)
        assertEquals("50.0", result[1].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line is blank`() = testCalculate(
        "10",
        "   ",
        "last + 5"
    ) { result ->
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line is a comment`() = testCalculate(
        "10",
        "# comment",
        "last + 5"
    ) { result ->
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 if the preceding line resulted in an error`() = testCalculate(
        "10",
        "{",
        "last + 5"
    ) { result ->
        assertError("Unexpected character `{`", result, 1)
        assertEquals("5.0", result[2].result)
    }

    @Test
    fun `last keyword returns 0 on the first line`() = testCalculate("last + 10") { result ->
        assertEquals("10.0", result[0].result)
    }

    @Test
    fun `last keyword reassignment is blocked`() = testCalculate("last = 10") { result ->
        assertError("`last` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `compound assignment to underscore is blocked`() = testCalculate("_ += 5") { result ->
        assertError("`_` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `assignment to constant PI is not allowed`() = testCalculate("PI = 4") { result ->
        assertError("`PI` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `assignment to constant pi is not allowed`() = testCalculate("pi = 4") { result ->
        assertError("`pi` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `assignment to constant π is not allowed`() = testCalculate("π = 4") { result ->
        assertError("`π` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `assignment to constant E is not allowed`() = testCalculate("E = 4") { result ->
        assertError("`E` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `assignment to constant e is not allowed`() = testCalculate("e = 4") { result ->
        assertError("`e` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `formatDisplayResult formats raw strings correctly`() = runBlocking {
        assertEquals("0.33", MathEngine.formatDisplayResult("0.3333333333333333", 2))
        assertEquals("0.3333", MathEngine.formatDisplayResult("0.3333333333333333", 4))
        assertEquals("10", MathEngine.formatDisplayResult("10.0", 2)) // whole numbers are displayed without decimal points
        assertEquals("4", MathEngine.formatDisplayResult("4", 6)) // whole numbers are displayed without decimal points
        assertEquals("Err", MathEngine.formatDisplayResult("Err", 2))
        assertEquals("1,234.57", MathEngine.formatDisplayResult("1234.5678", 2, java.util.Locale.ROOT))
        assertEquals("1,234.5678000000", MathEngine.formatDisplayResult("1234.5678", 10, java.util.Locale.ROOT))
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
        assertEquals("1.00E20", MathEngine.formatDisplayResult("1.0E20", 2, java.util.Locale.ROOT))
        assertEquals("1E20", MathEngine.formatDisplayResult("1.0E20", 0, java.util.Locale.ROOT))
        assertEquals("1.00E1000", MathEngine.formatDisplayResult("1.0E1000", 2, java.util.Locale.ROOT))
    }

    @Test
    fun `formatDisplayResult respects explicit locales`() = runBlocking {
        val raw = "1234.567"
        assertEquals("1,234.57", MathEngine.formatDisplayResult(raw, 2, java.util.Locale.ROOT))
        assertEquals("1.234,57", MathEngine.formatDisplayResult(raw, 2, java.util.Locale.GERMANY))
        val largeRaw = "1.23E30"
        assertEquals("1,23E30", MathEngine.formatDisplayResult(largeRaw, 2, java.util.Locale.GERMANY))
    }

    @Test
    fun `formatDisplayResult respects region override for separators`() = runBlocking {
        assertEquals("1,234,567", MathEngine.formatDisplayResult("1234567", 2, java.util.Locale.FRANCE, "US"))
    }

    @Test
    fun `formatDisplayResult respects region for french style`() = runBlocking {
        // Note: France uses non-breaking space (narrow non-breaking space \u202F) as separator
        assertEquals("1\u202F234,57", MathEngine.formatDisplayResult("1234.567", 2, java.util.Locale.US, "FR"))
    }

    @Test
    fun `formatDisplayResult respects explicit US and DE region overrides`() = runBlocking {
        assertEquals("1,234.57", MathEngine.formatDisplayResult("1234.567", 2, java.util.Locale.GERMANY, "US"))

        assertEquals("1.234,57", MathEngine.formatDisplayResult("1234.567", 2, java.util.Locale.US, "DE"))
    }

    @Test
    fun `formatDisplayResult implements smart regional formatting with non-standard separator fallbacks`() = runBlocking {
        val value = "1234567.89"

        // Bahrain (BH) - Should fallback to Western symbols (en_BH) to avoid mixing Arabic symbols
        val bhLocale = Locale("ar", "BH")
        assertEquals("1,234,567.89", MathEngine.formatDisplayResult(value, 2, bhLocale, "BH"))

        // Germany (DE) - Should keep native de_DE symbols (dot grouping, comma decimal)
        val deLocale = Locale("de", "DE")
        assertEquals("1.234.567,89", MathEngine.formatDisplayResult(value, 2, deLocale, "DE"))

        // France (FR) - Should keep native fr_FR symbols (space grouping, comma decimal)
        val frLocale = Locale("fr", "FR")
        val frResult = MathEngine.formatDisplayResult(value, 2, frLocale, "FR")
        assertTrue(frResult.contains('\u00A0') || frResult.contains('\u202F') || frResult.contains(' '))
        assertTrue(frResult.endsWith(",89"))

        // India (IN) - Should keep custom Indian style with Western comma
        val inLocale = Locale("hi", "IN")
        assertEquals("12,34,567.89", MathEngine.formatDisplayResult(value, 2, inLocale, "IN"))

        // Switzerland (CH) - Should keep native apostrophe separator
        val chLocale = Locale("de", "CH")
        val chResult = MathEngine.formatDisplayResult(value, 2, chLocale, "CH")
        assertTrue(chResult.any { it in setOf('\'', '\u2019', '.', ',') })
        // de_CH uses the smart quote ’ (U+2019) as grouping separator
        assertEquals("1\u2019234\u2019567.89", chResult)
    }

    @Test
    fun `getErrorDetails handles undefined variable`() = testCalculate("x + 5") { result ->
        assertError("Unknown variable `x`", result[0], result, 0)
    }

    @Test
    fun `getErrorDetails handles syntax error`() = testCalculate("1 + (2 * 3") { result ->
        assertError("Expected `)`, but found `end of line`", result[0], result, 0)
    }

    @Test
    fun `getErrorDetails returns null for blank line`() = runBlocking {
        val lines = listOf(createLine(""))
        val err = MathEngine.getErrorDetails(lines, 0)
        assertNull(err)
    }

    @Test
    fun `getErrorDetails handles division by zero`() = testCalculate("10 / 0") { result ->
        assertError("Cannot divide by zero", result, 0)
    }

    @Test
    fun `getErrorDetails handles unknown function`() = testCalculate("unknown(5)") { result ->
        assertError("Unknown function `unknown()`", result, 0)
    }

    @Test
    fun `getErrorDetails handles lexer error`() = testCalculate("1 @ 2") { result ->
        assertError("Unexpected character `@`", result, 0)
    }

    @Test
    fun `getErrorDetails handles multiple operators`() = testCalculate("1 + * 2") { result ->
        assertError("Expected a value or `(`, but found `*`", result, 0)
    }

    @Test
    fun `getErrorDetails handles missing operand`() = testCalculate("5 + ") { result ->
        assertError("Expected a value or `(`, but found `end of line`", result, 0)
    }

    @Test
    fun `getErrorDetails handles empty parentheses`() = testCalculate("()") { result ->
        assertError("Expected a value or `(`, but found `)`", result, 0)
    }

    @Test
    fun `getErrorDetails handles arity mismatch for user-defined function`() = testCalculate(
        "f(x) = x * 2",
        "f(1, 2)"
    ) { result ->
        assertError("Function `f()` expects 1 argument, but got 2", result, 1)
    }

    @Test
    fun `arity mismatch reported before undefined argument for built in function`() = testCalculate("sinh(2, 5)") { result ->
        assertError("Function `sinh()` expects 1 argument, but got 2", result, 0)
    }

    @Test
    fun `arity mismatch reported before undefined argument for local function`() = testCalculate(
        "f(x) = x * 2",
        "f(1, unknown_var)"
    ) { result ->
        assertError("Function `f()` expects 1 argument, but got 2", result, 1)
    }

    @Test
    fun `arity mismatch reported before undefined argument for built-in function with invalid arg`() = testCalculate("sinh(1, unknown_var)") { result ->
        assertError("Function `sinh()` expects 1 argument, but got 2", result, 0)
    }

    @Test
    fun `factorial rejects fractional input`() = testCalculate("factorial(4.5)") { result ->
        assertError("Factorial is only defined for whole numbers", result, 0)
    }

    @Test
    fun `factorial rejects negative input`() = testCalculate("fact(-3)") { result ->
        assertError("Factorial is only defined for non-negative whole numbers", result, 0)
    }

    @Test
    fun `factorial rejects inputs beyond supported limit`() = testCalculate("factorial(1001)") { result ->
        assertError("Factorial is only supported up to 1000", result, 0)
    }

    @Test
    fun `lineno, linenumber, and currentLineNumber returns correct current line number`() = testCalculate(
        "lineno",
        "linenumber",
        "currentLineNumber"
    ) { result ->
        assertEquals("1.0", result[0].result)
        assertEquals("2.0", result[1].result)
        assertEquals("3.0", result[2].result)
    }

    @Test
    fun `lineno works in expressions`() = testCalculate(
        "10 + lineno",
        "lineno * 5"
    ) { result ->
        assertEquals("11.0", result[0].result)
        assertEquals("10.0", result[1].result)
    }

    @Test
    fun `lineno is reserved and cannot be reassigned`() = testCalculate("lineno = 10") { result ->
        assertError("`lineno` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `compound assignment to lineno is not allowed`() = testCalculate("lineno += 5") { result ->
        assertError("`lineno` is a reserved name and cannot be changed", result, 0)
    }

    @Test
    fun `increment on lineno is not allowed`() = testCalculate("lineno++") { result ->
        assertError("`lineno` is a reserved name and cannot be changed", result, 0)
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
        testCalculateFrom("a = 5", "b = 10", "lineno + a", changedIndex = 2) { partialResult ->
            assertEquals(1, partialResult.size)
            assertEquals("8.0", partialResult[0].result)
        }
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

        testCalculate(
            "f = file(\"File B\")",
            "f.x + 5",
            loader = loader
        ) { result ->
            assertEquals("25.0", result[1].result)
        }
    }

    @Test
    fun `evaluate with direct file function access`() = runBlocking {
        val remoteContext = MathContext(variables = mutableMapOf("total" to EvaluationResult(BigDecimal("100.0"))))
        val loader = FakeFileContextLoader(mapOf("Summary" to remoteContext))

        testCalculate("file(\"Summary\").total * 0.1", loader = loader) { result ->
            assertEquals("10.0", result[0].result)
        }
    }

    @Test
    fun `evaluate clears file linked state after reassignment to number`() = runBlocking {
        val remoteContext = MathContext(variables = mutableMapOf("x" to EvaluationResult(BigDecimal("20.0"))))
        val loader = FakeFileContextLoader(mapOf("File B" to remoteContext))

        testCalculate(
            "f = file(\"File B\")",
            "f = 42",
            "f.x",
            loader = loader
        ) { result ->
            assertError("`f` is not linked to any file. Use `file(\"...\")` to link first", result, 2, loader)
        }
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

        testCalculate("file(\"File B\").loopback()", loader = loader) { result ->
            assertError("File `File B` references itself, causing an endless loop", result, 0, loader)
        }
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

        testCalculate("file(\"Summary\").double(5) + 3", loader = loader) { result ->
            assertEquals("13.0", result[0].result)
        }
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

        testCalculate("file(\"Summary\").addOne(10 cm)", loader = loader) { result ->
            assertEquals("11.0 cm", result[0].result)
        }
    }

    @Test
    fun `standalone string literal throws error`() = testCalculate("\"hello\"") { result ->
        assertError("Quotes are only allowed when specifying file names in `file(\"...\")`", result, 0)
    }

    @Test
    fun `writing to member access target is read-only`() = testCalculate(
        "f = file(\"File B\")",
        "f.x = 10"
    ) { result ->
        assertError("Variables from other files are read-only and cannot be changed", result, 1)
    }

    @Test
    fun `dot notation on global functions throws error`() = runBlocking {
        val remoteContext = MathContext()
        val loader = FakeFileContextLoader(mapOf("File B" to remoteContext))
        testCalculate(
            "f = file(\"File B\")",
            "f.sin(90)",
            loader = loader
        ) { result ->
            assertError("`sin()` is a global function and should be called directly, not via dot notation", result, 1, loader)
        }
    }

    @Test
    fun `file call missing closing parenthesis throws error`() = testCalculate("file(\"A\"") { result ->
        assertError("Expected `)`, but found `end of line`", result, 0)
    }

    @Test
    fun `dot notation missing identifier throws error`() = testCalculate("file(\"A\").") { result ->
        assertError("Missing variable or function name after `.`", result, 0)
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
        val fileALines = createLines(
            "b = file(\"File B\")",
            "b.y"
        )
        val fileBLines = createLines(
            "a = file(\"File A\")",
            "a.x"
        )

        val loader = RecursiveFakeFileContextLoader(mapOf(
            "File A" to fileALines,
            "File B" to fileBLines
        ))

        testCalculate("b = file(\"File B\")", "b.y", loader = loader) { result ->
            assertError("File `File A` also references file `File B`, causing an endless loop", result, 1, loader)
        }
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
    fun `unit conversion simple natural language`() = testCalculate(
        "10 km in m",
        "1000 m as kilometers",
        "2 hours in seconds"
    ) { result ->
        assertEquals("10000.0 m", result[0].result)
        assertEquals("1.0 km", result[1].result)
        assertEquals("7200.0 s", result[2].result)
    }

    @Test
    fun `unit conversion mixed arithmetic`() = testCalculate(
        "10 km + 5000 m in km",
        "1 m + 100 cm in m",
        "10 kg + 20 gram to kilograms",
        "53 weeks - 20 days"
    ) { result ->
        assertEquals("15.0 km", result[0].result)
        assertEquals("2.0 m", result[1].result)
        assertEquals("10.02 kg", result[2].result)
        assertEquals("351.0 d", result[3].result)
    }

    @Test
    fun `unit conversion temperature non linear`() = testCalculate(
        "0 degC in F",
        "212 F in C",
        "0 C in K"
    ) { result ->
        assertEquals("32.0 °F", result[0].result)
        assertEquals("100.0 °C", result[1].result)
        assertEquals("273.15 K", result[2].result)
    }

    @Test
    fun `unit conversion data storage`() = testCalculate(
        "1 GB in MB",
        "1 GiB in MiB"
    ) { result ->
        assertEquals("1000.0 MB", result[0].result)
        assertEquals("1024.0 MiB", result[1].result)
    }

    @Test
    fun `unit conversion css dynamic ppi`() = testCalculate(
        "96 px in inch",
        "ppi = 300",
        "300 px in inch",
        "em = 21px",
        "1.5 em in px"
    ) { result ->
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
    fun `unit conversion function syntax`() = testCalculate("convert(10, \"km\", \"m\")") { result ->
        assertEquals("10000.0 m", result[0].result)
    }

    @Test
    fun `mixed unit addition picking smaller unit`() = testCalculate("53 weeks + 2 days") { result ->
        assertEquals("373.0 d", result[0].result)
    }

    @Test
    fun `multiplying length quantities promotes area and volume`() = runBlocking {
        testCalculate("4 m * 2 m", "10 ft * 10 ft", "2 cm * 2 cm * 2 cm", "last + 4 cubic meter") {
            assertEquals("8.0 m²", it[0].result)
            assertEquals("100.0 ft²", it[1].result)
            assertEquals("8.0 cm³", it[2].result)
            assertEquals("4000008.0 cm³", it[3].result)
        }
    }

    @Test
    fun `dividing area and volume quantities reduces dimension`() = runBlocking {
        testCalculate("4 m * 2 m * 2 m / 2 m", "4 m * 2 m / 2 m", "8 m³ / 2 m") {
            assertEquals("8.0 m²", it[0].result)
            assertEquals("4.0 m", it[1].result)
            assertEquals("4.0 m²", it[2].result)
        }
    }

    @Test
    fun `unsupported multiplicative unit chain returns error`() = runBlocking {
        testCalculate("2m * 2m * 2m * 2m") {
            assertError("unsupported multiplicative unit: Cubic Meter * Meter", it, 0)
        }
    }

    @Test
    fun `same category multiplication without derivation returns error`() = runBlocking {
        testCalculate("2 kg * 3 kg", "4 h * 2 h", "2 ft * 3 m", "30 °C * 2 °C") {
            assertError("unsupported multiplicative unit: Kilogram * Kilogram", it, 0)
            assertError("unsupported multiplicative unit: Hour * Hour", it, 1)
            assertError("unsupported multiplicative unit: Foot * Meter", it, 2)
            assertError("unsupported multiplicative unit: Celsius * Celsius", it, 3)
        }
    }

    @Test
    fun `add scalar to physical quantity is disallowed`() = runBlocking {
        testCalculate("53 weeks", "last + 3 weeks", "last + 3") {
            assertEquals("53.0 wk", it[0].result)
            assertEquals("56.0 wk", it[1].result)
            assertError("Cannot add Week and unitless number: dimension mismatch", it, 2)
        }
    }

    @Test
    fun `reassigning to unit symbol is disallowed`() = runBlocking {
        testCalculate("km = 5") {
            assertError("`km` is a unit symbol and cannot be used as a variable name", it, 0)
        }
    }

    @Test
    fun `convert function performs full conversion from base`() = runBlocking {
        testCalculate("convert(10, \"km\", \"cm\")") {
            assertEquals("1000000.0 cm", it[0].result)
        }
    }

    @Test
    fun `unit conversion expression performs full conversion from base`() = runBlocking {
        testCalculate("10 km in cm") {
            assertEquals("1000000.0 cm", it[0].result)
        }
    }

    @Test
    fun `unit conversion error incompatible dimensions`() = runBlocking {
        testCalculate("10 km in kg") {
            assertError("Cannot convert `Kilometer` to `Kilogram`: dimension mismatch", it, 0)
        }
    }

    @Test
    fun `unit conversion multi word alias`() = runBlocking {
        testCalculate("10 degree celsius in fahrenheit") {
            assertEquals("50.0 °F", it[0].result)
        }
    }

    @Test
    fun `standalone quantity preserves unit in result`() = runBlocking {
        testCalculate("10 kg") {
            assertEquals("10.0 kg", it[0].result)
        }
    }

    @Test
    fun `quantity can be stripped to unitless value`() = runBlocking {
        testCalculate(
            "area = 4.20",
            "cost = area * 4.2 crores",
            "saleable = area * (1 acre as sqft) * 70%",
            "revenue = saleable * 5000",
            "value(10 kg)",
            "dropUnit(10 kg)",
            "raw(10 kg)",
            "value(128066.6 sqft)",
            "value(12 million)",
            "value(12 kg)",
            "value(128066.6 sqft) - 1",
            "12 kg - 1",
            "value(12 kg) - 1",
            "value(revenue) - cost"
        ) {
            assertEquals("4.2", it[0].result)
            assertEquals("176400000.0", it[1].result)
            assertTrue(it[2].result.startsWith("128066.4"))
            assertTrue(it[3].result.startsWith("640332"))
            assertTrue(it[3].result.endsWith(" ft²"))
            assertEquals("10", it[4].result)
            assertEquals("10", it[5].result)
            assertEquals("10", it[6].result)
            assertTrue(it[7].result.startsWith("128066.6"))
            assertEquals("12000000", it[8].result)
            assertEquals("12", it[9].result)
            assertTrue(it[10].result.startsWith("128065.6"))
            assertError("Cannot subtract Kilogram and unitless number: dimension mismatch", it, 11)
            assertEquals("11.0", it[12].result)
            assertEquals("463932000.0", it[13].result)
        }
    }

    @Test
    fun `value and dropUnit preserve display-space rational value`() = runBlocking {
        testCalculate("rational(value(5 km))", "rational(dropUnit(5 km))", "rational(raw(5 km))", rationalMode = true) {
            assertEquals("5", it[0].result)
            assertEquals("5", it[1].result)
            assertEquals("5", it[2].result)
        }
    }

    @Test
    fun `unit conversion calculation chain works as expected between different units by dropping units`() = runBlocking {
        testCalculate(
            "area = 4.20",
            "cost = area * 4.2 crores",
            "saleable = area * (1 acre as sqft) * 70%",
            "revenue = saleable * 5000",
            "value(revenue) - cost"
        ) {
            assertEquals("4.2", it[0].result)
            assertEquals("176400000.0", it[1].result)
            assertTrue(it[2].result.startsWith("128066.4"))
            assertEquals(640332000.0, it[3].result.split(" ")[0].toDouble(), 0.1)
            assertTrue(it[3].result.endsWith(" ft²"))
            assertEquals("463932000.0", it[4].result)
        }
    }

    @Test
    fun `unit stripping helpers cannot be reassigned`() = runBlocking {
        testCalculate("value = 1", "dropUnit = 1", "raw = 1") {
            assertError("`value` is a reserved name and cannot be changed", it, 0)
            assertError("`dropUnit` is a reserved name and cannot be changed", it, 1)
            assertError("`raw` is a reserved name and cannot be changed", it, 2)
        }
    }

    @Test
    fun `unitless conversion to non-scalar returns Err`() = runBlocking {
        testCalculate("5 as cm") {
            assertError("Cannot convert unitless number to `Centimeter`", it, 0)
        }
    }

    @Test
    fun `compound assignment enforces unit consistency`() = runBlocking {
        val lines = listOf(
            createLine("distance = 10 km"),
            createLine("distance += 1km"),
            createLine("distance += 1")
        )
        val result = MathEngine.calculate(lines)
        assertEquals("11.0 km", result[1].result)
        assertError("Cannot add Kilometer and unitless number: dimension mismatch", result[2], lines, 2)
    }

    @Test
    fun `increment support values with units`() = runBlocking {
        testCalculate("distance = 10 km", "distance++") {
            assertEquals("11.0 km", it[1].result)
        }
    }

    @Test
    fun `decrement support values with units`() = runBlocking {
        testCalculate("distance = 10 km", "distance--") {
            assertEquals("9.0 km", it[1].result)
        }
    }

    @Test
    fun `fractional numeral system conversion throws error`() = runBlocking {
        testCalculate("10.4 in binary") {
            assertError("Fractional value cannot be converted to numeral system", it, 0)
        }
    }

    @Test
    fun `formatDisplayResult honors precision`() {
        val input = "0.33333333333333335"
        assertEquals("0.33", MathEngine.formatDisplayResult(input, 2, java.util.Locale.US))
        assertEquals("0.3333", MathEngine.formatDisplayResult(input, 4, java.util.Locale.US))
        assertEquals("0.333333", MathEngine.formatDisplayResult(input, 6, java.util.Locale.US))
    }

    @Test
    fun `formatDisplayResult handles quantities with precision`() {
        val input = "0.33333333333333335 km"
        assertEquals("0.33 km", MathEngine.formatDisplayResult(input, 2, java.util.Locale.US))
        assertEquals("0.3333 km", MathEngine.formatDisplayResult(input, 4, java.util.Locale.US))
    }

    @Test
    fun `formatDisplayResult handles large numbers with precision`() {
        val input = "12345678901234567890"
        // Should use scientific notation with precision
        val formatted = MathEngine.formatDisplayResult(input, 2, java.util.Locale.US)
        assertTrue(formatted.contains("E"))
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
        // Threshold is 10^15 (1,000,000,000,000,000)
        assertEquals("9999999.0", MathEngine.formatBigDecimal(java.math.BigDecimal("9999999")))
        assertEquals("10000000.0", MathEngine.formatBigDecimal(java.math.BigDecimal("10000000")))
        assertEquals("12345678.0", MathEngine.formatBigDecimal(java.math.BigDecimal("12345678")))
        assertEquals("1.0E100", MathEngine.formatBigDecimal(java.math.BigDecimal("10").pow(100)))
    }

    @Test
    fun `very small numbers should use scientific notation`() {
        // Threshold is 10^-6 (0.000001)
        assertEquals("0.001", MathEngine.formatBigDecimal(java.math.BigDecimal("0.001")))
        assertEquals("0.0001", MathEngine.formatBigDecimal(java.math.BigDecimal("0.0001")))
        assertEquals("0.00001", MathEngine.formatBigDecimal(java.math.BigDecimal("0.00001")))
        assertEquals("1.0E-6", MathEngine.formatBigDecimal(java.math.BigDecimal("0.000001")))
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
        assertEquals("-10000000.0", MathEngine.formatBigDecimal(java.math.BigDecimal("-10000000")))
        assertEquals("-0.0001", MathEngine.formatBigDecimal(java.math.BigDecimal("-0.0001")))
        assertEquals("-0.00001", MathEngine.formatBigDecimal(java.math.BigDecimal("-0.00001")))
        assertEquals("-1.0E-6", MathEngine.formatBigDecimal(java.math.BigDecimal("-0.000001")))
    }

    @Test
    fun `formatDisplayResult preserves scientific notation for small numbers`() = runBlocking {
        // Small number without unit
        val result1 = MathEngine.formatDisplayResult("1.0E-4", 2, Locale.ROOT)
        assertEquals("1.00E-4", result1)

        // Small number with unit
        val result2 = MathEngine.formatDisplayResult("1.0E-4 kg", 2, Locale.ROOT)
        assertEquals("1.00E-4 kg", result2)

        // Large number without unit
        val result3 = MathEngine.formatDisplayResult("1.0E10", 2, Locale.ROOT)
        assertEquals("1.00E10", result3)

        // Plain decimal should still use decimal formatting
        val result4 = MathEngine.formatDisplayResult("1.2345", 2, Locale.ROOT)
        assertEquals("1.23", result4)
    }
    @Test
    fun `variable as unit quantity anchor`() = runBlocking {
        testCalculate("a = 15", "a km", "a km in m") {
            assertEquals("15.0", it[0].result)
            assertEquals("15.0 km", it[1].result)
            assertEquals("15000.0 m", it[2].result)
        }
    }

    @Test
    fun `variable with composite unit conversion`() = runBlocking {
        testCalculate("a = 15", "a kilometers per hour to mph") {
            assertEquals("15.0", it[0].result)
            // 15 km/h to mph: 15 / 1.60934 = 9.32056788356...
            assertTrue(it[1].result.contains("9.3205"))
            assertTrue(it[1].result.endsWith(" mph"))
        }
    }

    @Test
    fun `multiple variables as unit quantities`() = runBlocking {
        testCalculate("v1 = 10", "v2 = 500", "v1 km + v2 m") {
            assertEquals("10.0", it[0].result)
            assertEquals("500.0", it[1].result)
            assertEquals("10500.0 m", it[2].result)
        }
    }

    @Test
    fun `underscore with units should parse correctly`() = runBlocking {
        testCalculate("10", "_ km") {
            assertEquals("10.0", it[0].result)
            assertEquals("10.0 km", it[1].result)
        }
    }

    @Test
    fun `last with units should parse correctly`() = runBlocking {
        testCalculate("10", "last m") {
            assertEquals("10.0", it[0].result)
            assertEquals("10.0 m", it[1].result)
        }
    }

    @Test
    fun `last with unit conversion should parse correctly`() = runBlocking {
        testCalculate("10", "last km to m") {
            assertEquals("10000.0 m", it[1].result)
        }
    }

    @Test
    fun `line number alias with units should parse correctly`() = runBlocking {
        testCalculate("10", "lineno km") {
            // lineno is 2 for the second line.
            assertEquals("2.0 km", it[1].result)
        }
    }

    @Test
    fun `global rational mode changes displayed division result`() = runBlocking {
        testCalculate("1/3 + 1/3 + 1/3") {
            assertEquals("0.9999999999999999999999999999999999", it[0].result)
        }

        testCalculate("1/3 + 1/3 + 1/3", rationalMode = true) {
            assertEquals("1", it[0].result)
        }
    }

    @Test
    fun `global rational mode changes displayed fractional result`() = runBlocking {
        testCalculate("1/3 + 1/6", "1/3 + 2", rationalMode = true) {
            assertEquals("1/2", it[0].result)
            assertEquals("7/3", it[1].result)
        }
    }

    @Test
    fun `rational function still stores rational result in decimal mode`() = runBlocking {
        testCalculate("rational(0.5)") {
            assertEquals("1/2", it[0].result)
        }
    }

    @Test
    fun `fraction function still stores rational result in decimal mode`() = runBlocking {
        testCalculate("fraction(0.75)") {
            assertEquals("3/4", it[0].result)
        }
    }

    @Test
    fun `rational function preserves rational result for rational expressions`() = runBlocking {
        testCalculate("rational(1/3)") {
            assertEquals("1/3", it[0].result)
        }
    }

    @Test
    fun `float function stores decimal result even in rational mode`() = runBlocking {
        testCalculate("float(1/3)", rationalMode = true) {
            assertEquals("0.3333333333", it[0].result.take(12))
        }
    }

    @Test
    fun `in decimal unit conversion enforces integer check`() = runBlocking {
        testCalculate("1/3 in decimal", rationalMode = true) {
            assertError("Fractional value cannot be converted to numeral system", it, 0)
        }
    }

    @Test
    fun `rational function preserves rational result for quantities`() = runBlocking {
        testCalculate("rational(5 km / 2)") {
            assertEquals("5/2 km", it[0].result)
        }
    }

    @Test
    fun `global rational mode changes displayed quantity result`() = runBlocking {
        testCalculate("5 km / 2") {
            assertEquals("2.5 km", it[0].result)
        }
        testCalculate("5 km / 2", rationalMode = true) {
            assertEquals("5/2 km", it[0].result)
        }
    }

    @Test
    fun `float function stores decimal result for quantities in rational mode`() = runBlocking {
        testCalculate("float(5 km / 2)", rationalMode = true) {
            assertEquals("2.5 km", it[0].result)
        }
    }

    @Test
    fun `global rational mode changes displayed value for fractional arithmetic`() = runBlocking {
        testCalculate("1 / 3") {
            assertEquals("0.3333333333333333333333333333333333", it[0].result)
        }
        testCalculate("1 / 3", rationalMode = true) {
            assertEquals("1/3", it[0].result)
        }
    }
}
