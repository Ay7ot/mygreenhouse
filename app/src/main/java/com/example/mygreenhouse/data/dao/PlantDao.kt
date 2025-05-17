package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)
    
    @Update
    suspend fun updatePlant(plant: Plant)
    
    @Delete
    suspend fun deletePlant(plant: Plant)
    
    @Query("SELECT * FROM plants WHERE isArchived = 0 ORDER BY lastUpdated DESC")
    fun getAllActivePlants(): Flow<List<Plant>>
    
    @Query("SELECT * FROM plants WHERE id = :plantId")
    fun getPlantById(plantId: String): Flow<Plant?>
    
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantByIdOnce(plantId: String): Plant?
    
    @Query("SELECT COUNT(*) FROM plants WHERE isArchived = 0")
    fun getActivePlantCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM plants WHERE growthStage = :stage AND isArchived = 0")
    fun getPlantCountByStage(stage: GrowthStage): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM plants WHERE growthStage IN ('DRYING') AND isArchived = 0")
    fun getDryingCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM plants WHERE growthStage IN ('CURING') AND isArchived = 0")
    fun getCuringCount(): Flow<Int>
} 