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
import androidx.compose.material.icons.filled.Delete
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
    onEditTask: (Task) -> Unit
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
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            when {
                isLoading || tasks == null -> TaskListSkeleton()
                tasks!!.isEmpty() -> EmptyTaskListView(onAddTaskClick = onNavigateBack)
                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks!!, key = { task -> task.id }) { task ->
                            TaskListItem(
                                task = task,
                                dateFormatter = dateFormatter,
                                onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                onEdit = { onEditTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTaskListView(onAddTaskClick: () -> Unit) {
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
            tint = PrimaryGreen.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Your Task List is Empty",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ready to get organized? Add your first task and keep your greenhouse thriving.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = TextWhite
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
    onEdit: () -> Unit
) {
    val viewModel: TaskViewModel = viewModel()
    val plantNameCache by viewModel.plantNameCache.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                PrimaryGreen.copy(alpha = 0.1f) 
            else 
                DarkSurface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = task.type.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled:",
                    fontSize = 14.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = task.scheduledDateTime.format(dateFormatter),
                    fontSize = 14.sp,
                    color = TextWhite.copy(alpha = 0.8f)
                )
                
                if (task.plantId != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    val plantName = plantNameCache[task.plantId] ?: "Unknown plant"
                    Text(
                        text = "Plant: $plantName",
                        fontSize = 14.sp,
                        color = TextGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Notes:",
                    fontSize = 14.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = TextWhite.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = TextWhite.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    isCompleted = task.isCompleted,
                    completedDate = task.completedDateTime?.format(dateFormatter)
                )
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onToggleComplete,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (task.isCompleted) 
                                Color(0xFFF9A825).copy(alpha = 0.2f) 
                            else 
                                PrimaryGreen.copy(alpha = 0.2f),
                            contentColor = if (task.isCompleted) 
                                Color(0xFFF9A825) 
                            else 
                                PrimaryGreen
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            if (task.isCompleted) "Mark Incomplete" else "Mark Complete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit Task", 
                            tint = PrimaryGreen.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete Task", 
                            tint = TextGrey,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    isCompleted: Boolean,
    completedDate: String? = null
) {
    Surface(
        color = if (isCompleted) PrimaryGreen.copy(alpha = 0.15f) else Color(0xFFF9A825).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isCompleted) PrimaryGreen else Color(0xFFF9A825),
                        CircleShape
                    )
            )
            Column {
                Text(
                    text = if (isCompleted) "Completed" else "Pending",
                    fontSize = 12.sp,
                    color = if (isCompleted) PrimaryGreen else Color(0xFFF9A825),
                    fontWeight = FontWeight.SemiBold
                )
                if (isCompleted && completedDate != null) {
                    Text(
                        text = completedDate,
                        fontSize = 10.sp,
                        color = TextGrey
                    )
                }
            }
        }
    }
} 