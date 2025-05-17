package com.example.mygreenhouse.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the Dashboard screen
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val plantRepository = PlantRepository(database.plantDao())
    private val taskRepository = TaskRepository(database.taskDao())
    
    // Exposed data
    val plants: Flow<List<Plant>> = plantRepository.allActivePlants
    val upcomingTasks: Flow<List<Task>> = taskRepository.getUpcomingTasks(5)
    
    // Sample data for testing
    init {
        // Use this for testing until the actual functionality to add plants and tasks is implemented
        viewModelScope.launch {
            if (plantRepository.activePlantCount.first() == 0) {
                addSampleData()
            }
        }
    }
    
    /**
     * Calculate days until the scheduled date/time
     */
    fun calculateDaysUntil(dateTime: LocalDateTime): Long {
        val today = LocalDate.now().atStartOfDay()
        return ChronoUnit.DAYS.between(today, dateTime)
    }
    
    /**
     * Add sample data for testing
     */
    private suspend fun addSampleData() {
        // Add sample plants
        val cactus = Plant(
            strainName = "Cactus",
            batchNumber = "C001",
            source = com.example.mygreenhouse.data.model.PlantSource.SEED,
            type = com.example.mygreenhouse.data.model.PlantType.AUTOFLOWER,
            growthStage = com.example.mygreenhouse.data.model.GrowthStage.VEGETATION,
            startDate = LocalDate.now().minusDays(30),
            lastUpdated = LocalDate.now(),
            seedToHarvestDays = 70,
            soilType = "Cactus Mix"
        )
        
        val bonsai = Plant(
            strainName = "Bonsai",
            batchNumber = "B001",
            source = com.example.mygreenhouse.data.model.PlantSource.CLONE,
            type = null,
            growthStage = com.example.mygreenhouse.data.model.GrowthStage.VEGETATION,
            startDate = LocalDate.now().minusDays(45),
            lastUpdated = LocalDate.now(),
            flowerDurationDays = 60,
            soilType = "Bonsai Soil"
        )
        
        plantRepository.insertPlant(cactus)
        plantRepository.insertPlant(bonsai)
        
        // Add sample tasks
        val wateringTask = Task(
            type = com.example.mygreenhouse.data.model.TaskType.WATERING,
            description = "Water your plant",
            scheduledDateTime = LocalDateTime.now().plusDays(2),
            plantId = cactus.id
        )
        
        val checkHumidityTask = Task(
            type = com.example.mygreenhouse.data.model.TaskType.OTHER,
            description = "Your plant is thirsty",
            scheduledDateTime = LocalDateTime.now().plusDays(3),
            plantId = bonsai.id
        )
        
        val checkGrowthTask = Task(
            type = com.example.mygreenhouse.data.model.TaskType.OTHER,
            description = "Your plant has grown",
            scheduledDateTime = LocalDateTime.now().plusDays(4),
            plantId = cactus.id
        )
        
        taskRepository.insertTask(wateringTask)
        taskRepository.insertTask(checkHumidityTask)
        taskRepository.insertTask(checkGrowthTask)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                DashboardViewModel(application)
            }
        }
    }
} 