package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.SeedType
import kotlinx.coroutines.flow.Flow

@Dao
interface SeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeed(seed: Seed)
    
    @Update
    suspend fun updateSeed(seed: Seed)
    
    @Delete
    suspend fun deleteSeed(seed: Seed)
    
    @Query("SELECT * FROM seeds ORDER BY strainName ASC")
    fun getAllSeeds(): Flow<List<Seed>>
    
    @Query("SELECT * FROM seeds ORDER BY strainName ASC")
    suspend fun getAllSeedsOneShot(): List<Seed>
    
    @Query("SELECT * FROM seeds WHERE id = :seedId")
    fun getSeedById(seedId: String): Flow<Seed?>
    
    @Query("SELECT * FROM seeds WHERE strainName LIKE '%' || :searchQuery || '%'")
    fun searchSeedsByName(searchQuery: String): Flow<List<Seed>>
    
    @Query("SELECT * FROM seeds WHERE seedType = :seedType")
    fun getSeedsByType(seedType: SeedType): Flow<List<Seed>>
    
    @Query("SELECT SUM(seedCount) FROM seeds")
    fun getTotalSeedCount(): Flow<Int?>
    
    @Query("SELECT COUNT(DISTINCT strainName) FROM seeds")
    fun getUniqueStrainCount(): Flow<Int?>
} 