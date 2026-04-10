package com.vishaltelangre.nerdcalci.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import java.util.UUID

@Entity(
    tableName = "files",
    indices = [Index(value = ["syncId"], unique = true)]
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val syncId: String = UUID.randomUUID().toString(),
    val isTemporary: Boolean = false
)

@Entity(
    tableName = "lines",
    foreignKeys = [ForeignKey(
        entity = FileEntity::class,
        parentColumns = ["id"],
        childColumns = ["fileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fileId"])]
)
data class LineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val sortOrder: Int,
    val expression: String,
    val result: String = "",
    val version: Long = 0
)
