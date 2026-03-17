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

        override suspend fun internalInsertLines(lines: List<LineEntity>) {
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
    fun `test import preserves empty lines`() = runBlocking {
        val dao = FakeCalculatorDao()
        val content = "a = 10\n\nb = 20"
        val zipBytes = createMockZip("test_file", content)

        val count = BackupManager.importFromZip(dao, ByteArrayInputStream(zipBytes))

        assertEquals(1, count)
        val importedLines = dao.getLinesForFileSync(1)

        // Expected: a = 10, empty line, b = 20
        assertEquals(3, importedLines.size)
        assertEquals("a = 10", importedLines[0].expression)
        assertEquals("", importedLines[1].expression)
        assertEquals("b = 20", importedLines[2].expression)
    }

    @Test
    fun `test import restores inline comments`() = runBlocking {
        val dao = FakeCalculatorDao()
        val content = "a = 10 # my comment\nb = 20 + 5 # another comment\nsum # this is total # 35"
        val zipBytes = createMockZip("test_file", content)

        BackupManager.importFromZip(dao, ByteArrayInputStream(zipBytes))

        val importedLines = dao.getLinesForFileSync(1)
        assertEquals(3, importedLines.size)
        assertEquals("a = 10 # my comment", importedLines[0].expression)
        assertEquals("b = 20 + 5 # another comment", importedLines[1].expression)
        assertEquals("sum # this is total", importedLines[2].expression)
    }

    @Test
    fun `test import preserves dedicated comment`() = runBlocking {
        val dao = FakeCalculatorDao()
        val content = "# This is a comment"
        val zipBytes = createMockZip("test_file", content)

        BackupManager.importFromZip(dao, ByteArrayInputStream(zipBytes))

        val importedLines = dao.getLinesForFileSync(1)
        assertEquals(1, importedLines.size)
        assertEquals("# This is a comment", importedLines[0].expression)
    }

    @Test
    fun `test import discards appended result on operators`() = runBlocking {
        val dao = FakeCalculatorDao()
        val content = "1 + 2 # 3" // 3 is the appended result
        val zipBytes = createMockZip("test_file", content)

        BackupManager.importFromZip(dao, ByteArrayInputStream(zipBytes))

        val importedLines = dao.getLinesForFileSync(1)
        assertEquals(1, importedLines.size)
        assertEquals("1 + 2", importedLines[0].expression)
    }
}
