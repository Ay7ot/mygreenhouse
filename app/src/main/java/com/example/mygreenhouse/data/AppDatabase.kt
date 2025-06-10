package com.example.mygreenhouse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mygreenhouse.data.dao.PlantDao
import com.example.mygreenhouse.data.dao.TaskDao
import com.example.mygreenhouse.data.dao.HarvestDao
import com.example.mygreenhouse.data.dao.SeedDao
import com.example.mygreenhouse.data.dao.PlantStageTransitionDao
import com.example.mygreenhouse.data.dao.StrainDao
import com.example.mygreenhouse.data.model.Converters
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.PlantStageTransition
import com.example.mygreenhouse.data.model.Strain

/**
 * Main database class for the app
 */
@Database(
    entities = [Plant::class, Task::class, Harvest::class, Seed::class, PlantStageTransition::class, Strain::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao
    abstract fun harvestDao(): HarvestDao
    abstract fun seedDao(): SeedDao
    abstract fun plantStageTransitionDao(): PlantStageTransitionDao
    abstract fun strainDao(): StrainDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE plants_new (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "strainName TEXT NOT NULL, " +
                        "batchNumber TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "source TEXT NOT NULL, " +
                        "type TEXT, " +
                        "gender TEXT NOT NULL, " +
                        "growthStage TEXT NOT NULL, " +
                        "startDate TEXT NOT NULL, " +
                        "lastUpdated TEXT NOT NULL, " +
                        "dryingStartDate TEXT, " +
                        "curingStartDate TEXT, " +
                        "seedToHarvestDays INTEGER, " +
                        "flowerDurationDays INTEGER, " +
                        "growMedium TEXT, " +
                        "nutrients TEXT NOT NULL, " +
                        "imagePath TEXT, " +
                        "isArchived INTEGER NOT NULL DEFAULT 0)")

                database.execSQL("INSERT INTO plants_new (id, strainName, batchNumber, quantity, source, type, gender, growthStage, startDate, lastUpdated, dryingStartDate, curingStartDate, seedToHarvestDays, flowerDurationDays, growMedium, nutrients, imagePath, isArchived) " +
                        "SELECT id, strainName, batchNumber, quantity, source, type, gender, growthStage, startDate, lastUpdated, dryingStartDate, curingStartDate, seedToHarvestDays, flowerDurationDays, soilType, nutrients, imagePath, isArchived FROM plants")

                database.execSQL("DROP TABLE plants")

                database.execSQL("ALTER TABLE plants_new RENAME TO plants")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN isCustomStrain INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE seeds ADD COLUMN isCustomStrain INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `plant_stage_transitions` (`id` TEXT NOT NULL, `plantId` TEXT NOT NULL, `stage` TEXT NOT NULL, `transitionDate` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`plantId`) REFERENCES `plants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_stage_transitions_plantId` ON `plant_stage_transitions` (`plantId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add repeatDays column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN repeatDays TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add completedDates column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN completedDates TEXT NOT NULL DEFAULT '[]'")
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create strains table for strain name management
                database.execSQL("""
                    CREATE TABLE strains (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        isCustomStrain INTEGER NOT NULL DEFAULT 0,
                        firstUsedDate TEXT NOT NULL,
                        lastUsedDate TEXT NOT NULL,
                        usageCount INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "greenhouse_db"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}