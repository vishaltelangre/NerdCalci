package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.UnitCategory
import org.junit.Test
import org.junit.Assert.*

class SuggestionContextTest {

    @Test
    fun `test attached unit suggestion trigger`() {
        val text = "10k"
        val cursorPos = 3
        val beforeCursor = "10k"
        val context = getSuggestionContext(beforeCursor, text, cursorPos, emptyMap())
        assertEquals("k", context.word)
        assertEquals(SuggestionType.VARIABLE, context.type)
        assertEquals(2, context.replaceStart)
    }

    @Test
    fun `test explicit unit conversion trigger`() {
        val text = "10 kg to "
        val cursorPos = 9
        val beforeCursor = "10 kg to "
        val context = getSuggestionContext(beforeCursor, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertTrue(context.isExplicitTrigger)
        assertEquals(UnitCategory.MASS, context.unitCategory)
    }

    @Test
    fun `test keyword suggestion trigger after quantity`() {
        val text = "10 kg t"
        val cursorPos = 7
        val beforeCursor = "10 kg t"
        val context = getSuggestionContext(beforeCursor, text, cursorPos, emptyMap())
        assertEquals("t", context.word)
        assertEquals(SuggestionType.KEYWORD, context.type)
    }

    @Test
    fun `test convert 2nd argument triggers unit suggestions`() {
        val text = "convert(10,"
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertEquals(2, context.argumentIndex)
    }

    @Test
    fun `test convert 3rd argument triggers context-aware unit suggestions`() {
        val text = "convert(10, \"kg\","
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertEquals(3, context.argumentIndex)
        assertEquals(UnitCategory.MASS, context.unitCategory)
    }

    @Test
    fun `test convert with unclosed string literal`() {
        val text = "convert(10, \"kg\", \"m"
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("m", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertEquals(3, context.argumentIndex)
        assertEquals(UnitCategory.MASS, context.unitCategory)
    }

    @Test
    fun `test file path suggestion trigger`() {
        val text = "file(\""
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.FILE, context.type)
        assertTrue(context.isExplicitTrigger)
    }

    @Test
    fun `test file path suggestion with partial path`() {
        val text = "file(\"doc"
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("doc", context.word)
        assertEquals(SuggestionType.FILE, context.type)
    }
    @Test
    fun `test number conversion trigger`() {
        val text = "15 in "
        val cursorPos = 6
        val beforeCursor = "15 in "
        val context = getSuggestionContext(beforeCursor, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertTrue(context.isExplicitTrigger)
        assertNull(context.unitCategory)
    }
    @Test
    fun `test scalar multiplier conversion trigger`() {
        val text = "10 thousand in "
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertTrue(context.isExplicitTrigger)
        assertEquals(UnitCategory.SCALAR, context.unitCategory)
    }

    @Test
    fun `test parenthesized expression conversion trigger`() {
        val text = "(35 °C + 10 degF) in "
        val cursorPos = text.length
        val context = getSuggestionContext(text, text, cursorPos, emptyMap())
        assertEquals("", context.word)
        assertEquals(SuggestionType.UNIT, context.type)
        assertTrue(context.isExplicitTrigger)
        assertNull(context.unitCategory)
    }
}
