package com.vishaltelangre.nerdcalci.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.FileContextLoader
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "SyncManager"

object SyncManager {
    const val PREFS_NAME = "nerdcalci_prefs"
    const val PREF_SYNC_ENABLED = "sync_enabled"
    const val PREF_SYNC_FOLDER_URI = "sync_folder_uri"
    const val PREF_LAST_SYNC_AT = "last_sync_at"

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun performSync(context: Context, dao: CalculatorDao): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = prefs(context)
                val enabled = prefs.getBoolean(PREF_SYNC_ENABLED, false)
                val folderUri = prefs.getString(PREF_SYNC_FOLDER_URI, null)

                if (!enabled || folderUri.isNullOrBlank()) {
                    return@withContext Result.success("Sync disabled")
                }

                val treeUri = Uri.parse(folderUri)
                val folder = DocumentFile.fromTreeUri(context, treeUri)
                    ?: return@withContext Result.failure(Exception("Sync folder unavailable"))

                val roomFiles = dao.getAllFiles().first()
                val safFiles = folder.listFiles()
                    .filter { it.isFile && (it.name?.endsWith(Constants.EXPORT_FILE_EXTENSION) == true) }
                    .associateBy { it.name ?: "" }

                var inboundCount = 0
                var outboundCount = 0

                val processedSafNames = mutableSetOf<String>()

                // outbound/Conflict resolution for Room files
                roomFiles.forEach { roomFile ->
                    val expectedName = "${roomFile.name}${Constants.EXPORT_FILE_EXTENSION}"
                    val safFile = safFiles[expectedName]

                    if (safFile != null) {
                        processedSafNames.add(expectedName)
                        val safModified = safFile.lastModified()
                        val roomModified = roomFile.lastModified

                        val timeDiff = roomModified - safModified

                        // Allow 2-second buffer for sync offsets
                        if (timeDiff > 2000L) {
                            // Room is newer -> Outbound sync
                            writeToSaf(context, folder, roomFile, dao)
                            outboundCount++
                        } else if (timeDiff < -2000L) {
                            // SAF is newer -> Inbound sync
                            importFromSaf(context, safFile, dao)
                            inboundCount++
                        }
                    } else {
                        // File not in SAF -> Outboundsync creation
                        writeToSaf(context, folder, roomFile, dao)
                        outboundCount++
                    }
                }

                // Inbound sync for files only in SAF
                safFiles.forEach { (name, safFile) ->
                    if (!processedSafNames.contains(name)) {
                        importFromSaf(context, safFile, dao)
                        inboundCount++
                    }
                }

                prefs.edit().putLong(PREF_LAST_SYNC_AT, System.currentTimeMillis()).apply()
                Result.success("Synced: Inbound $inboundCount, Outbound $outboundCount")
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun writeToSaf(context: Context, folder: DocumentFile, file: FileEntity, dao: CalculatorDao) {
        val fileNameWithExtension = "${file.name}${Constants.EXPORT_FILE_EXTENSION}"
        var safFile = folder.findFile(fileNameWithExtension)

        if (safFile == null) {
            safFile = folder.createFile("text/plain", fileNameWithExtension)
                ?: throw Exception("Could not create file $fileNameWithExtension")
        }

        val lines = dao.getLinesForFileSync(file.id)
        val precision = prefs(context).getInt("precision", Constants.DEFAULT_PRECISION)
        val content = FileUtils.formatFileContent(lines, precision)

        context.contentResolver.openOutputStream(safFile.uri)?.use { output ->
             output.write(content.toByteArray())
        }
        // Force timestamp match to avoid loop sync trigger
        // DocumentFile doesn't support setting lastModified directly, usually sets to "now"
        dao.touchFile(file.id, safFile.lastModified())
    }

    private suspend fun importFromSaf(context: Context, safFile: DocumentFile, dao: CalculatorDao) {
        val fullFileName = safFile.name ?: return
        val fileName = fullFileName.removeSuffix(Constants.EXPORT_FILE_EXTENSION)

        val content = context.contentResolver.openInputStream(safFile.uri)?.use { input ->
             BufferedReader(InputStreamReader(input)).readText()
        } ?: return

        val expressions = parseExpressions(content)
        val safModified = safFile.lastModified()

        // Sync updates or insertions
        val existingFile = dao.getFileByName(fileName)
        val fileId = if (existingFile != null) {
            // Update existing
            dao.restoreLines(existingFile.id, emptyList()) // clear lines or use proper replace
            existingFile.id
        } else {
            // Create new
            dao.insertFile(
                FileEntity(
                    name = fileName,
                    lastModified = safModified,
                    createdAt = safModified
                )
            )
        }

        val lineEntities = expressions.mapIndexed { index, expr ->
            LineEntity(fileId = fileId, sortOrder = index, expression = expr, result = "")
        }
        dao.restoreLines(fileId, lineEntities) // restoreLines also touches file internally

        // Recalculate
        val cache = mutableMapOf<String, MathContext>()
        val fileContextLoader = object : FileContextLoader {
            override suspend fun loadContext(name: String, loadingStack: Set<String>): MathContext? {
                cache[name]?.let { return it }
                val f = dao.getFileByName(name) ?: return null
                val l = dao.getLinesForFileSync(f.id)
                val context = MathEngine.buildVariableState(l, this, loadingStack)
                cache[name] = context
                return context
            }
        }

        val allLines = dao.getLinesForFileSync(fileId)
        val calculatedLines = MathEngine.calculate(allLines, fileContextLoader)
        dao.updateLines(fileId, calculatedLines)
        dao.touchFile(fileId, safModified) // Restore original SAF timestamp in DB
    }

    suspend fun deleteExternalFile(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val prefs = prefs(context)
                val folderUri = prefs.getString(PREF_SYNC_FOLDER_URI, null) ?: return@withContext
                val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri)) ?: return@withContext

                val fullFileName = "$fileName${Constants.EXPORT_FILE_EXTENSION}"
                val safFile = folder.findFile(fullFileName)
                safFile?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete external file $fileName", e)
            }
        }
    }

    private fun parseExpressions(content: String): List<String> {
        return content.lines().map { line ->
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
    }
}
