package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.HarvestDao
import com.example.mygreenhouse.data.model.Harvest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository class for accessing harvest data
 */
class HarvestRepository(private val harvestDao: HarvestDao) {
    
    val allHarvests: Flow<List<Harvest>> = harvestDao.getAllHarvests()
    val dryingHarvests: Flow<List<Harvest>> = harvestDao.getDryingHarvests()
    val curingHarvests: Flow<List<Harvest>> = harvestDao.getCuringHarvests()
    val completedHarvests: Flow<List<Harvest>> = harvestDao.getCompletedHarvests()
    val totalHarvestedWeight: Flow<Double?> = harvestDao.getTotalHarvestedWeight()
    
    suspend fun insertHarvest(harvest: Harvest) {
        harvestDao.insertHarvest(harvest)
    }
    
    suspend fun updateHarvest(harvest: Harvest) {
        harvestDao.updateHarvest(harvest)
    }
    
    suspend fun deleteHarvest(harvest: Harvest) {
        harvestDao.deleteHarvest(harvest)
    }
    
    fun getHarvestById(id: String): Flow<Harvest?> {
        return harvestDao.getHarvestById(id)
    }
    
    fun getHarvestsForPlant(plantId: String): Flow<List<Harvest>> {
        return harvestDao.getHarvestsForPlant(plantId)
    }
    
    fun getHarvestsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Harvest>> {
        return harvestDao.getHarvestsInDateRange(startDate, endDate)
    }
    
    suspend fun getAllHarvestsOneShot(): List<Harvest> {
        return harvestDao.getAllHarvestsOneShot()
    }
} 