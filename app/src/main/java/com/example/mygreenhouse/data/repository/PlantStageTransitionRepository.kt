package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.PlantStageTransitionDao
import com.example.mygreenhouse.data.model.PlantStageTransition
import kotlinx.coroutines.flow.Flow

class PlantStageTransitionRepository(private val plantStageTransitionDao: PlantStageTransitionDao) {

    suspend fun insertTransition(transition: PlantStageTransition) {
        plantStageTransitionDao.insertTransition(transition)
    }

    fun getTransitionsForPlant(plantId: String): Flow<List<PlantStageTransition>> {
        return plantStageTransitionDao.getTransitionsForPlant(plantId)
    }

    suspend fun getTransitionsForPlantOnce(plantId: String): List<PlantStageTransition> {
        return plantStageTransitionDao.getTransitionsForPlantOnce(plantId)
    }
} 