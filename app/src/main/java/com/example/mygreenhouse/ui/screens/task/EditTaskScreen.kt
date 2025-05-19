package com.example.mygreenhouse.ui.screens.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import kotlinx.coroutines.flow.first

/**
 * A wrapper composable that loads a task and then displays the ScheduleTaskScreen for editing
 */
@Composable
fun EditTaskScreen(
    taskId: String,
    taskType: TaskType,
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    darkTheme: Boolean
) {
    var task by remember { mutableStateOf<Task?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Load the task when the composable is first displayed
    LaunchedEffect(taskId) {
        try {
            val loadedTask = viewModel.loadTaskAsync(taskId)
            task = loadedTask
            isLoading = false
        } catch (e: Exception) {
            error = "Error loading task: ${e.message}"
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
            error != null -> Text(error!!, color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground)
            task != null -> ScheduleTaskScreen(
                taskType = taskType,
                onNavigateBack = onNavigateBack,
                onSaveTask = { _, _, _, _ -> /* This parameter is unused as we use viewModel directly */ },
                viewModel = viewModel,
                existingTask = task,
                darkTheme = darkTheme
            )
            else -> {
                // If task couldn't be loaded, go back
                LaunchedEffect(Unit) {
                    onNavigateBack()
                }
            }
        }
    }
} 