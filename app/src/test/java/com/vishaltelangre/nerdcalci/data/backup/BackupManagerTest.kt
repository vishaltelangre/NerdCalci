package com.vishaltelangre.nerdcalci.data.backup

import android.content.Context
import android.content.SharedPreferences
import com.vishaltelangre.nerdcalci.data.local.FakeCalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.data.sync.SyncManager
import io.mockk.every
import io.mockk.mockk
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

        val importedFile = dao.files.single { it.name == "test_file" }
        val importedLines = dao.getLinesForFileSync(importedFile.id)
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
        dao.addLine(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = ""))

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
        dao.addLine(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = ""))

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
        dao.addLine(LineEntity(fileId = fileId, expression = "local expression", sortOrder = 0, result = ""))

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
