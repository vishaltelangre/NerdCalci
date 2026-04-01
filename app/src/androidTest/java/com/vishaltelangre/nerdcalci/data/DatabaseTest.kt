package com.vishaltelangre.nerdcalci.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishaltelangre.nerdcalci.data.local.AppDatabase
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    fun updateFile_changesArePersistedAndTimestampUpdated() = runBlocking {
        val beforeUpdate = System.currentTimeMillis()
        Thread.sleep(10) // Ensure timestamp changes

        val fileId = dao.insertFile(FileEntity(name = "Original", lastModified = beforeUpdate))
        val file = dao.getFileById(fileId)!!

        val updated = file.copy(name = "Updated", isPinned = true)
        dao.updateFile(updated)

        val retrieved = dao.getFileById(fileId)
        assertEquals("Updated", retrieved?.name)
        assertTrue(retrieved!!.lastModified > beforeUpdate)
        assertTrue(retrieved.isPinned)
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

    @Test
    fun togglePinFileIfAllowed_underLimit_pinsFile() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Unpinned", lastModified = 1000L, isPinned = false))

        val result = dao.togglePinFileIfAllowed(fileId, maxPinned = 2)
        assertTrue(result)

        val file = dao.getFileById(fileId)
        assertTrue(file!!.isPinned)
    }

    @Test
    fun togglePinFileIfAllowed_atLimit_unpinsFile() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Pinned", lastModified = 1000L, isPinned = true))

        // Unpinning should be allowed regardless of max pinned constraint
        val result = dao.togglePinFileIfAllowed(fileId, maxPinned = 1)
        assertTrue(result)

        val file = dao.getFileById(fileId)
        assertFalse(file!!.isPinned)
    }

    @Test
    fun togglePinFileIfAllowed_overLimit_rejectsPinning() = runBlocking {
        dao.insertFile(FileEntity(name = "Pinned 1", lastModified = 1000L, isPinned = true))
        dao.insertFile(FileEntity(name = "Pinned 2", lastModified = 2000L, isPinned = true))
        val unpinnedId = dao.insertFile(FileEntity(name = "Unpinned", lastModified = 3000L, isPinned = false))

        val result = dao.togglePinFileIfAllowed(unpinnedId, maxPinned = 2)
        assertFalse(result)

        val file = dao.getFileById(unpinnedId)
        assertFalse(file!!.isPinned)
    }

    @Test
    fun togglePinFileIfAllowed_concurrentPin_onlyOneSucceeds() = runBlocking {
        dao.insertFile(FileEntity(name = "Pinned 1", lastModified = 1000L, isPinned = true))

        val fileIdA = dao.insertFile(FileEntity(name = "File A", lastModified = 2000L, isPinned = false))
        val fileIdB = dao.insertFile(FileEntity(name = "File B", lastModified = 3000L, isPinned = false))

        val maxPinned = 2

        // Concurrently launch two coroutines on Dispatchers.IO to ensure multi-threaded execution
        val defA = async(Dispatchers.IO) { dao.togglePinFileIfAllowed(fileIdA, maxPinned) }
        val defB = async(Dispatchers.IO) { dao.togglePinFileIfAllowed(fileIdB, maxPinned) }

        val resultA = defA.await()
        val resultB = defB.await()

        // Exactly one should return true (since we only had room for 1 more pinned file)
        assertTrue(resultA || resultB)
        assertFalse(resultA && resultB)

        val finalPinnedCount = dao.getPinnedFilesCount()
        assertEquals(maxPinned, finalPinnedCount)

        val fileA = dao.getFileById(fileIdA)!!
        val fileB = dao.getFileById(fileIdB)!!

        if (resultA) {
            assertTrue(fileA.isPinned)
            assertFalse(fileB.isPinned)
        } else {
            assertFalse(fileA.isPinned)
            assertTrue(fileB.isPinned)
        }
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
    fun insertMultipleLines_withDuplicateSortOrder_orderedById() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        // Lines with same sortOrder but different IDs (IDs assigned sequentially by autoGenerate)
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 5, expression = "line 1"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 5, expression = "line 2"))

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(2, lines.size)
        // Since sortOrder is both 5, the sorting should fallback to id ASC
        assertEquals("line 1", lines[0].expression)
        assertEquals("line 2", lines[1].expression)
        assertTrue(lines[0].id < lines[1].id)
    }

    @Test
    fun updateLine_changesArePersistedAndFileTouched() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        val lineId = dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "original", result = ""))

        val lines = dao.getLinesForFileSync(fileId)
        val updated = lines[0].copy(expression = "updated", result = "result")

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.updateLine(updated)

        val retrieved = dao.getLinesForFileSync(fileId)
        assertEquals("updated", retrieved[0].expression)

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }

    @Test
    fun updateLines_allChangesPersistedInBatchAndFileTouched() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "1 + 1", result = ""))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "2 + 2", result = ""))

        val lines = dao.getLinesForFileSync(fileId)
        val updated = lines.map { it.copy(result = "DONE") }

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.updateLines(fileId, updated)

        val retrieved = dao.getLinesForFileSync(fileId)
        assertTrue(retrieved.all { it.result == "DONE" })

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }

    // Opening a file doesn't bump modified timestamp
    @Test
    fun updateLines_withUpdateTimestampFalse_doesNotTouchFile() = runBlocking {
        val initialTimestamp = 1000L
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = initialTimestamp))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "1 + 1", result = ""))

        // insertLine touches the file, so capture the new timestamp
        val postInsertTimestamp = dao.getFileById(fileId)!!.lastModified
        assertNotEquals(initialTimestamp, postInsertTimestamp)

        val lines = dao.getLinesForFileSync(fileId)
        val updated = lines.map { it.copy(result = "DONE") }

        Thread.sleep(10)
        dao.updateLines(fileId, updated, updateTimestamp = false)

        val retrieved = dao.getLinesForFileSync(fileId)
        assertTrue(retrieved.all { it.result == "DONE" })

        val file = dao.getFileById(fileId)
        assertEquals(postInsertTimestamp, file!!.lastModified)
    }

    @Test
    fun updateLines_emptyListIsNoOp() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Test", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "1 + 1", result = "2"))

        // Should not throw and should leave existing data untouched
        dao.updateLines(fileId, emptyList())

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(1, lines.size)
        assertEquals("2", lines[0].result)
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

    @Test
    fun createNewFile_isAtomicAndGeneratesUniqueName() = runBlocking {
        val now = System.currentTimeMillis()
        val id1 = dao.createNewFile("Untitled", now)
        val id2 = dao.createNewFile("Untitled", now)

        val file1 = dao.getFileById(id1)
        val file2 = dao.getFileById(id2)

        assertEquals("Untitled", file1?.name)
        assertEquals("Untitled (1)", file2?.name)

        // Verify initial line
        val lines1 = dao.getLinesForFileSync(id1)
        assertEquals(1, lines1.size)
        assertEquals("", lines1[0].expression)
    }

    @Test
    fun duplicateFile_isAtomicAndCopiesAllLines() = runBlocking {
        val file1Id = dao.insertFile(FileEntity(name = "Source", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 0, expression = "1+1", result = "2"))
        dao.insertLine(LineEntity(fileId = file1Id, sortOrder = 1, expression = "2+2", result = "4"))

        val now = System.currentTimeMillis()
        val copyId = dao.duplicateFile(file1Id, now)!!

        val copyFile = dao.getFileById(copyId)
        assertEquals("Copy of Source", copyFile?.name)

        val copyLines = dao.getLinesForFileSync(copyId)
        assertEquals(2, copyLines.size)
        assertEquals("1+1", copyLines[0].expression)
        assertEquals("2+2", copyLines[1].expression)
    }

    @Test
    fun clearAllLines_removesEverythingAndAddsOneEmptyLine() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "To Clear", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "1+1"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "2+2"))

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.clearAllLines(fileId)

        val lines = dao.getLinesForFileSync(fileId)
        assertEquals(1, lines.size)
        assertEquals("", lines[0].expression)
        assertEquals(0, lines[0].sortOrder)

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }

    @Test
    fun moveAndInsertLine_repairsDuplicateSortOrders() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Repair", lastModified = 1000L))
        // Seed with duplicates and non-sequential sortOrder
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "A"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "B"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 5, expression = "C"))

        val anchorLine = dao.getLinesForFileSync(fileId)[1] // "B"
        val newLine = LineEntity(fileId = fileId, sortOrder = 0, expression = "NEW")

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.moveAndInsertLine(fileId, anchorLine.id, newLine)

        val finalLines = dao.getLinesForFileSync(fileId)
        assertEquals(4, finalLines.size)
        // Should be: A(0), B(1), NEW(2), C(3)
        assertEquals("A", finalLines[0].expression)
        assertEquals(0, finalLines[0].sortOrder)
        assertEquals("B", finalLines[1].expression)
        assertEquals(1, finalLines[1].sortOrder)
        assertEquals("NEW", finalLines[2].expression)
        assertEquals(2, finalLines[2].sortOrder)
        assertEquals("C", finalLines[3].expression)
        assertEquals(3, finalLines[3].sortOrder)

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }

    @Test
    fun moveAndInsertLine_positioningSemantics() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Position", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "1"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "2"))

        // afterLineId == null -> insert at top
        dao.moveAndInsertLine(fileId, null, LineEntity(fileId = fileId, sortOrder = 0, expression = "TOP"))
        var lines = dao.getLinesForFileSync(fileId)
        assertEquals("TOP", lines[0].expression)
        assertEquals(0, lines[0].sortOrder)

        // afterLineId matching existing line -> insert below it
        val anchorId = lines[1].id // "1"
        dao.moveAndInsertLine(fileId, anchorId, LineEntity(fileId = fileId, sortOrder = 0, expression = "MIDDLE"))
        lines = dao.getLinesForFileSync(fileId)
        // Expected: TOP, 1, MIDDLE, 2
        assertEquals("MIDDLE", lines[2].expression)

        // afterLineId missing -> append at end
        dao.moveAndInsertLine(fileId, 999L, LineEntity(fileId = fileId, sortOrder = 0, expression = "END"))
        lines = dao.getLinesForFileSync(fileId)
        assertEquals("END", lines.last().expression)
        assertEquals(lines.size - 1, lines.last().sortOrder)
    }

    @Test
    fun deleteAndNormalize_repairsSortOrders() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Delete", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "0"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 1, expression = "1"))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 2, expression = "2"))

        val lineToDelete = dao.getLinesForFileSync(fileId)[1] // "1"

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.deleteAndNormalize(lineToDelete)

        val remaining = dao.getLinesForFileSync(fileId)
        assertEquals(2, remaining.size)
        assertEquals(0, remaining[0].sortOrder)
        assertEquals(1, remaining[1].sortOrder)
        assertEquals("0", remaining[0].expression)
        assertEquals("2", remaining[1].expression)

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }

    @Test
    fun restoreLines_isAtomicAndNormalized() = runBlocking {
        val fileId = dao.insertFile(FileEntity(name = "Restore", lastModified = 1000L))
        dao.insertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "OLD"))

        val snapshots = listOf(
            LineEntity(fileId = fileId, sortOrder = 99, expression = "NEW 1"),
            LineEntity(fileId = fileId, sortOrder = 100, expression = "NEW 2")
        )

        val beforeTouch = System.currentTimeMillis()
        Thread.sleep(10)
        dao.restoreLines(fileId, snapshots)

        val current = dao.getLinesForFileSync(fileId)
        assertEquals(2, current.size)
        assertEquals("NEW 1", current[0].expression)
        assertEquals(0, current[0].sortOrder) // Should be normalized to 0
        assertEquals("NEW 2", current[1].expression)
        assertEquals(1, current[1].sortOrder) // Should be normalized to 1

        val file = dao.getFileById(fileId)
        assertTrue(file!!.lastModified >= beforeTouch)
    }
}
