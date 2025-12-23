package com.networkfrequency.controller

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FrequencyChangeLog::class], version = 1, exportSchema = false)
abstract class FrequencyDatabase : RoomDatabase() {
    abstract fun frequencyDao(): FrequencyDao
    
    companion object {
        @Volatile
        private var INSTANCE: FrequencyDatabase? = null
        
        fun getDatabase(context: Context): FrequencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrequencyDatabase::class.java,
                    "frequency_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
