package com.vishaltelangre.nerdcalci.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO files (name, lastModified, isPinned) VALUES ('Test File', 1000, 0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, DatabaseMigrations.MIGRATION_2_3)

        val cursor = db.query("SELECT * FROM files WHERE name = 'Test File'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()

        val nameIndex = cursor.getColumnIndex("name")
        val lastModifiedIndex = cursor.getColumnIndex("lastModified")
        val createdAtIndex = cursor.getColumnIndex("createdAt")
        val isPinnedIndex = cursor.getColumnIndex("isPinned")

        assertEquals("Test File", cursor.getString(nameIndex))
        assertEquals(1000L, cursor.getLong(lastModifiedIndex))
        assertEquals(1000L, cursor.getLong(createdAtIndex))
        assertEquals(0, cursor.getInt(isPinnedIndex))

        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL("INSERT INTO files (name, lastModified, isPinned, createdAt) VALUES ('Sync File', 1000, 0, 1000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, DatabaseMigrations.MIGRATION_3_4)
        
        val cursor = db.query("SELECT * FROM files WHERE name = 'Sync File'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        
        val syncIdIndex = cursor.getColumnIndex("syncId")
        val syncId = cursor.getString(syncIdIndex)
        assertTrue("syncId should not be blank", syncId.isNotBlank())
        
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("INSERT INTO files (id, name, lastModified, isPinned, createdAt, syncId) VALUES (1, 'Version File', 1000, 0, 1000, 'sync-id')")
            execSQL("INSERT INTO lines (id, fileId, expression, result, sortOrder) VALUES (1, 1, '1+1', '2', 0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, DatabaseMigrations.MIGRATION_4_5)
        
        val cursor = db.query("SELECT * FROM lines WHERE id = 1")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        
        val versionIndex = cursor.getColumnIndex("version")
        assertEquals(0L, cursor.getLong(versionIndex))
        
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("INSERT INTO files (id, name, lastModified, isPinned, createdAt, syncId) VALUES (1, 'Temp File', 1000, 0, 1000, 'sync-id')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, DatabaseMigrations.MIGRATION_5_6)
        
        val cursor = db.query("SELECT * FROM files WHERE id = 1")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        
        val tempIndex = cursor.getColumnIndex("isTemporary")
        assertEquals(0, cursor.getInt(tempIndex)) // Existing files should be 0 (false)
        
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 6, true, *DatabaseMigrations.ALL_MIGRATIONS)
    }

    // Static assertTrue proxy for brevity
    private fun assertTrue(message: String, condition: Boolean) {
        org.junit.Assert.assertTrue(message, condition)
    }
}
