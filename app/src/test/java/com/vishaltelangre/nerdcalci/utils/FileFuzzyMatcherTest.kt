package com.vishaltelangre.nerdcalci.utils

import org.junit.Assert.*
import org.junit.Test

class FileFuzzyMatcherTest {

    @Test
    fun `empty query returns zero score and empty indices`() {
        val result = FileFuzzyMatcher.fuzzyMatch("Calculator", "")
        assertNotNull(result)
        assertEquals(0, result?.score)
        assertTrue(result?.matchedIndices?.isEmpty() ?: false)
    }

    @Test
    fun `no match returns null`() {
        val result = FileFuzzyMatcher.fuzzyMatch("Calculator", "xyz")
        assertNull(result)
    }

    @Test
    fun `exact match returns highest score`() {
        val result = FileFuzzyMatcher.fuzzyMatch("Calculator", "Calculator")
        assertNotNull(result)
        assertTrue((result?.score ?: 0) >= 1000)
    }

    @Test
    fun `sparse fuzzy match returns valid results`() {
        val result = FileFuzzyMatcher.fuzzyMatch("Calculator", "clc")
        assertNotNull(result)
        assertEquals(listOf(0, 2, 3), result?.matchedIndices)
    }
}
