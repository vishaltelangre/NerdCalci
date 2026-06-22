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
    val isTemporary: Boolean = false,
    val isGlobal: Boolean = false,
    val tags: String = ""
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

/**
 * Returns the file's tags as a list.
 * The stored string is already normalized (lowercase, hyphenated, sorted, deduplicated).
 */
val FileEntity.tagList: List<String>
    get() = if (tags.isBlank()) emptyList()
            else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }

/**
 * Normalizes a raw user-typed tag:
 * - Lowercase
 * - Trim leading/trailing whitespace
 * - Replace spaces and any character that isn't a letter, digit, or hyphen with a hyphen
 * - Collapse consecutive hyphens
 * - Strip leading/trailing hyphens
 */
fun String.normalizeTag(): String =
    this.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9-]+"), "-")
        .replace(Regex("-{2,}"), "-")
        .trim('-')

/**
 * Converts a list of tags into the canonical storage string:
 * normalize each tag, remove blanks, deduplicate, sort alphabetically, join with comma.
 */
fun List<String>.toTagString(): String =
    this.map { it.normalizeTag() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
        .joinToString(",")
