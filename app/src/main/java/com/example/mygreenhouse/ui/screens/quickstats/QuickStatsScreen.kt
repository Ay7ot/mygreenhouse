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
import androidx.compose.material.icons.filled.Inventory2
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
import com.example.mygreenhouse.ui.screens.addplant.DropdownMenuField
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickStatsScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: QuickStatsViewModel = viewModel(factory = QuickStatsViewModel.Factory),
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Stats", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
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
                currentRoute = NavDestination.QuickStats.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
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
                        modifier = Modifier.weight(1f),
                        darkTheme = darkTheme
                    )
                    StatCard(
                        title = "Drying",
                        value = uiState.dryingCount.toString(),
                        icon = Icons.Default.Spa,
                        modifier = Modifier.weight(1f),
                        darkTheme = darkTheme
                    )
                    StatCard(
                        title = "Curing",
                        value = uiState.curingCount.toString(),
                        icon = Icons.Filled.Inventory2,
                        modifier = Modifier.weight(1f),
                        darkTheme = darkTheme
                    )
                }
                
                // Growth Stage Distribution
                val totalPlantsForDisplay = uiState.totalActivePlants + uiState.dryingCount + uiState.curingCount
                if (totalPlantsForDisplay > 0) {
                    Text(
                        text = "Plants by Growth Stage",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
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
                                        percentage = percentage,
                                        darkTheme = darkTheme
                                    )
                                }
                            }
                        }
                    }
                    
                    // Average Days in Growth Stage Section
                    if (uiState.totalActivePlants > 0) { // Conditionally display this whole section
                        Text(
                            text = "Average Days in Growth Stage",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Strain Name Dropdown
                                DropdownMenuField(
                                    label = "Select Strain",
                                    selectedValue = uiState.selectedStrain,
                                    options = uiState.strainNameOptions,
                                    onOptionSelected = { viewModel.updateSelectedStrain(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Separator

                                // Filter out drying and curing stages before checking if data exists
                                val filteredAverageDaysData = uiState.averageDaysInStage.filterKeys { 
                                    it !in listOf(GrowthStage.DRYING, GrowthStage.CURING)
                                }
                                
                                if (filteredAverageDaysData.any { it.value > 0f }) {
                                    AverageDaysInStageBarChart(
                                        averageDaysData = filteredAverageDaysData,
                                        darkTheme = darkTheme
                                    )
                                } else {
                                    Text(
                                        text = "Data Unavailable until at least one strain has been moved from one growth stage into another!",
                                        color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Add some space at the bottom
                } else {
                    // No plants yet
                    EmptyStatsView(darkTheme = darkTheme)
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
    darkTheme: Boolean
) {
    val lineColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
    val gridColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.1f)
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
                        color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
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
                    color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
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
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
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
                tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StageProgressBar(
    stage: GrowthStage,
    count: Int,
    percentage: Int,
    darkTheme: Boolean
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
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$count plants ($percentage%)",
                fontSize = 14.sp,
                color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
            trackColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        )
    }
}

@Composable
fun EmptyStatsView(darkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
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
                tint = (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Plants Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add plants to your greenhouse to see statistics and growth trends.",
                fontSize = 14.sp,
                color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
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

@Composable
fun AverageDaysInStageBarChart(
    averageDaysData: Map<GrowthStage, Float>,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Show all relevant stages (excluding DRYING and CURING) even if they have 0 data
    val allRelevantStages = listOf(
        GrowthStage.GERMINATION,
        GrowthStage.SEEDLING, 
        GrowthStage.NON_ROOTED,
        GrowthStage.ROOTED,
        GrowthStage.VEGETATION,
        GrowthStage.FLOWER
    )
    
    // Create a map with all stages, using 0f for stages without data
    val completeStageData = allRelevantStages.associateWith { stage ->
        averageDaysData[stage] ?: 0f
    }
    
    // Check if there's any data at all
    val hasAnyData = completeStageData.values.any { it > 0f }
    if (!hasAnyData) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No average day data to display for selected strain and stages.",
                color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val maxValue = completeStageData.values.maxOrNull() ?: 1f
    val barColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
    val emptyBarColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val labelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val gridColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.2f)
    val yAxisLabelColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.6f)
    val yAxisLabelPadding = 30.dp

    Column(modifier = modifier
        .fillMaxWidth()
        .height(280.dp)) { 
        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            // Y-axis Labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .width(yAxisLabelPadding),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                val yGridLines = 5
                for (i in yGridLines downTo 0) {
                    Text(
                        text = "${(maxValue * i / yGridLines).toInt()}", 
                        fontSize = 10.sp, 
                        color = yAxisLabelColor,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Chart Area
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 8.dp)) { 
                val barCount = completeStageData.size
                if (barCount == 0) return@Canvas

                val barWidthRatio = 0.6f
                val totalBarAndSpacingWidth = size.width / barCount
                val barWidth = totalBarAndSpacingWidth * barWidthRatio
                val spacing = totalBarAndSpacingWidth * (1 - barWidthRatio)
                val yGridLines = 5

                // Draw Y-axis grid lines
                for (i in 0..yGridLines) {
                    val yPos = size.height * (i.toFloat() / yGridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, size.height - yPos), 
                        end = Offset(size.width, size.height - yPos),
                        strokeWidth = 1f
                    )
                }

                // Draw bars for all stages
                completeStageData.entries.toList().forEachIndexed { index, (stage, avgDays) ->
                    val xOffset = index * totalBarAndSpacingWidth + spacing / 2
                    
                    if (avgDays > 0f) {
                        // Draw bar with data
                        val barHeight = (avgDays / maxValue) * size.height
                        drawRect(
                            color = barColor,
                            topLeft = Offset(xOffset, size.height - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    } else {
                        // Draw empty bar placeholder (small height to show it exists)
                        val minBarHeight = 2f
                        drawRect(
                            color = emptyBarColor,
                            topLeft = Offset(xOffset, size.height - minBarHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, minBarHeight)
                        )
                    }
                }
            }
        }
        // X-axis labels for ALL stages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = yAxisLabelPadding + 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            completeStageData.keys.forEach { stage ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (stage) {
                        GrowthStage.GERMINATION -> {
                            Text(
                                text = "GERM",
                                fontSize = 10.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        GrowthStage.SEEDLING -> {
                            Text(
                                text = "SEEDLING",
                                fontSize = 9.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        GrowthStage.NON_ROOTED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Non",
                                    fontSize = 9.sp,
                                    color = labelColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Rooted",
                                    fontSize = 9.sp,
                                    color = labelColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        GrowthStage.ROOTED -> {
                            Text(
                                text = "Rooted",
                                fontSize = 10.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        GrowthStage.VEGETATION -> {
                            Text(
                                text = "Veg",
                                fontSize = 10.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        GrowthStage.FLOWER -> {
                            Text(
                                text = "Flower",
                                fontSize = 9.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(
                                text = formatGrowthStageName(stage).take(3).uppercase(),
                                fontSize = 10.sp,
                                color = labelColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
} 