package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.*
import org.junit.Test
import com.vishaltelangre.nerdcalci.core.testCalculate
import com.vishaltelangre.nerdcalci.core.assertError

class MixedUnitUsageTest {

    private val lengthSymbols = UnitConverter.UNITS
        .filter { it.category == UnitCategory.LENGTH }
        .flatMap { it.symbols }

    private val timeSymbols = UnitConverter.UNITS
        .filter { it.category == UnitCategory.TIME }
        .flatMap { it.symbols }

    private val speedSymbols = UnitConverter.UNITS
        .filter { it.category == UnitCategory.SPEED }
        .flatMap { it.symbols }

    private fun isSpeedUnit(result: String): Boolean {
        return speedSymbols.any { result.endsWith(it) || result.contains(" $it") }
    }

    private fun isLengthUnit(result: String): Boolean {
        return lengthSymbols.any { result.endsWith(it) || result.contains(" $it") }
    }

    private fun isTimeUnit(result: String): Boolean {
        return timeSymbols.any { result.endsWith(it) || result.contains(" $it") }
    }

    @Test
    fun `basic mixed unit addition fails`() {
        testCalculate("10m + 10kg", "10m - 5s") { result ->
            assertError("Addition of `Meter` and `Kilogram` is not supported", result, 0)
            assertError("Subtraction of `Meter` and `Second` is not supported", result, 1)
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
            assertError("Addition of `Meter` and `unitless number` is not supported", result, 0)
            assertError("Addition of `unitless number` and `Meter` is not supported", result, 1)
            assertError("Subtraction of `Meter` and `unitless number` is not supported", result, 2)
            assertError("Subtraction of `unitless number` and `Meter` is not supported", result, 3)
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
            assertError("Addition of `Meter` and `unitless number` is not supported", result, 0)
            assertError("Addition of `unitless number` and `Meter` is not supported", result, 1)
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
            assertError("Summation of `Meter` and `unitless number` is not supported", result, 2)
        }
    }

