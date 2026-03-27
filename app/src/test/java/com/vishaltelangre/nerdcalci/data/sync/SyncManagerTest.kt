package com.vishaltelangre.nerdcalci.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.utils.FileUtils
import com.vishaltelangre.nerdcalci.utils.FileMetadata
import com.vishaltelangre.nerdcalci.utils.ParsedFileContent
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.Collections

class SyncManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val prefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val dao = FakeCalculatorDao()
    private val folder = mockk<DocumentFile>(relaxed = true)
    private val contentResolver = mockk<android.content.ContentResolver>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        mockkStatic(DocumentFile::class)
        mockkStatic(Log::class)
        mockkObject(MathEngine)
        mockkObject(FileUtils)
        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } answers { callOriginal() }
        every { FileUtils.formatFileContent(any(), any(), any()) } answers { callOriginal() }
        every { FileUtils.parseFileContent(any()) } answers { callOriginal() }
        // Keep calculateHash mocked for speed/determinism
        every { FileUtils.calculateHash(any<String>()) } answers {
            val s = it.invocation.args[0] as String
            if (s.isEmpty()) "empty-hash" else "hash-of-$s"
        }

        every { Log.e(any<String>(), any<String>()) } answers { println("LOG.E: ${it.invocation.args[1]}"); 0 }
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } answers { println("LOG.E: ${it.invocation.args[1]}"); (it.invocation.args[2] as Throwable).printStackTrace(); 0 }
        every { Log.d(any<String>(), any<String>()) } answers { println("LOG.D: ${it.invocation.args[1]}"); 0 }
        every { Log.w(any<String>(), any<String>()) } answers { println("LOG.W: ${it.invocation.args[1]}"); 0 }

        coEvery { MathEngine.calculate(any<List<LineEntity>>(), any()) } answers { it.invocation.args[0] as List<LineEntity> }
        coEvery { MathEngine.buildVariableState(any(), any(), any()) } returns MathContext()
        every { MathEngine.formatDisplayResult(any<String>(), any<Int>()) } answers { it.invocation.args[0] as String }

        every { context.contentResolver } returns contentResolver

        every { contentResolver.openInputStream(any<Uri>()) } answers {
            ByteArrayInputStream("mock content".toByteArray())
        }
        every { contentResolver.openOutputStream(any<Uri>()) } returns mockk<OutputStream>(relaxed = true)
        every { contentResolver.openOutputStream(any<Uri>(), any()) } returns mockk<OutputStream>(relaxed = true)

        every { context.getSharedPreferences(any<String>(), any<Int>()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor

        every { Uri.parse(any()) } returns mockk<Uri>(relaxed = true)
        every { DocumentFile.fromTreeUri(any(), any()) } returns folder
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `performSync recovers legacy string set last sync preference`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns mapOf(
            SyncManager.PREF_LAST_SYNC_FILES to Collections.singleton("legacy-json")
        )

        every { folder.listFiles() } returns emptyArray()

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        verify { prefs.getAll() }
    }

    @Test
    fun `performSync does not write when sync is disabled`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns false

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        verify(exactly = 0) { FileUtils.formatFileContent(any(), any(), any()) }
        verify(exactly = 0) { contentResolver.openOutputStream(any<Uri>()) }
    }

    @Test
    fun `performSync writes metadata for new local files`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()
        every { folder.listFiles() } returns emptyArray()

        dao.files.add(
            FileEntity(
                id = 1L,
                name = "Untitled",
                syncId = "",
                lastModified = 1234L,
                createdAt = 1234L,
                isPinned = true
            )
        )
        dao.lines.add(
            LineEntity(
                id = 1L,
                fileId = 1L,
                sortOrder = 0,
                expression = "1 + 1",
                result = "2"
            )
        )

        val metadataSlot = slot<FileMetadata>()
        every { FileUtils.formatFileContent(any(), any(), capture(metadataSlot)) } answers { callOriginal() }

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        assertTrue(metadataSlot.isCaptured)
        val metadata = metadataSlot.captured
        assertNotNull(metadata)
        assertTrue(metadata!!.id?.isNotBlank() == true)
        assertTrue(metadata.isPinned)
        assertEquals(1234L, metadata.lastModified)
        assertEquals(1234L, metadata.createdAt)
        verify(atLeast = 1) { FileUtils.formatFileContent(any(), any(), any()) }
    }

    @Test
    fun `performSync skips streaming remote hash for oversized saf files`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()

        val syncId = "large-file-id"
        val filename = "large${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"
        val safUri = Uri.parse("content://mock/large")

        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns filename
        every { safFile.uri } returns safUri
        every { safFile.lastModified() } returns 5000L
        every { safFile.length() } returns 10L * 1024L * 1024L
        every { folder.listFiles() } returns arrayOf(safFile)

        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(
            id = syncId,
            lastModified = 5000L,
            contentHash = null
        )

        dao.insertFile(FileEntity(name = "large", syncId = syncId, lastModified = 7000L))

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        verify(exactly = 0) { FileUtils.calculateHash(any<java.io.InputStream>()) }
    }

    private class FakeCalculatorDao : CalculatorDao() {
        val files = mutableListOf<FileEntity>()
        val lines = mutableListOf<LineEntity>()

        override fun getAllFiles() = flowOf(files)
        override suspend fun getFileById(fileId: Long) = files.find { it.id == fileId }
        override suspend fun getFileByName(name: String) = files.find { it.name == name }
        override suspend fun getFileBySyncId(syncId: String) = files.find { it.syncId == syncId }
        override suspend fun getPinnedFilesCount(): Int = files.count { it.isPinned }
        override suspend fun doesFileExist(name: String): Boolean = files.any { it.name == name }
        override suspend fun doesFileExist(name: String, excludeId: Long): Boolean = files.any { it.name == name && it.id != excludeId }
        override suspend fun getLineCountForFile(fileId: Long): Int = lines.count { it.fileId == fileId }

        override suspend fun insertFile(file: FileEntity): Long {
            val id = (files.size + 1).toLong()
            val newFile = file.copy(id = id)
            files.add(newFile)
            return id
        }

        override suspend fun updateFileFromSync(file: FileEntity) {
            val idx = files.indexOfFirst { it.syncId == file.syncId }
            if (idx != -1) files[idx] = file
        }

        override fun getLinesForFile(fileId: Long) = flowOf(lines.filter { it.fileId == fileId })
        override suspend fun getLinesForFileSync(fileId: Long) = lines.filter { it.fileId == fileId }

        override suspend fun internalInsertLines(lines: List<LineEntity>) {
            this.lines.addAll(lines)
        }

        override suspend fun internalInsertLine(line: LineEntity): Long {
            val id = (lines.size + 1).toLong()
            lines.add(line.copy(id = id))
            return id
        }

        override suspend fun internalUpdateLine(line: LineEntity) {
            val idx = lines.indexOfFirst { it.id == line.id }
            if (idx != -1) lines[idx] = line
        }

        override suspend fun internalUpdateLines(lines: List<LineEntity>) {
            lines.forEach { line ->
                val idx = this.lines.indexOfFirst { it.id == line.id }
                if (idx != -1) this.lines[idx] = line
            }
        }

        override suspend fun internalDeleteLine(line: LineEntity) {
            lines.removeIf { it.id == line.id }
        }

        override suspend fun updateLines(fileId: Long, lines: List<LineEntity>, updateTimestamp: Boolean) {
            this.lines.removeIf { it.fileId == fileId }
            this.lines.addAll(lines.map { it.copy(fileId = fileId) })
            if (updateTimestamp) {
                touchFile(fileId)
            }
        }

        override suspend fun restoreLines(fileId: Long, lines: List<LineEntity>, updateTimestamp: Boolean) {
            this.lines.removeIf { it.fileId == fileId }
            val toInsert = lines.mapIndexed { index, line ->
                line.copy(id = 0, fileId = fileId, sortOrder = index)
            }
            if (toInsert.isNotEmpty()) {
                this.lines.addAll(toInsert)
            }
            if (updateTimestamp) {
                touchFile(fileId)
            }
        }

        override suspend fun internalUpdateFile(file: FileEntity) {
            val idx = files.indexOfFirst { it.id == file.id }
            if (idx != -1) files[idx] = file
        }

        override suspend fun internalUpdateFiles(files: List<FileEntity>) {
            files.forEach { internalUpdateFile(it) }
        }

        override suspend fun deleteFile(file: FileEntity) {
            files.removeIf { it.id == file.id }
            internalDeleteLinesForFile(file.id)
        }

        override suspend fun updateFileTimestamp(fileId: Long, timestamp: Long) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx != -1) files[idx] = files[idx].copy(lastModified = timestamp)
        }

        override suspend fun internalRenameFile(fileId: Long, name: String) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx != -1) files[idx] = files[idx].copy(name = name)
        }

        override suspend fun internalDeleteLinesForFile(fileId: Long) {
            lines.removeIf { it.fileId == fileId }
        }

        override suspend fun updateSyncId(fileId: Long, newSyncId: String) {
            val idx = files.indexOfFirst { it.id == fileId }
            if (idx != -1) files[idx] = files[idx].copy(syncId = newSyncId)
        }

        override suspend fun duplicateFile(fileId: Long, newName: String, newSyncId: String, lastModified: Long?): Long {
            val original = files.find { it.id == fileId } ?: return -1L
            val nextId = (files.maxOfOrNull { it.id } ?: 0L) + 1L
            // For tests, use the provided timestamp, or the original file's if null, or default.
            val finalTimestamp = lastModified ?: original.lastModified
            val copy = original.copy(
                id = nextId,
                name = newName,
                syncId = newSyncId,
                lastModified = finalTimestamp
            )
            files.add(copy)

            val originalLines = lines.filter { it.fileId == fileId }
            var lineId = (lines.maxOfOrNull { l -> l.id } ?: 0L) + 1L
            val newLines = originalLines.map { it.copy(id = lineId++, fileId = nextId) }
            lines.addAll(newLines)
            return nextId
        }

        override suspend fun touchFile(fileId: Long, timestamp: Long) {
            updateFileTimestamp(fileId, timestamp)
        }

        override suspend fun getAllFilesSync(): List<FileEntity> = files.toList()
    }

    @Test
    fun `test embedded hash from metadata is used to skip write`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "hash-id"
        val filename = "hash${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"
        val hash = "abcd-1234"

        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns filename
        every { safFile.lastModified() } returns 9999L
        every { folder.listFiles() } returns arrayOf(safFile)

        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(
            id = syncId,
            lastModified = 5000L,
            contentHash = hash
        )

        val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
            put("filename", filename)
            put("osTimestamp", 9999L)
            put("metadataTimestamp", 5000L)
            put("isPinned", false)
            put("contentHash", hash)
        }).toString()
        every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

        dao.insertFile(FileEntity(name = "hash", syncId = syncId, lastModified = 5000L))

        val result = SyncManager.performSync(context, dao)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `test legacy file import generates syncId`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val filename = "legacy${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"
        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns filename
        every { folder.listFiles() } returns arrayOf(safFile)

        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(id = null, lastModified = 1000L)

        every { FileUtils.parseFileContent(any()) } returns ParsedFileContent(
            expressions = listOf("1+1"),
            metadata = FileMetadata(id = null, lastModified = 1000L)
        )

        val result = SyncManager.performSync(context, dao)
        assertEquals("Synced: Inbound 1, Outbound 0", result.getOrNull())

        assertNotNull(dao.files[0].syncId)
        assertTrue(dao.files[0].syncId.isNotEmpty())
    }

    @Test
    fun `test sync is triggered when pin status changes even if content matches`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "pin-change-id"
        val filename = "pin${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"
        val hash = "same-hash"

        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns filename
        every { safFile.lastModified() } returns 9999L
        every { folder.listFiles() } returns arrayOf(safFile)

        // Remote has isPinned = true
        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(
            id = syncId,
            lastModified = 5000L,
            isPinned = true,
            contentHash = hash
        )

        // Snapshot matches remote
        val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
            put("filename", filename)
            put("osTimestamp", 9999L)
            put("metadataTimestamp", 5000L)
            put("isPinned", true)
            put("contentHash", hash)
        }).toString()
        every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

        // Local has isPinned = false (changed locally)
        dao.insertFile(FileEntity(name = "pin", syncId = syncId, lastModified = 6000L, isPinned = false))

        val result = SyncManager.performSync(context, dao)
        val stats = result.getOrNull()

        assertEquals("Synced: Inbound 1, Outbound 0, Conflicts 1", stats)

        // Verify write was attempted
        verify(atLeast = 1) { contentResolver.openOutputStream(any<Uri>()) }
    }

    @Test
    fun `test local rename propagates to remote`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "rename-id"
        val oldFilename = "old-name${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"
        val newFilename = "new-name${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"

        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns oldFilename
        every { safFile.lastModified() } returns 5000L
        every { folder.listFiles() } returns arrayOf(safFile)

        // Snapshot matches old remote
        val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
            put("filename", oldFilename)
            put("osTimestamp", 5000L)
            put("metadataTimestamp", 5000L)
            put("isPinned", false)
            put("contentHash", "old-hash")
        }).toString()
        every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

        // Local has new name and newer timestamp (well beyond tolerance)
        dao.insertFile(FileEntity(name = "new-name", syncId = syncId, lastModified = 9000L))

        // Mock remote file content/metadata
        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(
            id = syncId,
            lastModified = 5000L,
            isPinned = false,
            contentHash = "old-hash"
        )
        // Mock different hashes to force sync if rename wasn't enough
        every { FileUtils.formatFileContent(any(), any(), any()) } returns "new content"
        every { FileUtils.calculateHash("new content") } returns "hash-of-new content"
        every { FileUtils.calculateHash("mock content") } returns "old-hash"

        val result = SyncManager.performSync(context, dao)
        val stats = result.getOrNull()
        if (stats != "Synced: Inbound 1, Outbound 0, Conflicts 1") {
            println("RENAME TEST FAILED: Result was '$stats'")
            if (result.isFailure) result.exceptionOrNull()?.printStackTrace()
        }
        assertEquals("Synced: Inbound 1, Outbound 0, Conflicts 1", stats)

        // Verify remote rename was attempted
        verify { safFile.renameTo(newFilename) }
    }

    @Test
    fun `test delete vs edit local wins if newer`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "del-edit-id"
        val filename = "file${com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION}"

        // Snapshot: both were at 10000L
        val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
            put("filename", filename)
            put("osTimestamp", 10000L)
            put("metadataTimestamp", 10000L)
            put("isPinned", false)
            put("contentHash", "old-hash")
        }).toString()
        every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

        // No files in folder
        every { folder.listFiles() } returns emptyArray()

        // Local has a newer edit (20000L > 10000L)
        dao.insertFile(FileEntity(name = "file", syncId = syncId, lastModified = 20000L))

        val result = SyncManager.performSync(context, dao)
        val stats = result.getOrThrow()
        assertEquals("Synced: Inbound 0, Outbound 1", stats)

        // Verify it was re-uploaded (createFile called)
        verify { folder.createFile(any(), filename) }
    }

    @Test
    fun `test rename vs edit merges both`() = runBlocking {
        try {
            every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
            every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

            val syncId = "ren-edit-id"
            val ext = com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
            val oldFilename = "old$ext"
            val newFilename = "new$ext"

            // Snapshot: 10000L
            val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
                put("filename", oldFilename)
                put("osTimestamp", 10000L)
                put("metadataTimestamp", 10000L)
                put("isPinned", false)
                put("contentHash", "old-hash")
            }).toString()
            every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

            // Remote: Modifies content (metadata mtime = 20000L)
            var currentRemoteName = oldFilename
            val safFile = mockk<DocumentFile>(relaxed = true)
            val safUri = Uri.parse("content://mock/old$ext")
            every { safFile.isFile } returns true
            every { safFile.name } answers { currentRemoteName }
            every { safFile.uri } returns safUri
            every { safFile.lastModified() } returns 20000L
            every { safFile.renameTo(any()) } answers {
                currentRemoteName = it.invocation.args[0] as String
                true
            }
            every { folder.listFiles() } returns arrayOf(safFile)

            // Provide REAL content that says 20000L
            val remoteMetadata = FileMetadata(id = syncId, lastModified = 20000L, contentHash = "hash-of-remote-content")
            val content = FileUtils.formatFileContent(listOf(LineEntity(fileId = 1, sortOrder = 0, expression = "remote content")), 2, remoteMetadata)
            every { contentResolver.openInputStream(safUri) } answers { java.io.ByteArrayInputStream(content.toByteArray()) }

            // Local: Renames to 'new' and sets mtime to 10000L (so localChanged is false)
            dao.insertFile(FileEntity(name = "new", syncId = syncId, lastModified = 10000L))

            val result = SyncManager.performSync(context, dao)
            val stats = result.getOrThrow()

            println("RENAME_VS_EDIT_STATS: $stats")
            assertTrue("Expected Inbound 1 but got $stats", stats.contains("Inbound 1"))

            // Verify remote rename happened
            verify { safFile.renameTo(newFilename) }

            // Verify local content updated to 20000L
            val finalFile = dao.getFileBySyncId(syncId)
            assertEquals("new", finalFile?.name)
            assertEquals(20000L, finalFile?.lastModified)
        } catch (e: Throwable) {
            println("RENAME_VS_EDIT_FAILED: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `test two-device edit triggers conflict duplication`() = runBlocking {
        try {
            every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
            every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

            val syncId = "conflict-id"
            val ext = com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
            val filename = "file$ext"

            // Snapshot: both were at 10000L
            val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
                put("filename", filename)
                put("osTimestamp", 10000L)
                put("metadataTimestamp", 10000L)
                put("isPinned", false)
                put("contentHash", "old-hash")
            }).toString()
            every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

            // Remote: Edited to 30000L
            val safFile = mockk<DocumentFile>(relaxed = true)
            val safUri = Uri.parse("content://mock/file$ext")
            every { safFile.isFile } returns true
            every { safFile.name } returns filename
            every { safFile.uri } returns safUri
            every { safFile.lastModified() } returns 30000L
            every { safFile.renameTo(any()) } returns true
            every { folder.listFiles() } returns arrayOf(safFile)

            // Provide REAL content for conflict
            val remoteMetadata = FileMetadata(id = syncId, lastModified = 30000L, contentHash = "hash-of-remote-edit")
            val content = FileUtils.formatFileContent(listOf(LineEntity(fileId = 1, sortOrder = 0, expression = "remote-edit")), 2, remoteMetadata)
            every { contentResolver.openInputStream(safUri) } answers { java.io.ByteArrayInputStream(content.toByteArray()) }

            // Local: Edited to 25000L (Conflict!)
            val localId = dao.insertFile(FileEntity(name = "file", syncId = syncId, lastModified = 25000L))
            dao.insertLine(LineEntity(fileId = localId, sortOrder = 0, expression = "local-edit"))
            dao.touchFile(localId, 25000L) // Fix timestamp after insertLine touches it

            val result = SyncManager.performSync(context, dao)
            val stats = result.getOrThrow()
            println("CONFLICT_STATS: $stats")
            assertTrue("Expected Conflict but got $stats", stats.contains("Conflicts 1"))

            // Verify we have TWO files now
            val finalFiles = dao.getAllFilesSync()
            assertEquals(2, finalFiles.size)
            val original = finalFiles.find { it.syncId == syncId }
            val conflict = finalFiles.find { it.name.contains("(conflict copy)") }

            assertNotNull("Original file should exist", original)
            assertNotNull("Conflict copy should exist", conflict)
            assertEquals(30000L, original?.lastModified) // Original updated to remote
            assertEquals(25000L, conflict?.lastModified) // Conflict copy has local state
        } catch (e: Throwable) {
            println("CONFLICT_TEST_FAILED: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `test partial write does not delete original file`() = runBlocking {
        try {
            every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
            every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

            val syncId = "crash-id"
            val ext = com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
            val filename = "file$ext"

            // Local is newer
            dao.insertFile(FileEntity(name = "file", syncId = syncId, lastModified = 30000L))

            // Remote exists
            val safFile = mockk<DocumentFile>(relaxed = true)
            every { safFile.isFile } returns true
            every { safFile.name } returns filename
            every { folder.listFiles() } returns arrayOf(safFile)
            every { folder.findFile(filename) } returns safFile

            // Mock a failure during stream writing
            val errorStream = object : java.io.OutputStream() {
                override fun write(b: Int) { throw java.io.IOException("Disk Full") }
            }
            every { contentResolver.openOutputStream(any()) } returns errorStream
            every { contentResolver.openOutputStream(any(), any()) } returns errorStream

            // Mock temp file creation
            val tempFile = mockk<DocumentFile>(relaxed = true)
            every { folder.createFile(any(), any()) } returns tempFile

            val result = SyncManager.performSync(context, dao)
            assertTrue("Sync should have failed", result.isFailure)

            // Verify original file was NEVER called for delete
            verify(exactly = 0) { safFile.delete() }
        } catch (e: Throwable) {
            println("PARTIAL_WRITE_FAILED: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `test zero timestamp falls back to metadata`() = runBlocking {
        try {
            every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
            every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

            val syncId = "zero-id"
            val ext = com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
            val filename = "file$ext"

            // Snapshot was at 10000L
            val snapshotJson = JSONObject().put(syncId, JSONObject().apply {
                put("filename", filename)
                put("osTimestamp", 10000L)
                put("metadataTimestamp", 10000L)
                put("isPinned", false)
                put("contentHash", "old-hash")
            }).toString()
            every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

            // Remote: OS returns 0, but Metadata has 20000L
            val safFile = mockk<DocumentFile>(relaxed = true)
            val safUri = Uri.parse("content://mock/zero$ext")
            every { safFile.isFile } returns true
            every { safFile.name } returns filename
            every { safFile.uri } returns safUri
            every { safFile.lastModified() } returns 0L
            every { safFile.renameTo(any()) } returns true
            every { folder.listFiles() } returns arrayOf(safFile)

            // Provide REAL content that says 20000L
            val remoteMetadata = FileMetadata(id = syncId, lastModified = 20000L, contentHash = "hash-of-zero-content")
            val content = FileUtils.formatFileContent(listOf(LineEntity(fileId = 1, sortOrder = 0, expression = "zero-content")), 2, remoteMetadata)
            println("DEBUG_ZERO_CONTENT: $content")
            every { contentResolver.openInputStream(safUri) } answers { java.io.ByteArrayInputStream(content.toByteArray()) }

            // Local is at snapshot level (10000L)
            dao.insertFile(FileEntity(name = "file", syncId = syncId, lastModified = 10000L))

            val result = SyncManager.performSync(context, dao)
            val stats = result.getOrThrow()

            assertTrue("Expected Inbound 1 but got $stats", stats.contains("Inbound 1"))
            val finalFile = dao.getFileBySyncId(syncId)
            assertEquals(20000L, finalFile?.lastModified)
        } finally {
            // cleanup if needed
        }
    }
}
