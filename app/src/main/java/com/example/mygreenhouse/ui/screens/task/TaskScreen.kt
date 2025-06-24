package com.example.mygreenhouse.ui.screens.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.DarkerGreenButton
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import androidx.navigation.NavController


// For displaying user-friendly names for TaskType
fun TaskType.displayName(): String {
    return when (this) {
        TaskType.WATERING -> "Watering"
        TaskType.FEEDING -> "Feeding"
        TaskType.PEST_CONTROL -> "Pest Control"
        TaskType.SOIL_TEST -> "Soil Test"
        TaskType.WATER_TEST -> "Water Test"
        TaskType.LIGHT_CYCLE_CHANGE -> "Light Cycle"
        TaskType.CO2_SUPPLEMENTATION -> "CO₂ Supplementation"
        TaskType.OTHER -> "Other Task"
    }
}

/**
 * Get appropriate icon for a task type
 */
@Composable
fun TaskType.getIcon(): ImageVector {
    return when (this) {
        TaskType.WATERING -> Icons.Filled.Opacity
        TaskType.FEEDING -> FeedingIcon.WateringCan
        TaskType.PEST_CONTROL -> Icons.Filled.BugReport
        TaskType.SOIL_TEST -> Icons.Filled.Science
        TaskType.WATER_TEST -> Icons.Filled.WaterDrop
        TaskType.LIGHT_CYCLE_CHANGE -> Icons.Filled.LightMode
        TaskType.CO2_SUPPLEMENTATION -> Icons.Filled.Co2
        TaskType.OTHER -> Icons.AutoMirrored.Filled.FormatListBulleted
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    onNavigateBack: () -> Unit,
    onTaskTypeSelected: (TaskType) -> Unit,
    onViewTaskList: () -> Unit,
    navController: NavController,
    darkTheme: Boolean
) {
    // Task types to be displayed
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
                title = { 
                    Text(
                        "Schedule Task", 
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) DarkBackground else MaterialTheme.colorScheme.surface,
                    titleContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.Task.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header text
            Text(
                text = "Select a task type to schedule",
                style = MaterialTheme.typography.titleMedium,
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.Start)
            )
            
            // Task type cards
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(taskTypesToDisplay) { taskType ->
                    TaskTypeCard(
                        taskType = taskType,
                        onClick = { onTaskTypeSelected(taskType) },
                        darkTheme = darkTheme
                    )
                }
            }
            
            // Add "View All Tasks" button at the bottom
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewTaskList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View All Tasks", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun TaskTypeCard(
    taskType: TaskType,
    onClick: () -> Unit,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task icon with circular background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (darkTheme) DarkerGreenButton else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = taskType.getIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Task name
            Text(
                text = taskType.displayName(),
                style = MaterialTheme.typography.titleMedium,
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 