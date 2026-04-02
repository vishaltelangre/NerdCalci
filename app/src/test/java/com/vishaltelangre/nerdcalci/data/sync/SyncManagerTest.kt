package com.vishaltelangre.nerdcalci.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vishaltelangre.nerdcalci.data.local.FakeCalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.utils.FileUtils
import com.vishaltelangre.nerdcalci.utils.FileMetadata
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
import org.json.JSONTokener
import org.junit.Assert.assertNotEquals
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
        every { FileUtils.parseFileContent(any()) } answers { callOriginal() }
        coEvery { FileUtils.formatCanonicalFileContent(any(), any()) } answers { callOriginal() }
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
        coVerify(exactly = 0) { FileUtils.formatCanonicalFileContent(any(), any()) }
        verify(exactly = 0) { contentResolver.openOutputStream(any<Uri>()) }
        verify(exactly = 0) { contentResolver.openOutputStream(any<Uri>(), any()) }
    }

    @Test
    fun `performSync writes metadata for new local files`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()
        every { folder.listFiles() } returns emptyArray()

        dao.addFile(
            FileEntity(
                id = 1L,
                name = "Untitled",
                syncId = "",
                lastModified = 1234L,
                createdAt = 1234L,
                isPinned = true
            )
        )
        dao.addLine(
            LineEntity(
                id = 1L,
                fileId = 1L,
                sortOrder = 0,
                expression = "1 + 1",
                result = "2"
            )
        )

        val metadataSlot = slot<FileMetadata>()
        coEvery { FileUtils.formatCanonicalFileContent(any(), capture(metadataSlot)) } answers { callOriginal() }

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        assertTrue(metadataSlot.isCaptured)
        val metadata = metadataSlot.captured
        assertNotNull(metadata)
        assertTrue(metadata!!.id?.isNotBlank() == true)
        assertTrue(metadata.isPinned)
        assertEquals(1234L, metadata.lastModified)
        assertEquals(1234L, metadata.createdAt)
        coVerify(atLeast = 1) { FileUtils.formatCanonicalFileContent(any(), any()) }
    }

    @Test
    fun `performSync uses canonical content independent of precision setting`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()
        every { folder.listFiles() } returns emptyArray()

        dao.addFile(
            FileEntity(
                id = 1L,
                name = "Precision",
                syncId = "",
                lastModified = 1234L,
                createdAt = 1234L,
                isPinned = false
            )
        )
        dao.addLine(
            LineEntity(
                id = 1L,
                fileId = 1L,
                sortOrder = 0,
                expression = "10 / 3",
                result = "3.333333333333333333"
            )
        )

        val contentSlot = slot<String>()
        every { FileUtils.calculateHash(capture(contentSlot)) } answers { "hash" }

        SyncManager.performSync(context, dao)
        val canonicalContent = contentSlot.captured

        assertTrue(canonicalContent.contains("""{"result":"3.333333333333333333"}"""))
        assertNotEquals(canonicalContent, FileUtils.formatFileBody(dao.getLinesForFileSync(1L), 2))
    }

    @Test
    fun `performSync preserves explicit rational stored result`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getBoolean(com.vishaltelangre.nerdcalci.core.Constants.SYNC_ENGINE_RATIONAL_MODE, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()
        every { folder.listFiles() } returns emptyArray()

        dao.addFile(
            FileEntity(
                id = 1L,
                name = "Rational",
                syncId = "rational-id",
                lastModified = 1234L,
                createdAt = 1234L,
                isPinned = false
            )
        )
        dao.addLine(
            LineEntity(
                id = 1L,
                fileId = 1L,
                sortOrder = 0,
                expression = "rational(1 / 3)",
                result = "1/3"
            )
        )

        val outputOffSlot = slot<String>()
        every { FileUtils.calculateHash(capture(outputOffSlot)) } answers { "hash" }

        SyncManager.performSync(context, dao)

        assertEquals(
            """rational(1 / 3) # {"result":"1/3"}""",
            outputOffSlot.captured
        )

        val outputOnSlot = slot<String>()
        every { FileUtils.calculateHash(capture(outputOnSlot)) } answers { "hash" }

        SyncManager.performSync(context, dao)

        assertEquals(outputOffSlot.captured, outputOnSlot.captured)
    }

    @Test
    fun `performSync serializes plain fractional result as decimal`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()
        every { folder.listFiles() } returns emptyArray()

        dao.addFile(
            FileEntity(
                id = 1L,
                name = "Decimal",
                syncId = "decimal-id",
                lastModified = 1234L,
                createdAt = 1234L,
                isPinned = false
            )
        )
        dao.addLine(
            LineEntity(
                id = 1L,
                fileId = 1L,
                sortOrder = 0,
                expression = "1 / 3",
                result = "0.3333333333333333333333333333333333"
            )
        )

        val contentSlot = slot<String>()
        every { FileUtils.calculateHash(capture(contentSlot)) } answers { "hash" }

        SyncManager.performSync(context, dao)

        assertEquals(
            """1 / 3 # {"result":"0.3333333333333333333333333333333333"}""",
            contentSlot.captured
        )
    }

    @Test
    fun `performSync skips streaming remote hash for oversized saf files`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"
        every { prefs.getAll() } returns emptyMap()

        val syncId = "large-file-id"
        val filename = "large${EXPORT_FILE_EXTENSION}"
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

        dao.addFile(FileEntity(name = "large", syncId = syncId, lastModified = 7000L))

        val result = SyncManager.performSync(context, dao)

        assertTrue(result.isSuccess)
        verify(exactly = 0) { FileUtils.calculateHash(any<java.io.InputStream>()) }
    }


    @Test
    fun `test embedded hash from metadata is used to skip write`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "hash-id"
        val filename = "hash${EXPORT_FILE_EXTENSION}"
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

        dao.addFile(FileEntity(name = "hash", syncId = syncId, lastModified = 5000L))

        val result = SyncManager.performSync(context, dao)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `test legacy file import generates syncId`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val filename = "legacy${EXPORT_FILE_EXTENSION}"
        val safFile = mockk<DocumentFile>(relaxed = true)
        every { safFile.isFile } returns true
        every { safFile.name } returns filename
        every { folder.listFiles() } returns arrayOf(safFile)

        every { FileUtils.readMetadataHeader(any<java.io.BufferedReader>()) } returns FileMetadata(id = null, lastModified = 1000L)

        every { FileUtils.parseFileContent(any()) } returns com.vishaltelangre.nerdcalci.utils.ParsedFileContent(
            expressions = listOf("1+1"),
            metadata = FileMetadata(id = null, lastModified = 1000L)
        )

        val result = SyncManager.performSync(context, dao)
        assertEquals("Synced: Inbound 1, Outbound 0", result.getOrNull())

        val files = dao.getAllFilesSync()
        assertNotNull(files[0].syncId)
        assertTrue(files[0].syncId.isNotEmpty())
    }

    @Test
    fun `test sync is triggered when pin status changes even if content matches`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "pin-change-id"
        val filename = "pin${EXPORT_FILE_EXTENSION}"
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
        dao.addFile(FileEntity(name = "pin", syncId = syncId, lastModified = 6000L, isPinned = false))

        val result = SyncManager.performSync(context, dao)
        val stats = result.getOrNull()

        assertEquals("Synced: Inbound 1, Outbound 0, Conflicts 1", stats)

        // Verify write was attempted (using 2-arg variant for overwrites)
        verify(atLeast = 1) { contentResolver.openOutputStream(any<Uri>(), eq("wt")) }
    }

    @Test
    fun `test local rename propagates to remote`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "rename-id"
        val oldFilename = "old-name${EXPORT_FILE_EXTENSION}"
        val newFilename = "new-name${EXPORT_FILE_EXTENSION}"

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
        dao.addFile(FileEntity(name = "new-name", syncId = syncId, lastModified = 9000L))

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
        val filename = "file${EXPORT_FILE_EXTENSION}"

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
        dao.addFile(FileEntity(name = "file", syncId = syncId, lastModified = 20000L))

        val tempFile = mockk<DocumentFile>(relaxed = true)
        var fileExistsInSaf = false
        every { folder.findFile(filename) } answers { if (fileExistsInSaf) tempFile else null }
        every { folder.findFile(filename + ".tmp") } returns null
        every { folder.createFile(any(), filename + ".tmp") } returns tempFile
        every { tempFile.renameTo(filename) } answers { fileExistsInSaf = true; true }

        val result = SyncManager.performSync(context, dao)
        val stats = result.getOrThrow()
        assertEquals("Synced: Inbound 0, Outbound 1", stats)

        // Verify it was re-uploaded via temp file
        verify { folder.createFile(any(), filename + ".tmp") }
        verify { tempFile.renameTo(filename) }
    }

    @Test
    fun `test rename vs edit merges both`() = runBlocking {
        try {
            every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
            every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

            val syncId = "ren-edit-id"
            val ext = EXPORT_FILE_EXTENSION
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
            dao.addFile(FileEntity(name = "new", syncId = syncId, lastModified = 10000L))

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
            val ext = EXPORT_FILE_EXTENSION
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
            dao.addFile(FileEntity(name = "old", syncId = syncId, lastModified = 25000L))
            dao.addLine(LineEntity(fileId = 1, expression = "old", sortOrder = 0))
            
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
            val ext = EXPORT_FILE_EXTENSION
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
            val ext = EXPORT_FILE_EXTENSION
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

    @Test
    fun `test duplicate syncId found in SAF prioritizing matching name`() = runBlocking {
        every { prefs.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) } returns true
        every { prefs.getString(SyncManager.PREF_SYNC_FOLDER_URI, null) } returns "content://mock"

        val syncId = "dup-id"
        val ext = EXPORT_FILE_EXTENSION
        val originalName = "file$ext"
        val suffixName = "file (1)$ext"

        // Snapshot says we are on "file.nerdcalci"
        val snapshotJson = org.json.JSONObject().put(syncId, org.json.JSONObject().apply {
            put("filename", originalName)
            put("osTimestamp", 10000L)
            put("metadataTimestamp", 10000L)
            put("isPinned", false)
            put("contentHash", "empty-hash")
        }).toString()
        every { prefs.getString(SyncManager.PREF_LAST_SYNC_FILES, null) } returns snapshotJson

        // SAF has BOTH "file.nerdcalci" (older) and "file (1).nerdcalci" (newer)
        val fileOriginal = mockk<DocumentFile>(relaxed = true)
        val fileSuffix = mockk<DocumentFile>(relaxed = true)

        every { fileOriginal.isFile } returns true
        every { fileOriginal.name } returns originalName
        every { fileOriginal.lastModified() } returns 10000L
        every { fileOriginal.uri } returns Uri.parse("content://mock/orig")

        every { fileSuffix.isFile } returns true
        every { fileSuffix.name } returns suffixName
        every { fileSuffix.lastModified() } returns 20000L // Newer!
        every { fileSuffix.uri } returns Uri.parse("content://mock/suffix")

        every { folder.listFiles() } returns arrayOf(fileOriginal, fileSuffix)

        // Metadata for both is the same
        val metadata = FileMetadata(id = syncId, lastModified = 10000L, contentHash = "empty-hash")
        val content = FileUtils.formatFileContent(emptyList(), 2, metadata)
        every { contentResolver.openInputStream(any()) } answers { java.io.ByteArrayInputStream(content.toByteArray()) }

        // Local State
        dao.insertFile(FileEntity(name = "file", syncId = syncId, lastModified = 10000L))

        val result = SyncManager.performSync(context, dao)
        org.junit.Assert.assertTrue("Sync failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
    }
}
