package com.vishaltelangre.nerdcalci.ui.calculator

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.vishaltelangre.nerdcalci.data.local.FakeCalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.data.sync.SyncManager
import com.vishaltelangre.nerdcalci.utils.SuggestionType
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
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class CalculatorViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

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

        fakeDao = FakeCalculatorDao()
        viewModel = CalculatorViewModel(fakeDao, ioDispatcher = mainDispatcherRule.testDispatcher)
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
        val line =
                LineEntity(
                        id = 1L,
                        fileId = 1L,
                        expression = "a = 10 + 20",
                        result = "30.0",
                        sortOrder = 0
                )
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
        fakeDao.insertLine(
                LineEntity(fileId = 10L, expression = "1+1", result = "2", sortOrder = 0)
        )

        // When duplicating it
        val capturedNewId = CompletableDeferred<Long?>()
        viewModel.duplicateFile(mockContext, 10L) { newId -> capturedNewId.complete(newId) }

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

    @Test
    fun `getSuggestionsForFile includes date keywords`() = runTest {
        fakeDao.insertFile(FileEntity(id = 1L, name = "Dates"))
        fakeDao.insertLine(LineEntity(id = 1L, fileId = 1L, expression = "today", sortOrder = 0))
        fakeDao.insertLine(LineEntity(id = 2L, fileId = 1L, expression = "x = 1", sortOrder = 1))

        val suggestions = viewModel.getSuggestionsForFile("Dates")

        assertTrue(
                suggestions.contains(
                        com.vishaltelangre.nerdcalci.utils.Suggestion(
                                "today",
                                SuggestionType.KEYWORD
                        )
                )
        )
        assertTrue(
                suggestions.contains(
                        com.vishaltelangre.nerdcalci.utils.Suggestion(
                                "between",
                                SuggestionType.KEYWORD
                        )
                )
        )
        assertTrue(
                suggestions.contains(
                        com.vishaltelangre.nerdcalci.utils.Suggestion(
                                "tomorrow",
                                SuggestionType.KEYWORD
                        )
                )
        )
    }

    @Test
    fun `lastSyncAt is initialized from SharedPreferences`() = runTest {
        // Given: SharedPreferences has a last sync timestamp
        val expectedTimestamp = 123456789L
        val mockPrefs = mockk<SharedPreferences>(relaxed = true)
        every { mockPrefs.getLong(SyncManager.PREF_LAST_SYNC_AT, 0L) } returns expectedTimestamp

        // When: ViewModel is created with these prefs
        val testViewModel = CalculatorViewModel(fakeDao, mockPrefs)

        // Then: lastSyncAt should reflect the stored timestamp
        assertEquals(expectedTimestamp, testViewModel.lastSyncAt.value)
    }

    @Test
    fun `pasteLines inserts multi-line paste atomically`() = runTest {
        fakeDao.insertFile(FileEntity(id = 1L, name = "TestFile"))
        val line =
                LineEntity(id = 10L, fileId = 1L, expression = "1 + ", result = "", sortOrder = 0)
        fakeDao.insertLine(line)

        val firstChunk = "2"
        val middleLines = listOf("3 + 3", "4 + 4")
        val lastChunk = "5"

        val lastInsertedId =
                viewModel.pasteLines(
                        lineId = 10L,
                        cursorPosInExpr = 4, // end of "1 + "
                        firstChunk = firstChunk,
                        middleLines = middleLines,
                        lastChunk = lastChunk,
                        rationalMode = false
                )

        val lines = fakeDao.getLinesForFileSync(1L)
        assertEquals(4, lines.size)
        assertEquals("1 + 2", lines[0].expression)
        assertEquals("3 + 3", lines[1].expression)
        assertEquals("4 + 4", lines[2].expression)
        assertEquals("5", lines[3].expression)
        assertEquals(lastInsertedId, lines[3].id)
    }

    @Test
    fun `deleteLines deletes multiple lines atomically, recalculates and supports undo`() = runTest {
        // Given
        val fileId = 1L
        fakeDao.insertFile(FileEntity(id = fileId, name = "TestFile"))
        val line0 = LineEntity(id = 10L, fileId = fileId, expression = "x = 10", result = "10.0", sortOrder = 0)
        val line1 = LineEntity(id = 11L, fileId = fileId, expression = "y = 20", result = "20.0", sortOrder = 1)
        val line2 = LineEntity(id = 12L, fileId = fileId, expression = "x + y", result = "30.0", sortOrder = 2)
        val line3 = LineEntity(id = 13L, fileId = fileId, expression = "z = 40", result = "40.0", sortOrder = 3)
        fakeDao.insertLine(line0)
        fakeDao.insertLine(line1)
        fakeDao.insertLine(line2)
        fakeDao.insertLine(line3)

        // When: Deleting line 1 ("y = 20") and line 3 ("z = 40")
        val linesToDelete = listOf(line1, line3)
        viewModel.deleteLines(fileId, linesToDelete, rationalMode = false)

        // Advance coroutines
        testScheduler.advanceUntilIdle()

        // Then: remaining lines should be line0 (sortOrder 0) and line2 (sortOrder 1)
        val remainingLines = fakeDao.getLinesForFileSync(fileId)
        assertEquals(2, remainingLines.size)
        
        // Let's verify sortOrder normalization
        assertEquals(0, remainingLines[0].sortOrder)
        assertEquals(1, remainingLines[1].sortOrder)
        
        // Line0 is untouched
        assertEquals("x = 10", remainingLines[0].expression)
        
        // Line2 expression was "x + y", but y was deleted.
        // It should be recalculated. Since y is undefined now, evaluating "x + y" should result in an error or unresolved state.
        // Let's assert that the recalculation was triggered.
        assertNotEquals("30.0", remainingLines[1].result)

        // Now test undo
        assertTrue(viewModel.canUndo.value[fileId] == true)
        viewModel.undo(fileId, rationalMode = false)
        testScheduler.advanceUntilIdle()

        val restoredLines = fakeDao.getLinesForFileSync(fileId)
        assertEquals(4, restoredLines.size)
        assertEquals("x = 10", restoredLines[0].expression)
        assertEquals("y = 20", restoredLines[1].expression)
        assertEquals("x + y", restoredLines[2].expression)
        assertEquals("z = 40", restoredLines[3].expression)
        assertEquals("30.0", restoredLines[2].result)
    }
}
