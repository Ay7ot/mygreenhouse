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
import androidx.compose.ui.graphics.Color
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
    navController: NavController
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
                        if (seed != null) {
                            onNavigateToEdit(seedId)
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
        } else if (seed == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("Seed not found", color = TextWhite)
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
                // Count and type card
                SeedTypeCard(seed!!)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Basic info
                DetailCard(
                    title = "Seed Information",
                    content = {
                        DetailRow("Strain Name", seed!!.strainName)
                        DetailRow("Batch Number", "#${seed!!.batchNumber}")
                        DetailRow("Seed Count", seed!!.seedCount.toString())
                        DetailRow("Acquisition Date", seed!!.acquisitionDate.format(dateFormatter))
                        
                        if (seed!!.breeder.isNotBlank()) {
                            DetailRow("Breeder", seed!!.breeder)
                        }
                        
                        if (seed!!.source.isNotBlank()) {
                            DetailRow("Source", seed!!.source)
                        }
                    }
                )
                
                if (seed!!.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    DetailCard(
                        title = "Notes",
                        content = {
                            Text(
                                text = seed!!.notes,
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
            title = { Text("Delete Seed Entry", color = TextWhite) },
            text = { 
                Text(
                    "Are you sure you want to delete this seed entry? This action cannot be undone.",
                    color = TextGrey
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
                        containerColor = Color.Red.copy(alpha = 0.8f)
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
fun SeedTypeCard(seed: Seed) {
    val seedTypeColor = when (seed.seedType) {
        SeedType.FEMINIZED -> Color(0xFFE57373) // Light red
        SeedType.AUTOFLOWER -> Color(0xFF64B5F6) // Light blue
        SeedType.REGULAR -> Color(0xFF81C784) // Light green
    }
    
    val seedTypeName = seed.seedType.name.lowercase().replaceFirstChar { it.uppercase() }
    
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
                    .background(seedTypeColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = seedTypeName,
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "${seed.seedCount} seeds",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
} 