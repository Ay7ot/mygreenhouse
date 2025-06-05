package com.example.mygreenhouse.ui.screens.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.TaskRepository
import com.example.mygreenhouse.utils.TaskNotificationScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository: TaskRepository = TaskRepository(AppDatabase.getDatabase(application).taskDao())
    private val plantRepository: PlantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    private val app = application // Store application context for scheduler

    // Loading states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingPlants = MutableStateFlow(true)
    val isLoadingPlants: StateFlow<Boolean> = _isLoadingPlants.asStateFlow()

    // Data states
    val allTasks: StateFlow<List<Task>> = taskRepository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val plants: StateFlow<List<Plant>> = plantRepository.allActivePlants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // Plant name cache
    private val _plantNameCache = MutableStateFlow<Map<String, String>>(emptyMap())
    val plantNameCache: StateFlow<Map<String, String>> = _plantNameCache.asStateFlow()

    init {
        // Simulate loading delay
        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
            _isLoadingPlants.value = false
            
            // Initialize plant name cache
            plants.collect { plantList ->
                val newCache = plantList.associate { plant -> 
                    plant.id to "${plant.strainName} - ${plant.batchNumber}"
                }
                _plantNameCache.value = newCache
            }
        }
    }

    /**
     * Helper function to calculate the next occurrence date based on selected repeat days
     */
    private fun calculateNextOccurrence(time: Calendar, repeatDays: List<String>): java.time.LocalDateTime {
        val localTime = LocalTime.of(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
        
        // If no repeat days selected, use today's date (non-recurring task)
        if (repeatDays.isEmpty()) {
            val today = java.time.LocalDate.now()
            val currentTime = java.time.LocalTime.now()
            
            // If the selected time is later than current time, schedule for today
            // Otherwise, schedule for tomorrow
            return if (localTime.isAfter(currentTime)) {
                today.atTime(localTime)
            } else {
                today.plusDays(1).atTime(localTime)
            }
        }
        
        val today = java.time.LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek
        
        // Map repeat day strings to DayOfWeek enum
        val dayOfWeekMap = mapOf(
            "MON" to DayOfWeek.MONDAY,
            "TUE" to DayOfWeek.TUESDAY, 
            "WED" to DayOfWeek.WEDNESDAY,
            "THU" to DayOfWeek.THURSDAY,
            "FRI" to DayOfWeek.FRIDAY,
            "SAT" to DayOfWeek.SATURDAY,
            "SUN" to DayOfWeek.SUNDAY
        )
        
        val selectedDaysOfWeek = repeatDays.mapNotNull { dayOfWeekMap[it] }
        
        if (selectedDaysOfWeek.isEmpty()) {
            // Fallback to today if no valid days found
            val currentTime = java.time.LocalTime.now()
            return if (localTime.isAfter(currentTime)) {
                today.atTime(localTime)
            } else {
                today.plusDays(1).atTime(localTime)
            }
        }
        
        // Find the next occurrence
        // First check if today is one of the selected days and the time hasn't passed yet
        val currentTime = java.time.LocalTime.now()
        if (selectedDaysOfWeek.contains(currentDayOfWeek) && localTime.isAfter(currentTime)) {
            return today.atTime(localTime)
        }
        
        // Find the next day that matches one of the selected days
        var nextDate = today.plusDays(1)
        while (!selectedDaysOfWeek.contains(nextDate.dayOfWeek)) {
            nextDate = nextDate.plusDays(1)
            // Safety check to prevent infinite loop (should never happen with valid days)
            if (nextDate.isAfter(today.plusDays(7))) {
                // Fallback to next week's first selected day
                nextDate = today.with(TemporalAdjusters.next(selectedDaysOfWeek.minBy { it.value }))
                break
            }
        }
        
        return nextDate.atTime(localTime)
    }

    fun saveTask(
        taskType: TaskType,
        time: Calendar,
        repeatDays: List<String>, // We'll need to decide how to store/use this
        notes: String,
        plantId: String? = null // Optional plant ID
    ) {
        viewModelScope.launch {
            // Calculate the next occurrence based on selected repeat days
            val scheduledDateTime = calculateNextOccurrence(time, repeatDays)

            // Create task with repeat days information
            val newTask = Task(
                type = taskType,
                description = notes, // Using notes as description for now
                scheduledDateTime = scheduledDateTime,
                repeatDays = repeatDays, // Store the repeat days
                plantId = plantId
                // isCompleted and completedDateTime have defaults
            )
            taskRepository.insertTask(newTask)
            TaskNotificationScheduler.scheduleTaskNotification(app, newTask) // Schedule notification
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            TaskNotificationScheduler.cancelTaskNotification(app, task.id) // Cancel notification
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                taskRepository.markTaskAsIncomplete(task.id)
                // Reschedule if it was marked incomplete and is still in the future
                val updatedTask = task.copy(isCompleted = false, completedDateTime = null)
                TaskNotificationScheduler.scheduleTaskNotification(app, updatedTask)
            } else {
                taskRepository.markTaskAsCompleted(task.id)
                TaskNotificationScheduler.cancelTaskNotification(app, task.id) // Cancel if marked complete
            }
        }
    }

    // New methods for date-specific completion
    fun markDateAsCompleted(task: Task, date: java.time.LocalDate) {
        viewModelScope.launch {
            taskRepository.markDateAsCompleted(task.id, date)
            // Cancel notification only if this is for today and task is non-recurring
            if (task.repeatDays.isEmpty() && date.isEqual(java.time.LocalDate.now())) {
                TaskNotificationScheduler.cancelTaskNotification(app, task.id)
            }
        }
    }
    
    fun markDateAsIncomplete(task: Task, date: java.time.LocalDate) {
        viewModelScope.launch {
            taskRepository.markDateAsIncomplete(task.id, date)
            // Reschedule notification if marking today as incomplete
            if (date.isEqual(java.time.LocalDate.now()) && task.scheduledDateTime.isAfter(java.time.LocalDateTime.now())) {
                TaskNotificationScheduler.scheduleTaskNotification(app, task)
            }
        }
    }
    
    // Toggle completion for a specific date (for dashboard use)
    fun toggleDateCompletion(task: Task, date: java.time.LocalDate) {
        viewModelScope.launch {
            val isCompleted = date.toString() in task.completedDates
            if (isCompleted) {
                markDateAsIncomplete(task, date)
            } else {
                markDateAsCompleted(task, date)
            }
        }
    }

    fun updateTask(
        existingTaskId: String,
        taskType: TaskType,
        time: Calendar,
        repeatDays: List<String>,
        notes: String,
        plantId: String? = null
    ) {
        viewModelScope.launch {
            // Calculate the next occurrence based on selected repeat days
            val scheduledDateTime = calculateNextOccurrence(time, repeatDays)

            // Fetch the original task to maintain its completed status if not changing it here
            val originalTask = taskRepository.getTaskById(existingTaskId).first()

            val updatedTask = Task(
                id = existingTaskId, 
                type = taskType,
                description = notes,
                scheduledDateTime = scheduledDateTime,
                repeatDays = repeatDays,
                plantId = plantId,
                isCompleted = originalTask?.isCompleted ?: false, // Preserve original completion status
                completedDateTime = originalTask?.completedDateTime // Preserve original completion time
            )
            taskRepository.updateTask(updatedTask)
            // Schedule/reschedule notification for the updated task
            TaskNotificationScheduler.scheduleTaskNotification(app, updatedTask) 
        }
    }

    fun loadTask(taskId: String, onTaskLoaded: (Task?) -> Unit) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId).first()
            onTaskLoaded(task)
        }
    }

    // Suspending function for use with LaunchedEffect
    suspend fun loadTaskAsync(taskId: String): Task? {
        return taskRepository.getTaskById(taskId).first()
    }

    /**
     * Get plant name by its ID
     */
    fun getPlantNameById(plantId: String?, fallback: String = "Unknown plant"): String {
        if (plantId == null) return fallback
        return _plantNameCache.value[plantId] ?: fallback
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY] as Application
                TaskViewModel(application)
            }
        }
    }
} 