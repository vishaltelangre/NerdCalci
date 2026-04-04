package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MixedUnitUsageTest {

    @Test
    fun `basic mixed unit addition fails`() {
        testCalculate("10m + 10kg", "10m - 5s") { result ->
            assertError("Addition of Meter and Kilogram is not supported", result, 0)
            assertError("Subtraction of Meter and Second is not supported", result, 1)
        }
    }

    @Test
    fun `unitless mixed with unit fails`() {
        testCalculate(
            "10m + 5",
            "5 + 10m",
            "10m - 5",
            "5 - 10m"
        ) { result ->
            assertError("Addition of Meter and unitless number is not supported", result, 0)
            assertError("Addition of unitless number and Meter is not supported", result, 1)
            assertError("Subtraction of Meter and unitless number is not supported", result, 2)
            assertError("Subtraction of unitless number and Meter is not supported", result, 3)
        }
    }

    @Test
    fun `same category addition passes with smallest unit inheritance`() {
        testCalculate("10m + 10cm", "10mm + 10m") { result ->
            assertEquals("1010.0 cm", result[0].result)
            assertEquals("10010.0 mm", result[1].result)
        }
    }

    @Test
    fun `nested mixed unit expressions inherit from the smallest unit`() {
        testCalculate("(1m + 1cm) + 1mm", "10kg + (500g + 10mg)") { result ->
            assertEquals("1011.0 mm", result[0].result)
            assertEquals("10500010.0 mg", result[1].result)
        }
    }

    @Test
    fun `variable propagation with unit inheritance`() {
        testCalculate(
            "x = 1m",
            "y = x + 1cm",
            "z = y + 1mm"
        ) { result ->
            assertEquals("101.0 cm", result[1].result)
            assertEquals("1011.0 mm", result[2].result)
        }
    }

    @Test
    fun `order independence in units with same dimension`() {
        testCalculate("1cm + 1m", "1m + 1cm") { result ->
            assertEquals("101.0 cm", result[0].result)
            assertEquals("101.0 cm", result[1].result)
        }
    }

    @Test
    fun `multiple operands fail on first mismatch involving unitless numbers`() {
        testCalculate("10m + 20m + 5", "10 + 20 + 5m") { result ->
            assertError("Addition of Meter and unitless number is not supported", result, 0)
            assertError("Addition of unitless number and Meter is not supported", result, 1)
        }
    }

    @Test
    fun `multiplicative isolation passes`() {
        testCalculate(
            "(10kg * 2) + 500g",
            "10kg + (500g * 2)",
            "(3 kilograms / 2) + 10 grams"
        ) { result ->
            assertEquals("20500.0 g", result[0].result) // Inherits g
            assertEquals("11000.0 g", result[1].result) // Inherits g
            assertEquals("1510.0 g", result[2].result)  // Inherits g
        }
    }

    @Test
    fun `block sum with mixed units fails`() {
        testCalculate("10m", "5", "sum") { result ->
            assertEquals("10.0 m", result[0].result)
            assertEquals("5.0", result[1].result)
            assertError("Summation of Meter and unitless number is not supported", result, 2)
        }
    }

    @Test
    fun `block sum with different physical categories fails`() {
        testCalculate("10m", "5kg", "sum") { result ->
            assertEquals("10.0 m", result[0].result)
            assertEquals("5.0 kg", result[1].result)
            assertError("Summation of Meter and Kilogram is not supported", result, 2)
        }
    }

    @Test
    fun `block sum with same physical category passes with last unit inheritance`() {
        testCalculate("10m", "500cm", "sum") { result ->
            assertEquals("1500.0 cm", result[2].result)
        }
    }

    @Test
    fun `block average with mixed units fails`() {
        testCalculate("10m", "5", "avg") { result ->
            assertEquals("10.0 m", result[0].result)
            assertEquals("5.0", result[1].result)
            assertError("Average of Meter and unitless number is not supported", result, 2)
        }
    }

    @Test
    fun `block average with same physical category passes with last unit inheritance`() {
        testCalculate("10m", "500cm", "avg") { result ->
            assertEquals("750.0 cm", result[2].result)
        }
    }

    @Test
    fun `scalar mixed with unitless passes`() {
        testCalculate("30 + 1 thousand", "1 thousand + 30") { result ->
            assertEquals("1030.0", result[0].result)
            assertEquals("1030.0", result[1].result)
        }
    }

    @Test
    fun `numeral system mixed with unitless passes`() {
        testCalculate("10 + 20 hex", "20 hex + 10") { result ->
            assertEquals("0x14a", result[0].result.lowercase())
            assertEquals("0x14a", result[1].result.lowercase())
        }
    }

    @Test
    fun `compound assignments fail on mismatch`() {
        testCalculate("x = 10m", "x += 5") { result ->
            assertError("Addition of Meter and unitless number is not supported", result, 1)
        }
    }

    @Test
    fun `compound assignment with same unit passes`() {
        testCalculate("x = 10m", "x += 5m") { result ->
            assertEquals("15.0 m", result[1].result)
        }
    }

    @Test
    fun `complex mixed expression with variables fails`() {
        testCalculate("a = 10m", "b = 5", "a + b") { result ->
            assertError("Addition of Meter and unitless number is not supported", result, 2)
        }
    }

    @Test
    fun `increment and decrement support values with units`() {
        testCalculate("distance = 10 km", "distance++", "distance--") { result ->
            assertEquals("11.0 km", result[1].result)
            assertEquals("10.0 km", result[2].result)
        }
    }
}
