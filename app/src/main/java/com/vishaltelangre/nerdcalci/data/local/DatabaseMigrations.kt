package com.vishaltelangre.nerdcalci.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for AppDatabase.
 *
 * Each migration should:
 * - Have a descriptive comment explaining what changed
 * - Include SQL statements to modify schema
 * - Be added to the migrations array in AppDatabase initialization
 *
 * Migration naming: MIGRATION_[FROM]_[TO]
 */
object DatabaseMigrations {

    /**
     * Migration from version 1 to 2.
     *
     * Changes:
     * - Added index on lines.fileId column for improved query performance
     *   when deleting files or fetching lines for a specific file
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_lines_fileId ON lines(fileId)")
        }
    }

    /**
     * Migration from version 2 to 3.
     *
     * Changes:
     * - Added createdAt column to files table
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add createdAt column to files table
            db.execSQL("ALTER TABLE files ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            // Initialize createdAt with lastModified for existing files
            db.execSQL("UPDATE files SET createdAt = lastModified WHERE createdAt = 0")
        }
    }

    /**
     * Migration from version 3 to 4.
     *
     * Changes:
     * - Added syncId column to files table for robust two-way sync
     * - Added unique index on syncId for efficient lookups
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE files ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                UPDATE files
                SET syncId = lower(hex(randomblob(16))) || '-' || id
                WHERE syncId = ''
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_files_syncId ON files(syncId)")
        }
    }

    /**
     * Migration from version 4 to 5.
     *
     * Changes:
     * - Added version column to lines table for state-of-the-art sync
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE lines ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Migration from version 5 to 6.
     *
     * Changes:
     * - Added isTemporary column to files table
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE files ADD COLUMN isTemporary INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * All migrations in order.
     * Add new migrations to this array as the database evolves.
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE files ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7
    )
}
