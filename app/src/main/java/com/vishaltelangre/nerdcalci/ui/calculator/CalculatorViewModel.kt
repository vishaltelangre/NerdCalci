package com.vishaltelangre.nerdcalci.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.backup.AutoBackupScheduler
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupFrequency
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.data.backup.BackupManager
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FileSnapshot(
    val lines: List<LineEntity>
)

class CalculatorViewModel(
    private val dao: CalculatorDao,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    companion object {
        private const val PREF_PRECISION = "precision"
        private const val PREF_THEME = "theme"
        private const val PREF_SHOW_LINE_NUMBERS = "show_line_numbers"
        private const val DEFAULT_THEME = "system"
    }

    // Theme state - load saved preference or default to "system"
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

    fun setTheme(theme: String) {
        _currentTheme.value = theme
        // Persist theme preference
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

    // Undo/Redo stacks with max limit per file
    private val undoStacks = mutableMapOf<Long, MutableList<FileSnapshot>>()
    private val redoStacks = mutableMapOf<Long, MutableList<FileSnapshot>>()
    private val maxHistorySize = Constants.MAX_HISTORY_SIZE

    // State flows to notify UI about undo/redo availability
    private val _canUndo = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val canUndo: StateFlow<Map<Long, Boolean>> = _canUndo

    private val _canRedo = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val canRedo: StateFlow<Map<Long, Boolean>> = _canRedo

    val allFiles = dao.getAllFiles()

    fun getLines(fileId: Long): Flow<List<LineEntity>> = dao.getLinesForFile(fileId)

    suspend fun getLineCount(fileId: Long): Int = withContext(Dispatchers.IO) {
        dao.getLineCountForFile(fileId)
    }

    // Update undo/redo availability for a file
    private fun updateUndoRedoState(fileId: Long) {
        _canUndo.value = _canUndo.value + (fileId to (undoStacks[fileId]?.isNotEmpty() == true))
        _canRedo.value = _canRedo.value + (fileId to (redoStacks[fileId]?.isNotEmpty() == true))
    }

    // Save current state before making changes (used for undo/redo)
    private suspend fun saveStateForUndo(fileId: Long) {
        val currentLines = dao.getLinesForFileSync(fileId)
        val snapshot = FileSnapshot(currentLines.map { it.copy() })

        val undoStack = undoStacks.getOrPut(fileId) { mutableListOf() }
        undoStack.add(snapshot)

        // Limit stack size
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }

        // Clear redo stack when new action is performed
        redoStacks[fileId]?.clear()

        updateUndoRedoState(fileId)
    }

    // Undo last action
    fun undo(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val undoStack = undoStacks[fileId] ?: return@launch
            if (undoStack.isEmpty()) return@launch

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

    // Redo last undone action
    fun redo(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val redoStack = redoStacks[fileId] ?: return@launch
            if (redoStack.isEmpty()) return@launch

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

    // Restore a snapshot with minimal UI flashing
    private suspend fun restoreSnapshot(fileId: Long, snapshot: FileSnapshot) {
        val currentLines = dao.getLinesForFileSync(fileId)
        val snapshotLines = snapshot.lines

        // Update existing lines in-place, then handle extras
        val minSize = minOf(currentLines.size, snapshotLines.size)

        // Update existing lines
        for (i in 0 until minSize) {
            val updatedLine = currentLines[i].copy(
                expression = snapshotLines[i].expression,
                result = snapshotLines[i].result,
                sortOrder = snapshotLines[i].sortOrder
            )
            dao.updateLine(updatedLine)
        }

        // If snapshot has more lines, insert the extras
        if (snapshotLines.size > currentLines.size) {
            for (i in minSize until snapshotLines.size) {
                dao.insertLine(snapshotLines[i].copy(id = 0))
            }
        }

        // If current has more lines, delete the extras
        if (currentLines.size > snapshotLines.size) {
            for (i in minSize until currentLines.size) {
                dao.deleteLine(currentLines[i])
            }
        }

        // Recalculate everything and batch-write results in one transaction
        val allLines = dao.getLinesForFileSync(fileId)
        val calculatedLines = MathEngine.calculate(allLines)
        dao.updateLines(fileId, calculatedLines)
    }

    // Clear undo/redo history for a file
    fun clearHistory(fileId: Long) {
        undoStacks[fileId]?.clear()
        redoStacks[fileId]?.clear()
        updateUndoRedoState(fileId)
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

    // Determine if result should be shown based on expression complexity
    private fun shouldShowResult(expression: String): Boolean {
        // Check if expression has operators (indicating computation)
        val hasOperators = expression.any { it in "+-*/%^" }

        // Check if it's a simple assignment like "a = 5"
        val simpleAssignmentRegex = Regex("""^\s*[a-zA-Z][a-zA-Z0-9\s]*\s*=\s*[\d.]+\s*$""")
        if (simpleAssignmentRegex.matches(expression)) {
            // For "a = 5", result is "5", no need to show result
            return false
        }

        // If there are operators or it's a variable reference, show result
        return hasOperators || !expression.contains("=")
    }

    // Update a line and recalculate everything from it downward
    fun updateLine(updatedLine: LineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save the user's current typing
            dao.updateLine(updatedLine)

            // Fetch all lines for this file to ensure context is correct
            val allLines = dao.getLinesForFileSync(updatedLine.fileId)

            // Find where the changed line sits. Preceding lines are not re-evaluated;
            // their variable state is inherited. If not found, fall back to 0 (recalculate all).
            val changedIndex = allLines.indexOfFirst { it.id == updatedLine.id }.coerceAtLeast(0)

            // Recalculate only affected lines (from changedIndex onward), inheriting variable
            // state from the preceding lines without re-evaluating them.
            val affectedLines = MathEngine.calculateFrom(allLines, changedIndex)

            // Batch-write all updated results in one DB transaction
            dao.updateLines(updatedLine.fileId, affectedLines)
        }
    }

    /**
     * Get the exact error message for a specific line by re-evaluating it on demand.
     */
    suspend fun getLineErrorMessage(fileId: Long, targetLineId: Long): String? {
        return withContext(Dispatchers.IO) {
            val allLines = dao.getLinesForFileSync(fileId)
            val targetIndex = allLines.indexOfFirst { it.id == targetLineId }
            if (targetIndex != -1) {
                MathEngine.getErrorDetails(allLines, targetIndex)
            } else {
                null
            }
        }
    }



    // Create a new file with a default "Untitled" name
    fun createNewFile(onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = dao.createNewFile("Untitled", System.currentTimeMillis())
            // Notify callback with new file ID on main thread
            withContext(Dispatchers.Main) {
                onCreated(fileId)
            }
        }
    }

    // Duplicate an existing file with all its lines
    fun duplicateFile(sourceFileId: Long, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = dao.duplicateFile(sourceFileId, System.currentTimeMillis())
            if (fileId != null) {
                // Notify callback with new file ID on main thread
                withContext(Dispatchers.Main) {
                    onCreated(fileId)
                }
            }
        }
    }

    fun addLine(fileId: Long, sortOrder: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save state for undo
            saveStateForUndo(fileId)

            val allLines = dao.getLinesForFileSync(fileId)

            // Shift all lines after this position down by 1
            allLines.filter { it.sortOrder >= sortOrder }
                .sortedByDescending { it.sortOrder }
                .forEach { line ->
                    dao.updateLine(line.copy(sortOrder = line.sortOrder + 1))
                }

            // Insert new line at the specified position
            dao.insertLine(LineEntity(fileId = fileId, sortOrder = sortOrder, expression = "", result = ""))

            // Recalculate everything from the new line downward
            val updatedAllLines = dao.getLinesForFileSync(fileId)
            val affectedLines = MathEngine.calculateFrom(updatedAllLines, sortOrder)
            dao.updateLines(fileId, affectedLines)
        }
    }

    fun deleteLine(line: LineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save state for undo
            saveStateForUndo(line.fileId)

            // Delete the line
            dao.deleteLine(line)

            // Shift all lines after this position up by 1
            val allLines = dao.getLinesForFileSync(line.fileId)
            allLines.filter { it.sortOrder > line.sortOrder }
                .sortedBy { it.sortOrder }
                .forEach { lineToShift ->
                    dao.updateLine(lineToShift.copy(sortOrder = lineToShift.sortOrder - 1))
                }

            // Recalculate everything from the deleted line's position downward
            val updatedAllLines = dao.getLinesForFileSync(line.fileId)
            val affectedLines = MathEngine.calculateFrom(updatedAllLines, line.sortOrder)
            dao.updateLines(line.fileId, affectedLines)
        }
    }

    // Clear all lines in a file
    fun clearAllLines(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            saveStateForUndo(fileId)
            dao.clearAllLines(fileId)
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = dao.getFileById(fileId)
            if (file != null) {
                dao.deleteFile(file)
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
            val file = dao.getFileById(fileId) ?: return@withContext
            val lines = dao.getLinesForFileSync(fileId)

            val isEmpty = lines.all { it.expression.isBlank() }
            val now = System.currentTimeMillis()
            val isRecent = now - file.createdAt < Constants.EMPTY_FILE_CLEANUP_THRESHOLD_MS
            val untitledRegex = Regex("""^Untitled(\s\(\d+\))?$""") // Matches "Untitled", "Untitled (1)", "Untitled (2)", etc.
            val isUntitled = untitledRegex.matches(file.name)

            if (isEmpty && isRecent && isUntitled) {
                dao.deleteFile(file)
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
            if (dao.doesFileExist(finalName, fileId)) {
                return@withContext false
            }

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
            if (excludeId == null) {
                dao.doesFileExist(name)
            } else {
                dao.doesFileExist(name, excludeId)
            }
        }
    }

    // Toggle pin status for a file
    fun togglePinFile(fileId: Long, onMaxPinnedReached: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = dao.getFileById(fileId)
            if (file != null) {
                // If trying to pin and already at max, notify user
                if (!file.isPinned) {
                    val pinnedCount = dao.getPinnedFilesCount()
                    if (pinnedCount >= Constants.MAX_PINNED_FILES) {
                        withContext(Dispatchers.Main) {
                            onMaxPinnedReached()
                        }
                        return@launch
                    }
                }
                dao.updateFile(file.copy(isPinned = !file.isPinned))
            }
        }
    }

    // Export all files to ZIP
    suspend fun exportAllFiles(context: Context, outputUri: Uri): Result<String> {
        return BackupManager.exportAllFiles(context, dao, outputUri)
    }

    // Import files from ZIP
    suspend fun importFiles(context: Context, inputUri: Uri): Result<String> {
        return BackupManager.importFiles(context, dao, inputUri)
    }

    // Copy current file to clipboard with results
    suspend fun copyFileToClipboard(context: Context, fileId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val lines = dao.getLinesForFileSync(fileId)
                val content = formatFileContent(lines, precision.value)

                withContext(Dispatchers.Main) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("NerdCalci File", content)
                    clipboard.setPrimaryClip(clip)
                }

                Result.success("Copied to clipboard")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
