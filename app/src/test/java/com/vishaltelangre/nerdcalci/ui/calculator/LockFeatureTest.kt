package com.vishaltelangre.nerdcalci.ui.calculator

import android.content.Context
import android.util.Log
import com.vishaltelangre.nerdcalci.data.local.FakeCalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.data.sync.SyncManager
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.utils.FileUtils
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LockFeatureTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CalculatorViewModel
    private lateinit var fakeDao: FakeCalculatorDao
    private val mockContext = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        mockkObject(SyncManager)
        every { SyncManager.isSyncActive(any<Context>()) } returns false

        mockkObject(MathEngine)
        // Basic mocks for MathEngine to avoid crashes if called
        coEvery { MathEngine.calculateFrom(any(), any(), any(), any()) } answers { it.invocation.args[0] as List<LineEntity> }

        mockkObject(FileUtils)
        every { FileUtils.calculateHash(any<String>()) } returns "mock-hash"

        fakeDao = FakeCalculatorDao()
        viewModel = CalculatorViewModel(fakeDao, ioDispatcher = mainDispatcherRule.testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `locked file cannot be renamed`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))

        val result = viewModel.renameFile(mockContext, fileId, "NewName")

        assertFalse("Rename should fail for locked file", result)
        assertEquals("LockedFile", fakeDao.getFileById(fileId)?.name)
    }

    @Test
    fun `locked file cannot be deleted`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))

        val result = viewModel.deleteFile(mockContext, fileId)

        assertFalse("Delete should fail for locked file", result)
        assertNotNull("File should still exist", fakeDao.getFileById(fileId))
    }

    @Test
    fun `locked file lines cannot be updated`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        val line = LineEntity(id = 10L, fileId = fileId, expression = "1+1", sortOrder = 0)
        fakeDao.insertLine(line)

        viewModel.updateLine(line.copy(expression = "2+2"))

        assertEquals("1+1", fakeDao.getLineById(10L)?.expression)
    }

    @Test
    fun `locked file cannot have lines added`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        fakeDao.insertLine(LineEntity(id = 10L, fileId = fileId, expression = "1+1", sortOrder = 0))

        val newId = viewModel.addLine(fileId, 1, "2+2")

        assertEquals(-1L, newId)
        assertEquals(1, fakeDao.getLinesForFileSync(fileId).size)
    }

    @Test
    fun `locked file cannot have lines deleted`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        val line = LineEntity(id = 10L, fileId = fileId, expression = "1+1", sortOrder = 0)
        fakeDao.insertLine(line)

        viewModel.deleteLine(line)

        assertNotNull("Line should still exist", fakeDao.getLineById(10L))
    }

    @Test
    fun `locked file lines cannot be split`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        val line = LineEntity(id = 10L, fileId = fileId, expression = "123456", sortOrder = 0)
        fakeDao.insertLine(line)

        val newId = viewModel.splitLine(10L, 3)

        assertEquals(-1L, newId)
        val lines = fakeDao.getLinesForFileSync(fileId)
        assertEquals(1, lines.size)
        assertEquals("123456", lines[0].expression)
    }

    @Test
    fun `locked file lines cannot be merged`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        fakeDao.insertLine(LineEntity(id = 10L, fileId = fileId, expression = "line1", sortOrder = 0))
        fakeDao.insertLine(LineEntity(id = 11L, fileId = fileId, expression = "line2", sortOrder = 1))

        viewModel.mergeLines(10L, 11L)

        val lines = fakeDao.getLinesForFileSync(fileId)
        assertEquals(2, lines.size)
        assertEquals("line1", lines[0].expression)
        assertEquals("line2", lines[1].expression)
    }

    @Test
    fun `locked file cannot be cleared`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        fakeDao.insertLine(LineEntity(id = 10L, fileId = fileId, expression = "1+1", sortOrder = 0))

        viewModel.clearAllLines(fileId)

        assertEquals(1, fakeDao.getLinesForFileSync(fileId).size)
    }

    @Test
    fun `temporary scratchpad cannot be locked`() = runTest {
        val fileId = 10L
        fakeDao.insertFile(FileEntity(id = fileId, name = "Scratchpad", isTemporary = true))

        viewModel.toggleLockFile(fileId)

        assertFalse("Scratchpad should not be locked", fakeDao.getFileById(fileId)?.isLocked ?: true)
    }

    @Test
    fun `duplicating a locked file creates an unlocked copy`() = runTest {
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "LockedFile", isLocked = true))
        fakeDao.insertLine(LineEntity(id = 10L, fileId = fileId, expression = "1+1", sortOrder = 0))

        val capturedNewId = CompletableDeferred<Long?>()
        viewModel.duplicateFile(fileId) { newId ->
            capturedNewId.complete(newId)
        }

        val newId = capturedNewId.await()
        assertNotNull(newId)
        val copyFile = fakeDao.getFileById(newId!!)
        assertNotNull(copyFile)
        assertFalse("Copy should be unlocked", copyFile!!.isLocked)
        assertEquals("Copy of LockedFile", copyFile.name)
    }

    @Test
    fun `locked empty and recent file is not automatically deleted`() = runTest {
        val fileId = 1L
        // Untitled, empty, recent, and LOCKED
        fakeDao.insertFile(FileEntity(
            id = fileId,
            name = "Untitled",
            isLocked = true,
            lastModified = System.currentTimeMillis()
        ))

        viewModel.deleteFileIfEmptyAndRecent(fileId)

        assertNotNull("Locked file should not be auto-deleted", fakeDao.getFileById(fileId))
    }
}