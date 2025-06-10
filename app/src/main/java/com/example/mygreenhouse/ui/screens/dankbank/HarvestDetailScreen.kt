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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
    navController: NavController,
    darkTheme: Boolean
) {
    var harvest by remember { mutableStateOf<Harvest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var associatedPlantName by remember { mutableStateOf<String?>(null) }
    
    // Add dialog states for weight updates
    var showDryWeightDialog by remember { mutableStateOf(false) }
    var showRateStrainDialog by remember { mutableStateOf(false) }
    
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
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
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
                actions = {
                    IconButton(onClick = { 
                        if (harvest != null) {
                            onNavigateToEdit(harvestId)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
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
                currentRoute = NavDestination.DankBank.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                )
            }
        } else if (harvest == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Harvest not found", 
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Status indicator
                StatusCard(harvest!!, darkTheme)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Basic info
                DetailCard(
                    title = "Harvest Information",
                    content = {
                        DetailRow("Strain Name", harvest!!.strainName, darkTheme)
                        DetailRow("Batch Number", "#${harvest!!.batchNumber}", darkTheme)
                        DetailRow("Harvest Date", harvest!!.harvestDate.format(dateFormatter), darkTheme)
                        
                        if (associatedPlantName != null) {
                            DetailRow("From Plant", associatedPlantName!!, darkTheme)
                        }
                    },
                    darkTheme = darkTheme
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weight information
                DetailCard(
                    title = "Weight Information",
                    content = {
                        harvest!!.wetWeight?.let {
                            DetailRow("Wet Weight", "${String.format("%.1f", it)} grams", darkTheme)
                        }
                        
                        harvest!!.dryWeight?.let {
                            DetailRow("Dry Weight", "${String.format("%.1f", it)} grams", darkTheme)
                            if (harvest!!.dryingCompleteDate != null) {
                                DetailRow("Drying Completed", harvest!!.dryingCompleteDate!!.format(dateFormatter), darkTheme)
                            }
                        }
                        
                        harvest!!.finalCuredWeight?.let {
                            DetailRow("Final Cured Weight", "${String.format("%.1f", it)} grams", darkTheme)
                            if (harvest!!.curingCompleteDate != null) {
                                DetailRow("Curing Completed", harvest!!.curingCompleteDate!!.format(dateFormatter), darkTheme)
                            }
                        }
                        
                        harvest!!.qualityRating?.let {
                            DetailRow("Quality Rating", "$it/5", darkTheme)
                        }
                        
                        if (harvest!!.wetWeight != null && harvest!!.finalCuredWeight != null) {
                            val yieldPercentage = (harvest!!.finalCuredWeight!! / harvest!!.wetWeight!!) * 100
                            DetailRow("Yield Percentage", "${String.format("%.1f", yieldPercentage)}%", darkTheme)
                        }
                    },
                    darkTheme = darkTheme
                )
                
                if (harvest!!.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    DetailCard(
                        title = "Notes",
                        content = {
                            Text(
                                text = harvest!!.notes,
                                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        darkTheme = darkTheme
                    )
                }
                
                // Add action buttons based on the current state of the harvest
                if (harvest!!.isDrying) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showDryWeightDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update Dry Weight")
                    }
                } else if (harvest!!.isCuring) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showRateStrainDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rate This Strain")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Delete Harvest", 
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete this harvest? This action cannot be undone.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text(
                        "Cancel", 
                        color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
        )
    }
    
    // Add Dry Weight Dialog
    if (showDryWeightDialog && harvest != null) {
        DryWeightInputDialog(
            harvest = harvest!!,
            onDismiss = { showDryWeightDialog = false },
            onConfirm = { dryWeight: Double ->
                viewModel.updateHarvestWithDryWeight(harvest!!.id, dryWeight)
                showDryWeightDialog = false
            },
            darkTheme = darkTheme
        )
    }
    
    // Add Rate Strain Dialog
    if (showRateStrainDialog && harvest != null) {
        RateStrainDialog(
            harvest = harvest!!,
            onDismiss = { showRateStrainDialog = false },
            onConfirm = { qualityRating: Int ->
                viewModel.rateStrain(harvest!!.id, qualityRating)
                showRateStrainDialog = false
            },
            darkTheme = darkTheme
        )
    }
}

@Composable
fun StatusCard(harvest: Harvest, darkTheme: Boolean) {
    val statusText = when {
        harvest.isCompleted -> "Completed"
        harvest.isCuring -> "Curing"
        harvest.isDrying -> "Drying"
        else -> "Processing"
    }
    
    val statusColor = when {
        harvest.isCompleted -> if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
        harvest.isCuring -> if (darkTheme) PrimaryGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        harvest.isDrying -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.outline
        else -> if (darkTheme) TextGrey else MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant
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
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
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
    content: @Composable () -> Unit,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant
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
                color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, darkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(140.dp)
        )
        
        Text(
            text = value,
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateStrainDialog(
    harvest: Harvest,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    darkTheme: Boolean
) {
    var qualityRatingInput by remember { mutableStateOf(harvest.qualityRating?.toString() ?: "") }

    val isRatingError = qualityRatingInput.toIntOrNull() == null && qualityRatingInput.isNotEmpty() ||
            (qualityRatingInput.toIntOrNull() != null && (qualityRatingInput.toInt() < 1 || qualityRatingInput.toInt() > 5))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Rate This Strain", 
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Rate the quality of ${harvest.strainName} (Batch #${harvest.batchNumber}) from 1 to 5 stars.", 
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = qualityRatingInput,
                    onValueChange = { qualityRatingInput = it },
                    label = { 
                        Text(
                            "Quality Rating (1-5)",
                            color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    singleLine = true,
                    isError = isRatingError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorIndicatorColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    )
                )
                if (isRatingError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Rating must be a number between 1 and 5", 
                        color = MaterialTheme.colorScheme.error, 
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    qualityRatingInput.toIntOrNull()?.let { rating ->
                        if (rating in 1..5) {
                            onConfirm(rating)
                        }
                    }
                },
                enabled = !isRatingError && qualityRatingInput.toIntOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Rate Strain")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel", 
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface,
        titleContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
        textContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

 