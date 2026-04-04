package com.vishaltelangre.nerdcalci.ui.calculator

import android.util.Log
import com.vishaltelangre.nerdcalci.data.local.FakeCalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class CalculatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CalculatorViewModel
    private lateinit var fakeDao: FakeCalculatorDao

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        fakeDao = FakeCalculatorDao()
        viewModel = CalculatorViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial setup sets up lines and file`() = runTest {
        fakeDao.insertFile(FileEntity(id = 1L, name = "TestFile"))
        fakeDao.insertLine(LineEntity(id = 1L, fileId = 1L, expression = "1+1", sortOrder = 0))

        val lines = fakeDao.getLinesForFileSync(1L)
        assertEquals(1, lines.size)
        assertEquals("1+1", lines[0].expression)
    }

    @Test
    fun `splitLine split in middle of expression`() = runTest {
        fakeDao.insertFile(FileEntity(id = 1L, name = "TestFile"))
        val line = LineEntity(id = 1L, fileId = 1L, expression = "a = 10 + 20", result = "30.0", sortOrder = 0)
        fakeDao.insertLine(line)

        viewModel.splitLine(1L, 7)

        val lines = fakeDao.getLinesForFileSync(1L)
        assertEquals(2, lines.size)
        assertEquals("a = 10 ", lines[0].expression)
        assertEquals("+ 20", lines[1].expression)
    }

    @Test
    fun `duplicate scratchpad results in Copy of Scratchpad and is not temporary`() = runTest {
        // Given a temporary scratchpad
        val tempFile = FileEntity(id = 10L, name = "Temp", isTemporary = true)
        fakeDao.insertFile(tempFile)
        fakeDao.insertLine(LineEntity(fileId = 10L, expression = "1+1", result = "2", sortOrder = 0))

        // When duplicating it
        val capturedNewId = CompletableDeferred<Long?>()
        viewModel.duplicateFile(10L) { newId ->
            capturedNewId.complete(newId)
        }
        
        // Wait for coroutines
        val newId = capturedNewId.await()
        assertNotNull("New ID should not be null", newId)

        // Then its name should be "Copy of Scratchpad" and it should NOT be temporary
        val copyFile = fakeDao.getFileById(newId!!)!!
        assertEquals("Copy of Scratchpad", copyFile.name)
        assertFalse("Copy should not be temporary", copyFile.isTemporary)
    }

    @Test
    fun `ensureScratchpadExists clears lines if scratchpad already exists`() = runTest {
        // Given an existing scratchpad with content
        val existingTempFile = FileEntity(id = 20L, name = "Scratchpad", isTemporary = true)
        fakeDao.insertFile(existingTempFile)
        fakeDao.insertLine(LineEntity(id = 100L, fileId = 20L, expression = "1+1", sortOrder = 0))
        
        // When a new ViewModel is initialized (it calls ensureScratchpadExists in init)
        val newViewModel = CalculatorViewModel(fakeDao)
        
        // Wait for coroutines in init to finish
        testScheduler.advanceUntilIdle()
        
        // Find the temporary file
        val currentTempFile = fakeDao.files.find { it.isTemporary }
        assertNotNull("Should have a temporary file", currentTempFile)
        
        val lines = fakeDao.getLinesForFileSync(currentTempFile!!.id)
        // Should only have one empty line now (reset/cleared)
        assertEquals("Should have exactly 1 line", 1, lines.size)
        assertEquals("Line expression should be empty", "", lines[0].expression)
    }

    @Test
    fun `create temporary scratchpad seeds one empty line`() = runTest {
        val scratchpadId = fakeDao.createTemporaryFileWithInitialLine()

        val scratchpad = fakeDao.getFileById(scratchpadId)!!
        assertTrue(scratchpad.isTemporary)

        val lines = fakeDao.getLinesForFileSync(scratchpadId)
        assertEquals(1, lines.size)
        assertEquals("", lines[0].expression)
        assertEquals(0, lines[0].sortOrder)
    }

    @Test
    fun `allFiles excludes temporary files for sync and backup safety`() = runTest {
        // Given: One normal file and one temporary file
        fakeDao.insertFile(FileEntity(id = 1L, name = "Normal", isTemporary = false))
        fakeDao.insertFile(FileEntity(id = 2L, name = "Scratchpad", isTemporary = true))

        // When collecting allFiles
        val allFiles = viewModel.allFiles.first()

        // Then only the normal file should be present
        assertEquals(1, allFiles.size)
        assertEquals("Normal", allFiles[0].name)
        assertTrue(allFiles.none { it.isTemporary })
    }
}
