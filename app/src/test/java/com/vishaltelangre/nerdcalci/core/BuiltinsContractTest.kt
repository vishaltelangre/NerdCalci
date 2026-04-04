package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BuiltinsContractTest {

    @Test
    fun `unit stripping helpers preserve display space value`() {
        val quantity = EvaluationResult(
            value = BigDecimal("5000"),
            unit = "km",
            rationalValue = Rational.toRational(BigDecimal("5000"))
        )

        val valueResult = Builtins.execute("value", listOf(quantity), emptyMap())
        val dropUnitResult = Builtins.execute("dropUnit", listOf(quantity), emptyMap())
        val rawResult = Builtins.execute("raw", listOf(quantity), emptyMap())

        assertEquals(BigDecimal("5"), valueResult.value)
        assertEquals(BigDecimal("5"), dropUnitResult.value)
        assertEquals(BigDecimal("5"), rawResult.value)
        assertEquals(null, valueResult.unit)
        assertEquals(null, dropUnitResult.unit)
        assertEquals(null, rawResult.unit)
        assertEquals(true, valueResult.explicitUnitless)
        assertEquals(true, dropUnitResult.explicitUnitless)
        assertEquals(true, rawResult.explicitUnitless)
    }

    @Test
    fun `display helpers keep value semantics`() {
        val quantity = EvaluationResult(
            value = BigDecimal("5"),
            unit = "km",
            rationalValue = Rational.toRational(BigDecimal("5"))
        )

        val rationalResult = Builtins.execute("rational", listOf(quantity), emptyMap())
        val floatResult = Builtins.execute("float", listOf(quantity), emptyMap())

        assertEquals(BigDecimal("5"), rationalResult.value)
        assertEquals("km", rationalResult.unit)
        assertEquals(true, rationalResult.explicitRational)
        assertEquals(true, floatResult.forceFloat)
        assertEquals("km", floatResult.unit)
    }

    @Test
    fun `angle helpers accept unitless and angle input`() {
        val radians = EvaluationResult(value = BigDecimal.ZERO, rationalValue = Rational.toRational(BigDecimal.ZERO))
        val degrees = EvaluationResult(value = BigDecimal.ZERO, unit = "°", rationalValue = Rational.toRational(BigDecimal.ZERO))

        val radiansResult = Builtins.execute("sin", listOf(radians), emptyMap())
        val degreesResult = Builtins.execute("sin", listOf(degrees), emptyMap())

        assertEquals("0", radiansResult.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("0", degreesResult.value?.stripTrailingZeros()?.toPlainString())
        assertEquals(null, radiansResult.unit)
        assertEquals(null, degreesResult.unit)
    }

    @Test
    fun `unitless only helpers reject dimensional input`() {
        val quantity = EvaluationResult(value = BigDecimal("10"), unit = "m", rationalValue = Rational.toRational(BigDecimal("10")))

        try {
            Builtins.execute("log", listOf(quantity), emptyMap())
        } catch (e: EvalException) {
            assertEquals("`log()` does not accept `Meter` type, pass a unitless value.", e.message)
            return
        }

        throw AssertionError("Expected EvalException")
    }

    @Test
    fun `unit transforming helpers derive output units`() {
        val sixteen = EvaluationResult(value = BigDecimal("16"), rationalValue = Rational.toRational(BigDecimal("16")))
        val twentySeven = EvaluationResult(value = BigDecimal("27"), rationalValue = Rational.toRational(BigDecimal("27")))
        val powResult = Builtins.execute(
            "pow",
            listOf(
                EvaluationResult(value = BigDecimal("2"), rationalValue = Rational.toRational(BigDecimal("2"))),
                EvaluationResult(value = BigDecimal("8"), rationalValue = Rational.toRational(BigDecimal("8")))
            ),
            emptyMap()
        )
        val sqrtResult = Builtins.execute("sqrt", listOf(sixteen), emptyMap())
        val cbrtResult = Builtins.execute("cbrt", listOf(twentySeven), emptyMap())

        assertEquals(BigDecimal("4"), sqrtResult.value?.stripTrailingZeros())
        assertEquals(null, sqrtResult.unit)
        assertEquals(BigDecimal("3"), cbrtResult.value?.stripTrailingZeros())
        assertEquals(null, cbrtResult.unit)
        assertEquals(BigDecimal("256"), powResult.value?.stripTrailingZeros())
        assertEquals(null, powResult.unit)
    }

    @Test
    fun `unit preserving helpers work across categories`() {
        val mass = EvaluationResult(
            value = BigDecimal("4000"),
            unit = "kg",
            rationalValue = Rational.toRational(BigDecimal("4000"))
        )
        val massFloor = Builtins.execute("floor", listOf(EvaluationResult(value = BigDecimal("2500"), unit = "kg", rationalValue = Rational.toRational(BigDecimal("2500")))), emptyMap())
        val massCeil = Builtins.execute("ceil", listOf(EvaluationResult(value = BigDecimal("2500"), unit = "kg", rationalValue = Rational.toRational(BigDecimal("2500")))), emptyMap())
        val massAbs = Builtins.execute("abs", listOf(EvaluationResult(value = BigDecimal("-4000"), unit = "kg", rationalValue = Rational.toRational(BigDecimal("-4000")))), emptyMap())
        val lengthAbs = Builtins.execute(
            "abs",
            listOf(EvaluationResult(value = BigDecimal("-1.2192"), unit = "ft", rationalValue = Rational.toRational(BigDecimal("-1.2192")))),
            emptyMap()
        )

        assertEquals("2000", massFloor.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("kg", massFloor.unit)
        assertEquals("3000", massCeil.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("kg", massCeil.unit)
        assertEquals("4000", massAbs.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("kg", massAbs.unit)
        assertEquals("1.2192", lengthAbs.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("ft", lengthAbs.unit)
        assertEquals("4000", mass.value?.stripTrailingZeros()?.toPlainString())
        assertEquals("kg", mass.unit)
    }
}
