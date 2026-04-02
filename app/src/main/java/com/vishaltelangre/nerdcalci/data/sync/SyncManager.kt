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
import kotlin.math.abs

private const val TAG = "SyncManager"
private const val SYNC_TIMESTAMP_TOLERANCE_MS = 2000L
private const val SAF_METADATA_RETRY_DELAY_MS = 150L
private const val SAF_METADATA_MAX_RETRIES = 5
private const val MAX_CONFLICT_FILENAME_ATTEMPTS = 100
private const val MAX_REMOTE_HASH_FILE_SIZE_BYTES = 5L * 1024L * 1024L // 5MB

object SyncManager {
    const val PREFS_NAME = "nerdcalci_prefs"
    const val PREF_SYNC_ENABLED = "sync_enabled"
    const val PREF_SYNC_FOLDER_URI = "sync_folder_uri"
    const val PREF_LAST_SYNC_AT = "last_sync_at"
    const val PREF_LAST_SYNC_FILES = "last_sync_files"

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSyncActive(context: Context): Boolean {
        val prefs = prefs(context)
        return prefs.safeGetBoolean(PREF_SYNC_ENABLED, false) &&
            !prefs.getString(PREF_SYNC_FOLDER_URI, null).isNullOrBlank()
    }

    private fun SharedPreferences.safeGetString(key: String, defaultValue: String? = null): String? {
        val rawValue = getAll()[key]
        return when (rawValue) {
            null -> defaultValue
            is String -> rawValue
            is Set<*> -> {
                val stringValues = rawValue.filterIsInstance<String>()
                when {
                    stringValues.isEmpty() -> {
                        Log.w(TAG, "Ignoring malformed preference type for $key")
                        defaultValue
                    }
                    stringValues.size == 1 -> {
                        Log.w(TAG, "Recovered legacy string set preference for $key")
                        stringValues.first()
                    }
                    else -> {
                        Log.w(TAG, "Ignoring malformed preference type for $key")
                        defaultValue
                    }
                }
            }
            else -> {
                Log.w(TAG, "Ignoring malformed preference type for $key")
                defaultValue
            }
        }
    }

