package com.example.mygreenhouse.ui.screens.quickstats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickStatsScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: QuickStatsViewModel = viewModel(factory = QuickStatsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Stats", color = TextWhite) },
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
                currentRoute = NavDestination.QuickStats.route,
                navController = navController
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Active Plants",
                        value = uiState.totalActivePlants.toString(),
                        icon = Icons.Default.LocalFlorist,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Drying",
                        value = uiState.dryingCount.toString(),
                        icon = Icons.Default.Spa,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Curing",
                        value = uiState.curingCount.toString(),
                        icon = Icons.Default.Healing,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Growth Stage Distribution
                val totalPlantsForDisplay = uiState.totalActivePlants + uiState.dryingCount + uiState.curingCount
                if (totalPlantsForDisplay > 0) {
                    Text(
                        text = "Plants by Growth Stage",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.plantsByStage.forEach { (stage, count) ->
                                if (count > 0) {
                                    val percentage = (count.toFloat() / totalPlantsForDisplay.toFloat() * 100).toInt()
                                    StageProgressBar(
                                        stage = stage,
                                        count = count,
                                        percentage = percentage
                                    )
                                }
                            }
                        }
                    }
                    
                    // Plants Added Over Time (Line Chart)
                    if (uiState.plantsCreatedByMonth.isNotEmpty()) {
                        Text(
                            text = "Growth Trends",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DarkSurface.copy(alpha = 0.7f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Get data points for chart
                                val dataPoints = uiState.plantsCreatedByMonth.values.toList()
                                if (dataPoints.isNotEmpty()) {
                                    SimpleLineChart(
                                        dataPoints = dataPoints,
                                        labels = uiState.plantsCreatedByMonth.keys.map { 
                                            it.split(" ")[0].take(3) // Take first 3 chars of month name
                                        }.toList(),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = "No growth data available yet",
                                        color = TextWhite.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Days in Growth Stage
                    Text(
                        text = "Average Days in Growth Stage",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.averageDaysInStage.entries
                                .filter { (stage, days) -> 
                                    uiState.plantsByStage[stage] ?: 0 > 0 && days > 0 
                                }
                                .forEach { (stage, days) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatGrowthStageName(stage),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "$days days",
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    HorizontalDivider(color = TextWhite.copy(alpha = 0.1f))
                                }
                            
                            if (uiState.averageDaysInStage.entries.none { 
                                (uiState.plantsByStage[it.key] ?: 0) > 0 && it.value > 0 
                            }) {
                                Text(
                                    text = "No data available yet",
                                    color = TextWhite.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // No plants yet
                    EmptyStatsView()
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    dataPoints: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryGreen
) {
    Box(
        modifier = modifier.padding(
            start = 32.dp, // Space for y-axis labels
            end = 16.dp,
            top = 24.dp,
            bottom = 40.dp // Space for x-axis labels
        )
    ) {
        val maxValue = dataPoints.maxOrNull()?.toFloat() ?: 1f
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val xStep = size.width / (dataPoints.size - 1).coerceAtLeast(1)
            val yStep = size.height / maxValue.coerceAtLeast(1f)
            
            // Draw grid lines
            val gridColor = TextWhite.copy(alpha = 0.1f)
            for (i in 0..4) {
                val y = size.height - (i * size.height / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            // Draw data line
            val path = Path()
            if (dataPoints.isNotEmpty()) {
                // Move to first point
                val firstX = 0f
                val firstY = size.height - (dataPoints[0] * yStep)
                path.moveTo(firstX, firstY)
                
                // Connect to other points
                for (i in 1 until dataPoints.size) {
                    val x = i * xStep
                    val y = size.height - (dataPoints[i] * yStep)
                    path.lineTo(x, y)
                }
                
                // Draw the path
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3f)
                )
                
                // Draw points
                for (i in dataPoints.indices) {
                    val x = i * xStep
                    val y = size.height - (dataPoints[i] * yStep)
                    drawCircle(
                        color = lineColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier
                .height(240.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-24).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 5 downTo 0) {
                val value = if (maxValue > 0f) ((maxValue * i) / 5).toInt() else i
                if (i < 5) { // Skip the very top label to avoid overlap
                    Text(
                        text = value.toString(),
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = TextWhite.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextWhite.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StageProgressBar(
    stage: GrowthStage,
    count: Int,
    percentage: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatGrowthStageName(stage),
                fontSize = 14.sp,
                color = TextWhite
            )
            Text(
                text = "$count plants ($percentage%)",
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PrimaryGreen,
            trackColor = DarkSurface.copy(alpha = 0.3f),
        )
    }
}

@Composable
fun EmptyStatsView() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFlorist,
                contentDescription = null,
                tint = PrimaryGreen.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Plants Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add plants to your greenhouse to see statistics and growth trends.",
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Utility function to format growth stage names
fun formatGrowthStageName(stage: GrowthStage): String {
    return stage.name
        .replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
} 