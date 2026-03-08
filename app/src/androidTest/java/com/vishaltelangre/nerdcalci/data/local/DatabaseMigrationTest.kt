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
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 3, true, *DatabaseMigrations.ALL_MIGRATIONS)
    }
}
