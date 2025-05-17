package com.example.mygreenhouse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mygreenhouse.data.dao.PlantDao
import com.example.mygreenhouse.data.dao.TaskDao
import com.example.mygreenhouse.data.model.Converters
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task

/**
 * Main database class for the app
 */
@Database(
    entities = [Plant::class, Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "greenhouse_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}