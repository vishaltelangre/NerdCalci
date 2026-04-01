package com.vishaltelangre.nerdcalci.ui.calculator

import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel
    private lateinit var fakeDao: FakeCalculatorDao

    @Before
    fun setup() {
        fakeDao = FakeCalculatorDao()
        viewModel = CalculatorViewModel(fakeDao)
        
        // Setup initial file
        runBlocking {
            fakeDao.insertFile(FileEntity(id = 1L, name = "TestFile"))
        }
    }

    @Test
    fun `splitLine split in middle of expression`() = runBlocking {
        // Given a line with "a = 10 + 20"
        val line = LineEntity(id = 1L, fileId = 1L, expression = "a = 10 + 20", result = "30.0", sortOrder = 0)
        fakeDao.insertLine(line)

        // When splitting at index 7 ("a = 10 " | "+ 20")
        // Index 7 is just before '+'
        viewModel.splitLine(1L, 7)

        // Then two lines should exist
        val lines = fakeDao.lines.value
        assertEquals(2, lines.size)

        // Original line updated
        val updatedLine = lines.find { it.id == 1L }!!
        assertEquals("a = 10 ", updatedLine.expression)
        assertEquals("10.0", updatedLine.result) // Recalculated

        // New line inserted
        val newLine = lines.find { it.id != 1L }!!
        assertEquals("+ 20", newLine.expression)
        assertEquals("Err", newLine.result) 
        assertEquals(1, newLine.sortOrder)
    }

    @Test
    fun `splitLine split at beginning of expression`() = runBlocking {
        // Given a line with "a = 10"
        val line = LineEntity(id = 1L, fileId = 1L, expression = "a = 10", result = "10.0", sortOrder = 0)
        fakeDao.insertLine(line)

        // When splitting at index 0
        viewModel.splitLine(1L, 0)

        val lines = fakeDao.lines.value
        assertEquals(2, lines.size)

        val originalLine = lines.find { it.id == 1L }!!
        assertEquals("", originalLine.expression)
        assertEquals("", originalLine.result)

        val newLine = lines.find { it.id != 1L }!!
        assertEquals("a = 10", newLine.expression)
        assertEquals("10.0", newLine.result)
    }

    @Test
    fun `splitLine split at end of expression`() = runBlocking {
        // Given a line with "a = 10"
        val line = LineEntity(id = 1L, fileId = 1L, expression = "a = 10", result = "10.0", sortOrder = 0)
        fakeDao.insertLine(line)

        // When splitting at end
        viewModel.splitLine(1L, 6)

        val lines = fakeDao.lines.value
        assertEquals(2, lines.size)

        val originalLine = lines.find { it.id == 1L }!!
        assertEquals("a = 10", originalLine.expression)
        assertEquals("10.0", originalLine.result)

        val newLine = lines.find { it.id != 1L }!!
        assertEquals("", newLine.expression)
        assertEquals("", newLine.result)
    }

    @Test
    fun `splitLine uses currentExpression when provided`() = runBlocking {
        // Given a line with "a = 10" in DB
        val line = LineEntity(id = 1L, fileId = 1L, expression = "a = 10", result = "10.0", sortOrder = 0)
        fakeDao.insertLine(line)

        // When splitting with "a = 10 + 20" (live buffer) at index 6 ("a = 10")
        viewModel.splitLine(1L, 6, "a = 10 + 20")

        val lines = fakeDao.lines.value
        assertEquals(2, lines.size)

        val originalLine = lines.find { it.id == 1L }!!
        assertEquals("a = 10", originalLine.expression)

        val newLine = lines.find { it.id != 1L }!!
        assertEquals(" + 20", newLine.expression)
    }

    @Test
    fun `mergeLines appends content and deletes current line`() = runBlocking {
        // Given two lines
        val line1 = LineEntity(id = 1L, fileId = 1L, expression = "a = 10", result = "10.0", sortOrder = 0)
        val line2 = LineEntity(id = 2L, fileId = 1L, expression = " + 20", result = "30.0", sortOrder = 1)
        fakeDao.insertLine(line1)
        fakeDao.insertLine(line2)

        // When merging line 2 into line 1
        viewModel.mergeLines(1L, 2L)

        // Then only one line should remain
        val lines = fakeDao.lines.value
        assertEquals(1, lines.size)

        val mergedLine = lines[0]
        assertEquals(1L, mergedLine.id)
        assertEquals("a = 10 + 20", mergedLine.expression)
        assertEquals("30.0", mergedLine.result) // Should be recalculated
    }

    @Test
    fun `mergeLines with empty current line just deletes it`() = runBlocking {
        val line1 = LineEntity(id = 1L, fileId = 1L, expression = "a = 10", result = "10.0", sortOrder = 0)
        val line2 = LineEntity(id = 2L, fileId = 1L, expression = "", result = "", sortOrder = 1)
        fakeDao.insertLine(line1)
        fakeDao.insertLine(line2)

        viewModel.mergeLines(1L, 2L)

        val lines = fakeDao.lines.value
        assertEquals(1, lines.size)
        assertEquals("a = 10", lines[0].expression)
        assertEquals("10.0", lines[0].result) // Should keep or re-calc to 10.0
    }

    @Test
    fun `updateLine increments version`() = runBlocking {
        // Given a line with version 10
        val line = LineEntity(id = 1L, fileId = 1L, expression = "x = 5", result = "5.0", sortOrder = 0, version = 10L)
        fakeDao.insertLine(line)

        // When updating the expression (using the suspend version for synchronous test)
        viewModel.updateLineInternal(line.copy(expression = "x = 10", version = 11L))

        // Then the version should be 12 (11 from UI + 1 from calculation loop)
        val updatedLine = fakeDao.getLineById(1L)!!
        assertEquals("x = 10", updatedLine.expression)
        assertEquals(12L, updatedLine.version)
    }

    private class FakeCalculatorDao : CalculatorDao() {
        private val _files = MutableStateFlow<List<FileEntity>>(emptyList())
        private val _lines = MutableStateFlow<List<LineEntity>>(emptyList())
        val lines = _lines.asStateFlow()
        
        private val fileIdGen = java.util.concurrent.atomic.AtomicLong(100L)
        private val lineIdGen = java.util.concurrent.atomic.AtomicLong(1000L)

        private fun nextFileId() = fileIdGen.incrementAndGet()
        private fun nextLineId() = lineIdGen.incrementAndGet()

        override fun getAllFiles(): Flow<List<FileEntity>> = _files
        override suspend fun getAllFilesSync(): List<FileEntity> = _files.value
        override suspend fun getFileById(fileId: Long): FileEntity? = _files.value.find { file: FileEntity -> file.id == fileId }
        override suspend fun getFileByName(name: String): FileEntity? = _files.value.find { file: FileEntity -> file.name == name }
        override suspend fun getFileBySyncId(syncId: String): FileEntity? = _files.value.find { file: FileEntity -> file.syncId == syncId }
        override suspend fun getPinnedFilesCount(): Int = _files.value.count { file: FileEntity -> file.isPinned }
        override suspend fun doesFileExist(name: String): Boolean = _files.value.any { file: FileEntity -> file.name == name }
        override suspend fun doesFileExist(name: String, excludeId: Long): Boolean = _files.value.any { file: FileEntity -> file.name == name && file.id != excludeId }

        override fun getLinesForFile(fileId: Long): Flow<List<LineEntity>> = 
            _lines.map { list: List<LineEntity> -> list.filter { line: LineEntity -> line.fileId == fileId }.sortedBy { line: LineEntity -> line.sortOrder } }

        override suspend fun getLinesForFileSync(fileId: Long): List<LineEntity> = 
            _lines.value.filter { line: LineEntity -> line.fileId == fileId }.sortedBy { line: LineEntity -> line.sortOrder }

        override suspend fun getLineById(lineId: Long): LineEntity? = 
            _lines.value.find { line: LineEntity -> line.id == lineId }

        override suspend fun getLineCountForFile(fileId: Long): Int = _lines.value.count { line: LineEntity -> line.fileId == fileId }

        override suspend fun insertFile(file: FileEntity): Long {
            val id = if (file.id == 0L) nextFileId() else file.id
            val newFile = file.copy(id = id)
            _files.value = _files.value + newFile
            return id
        }

        override suspend fun internalInsertLines(lines: List<LineEntity>) {
            val toInsert = lines.map { l: LineEntity -> 
                val id = if (l.id == 0L) nextLineId() else l.id
                l.copy(id = id)
            }
            _lines.value = _lines.value + toInsert
        }

        override suspend fun internalInsertLine(line: LineEntity): Long {
            val id = if (line.id == 0L) nextLineId() else line.id
            val newLine = line.copy(id = id)
            _lines.value = _lines.value + newLine
            return id
        }

        override suspend fun internalUpdateLine(line: LineEntity) {
            _lines.value = _lines.value.map { existing: LineEntity -> if (existing.id == line.id) line else existing }
        }

        override suspend fun internalUpdateLines(lines: List<LineEntity>) {
            val updates = lines.associateBy { l: LineEntity -> l.id }
            _lines.value = _lines.value.map { existing: LineEntity -> updates[existing.id] ?: existing }
        }

        override suspend fun internalDeleteLine(line: LineEntity) {
            _lines.value = _lines.value.filter { existing: LineEntity -> existing.id != line.id }
        }

        override suspend fun internalUpdateFile(file: FileEntity) {
            _files.value = _files.value.map { existing: FileEntity -> if (existing.id == file.id) file else existing }
        }

        override suspend fun internalUpdateFiles(files: List<FileEntity>) {
            val updates = files.associateBy { f: FileEntity -> f.id }
            _files.value = _files.value.map { existing: FileEntity -> updates[existing.id] ?: existing }
        }

        override suspend fun deleteFile(file: FileEntity) {
            _files.value = _files.value.filter { existing: FileEntity -> existing.id != file.id }
            _lines.value = _lines.value.filter { existing: LineEntity -> existing.fileId != file.id }
        }

        override suspend fun updateFileTimestamp(fileId: Long, timestamp: Long) {
            _files.value = _files.value.map { existing: FileEntity -> if (existing.id == fileId) existing.copy(lastModified = timestamp) else existing }
        }

        override suspend fun internalRenameFile(fileId: Long, name: String) {
            _files.value = _files.value.map { existing: FileEntity -> if (existing.id == fileId) existing.copy(name = name) else existing }
        }

        override suspend fun updateSyncId(fileId: Long, newSyncId: String) {
            _files.value = _files.value.map { existing: FileEntity -> if (existing.id == fileId) existing.copy(syncId = newSyncId) else existing }
        }

        override suspend fun internalDeleteLinesForFile(fileId: Long) {
            _lines.value = _lines.value.filter { line: LineEntity -> line.fileId != fileId }
        }

        override suspend fun touchFile(fileId: Long, timestamp: Long) {
            updateFileTimestamp(fileId, timestamp)
        }
    }
}
