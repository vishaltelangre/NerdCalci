package com.vishaltelangre.nerdcalci.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.utils.RegionUtils
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.core.FileContextLoader
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.core.EvalException
import com.vishaltelangre.nerdcalci.data.backup.AutoBackupScheduler
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupFrequency
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.data.backup.BackupManager
import com.vishaltelangre.nerdcalci.data.backup.ConflictResolution
import com.vishaltelangre.nerdcalci.data.backup.RestoreResult
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.vishaltelangre.nerdcalci.utils.FileUtils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import com.vishaltelangre.nerdcalci.data.sync.SyncManager
import com.vishaltelangre.nerdcalci.core.LaunchMode
import java.text.SimpleDateFormat

sealed class HomeUiEvent {
    data class ShowMessage(val message: String) : HomeUiEvent()
}

private const val TAG = "CalculatorViewModel"

data class FileSnapshot(
    val lines: List<LineEntity>
)

data class RestoreProgressState(
    val isProcessing: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentFile: String = "",
    val conflictFile: String? = null,
    val localConflictModified: Long = 0L,
    val zipConflictModified: Long = 0L,
    val applyToAll: ConflictResolution? = null,
    val completionMessage: String? = null,
    val overwrittenCount: Int = 0,
    val processedCount: Int = 0,
    val addedCount: Int = 0,
    val skippedCount: Int = 0,
    val isSuccess: Boolean = true
)

