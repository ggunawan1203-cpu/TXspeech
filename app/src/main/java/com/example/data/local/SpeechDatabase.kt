package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpeechHistoryEntity::class], version = 1, exportSchema = false)
abstract class SpeechDatabase : RoomDatabase() {
    abstract fun speechHistoryDao(): SpeechHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SpeechDatabase? = null

        fun getInstance(context: Context): SpeechDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpeechDatabase::class.java,
                    "speech_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