    private fun SharedPreferences.safeGetBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            getBoolean(key, defaultValue)
        } catch (e: ClassCastException) {
            Log.w(TAG, "Ignoring malformed preference type for $key", e)
            defaultValue
        }
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
            if (!isSyncActive(context)) return@withContext Result.success("Sync disabled")

            val folderUriString = prefs.getString(PREF_SYNC_FOLDER_URI, null)
                ?: return@withContext Result.failure(Exception("Sync folder not set"))

            val treeUri = Uri.parse(folderUriString)
            val folder = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(Exception("Sync folder unavailable"))

            val lastSyncMap = decodeLastSyncMap(prefs.safeGetString(PREF_LAST_SYNC_FILES, null))
            val isFirstSync = lastSyncMap.isEmpty()
            val currentSyncSnapshot = mutableMapOf<String, LastSyncInfo>()
            val stats = SyncStats()

            // 1. Gather Room Files and ensure they have a syncId
            val roomFilesSnapshot = dao.getAllFiles().first()
            val filesToUpdate = roomFilesSnapshot.mapNotNull { file ->
                if (file.syncId.isEmpty()) file.copy(syncId = UUID.randomUUID().toString()) else null
            }
            if (filesToUpdate.isNotEmpty()) {
                dao.updateFilesFromSync(filesToUpdate)
            }
            val roomFiles = if (filesToUpdate.isEmpty()) {
                roomFilesSnapshot
            } else {
                val updatedById = filesToUpdate.associateBy { it.id }
                roomFilesSnapshot.map { file -> updatedById[file.id] ?: file }
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
                    if (cachedEntry != null && abs(cachedEntry.value.osTimestamp - osTime) < SYNC_TIMESTAMP_TOLERANCE_MS) {
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

            val safFilesById = safFiles.groupBy { it.syncId }.mapValues { (syncId, files) ->
                if (files.size > 1) {
                    val roomFile = roomFilesById[syncId]
                    val lastInfo = lastSyncMap[syncId]
                    val expectedLocalName = if (roomFile != null) "${roomFile.name}${EXPORT_FILE_EXTENSION}" else null
                    val lastKnownName = lastInfo?.filename

                    // Prioritize name match to prevent "RENAME DETECTED" loops caused by SAF suffixes
                    val bestMatch = files.find { it.name == expectedLocalName }
                        ?: files.find { it.name == lastKnownName }
                        ?: files.maxByOrNull { if (it.lastModified > 0) it.lastModified else it.osLastModified }!!

                    Log.w(TAG, "Duplicate syncId $syncId in SAF. Picking ${bestMatch.name} (Priority Match). Others: ${files.filter { it != bestMatch }.map { it.name }}")
                    bestMatch
                } else {
                    files.first()
                }
            }

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
        Log.d(TAG, "Sync state for $filename: localTime=$localTime, lastMeta=$lastMetadataTimestamp, remoteTime=$remoteTime, lastSync=$lastSyncTimestamp, localChanged=$localChanged, remoteChanged=$remoteChanged")

        // Detect Rename
        val lastFilename = lastInfo?.filename ?: filename
        val localNameWithExt = roomFile.name + EXPORT_FILE_EXTENSION
        var currentFilenameForSnapshot = filename

        if (localNameWithExt != filename) {
            val localRenamed = localNameWithExt != lastFilename
            val remoteRenamed = filename != lastFilename

            if (remoteRenamed && !localRenamed) {
                Log.d(TAG, "RENAME DETECTED (Remote win): Local ${roomFile.name} -> Remote $filename")
                dao.renameFileFromSync(roomFile.id, filename.removeSuffix(EXPORT_FILE_EXTENSION))
                // Note: roomFile.name is still the old one, but we use filename for snapshot below
            } else if (localRenamed && !remoteRenamed) {
                Log.d(TAG, "RENAME DETECTED (Local win): Remote $filename -> $localNameWithExt")
                if (safFile.documentFile.renameTo(localNameWithExt)) {
                    currentFilenameForSnapshot = localNameWithExt
                } else {
                    Log.w(TAG, "Remote rename failed for $filename -> $localNameWithExt. Will attempt overwrite via writeToSaf.")
                }
            } else if (localRenamed && remoteRenamed) {
                Log.d(TAG, "RENAME CONFLICT: Local ${roomFile.name} vs Remote $filename. Remote name wins.")
                dao.renameFileFromSync(roomFile.id, filename.removeSuffix(EXPORT_FILE_EXTENSION))
            }
        }

        if (localChanged || remoteChanged || roomFile.isPinned != safFile.isPinned) {
            val precision = prefs(context).getInt(Constants.SYNC_ENGINE_PRECISION, Constants.DEFAULT_PRECISION)
            val lines = dao.getLinesForFileSync(roomFile.id)
            val body = FileUtils.formatFileBody(lines, precision)
            val computedHash = FileUtils.calculateHash(body)
            val remoteHash = safFile.contentHash ?: run {
                val fileSize = safFile.documentFile.length()
                if (fileSize <= 0L || fileSize > MAX_REMOTE_HASH_FILE_SIZE_BYTES) {
                    Log.w(TAG, "Skipping remote hash for $filename due to size=$fileSize")
                    ""
                } else {
                    context.contentResolver.openInputStream(safFile.documentFile.uri)?.use { input ->
                        FileUtils.calculateHash(input)
                    } ?: ""
                }
            }
            if (computedHash == remoteHash && roomFile.isPinned == safFile.isPinned) {
                Log.d(TAG, "DECISION: LOCAL_METADATA_ONLY_UPDATE for $currentFilenameForSnapshot (hashes and pin match)")
                currentSyncSnapshot[syncId] = LastSyncInfo(currentFilenameForSnapshot, safFile.osLastModified, safFile.lastModified, roomFile.isPinned, computedHash)
                return
            }

            if (localChanged && remoteChanged) {
                val conflictName = createConflictFilename(roomFile.name, dao)
                Log.d(TAG, "CONFLICT: $currentFilenameForSnapshot. Duplicating local version to $conflictName.")
                stats.conflicts++

                // 1. Preserve local version as a separate conflict copy with a NEW identity
                val conflictSyncId = java.util.UUID.randomUUID().toString()
                dao.duplicateFile(roomFile.id, conflictName, conflictSyncId, roomFile.lastModified)

                // 2. Overwrite the original local file with the remote SAF entry
                val result = importFromSaf(context, safFile.documentFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(currentFilenameForSnapshot, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
                stats.inbound++
            } else if (localChanged || roomFile.isPinned != safFile.isPinned) {
                Log.d(TAG, "DECISION: LOCAL_NEWER for $currentFilenameForSnapshot")
                val result = writeToSaf(context, folder, roomFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(currentFilenameForSnapshot, result.osTimestamp, roomFile.lastModified, roomFile.isPinned, result.contentHash)
                stats.outbound++
            } else {
                Log.d(TAG, "DECISION: REMOTE_NEWER for $currentFilenameForSnapshot")
                val result = importFromSaf(context, safFile.documentFile, dao)
                currentSyncSnapshot[syncId] = LastSyncInfo(currentFilenameForSnapshot, result.osTimestamp, result.metadataTimestamp, result.isPinned, result.contentHash)
                stats.inbound++
            }
        } else {
            currentSyncSnapshot[syncId] = LastSyncInfo(currentFilenameForSnapshot, safFile.osLastModified, safFile.lastModified, safFile.isPinned, safFile.contentHash)
        }
    }

    private suspend fun createConflictFilename(baseName: String, dao: CalculatorDao): String {
        val existingNames = dao.getAllFilesSync().map { it.name }.toHashSet()
        var suffix = 0

        while (suffix < MAX_CONFLICT_FILENAME_ATTEMPTS) {
            val conflictName = if (suffix == 0) {
                "$baseName (conflict copy)"
            } else {
                "$baseName (conflict copy ${suffix + 1})"
            }

            if (conflictName !in existingNames && dao.getFileByName(conflictName) == null) {
                return conflictName
            }

            suffix++
        }
        throw Exception("Could not generate unique conflict filename after $MAX_CONFLICT_FILENAME_ATTEMPTS attempts")
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
        val syncId = safFile.syncId
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

        val lines = dao.getLinesForFileSync(file.id)
        val body = FileUtils.formatCanonicalFileBody(lines)
        val contentHash = FileUtils.calculateHash(body)
        val content = FileUtils.formatCanonicalFileContent(
            lines = lines,
            metadata = FileMetadata(
                version = 1,
                id = file.syncId,
                isPinned = file.isPinned,
                lastModified = file.lastModified,
                createdAt = file.createdAt,
                contentHash = contentHash
            )
        )

        val originalFile = folder.findFile(fileNameWithExtension)
        if (originalFile != null) {
            // Pre-delete any stale .tmp to avoid leaving clutter
            folder.findFile(tempFileName)?.delete()

            // Overwrite existing file directly. 
            // This is the ONLY way to be sure SAF doesn't create a suffix duplicate.
            context.contentResolver.openOutputStream(originalFile.uri, "wt")?.use { output ->
                output.write(content.toByteArray())
            }
        } else {
            // New file: use temp file + rename
            folder.findFile(tempFileName)?.delete()
            val tempFile = folder.createFile("application/octet-stream", tempFileName)
                ?: throw Exception("Could not create temp file $tempFileName")
            
            try {
                context.contentResolver.openOutputStream(tempFile.uri)?.use { output ->
                    output.write(content.toByteArray())
                }
                if (!tempFile.renameTo(fileNameWithExtension)) {
                    // Fallback if rename failed (though originalFile was null above)
                    Log.w(TAG, "Rename failed even though originalFile was null. Deleting temp.")
                    tempFile.delete()
                    throw Exception("Failed to rename $tempFileName to $fileNameWithExtension")
                }
            } catch (e: Exception) {
                tempFile.delete()
                throw e
            }
        }

        val expectedMetadataTimestamp = file.lastModified
        var finalFile = folder.findFile(fileNameWithExtension) ?: throw Exception("Could not find file after write")
        for (attempt in 0 until SAF_METADATA_MAX_RETRIES) {
            if (finalFile.lastModified() == expectedMetadataTimestamp) break
            if (attempt < SAF_METADATA_MAX_RETRIES - 1) {
                // Allow OS to flush metadata. Some SAF providers (especially network/WebDAV)
                // may not immediately reflect the updated lastModified() after write.
                delay(SAF_METADATA_RETRY_DELAY_MS)
                finalFile = folder.findFile(fileNameWithExtension) ?: finalFile
            }
        }

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
                dao.renameFileFromSync(existingFile.id, conflictName)
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

    suspend fun deleteExternalFile(context: Context, fileName: String): Throwable? {
        return withContext(Dispatchers.IO) {
            try {
                if (!isSyncActive(context)) {
                    return@withContext IllegalStateException("Sync is disabled")
                }
                val prefs = prefs(context)
                val folderUri = prefs.getString(PREF_SYNC_FOLDER_URI, null)
                    ?: return@withContext IllegalStateException("Sync folder not set")
                val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri))
                    ?: return@withContext IllegalStateException("Sync folder unavailable")

                val fileNameWithExtension = "$fileName$EXPORT_FILE_EXTENSION"
                val safFile = folder.findFile(fileNameWithExtension)
                    ?: return@withContext IllegalStateException("External file not found: $fileNameWithExtension")

                // Get syncId from file before deleting it so we can clean up snapshot
                val syncId = context.contentResolver.openInputStream(safFile.uri)?.use {
                    FileUtils.readMetadataHeader(it.bufferedReader())?.id
                }

                val deleted = safFile.delete()
                if (!deleted) {
                    Log.e(TAG, "Failed to delete external file: $fileNameWithExtension")
                    return@withContext IllegalStateException("Failed to delete external file: $fileNameWithExtension")
                }

                if (syncId != null) {
                    val encodedMap = prefs.safeGetString(PREF_LAST_SYNC_FILES, null)
                    val syncMap = decodeLastSyncMap(encodedMap).toMutableMap()
                    if (syncMap.remove(syncId) != null) {
                        prefs.edit()
                            .putString(PREF_LAST_SYNC_FILES, encodeLastSyncMap(syncMap))
                            .apply()
                    }
                }
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete external file", e)
                e
            }
        }
    }
}
