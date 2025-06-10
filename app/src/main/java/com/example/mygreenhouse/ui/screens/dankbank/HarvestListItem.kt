package com.example.mygreenhouse.ui.screens.dankbank

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

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
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    
    // Card color based on stage
    val cardColor = when {
        harvest.isCuring -> if (darkTheme) PrimaryGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        harvest.isDrying -> if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else -> if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    
    // Status text based on stage
    val statusText = when {
        harvest.isCuring -> "Curing"
        harvest.isDrying -> "Drying"
        else -> "Processing"
    }
    
    // Status color based on stage
    val statusColor = when {
        harvest.isCuring -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
        harvest.isDrying -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    
    // Calculate days for curing counter
    val daysCuring = if (harvest.isCuring && harvest.dryingCompleteDate != null) {
        ChronoUnit.DAYS.between(harvest.dryingCompleteDate, LocalDate.now()).toInt()
    } else 0
    
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
            // Header with strain name and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Strain name with better text handling like seed distribution
                    Text(
                        text = harvest.strainName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    
                    Text(
                        text = "Batch #${harvest.batchNumber}",
                        fontSize = 14.sp,
                        color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Action buttons based on stage
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    IconButton(
                        onClick = { onEdit(harvest) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { onDelete(harvest) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Harvest date
                Text(
                    text = "Harvested: ${harvest.harvestDate.format(dateFormatter)}",
                    fontSize = 14.sp,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Weight information and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weight information
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    }
                    
                    // Dry weight (final weight)
                harvest.dryWeight?.let {
                    WeightInfoChip(
                            label = "Final",
                        weight = it,
                        isHighlighted = harvest.isCuring,
                        darkTheme = darkTheme
                    )
                    }
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button row based on current stage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    harvest.isDrying -> {
                        // "Move to Curing" button for drying stage
                        Button(
                            onClick = { onMarkDryClick(harvest) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Move to Curing",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    harvest.isCuring -> {
                        // Days curing counter
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) PrimaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Day $daysCuring Curing",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Rate this strain button
                        Button(
                            onClick = { onMarkCuredClick(harvest) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rate Strain",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Quality rating if available
            if (harvest.qualityRating != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) PrimaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quality: ${harvest.qualityRating}/5",
                    fontSize = 14.sp,
                            color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                )
                    }
                }
            }
            
            // Notes if available
            if (harvest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
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