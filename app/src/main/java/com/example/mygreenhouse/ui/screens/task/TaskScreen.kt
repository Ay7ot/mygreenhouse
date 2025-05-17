package com.example.mygreenhouse.ui.screens.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygreenhouse.data.model.TaskType // Assuming TaskType has a user-friendly name property or we map it
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkerGreenButton
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite

// For displaying user-friendly names for TaskType
fun TaskType.displayName(): String {
    return when (this) {
        TaskType.WATERING -> "Watering"
        TaskType.FEEDING -> "Feeding"
        TaskType.PEST_CONTROL -> "Pest Control"
        TaskType.SOIL_TEST -> "Soil Test"
        TaskType.WATER_TEST -> "Water Test"
        TaskType.LIGHT_CYCLE_CHANGE -> "Light Cycle Change"
        TaskType.CO2_SUPPLEMENTATION -> "CO₂ Supplementation"
        TaskType.OTHER -> "Other Task"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    onNavigateBack: () -> Unit,
    onTaskTypeSelected: (TaskType) -> Unit, // Callback for when a task type is selected
    onViewTaskList: () -> Unit // New callback for viewing all tasks
) {
    // Task types to be displayed, matching the second image
    val taskTypesToDisplay = listOf(
        TaskType.WATERING,
        TaskType.FEEDING,
        TaskType.PEST_CONTROL,
        TaskType.SOIL_TEST,
        TaskType.WATER_TEST,
        TaskType.LIGHT_CYCLE_CHANGE
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.Task.route,
                onNavItemClick = { route ->
                    if (route != NavDestination.Task.route) {
                        // This navigation logic might need adjustment based on actual NavHost controller
                        // For now, assumes onNavigateBack can handle going "up" or to a different tab
                        onNavigateBack() // Or a more specific navigation action
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task type buttons
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(taskTypesToDisplay) { taskType ->
                    Button(
                        onClick = { onTaskTypeSelected(taskType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkerGreenButton,
                            contentColor = TextWhite
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = taskType.displayName(), fontSize = 18.sp)
                    }
                }
            }
            
            // Add "View All Tasks" button at the bottom
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewTaskList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = TextWhite
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "View All Tasks", fontSize = 18.sp)
            }
        }
    }
} 