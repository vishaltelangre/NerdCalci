package com.vishaltelangre.nerdcalci.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.Constants.EXPORT_FILE_EXTENSION
import com.vishaltelangre.nerdcalci.core.FileContextLoader
import com.vishaltelangre.nerdcalci.core.MathContext
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.FileUtils
import com.vishaltelangre.nerdcalci.utils.FileMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

private const val TAG = "SyncManager"
private const val SYNC_TIMESTAMP_TOLERANCE_MS = 2000L

object SyncManager {
    const val PREFS_NAME = "nerdcalci_prefs"
    const val PREF_SYNC_ENABLED = "sync_enabled"
    const val PREF_SYNC_FOLDER_URI = "sync_folder_uri"
    const val PREF_LAST_SYNC_AT = "last_sync_at"
    const val PREF_LAST_SYNC_FILES = "last_sync_files"

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private data class SyncFile(
        val name: String,
        val isFile: Boolean,
        val lastModified: Long, // from metadata
        val osLastModified: Long, // from file system
        val syncId: String,
        val isPinned: Boolean = false,
        val contentHash: String? = null,
        val documentFile: DocumentFile
    )

    private data class LastSyncInfo(
        val filename: String,
        val osTimestamp: Long, // OS file system time
        val metadataTimestamp: Long, // Internal "Last Modified" time
        val isPinned: Boolean = false,
        val contentHash: String? = null
    )

