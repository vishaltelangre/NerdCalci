package com.vishaltelangre.nerdcalci.data.local

import androidx.room.*
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.FilenameUtils
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for calculator database operations.
 */
@Dao
abstract class CalculatorDao {
    // Returns files with pinned files first, then sorted by most recently modified
    @Query("SELECT * FROM files ORDER BY isPinned DESC, lastModified DESC")
    abstract fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY isPinned DESC, lastModified DESC")
    abstract suspend fun getAllFilesSync(): List<FileEntity>

    @Query("SELECT * FROM files WHERE id = :fileId")
    abstract suspend fun getFileById(fileId: Long): FileEntity?

    @Query("SELECT * FROM files WHERE name = :name")
    abstract suspend fun getFileByName(name: String): FileEntity?

    @Query("SELECT * FROM files WHERE syncId = :syncId")
    abstract suspend fun getFileBySyncId(syncId: String): FileEntity?

    @Query("SELECT COUNT(*) FROM files WHERE isPinned = 1")
    abstract suspend fun getPinnedFilesCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM files WHERE name = :name)")
    abstract suspend fun doesFileExist(name: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM files WHERE name = :name AND id != :excludeId)")
    abstract suspend fun doesFileExist(name: String, excludeId: Long): Boolean

    // Returns lines ordered by sortOrder (determines display order in UI)
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC, id ASC")
    abstract fun getLinesForFile(fileId: Long): Flow<List<LineEntity>>

    // Synchronous version for operations that need immediate results
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC, id ASC")
    abstract suspend fun getLinesForFileSync(fileId: Long): List<LineEntity>

    @Query("SELECT COUNT(*) FROM lines WHERE fileId = :fileId")
    abstract suspend fun getLineCountForFile(fileId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun internalInsertLines(lines: List<LineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun internalInsertLine(line: LineEntity): Long

    @Update
    protected abstract suspend fun internalUpdateLine(line: LineEntity)

    @Update
    protected abstract suspend fun internalUpdateLines(lines: List<LineEntity>)

    @Delete
    protected abstract suspend fun internalDeleteLine(line: LineEntity)

    @Update
    protected abstract suspend fun internalUpdateFile(file: FileEntity)

    @Delete
    abstract suspend fun deleteFile(file: FileEntity)

    @Query("UPDATE files SET lastModified = :timestamp WHERE id = :fileId")
    protected abstract suspend fun updateFileTimestamp(fileId: Long, timestamp: Long)

    @Transaction
    open suspend fun renameFile(fileId: Long, name: String) {
        internalRenameFile(fileId, name)
        touchFile(fileId)
    }

    @Query("UPDATE files SET name = :name WHERE id = :fileId")
    protected abstract suspend fun internalRenameFile(fileId: Long, name: String)

    open suspend fun touchFile(fileId: Long, timestamp: Long = System.currentTimeMillis()) {
        updateFileTimestamp(fileId, timestamp)
    }

    @Transaction
    open suspend fun insertLine(line: LineEntity): Long {
        val id = internalInsertLine(line)
        touchFile(line.fileId)
        return id
    }

    @Transaction
    open suspend fun insertLinesWithoutTouch(lines: List<LineEntity>) {
        internalInsertLines(lines)
    }

    @Transaction
    open suspend fun updateLine(line: LineEntity) {
        internalUpdateLine(line)
        touchFile(line.fileId)
    }

    @Transaction
    open suspend fun updateLines(fileId: Long, lines: List<LineEntity>, updateTimestamp: Boolean = true) {
        if (lines.isEmpty()) return
        internalUpdateLines(lines)
        if (updateTimestamp) {
            touchFile(fileId)
        }
    }

    @Transaction
    open suspend fun deleteLine(line: LineEntity) {
        internalDeleteLine(line)
        touchFile(line.fileId)
    }

    @Transaction
    open suspend fun updateFile(file: FileEntity) {
        internalUpdateFile(file.copy(lastModified = System.currentTimeMillis()))
    }

    @Transaction
    open suspend fun updateFileFromSync(file: FileEntity) {
        internalUpdateFile(file)
    }
    @Transaction
    open suspend fun duplicateFile(fileId: Long, newName: String, newSyncId: String, lastModified: Long? = null): Long {
        val originalFile = getFileById(fileId) ?: throw Exception("Original file not found")
        val newFile = originalFile.copy(
            id = 0L,
            name = newName,
            syncId = newSyncId,
            lastModified = lastModified ?: System.currentTimeMillis()
        )
        val newFileId = insertFile(newFile)
        val originalLines = getLinesForFileSync(fileId)
        val newLines = originalLines.map { it.copy(id = 0L, fileId = newFileId) }
        internalInsertLines(newLines)
        return newFileId
    }

    @Query("UPDATE files SET syncId = :newSyncId WHERE id = :fileId")
    abstract suspend fun updateSyncId(fileId: Long, newSyncId: String)

    @Transaction
    open suspend fun createNewFile(baseName: String, createdAt: Long): Long {
        val uniqueName = FilenameUtils.generateUniqueFileName(baseName) { name ->
            doesFileExist(name)
        }
        val fileId = insertFile(FileEntity(name = uniqueName, lastModified = createdAt, createdAt = createdAt))
        // Insert a default empty line
        internalInsertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "", result = ""))
        return fileId
    }

    @Transaction
    open suspend fun duplicateFile(sourceFileId: Long, createdAt: Long): Long? {
        val sourceFile = getFileById(sourceFileId) ?: return null
        val sourceLines = getLinesForFileSync(sourceFileId)

        val baseName = "Copy of ${sourceFile.name}"
        val uniqueName = FilenameUtils.generateUniqueFileName(baseName.take(Constants.MAX_FILE_NAME_LENGTH)) { name ->
            doesFileExist(name)
        }

        val newFileId = insertFile(
            FileEntity(
                name = uniqueName,
                lastModified = createdAt,
                createdAt = createdAt,
                isPinned = false
            )
        )

        val newLines = sourceLines.map { it.copy(id = 0, fileId = newFileId) }
        internalInsertLines(newLines)

        return newFileId
    }

    @Transaction
    open suspend fun clearAllLines(fileId: Long) {
        internalDeleteLinesForFile(fileId)
        // Create one empty line to start fresh
        internalInsertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "", result = ""))
        touchFile(fileId)
    }

    /**
     * Atomically replaces all lines in a file with the given list of lines.
     * Ensures all lines are correctly attributed to the file and IDs are reset for insertion.
     */
    @Transaction
    open suspend fun restoreLines(fileId: Long, lines: List<LineEntity>, updateTimestamp: Boolean = true) {
        internalDeleteLinesForFile(fileId)
        val toInsert = lines.mapIndexed { index, line ->
            line.copy(id = 0, fileId = fileId, sortOrder = index)
        }
        if (toInsert.isNotEmpty()) {
            internalInsertLines(toInsert)
        } else {
            // Ensure at least one empty line if the list was empty
            internalInsertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "", result = ""))
        }
        if (updateTimestamp) {
            touchFile(fileId)
        }
    }

    @Query("DELETE FROM lines WHERE fileId = :fileId")
    protected abstract suspend fun internalDeleteLinesForFile(fileId: Long)

    /**
     * Atomically shifts existing lines and inserts a new line.
     * Then re-normalizes all sortOrder values to 0, 1, 2... to ensure consistency.
     */
    @Transaction
    open suspend fun moveAndInsertLine(fileId: Long, afterLineId: Long?, newLine: LineEntity): Long {
        val currentLines = getLinesForFileSync(fileId).toMutableList()

        // Use the ID of the line the user is on to find exactly where to insert.
        // If we don't have an ID (null), we're at the very top.
        val insertIndex = if (afterLineId == null) {
            0
        } else {
            val idx = currentLines.indexOfFirst { it.id == afterLineId }
            // If we found the line, we go just below it (idx + 1).
            // If not found, we fallback to the very end.
            if (idx == -1) currentLines.size else idx + 1
        }

        // "Slip" the new line into our list at that specific spot.
        currentLines.add(insertIndex, newLine)

        // Since we've inserted a line, all lines below it need their sortOrder updated.
        // We re-assign them 0, 1, 2, 3... so they are sequential and clean.
        val normalizedLines = currentLines.mapIndexed { index, line ->
            line.copy(sortOrder = index)
        }

        // Save everything back to the database in one go.
        // We separate existing lines (which just need a number update) from the brand new line.
        val existingToUpdate = normalizedLines.filter { it.id != 0L }
        val lineToInsert = normalizedLines.first { it.id == 0L }

        if (existingToUpdate.isNotEmpty()) internalUpdateLines(existingToUpdate)

        val newId = internalInsertLine(lineToInsert)

        touchFile(fileId)

        return newId
    }

    /**
     * Atomically deletes a line and re-normalizes all remaining sortOrder values.
     */
    @Transaction
    open suspend fun deleteAndNormalize(line: LineEntity) {
        internalDeleteLine(line)

        val remainingLines = getLinesForFileSync(line.fileId)
        val normalizedLines = remainingLines.mapIndexed { index, l ->
            l.copy(sortOrder = index)
        }
        if (normalizedLines.isNotEmpty()) {
            internalUpdateLines(normalizedLines)
        }

        touchFile(line.fileId)
    }
    @Transaction
    open suspend fun togglePinFileIfAllowed(fileId: Long, maxPinned: Int): Boolean {
        val file = getFileById(fileId) ?: return true // no-op
        if (!file.isPinned) {
            val pinnedCount = getPinnedFilesCount()
            if (pinnedCount >= maxPinned) {
                return false
            }
        }
        updateFile(file.copy(isPinned = !file.isPinned))
        return true
    }
}
