package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mygreenhouse.data.model.Strain
import kotlinx.coroutines.flow.Flow

@Dao
interface StrainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrain(strain: Strain)
    
    @Update
    suspend fun updateStrain(strain: Strain)
    
    @Delete
    suspend fun deleteStrain(strain: Strain)
    
    @Query("SELECT * FROM strains ORDER BY lastUsedDate DESC, name ASC")
    fun getAllStrains(): Flow<List<Strain>>
    
    @Query("SELECT * FROM strains ORDER BY lastUsedDate DESC, name ASC")
    suspend fun getAllStrainsOneShot(): List<Strain>
    
    @Query("SELECT * FROM strains WHERE id = :strainId")
    fun getStrainById(strainId: String): Flow<Strain?>
    
    @Query("SELECT * FROM strains WHERE id = :strainId")
    suspend fun getStrainByIdOnce(strainId: String): Strain?
    
    @Query("SELECT * FROM strains WHERE name = :name LIMIT 1")
    suspend fun getStrainByName(name: String): Strain?
    
    @Query("SELECT * FROM strains WHERE name LIKE '%' || :searchQuery || '%' ORDER BY lastUsedDate DESC")
    fun searchStrainsByName(searchQuery: String): Flow<List<Strain>>
    
    @Query("SELECT COUNT(*) FROM strains")
    fun getStrainCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM strains WHERE isCustomStrain = 1")
    fun getCustomStrainCount(): Flow<Int>
    
    @Query("UPDATE strains SET lastUsedDate = :date, usageCount = usageCount + 1 WHERE name = :name")
    suspend fun incrementStrainUsage(name: String, date: java.time.LocalDate)
    
    @Query("UPDATE strains SET usageCount = 1 WHERE name = :name")
    suspend fun resetStrainUsageCount(name: String)
    
    @Query("DELETE FROM strains")
    suspend fun deleteAllStrains()
} 