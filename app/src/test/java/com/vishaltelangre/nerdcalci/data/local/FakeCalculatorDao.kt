package com.vishaltelangre.nerdcalci.data.local

import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.utils.FilenameUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * A fake implementation of [CalculatorDao] for unit tests.
 * Maintains state in memory using [MutableStateFlow].
 */
open class FakeCalculatorDao : CalculatorDao() {
    private val _files = MutableStateFlow<List<FileEntity>>(emptyList())
    private val _lines = MutableStateFlow<List<LineEntity>>(emptyList())

    // Exposed for direct manipulation and assertion in tests
    val files: List<FileEntity> get() = _files.value
    val lines: List<LineEntity> get() = _lines.value

    /**
     * Injects a file into the fake storage.
     */
    fun addFile(file: FileEntity) {
        _files.value = _files.value + file
    }

    /**
     * Injects a line into the fake storage.
     */
    fun addLine(line: LineEntity) {
        _lines.value = _lines.value + line
    }

    private val fileIdGen = AtomicLong(100L)
    private val lineIdGen = AtomicLong(1000L)

    private fun nextFileId() = fileIdGen.incrementAndGet()
    private fun nextLineId() = lineIdGen.incrementAndGet()

    override fun getAllFiles(): Flow<List<FileEntity>> = 
        _files.map { it.filter { !it.isTemporary }.sortedByDescending { it.lastModified } }

    override suspend fun getAllFilesSync(): List<FileEntity> = 
        _files.value.filter { !it.isTemporary }.sortedByDescending { it.lastModified }

    override suspend fun getTemporaryFile(): FileEntity? = 
        _files.value.find { it.isTemporary }

    override suspend fun getUntitledFileNames(): List<String> =
        _files.value.filter { !it.isTemporary && it.name.startsWith("Untitled ") }.map { it.name }

    override suspend fun getFileById(fileId: Long): FileEntity? = 
        _files.value.find { it.id == fileId }

    override suspend fun getFileByName(name: String): FileEntity? = 
        _files.value.find { it.name == name && !it.isTemporary }

    override suspend fun getFileBySyncId(syncId: String): FileEntity? = 
        _files.value.find { it.syncId == syncId }

    override suspend fun getPinnedFilesCount(): Int = 
        _files.value.count { it.isPinned }

    override suspend fun doesFileExist(name: String): Boolean = 
        _files.value.any { it.name == name && !it.isTemporary }

    override suspend fun doesFileExist(name: String, excludeId: Long): Boolean = 
        _files.value.any { it.name == name && !it.isTemporary && it.id != excludeId }

    override fun getLinesForFile(fileId: Long): Flow<List<LineEntity>> =
        _lines.map { list -> list.filter { it.fileId == fileId }.sortedWith(compareBy({ it.sortOrder }, { it.id })) }

    override suspend fun getLinesForFileSync(fileId: Long): List<LineEntity> =
        _lines.value.filter { it.fileId == fileId }.sortedWith(compareBy({ it.sortOrder }, { it.id }))

    override suspend fun getLineById(lineId: Long): LineEntity? = 
        _lines.value.find { it.id == lineId }

    override suspend fun getLineCountForFile(fileId: Long): Int = 
        _lines.value.count { it.fileId == fileId }

    override suspend fun insertFile(file: FileEntity): Long {
        val id = if (file.id == 0L) nextFileId() else file.id
        val newFile = file.copy(id = id)
        _files.value = _files.value.filter { it.id != id } + newFile
        return id
    }

    override suspend fun internalInsertLines(lines: List<LineEntity>) {
        val toInsert = lines.map { 
            val id = if (it.id == 0L) nextLineId() else it.id
            it.copy(id = id)
        }
        _lines.value = _lines.value + toInsert
    }

    override suspend fun internalInsertLine(line: LineEntity): Long {
        val id = if (line.id == 0L) nextLineId() else line.id
        val newLine = line.copy(id = id)
        _lines.value = _lines.value + newLine
        return id
    }

