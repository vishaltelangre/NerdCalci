package com.vishaltelangre.nerdcalci.data.backup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.AppDatabase
import com.vishaltelangre.nerdcalci.data.local.DatabaseMigrations

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "AutoBackupWorker"
    }

    override suspend fun doWork(): Result {
        val settings = BackupManager.readSettings(BackupManager.prefs(applicationContext))
        if (!settings.enabled) {
            return Result.success()
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()

        return try {
            val result = BackupManager.backupNow(applicationContext, db.calculatorDao())
            db.close()

            result.onSuccess { message ->
                Log.d(TAG, "Backup succeeded: $message")
            }
            result.onFailure { error ->
                Log.w(TAG, "Backup failed", error)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Backup exception", e)
            db.close()
            Result.success()
        }
    }
}
