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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
    
    // Loading states
    private val _isLoadingPlants = MutableStateFlow(true)
    val isLoadingPlants: StateFlow<Boolean> = _isLoadingPlants.asStateFlow()
    
    private val _isLoadingTasks = MutableStateFlow(true)
    val isLoadingTasks: StateFlow<Boolean> = _isLoadingTasks.asStateFlow()
    
    // Plant name cache
    private val _plantNameCache = MutableStateFlow<Map<String, String>>(emptyMap())
    val plantNameCache: StateFlow<Map<String, String>> = _plantNameCache.asStateFlow()
    
    init {
        // Simulate loading delay and then set loading states to false
        viewModelScope.launch {
            // Simulate network delay (remove in production)
            delay(1500)
            _isLoadingPlants.value = false
            _isLoadingTasks.value = false
            
            // Load plant data into cache
            loadPlantCache()
        }
    }
    
    private fun loadPlantCache() {
        viewModelScope.launch {
            plants.collect { plantList ->
                val newCache = plantList.associate { it.id to it.strainName }
                _plantNameCache.value = newCache
            }
        }
    }
    
    /**
     * Get plant name by ID from cache or return fallback text
     */
    fun getPlantNameById(plantId: String?, fallback: String = "Unknown plant"): String {
        if (plantId == null) return fallback
        return _plantNameCache.value[plantId] ?: fallback
    }
    
    /**
     * Calculate days until the scheduled date/time
     */
    fun calculateDaysUntil(dateTime: LocalDateTime): Long {
        val today = LocalDate.now().atStartOfDay()
        return ChronoUnit.DAYS.between(today, dateTime)
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