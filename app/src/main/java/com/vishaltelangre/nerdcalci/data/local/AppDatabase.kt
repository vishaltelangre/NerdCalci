package com.vishaltelangre.nerdcalci.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

@Database(entities = [FileEntity::class, LineEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculatorDao(): CalculatorDao
}
