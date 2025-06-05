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
    val upcomingTasks: Flow<List<Task>> = taskRepository.getDashboardTasks(5)
    
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
                val newCache = plantList.associate { plant -> 
                    plant.id to "${plant.strainName} - ${plant.batchNumber}"
                }
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
     * Returns negative values for overdue tasks
     */
    fun calculateDaysUntil(dateTime: LocalDateTime): Long {
        val now = LocalDateTime.now()
        return ChronoUnit.DAYS.between(now.toLocalDate(), dateTime.toLocalDate())
    }
    
    /**
     * Check if a task is overdue
     */
    fun isTaskOverdue(dateTime: LocalDateTime): Boolean {
        return dateTime.isBefore(LocalDateTime.now())
    }
    
    /**
     * Check if a specific date is completed for a task
     */
    fun isDateCompleted(task: Task, date: LocalDate): Boolean {
        return date.toString() in task.completedDates
    }
    
    /**
     * Get the task status for a specific date
     */
    fun getTaskStatusForDate(task: Task, date: LocalDate): TaskStatus {
        val today = LocalDate.now()
        val isCompleted = isDateCompleted(task, date)
        
        return when {
            isCompleted -> TaskStatus.COMPLETED
            date.isBefore(today) -> TaskStatus.OVERDUE
            date.isEqual(today) -> TaskStatus.DUE_TODAY
            else -> TaskStatus.UPCOMING
        }
    }
    
    /**
     * Calculate the next occurrence date for a recurring task
     */
    fun getNextOccurrenceDate(task: Task): LocalDate? {
        if (task.repeatDays.isEmpty()) {
            // Non-recurring task
            return task.scheduledDateTime.toLocalDate()
        }
        
        // For recurring tasks, find the next occurrence that isn't completed
        val today = LocalDate.now()
        val dayOfWeekMap = mapOf(
            "MON" to java.time.DayOfWeek.MONDAY,
            "TUE" to java.time.DayOfWeek.TUESDAY,
            "WED" to java.time.DayOfWeek.WEDNESDAY,
            "THU" to java.time.DayOfWeek.THURSDAY,
            "FRI" to java.time.DayOfWeek.FRIDAY,
            "SAT" to java.time.DayOfWeek.SATURDAY,
            "SUN" to java.time.DayOfWeek.SUNDAY
        )
        
        val selectedDaysOfWeek = task.repeatDays.mapNotNull { dayOfWeekMap[it] }
        if (selectedDaysOfWeek.isEmpty()) return null
        
        // Check today first
        if (selectedDaysOfWeek.contains(today.dayOfWeek) && !isDateCompleted(task, today)) {
            return today
        }
        
        // Find the next occurrence within the next 7 days
        for (i in 1..7) {
            val checkDate = today.plusDays(i.toLong())
            if (selectedDaysOfWeek.contains(checkDate.dayOfWeek) && !isDateCompleted(task, checkDate)) {
                return checkDate
            }
        }
        
        return null
    }
    
    /**
     * Get the most relevant date to display for a task (either overdue or next occurrence)
     */
    fun getDisplayDate(task: Task): LocalDate {
        val nextOccurrence = getNextOccurrenceDate(task)
        if (nextOccurrence != null) {
            return nextOccurrence
        }
        
        // Fallback to original scheduled date
        return task.scheduledDateTime.toLocalDate()
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

/**
 * Enum representing different task statuses
 */
enum class TaskStatus {
    UPCOMING,
    DUE_TODAY,
    OVERDUE,
    COMPLETED
} 