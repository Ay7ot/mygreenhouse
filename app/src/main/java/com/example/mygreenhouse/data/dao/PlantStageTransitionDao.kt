package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mygreenhouse.data.model.PlantStageTransition
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantStageTransitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: PlantStageTransition)

    @Query("SELECT * FROM plant_stage_transitions WHERE plantId = :plantId ORDER BY transitionDate ASC")
    fun getTransitionsForPlant(plantId: String): Flow<List<PlantStageTransition>>

    @Query("SELECT * FROM plant_stage_transitions WHERE plantId = :plantId ORDER BY transitionDate ASC")
    suspend fun getTransitionsForPlantOnce(plantId: String): List<PlantStageTransition>
} 