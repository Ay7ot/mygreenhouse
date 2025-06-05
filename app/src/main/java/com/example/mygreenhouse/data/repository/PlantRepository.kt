package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.PlantDao
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.Plant
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for accessing plant data
 */
class PlantRepository(private val plantDao: PlantDao) {
    
    val allActivePlants: Flow<List<Plant>> = plantDao.getAllActivePlants()
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()
    
    // Legacy count methods (count records/batches)
    val activePlantCount: Flow<Int> = plantDao.getActivePlantCount()
    val dryingCount: Flow<Int> = plantDao.getDryingCount()
    val curingCount: Flow<Int> = plantDao.getCuringCount()
    
    // New quantity-based methods (sum quantities)
    val activePlantQuantity: Flow<Int> = plantDao.getActivePlantQuantity()
    val dryingQuantity: Flow<Int> = plantDao.getDryingQuantity()
    val curingQuantity: Flow<Int> = plantDao.getCuringQuantity()
    
    suspend fun insertPlant(plant: Plant) {
        plantDao.insertPlant(plant)
    }
    
    suspend fun updatePlant(plant: Plant) {
        plantDao.updatePlant(plant)
    }
    
    suspend fun deletePlant(plant: Plant) {
        plantDao.deletePlant(plant)
    }
    
    fun getPlantById(id: String): Flow<Plant?> {
        return plantDao.getPlantById(id)
    }
    
    // Added for one-shot fetch for EditPlantViewModel
    suspend fun getPlantByIdOnce(id: String): Plant? {
        return plantDao.getPlantByIdOnce(id)
    }
    
    suspend fun getAllPlantsOneShot(): List<Plant> {
        return plantDao.getAllPlantsOneShot()
    }
    
    fun getPlantCountByStage(stage: GrowthStage): Flow<Int> {
        return plantDao.getPlantCountByStage(stage)
    }
    
    fun getPlantQuantityByStage(stage: GrowthStage): Flow<Int> {
        return plantDao.getPlantQuantityByStage(stage)
    }
} 