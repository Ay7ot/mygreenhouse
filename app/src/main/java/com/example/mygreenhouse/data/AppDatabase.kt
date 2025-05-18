package com.example.mygreenhouse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mygreenhouse.data.dao.PlantDao
import com.example.mygreenhouse.data.dao.TaskDao
import com.example.mygreenhouse.data.dao.HarvestDao
import com.example.mygreenhouse.data.dao.SeedDao
import com.example.mygreenhouse.data.model.Converters
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.data.model.Seed

/**
 * Main database class for the app
 */
@Database(
    entities = [Plant::class, Task::class, Harvest::class, Seed::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    abstract fun harvestDao(): HarvestDao
    abstract fun seedDao(): SeedDao
    
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