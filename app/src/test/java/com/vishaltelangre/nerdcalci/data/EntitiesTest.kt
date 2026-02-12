package com.vishaltelangre.nerdcalci.data

import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.junit.Assert.*
import org.junit.Test

class EntitiesTest {

    // ==================== FileEntity Tests ====================

    @Test
    fun `FileEntity creation with all parameters`() {
        val file = FileEntity(
            id = 1L,
            name = "Test File",
            lastModified = 1234567890L,
            isPinned = true
        )

        assertEquals(1L, file.id)
        assertEquals("Test File", file.name)
        assertEquals(1234567890L, file.lastModified)
        assertTrue(file.isPinned)
    }

    @Test
    fun `FileEntity defaults to unpinned`() {
        val file = FileEntity(name = "Test", lastModified = 0L)
        assertFalse(file.isPinned)
    }

    @Test
    fun `FileEntity id defaults to 0`() {
        val file = FileEntity(name = "Test", lastModified = 0L)
        assertEquals(0L, file.id)
    }

    @Test
    fun `FileEntity copy updates only specified fields`() {
        val original = FileEntity(id = 1L, name = "Original", lastModified = 1000L, isPinned = false)
        val updated = original.copy(name = "Updated", isPinned = true)

        assertEquals(1L, updated.id)
        assertEquals("Updated", updated.name)
        assertEquals(1000L, updated.lastModified) // unchanged
        assertTrue(updated.isPinned)
    }

    @Test
    fun `FileEntity with empty name is allowed`() {
        val file = FileEntity(name = "", lastModified = 0L)
        assertEquals("", file.name)
    }

    @Test
    fun `FileEntity with long name is preserved`() {
        val longName = "A".repeat(100)
        val file = FileEntity(name = longName, lastModified = 0L)
        assertEquals(longName, file.name)
    }

    @Test
    fun `FileEntity lastModified timestamp can be current time`() {
        val now = System.currentTimeMillis()
        val file = FileEntity(name = "Test", lastModified = now)
        assertTrue(file.lastModified <= System.currentTimeMillis())
        assertTrue(file.lastModified >= now - 1000) // Within 1 second tolerance
    }

    // ==================== LineEntity Tests ====================

    @Test
    fun `LineEntity creation with all parameters`() {
        val line = LineEntity(
            id = 1L,
            fileId = 10L,
            sortOrder = 5,
            expression = "10 + 20",
            result = "30"
        )

        assertEquals(1L, line.id)
        assertEquals(10L, line.fileId)
        assertEquals(5, line.sortOrder)
        assertEquals("10 + 20", line.expression)
        assertEquals("30", line.result)
    }

    @Test
    fun `LineEntity result defaults to empty string`() {
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = "test")
        assertEquals("", line.result)
    }

    @Test
    fun `LineEntity id defaults to 0`() {
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = "test")
        assertEquals(0L, line.id)
    }

    @Test
    fun `LineEntity copy updates only specified fields`() {
        val original = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "original", result = "old")
        val updated = original.copy(expression = "updated", result = "new")

        assertEquals(1L, updated.id)
        assertEquals(1L, updated.fileId)
        assertEquals(0, updated.sortOrder)
        assertEquals("updated", updated.expression)
        assertEquals("new", updated.result)
    }

    @Test
    fun `LineEntity with empty expression is allowed`() {
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = "")
        assertEquals("", line.expression)
    }

    @Test
    fun `LineEntity with multiline expression is preserved`() {
        val multiline = "line1\nline2\nline3"
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = multiline)
        assertEquals(multiline, line.expression)
    }

    @Test
    fun `LineEntity sortOrder can be negative`() {
        val line = LineEntity(fileId = 1L, sortOrder = -1, expression = "test")
        assertEquals(-1, line.sortOrder)
    }

    @Test
    fun `LineEntity sortOrder can be very large`() {
        val line = LineEntity(fileId = 1L, sortOrder = Int.MAX_VALUE, expression = "test")
        assertEquals(Int.MAX_VALUE, line.sortOrder)
    }

    @Test
    fun `LineEntity with error result`() {
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = "bad expression", result = "Err")
        assertEquals("Err", line.result)
    }

    @Test
    fun `LineEntity expression with special characters`() {
        val expression = "price × 2 ÷ 3 # comment"
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = expression)
        assertEquals(expression, line.expression)
    }

    @Test
    fun `LineEntity with very long expression`() {
        val longExpression = "1 + 2 + 3 + 4 + 5 + ".repeat(100)
        val line = LineEntity(fileId = 1L, sortOrder = 0, expression = longExpression)
        assertEquals(longExpression, line.expression)
    }

    // ==================== Entity Relationships ====================

    @Test
    fun `Multiple LineEntities can reference same FileEntity`() {
        val fileId = 1L
        val line1 = LineEntity(fileId = fileId, sortOrder = 0, expression = "expr1")
        val line2 = LineEntity(fileId = fileId, sortOrder = 1, expression = "expr2")
        val line3 = LineEntity(fileId = fileId, sortOrder = 2, expression = "expr3")

        assertEquals(fileId, line1.fileId)
        assertEquals(fileId, line2.fileId)
        assertEquals(fileId, line3.fileId)
    }

    @Test
    fun `LineEntity sortOrder determines line position`() {
        val lines = listOf(
            LineEntity(fileId = 1L, sortOrder = 2, expression = "third"),
            LineEntity(fileId = 1L, sortOrder = 0, expression = "first"),
            LineEntity(fileId = 1L, sortOrder = 1, expression = "second")
        )

        val sorted = lines.sortedBy { it.sortOrder }
        assertEquals("first", sorted[0].expression)
        assertEquals("second", sorted[1].expression)
        assertEquals("third", sorted[2].expression)
    }

    // ==================== Data Integrity ====================

    @Test
    fun `FileEntity equality based on all fields`() {
        val file1 = FileEntity(id = 1L, name = "Test", lastModified = 1000L, isPinned = false)
        val file2 = FileEntity(id = 1L, name = "Test", lastModified = 1000L, isPinned = false)
        assertEquals(file1, file2)
    }

    @Test
    fun `FileEntity inequality when fields differ`() {
        val file1 = FileEntity(id = 1L, name = "Test1", lastModified = 1000L)
        val file2 = FileEntity(id = 1L, name = "Test2", lastModified = 1000L)
        assertNotEquals(file1, file2)
    }

    @Test
    fun `LineEntity equality based on all fields`() {
        val line1 = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "test", result = "result")
        val line2 = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "test", result = "result")
        assertEquals(line1, line2)
    }

    @Test
    fun `LineEntity inequality when fields differ`() {
        val line1 = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "test1", result = "")
        val line2 = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "test2", result = "")
        assertNotEquals(line1, line2)
    }

    @Test
    fun `FileEntity hashCode consistency`() {
        val file = FileEntity(id = 1L, name = "Test", lastModified = 1000L)
        val hashCode1 = file.hashCode()
        val hashCode2 = file.hashCode()
        assertEquals(hashCode1, hashCode2)
    }

    @Test
    fun `LineEntity hashCode consistency`() {
        val line = LineEntity(id = 1L, fileId = 1L, sortOrder = 0, expression = "test")
        val hashCode1 = line.hashCode()
        val hashCode2 = line.hashCode()
        assertEquals(hashCode1, hashCode2)
    }
}
