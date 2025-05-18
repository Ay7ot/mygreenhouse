package com.example.mygreenhouse.ui.screens.allplants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Spa // Placeholder for plant icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPlantsScreen(
    viewModel: AllPlantsViewModel = viewModel(factory = AllPlantsViewModel.Factory),
    onNavigateBack: () -> Unit,
    onEditPlant: (String) -> Unit
) {
    val plants by viewModel.allPlants.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var plantToDelete by remember { mutableStateOf<Plant?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Plants", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            if (plants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No plants in your greenhouse yet.", 
                        color = TextGrey, 
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plants, key = { it.id }) { plant ->
                        PlantListItem(plant = plant, onEdit = {
                            onEditPlant(plant.id)
                        }, onDelete = {
                            plantToDelete = plant
                            showDeleteConfirmationDialog = true
                        })
                    }
                }
            }
        }
    }

    if (showDeleteConfirmationDialog && plantToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirm Deletion", color = TextWhite) },
            text = { Text("Are you sure you want to delete '${plantToDelete!!.strainName}'? This action cannot be undone.", color = TextGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        plantToDelete?.let { viewModel.deletePlant(it) }
                        showDeleteConfirmationDialog = false
                        plantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel", color = PrimaryGreen)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun PlantListItem(
    plant: Plant,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (plant.imagePath != null) {
                    AsyncImage(
                        model = plant.imagePath,
                        contentDescription = plant.strainName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Spa, // Placeholder icon
                        contentDescription = "Plant Image Placeholder",
                        tint = PrimaryGreen.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.strainName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text("Batch: ${plant.batchNumber}", fontSize = 14.sp, color = TextGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text("Started: ${plant.startDate.format(dateFormatter)}", fontSize = 12.sp, color = TextGrey)
                Text("Stage: ${plant.growthStage.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }}", fontSize = 12.sp, color = TextGrey)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit Plant", tint = PrimaryGreen)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete Plant", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
} 