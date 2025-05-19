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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.SeedType
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
fun SeedDetailScreen(
    seedId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteSeed: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    var seed by remember { mutableStateOf<Seed?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Fetch seed data
    LaunchedEffect(seedId) {
        val fetchedSeed = viewModel.getSeedById(seedId).firstOrNull()
        seed = fetchedSeed
        isLoading = false
    }
    
    // Date formatter
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = seed?.strainName ?: "Seed Details", 
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
                        if (seed != null) {
                            onNavigateToEdit(seedId)
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
        } else if (seed == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Seed not found", 
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
                // Count and type card
                SeedTypeCard(seed!!, darkTheme)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Basic info
                DetailCard(
                    title = "Seed Information",
                    content = {
                        DetailRow("Strain Name", seed!!.strainName, darkTheme)
                        DetailRow("Batch Number", "#${seed!!.batchNumber}", darkTheme)
                        DetailRow("Seed Count", seed!!.seedCount.toString(), darkTheme)
                        DetailRow("Acquisition Date", seed!!.acquisitionDate.format(dateFormatter), darkTheme)
                        
                        if (seed!!.breeder.isNotBlank()) {
                            DetailRow("Breeder", seed!!.breeder, darkTheme)
                        }
                        
                        if (seed!!.source.isNotBlank()) {
                            DetailRow("Source", seed!!.source, darkTheme)
                        }
                    },
                    darkTheme = darkTheme
                )
                
                if (seed!!.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    DetailCard(
                        title = "Notes",
                        content = {
                            Text(
                                text = seed!!.notes,
                                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        darkTheme = darkTheme
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
            title = { 
                Text(
                    "Delete Seed Entry", 
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete this seed entry? This action cannot be undone.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (seed != null) {
                            viewModel.deleteSeed(seed!!)
                            showDeleteDialog = false
                            onDeleteSeed()
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
}

@Composable
fun SeedTypeCard(seed: Seed, darkTheme: Boolean) {
    val seedTypeColor = when (seed.seedType) {
        SeedType.FEMINIZED -> if (darkTheme) androidx.compose.ui.graphics.Color(0xFFE57373) else androidx.compose.ui.graphics.Color(0xFFE57373).copy(alpha = 0.8f) // Light red
        SeedType.AUTOFLOWER -> if (darkTheme) androidx.compose.ui.graphics.Color(0xFF64B5F6) else androidx.compose.ui.graphics.Color(0xFF64B5F6).copy(alpha = 0.8f) // Light blue
        SeedType.REGULAR -> if (darkTheme) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color(0xFF81C784).copy(alpha = 0.8f) // Light green
    }
    
    val seedTypeName = seed.seedType.name.lowercase().replaceFirstChar { it.uppercase() }
    
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
                    .background(seedTypeColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = seedTypeName,
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "${seed.seedCount} seeds",
                color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
} 