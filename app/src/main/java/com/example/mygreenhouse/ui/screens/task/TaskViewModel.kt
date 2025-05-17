package com.example.mygreenhouse.ui.screens.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository = TaskRepository(AppDatabase.getDatabase(application).taskDao())

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            repository.insertTask(newTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                repository.markTaskAsIncomplete(task.id)
            } else {
                repository.markTaskAsCompleted(task.id)
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
            repository.updateTask(updatedTask)
        }
    }

    fun loadTask(taskId: String, onTaskLoaded: (Task?) -> Unit) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).first()
            onTaskLoaded(task)
        }
    }

    // Suspending function for use with LaunchedEffect
    suspend fun loadTaskAsync(taskId: String): Task? {
        return repository.getTaskById(taskId).first()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                TaskViewModel(application = application)
            }
        }
    }
} 