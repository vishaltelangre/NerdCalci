package com.vishaltelangre.nerdcalci.data.backup

import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManagerTest {

    // Fake DAO Implementation
    class FakeCalculatorDao : CalculatorDao() {
        val files = mutableListOf<FileEntity>()
        val lines = mutableListOf<LineEntity>()
        var nextFileId = 1L
        var nextLineId = 1L

        override fun getAllFiles(): Flow<List<FileEntity>> = flowOf(files)

        override suspend fun getFileById(fileId: Long): FileEntity? = files.find { it.id == fileId }

        override suspend fun getPinnedFilesCount(): Int = files.count { it.isPinned }

        override suspend fun getFileByName(name: String): FileEntity? = files.find { it.name == name }

        override suspend fun doesFileExist(name: String): Boolean = files.any { it.name == name }

        override suspend fun doesFileExist(name: String, excludeId: Long): Boolean =
            files.any { it.name == name && it.id != excludeId }

        override fun getLinesForFile(fileId: Long): Flow<List<LineEntity>> =
            flowOf(lines.filter { it.fileId == fileId })

        override suspend fun getLinesForFileSync(fileId: Long): List<LineEntity> =
            lines.filter { it.fileId == fileId }

        override suspend fun getLineCountForFile(fileId: Long): Int =
            lines.count { it.fileId == fileId }

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

        override suspend fun internalDeleteLine(line: LineEntity) {
            lines.removeAll { it.id == line.id }
        }

        override suspend fun internalUpdateFile(file: FileEntity) {
            val idx = files.indexOfFirst { it.id == file.id }
            if (idx >= 0) files[idx] = file
        }

        override suspend fun deleteFile(file: FileEntity) {
            files.removeAll { it.id == file.id }
            lines.removeAll { it.fileId == file.id }
        }

        override suspend fun updateFileTimestamp(fileId: Long, timestamp: Long) {
            // Simplified for test
        }


        override suspend fun internalDeleteLinesForFile(fileId: Long) {
            lines.removeAll { it.fileId == fileId }
        }
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

        BackupManager.importFromZip(dao, { ByteArrayInputStream(zipBytes) }, { _, _, _ -> }, { _, _, _ -> ConflictResolution.KEEP_LOCAL_FILE })

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
}
