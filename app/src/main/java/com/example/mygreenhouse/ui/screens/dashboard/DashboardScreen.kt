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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.components.PlantCarouselSkeleton
import com.example.mygreenhouse.ui.components.TaskAlertsSkeleton
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.screens.task.displayName
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import androidx.navigation.NavController

/**
 * Main Dashboard screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navigateToAddPlant: () -> Unit,
    navigateToEditPlant: (String) -> Unit,
    navigateToAllPlants: () -> Unit,
    navigateToTask: () -> Unit,
    navigateToTaskList: () -> Unit,
    navigateToEditTask: (String, String) -> Unit,
    navigateToQuickStats: () -> Unit,
    navigateToDankBank: () -> Unit,
    navigateToSettings: () -> Unit,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    darkTheme: Boolean
) {
    val plants by viewModel.plants.collectAsState(initial = null)
    val upcomingTasks by viewModel.upcomingTasks.collectAsState(initial = null)
    
    val isLoadingPlants by viewModel.isLoadingPlants.collectAsState()
    val isLoadingTasks by viewModel.isLoadingTasks.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard",
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
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
                currentRoute = NavDestination.Dashboard.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Section Header for Plants
            SectionHeader(
                title = "My Greenhouse",
                actionText = if (!isLoadingPlants && plants?.isNotEmpty() == true) "View All" else null,
                onActionClick = navigateToAllPlants
            )
            
            // Plant carousel, skeleton, or empty state
            when {
                isLoadingPlants || plants == null -> PlantCarouselSkeleton()
                plants!!.isNotEmpty() -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(plants!!) { plant ->
                            PlantCard(
                                imageUrl = plant.imagePath,
                                name = plant.strainName,
                                onClick = { navigateToEditPlant(plant.id) }
                            )
                        }
                    }
                }
                else -> EmptyPlantsView(
                    onAddPlantClick = navigateToAddPlant,
                    darkTheme = darkTheme
                )
            }
            
            // Section header for tasks
            SectionHeader(
                title = "Alerts & Notifications",
                actionText = if (!isLoadingTasks && upcomingTasks?.isNotEmpty() == true) "View All" else null,
                onActionClick = navigateToTaskList
            )
            
            // Task alerts, skeleton, or empty state
            when {
                isLoadingTasks || upcomingTasks == null -> TaskAlertsSkeleton()
                upcomingTasks!!.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        upcomingTasks!!.forEach { task ->
                            val daysUntil = viewModel.calculateDaysUntil(task.scheduledDateTime)
                            TaskAlert(
                                task = task,
                                daysUntil = daysUntil,
                                onClick = { navigateToEditTask(task.id, task.type.name) },
                                darkTheme = darkTheme
                            )
                        }
                    }
                }
                else -> EmptyTasksView(
                    onAddTaskClick = navigateToTask,
                    darkTheme = darkTheme
                )
            }
        }
    }
}

/**
 * Section header component
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        if (actionText != null) {
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Composable for displaying an empty state for the plants carousel.
 */
@Composable
fun EmptyPlantsView(onAddPlantClick: () -> Unit, darkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(220.dp)
            .clickable { onAddPlantClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AddCircleOutline,
                contentDescription = "Add Plant",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your greenhouse is empty!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap here to add your first plant and start tracking its growth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Composable for displaying an empty state for task alerts.
 */
@Composable
fun EmptyTasksView(onAddTaskClick: () -> Unit, darkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(160.dp)
            .clickable { onAddTaskClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = "No Tasks",
                modifier = Modifier.size(48.dp),
                tint = if (darkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No upcoming tasks",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap here to schedule a new task and keep your plants healthy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
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
        // Plant image - circular shape
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Plant name
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
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
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel: DashboardViewModel = viewModel()
    val plantNameCache by viewModel.plantNameCache.collectAsState()
    
    // Define card colors based on urgency and theme
    val cardColor = if (darkTheme) {
        // Dark theme cards
        if (daysUntil == 0L) PrimaryGreen.copy(alpha = 0.2f) else DarkSurface
    } else {
        // Light theme cards - more distinctive
        if (daysUntil == 0L) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    
    // Title colors
    val titleColor = if (darkTheme) {
        // Dark theme text
        if (daysUntil == 0L) PrimaryGreen else TextWhite
    } else {
        // Light theme text
        if (daysUntil == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
    }
    
    // Description colors
    val descriptionColor = if (darkTheme) {
        TextWhite.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (darkTheme) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Task title
            Text(
                text = task.type.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            
            // Task description if available
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Task due date indicator with enhanced UI
            val daysText = when (daysUntil) {
                0L -> "Today"
                1L -> "Tomorrow"
                else -> "In $daysUntil days"
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (daysUntil == 0L) {
                                if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                            } else {
                                if (darkTheme) TextWhite.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            }
                        )
                )
                
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysUntil == 0L) {
                        if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                    } else {
                        if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    },
                    fontWeight = if (daysUntil == 0L) FontWeight.SemiBold else FontWeight.Normal
                )
                
                if (task.plantId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val plantName = plantNameCache[task.plantId] ?: "Unknown plant"
                    Text(
                        text = "For: $plantName",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 