package com.example.mygreenhouse.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.screens.task.displayName
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TaskAlertGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite

/**
 * Main Dashboard screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navigateToAddPlant: () -> Unit,
    navigateToEditPlant: (String) -> Unit,
    navigateToTask: () -> Unit,
    navigateToEditTask: (String, String) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val plants by viewModel.plants.collectAsState(initial = emptyList())
    val upcomingTasks by viewModel.upcomingTasks.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard",
                        color = TextWhite
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open drawer */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
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
                currentRoute = NavDestination.Dashboard.route,
                onNavItemClick = { route ->
                    when (route) {
                        NavDestination.AddPlant.route -> navigateToAddPlant()
                        NavDestination.Task.route -> navigateToTask()
                        // Add other navigation handlers as needed
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            // Plant carousel
            if (plants.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(plants) { plant ->
                        PlantCard(
                            imageUrl = plant.imagePath,
                            name = plant.strainName,
                            onClick = { navigateToEditPlant(plant.id) }
                        )
                    }
                }
            } else {
                // Placeholder for empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(16.dp)
                        .clickable { navigateToAddPlant() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add your first plant",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Task alerts section with improved header and more info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alerts & Notifications",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                
                if (upcomingTasks.isNotEmpty()) {
                    TextButton(
                        onClick = navigateToTask,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = PrimaryGreen
                        )
                    ) {
                        Text("View All")
                    }
                }
            }
            
            if (upcomingTasks.isNotEmpty()) {
                Column {
                    upcomingTasks.forEach { task ->
                        val daysUntil = viewModel.calculateDaysUntil(task.scheduledDateTime)
                        TaskAlert(
                            task = task,
                            daysUntil = daysUntil,
                            onClick = { 
                                // Navigate to edit task screen
                                // This will be implemented by the calling NavHost
                                navigateToEditTask(task.id, task.type.name) 
                            }
                        )
                    }
                }
            } else {
                // Placeholder for empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming tasks",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Plant card component for the carousel
 */
@Composable
fun PlantCard(
    imageUrl: String?,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        // Plant image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkSurface)
                )
            }
        }
        
        // Plant name
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
            color = TextWhite
        )
    }
}

/**
 * Task alert component
 */
@Composable
fun TaskAlert(
    task: Task,
    daysUntil: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.type.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextWhite
                )
                
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.8f)
                    )
                }
                
                val daysText = when (daysUntil) {
                    0L -> "Today"
                    1L -> "Tomorrow"
                    else -> "In $daysUntil days"
                }
                
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysUntil == 0L) PrimaryGreen else TextGrey,
                    fontWeight = if (daysUntil == 0L) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            
            // Alert indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (daysUntil == 0L) TaskAlertGreen else TextGrey.copy(alpha = 0.5f))
            )
        }
        
        Divider(
            color = TextWhite.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
} 