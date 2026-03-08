package com.vishaltelangre.nerdcalci.data.local

import androidx.room.*
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for calculator database operations.
 */
@Dao
abstract class CalculatorDao {
    // Returns files with pinned files first, then sorted by most recently modified
    @Query("SELECT * FROM files ORDER BY isPinned DESC, lastModified DESC")
    abstract fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :fileId")
    abstract suspend fun getFileById(fileId: Long): FileEntity?

    @Query("SELECT COUNT(*) FROM files WHERE isPinned = 1")
    abstract suspend fun getPinnedFilesCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM files WHERE name = :name)")
    abstract suspend fun doesFileExist(name: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM files WHERE name = :name AND id != :excludeId)")
    abstract suspend fun doesFileExist(name: String, excludeId: Long): Boolean

    // Returns lines ordered by sortOrder (determines display order in UI)
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC")
    abstract fun getLinesForFile(fileId: Long): Flow<List<LineEntity>>

    // Synchronous version for operations that need immediate results
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC")
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

    suspend fun touchFile(fileId: Long, timestamp: Long = System.currentTimeMillis()) {
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
    open suspend fun updateLines(fileId: Long, lines: List<LineEntity>) {
        internalUpdateLines(lines)
        touchFile(fileId)
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
}
