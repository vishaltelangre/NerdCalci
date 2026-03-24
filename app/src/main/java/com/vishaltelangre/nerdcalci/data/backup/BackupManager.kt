package com.vishaltelangre.nerdcalci.data.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.core.FileContextLoader
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.FilenameUtils
import com.vishaltelangre.nerdcalci.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.nio.file.attribute.FileTime
import android.os.Build

private const val TAG = "BackupManager"

enum class BackupFrequency(val prefValue: String, val intervalDays: Long) {
    DAILY("daily", 1L),
    WEEKLY("weekly", 7L);

    companion object {
        fun fromPrefValue(value: String?): BackupFrequency = entries.firstOrNull {
            it.prefValue == value
        } ?: DAILY
    }
}

enum class BackupLocationMode(val prefValue: String) {
    APP_STORAGE("app_storage"),
    CUSTOM_FOLDER("custom_folder");

    companion object {
        fun fromPrefValue(value: String?): BackupLocationMode = entries.firstOrNull {
            it.prefValue == value
        } ?: APP_STORAGE
    }
}

enum class BackupSource {
    APP_STORAGE,
    CUSTOM_FOLDER
}

data class BackupSettings(
    val enabled: Boolean,
    val frequency: BackupFrequency,
    val locationMode: BackupLocationMode,
    val customFolderUri: String?,
    val keepLatestCount: Int
)

enum class ConflictResolution {
    REPLACE_WITH_FILE_FROM_ZIP,
    KEEP_LOCAL_FILE,
    KEEP_BOTH_FILES
}

data class RestoreResult(
    val processedCount: Int,
    val overwrittenCount: Int,
    val skippedCount: Int,
    val addedCount: Int
)

data class BackupFileInfo(
    val id: String,
    val displayName: String,
    val lastModified: Long,
    val source: BackupSource,
    val pathOrUri: String
)

object BackupManager {
    const val PREFS_NAME = "nerdcalci_prefs"
    const val PREF_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val PREF_AUTO_BACKUP_FREQUENCY = "auto_backup_frequency"
    const val PREF_AUTO_BACKUP_LOCATION_MODE = "auto_backup_location_mode"
    const val PREF_AUTO_BACKUP_CUSTOM_FOLDER_URI = "auto_backup_custom_folder_uri"
    const val PREF_AUTO_BACKUP_KEEP_COUNT = "auto_backup_keep_count"
    const val PREF_LAST_BACKUP_AT = "last_backup_at"

    private const val BACKUP_DIR_NAME = "backups"
    private const val BACKUP_FILE_PREFIX = "nerdcalci_backup_"
    private const val BACKUP_FILE_SUFFIX = ".zip"

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun readSettings(prefs: SharedPreferences): BackupSettings {
        val keepCount = prefs.getInt(PREF_AUTO_BACKUP_KEEP_COUNT, Constants.DEFAULT_BACKUP_KEEP_COUNT).coerceAtLeast(1)
        return BackupSettings(
            enabled = prefs.getBoolean(PREF_AUTO_BACKUP_ENABLED, true),
            frequency = BackupFrequency.fromPrefValue(prefs.getString(PREF_AUTO_BACKUP_FREQUENCY, BackupFrequency.DAILY.prefValue)),
            locationMode = BackupLocationMode.fromPrefValue(
                prefs.getString(PREF_AUTO_BACKUP_LOCATION_MODE, BackupLocationMode.APP_STORAGE.prefValue)
            ),
            customFolderUri = prefs.getString(PREF_AUTO_BACKUP_CUSTOM_FOLDER_URI, null),
            keepLatestCount = keepCount
        )
    }

    suspend fun exportAllFiles(context: Context, dao: CalculatorDao, outputUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val exportedCount = context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    writeBackupZip(context, dao, outputStream)
                } ?: return@withContext Result.failure(Exception("Could not open output stream"))

