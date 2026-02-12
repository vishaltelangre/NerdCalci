package com.vishaltelangre.nerdcalci.data.local

import androidx.room.*
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for calculator database operations.
 */
@Dao
interface CalculatorDao {
    // Returns files with pinned files first, then sorted by most recently modified
    @Query("SELECT * FROM files ORDER BY isPinned DESC, lastModified DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :fileId")
    suspend fun getFileById(fileId: Long): FileEntity?

    @Query("SELECT COUNT(*) FROM files WHERE isPinned = 1")
    suspend fun getPinnedFilesCount(): Int

    // Returns lines ordered by sortOrder (determines display order in UI)
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC")
    fun getLinesForFile(fileId: Long): Flow<List<LineEntity>>

    // Synchronous version for operations that need immediate results
    @Query("SELECT * FROM lines WHERE fileId = :fileId ORDER BY sortOrder ASC")
    suspend fun getLinesForFileSync(fileId: Long): List<LineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: LineEntity): Long

    @Update
    suspend fun updateLine(line: LineEntity)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Delete
    suspend fun deleteLine(line: LineEntity)
}
