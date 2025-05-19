package com.example.mygreenhouse.ui.screens.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.material3.HorizontalDivider
import com.example.mygreenhouse.ui.components.TaskListSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onEditTask: (Task) -> Unit,
    darkTheme: Boolean
) {
    val tasks by viewModel.allTasks.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Scheduled Tasks", 
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                isLoading || tasks == null -> TaskListSkeleton()
                tasks!!.isEmpty() -> EmptyTaskListView(
                    onAddTaskClick = onNavigateBack,
                    darkTheme = darkTheme
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tasks!!, key = { task -> task.id }) { task ->
                            TaskListItem(
                                task = task,
                                dateFormatter = dateFormatter,
                                onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                onEdit = { onEditTask(task) },
                                darkTheme = darkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTaskListView(onAddTaskClick: () -> Unit, darkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = "No Tasks",
            modifier = Modifier.size(80.dp),
            tint = (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Your Task List is Empty",
            style = MaterialTheme.typography.headlineMedium,
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ready to get organized? Add your first task and keep your greenhouse thriving.",
            style = MaterialTheme.typography.bodyLarge,
            color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Schedule New Task", 
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    dateFormatter: DateTimeFormatter,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    darkTheme: Boolean
) {
    val viewModel: TaskViewModel = viewModel()
    val plantNameCache by viewModel.plantNameCache.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with task type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.type.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Edit icon moved here
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (darkTheme) 
                                DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit Task", 
                            tint = (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                StatusChip(
                    isCompleted = task.isCompleted,
                    completedDate = task.completedDateTime?.format(dateFormatter),
                    darkTheme = darkTheme
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Info row with schedule and plant
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Schedule info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (darkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) 
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.scheduledDateTime.format(dateFormatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface)
                    )
                }
                
                // Plant info if available
                if (task.plantId != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (darkTheme) PrimaryGreen.copy(alpha = 0.7f) 
                                    else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = plantNameCache[task.plantId] ?: "Unknown plant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }
            
            // Description if available
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = (if (darkTheme) TextWhite.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)),
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (task.isCompleted) 
                            if (darkTheme) Color(0xFFF9A825).copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        else 
                            if (darkTheme) PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Close else Icons.Default.Done,
                        contentDescription = if (task.isCompleted) "Mark Incomplete" else "Mark Complete",
                        tint = if (task.isCompleted) 
                            if (darkTheme) Color(0xFFF9A825) else MaterialTheme.colorScheme.onTertiaryContainer
                        else 
                            if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledTonalIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (darkTheme) 
                            DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete Task", 
                        tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    isCompleted: Boolean,
    completedDate: String? = null,
    darkTheme: Boolean
) {
    Surface(
        color = if (isCompleted) 
            if (darkTheme) PrimaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else 
            if (darkTheme) Color(0xFFF9A825).copy(alpha = 0.15f) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isCompleted) 
                            if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                        else 
                            if (darkTheme) Color(0xFFF9A825) else MaterialTheme.colorScheme.tertiary,
                        CircleShape
                    )
            )
            Text(
                text = if (isCompleted) "Completed" else "Pending",
                fontSize = 12.sp,
                color = if (isCompleted) 
                    if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                else 
                    if (darkTheme) Color(0xFFF9A825) else MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 