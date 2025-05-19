package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.SeedDao
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.SeedType
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for accessing seed bank data
 */
class SeedRepository(private val seedDao: SeedDao) {
    
    val allSeeds: Flow<List<Seed>> = seedDao.getAllSeeds()
    val totalSeedCount: Flow<Int?> = seedDao.getTotalSeedCount()
    val uniqueStrainCount: Flow<Int?> = seedDao.getUniqueStrainCount()
    
    suspend fun insertSeed(seed: Seed) {
        seedDao.insertSeed(seed)
    }
    
    suspend fun updateSeed(seed: Seed) {
        seedDao.updateSeed(seed)
    }
    
    suspend fun deleteSeed(seed: Seed) {
        seedDao.deleteSeed(seed)
    }
    
    fun getSeedById(id: String): Flow<Seed?> {
        return seedDao.getSeedById(id)
    }
    
    fun searchSeedsByName(searchQuery: String): Flow<List<Seed>> {
        return seedDao.searchSeedsByName(searchQuery)
    }
    
    fun getSeedsByType(seedType: SeedType): Flow<List<Seed>> {
        return seedDao.getSeedsByType(seedType)
    }
    
    suspend fun getAllSeedsOneShot(): List<Seed> {
        return seedDao.getAllSeedsOneShot()
    }
} 