package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mygreenhouse.data.model.Harvest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HarvestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: Harvest)
    
    @Update
    suspend fun updateHarvest(harvest: Harvest)
    
    @Delete
    suspend fun deleteHarvest(harvest: Harvest)
    
    @Query("SELECT * FROM harvests ORDER BY harvestDate DESC")
    fun getAllHarvests(): Flow<List<Harvest>>
    
    @Query("SELECT * FROM harvests ORDER BY harvestDate DESC")
    suspend fun getAllHarvestsOneShot(): List<Harvest>
    
    @Query("SELECT * FROM harvests WHERE id = :harvestId")
    fun getHarvestById(harvestId: String): Flow<Harvest?>
    
    @Query("SELECT * FROM harvests WHERE isDrying = 1")
    fun getDryingHarvests(): Flow<List<Harvest>>
    
    @Query("SELECT * FROM harvests WHERE isCuring = 1")
    fun getCuringHarvests(): Flow<List<Harvest>>
    
    @Query("SELECT * FROM harvests WHERE isCompleted = 1")
    fun getCompletedHarvests(): Flow<List<Harvest>>
    
    @Query("SELECT * FROM harvests WHERE plantId = :plantId")
    fun getHarvestsForPlant(plantId: String): Flow<List<Harvest>>
    
    @Query("SELECT SUM(finalCuredWeight) FROM harvests WHERE isCompleted = 1")
    fun getTotalHarvestedWeight(): Flow<Double?>
    
    @Query("SELECT * FROM harvests WHERE harvestDate BETWEEN :startDate AND :endDate")
    fun getHarvestsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Harvest>>
} 