    private fun decodeLastSyncMap(encodedString: String?): Map<String, LastSyncInfo> {
        if (encodedString.isNullOrEmpty()) return emptyMap()
        val map = mutableMapOf<String, LastSyncInfo>()
        try {
            val json = JSONObject(encodedString)
            val keys = json.keys()
            while (keys.hasNext()) {
                val syncId = keys.next()
                val infoJson = json.getJSONObject(syncId)
                val info = LastSyncInfo(
                    infoJson.getString("filename"),
                    infoJson.getLong("osTimestamp"),
                    infoJson.optLong("metadataTimestamp", -1L),
                    infoJson.optBoolean("isPinned", false),
                    if (infoJson.has("contentHash")) infoJson.getString("contentHash") else null
                )
                map[syncId] = info
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode sync map", e)
        }
        return map
    }

    private fun encodeLastSyncMap(map: Map<String, LastSyncInfo>): String {
        val json = JSONObject()
        map.forEach { (syncId, info) ->
            val infoJson = JSONObject().apply {
                put("filename", info.filename)
                put("osTimestamp", info.osTimestamp)
                put("metadataTimestamp", info.metadataTimestamp)
                put("isPinned", info.isPinned)
                info.contentHash?.let { put("contentHash", it) }
            }
            json.put(syncId, infoJson)
        }
        return json.toString()
    }

    suspend fun performSync(context: Context, dao: CalculatorDao): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prefs = prefs(context)
            if (!prefs.getBoolean(PREF_SYNC_ENABLED, false)) return@withContext Result.success("Sync disabled")

            val folderUriString = prefs.getString(PREF_SYNC_FOLDER_URI, null)
                ?: return@withContext Result.failure(Exception("Sync folder not set"))

            val treeUri = Uri.parse(folderUriString)
            val folder = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(Exception("Sync folder unavailable"))

            val lastSyncMap = decodeLastSyncMap(prefs.getString(PREF_LAST_SYNC_FILES, null))
            val isFirstSync = lastSyncMap.isEmpty()
            val currentSyncSnapshot = mutableMapOf<String, LastSyncInfo>()
            val stats = SyncStats()

            // 1. Gather Room Files and ensure they have a syncId
            val roomFiles = dao.getAllFiles().first().map { file ->
                if (file.syncId.isEmpty()) {
                    val updated = file.copy(syncId = UUID.randomUUID().toString())
                    dao.updateFileFromSync(updated)
                    updated
                } else file
            }
            val roomFilesById = roomFiles.associateBy { it.syncId }

            // 2. Gather SAF Files and read metadata for IDs (Parallel)
            // Pre-index by filename for O(1) matching in the loop
            val snapshotByFilename = lastSyncMap.entries.associateBy { it.value.filename }

            val safFilesArray = folder.listFiles()?.filter {
                it.isFile && (it.name?.endsWith(EXPORT_FILE_EXTENSION) == true)
            } ?: emptyList()

            val safFiles = safFilesArray.map { file ->
                async<SyncFile> {
                    val name = file.name ?: "unnamed"
                    val osTime = file.lastModified()

                    // OPTIMIZATION: Check cache first to avoid expensive I/O
                    var syncId: String? = null
                    var contentHash: String? = null
                    var metadataLastModified: Long = -1L
                    var isPinned: Boolean = false

                    val cachedEntry = snapshotByFilename[name]
                    if (cachedEntry != null && Math.abs(cachedEntry.value.osTimestamp - osTime) < SYNC_TIMESTAMP_TOLERANCE_MS) {
                        syncId = cachedEntry.key
                        metadataLastModified = cachedEntry.value.metadataTimestamp
                        contentHash = cachedEntry.value.contentHash
                        isPinned = cachedEntry.value.isPinned
                    } else {
                        context.contentResolver.openInputStream(file.uri)?.use { inputStream: java.io.InputStream ->
                            val metadata = FileUtils.readMetadataHeader(inputStream.bufferedReader())
                            syncId = metadata?.id
                            metadataLastModified = metadata?.lastModified ?: -1L
                            contentHash = metadata?.contentHash
                            isPinned = metadata?.isPinned ?: false
                        }
                    }

                    val finalSyncId = syncId ?: java.util.UUID.randomUUID().toString()

                    SyncFile(
                        name = name,
                        isFile = true,
                        lastModified = metadataLastModified,
                        osLastModified = osTime,
                        syncId = finalSyncId,
                        isPinned = isPinned,
                        contentHash = contentHash,
                        documentFile = file
                    )
                }
            }.awaitAll()

            val safFilesById = safFiles.associateBy { it.syncId }

            // 3. Process Union of Sync IDs
            val allSyncIds = (roomFilesById.keys + safFilesById.keys + lastSyncMap.keys).toSet()

            allSyncIds.forEach { syncId ->
                val roomFile = roomFilesById[syncId]
                val safFile = safFilesById[syncId]
                val lastInfo = lastSyncMap[syncId]

                when {
                    roomFile != null && safFile != null -> {
                        handleFileInBoth(context, folder, roomFile, safFile, lastInfo, dao, currentSyncSnapshot, stats)
                    }
                    roomFile != null -> { // Local Only
                        handleLocalOnlyFile(context, folder, roomFile, lastInfo, isFirstSync, dao, currentSyncSnapshot, stats)
                    }
                    safFile != null -> { // Remote Only
                        handleRemoteOnlyFile(context, dao, safFile, lastInfo, isFirstSync, currentSyncSnapshot, stats)
                    }
                    // If it was in lastInfo but not now, it's fully gone (deleted both sides).
                }
            }

            // Update Sync Snapshot
            prefs.edit()
                .putLong(PREF_LAST_SYNC_AT, System.currentTimeMillis())
                .putString(PREF_LAST_SYNC_FILES, encodeLastSyncMap(currentSyncSnapshot))
                .apply()

            Log.d(TAG, "SNAPSHOT: $currentSyncSnapshot")
            Result.success(stats.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    private data class SyncStats(
        var inbound: Int = 0,
        var outbound: Int = 0,
        var conflicts: Int = 0
    ) {
        override fun toString(): String {
            val sb = StringBuilder("Synced: Inbound $inbound, Outbound $outbound")
            if (conflicts > 0) sb.append(", Conflicts $conflicts")
            if (inbound == 0 && outbound == 0 && conflicts == 0) return "Up to date"
            return sb.toString()
        }
    }

    private suspend fun handleFileInBoth(
        context: Context,
        folder: DocumentFile,
        roomFile: FileEntity,
        safFile: SyncFile,
        lastInfo: LastSyncInfo?,
        dao: CalculatorDao,
        currentSyncSnapshot: MutableMap<String, LastSyncInfo>,
        stats: SyncStats
    ) {
        val lastOsTimestamp = lastInfo?.osTimestamp ?: 0L
        val lastMetadataTimestamp = lastInfo?.metadataTimestamp ?: 0L

        // Use metadata timestamp primarily, fallback to OS time
        val remoteTime = if (safFile.lastModified > 0) safFile.lastModified else safFile.osLastModified
        val localTime = roomFile.lastModified

        // Compare against appropriate component of the snapshot
        val lastSyncTimestamp = if (safFile.lastModified > 0) lastMetadataTimestamp else lastOsTimestamp

        val localChanged = localTime > lastMetadataTimestamp + SYNC_TIMESTAMP_TOLERANCE_MS
        val remoteChanged = remoteTime > lastSyncTimestamp + SYNC_TIMESTAMP_TOLERANCE_MS

        val syncId = roomFile.syncId
        var filename = safFile.name

        // Detect Rename
        val lastFilename = lastInfo?.filename ?: filename
        val localNameWithExt = roomFile.name + EXPORT_FILE_EXTENSION
        if (localNameWithExt != filename) {
            val localRenamed = localNameWithExt != lastFilename
            val remoteRenamed = filename != lastFilename

            if (remoteRenamed && !localRenamed) {
                Log.d(TAG, "RENAME DETECTED (Remote win): Local ${roomFile.name} -> Remote $filename")
                dao.renameFile(roomFile.id, filename.removeSuffix(EXPORT_FILE_EXTENSION))
            } else if (localRenamed && !remoteRenamed) {
                Log.d(TAG, "RENAME DETECTED (Local win): Remote $filename -> $localNameWithExt")
                if (safFile.documentFile.renameTo(localNameWithExt)) {
                    filename = localNameWithExt
                } else {
                    Log.w(TAG, "Remote rename failed for $filename -> $localNameWithExt")
                }
            } else if (localRenamed && remoteRenamed) {
                Log.d(TAG, "RENAME CONFLICT: Local ${roomFile.name} vs Remote $filename. Remote name wins for simplicity.")
                dao.renameFile(roomFile.id, filename.removeSuffix(EXPORT_FILE_EXTENSION))
            }
        }

        if (localChanged || remoteChanged || roomFile.isPinned != safFile.isPinned) {
            val precision = prefs(context).getInt("precision", 2)
            val lines = dao.getLinesForFileSync(roomFile.id)
            val body = FileUtils.formatFileBody(lines, precision)
            val computedHash = FileUtils.calculateHash(body)
            val remoteHash = safFile.contentHash ?: context.contentResolver.openInputStream(safFile.documentFile.uri)?.use { input ->
                val text = BufferedReader(InputStreamReader(input)).readText()
                FileUtils.calculateHash(text)
            } ?: ""
            if (computedHash == remoteHash && roomFile.isPinned == safFile.isPinned) {
                Log.d(TAG, "DECISION: LOCAL_METADATA_ONLY_UPDATE for ${roomFile.name} (hashes and pin match)")
                currentSyncSnapshot[syncId] = LastSyncInfo(roomFile.name + EXPORT_FILE_EXTENSION, safFile.osLastModified, safFile.lastModified, roomFile.isPinned, computedHash)
                return
            }

            if (localChanged && remoteChanged) {
                val conflictName = "${roomFile.name} (conflict copy)"
                Log.d(TAG, "CONFLICT: $filename. Duplicating local version to $conflictName.")
                stats.conflicts++

                // 1. Preserve local version as a separate conflict copy with a NEW identity
                val conflictSyncId = java.util.UUID.randomUUID().toString()
                dao.duplicateFile(roomFile.id, conflictName, conflictSyncId, roomFile.lastModified)

                // 2. Overwrite the original local file with the remote SAF entry
                val result = importFromSaf(context, safFile.documentFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(filename, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
                stats.inbound++
            } else if (localChanged || roomFile.isPinned != safFile.isPinned) {
                Log.d(TAG, "DECISION: LOCAL_NEWER for $filename")
                val result = writeToSaf(context, folder, roomFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(filename, result.osTimestamp, roomFile.lastModified, roomFile.isPinned, result.contentHash)
                stats.outbound++
            } else {
                Log.d(TAG, "DECISION: REMOTE_NEWER for $filename")
                val result = importFromSaf(context, safFile.documentFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(filename, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
                stats.inbound++
            }
        } else {
            currentSyncSnapshot[syncId] = LastSyncInfo(filename, safFile.osLastModified, safFile.lastModified, safFile.isPinned, safFile.contentHash)
        }
    }

    private suspend fun handleLocalOnlyFile(
        context: Context,
        folder: DocumentFile,
        roomFile: FileEntity,
        lastInfo: LastSyncInfo?,
        isFirstSync: Boolean,
        dao: CalculatorDao,
        currentSyncSnapshot: MutableMap<String, LastSyncInfo>,
        stats: SyncStats
    ) {
        val syncId = roomFile.syncId
        val expectedName = roomFile.name + EXPORT_FILE_EXTENSION

        if (lastInfo != null && !isFirstSync) {
            // File existed before, now missing in SAF -> Remote Delete
            val localTime = roomFile.lastModified
            val lastSyncTime = lastInfo.metadataTimestamp

            if (localTime > lastSyncTime + SYNC_TIMESTAMP_TOLERANCE_MS || roomFile.isPinned != lastInfo.isPinned) {
                Log.d(TAG, "DECISION: RE-UPLOAD (Locally Modified) for $expectedName")
                val result = writeToSaf(context, folder, roomFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(expectedName, result.osTimestamp, roomFile.lastModified, roomFile.isPinned, result.contentHash)
                stats.outbound++
            } else {
                Log.d(TAG, "DECISION: DELETE_LOCAL for $expectedName")
                dao.deleteFile(roomFile)
                stats.inbound++
            }
        } else {
            // NEW local file or first sync merge -> Upload
            Log.d(TAG, "DECISION: UPLOAD_NEW_LOCAL for $expectedName")
            val result = writeToSaf(context, folder, roomFile, dao)
            currentSyncSnapshot[syncId] = LastSyncInfo(expectedName, result.osTimestamp, roomFile.lastModified, roomFile.isPinned, result.contentHash)
            stats.outbound++
        }
    }

    private suspend fun handleRemoteOnlyFile(
        context: Context,
        dao: CalculatorDao,
        safFile: SyncFile,
        lastInfo: LastSyncInfo?,
        isFirstSync: Boolean,
        currentSyncSnapshot: MutableMap<String, LastSyncInfo>,
        stats: SyncStats
    ) {
        val syncId = safFile.syncId ?: return
        val filename = safFile.name

        if (lastInfo != null && !isFirstSync) {
            // File existed before, now missing in Room -> Local Delete (from SAF)
            val remoteTime = if (safFile.lastModified > 0) safFile.lastModified else safFile.osLastModified
            val lastSyncTime = if (safFile.lastModified > 0) lastInfo.metadataTimestamp else lastInfo.osTimestamp

            if (remoteTime > lastSyncTime + SYNC_TIMESTAMP_TOLERANCE_MS || safFile.isPinned != lastInfo.isPinned) {
                Log.d(TAG, "DECISION: RE-DOWNLOAD (Remotely Modified) for $filename")
                val result = importFromSaf(context, safFile.documentFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(filename, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
                stats.inbound++
            } else {
                Log.d(TAG, "DECISION: DELETE_REMOTE for $filename")
                safFile.documentFile.delete()
                stats.outbound++
            }
        } else {
            // NEW remote file or first sync merge -> Download
            Log.d(TAG, "DECISION: DOWNLOAD_NEW_REMOTE for $filename")
            val result = importFromSaf(context, safFile.documentFile, dao)
            currentSyncSnapshot[syncId] = LastSyncInfo(filename, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
            stats.inbound++
        }
    }

    private data class SyncResult(
        val osTimestamp: Long,
        val metadataTimestamp: Long,
        val contentHash: String,
        val isPinned: Boolean
    )

    private suspend fun writeToSaf(context: Context, folder: DocumentFile, file: FileEntity, dao: CalculatorDao): SyncResult {
        val fileNameWithExtension = "${file.name}$EXPORT_FILE_EXTENSION"
        val tempFileName = "$fileNameWithExtension.tmp"

        // 1. Write to temp file
        var tempFile = folder.findFile(tempFileName) ?: folder.createFile("application/octet-stream", tempFileName)
            ?: throw Exception("Could not create temp file $tempFileName")

        val lines = dao.getLinesForFileSync(file.id)
        val precision = prefs(context).getInt("precision", 2)

        val body = FileUtils.formatFileBody(lines, precision)
        val contentHash = FileUtils.calculateHash(body)
        val content = FileUtils.formatFileContent(
            lines = lines,
            precision = precision,
            metadata = FileMetadata(
                version = 1,
                id = file.syncId,
                isPinned = file.isPinned,
                lastModified = file.lastModified,
                createdAt = file.createdAt,
                contentHash = contentHash
            )
        )

        context.contentResolver.openOutputStream(tempFile.uri)?.use { output ->
             output.write(content.toByteArray())
        }

        // 2. Atomic-like swap
        if (!tempFile.renameTo(fileNameWithExtension)) {
            Log.d(TAG, "Initial rename failed for $fileNameWithExtension (likely exists), checking for original file to delete")
            val originalFile = folder.findFile(fileNameWithExtension)
            originalFile?.delete()

            if (!tempFile.renameTo(fileNameWithExtension)) {
                Log.w(TAG, "Secondary rename failed for $fileNameWithExtension, fallback to fresh recreate")
                // Re-create if rename failed (some providers don't support it well)
                val safFile = folder.createFile("application/octet-stream", fileNameWithExtension)
                    ?: throw Exception("Could not recreate file $fileNameWithExtension")
                context.contentResolver.openOutputStream(safFile.uri)?.use { output ->
                     output.write(content.toByteArray())
                }
                tempFile.delete()
            }
        }

        // Allow OS to flush metadata. Some SAF providers (especially network/WebDAV)
        // may not immediately reflect the updated lastModified() after write.
        delay(150)

        val finalFile = folder.findFile(fileNameWithExtension) ?: tempFile
        return SyncResult(finalFile.lastModified(), file.lastModified, contentHash, file.isPinned)
    }

    private suspend fun importFromSaf(context: Context, safFile: DocumentFile, dao: CalculatorDao): SyncResult {
        val fullFileName = safFile.name ?: return SyncResult(0L, 0L, "", false)
        val fileName = fullFileName.removeSuffix(EXPORT_FILE_EXTENSION)

        val content = context.contentResolver.openInputStream(safFile.uri)?.use { input ->
             BufferedReader(InputStreamReader(input)).readText()
        } ?: return SyncResult(0L, 0L, "", false)

        val parsed = FileUtils.parseFileContent(content)
        val metadata = parsed.metadata
        val expressions = parsed.expressions

        val bodyToCheck = content.lines().filter { !it.trim().startsWith("# @metadata ") }.joinToString("\n")
        val computedHash = FileUtils.calculateHash(bodyToCheck)
        if (metadata.contentHash != null && metadata.contentHash != computedHash) {
            Log.w(TAG, "CORRUPTION DETECTED for $fullFileName (hash mismatch)")
        }

        val syncId = metadata.id ?: UUID.randomUUID().toString()

        // Fallback for older files that don't have metadata headers yet
        val finalLastModified = if (metadata.lastModified != -1L) metadata.lastModified
                else if (safFile.lastModified() > 0L) safFile.lastModified()
                else System.currentTimeMillis()

        // Sync updates or insertions by syncId
        val existingFile = dao.getFileBySyncId(syncId)
        val fileId = if (existingFile != null) {
            val updatedFile = existingFile.copy(
                name = fileName,
                isPinned = metadata.isPinned,
                lastModified = finalLastModified,
                createdAt = if (metadata.createdAt != -1L) metadata.createdAt else existingFile.createdAt
            )
            dao.updateFileFromSync(updatedFile)
            existingFile.id
        } else {
            // Check if name collision exists for a DIFFERENT syncId (should be rare)
            dao.getFileByName(fileName)?.let { existingFile ->
                val conflictName = "$fileName (local)"
                dao.renameFile(existingFile.id, conflictName)
                Log.w(TAG, "Name collision: Renamed existing local file to $conflictName")
            }

            // Create new
            val newFile = FileEntity(
                name = fileName,
                syncId = syncId,
                lastModified = finalLastModified,
                createdAt = metadata.createdAt.takeIf { it != -1L } ?: finalLastModified,
                isPinned = metadata.isPinned
            )
            dao.insertFile(newFile)
            dao.getFileBySyncId(syncId)?.id ?: throw Exception("Failed to retrieve inserted file")
        }

        val lineEntities = expressions.mapIndexed { index, expr ->
            LineEntity(fileId = fileId, sortOrder = index, expression = expr, result = "")
        }
        dao.restoreLines(fileId, lineEntities, false)

        val cache = mutableMapOf<String, MathContext>()
        val fileContextLoader = object : FileContextLoader {
            override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
                cache[fileName]?.let { return it }
                val f = dao.getFileByName(fileName) ?: return null
                val l = dao.getLinesForFileSync(f.id)
                val ctx = MathEngine.buildVariableState(l, this, loadingStack)
                cache[fileName] = ctx
                return ctx
            }
        }

        val allLines = dao.getLinesForFileSync(fileId)
        val calculatedLines = MathEngine.calculate(allLines, fileContextLoader)
        dao.updateLines(fileId, calculatedLines, false)
        val importedFileEntity = dao.getFileBySyncId(syncId) ?: throw Exception("Failed to retrieve imported file entity")

        // If the file was legacy (no ID in metadata), write it back to SAF immediately to persist the new ID
        if (metadata.id == null) {
            Log.d(TAG, "Legacy file $fullFileName imported, writing back UUID $syncId to remote")
            val parent = safFile.parentFile ?: throw Exception("Could not find parent for $fullFileName")
            return writeToSaf(context, parent, importedFileEntity, dao)
        }

        return SyncResult(safFile.lastModified(), parsed.metadata.lastModified, computedHash, parsed.metadata.isPinned)
    }

    suspend fun deleteExternalFile(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val prefs = prefs(context)
                val folderUri = prefs.getString(PREF_SYNC_FOLDER_URI, null) ?: return@withContext
                val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri)) ?: return@withContext

                val fileNameWithExtension = "$fileName$EXPORT_FILE_EXTENSION"
                val safFile = folder.findFile(fileNameWithExtension)

                // Get syncId from file before deleting it so we can clean up snapshot
                val syncId = context.contentResolver.openInputStream(safFile?.uri ?: return@withContext)?.use {
                    FileUtils.readMetadataHeader(it.bufferedReader())?.id
                }

                safFile.delete()

                if (syncId != null) {
                    val encodedMap = prefs.getString(PREF_LAST_SYNC_FILES, null)
                    val syncMap = decodeLastSyncMap(encodedMap).toMutableMap()
                    if (syncMap.remove(syncId) != null) {
                        prefs.edit()
                            .putString(PREF_LAST_SYNC_FILES, encodeLastSyncMap(syncMap))
                            .apply()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete external file", e)
            }
        }
    }
}