    @Test
    fun `block sum with different physical categories fails`() {
        testCalculate("10m", "5kg", "sum") { result ->
            assertEquals("10.0 m", result[0].result)
            assertEquals("5.0 kg", result[1].result)
            assertError("Summation of `Meter` and `Kilogram` is not supported", result, 2)
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
            assertError("Average of `Meter` and `unitless number` is not supported", result, 2)
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
        testCalculate(
            "30 + 1 thousand",
            "1 thousand + 30",
            "4 dozen",
            "raw(4 dozen)",
            "4 dozens",
            "raw(4 dozens)",
            "1 dozen + 1",
            "raw(1 dozen + 1)"
        ) { result ->
            assertEquals("1030.0", result[0].result)
            assertEquals("1030.0", result[1].result)
            assertEquals("48.0", result[2].result)
            assertEquals("48", result[3].result)
            assertEquals("48.0", result[4].result)
            assertEquals("48", result[5].result)
            assertEquals("13.0", result[6].result)
            assertEquals("13", result[7].result)
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
            assertError("Addition of `Meter` and `unitless number` is not supported", result, 1)
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
            assertError("Addition of `Meter` and `unitless number` is not supported", result, 2)
        }
    }

    @Test
    fun `increment and decrement support values with units`() {
        testCalculate("distance = 10 km", "distance++", "distance--") { result ->
            assertEquals("11.0 km", result[1].result)
            assertEquals("10.0 km", result[2].result)
        }
    }

    @Test
    fun `multiply speed by time`() {
        testCalculate(
            "10 mps * 5 s",
            "5 s * 10 mps",
            "10 kmh * 5 h",
            "5 h * 10 kmh",
            "10 mph * 5 h",
            "5 h * 10 mph",
            "10 kn * 5 h",
            "5 h * 10 kn",
            "10 fps * 5 s",
            "5 s * 10 fps",
            "1 speed of light * 10 s",
            "10 s * 1 speed of light"
        ) { result ->
            assertEquals("50.0 m", result[0].result)
            assertEquals("50.0 m", result[1].result)
            assertEquals("50.0 km", result[2].result)
            assertEquals("50.0 km", result[3].result)
            assertEquals("50.0 mi", result[4].result)
            assertEquals("50.0 mi", result[5].result)
            // Near 50.0 NM (handles floating point discrepancies)
            assertResultNear("50.0 NM", result[6].result)
            assertResultNear("50.0 NM", result[7].result)
            assertEquals("50.0 ft", result[8].result)
            assertEquals("50.0 ft", result[9].result)
            assertEquals("2997924580.0 m", result[10].result)
            assertEquals("2997924580.0 m", result[11].result)
        }
    }

    @Test
    fun `multiply speed by non-matching time`() {
        testCalculate(
            "1 mps * 1 s",
            "1 mps * 1 ms",
            "1 mps * 1 µs",
            "1 mps * 1 ns",
            "1 mps * 1 min",
            "1 mps * 1 h",
            "1 mps * 1 d",
            "1 mps * 1 wk",
            "1 mps * 1 mo",
            "1 mps * 1 yr",
            "1 mps * 1 lustrum",
            "1 mps * 1 decade",
            "1 mps * 1 century",
            "1 mps * 1 millennium",
            "1 mps * 1 decisecond",
            "1 mps * 1 centisecond"
        ) { result ->
            assertEquals("1.0 m", result[0].result)
            assertEquals("0.001 m", result[1].result)
            assertEquals("1.0E-6 m", result[2].result)
            assertEquals("1.0E-9 m", result[3].result)
            assertEquals("60.0 m", result[4].result)
            assertEquals("3600.0 m", result[5].result)
            assertEquals("86400.0 m", result[6].result)
            assertEquals("604800.0 m", result[7].result)
            assertEquals("2629746.0 m", result[8].result)
            assertEquals("31556952.0 m", result[9].result)
            assertEquals("157784760.0 m", result[10].result)
            assertEquals("315569520.0 m", result[11].result)
            assertEquals("3155695200.0 m", result[12].result)
            assertEquals("31556952000.0 m", result[13].result)
            assertEquals("0.1 m", result[14].result)
            assertEquals("0.01 m", result[15].result)
        }
    }

    @Test
    fun `divide length by time`() {
        testCalculate(
            "10 m / 5 s",
            "10 km / 5 h",
            "10 mi / 5 h",
            "10 NM / 5 h",
            "10 ft / 5 s"
        ) { result ->
            assertEquals("2.0 mps", result[0].result)
            assertEquals("2.0 kmh", result[1].result)
            assertEquals("2.0 mph", result[2].result)
            assertResultNear("2.0 kn", result[3].result)
            assertEquals("2.0 fps", result[4].result)
        }
    }

    @Test
    fun `divide non-matching length by time`() {
        testCalculate(
            "1 nm / 1 s",
            "1 µm / 1 s",
            "1 mm / 1 s",
            "1 cm / 1 s",
            "1 dm / 1 s",
            "1 m / 1 s",
            "1 km / 1 h",
            "1 inch / 1 s",
            "1 ft / 1 s",
            "1 yd / 1 s",
            "1 mi / 1 h",
            "1 fur / 1 h",
            "1 ftm / 1 h",
            "1 NM / 1 h",
            "1 ly / 1 s",
            "1 Å / 1 s",
            "1 pm / 1 s",
            "1 au / 1 s"
        ) {result ->
            assertEquals("1.0E-9 mps", result[0].result)
            assertEquals("1.0E-6 mps", result[1].result)
            assertEquals("0.001 mps", result[2].result)
            assertEquals("0.01 mps", result[3].result)
            assertEquals("0.1 mps", result[4].result)
            assertEquals("1.0 mps", result[5].result)
            assertEquals("1.0 kmh", result[6].result)
            assertEquals("0.08333333333333333333333333333333333 fps", result[7].result)
            assertEquals("1.0 fps", result[8].result)
            assertEquals("3.0 fps", result[9].result)
            assertEquals("1.0 mph", result[10].result)
            assertEquals("0.125 mph", result[11].result)
            assertResultNear("0.000987473 kn", result[12].result)
            assertResultNear("1.0 kn", result[13].result)
            assertEquals("9.4607304725808E15 mps", result[14].result)
            assertEquals("1.0E-10 mps", result[15].result)
            assertEquals("1.0E-12 mps", result[16].result)
            assertEquals("149597870700.0 mps", result[17].result)
        }
    }

    @Test
    fun `divide length by non-matching time`() {
        testCalculate(
            "1 m / 1 s",
            "1 m / 1 ms",
            "1 m / 1 µs",
            "1 m / 1 ns",
            "1 m / 1 min",
            "1 m / 1 hr",
            "1 m / 1 d",
            "1 m / 1 wk",
            "1 m / 1 mo",
            "1 m / 1 yr",
            "1 m / 1 lustrum",
            "1 m / 1 decade",
            "1 m / 1 century",
            "1 m / 1 millennium",
            "1 m / 1 ds",
            "1 m / 1 cs"
        ) { result ->
            assertEquals("1.0 mps", result[0].result)
            assertEquals("1000.0 mps", result[1].result)
            assertEquals("1000000.0 mps", result[2].result)
            assertEquals("1000000000.0 mps", result[3].result)
            assertEquals("0.01666666666666666666666666666666667 mps", result[4].result)
            assertEquals("0.0002777777777777777777777777777777778 mps", result[5].result)
            assertEquals("0.00001157407407407407407407407407407407 mps", result[6].result)
            assertEquals("1.653439153439153439153439153439153E-6 mps", result[7].result)
            assertEquals("3.802648620817371715747452415556483E-7 mps", result[8].result)
            assertEquals("3.16887385068114309645621034629707E-8 mps", result[9].result)
            assertEquals("6.337747701362286192912420692594139E-9 mps", result[10].result)
            assertEquals("3.16887385068114309645621034629707E-9 mps", result[11].result)
            assertEquals("3.16887385068114309645621034629707E-10 mps", result[12].result)
            assertEquals("3.16887385068114309645621034629707E-11 mps", result[13].result)
            assertEquals("10.0 mps", result[14].result)
            assertEquals("100.0 mps", result[15].result)
        }
    }

    private fun assertResultNear(expected: String, actual: String, tolerance: Double = 1e-9) {
        val expectedParts = expected.split(" ")
        val actualParts = actual.split(" ")

        assertEquals("Unit mismatch", expectedParts.last(), actualParts.last())

        val expectedVal = expectedParts.first().toDouble()
        val actualVal = actualParts.first().toDouble()

        assertEquals("Value too far from expected", expectedVal, actualVal, tolerance)
    }

    @Test
    fun `length divided by time yields speed results`() {
        lengthSymbols.forEach { l ->
            timeSymbols.forEach { t ->
                testCalculate("1 $l / 1 $t") { result ->
                    assertFalse("Division failed for 1 $l / 1 $t", result[0].result == "Err")
                    assertTrue("Result for 1 $l / 1 $t is not a speed unit: ${result[0].result}",
                        isSpeedUnit(result[0].result))
                }
            }
        }
    }

    @Test
    fun `speed multiplied by time yields length results`() {
        speedSymbols.forEach { s ->
            timeSymbols.forEach { t ->
                testCalculate("1 $s * 1 $t", "1 $t * 1 $s") { result ->
                    assertFalse("Multiplication failed for 1 $s * 1 $t", result[0].result == "Err")
                    assertTrue("Result for 1 $s * 1 $t is not a length unit: ${result[0].result}",
                        isLengthUnit(result[0].result))

                    assertFalse("Multiplication failed for 1 $t * 1 $s", result[1].result == "Err")
                    assertTrue("Result for 1 $t * 1 $s is not a length unit: ${result[1].result}",
                        isLengthUnit(result[1].result))
                }
            }
        }
    }

    @Test
    fun `length divided by speed yields time results`() {
        lengthSymbols.forEach { l ->
            speedSymbols.forEach { s ->
                testCalculate("1 $l / 1 $s") { result ->
                    assertFalse("Division failed for 1 $l / 1 $s", result[0].result == "Err")
                    assertTrue("Result for 1 $l / 1 $s is not a time unit: ${result[0].result}",
                        isTimeUnit(result[0].result))
                }
            }
        }
    }
}
