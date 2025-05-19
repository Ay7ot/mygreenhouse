package com.example.mygreenhouse.ui.screens.dankbank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HarvestListItem(
    harvest: Harvest,
    onHarvestClick: (Harvest) -> Unit,
    onEdit: (Harvest) -> Unit,
    onDelete: (Harvest) -> Unit,
    onMarkDryClick: (Harvest) -> Unit,
    onMarkCuredClick: (Harvest) -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    // Menu state
    var showMenu by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    
    // Card color based on stage
    val cardColor = when {
        harvest.isCompleted -> if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        harvest.isCuring -> if (darkTheme) PrimaryGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        harvest.isDrying -> if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else -> if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    
    // Status text based on stage
    val statusText = when {
        harvest.isCompleted -> "Completed"
        harvest.isCuring -> "Curing"
        harvest.isDrying -> "Drying"
        else -> "Processing"
    }
    
    // Status color based on stage
    val statusColor = when {
        harvest.isCompleted -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
        harvest.isCuring -> if (darkTheme) PrimaryGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        harvest.isDrying -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onHarvestClick(harvest) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = harvest.strainName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Batch #${harvest.batchNumber}",
                        fontSize = 14.sp,
                        color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Menu button with dropdown
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onEdit(harvest)
                                showMenu = false
                            },
                            modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                        )
                        
                        if (harvest.isDrying) {
                            DropdownMenuItem(
                                text = { Text("Enter Dry Weight", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    onMarkDryClick(harvest)
                                    showMenu = false
                                },
                                modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                            )
                        }
                        
                        if (harvest.isCuring) {
                            DropdownMenuItem(
                                text = { Text("Enter Cured Weight", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    onMarkCuredClick(harvest)
                                    showMenu = false
                                },
                                modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("Delete", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onDelete(harvest)
                                showMenu = false
                            },
                            modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Harvested: ${harvest.harvestDate.format(dateFormatter)}",
                    fontSize = 14.sp,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Weight information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wet weight
                harvest.wetWeight?.let {
                    WeightInfoChip(
                        label = "Wet",
                        weight = it,
                        isHighlighted = harvest.isDrying,
                        darkTheme = darkTheme
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Dry weight
                harvest.dryWeight?.let {
                    WeightInfoChip(
                        label = "Dry",
                        weight = it,
                        isHighlighted = harvest.isCuring,
                        darkTheme = darkTheme
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Final cured weight
                harvest.finalCuredWeight?.let {
                    WeightInfoChip(
                        label = "Final",
                        weight = it,
                        isHighlighted = harvest.isCompleted,
                        darkTheme = darkTheme
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Quality rating if available
            if (harvest.isCompleted && harvest.qualityRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quality: ${harvest.qualityRating}/5",
                    fontSize = 14.sp,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Notes if available
            if (harvest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = harvest.notes,
                    fontSize = 14.sp,
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WeightInfoChip(
    label: String,
    weight: Double,
    isHighlighted: Boolean = false,
    darkTheme: Boolean = true
) {
    val backgroundColor = if (isHighlighted) {
        if (darkTheme) PrimaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    
    val textColor = if (isHighlighted) {
        if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
    } else {
        if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: ${String.format("%.1f", weight)}g",
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
} 