package com.vishaltelangre.nerdcalci.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishaltelangre.nerdcalci.data.local.AppDatabase
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var dao: CalculatorDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.calculatorDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // ==================== File Operations ====================

    @Test
    fun insertFile_andRetrieveById() = runBlocking {
        val file = FileEntity(name = "Test File", lastModified = 1000L)
        val fileId = dao.insertFile(file)

        val retrieved = dao.getFileById(fileId)
        assertNotNull(retrieved)
        assertEquals("Test File", retrieved?.name)
        assertEquals(1000L, retrieved?.lastModified)
    }

    @Test
    fun insertMultipleFiles_andGetAll() = runBlocking {
        dao.insertFile(FileEntity(name = "File 1", lastModified = 1000L))
        dao.insertFile(FileEntity(name = "File 2", lastModified = 2000L))
        dao.insertFile(FileEntity(name = "File 3", lastModified = 3000L))

        val files = dao.getAllFiles().first()
        assertEquals(3, files.size)
    }

    @Test
    fun getAllFiles_orderedByPinnedThenModified() = runBlocking {
        dao.insertFile(FileEntity(name = "Old Unpinned", lastModified = 1000L, isPinned = false))
        dao.insertFile(FileEntity(name = "New Unpinned", lastModified = 3000L, isPinned = false))
        dao.insertFile(FileEntity(name = "Old Pinned", lastModified = 2000L, isPinned = true))

        val files = dao.getAllFiles().first()
        // Pinned files should come first
        assertEquals("Old Pinned", files[0].name)
        assertTrue(files[0].isPinned)
        // Then unpinned, ordered by lastModified DESC
        assertEquals("New Unpinned", files[1].name)
        assertEquals("Old Unpinned", files[2].name)
    }

    @Test
    fun updateFile_changesArePersisted() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Original", lastModified = 1000L))
        val file = dao.getFileById(fileId)!!

        val updated = file.copy(name = "Updated", lastModified = 2000L, isPinned = true)
        dao.updateFile(updated)

        val retrieved = dao.getFileById(fileId)
        assertEquals("Updated", retrieved?.name)
        assertEquals(2000L, retrieved?.lastModified)
        assertTrue(retrieved?.isPinned == true)
    }

    @Test
    fun deleteFile_removesFromDatabase() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "To Delete", lastModified = 1000L))
        val file = dao.getFileById(fileId)!!

        dao.deleteFile(file)

        val retrieved = dao.getFileById(fileId)
        assertNull(retrieved)
    }

    @Test
    fun getPinnedFilesCount_returnsCorrectCount() = runBlocking {
        dao.insertFile(FileEntity(name = "Pinned 1", lastModified = 1000L, isPinned = true))
        dao.insertFile(FileEntity(name = "Unpinned", lastModified = 2000L, isPinned = false))
        dao.insertFile(FileEntity(name = "Pinned 2", lastModified = 3000L, isPinned = true))

        val count = dao.getPinnedFilesCount()
        assertEquals(2, count)
    }

    // ==================== Line Operations ====================

    @Test
    fun insertLine_andRetrieveForFile() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "10 + 20", result = "30"))

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(1, lines.size)
        assertEquals("10 + 20", lines[0].expression)
        assertEquals("30", lines[0].result)
    }

    @Test
    fun insertMultipleLines_orderedBySortOrder() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 2, expression = "third"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "first"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "second"))

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals("first", lines[0].expression)
        assertEquals("second", lines[1].expression)
        assertEquals("third", lines[2].expression)
    }

    @Test
    fun updateLine_changesArePersisted() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        val lineId = dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "original", result = ""))

        val lines = dao.getLinesForFileSync(fileId)
        val updated = lines[0].copy(expression = "updated", result = "result")
        dao.updateLine(updated)

        val retrieved = dao.getLinesForFileSync(fileId)
        assertEquals("updated", retrieved[0].expression)
        assertEquals("result", retrieved[0].result)
    }

    @Test
    fun deleteLine_removesFromDatabase() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "keep"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "delete"))

        val lines = dao.getLinesForFileSync(fileId)
        dao.deleteLine(lines[1])

        val remaining = dao.getLinesForFileSync(fileId)
        assertEquals(1, remaining.size)
        assertEquals("keep", remaining[0].expression)
    }

    @Test
    fun getLinesForFile_flowEmitsUpdates() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "line1"))

        // Get initial state
        val lines1 = dao.getLinesForFile(fileId).first()
        assertEquals(1, lines1.size)

        // Add another line
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "line2"))

        // Flow should emit updated state
        val lines2 = dao.getLinesForFile(fileId).first()
        assertEquals(2, lines2.size)
    }

    // ==================== Foreign Key Cascade ====================

    @Test
    fun deleteFile_cascadesDeleteLines() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "line1"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "line2"))

        val file = dao.getFileById(fileId)!!
        dao.deleteFile(file)

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(0, lines.size)
    }

    // ==================== Multiple Files Isolation ====================

    @Test
    fun linesAreIsolatedBetweenFiles() = runBlocking {
        val file1Id = dao.insertFile(FileEntity(name = "File 1", lastModified = 1000L))
        val file2Id = dao.insertFile(FileEntity(name = "File 2", lastModified = 2000L))

        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 0, expression = "file1 line1"))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 1, expression = "file1 line2"))
        dao.insertLine(LineEntity(fileId = file2Id, sortOrder = 0, expression = "file2 line1"))

        val file1Lines = dao.getLinesForFileSync(file1Id)
        val file2Lines = dao.getLinesForFileSync(file2Id)

        assertEquals(2, file1Lines.size)
        assertEquals(1, file2Lines.size)
        assertTrue(file1Lines.all { it.expression.startsWith("file1") })
        assertTrue(file2Lines.all { it.expression.startsWith("file2") })
    }

    // ==================== Edge Cases ====================

    @Test
    fun getFileById_nonExistentReturnsNull() = runBlocking {
        val file = dao.getFileById(999L)
        assertNull(file)
    }

    @Test
    fun getLinesForFile_emptyFileReturnsEmptyList() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Empty", lastModified = 1000L))
        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(0, lines.size)
    }

    @Test
    fun insertLine_withEmptyExpression() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "", result = ""))

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals("", lines[0].expression)
    }

    @Test
    fun insertFile_withVeryLongName() = runBlocking {
        val longName = "A".repeat(500)
        val fileId = dao.insertFile(FileEntity(name = longName, lastModified = 1000L))

        val file = dao.getFileById(fileId)
        assertEquals(longName, file?.name)
    }

    @Test
    fun updateFile_togglePinStatus() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L, isPinned = false))
        var file = dao.getFileById(fileId)!!

        // Pin it
        dao.updateFile(file.copy(isPinned = true))
        file = dao.getFileById(fileId)!!
        assertTrue(file.isPinned)

        // Unpin it
        dao.updateFile(file.copy(isPinned = false))
        file = dao.getFileById(fileId)!!
        assertFalse(file.isPinned)
    }

    @Test
    fun insertLine_negativeSortOrder() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = -1, expression = "negative"))

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(-1, lines[0].sortOrder)
    }

    @Test
    fun replaceFile_withSameId() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Original", lastModified = 1000L))
        // Insert with same ID (REPLACE strategy)
        dao.insertFile(FileEntity(id = fileId, name = "Replaced", lastModified = 2000L))

        val file = dao.getFileById(fileId)
        assertEquals("Replaced", file?.name)
    }

    // ==================== Complex Scenarios ====================

    @Test
    fun complexScenario_multipleFilesAndLines() = runBlocking {
        // Create multiple files with multiple lines
        val file1Id = dao.insertFile(FileEntity(name = "Budget", lastModified = 1000L, isPinned = true))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 0, expression = "income = 5000"))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 1, expression = "expenses = 3000"))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 2, expression = "income - expenses"))

        val file2Id = dao.insertFile(FileEntity(name = "Shopping", lastModified = 2000L, isPinned = false))
        dao.insertLine(LineEntity(fileId = file2Id, sortOrder = 0, expression = "price = 100"))
        dao.insertLine(LineEntity(fileId = file2Id, sortOrder = 1, expression = "20% off price"))

        // Verify file ordering
        val files = dao.getAllFiles().first()
        assertEquals(2, files.size)
        assertEquals("Budget", files[0].name) // Pinned comes first
        assertEquals("Shopping", files[1].name)

        // Verify line counts
        val budgetLines = dao.getLinesForFileSync(file1Id)
        val shoppingLines = dao.getLinesForFileSync(file2Id)
        assertEquals(3, budgetLines.size)
        assertEquals(2, shoppingLines.size)

        // Delete one file
        dao.deleteFile(files[1])

        // Verify only one file remains
        val remainingFiles = dao.getAllFiles().first()
        assertEquals(1, remainingFiles.size)
        assertEquals("Budget", remainingFiles[0].name)

        // Verify its lines are still intact
        val remainingLines = dao.getLinesForFileSync(file1Id)
        assertEquals(3, remainingLines.size)
    }

    @Test
    fun stressTest_manyLinesInOneFile() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Large File", lastModified = 1000L))

        // Insert 100 lines
        for (i in 0 until 100) {
            dao.insertLine(LineEntity(fileId = fileId, sortOrder = i, expression = "line $i"))
        }

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(100, lines.size)
        assertEquals("line 0", lines[0].expression)
        assertEquals("line 99", lines[99].expression)
    }
}
