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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository: TaskRepository = TaskRepository(AppDatabase.getDatabase(application).taskDao())
    private val plantRepository: PlantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())

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
                val newCache = plantList.associate { it.id to it.strainName }
                _plantNameCache.value = newCache
            }
        }
    }

    fun saveTask(
        taskType: TaskType,
        time: Calendar,
        repeatDays: List<String>, // We'll need to decide how to store/use this
        notes: String,
        plantId: String? = null // Optional plant ID
    ) {
        viewModelScope.launch {
            // Convert Calendar to LocalDateTime
            val localTime = LocalTime.of(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
            val localDateTime = java.time.LocalDateTime.now().with(localTime)

            // The Task model expects a description. For now, notes can be the description.
            // RepeatDays logic will need further implementation (e.g. creating multiple tasks or a recurring task entity)
            // For this initial step, we save a single task instance.
            val newTask = Task(
                type = taskType,
                description = notes, // Using notes as description for now
                scheduledDateTime = localDateTime,
                plantId = plantId
                // isCompleted and completedDateTime have defaults
            )
            taskRepository.insertTask(newTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                taskRepository.markTaskAsIncomplete(task.id)
            } else {
                taskRepository.markTaskAsCompleted(task.id)
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
            // Convert Calendar to LocalDateTime
            val localTime = LocalTime.of(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
            val localDateTime = java.time.LocalDateTime.now().with(localTime)

            // Create updated task object with the same ID
            val updatedTask = Task(
                id = existingTaskId, // Keep the same ID to replace the existing task
                type = taskType,
                description = notes,
                scheduledDateTime = localDateTime,
                plantId = plantId
                // isCompleted and completedDateTime remain unchanged from original task
                // This simplification might not be ideal in all scenarios
            )
            taskRepository.updateTask(updatedTask)
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