package com.vishaltelangre.nerdcalci.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.core.FileContextLoader
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.core.EvalException
import com.vishaltelangre.nerdcalci.data.backup.AutoBackupScheduler
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupFrequency
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.data.backup.BackupManager
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.Suggestion
import com.vishaltelangre.nerdcalci.utils.SuggestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class HomeUiEvent {
    data class ShowMessage(val message: String) : HomeUiEvent()
}

data class FileSnapshot(
    val lines: List<LineEntity>
)

class CalculatorViewModel(
    private val dao: CalculatorDao,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    // Mutex to ensure atomic recalculation cycles per fileId
    private val calculationMutex = Mutex()

    private fun createFileContextLoader(currentFileId: Long): FileContextLoader {
        val cache = mutableMapOf<String, MathContext>()
        return object : FileContextLoader {
            override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
                cache[fileName]?.let { return it }
                val file = dao.getFileByName(fileName) ?: return null
                val lines = dao.getLinesForFileSync(file.id)
                val context = MathEngine.buildVariableState(lines, this, loadingStack)
                cache[fileName] = context
                return context
            }
        }
    }

    suspend fun getSuggestionsForFile(fileName: String): Set<Suggestion> {
        val file = dao.getFileByName(fileName) ?: return emptySet()
        val lines = dao.getLinesForFileSync(file.id)
        val context = try {
            MathEngine.buildVariableState(lines, createFileContextLoader(file.id))
        } catch (e: Exception) {
            MathContext()
        }
        val suggestions = mutableSetOf<Suggestion>()
        context.variables.keys
            .filter { it !in MathEngine.EXCLUDED_DOT_NOTATION_VARIABLES }
            .forEach {
                val type = if (MathEngine.dynamicVariableNames.contains(it)) SuggestionType.DYNAMIC_VARIABLE else SuggestionType.VARIABLE
                suggestions.add(Suggestion(it, type))
            }
        context.localFunctions.keys.forEach { suggestions.add(Suggestion(it, SuggestionType.LOCAL_FUNCTION)) }

        // Add select dynamic variables for dot notation with accurate type
        MathEngine.dynamicVariableNames
            .filter { it !in MathEngine.EXCLUDED_DOT_NOTATION_VARIABLES }
            .forEach {
                suggestions.add(Suggestion(it, SuggestionType.DYNAMIC_VARIABLE))
            }
        
        return suggestions
    }


    companion object {
        private const val PREF_PRECISION = "precision"
        private const val PREF_THEME = "theme"
        private const val PREF_SHOW_LINE_NUMBERS = "show_line_numbers"
        private const val PREF_SHOW_SUGGESTIONS = "show_suggestions"
        private const val PREF_SHOW_SYMBOLS_SHORTCUTS = "show_symbols_shortcuts"
        private const val PREF_SHOW_NUMBERS_SHORTCUTS = "show_numbers_shortcuts"
        private const val DEFAULT_THEME = "system"
    }

    private val _currentTheme = MutableStateFlow(
        prefs?.getString(PREF_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    )
    val currentTheme: StateFlow<String> = _currentTheme

    private val _precision = MutableStateFlow(
        (prefs?.getInt(PREF_PRECISION, Constants.DEFAULT_PRECISION) ?: Constants.DEFAULT_PRECISION)
            .coerceIn(Constants.MIN_PRECISION, Constants.MAX_PRECISION)
    )
    val precision: StateFlow<Int> = _precision

    private val _autoBackupEnabled = MutableStateFlow(
        prefs?.getBoolean(BackupManager.PREF_AUTO_BACKUP_ENABLED, true) ?: true
    )
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _backupFrequency = MutableStateFlow(
        BackupFrequency.fromPrefValue(
            prefs?.getString(
                BackupManager.PREF_AUTO_BACKUP_FREQUENCY,
                BackupFrequency.DAILY.prefValue
            )
        )
    )
    val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency

    private val _backupLocationMode = MutableStateFlow(
        BackupLocationMode.fromPrefValue(
            prefs?.getString(
                BackupManager.PREF_AUTO_BACKUP_LOCATION_MODE,
                BackupLocationMode.APP_STORAGE.prefValue
            )
        )
    )
    val backupLocationMode: StateFlow<BackupLocationMode> = _backupLocationMode

    private val _customBackupFolderUri = MutableStateFlow(
        prefs?.getString(BackupManager.PREF_AUTO_BACKUP_CUSTOM_FOLDER_URI, null)
    )
    val customBackupFolderUri: StateFlow<String?> = _customBackupFolderUri

    private val _availableBackups = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val availableBackups: StateFlow<List<BackupFileInfo>> = _availableBackups
    private val _lastBackupAt = MutableStateFlow(
        prefs?.getLong(BackupManager.PREF_LAST_BACKUP_AT, 0L)?.takeIf { it > 0L }
    )
    val lastBackupAt: StateFlow<Long?> = _lastBackupAt

    private val _showLineNumbers = MutableStateFlow(
        prefs?.getBoolean(PREF_SHOW_LINE_NUMBERS, true) ?: true
    )
    val showLineNumbers: StateFlow<Boolean> = _showLineNumbers

    private val _showSuggestions = MutableStateFlow(
        prefs?.getBoolean(PREF_SHOW_SUGGESTIONS, true) ?: true
    )
    val showSuggestions: StateFlow<Boolean> = _showSuggestions

    private val _showSymbolsShortcuts = MutableStateFlow(
        prefs?.getBoolean(PREF_SHOW_SYMBOLS_SHORTCUTS, true) ?: true
    )
    val showSymbolsShortcuts: StateFlow<Boolean> = _showSymbolsShortcuts

    private val _showNumbersShortcuts = MutableStateFlow(
        prefs?.getBoolean(PREF_SHOW_NUMBERS_SHORTCUTS, true) ?: true
    )
    val showNumbersShortcuts: StateFlow<Boolean> = _showNumbersShortcuts

    fun setTheme(theme: String) {
        _currentTheme.value = theme
        prefs?.edit()?.putString(PREF_THEME, theme)?.apply()
    }

    fun setPrecision(precision: Int) {
        val clampedPrecision = precision.coerceIn(Constants.MIN_PRECISION, Constants.MAX_PRECISION)
        _precision.value = clampedPrecision
        prefs?.edit()?.putInt(PREF_PRECISION, clampedPrecision)?.apply()
    }

    fun setShowLineNumbers(enabled: Boolean) {
        _showLineNumbers.value = enabled
        prefs?.edit()?.putBoolean(PREF_SHOW_LINE_NUMBERS, enabled)?.apply()
    }

    fun setShowSuggestions(enabled: Boolean) {
        _showSuggestions.value = enabled
        prefs?.edit()?.putBoolean(PREF_SHOW_SUGGESTIONS, enabled)?.apply()
    }

    fun setShowSymbolsShortcuts(enabled: Boolean) {
        _showSymbolsShortcuts.value = enabled
        prefs?.edit()?.putBoolean(PREF_SHOW_SYMBOLS_SHORTCUTS, enabled)?.apply()
    }

    fun setShowNumbersShortcuts(enabled: Boolean) {
        _showNumbersShortcuts.value = enabled
        prefs?.edit()?.putBoolean(PREF_SHOW_NUMBERS_SHORTCUTS, enabled)?.apply()
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        prefs?.edit()?.putBoolean(BackupManager.PREF_AUTO_BACKUP_ENABLED, enabled)?.apply()
        AutoBackupScheduler.sync(context, prefs ?: BackupManager.prefs(context))
    }

    fun setBackupFrequency(context: Context, frequency: BackupFrequency) {
        _backupFrequency.value = frequency
        prefs?.edit()?.putString(BackupManager.PREF_AUTO_BACKUP_FREQUENCY, frequency.prefValue)?.apply()
        AutoBackupScheduler.sync(context, prefs ?: BackupManager.prefs(context))
    }

    fun setBackupLocationToAppStorage(context: Context) {
        _backupLocationMode.value = BackupLocationMode.APP_STORAGE
        _customBackupFolderUri.value = null
        prefs?.edit()
            ?.putString(BackupManager.PREF_AUTO_BACKUP_LOCATION_MODE, BackupLocationMode.APP_STORAGE.prefValue)
            ?.remove(BackupManager.PREF_AUTO_BACKUP_CUSTOM_FOLDER_URI)
            ?.apply()
        AutoBackupScheduler.sync(context, prefs ?: BackupManager.prefs(context))
        refreshBackups(context)
    }

    fun setCustomBackupFolder(context: Context, uri: Uri) {
        _backupLocationMode.value = BackupLocationMode.CUSTOM_FOLDER
        _customBackupFolderUri.value = uri.toString()
        prefs?.edit()
            ?.putString(BackupManager.PREF_AUTO_BACKUP_LOCATION_MODE, BackupLocationMode.CUSTOM_FOLDER.prefValue)
            ?.putString(BackupManager.PREF_AUTO_BACKUP_CUSTOM_FOLDER_URI, uri.toString())
            ?.apply()
        AutoBackupScheduler.sync(context, prefs ?: BackupManager.prefs(context))
        refreshBackups(context)
    }

    fun refreshBackups(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _availableBackups.value = BackupManager.listBackups(context)
            _lastBackupAt.value = (prefs ?: BackupManager.prefs(context))
                .getLong(BackupManager.PREF_LAST_BACKUP_AT, 0L)
                .takeIf { it > 0L }
        }
    }

    suspend fun backupNow(context: Context): Result<String> {
        val result = BackupManager.backupNow(context, dao)
        refreshBackups(context)
        return result
    }

    suspend fun restoreFromBackup(context: Context, backup: BackupFileInfo): Result<String> {
        val result = BackupManager.restoreFromBackup(context, dao, backup)
        refreshBackups(context)
        return result
    }

    private val undoStacks = mutableMapOf<Long, MutableList<FileSnapshot>>()
    private val redoStacks = mutableMapOf<Long, MutableList<FileSnapshot>>()
    private val maxHistorySize = Constants.MAX_HISTORY_SIZE

    private val _canUndo = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val canUndo: StateFlow<Map<Long, Boolean>> = _canUndo

    private val _canRedo = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val canRedo: StateFlow<Map<Long, Boolean>> = _canRedo

    private val _excludedFileIds = MutableStateFlow<Set<Long>>(emptySet())
    val excludedFileIds: StateFlow<Set<Long>> = _excludedFileIds.asStateFlow()

    private val _uiEvents = MutableSharedFlow<HomeUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val allFiles: Flow<List<FileEntity>> = dao.getAllFiles()

    fun getLines(fileId: Long): Flow<List<LineEntity>> = dao.getLinesForFile(fileId)

    suspend fun getLineCount(fileId: Long): Int = withContext(Dispatchers.IO) {
        dao.getLineCountForFile(fileId)
    }

    private fun updateUndoRedoState(fileId: Long) {
        _canUndo.value = _canUndo.value + (fileId to (undoStacks[fileId]?.isNotEmpty() == true))
        _canRedo.value = _canRedo.value + (fileId to (redoStacks[fileId]?.isNotEmpty() == true))
    }

    private suspend fun saveStateForUndo(fileId: Long) {
        val currentLines = dao.getLinesForFileSync(fileId)
        val snapshot = FileSnapshot(currentLines.map { it.copy() })
        val undoStack = undoStacks.getOrPut(fileId) { mutableListOf() }
        undoStack.add(snapshot)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStacks[fileId]?.clear()
        updateUndoRedoState(fileId)
    }

    fun undo(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val undoStack = undoStacks[fileId] ?: return@withLock
                if (undoStack.isEmpty()) return@withLock

                // Save current state to redo stack
                val currentLines = dao.getLinesForFileSync(fileId)
                val currentSnapshot = FileSnapshot(currentLines.map { it.copy() })
                val redoStack = redoStacks.getOrPut(fileId) { mutableListOf() }
                redoStack.add(currentSnapshot)

                // Restore previous state
                val previousSnapshot = undoStack.removeAt(undoStack.size - 1)
                restoreSnapshot(fileId, previousSnapshot)

                updateUndoRedoState(fileId)
            }
        }
    }

    fun redo(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val redoStack = redoStacks[fileId] ?: return@withLock
                if (redoStack.isEmpty()) return@withLock

                // Save current state to undo stack
                val currentLines = dao.getLinesForFileSync(fileId)
                val currentSnapshot = FileSnapshot(currentLines.map { it.copy() })
                val undoStack = undoStacks.getOrPut(fileId) { mutableListOf() }
                undoStack.add(currentSnapshot)

                // Restore redo state
                val redoSnapshot = redoStack.removeAt(redoStack.size - 1)
                restoreSnapshot(fileId, redoSnapshot)

                updateUndoRedoState(fileId)
            }
        }
    }

    private suspend fun restoreSnapshot(fileId: Long, snapshot: FileSnapshot) {
        dao.restoreLines(fileId, snapshot.lines)

        // Recalculate everything and batch-write results in one transaction
        val allLines = dao.getLinesForFileSync(fileId)
        val calculatedLines = MathEngine.calculate(allLines, createFileContextLoader(fileId))
        dao.updateLines(fileId, calculatedLines)
    }

    fun clearHistory(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                undoStacks[fileId]?.clear()
                redoStacks[fileId]?.clear()
                updateUndoRedoState(fileId)
            }
        }
    }

    fun recalculateFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val allLines = dao.getLinesForFileSync(fileId)
                val calculatedLines = MathEngine.calculate(allLines, createFileContextLoader(fileId))
                dao.updateLines(fileId, calculatedLines)
            }
        }
    }

    // Format lines with intelligent result display
    private fun formatFileContent(lines: List<LineEntity>, precision: Int): String {
        return lines.joinToString("\n") { line ->
            val expr = line.expression.trim()
            val rawResult = line.result.trim()
            val displayResult = MathEngine.formatDisplayResult(rawResult, precision)

            // Don't show result if:
            // - Expression is empty or result is empty/error
            // - It's a comment line (starts with #)
            // - It's a simple assignment like "a = 5" where result is just "5"
            when {
                expr.isEmpty() || rawResult.isBlank() || rawResult == "Err" -> expr
                expr.trimStart().startsWith("#") -> expr // Full comment line
                shouldShowResult(expr) -> "$expr # $displayResult"
                else -> expr
            }
        }
    }

    private fun shouldShowResult(expression: String): Boolean {
        val hasOperators = expression.any { it in "+-*/%^" }
        val simpleAssignmentRegex = Regex("""^\s*[a-zA-Z][a-zA-Z0-9\s]*\s*=\s*[\d.]+\s*$""")
        if (simpleAssignmentRegex.matches(expression)) return false
        return hasOperators || !expression.contains("=")
    }

    // Update a line and recalculate everything from it downward
    fun updateLine(updatedLine: LineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                // Save the user's current typing
                dao.updateLine(updatedLine)

                // Fetch all lines for this file to ensure context is correct
                val allLines = dao.getLinesForFileSync(updatedLine.fileId)

                // Find where the changed line sits. Preceding lines are not re-evaluated;
                // their variable state is inherited. If not found, fall back to 0 (recalculate all).
                val changedIndex = allLines.indexOfFirst { it.id == updatedLine.id }.coerceAtLeast(0)

                // Recalculate only affected lines (from changedIndex onward), inheriting variable
                // state from the preceding lines without re-evaluating them.
                val affectedLines = MathEngine.calculateFrom(allLines, changedIndex, createFileContextLoader(updatedLine.fileId))

                // Batch-write all updated results in one DB transaction
                dao.updateLines(updatedLine.fileId, affectedLines)
            }
        }
    }

    suspend fun getLineErrorMessage(fileId: Long, targetLineId: Long): String? {
        return withContext(Dispatchers.IO) {
            val file = dao.getFileById(fileId) ?: return@withContext null
            val allLines = dao.getLinesForFileSync(fileId)
            val targetIndex = allLines.indexOfFirst { it.id == targetLineId }
            if (targetIndex != -1) MathEngine.getErrorDetails(allLines, targetIndex, createFileContextLoader(fileId), setOf(file.name)) else null
        }
    }

    fun createNewFile(onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = dao.createNewFile("Untitled", System.currentTimeMillis())
            withContext(Dispatchers.Main) { onCreated(fileId) }
        }
    }

    fun duplicateFile(sourceFileId: Long, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = dao.duplicateFile(sourceFileId, System.currentTimeMillis())
            if (fileId != null) {
                withContext(Dispatchers.Main) { onCreated(fileId) }
            }
        }
    }

    suspend fun addLine(fileId: Long, sortOrder: Int, afterLineId: Long? = null): Long {
        return withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                // Save state for undo
                saveStateForUndo(fileId)

                val newLine = LineEntity(fileId = fileId, sortOrder = sortOrder, expression = "", result = "")
                val newId = dao.moveAndInsertLine(fileId, afterLineId, newLine)

                // Fetch the now-normalized lines
                val updatedAllLines = dao.getLinesForFileSync(fileId)

                // Find the actual index of the new line (normalization ensures it matches current index)
                val insertIndex = updatedAllLines.indexOfFirst { it.id == newId }.coerceAtLeast(0)

                // Recalculate everything from the new line downward
                val affectedLines = MathEngine.calculateFrom(updatedAllLines, insertIndex, createFileContextLoader(fileId))
                dao.updateLines(fileId, affectedLines)

                newId
            }
        }
    }

    fun deleteLine(line: LineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                // Save state for undo
                saveStateForUndo(line.fileId)

                // Get current lines to find the actual index before deletion
                val currentLines = dao.getLinesForFileSync(line.fileId)
                val deletedIndex = currentLines.indexOfFirst { it.id == line.id }
                    .let { if (it >= 0) it else line.sortOrder }

                // Atomically delete line and fix order numbers
                dao.deleteAndNormalize(line)

                val updatedLines = dao.getLinesForFileSync(line.fileId)

                // Recalculate from the spot where we deleted.
                // Because we normalized (0, 1, 2...), the line that was below
                // the deleted one now sits at the deleted line's old position.
                if (deletedIndex in updatedLines.indices) {
                    val affectedLines = MathEngine.calculateFrom(updatedLines, deletedIndex, createFileContextLoader(line.fileId))
                    dao.updateLines(line.fileId, affectedLines)
                }
            }
        }
    }

    fun clearAllLines(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                saveStateForUndo(fileId)
                dao.clearAllLines(fileId)
            }
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val file = dao.getFileById(fileId)
                if (file != null) {
                    dao.deleteFile(file)
                    // Purge history for this file
                    undoStacks.remove(fileId)
                    redoStacks.remove(fileId)
                    updateUndoRedoState(fileId)
                }
                _excludedFileIds.value = _excludedFileIds.value - fileId
            }
        }
    }

    fun hideFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                if (!_excludedFileIds.value.contains(fileId)) {
                    _excludedFileIds.value = _excludedFileIds.value + fileId
                }
            }
        }
    }

    fun undoHideFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                _excludedFileIds.value = _excludedFileIds.value - fileId
            }
        }
    }

    fun permanentDeleteExclusions() {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val idsToDelete = _excludedFileIds.value
                if (idsToDelete.isEmpty()) return@withLock
                idsToDelete.forEach { fileId ->
                    val file = dao.getFileById(fileId)
                    if (file != null) {
                        dao.deleteFile(file)
                        undoStacks.remove(fileId)
                        redoStacks.remove(fileId)
                    }
                }
                _excludedFileIds.value = _excludedFileIds.value - idsToDelete
            }
        }
    }

    /**
     * Deletes a file if it is empty and was created a while ago.
     * This is used when navigating back from the calculator screen to prevent cluttering
     * the home screen with accidentally created empty files.
     */
    suspend fun deleteFileIfEmptyAndRecent(fileId: Long) {
        withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                val file = dao.getFileById(fileId) ?: return@withLock
                val lines = dao.getLinesForFileSync(fileId)

                val isEmpty = lines.all { it.expression.isBlank() }
                val now = System.currentTimeMillis()
                val isRecent = now - file.createdAt < Constants.EMPTY_FILE_CLEANUP_THRESHOLD_MS
                val untitledRegex = Regex("""^Untitled(\s\(\d+\))?$""") // Matches "Untitled", "Untitled (1)", "Untitled (2)", etc.
                val isUntitled = untitledRegex.matches(file.name)

                if (isEmpty && isRecent && isUntitled) {
                    dao.deleteFile(file)
                    // Purge history for this file
                    undoStacks.remove(fileId)
                    redoStacks.remove(fileId)
                    updateUndoRedoState(fileId)
                }
            }
        }
    }

    suspend fun renameFile(fileId: Long, newName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val trimmedName = newName.trim()
            if (trimmedName.isBlank()) return@withContext false

            val finalName = if (trimmedName.length > Constants.MAX_FILE_NAME_LENGTH) {
                trimmedName.substring(0, Constants.MAX_FILE_NAME_LENGTH).trim()
            } else {
                trimmedName
            }

            if (finalName.isBlank()) return@withContext false

            // Check if name is taken by another file
            if (dao.doesFileExist(finalName, fileId)) return@withContext false
            val file = dao.getFileById(fileId)
            if (file != null) {
                dao.updateFile(file.copy(name = finalName))
                true
            } else {
                false
            }
        }
    }

    suspend fun doesFileExist(name: String, excludeId: Long? = null): Boolean {
        return withContext(Dispatchers.IO) {
            if (excludeId == null) dao.doesFileExist(name) else dao.doesFileExist(name, excludeId)
        }
    }

    fun togglePinFile(fileId: Long) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                dao.togglePinFileIfAllowed(fileId, Constants.MAX_PINNED_FILES)
            }
            if (!success) {
                _uiEvents.emit(HomeUiEvent.ShowMessage("Maximum ${Constants.MAX_PINNED_FILES} files can be pinned"))
            }
        }
    }

    suspend fun exportAllFiles(context: Context, outputUri: Uri): Result<String> {
        return BackupManager.exportAllFiles(context, dao, outputUri)
    }

    suspend fun importFiles(context: Context, inputUri: Uri): Result<String> {
        return BackupManager.importFiles(context, dao, inputUri)
    }

    suspend fun copyFileToClipboard(context: Context, fileId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val lines = dao.getLinesForFileSync(fileId)
                val content = formatFileContent(lines, precision.value)
                withContext(Dispatchers.Main) { copyToClipboard(context, content, "NerdCalci File") }
                Result.success("Copied to clipboard")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "NerdCalci Result") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
