package com.example.mygreenhouse.ui.screens.dankbank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import kotlinx.coroutines.flow.firstOrNull
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestDetailScreen(
    harvestId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteHarvest: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController
) {
    var harvest by remember { mutableStateOf<Harvest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var associatedPlantName by remember { mutableStateOf<String?>(null) }
    
    // Fetch harvest data
    LaunchedEffect(harvestId) {
        val fetchedHarvest = viewModel.getHarvestById(harvestId).firstOrNull()
        harvest = fetchedHarvest
        
        if (fetchedHarvest?.plantId != null) {
            val plant = viewModel.plantRepository.getPlantById(fetchedHarvest.plantId).firstOrNull()
            associatedPlantName = plant?.strainName
        }
        
        isLoading = false
    }
    
    // Date formatter
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = harvest?.strainName ?: "Harvest Details", 
                        color = TextWhite
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
                actions = {
                    IconButton(onClick = { 
                        if (harvest != null) {
                            onNavigateToEdit(harvestId)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextWhite
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
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
                currentRoute = NavDestination.DankBank.route,
                navController = navController
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (harvest == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("Harvest not found", color = TextWhite)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Status indicator
                StatusCard(harvest!!)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Basic info
                DetailCard(
                    title = "Harvest Information",
                    content = {
                        DetailRow("Strain Name", harvest!!.strainName)
                        DetailRow("Batch Number", "#${harvest!!.batchNumber}")
                        DetailRow("Harvest Date", harvest!!.harvestDate.format(dateFormatter))
                        
                        if (associatedPlantName != null) {
                            DetailRow("From Plant", associatedPlantName!!)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weight information
                DetailCard(
                    title = "Weight Information",
                    content = {
                        harvest!!.wetWeight?.let {
                            DetailRow("Wet Weight", "${String.format("%.1f", it)} grams")
                        }
                        
                        harvest!!.dryWeight?.let {
                            DetailRow("Dry Weight", "${String.format("%.1f", it)} grams")
                            if (harvest!!.dryingCompleteDate != null) {
                                DetailRow("Drying Completed", harvest!!.dryingCompleteDate!!.format(dateFormatter))
                            }
                        }
                        
                        harvest!!.finalCuredWeight?.let {
                            DetailRow("Final Cured Weight", "${String.format("%.1f", it)} grams")
                            if (harvest!!.curingCompleteDate != null) {
                                DetailRow("Curing Completed", harvest!!.curingCompleteDate!!.format(dateFormatter))
                            }
                        }
                        
                        harvest!!.qualityRating?.let {
                            DetailRow("Quality Rating", "$it/5")
                        }
                        
                        if (harvest!!.wetWeight != null && harvest!!.finalCuredWeight != null) {
                            val yieldPercentage = (harvest!!.finalCuredWeight!! / harvest!!.wetWeight!!) * 100
                            DetailRow("Yield Percentage", "${String.format("%.1f", yieldPercentage)}%")
                        }
                    }
                )
                
                if (harvest!!.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    DetailCard(
                        title = "Notes",
                        content = {
                            Text(
                                text = harvest!!.notes,
                                color = TextWhite,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Harvest", color = TextWhite) },
            text = { 
                Text(
                    "Are you sure you want to delete this harvest? This action cannot be undone.",
                    color = TextGrey
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (harvest != null) {
                            viewModel.deleteHarvest(harvest!!)
                            showDeleteDialog = false
                            onDeleteHarvest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextGrey)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun StatusCard(harvest: Harvest) {
    val statusText = when {
        harvest.isCompleted -> "Completed"
        harvest.isCuring -> "Curing"
        harvest.isDrying -> "Drying"
        else -> "Processing"
    }
    
    val statusColor = when {
        harvest.isCompleted -> PrimaryGreen
        harvest.isCuring -> PrimaryGreen.copy(alpha = 0.8f)
        harvest.isDrying -> TextGrey
        else -> TextGrey
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Status: $statusText",
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = if (harvest.isCompleted) {
                    "Completed Harvest"
                } else {
                    "In Progress"
                },
                color = statusColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            color = TextGrey,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(140.dp)
        )
        
        Text(
            text = value,
            color = TextWhite,
            fontWeight = FontWeight.Medium
        )
    }
} 