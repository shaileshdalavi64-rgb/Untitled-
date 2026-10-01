package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchlistEntity::class,
        AlertRuleEntity::class,
        NotificationHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ScannerDatabase : RoomDatabase() {
    abstract fun scannerDao(): ScannerDao

    companion object {
        @Volatile
        private var INSTANCE: ScannerDatabase? = null

        fun getInstance(context: Context): ScannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScannerDatabase::class.java,
                    "sma44_scanner_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