                if (exportedCount == 0) {
                    return@withContext Result.failure(Exception("No files to export"))
                }

                Result.success("Exported $exportedCount file(s)")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun importFiles(
        context: Context,
        dao: CalculatorDao,
        inputUri: Uri,
        onProgress: suspend (current: Int, total: Int, fileName: String) -> Unit,
        onConflict: suspend (fileName: String, localModified: Long, zipModified: Long) -> ConflictResolution
    ): Result<RestoreResult> {
        return withContext(Dispatchers.IO) {
            try {
                val stats = importFromZip(dao, {
                    context.contentResolver.openInputStream(inputUri)
                        ?: throw Exception("Could not open input stream")
                }, onProgress, onConflict)

                Log.d(TAG, "Imported ${stats.processedCount} file(s), ${stats.overwrittenCount} overwritten from ${inputUri.lastPathSegment}")
                Result.success(stats)
            } catch (e: Exception) {
                Log.e(TAG, "Import failed from ${inputUri.lastPathSegment}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun backupNow(context: Context, dao: CalculatorDao): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val settings = readSettings(prefs(context))
                val preferredCustom = settings.locationMode == BackupLocationMode.CUSTOM_FOLDER && !settings.customFolderUri.isNullOrBlank()
                val result: Result<String> = if (preferredCustom) {
                    Log.d(TAG, "Starting backup to custom folder")
                    val customResult = writeToCustomFolder(context, dao, settings)
                    if (customResult.isSuccess) {
                        customResult
                    } else {
                        Log.w(TAG, "Custom folder backup failed, falling back to app storage")
                        val fallbackResult = writeToAppStorage(context, dao, settings.keepLatestCount)
                        if (fallbackResult.isSuccess) {
                            Result.success("Custom folder unavailable. Saved backup in app storage instead.")
                        } else {
                            Log.e(TAG, "Fallback to app storage also failed")
                            fallbackResult
                        }
                    }
                } else {
                    Log.d(TAG, "Starting backup to app storage")
                    writeToAppStorage(context, dao, settings.keepLatestCount)
                }
                val message = result.getOrNull()
                if (result.isSuccess && message != "No files to back up") {
                    prefs(context).edit().putLong(PREF_LAST_BACKUP_AT, System.currentTimeMillis()).apply()
                }
                result
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun listBackups(context: Context): List<BackupFileInfo> {
        return withContext(Dispatchers.IO) {
            val settings = readSettings(prefs(context))
            val backups = when (settings.locationMode) {
                BackupLocationMode.APP_STORAGE -> listAppStorageBackups(context)
                BackupLocationMode.CUSTOM_FOLDER -> {
                    if (settings.customFolderUri.isNullOrBlank()) {
                        emptyList()
                    } else {
                        listCustomFolderBackups(context, settings.customFolderUri)
                    }
                }
            }
            backups.sortedByDescending { it.lastModified }
        }
    }

    suspend fun restoreFromBackup(
        context: Context,
        dao: CalculatorDao,
        backup: BackupFileInfo,
        onProgress: suspend (current: Int, total: Int, fileName: String) -> Unit,
        onConflict: suspend (fileName: String, localModified: Long, zipModified: Long) -> ConflictResolution
    ): Result<RestoreResult> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Restoring from backup: ${backup.displayName}")
                val stats = when (backup.source) {
                    BackupSource.APP_STORAGE -> {
                        importFromZip(dao, { FileInputStream(File(backup.pathOrUri)) }, onProgress, onConflict)
                    }

                    BackupSource.CUSTOM_FOLDER -> {
                        val uri = Uri.parse(backup.pathOrUri)
                        importFromZip(dao, {
                            context.contentResolver.openInputStream(uri)
                                ?: throw Exception("Could not open backup file")
                        }, onProgress, onConflict)
                    }
                }
                Log.d(TAG, "Successfully restored ${stats.processedCount} file(s), ${stats.overwrittenCount} overwritten from ${backup.displayName}")
                Result.success(stats)
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed from ${backup.displayName}", e)
                Result.failure(e)
            }
        }
    }

    private fun getBackupDirectory(context: Context): File {
        val directory = File(context.filesDir, BACKUP_DIR_NAME)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    private suspend fun writeToAppStorage(context: Context, dao: CalculatorDao, keepLatestCount: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val outputFile = File(getBackupDirectory(context), generateBackupFileName())
                val exportedCount = FileOutputStream(outputFile).use { outputStream ->
                    writeBackupZip(context, dao, outputStream)
                }
                if (exportedCount == 0) {
                    outputFile.delete()
                    return@withContext Result.success("No files to back up")
                }

                enforceAppStorageRetention(context, keepLatestCount)
                Result.success("Backed up $exportedCount file(s)")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun writeToCustomFolder(context: Context, dao: CalculatorDao, settings: BackupSettings): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val folderUri = settings.customFolderUri ?: return@withContext Result.failure(Exception("Custom folder not set"))
                val treeUri = Uri.parse(folderUri)
                val folder = DocumentFile.fromTreeUri(context, treeUri)
                    ?: return@withContext Result.failure(Exception("Custom folder unavailable"))

                val backupFile = folder.createFile(Constants.EXPORT_MIME_TYPE, generateBackupFileName())
                    ?: return@withContext Result.failure(Exception("Could not create backup file"))

                val exportedCount = context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                    writeBackupZip(context, dao, output)
                } ?: return@withContext Result.failure(Exception("Could not open backup output stream"))

                if (exportedCount == 0) {
                    backupFile.delete()
                    return@withContext Result.success("No files to back up")
                }

                enforceCustomFolderRetention(context, folder, settings.keepLatestCount)
                Result.success("Backed up $exportedCount file(s)")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun listAppStorageBackups(context: Context): List<BackupFileInfo> {
        val files = getBackupDirectory(context)
            .listFiles { file -> file.isFile && file.name.endsWith(BACKUP_FILE_SUFFIX) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        return files.map { file ->
            BackupFileInfo(
                id = "app:${file.absolutePath}",
                displayName = file.name,
                lastModified = file.lastModified(),
                source = BackupSource.APP_STORAGE,
                pathOrUri = file.absolutePath
            )
        }
    }

    private fun listCustomFolderBackups(context: Context, folderUri: String): List<BackupFileInfo> {
        return try {
            val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri)) ?: return emptyList()
            folder.listFiles()
                .asSequence()
                .filter { it.isFile && (it.name?.endsWith(BACKUP_FILE_SUFFIX) == true) }
                .sortedByDescending { it.lastModified() }
                .map { file ->
                    BackupFileInfo(
                        id = "custom:${file.uri}",
                        displayName = file.name ?: "backup.zip",
                        lastModified = file.lastModified(),
                        source = BackupSource.CUSTOM_FOLDER,
                        pathOrUri = file.uri.toString()
                    )
                }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun enforceAppStorageRetention(context: Context, keepLatestCount: Int) {
        val keepCount = keepLatestCount.coerceAtLeast(1)
        getBackupDirectory(context)
            .listFiles { file -> file.isFile && file.name.endsWith(BACKUP_FILE_SUFFIX) }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(keepCount)
            ?.forEach { it.delete() }
    }

    private fun enforceCustomFolderRetention(context: Context, folder: DocumentFile, keepLatestCount: Int) {
        val keepCount = keepLatestCount.coerceAtLeast(1)
        folder.listFiles()
            .asSequence()
            .filter { it.isFile && (it.name?.endsWith(BACKUP_FILE_SUFFIX) == true) }
            .sortedByDescending { it.lastModified() }
            .drop(keepCount)
            .forEach { file ->
                try {
                    context.contentResolver.delete(file.uri, null, null)
                } catch (_: Exception) {
                    file.delete()
                }
            }
    }

    private suspend fun writeBackupZip(context: Context, dao: CalculatorDao, outputStream: OutputStream): Int {
        val filesList = dao.getAllFiles().first()
        var exportedCount = 0

        ZipOutputStream(outputStream).use { zipOut ->
            val precision = prefs(context).getInt("precision", Constants.DEFAULT_PRECISION)
            filesList.forEach { file ->
                val lines = dao.getLinesForFileSync(file.id)
                val content = FileUtils.formatFileContent(lines, precision)

                val entry = ZipEntry("${file.name}${Constants.EXPORT_FILE_EXTENSION}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    entry.setLastModifiedTime(FileTime.fromMillis(file.lastModified))
                    entry.setCreationTime(FileTime.fromMillis(file.createdAt))
                } else {
                    entry.time = file.lastModified
                }
                zipOut.putNextEntry(entry)
                zipOut.write(content.toByteArray())
                zipOut.closeEntry()
                exportedCount++
            }
            zipOut.finish()
        }

        return exportedCount
    }

    internal suspend fun importFromZip(
        dao: CalculatorDao,
        streamSupplier: () -> InputStream,
        onProgress: suspend (current: Int, total: Int, fileName: String) -> Unit,
        onConflict: suspend (fileName: String, localModified: Long, zipModified: Long) -> ConflictResolution
    ): RestoreResult {
        val existingFiles = dao.getAllFiles().first()
        val existingNames = existingFiles.map { it.name }.toMutableSet()
        var overwrittenCount = 0
        var skippedCount = 0
        var addedCount = 0

        val totalEntries = withContext(Dispatchers.IO) {
            try {
                streamSupplier().use { stream ->
                    ZipInputStream(stream).use { zipIn ->
                        var count = 0
                        var e = zipIn.nextEntry
                        while (e != null) {
                            if (!e.isDirectory && e.name.endsWith(Constants.EXPORT_FILE_EXTENSION)) {
                                count++
                            }
                            zipIn.closeEntry()
                            e = zipIn.nextEntry
                        }
                        count
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to count ZIP entries, proceeding with unknown total", e)
                0
            }
        }

        withContext(Dispatchers.IO) {
            streamSupplier().use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    val cache = mutableMapOf<String, MathContext>()
                    val fileContextLoader = object : FileContextLoader {
                        override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
                            cache[fileName]?.let { return it }
                            val file = dao.getFileByName(fileName) ?: return null
                            val lines = dao.getLinesForFileSync(file.id)
                            val context = MathEngine.buildVariableState(lines, this, loadingStack)
                            cache[fileName] = context
                            return context
                        }
                    }
                    val insertedFiles = mutableListOf<com.vishaltelangre.nerdcalci.data.local.entities.FileEntity>()
                    try {
                        var entry: ZipEntry? = zipIn.nextEntry
                        var currentProgress = 0

                        while (entry != null) {
                            if (!kotlin.coroutines.coroutineContext.isActive) {
                                throw kotlinx.coroutines.CancellationException("Restore cancelled")
                            }

                            if (!entry.isDirectory && entry.name.endsWith(Constants.EXPORT_FILE_EXTENSION)) {
                                val fileName = entry.name.removeSuffix(Constants.EXPORT_FILE_EXTENSION)
                                var fileToDelete: FileEntity? = null
                                var currentInsertedFile: FileEntity? = null
                                currentProgress++
                                onProgress(currentProgress, totalEntries, fileName)

                            val content = BufferedReader(InputStreamReader(zipIn)).readText()

                            val expressions = content.lines()
                                .map { line ->
                                    val lastHashIndex = line.lastIndexOf('#')
                                    if (lastHashIndex > 0) {
                                        val exprCandidate = line.substring(0, lastHashIndex).trim()
                                        val potentialResult = line.substring(lastHashIndex + 1).trim()
                                        val isResult = potentialResult == "Err" || potentialResult.toDoubleOrNull() != null

                                         if (isResult && FileUtils.shouldShowResult(exprCandidate)) {
                                            exprCandidate
                                        } else {
                                            line.trim()
                                        }
                                    } else {
                                        line.trim()
                                    }
                                }

                            var isOverwrite = false
                            val finalFileName = if (existingNames.contains(fileName)) {
                                val existingFile = dao.getFileByName(fileName)
                                val localModified = existingFile?.lastModified ?: 0L
                                val zipModified = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    entry.lastModifiedTime?.toMillis() ?: entry.time
                                } else {
                                    entry.time
                                }
                                val decision = onConflict(fileName, localModified, zipModified)
                                if (decision == ConflictResolution.KEEP_LOCAL_FILE) {
                                    skippedCount++
                                    zipIn.closeEntry()
                                    entry = zipIn.nextEntry
                                    continue
                                }
                                if (decision == ConflictResolution.KEEP_BOTH_FILES) {
                                    addedCount++
                                    var suffixCount = 1
                                    var uniqueName = fileName
                                    while (existingNames.contains(uniqueName)) {
                                        uniqueName = "$fileName ($suffixCount)"
                                        suffixCount++
                                    }
                                    uniqueName
                                } else {
                                    if (existingFile != null) {
                                        fileToDelete = existingFile
                                        isOverwrite = true
                                        overwrittenCount++
                                    }
                                    "${fileName}_importing_${System.currentTimeMillis()}"
                                }
                            } else {
                                addedCount++
                                fileName
                            }

                            val modifiedTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                entry.lastModifiedTime?.toMillis() ?: entry.time
                            } else {
                                entry.time
                            }
                            val createTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                entry.creationTime?.toMillis() ?: modifiedTime
                            } else {
                                modifiedTime
                            }

                    // ZipEntry.time defaults to -1 if no time info is present.
                    // We only use System.currentTimeMillis() as a final fallback.
                            val finalModifiedTime = if (modifiedTime != -1L) modifiedTime else System.currentTimeMillis()
                            val finalCreateTime = if (createTime != -1L) createTime else finalModifiedTime

                            val fileId = dao.insertFile(
                                FileEntity(
                                    name = finalFileName,
                                    lastModified = finalModifiedTime,
                                    createdAt = finalCreateTime
                                )
                            )
                            currentInsertedFile = FileEntity(
                                id = fileId,
                                name = finalFileName,
                                lastModified = finalModifiedTime,
                                createdAt = finalCreateTime
                            )
                            insertedFiles.add(currentInsertedFile)
                            existingNames.add(finalFileName)

                            val lineEntities = expressions.mapIndexed { index, expr ->
                                LineEntity(
                                    fileId = fileId,
                                    sortOrder = index,
                                    expression = expr,
                                    result = ""
                                )
                            }
                            dao.insertLinesWithoutTouch(lineEntities)

                            val allLines = dao.getLinesForFileSync(fileId)
                            val calculatedLines = MathEngine.calculate(allLines, fileContextLoader)
                            dao.updateLines(fileId, calculatedLines)

                    // Final touch to ensure the timestamp is exactly as intended,
                    // even after updateLines might have moved it to "now".
                            dao.touchFile(fileId, finalModifiedTime)
                            
                            if (fileToDelete != null) {
                                dao.deleteFile(fileToDelete)
                                dao.renameFile(fileId, fileName)
                            }
                            
                            currentInsertedFile?.let { insertedFiles.remove(it) }
                        }

                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                } catch (e: Exception) {
                    insertedFiles.forEach { file ->
                        try { dao.deleteFile(file) } catch (_: Exception) {}
                    }
                    throw e
                }
            }
        }
    }

        return RestoreResult(addedCount + overwrittenCount + skippedCount, overwrittenCount, skippedCount, addedCount)
    }


    private fun generateBackupFileName(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        val timestamp = formatter.format(Date())
        return "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_SUFFIX"
    }
}