class CalculatorViewModel(
    private val dao: CalculatorDao,
    private val prefs: SharedPreferences? = null
) : ViewModel() {
    private val _launchMode = MutableStateFlow(
        LaunchMode.fromPrefValue(prefs?.getString(Constants.PREF_LAUNCH_MODE, null))
    )
    val launchMode: StateFlow<LaunchMode> = _launchMode

    private val _launchFileId = MutableStateFlow(
        prefs?.getLong(Constants.PREF_LAUNCH_FILE_ID, -1L)?.takeIf { it != -1L }
    )
    val launchFileId: StateFlow<Long?> = _launchFileId

    private val _autoOpenFileId = MutableStateFlow<Long?>(null)
    val autoOpenFileId: StateFlow<Long?> = _autoOpenFileId

    private val _isAutoOpenReady = MutableStateFlow(false)
    val isAutoOpenReady: StateFlow<Boolean> = _isAutoOpenReady

    private val _currentTheme = MutableStateFlow(
        prefs?.getString(PREF_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    )
    val currentTheme: StateFlow<String> = _currentTheme

    private val _scratchpadFileId = MutableStateFlow<Long?>(null)
    val scratchpadFileId: StateFlow<Long?> = _scratchpadFileId

    private val _isScratchpadReady = MutableStateFlow(false)
    val isScratchpadReady: StateFlow<Boolean> = _isScratchpadReady

    private val _precision = MutableStateFlow(
        (prefs?.getInt(Constants.SYNC_ENGINE_PRECISION, Constants.DEFAULT_PRECISION) ?: Constants.DEFAULT_PRECISION)
            .coerceIn(Constants.MIN_PRECISION, Constants.MAX_PRECISION)
    )
    val precision: StateFlow<Int> = _precision

    private val _autoBackupEnabled = MutableStateFlow(
        prefs?.getBoolean(BackupManager.PREF_AUTO_BACKUP_ENABLED, true) ?: true
    )
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _syncEnabled = MutableStateFlow(
        prefs?.getBoolean(SyncManager.PREF_SYNC_ENABLED, false) ?: false
    )
    val syncEnabled: StateFlow<Boolean> = _syncEnabled

    private val _syncFolderUri = MutableStateFlow(
        prefs?.getString(SyncManager.PREF_SYNC_FOLDER_URI, null)
    )
    val syncFolderUri: StateFlow<String?> = _syncFolderUri

    private val _lastSyncAt = MutableStateFlow(
        prefs?.getLong(SyncManager.PREF_LAST_SYNC_AT, 0L)?.takeIf { it > 0L }
    )
    val lastSyncAt: StateFlow<Long?> = _lastSyncAt

    // Mutex to ensure atomic recalculation cycles per fileId
    private val calculationMutex = Mutex()

    private val _restoreProgress = MutableStateFlow(RestoreProgressState())
    val restoreProgress = _restoreProgress.asStateFlow()

    private var conflictDeferred: kotlinx.coroutines.CompletableDeferred<ConflictResolution>? = null

    init {
        viewModelScope.launch {
            try {
                // Perform migration if needed
                migrateLaunchSettings()

                // Initialize scratchpad (always needed for the "Open Scratchpad" button)
                ensureScratchpadExists()

                // Resolve which file to auto-open based on current mode
                resolveAutoOpenFile()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                _isAutoOpenReady.value = true // Fallback to home screen
            }
        }
    }

    private fun migrateLaunchSettings() {
        val prefs = prefs ?: return
        // If the new launch mode isn't set but the old toggle was true, migrate to SCRATCHPAD
        if (!prefs.contains(Constants.PREF_LAUNCH_MODE) &&
            prefs.getBoolean(Constants.PREF_AUTO_OPEN_SCRATCHPAD, false)) {
            setLaunchMode(LaunchMode.SCRATCHPAD)
        }

        // TODO: Remove PREF_AUTO_OPEN_SCRATCHPAD in a future release
        if (prefs.contains(Constants.PREF_AUTO_OPEN_SCRATCHPAD)) {
            prefs.edit().remove(Constants.PREF_AUTO_OPEN_SCRATCHPAD).apply()
        }
    }

    fun onValidateLaunchFile(fileId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = withContext(Dispatchers.IO) {
                dao.getFileById(fileId) != null
            }
            callback(exists)
        }
    }

    private suspend fun resolveAutoOpenFile() {
        withContext(Dispatchers.IO) {
            try {
                val mode = _launchMode.value
                val fileId = when (mode) {
                    LaunchMode.NOT_SET -> null
                    LaunchMode.SCRATCHPAD -> {
                        // Wait for scratchpad to be ready if it's not yet
                        var retry = 0
                        while (_scratchpadFileId.value == null && retry < 10) {
                            kotlinx.coroutines.delay(100)
                            retry++
                        }
                        _scratchpadFileId.value
                    }
                    LaunchMode.JOURNAL -> {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        dao.createJournalFileIfAbsent(today, System.currentTimeMillis())
                    }
                    LaunchMode.SPECIFIC_FILE -> {
                        val id = _launchFileId.value
                        if (id != null && dao.getFileById(id) != null) {
                            id
                        } else {
                            // Reset if file missing
                            withContext(Dispatchers.Main) {
                                setLaunchMode(LaunchMode.NOT_SET)
                                prefs?.edit()?.remove(Constants.PREF_LAUNCH_FILE_ID)?.apply()
                                _launchFileId.value = null
                            }
                            null
                        }
                    }
                }
                _autoOpenFileId.value = fileId
            } finally {
                _isAutoOpenReady.value = true
            }
        }
    }

    fun resolveConflict(resolution: ConflictResolution, rememberChoice: Boolean) {
        if (rememberChoice) {
            _restoreProgress.value = _restoreProgress.value.copy(applyToAll = resolution)
        }
        conflictDeferred?.complete(resolution)
    }

    fun dismissRestoreStats() {
        _restoreProgress.value = RestoreProgressState()
    }

    private fun createFileContextLoader(currentFileId: Long, rationalMode: Boolean = false): FileContextLoader {
        val cache = mutableMapOf<String, MathContext>()
        return object : FileContextLoader {
            override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
                cache[fileName]?.let { return it }
                val file = dao.getFileByName(fileName) ?: return null
                val lines = dao.getLinesForFileSync(file.id)
                val context = MathEngine.buildVariableState(lines, this, loadingStack, rationalMode = rationalMode)
                cache[fileName] = context
                return context
            }
        }
    }

    private suspend fun ensureScratchpadExists() {
        withContext(Dispatchers.IO) {
            try {
                val existing = dao.getTemporaryFile()
                if (existing != null) {
                    // Clear contents (session reset)
                    dao.clearAllLines(existing.id)
                    _scratchpadFileId.value = existing.id
                } else {
                    _scratchpadFileId.value = dao.createTemporaryFileWithInitialLine()
                }
                _isScratchpadReady.value = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to ensure scratchpad exists", e)
                _scratchpadFileId.value = null
                _isScratchpadReady.value = false
            }
        }
    }

    suspend fun getSuggestionsForFile(fileName: String): Set<Suggestion> {
        val file = dao.getFileByName(fileName) ?: return emptySet()
        val lines = dao.getLinesForFileSync(file.id)
        val effectiveRationalMode = _rationalMode.value
        val context = try {
            MathEngine.buildVariableState(lines, createFileContextLoader(file.id, effectiveRationalMode), rationalMode = effectiveRationalMode)
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
        private const val PREF_THEME = "theme"
        private const val PREF_SHOW_LINE_NUMBERS = "show_line_numbers"
        private const val PREF_SHOW_SUGGESTIONS = "show_suggestions"
        private const val PREF_SHOW_SYMBOLS_SHORTCUTS = "show_symbols_shortcuts"
        private const val PREF_SHOW_NUMBERS_SHORTCUTS = "show_numbers_shortcuts"
        private const val PREF_REGION_CODE = "number_format_region_code"
        private const val PREF_GROUPING_SEPARATOR_ENABLED = "number_format_grouping_separator_enabled"
        private const val DEFAULT_THEME = "system"
    }

    private val syncInProgress = AtomicBoolean(false)
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

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

    private val _regionCode = MutableStateFlow(
        prefs?.getString(PREF_REGION_CODE, RegionUtils.SYSTEM_DEFAULT) ?: RegionUtils.SYSTEM_DEFAULT
    )

    val regionCode: StateFlow<String> = _regionCode

    private val _groupingSeparatorEnabled = MutableStateFlow(
        prefs?.getBoolean(PREF_GROUPING_SEPARATOR_ENABLED, true) ?: true
    )
    val groupingSeparatorEnabled: StateFlow<Boolean> = _groupingSeparatorEnabled

    private val _rationalMode = MutableStateFlow(
        prefs?.getBoolean(Constants.SYNC_ENGINE_RATIONAL_MODE, Constants.DEFAULT_RATIONAL_MODE) ?: Constants.DEFAULT_RATIONAL_MODE
    )
    val rationalMode: StateFlow<Boolean> = _rationalMode

    fun setLaunchMode(mode: LaunchMode) {
        _launchMode.value = mode
        prefs?.edit()?.putString(Constants.PREF_LAUNCH_MODE, mode.prefValue)?.apply()
    }

    fun setLaunchFileId(fileId: Long?) {
        _launchFileId.value = fileId
        val prefs = prefs ?: return
        if (fileId != null) {
            prefs.edit().putLong(Constants.PREF_LAUNCH_FILE_ID, fileId).apply()
            setLaunchMode(LaunchMode.SPECIFIC_FILE)
        } else {
            prefs.edit().remove(Constants.PREF_LAUNCH_FILE_ID).apply()
        }
    }

    /**
     * Validates that the specifically set launch file still exists.
     * Resets to NOT_SET if it has been deleted.
     */
    fun validateSpecificFileSetting() {
        validateLaunchFile()
    }

    /**
     * Validates that the specifically set launch file still exists.
     * Resets to NOT_SET if it has been deleted.
     */
    fun validateLaunchFile(fileId: Long? = null, onResult: ((Boolean) -> Unit)? = null) {
        val id = fileId ?: if (_launchMode.value == LaunchMode.SPECIFIC_FILE) _launchFileId.value else null
        if (id != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val exists = dao.getFileById(id) != null
                if (!exists && fileId == null) { // Only auto-reset if we're validating the CURRENT setting
                    withContext(Dispatchers.Main) {
                        setLaunchMode(LaunchMode.NOT_SET)
                        prefs?.edit()?.remove(Constants.PREF_LAUNCH_FILE_ID)?.apply()
                        _launchFileId.value = null
                    }
                }
                withContext(Dispatchers.Main) {
                    onResult?.invoke(exists)
                }
            }
        } else {
            onResult?.invoke(false)
        }
    }

    fun setTheme(theme: String) {
        _currentTheme.value = theme
        prefs?.edit()?.putString(PREF_THEME, theme)?.apply()
    }

    fun setPrecision(precision: Int) {
        val clampedPrecision = precision.coerceIn(Constants.MIN_PRECISION, Constants.MAX_PRECISION)
        _precision.value = clampedPrecision
        prefs?.edit()?.putInt(Constants.SYNC_ENGINE_PRECISION, clampedPrecision)?.apply()
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

    fun setRationalMode(enabled: Boolean) {
        _rationalMode.value = enabled
        prefs?.edit()?.putBoolean(Constants.SYNC_ENGINE_RATIONAL_MODE, enabled)?.apply()
    }

    fun setShowNumbersShortcuts(enabled: Boolean) {
        _showNumbersShortcuts.value = enabled
        prefs?.edit()?.putBoolean(PREF_SHOW_NUMBERS_SHORTCUTS, enabled)?.apply()
    }

    fun setRegionCode(code: String) {
        _regionCode.value = code
        prefs?.edit()
            ?.putString(PREF_REGION_CODE, code)
            ?.apply()
    }

    fun setGroupingSeparatorEnabled(enabled: Boolean) {
        _groupingSeparatorEnabled.value = enabled
        prefs?.edit()
            ?.putBoolean(PREF_GROUPING_SEPARATOR_ENABLED, enabled)
            ?.apply()
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

    fun setSyncEnabled(context: Context, enabled: Boolean) {
        _syncEnabled.value = enabled
        prefs?.edit()?.putBoolean(SyncManager.PREF_SYNC_ENABLED, enabled)?.apply()
        if (enabled && SyncManager.isSyncActive(context)) syncFiles(context)
    }

    fun setSyncFolder(context: Context, uri: Uri) {
        _syncFolderUri.value = uri.toString()
        prefs?.edit()?.putString(SyncManager.PREF_SYNC_FOLDER_URI, uri.toString())?.apply()
        _syncEnabled.value = true
        prefs?.edit()?.putBoolean(SyncManager.PREF_SYNC_ENABLED, true)?.apply()
        if (SyncManager.isSyncActive(context)) syncFiles(context)
    }

    fun syncFiles(context: Context) {
        // Do not sync if disabled or no folder
        if (!SyncManager.isSyncActive(context)) return
        if (!syncInProgress.compareAndSet(false, true)) return

        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val result = SyncManager.performSync(context, dao)
                if (result.isFailure) {
                    _uiEvents.emit(HomeUiEvent.ShowMessage("Sync failed: ${result.exceptionOrNull()?.message}"))
                } else {
                    _lastSyncAt.value = System.currentTimeMillis()
                    refreshBackups(context) // refresh if backups were updated
                }
            } finally {
                _isSyncing.value = false
                syncInProgress.set(false)
            }
        }
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

    fun restoreFromBackup(context: Context, backup: BackupFileInfo) {
        executeRestoreOrImport("Restored", operation = {
            BackupManager.restoreFromBackup(
                context = context,
                dao = dao,
                backup = backup,
                onProgress = { current, total, fileName ->
                    _restoreProgress.value = _restoreProgress.value.copy(
                        current = current,
                        total = total,
                        currentFile = fileName
                    )
                },
                onConflict = conflict@ { fileName, localModified, zipModified ->
                    val state = _restoreProgress.value
                    if (state.applyToAll != null) return@conflict state.applyToAll

                    _restoreProgress.value = _restoreProgress.value.copy(
                        conflictFile = fileName,
                        localConflictModified = localModified,
                        zipConflictModified = zipModified
                    )
                    val deferred = kotlinx.coroutines.CompletableDeferred<ConflictResolution>()
                    conflictDeferred = deferred
                    val res = deferred.await()
                    conflictDeferred = null
                    _restoreProgress.value = _restoreProgress.value.copy(conflictFile = null)
                    res
                }
            )
        }, onSuccess = { refreshBackups(context) })
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

    fun undo(fileId: Long, rationalMode: Boolean? = null) {
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
                restoreSnapshot(fileId, previousSnapshot, rationalMode)

                updateUndoRedoState(fileId)
            }
        }
    }

    fun redo(fileId: Long, rationalMode: Boolean? = null) {
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
                restoreSnapshot(fileId, redoSnapshot, rationalMode)

                updateUndoRedoState(fileId)
            }
        }
    }

    private suspend fun restoreSnapshot(fileId: Long, snapshot: FileSnapshot, rationalMode: Boolean? = null) {
        dao.restoreLines(fileId, snapshot.lines)

        // Recalculate everything and batch-write results in one transaction
        val allLines = dao.getLinesForFileSync(fileId)
        val effectiveRationalMode = rationalMode ?: _rationalMode.value
        val calculatedLines = MathEngine.calculate(allLines, createFileContextLoader(fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
        val versionedLines = calculatedLines.map { it.copy(version = it.version + 1) }
        dao.updateLines(fileId, versionedLines)
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

    fun recalculateFile(fileId: Long, rationalMode: Boolean? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val allLines = dao.getLinesForFileSync(fileId)
                val effectiveRationalMode = rationalMode ?: _rationalMode.value
                val calculatedLines = MathEngine.calculate(allLines, createFileContextLoader(fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
                val versionedLines = calculatedLines.map { it.copy(version = it.version + 1) }
                dao.updateLines(fileId, versionedLines, updateTimestamp = false)
            }
        }
    }


    // Update a line and recalculate everything from it downward
    fun updateLine(updatedLine: LineEntity, rationalMode: Boolean? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            updateLineInternal(updatedLine, rationalMode)
        }
    }

    suspend fun updateLineInternal(updatedLine: LineEntity, rationalMode: Boolean? = null) {
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
            val effectiveRationalMode = rationalMode ?: _rationalMode.value
            val affectedLines = MathEngine.calculateFrom(allLines, changedIndex, createFileContextLoader(updatedLine.fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
            val versionedLines = affectedLines.map { it.copy(version = it.version + 1) }

            // Batch-write all updated results in one DB transaction
            dao.updateLines(updatedLine.fileId, versionedLines)
        }
    }

    suspend fun getLineErrorMessage(fileId: Long, targetLineId: Long, rationalMode: Boolean? = null): String? {
        return withContext(Dispatchers.IO) {
            val file = dao.getFileById(fileId) ?: return@withContext null
            val allLines = dao.getLinesForFileSync(fileId)
            val targetIndex = allLines.indexOfFirst { it.id == targetLineId }
            val effectiveRationalMode = rationalMode ?: _rationalMode.value
            if (targetIndex != -1) MathEngine.getErrorDetails(allLines, targetIndex, createFileContextLoader(fileId, effectiveRationalMode), setOf(file.name), rationalMode = effectiveRationalMode) else null
        }
    }

    fun createNewFile(context: Context, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = dao.createNewFile("Untitled", System.currentTimeMillis())
            if (SyncManager.isSyncActive(context)) {
                syncFiles(context)
            }
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

    suspend fun addLine(
        fileId: Long,
        sortOrder: Int,
        expression: String = "",
        afterLineId: Long? = null,
        rationalMode: Boolean? = null
    ): Long {
        return withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                // Save state for undo
                saveStateForUndo(fileId)

                val newLine = LineEntity(fileId = fileId, sortOrder = sortOrder, expression = expression, result = "")
                val newId = dao.moveAndInsertLine(fileId, afterLineId, newLine)

                // Fetch the now-normalized lines
                val updatedAllLines = dao.getLinesForFileSync(fileId)

                // Find the actual index of the new line (normalization ensures it matches current index)
                val insertIndex = updatedAllLines.indexOfFirst { it.id == newId }.coerceAtLeast(0)

                // Recalculate everything from the new line downward
                val effectiveRationalMode = rationalMode ?: _rationalMode.value
                val affectedLines = MathEngine.calculateFrom(updatedAllLines, insertIndex, createFileContextLoader(fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
                val versionedLines = affectedLines.map { it.copy(version = it.version + 1) }
                dao.updateLines(fileId, versionedLines, updateTimestamp = false)

                newId
            }
        }
    }

    /**
     * Splits a line at the given [splitIndex] within its expression.
     * The text before [splitIndex] stays on the original line, and text after it
     * is moved to a new line inserted immediately below.
     *
     * This operation is atomic and ensures consistent state between the split lines.
     */
    suspend fun splitLine(
        lineId: Long,
        splitIndex: Int,
        currentExpression: String? = null,
        rationalMode: Boolean? = null
    ): Long {
        return withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                val line = dao.getLineById(lineId) ?: return@withLock -1L
                val fileId = line.fileId
                saveStateForUndo(fileId)

                val originalExpression = currentExpression ?: line.expression
                val safeSplitIndex = splitIndex.coerceIn(0, originalExpression.length)
                val keep = originalExpression.substring(0, safeSplitIndex)
                val move = originalExpression.substring(safeSplitIndex)

                // Update the current line with the prefix and clear result
                dao.updateLine(line.copy(expression = keep, result = "", version = line.version + 1))

                // Insert the new line with the suffix and clear result
                val newLine = LineEntity(
                    fileId = fileId,
                    sortOrder = line.sortOrder + 1,
                    expression = move,
                    result = ""
                )
                val newId = dao.moveAndInsertLine(fileId, line.id, newLine)

                // Recalculate affected lines starting from the split point
                val updatedAllLines = dao.getLinesForFileSync(fileId)
                val splitIndexInList = updatedAllLines.indexOfFirst { it.id == line.id }.coerceAtLeast(0)
                val effectiveRationalMode = rationalMode ?: _rationalMode.value
                val affectedLines = MathEngine.calculateFrom(updatedAllLines, splitIndexInList, createFileContextLoader(fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
                val versionedLines = affectedLines.map { it.copy(version = it.version + 1) }
                dao.updateLines(fileId, versionedLines, updateTimestamp = false)

                newId
            }
        }
    }

    /**
     * Merges [currentLineId] into [prevLineId].
     * The expression of [currentLineId] is appended to [prevLineId], and [currentLineId] is then deleted.
     */
    suspend fun mergeLines(
        prevLineId: Long,
        currentLineId: Long,
        rationalMode: Boolean? = null
    ) {
        withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                val prevLine = dao.getLineById(prevLineId) ?: return@withLock
                val currentLine = dao.getLineById(currentLineId) ?: return@withLock

                val fileId = prevLine.fileId
                saveStateForUndo(fileId)

                val mergedExpression = prevLine.expression + currentLine.expression
                // Update and clear result to prevent stale data display during calculation
                dao.updateLine(prevLine.copy(expression = mergedExpression, result = "", version = prevLine.version + 1))
                dao.deleteAndNormalize(currentLine)

                // Recalculate starting from the merged line
                val updatedLines = dao.getLinesForFileSync(fileId)
                val mergedIndex = updatedLines.indexOfFirst { it.id == prevLine.id }.coerceAtLeast(0)
                val effectiveRationalMode = rationalMode ?: _rationalMode.value
                val affectedLines = MathEngine.calculateFrom(updatedLines, mergedIndex, createFileContextLoader(fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
                dao.updateLines(fileId, affectedLines, updateTimestamp = false)
            }
        }
    }

    fun deleteLine(line: LineEntity, rationalMode: Boolean? = null) {
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
                    val effectiveRationalMode = rationalMode ?: _rationalMode.value
                    val affectedLines = MathEngine.calculateFrom(updatedLines, deletedIndex, createFileContextLoader(line.fileId, effectiveRationalMode), rationalMode = effectiveRationalMode)
                    val versionedLines = affectedLines.map { l -> l.copy(version = l.version + 1) }
                    dao.updateLines(line.fileId, versionedLines)
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

    private suspend fun performSyncAwareDelete(context: Context, fileId: Long): Boolean {
        val file = dao.getFileById(fileId) ?: return false

        if (SyncManager.isSyncActive(context)) {
            val deleteError: Throwable? = try {
                SyncManager.deleteExternalFile(context, file.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete external file before local delete", e)
                e
            }

            if (deleteError != null) {
                Log.e(TAG, "Aborting local delete because external delete failed: ${deleteError.message}")
                _uiEvents.emit(
                    HomeUiEvent.ShowMessage(
                        deleteError.message ?: "Failed to delete external file"
                    )
                )
                return false
            }
        }

        dao.deleteFile(file)
        undoStacks.remove(fileId)
        redoStacks.remove(fileId)
        updateUndoRedoState(fileId)
        _excludedFileIds.value = _excludedFileIds.value - fileId
        return true
    }

    suspend fun deleteFile(context: Context, fileId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            calculationMutex.withLock {
                performSyncAwareDelete(context, fileId)
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

    fun permanentDeleteExclusions(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            calculationMutex.withLock {
                val idsToDelete = _excludedFileIds.value
                if (idsToDelete.isEmpty()) return@withLock
                idsToDelete.forEach { fileId ->
                    performSyncAwareDelete(context, fileId)
                }
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

    suspend fun renameFile(context: Context, fileId: Long, newName: String): Boolean {
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
                if (SyncManager.isSyncActive(context)) {
                    // Delete old external file in sync folder to prevent re-import as foreign file
                    val deleteError = SyncManager.deleteExternalFile(context, file.name)
                    if (deleteError != null) {
                        val missingExternalFile = deleteError.message?.startsWith("External file not found:") == true
                        if (!missingExternalFile) throw deleteError
                    }
                }
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

    private var restoreJob: kotlinx.coroutines.Job? = null

    fun cancelRestore() {
        restoreJob?.cancel()
        restoreJob = null
        _restoreProgress.value = RestoreProgressState() // Reset state to hide dialog
    }

    fun importFiles(context: Context, inputUri: Uri) {
        executeRestoreOrImport("Imported", operation = {
            BackupManager.importFiles(
                context = context,
                dao = dao,
                inputUri = inputUri,
                onProgress = { current, total, fileName ->
                    _restoreProgress.value = _restoreProgress.value.copy(
                        current = current,
                        total = total,
                        currentFile = fileName
                    )
                },
                onConflict = conflict@ { fileName, localModified, zipModified ->
                    val state = _restoreProgress.value
                    if (state.applyToAll != null) return@conflict state.applyToAll

                    _restoreProgress.value = _restoreProgress.value.copy(
                        conflictFile = fileName,
                        localConflictModified = localModified,
                        zipConflictModified = zipModified
                    )
                    val deferred = kotlinx.coroutines.CompletableDeferred<ConflictResolution>()
                    conflictDeferred = deferred
                    val res = deferred.await()
                    conflictDeferred = null
                    _restoreProgress.value = _restoreProgress.value.copy(conflictFile = null)
                    res
                }
            )
        })
    }

    suspend fun copyFileToClipboard(context: Context, fileId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val lines = dao.getLinesForFileSync(fileId)
                val content = FileUtils.formatFileContent(lines, precision.value)
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

    private fun executeRestoreOrImport(
        verb: String,
        operation: suspend () -> Result<RestoreResult>,
        onSuccess: () -> Unit = {}
    ) {
        _restoreProgress.value = RestoreProgressState(isProcessing = true)
        restoreJob?.cancel()
        restoreJob = viewModelScope.launch {
            val result = operation()
            _restoreProgress.value = _restoreProgress.value.copy(
                isProcessing = false,
                isSuccess = result.isSuccess,
                completionMessage = if (result.isSuccess) {
                    val count = result.getOrNull()?.processedCount ?: 0
                    if (count == 1) "$verb 1 file" else "$verb $count files"
                } else {
                    result.exceptionOrNull()?.message ?: "An unknown error occurred"
                },
                overwrittenCount = result.getOrNull()?.overwrittenCount ?: 0,
                processedCount = result.getOrNull()?.processedCount ?: 0,
                addedCount = result.getOrNull()?.addedCount ?: 0,
                skippedCount = result.getOrNull()?.skippedCount ?: 0
            )
            if (result.isSuccess) {
                onSuccess()
            }
            restoreJob = null
        }
    }
}