    override suspend fun internalUpdateLine(line: LineEntity) {
        _lines.value = _lines.value.map { if (it.id == line.id) line else it }
    }

    override suspend fun internalUpdateLines(lines: List<LineEntity>) {
        val updates = lines.associateBy { it.id }
        _lines.value = _lines.value.map { updates[it.id] ?: it }
    }

    override suspend fun internalDeleteLine(line: LineEntity) {
        _lines.value = _lines.value.filter { it.id != line.id }
    }

    override suspend fun internalUpdateFile(file: FileEntity) {
        _files.value = _files.value.map { if (it.id == file.id) file else it }
    }

    override suspend fun internalUpdateFiles(files: List<FileEntity>) {
        val updates = files.associateBy { it.id }
        _files.value = _files.value.map { updates[it.id] ?: it }
    }

    override suspend fun deleteFile(file: FileEntity) {
        _files.value = _files.value.filter { it.id != file.id }
        _lines.value = _lines.value.filter { it.fileId != file.id }
    }

    override suspend fun updateFileTimestamp(fileId: Long, timestamp: Long) {
        _files.value = _files.value.map { if (it.id == fileId) it.copy(lastModified = timestamp) else it }
    }

    override suspend fun internalRenameFile(fileId: Long, name: String) {
        _files.value = _files.value.map { if (it.id == fileId) it.copy(name = name) else it }
    }

    override suspend fun updateSyncId(fileId: Long, newSyncId: String) {
        _files.value = _files.value.map { if (it.id == fileId) it.copy(syncId = newSyncId) else it }
    }

    override suspend fun internalDeleteLinesForFile(fileId: Long) {
        _lines.value = _lines.value.filter { it.fileId != fileId }
    }

    override suspend fun clearAllLines(fileId: Long) {
        internalDeleteLinesForFile(fileId)
        internalInsertLine(LineEntity(fileId = fileId, sortOrder = 0, expression = "", result = ""))
        touchFile(fileId)
    }

    override suspend fun duplicateFile(fileId: Long, newName: String, newSyncId: String, lastModified: Long?): Long {
        val originalFile = getFileById(fileId) ?: throw Exception("Original file not found")
        val now = System.currentTimeMillis()
        val newFile = originalFile.copy(
            id = nextFileId(),
            name = newName,
            createdAt = now,
            isPinned = false,
            syncId = newSyncId,
            lastModified = lastModified ?: now,
            isTemporary = false
        )
        _files.value = _files.value + newFile
        
        val originalLines = getLinesForFileSync(fileId)
        val newLines = originalLines.map { it.copy(id = 0L, fileId = newFile.id) }
        internalInsertLines(newLines)
        return newFile.id
    }

    override suspend fun duplicateFile(sourceFileId: Long, createdAt: Long): Long? {
        val sourceFile = getFileById(sourceFileId) ?: return null
        val sourceLines = getLinesForFileSync(sourceFileId)

        val baseName = if (sourceFile.isTemporary) {
            "Copy of Scratchpad"
        } else {
            "Copy of ${sourceFile.name}"
        }
        val uniqueName = FilenameUtils.generateUniqueFileName(baseName) { name ->
            doesFileExist(name)
        }

        val newFileId = insertFile(
            FileEntity(
                name = uniqueName,
                lastModified = createdAt,
                createdAt = createdAt,
                isPinned = false,
                isTemporary = false,
                syncId = UUID.randomUUID().toString()
            )
        )

        val newLines = sourceLines.map { it.copy(id = 0, fileId = newFileId) }
        internalInsertLines(newLines)

        return newFileId
    }

    override suspend fun renameFileFromSync(fileId: Long, name: String) {
        internalRenameFile(fileId, name)
    }

    override suspend fun updateFileFromSync(file: FileEntity) {
        internalUpdateFile(file)
    }
}