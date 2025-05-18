package com.example.mygreenhouse.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mygreenhouse.ui.theme.DarkSurface

/**
 * Creates a shimmer brush for skeleton loading animations
 */
@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            DarkSurface.copy(alpha = 0.6f),
            DarkSurface.copy(alpha = 0.2f),
            DarkSurface.copy(alpha = 0.6f),
        )
        
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer animation"
        )
        
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(0f, 0f),
            end = Offset(translateAnimation, translateAnimation)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(0f, 0f)
        )
    }
}

/**
 * Skeleton loader for plant carousel
 */
@Composable
fun PlantCarouselSkeleton() {
    val brush = shimmerBrush()
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(4) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Plant image placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Plant name placeholder
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Skeleton loader for task alerts
 */
@Composable
fun TaskAlertsSkeleton() {
    val brush = shimmerBrush()
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Task title placeholder
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Task description placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Task date placeholder
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Skeleton loader for task list
 */
@Composable
fun TaskListSkeleton() {
    val brush = shimmerBrush()
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Task title placeholder
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Task date placeholder
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Task notes placeholder
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status and buttons area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(brush)
                        )
                        
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                            )
                        }
                    }
                }
            }
        }
    }
} 