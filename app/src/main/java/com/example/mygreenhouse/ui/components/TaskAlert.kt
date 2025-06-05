package com.example.mygreenhouse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.ui.screens.dashboard.DashboardViewModel
import com.example.mygreenhouse.ui.screens.dashboard.TaskStatus
import com.example.mygreenhouse.ui.screens.task.TaskViewModel
import com.example.mygreenhouse.ui.screens.task.displayName
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite

/**
 * Task alert component with enhanced styling and visual cues
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
    val taskViewModel: TaskViewModel = viewModel()
    val plantNameCache by viewModel.plantNameCache.collectAsState()
    
    // Calculate the relevant date to display (either next occurrence or original scheduled date)
    val displayDate = viewModel.getDisplayDate(task)
    val taskStatus = viewModel.getTaskStatusForDate(task, displayDate)
    val isCompleted = viewModel.isDateCompleted(task, displayDate)
    val isOverdue = taskStatus == TaskStatus.OVERDUE
    val isDueToday = taskStatus == TaskStatus.DUE_TODAY
    
    // Calculate days until for the display date
    val today = java.time.LocalDate.now()
    val actualDaysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, displayDate)
    
    // Define card colors based on urgency and theme
    val cardColor = if (darkTheme) {
        when {
            isCompleted -> PrimaryGreen.copy(alpha = 0.15f)
            isOverdue -> Color(0xFFFF5722).copy(alpha = 0.2f)
            isDueToday -> PrimaryGreen.copy(alpha = 0.2f)
            else -> DarkSurface
        }
    } else {
        when {
            isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            isDueToday -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        }
    }
    
    // Title colors
    val titleColor = if (darkTheme) {
        when {
            isCompleted -> PrimaryGreen.copy(alpha = 0.7f)
            isOverdue -> Color(0xFFFF7043)
            isDueToday -> PrimaryGreen
            else -> TextWhite
        }
    } else {
        when {
            isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            isOverdue -> MaterialTheme.colorScheme.error
            isDueToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onBackground
        }
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
            // Task title with completion button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.type.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Enhanced completion toggle button
                IconButton(
                    onClick = { taskViewModel.toggleDateCompletion(task, displayDate) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Default.CheckCircle
                            isOverdue -> Icons.Default.Warning  
                            else -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = when {
                            isCompleted -> "Completed"
                            isOverdue -> "Overdue"
                            else -> "Pending"
                        },
                        tint = when {
                            isCompleted -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                            isOverdue -> if (darkTheme) Color(0xFFFF7043) else MaterialTheme.colorScheme.error
                            else -> if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Task description if available
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) descriptionColor.copy(alpha = 0.6f) else descriptionColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Task due date indicator
            val daysText = when {
                isCompleted -> "✅ Completed"
                isOverdue -> {
                    val daysPast = kotlin.math.abs(actualDaysUntil)
                    when (daysPast) {
                        0L -> "🚨 Overdue - Today"
                        1L -> "⚠️ Overdue - 1 day ago" 
                        else -> "⚠️ Overdue - $daysPast days ago"
                    }
                }
                actualDaysUntil == 0L -> "🎯 Due Today"
                actualDaysUntil == 1L -> "📅 Tomorrow"
                else -> "📅 In $actualDaysUntil days"
            }
            
            // Status section
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when {
                                    isCompleted -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                                    isOverdue -> if (darkTheme) Color(0xFFFF7043) else MaterialTheme.colorScheme.error
                                    isDueToday -> if (darkTheme) Color(0xFFFFA726) else MaterialTheme.colorScheme.tertiary
                                    else -> if (darkTheme) TextWhite.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                },
                                shape = CircleShape
                            )
                    )
                    
                    Text(
                        text = daysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isCompleted -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                            isOverdue -> if (darkTheme) Color(0xFFFF7043) else MaterialTheme.colorScheme.error
                            isDueToday -> if (darkTheme) Color(0xFFFFA726) else MaterialTheme.colorScheme.tertiary
                            else -> if (darkTheme) TextWhite.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onBackground
                        },
                        fontWeight = when {
                            isOverdue -> FontWeight.Bold
                            isDueToday -> FontWeight.SemiBold
                            else -> FontWeight.Medium
                        },
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                
                // Recurring pattern info
                if (task.repeatDays.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (darkTheme) TextWhite.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "🔄 Repeats: ${task.repeatDays.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                
                // Plant association info
                if (task.plantId != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (darkTheme) PrimaryGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                        )
                        val plantName = plantNameCache[task.plantId] ?: "Unknown plant"
                        Text(
                            text = "🌱 For: $plantName",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }
    }
} 