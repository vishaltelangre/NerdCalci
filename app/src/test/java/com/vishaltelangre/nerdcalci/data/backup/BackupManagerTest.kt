package com.vishaltelangre.nerdcalci.data.backup

import android.content.Context
import android.content.SharedPreferences
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.data.sync.SyncManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManagerTest {
    
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockPrefs = mockk<SharedPreferences>(relaxed = true)

    init {
        every { mockContext.getSharedPreferences(SyncManager.PREFS_NAME, any()) } returns mockPrefs
        every { mockPrefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, any()) } returns false
    }

    // Fake DAO Implementation
    class FakeCalculatorDao : CalculatorDao() {
        val files = mutableListOf<FileEntity>()
        val lines = mutableListOf<LineEntity>()
        var nextFileId = 1L
        var nextLineId = 1L

        override fun getAllFiles(): Flow<List<FileEntity>> = flowOf(files.filter { !it.isTemporary }.toList())
        override suspend fun getTemporaryFile(): FileEntity? = files.find { it.isTemporary }
        override suspend fun getUntitledFileNames(): List<String> =
            files.filter { !it.isTemporary && it.name.startsWith("Untitled ") }.map { it.name }
        override suspend fun getFileById(fileId: Long): FileEntity? = files.find { it.id == fileId }
        override suspend fun getFileByName(name: String): FileEntity? = files.find { it.name == name }
        override suspend fun getFileBySyncId(syncId: String): FileEntity? = files.find { it.syncId == syncId }
        override suspend fun getPinnedFilesCount(): Int = files.count { it.isPinned }
        override suspend fun doesFileExist(name: String): Boolean = files.any { it.name == name }
        override suspend fun doesFileExist(name: String, excludeId: Long): Boolean =
            files.any { it.name == name && it.id != excludeId }

        override fun getLinesForFile(fileId: Long): Flow<List<LineEntity>> =
            flowOf(lines.filter { it.fileId == fileId })

        override suspend fun getLinesForFileSync(fileId: Long): List<LineEntity> =
            lines.filter { it.fileId == fileId }

        override suspend fun getLineCountForFile(fileId: Long): Int =
            lines.count { it.fileId == fileId }

        override suspend fun getLineById(lineId: Long): LineEntity? =
            lines.find { it.id == lineId }

        override suspend fun insertFile(file: FileEntity): Long {
            val id = if (file.id == 0L) nextFileId++ else file.id
            val newFile = file.copy(id = id)
            files.add(newFile)
            return id
        }

        public override suspend fun internalInsertLines(lines: List<LineEntity>) {
            lines.forEach { line ->
                val id = if (line.id == 0L) nextLineId++ else line.id
                this.lines.add(line.copy(id = id))
            }
        }

        override suspend fun internalInsertLine(line: LineEntity): Long {
            val id = if (line.id == 0L) nextLineId++ else line.id
            lines.add(line.copy(id = id))
            return id
        }

        override suspend fun internalUpdateLine(line: LineEntity) {
            val idx = lines.indexOfFirst { it.id == line.id }
            if (idx >= 0) lines[idx] = line
        }

        override suspend fun internalUpdateLines(lines: List<LineEntity>) {
            lines.forEach { internalUpdateLine(it) }
        }

        override suspend fun internalUpdateFiles(files: List<FileEntity>) {
            files.forEach { internalUpdateFile(it) }
        }

        override suspend fun internalDeleteLine(line: LineEntity) {
            lines.removeAll { it.id == line.id }
        }

        override suspend fun internalUpdateFile(file: FileEntity) {
            val idx = files.indexOfFirst { it.id == file.id }
            if (idx >= 0) files[idx] = file
        }

        override suspend fun updateFileFromSync(file: FileEntity) {
            internalUpdateFile(file)
        }

        override suspend fun internalRenameFile(fileId: Long, name: String) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx >= 0) files[idx] = files[idx].copy(name = name)
        }

        override suspend fun updateSyncId(fileId: Long, newSyncId: String) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx >= 0) files[idx] = files[idx].copy(syncId = newSyncId)
        }

        override suspend fun duplicateFile(fileId: Long, newName: String, newSyncId: String, lastModified: Long?): Long {
            val original = files.find { it.id == fileId } ?: return -1L
            val nextId = nextFileId++
            val copy = original.copy(
                id = nextId,
                name = newName,
                syncId = newSyncId,
                lastModified = lastModified ?: System.currentTimeMillis()
            )
            files.add(copy)

            val originalLines = lines.filter { it.fileId == fileId }
            val newLines = originalLines.map { it.copy(id = nextLineId++, fileId = nextId) }
            lines.addAll(newLines)
            return nextId
        }

        override suspend fun deleteFile(file: FileEntity) {
            files.removeAll { it.id == file.id }
            lines.removeAll { it.fileId == file.id }
        }

        override suspend fun updateFileTimestamp(fileId: Long, timestamp: Long) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx >= 0) files[idx] = files[idx].copy(lastModified = timestamp)
        }

        override suspend fun internalDeleteLinesForFile(fileId: Long) {
            lines.removeAll { it.fileId == fileId }
        }

        override suspend fun getAllFilesSync(): List<FileEntity> = files.filter { !it.isTemporary }.toList()
    }

    private fun createMockZip(fileName: String, content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zipOut ->
            val entry = ZipEntry("$fileName.nerdcalci")
            zipOut.putNextEntry(entry)
            zipOut.write(content.toByteArray())
            zipOut.closeEntry()
        }
        return bos.toByteArray()
    }

    @Test
    fun `test import restores empty lines, dedicated comments and inline comments and discards appended results`() = runBlocking {
        val dao = FakeCalculatorDao()
        val content = "10+10 # 20\n\n# this is a dedicated comment\na = 10 # my comment\nb = 20 + 5 # another comment\nsum # this is total # 35"
        val zipBytes = createMockZip("test_file", content)

        BackupManager.importFromZip(mockContext, dao, { ByteArrayInputStream(zipBytes) }, { _, _, _ -> }, { _, _, _ -> ConflictResolution.KEEP_LOCAL_FILE })

        val importedLines = dao.getLinesForFileSync(1)
        assertEquals(6, importedLines.size)
        assertEquals("10+10", importedLines[0].expression)
        assertEquals("", importedLines[1].expression)
        assertEquals("# this is a dedicated comment", importedLines[2].expression)
        assertEquals("a = 10 # my comment", importedLines[3].expression)
        assertEquals("b = 20 + 5 # another comment", importedLines[4].expression)
        assertEquals("sum # this is total", importedLines[5].expression)
    }

    @Test
    fun `test import conflict with keep local choice`() = runBlocking {
        val dao = FakeCalculatorDao()
        val fileId = dao.insertFile(FileEntity(name = "test_file", createdAt = 1000L, lastModified = 1000L))
        dao.internalInsertLines(listOf(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = "")))

        val zipBytes = createMockZip("test_file", "zip expression")

        val result = BackupManager.importFromZip(
            mockContext,
            dao,
            { ByteArrayInputStream(zipBytes) },
            onProgress = { _, _, _ -> },
            onConflict = { _, _, _ -> ConflictResolution.KEEP_LOCAL_FILE }
        )

        assertEquals(1, result.processedCount)
        assertEquals(0, result.addedCount)
        assertEquals(0, result.overwrittenCount)
        assertEquals(1, result.skippedCount)

        val localLines = dao.getLinesForFileSync(fileId)
        assertEquals(1, localLines.size)
        assertEquals("local expression", localLines[0].expression)
    }

    @Test
    fun `test import conflict with replace choice`() = runBlocking {
        val dao = FakeCalculatorDao()
        val fileId = dao.insertFile(FileEntity(name = "test_file", createdAt = 1000L, lastModified = 1000L))
        dao.internalInsertLines(listOf(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = "")))

        val zipBytes = createMockZip("test_file", "zip expression")

        val result = BackupManager.importFromZip(
            mockContext,
            dao,
            { ByteArrayInputStream(zipBytes) },
            onProgress = { _, _, _ -> },
            onConflict = { _, _, _ -> ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP }
        )

        assertEquals(1, result.processedCount)
        assertEquals(0, result.addedCount)
        assertEquals(1, result.overwrittenCount)
        assertEquals(0, result.skippedCount)

        assertEquals(1, dao.files.size)
        val newFileId = dao.files[0].id

        val localLines = dao.getLinesForFileSync(newFileId)
        assertEquals(1, localLines.size)
        assertEquals("zip expression", localLines[0].expression)
    }

    @Test
    fun `test import conflict with keep both choice`() = runBlocking {
        val dao = FakeCalculatorDao()
        val fileId = dao.insertFile(FileEntity(name = "test_file", createdAt = 1000L, lastModified = 1000L))
        dao.internalInsertLines(listOf(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = "")))

        val zipBytes = createMockZip("test_file", "zip expression")

        val result = BackupManager.importFromZip(
            mockContext,
            dao,
            { ByteArrayInputStream(zipBytes) },
            onProgress = { _, _, _ -> },
            onConflict = { _, _, _ -> ConflictResolution.KEEP_BOTH_FILES }
        )

        assertEquals(1, result.processedCount)
        assertEquals(1, result.addedCount)
        assertEquals(0, result.overwrittenCount)
        assertEquals(0, result.skippedCount)

        assertEquals(2, dao.files.size)
        val oldFile = dao.files.find { it.id == fileId }!!
        val newFile = dao.files.find { it.id != fileId }!!

        assertEquals("test_file", oldFile.name)
        assertEquals("test_file (1)", newFile.name)

        val localLines = dao.getLinesForFileSync(fileId)
        assertEquals(1, localLines.size)
        assertEquals("local expression", localLines[0].expression)

        val newLines = dao.getLinesForFileSync(newFile.id)
        assertEquals(1, newLines.size)
        assertEquals("zip expression", newLines[0].expression)
    }

    @Test
    fun `test import with embedded metadata priority`() = runBlocking {
        val dao = FakeCalculatorDao()
        val metadataJson = """{"isPinned":true,"lastModified":999111,"createdAt":888222}"""
        val content = "# @metadata $metadataJson\n10 + 20"
        val zipBytes = createMockZip("meta_file", content)

        BackupManager.importFromZip(
            mockContext,
            dao,
            { ByteArrayInputStream(zipBytes) },
            onProgress = { _, _, _ -> },
            onConflict = { _, _, _ -> ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP }
        )

        val importedFile = dao.files.find { it.name == "meta_file" }!!
        assertTrue(importedFile.isPinned)
        // lastModified should be preserved from metadata when sync is off
        assertEquals(999111L, importedFile.lastModified)
        assertEquals(888222L, importedFile.createdAt)
        
        val lines = dao.getLinesForFileSync(importedFile.id)
        assertEquals(1, lines.size)
        assertEquals("10 + 20", lines[0].expression)
    }

    @Test
    fun `test import with sync enabled forces now timestamp`() = runBlocking {
        every { mockPrefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, any()) } returns true
        
        val dao = FakeCalculatorDao()
        val metadataJson = """{"lastModified":999111}"""
        val content = "# @metadata $metadataJson\n10 + 20"
        val zipBytes = createMockZip("sync_file", content)

        BackupManager.importFromZip(
            mockContext,
            dao,
            { ByteArrayInputStream(zipBytes) },
            onProgress = { _, _, _ -> },
            onConflict = { _, _, _ -> ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP }
        )

        val importedFile = dao.files.find { it.name == "sync_file" }!!
        // lastModified should be "now" when sync is on
        assertTrue(Math.abs(System.currentTimeMillis() - importedFile.lastModified) < 2000L)
    }
}
