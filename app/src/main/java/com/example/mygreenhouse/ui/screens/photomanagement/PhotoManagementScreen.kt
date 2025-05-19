package com.example.mygreenhouse.ui.screens.photomanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mygreenhouse.data.service.PhotoManagementService.PhotoInfo
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoManagementViewModel = viewModel(factory = PhotoManagementViewModel.Factory),
    darkTheme: Boolean
) {
    val photoState by viewModel.photos.collectAsState()
    val totalPhotoSize by viewModel.totalPhotoSize.collectAsState()
    val selectedPhoto by viewModel.selectedPhoto.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Photo Management",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with stats
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Photo Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Photos: ${photoState.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Total Size: ${viewModel.formatFileSize(totalPhotoSize)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (photoState.isEmpty()) {
                    EmptyPhotosView()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(photoState) { photo ->
                            PhotoItem(
                                photo = photo,
                                onClick = { viewModel.selectPhoto(photo) }
                            )
                        }
                    }
                }
            }
            
            // Photo detail dialog
            if (selectedPhoto != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearSelectedPhoto() },
                    title = { Text("Photo Details") },
                    text = {
                        Column {
                            AsyncImage(
                                model = selectedPhoto?.uri,
                                contentDescription = "Selected photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Filename: ${selectedPhoto?.filename}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Size: ${viewModel.formatFileSize(selectedPhoto?.fileSize ?: 0)}")
                            Spacer(modifier = Modifier.height(8.dp))
                            if (selectedPhoto?.associatedPlantName != null) {
                                Text("Used by plant: ${selectedPhoto?.associatedPlantName}")
                            } else {
                                Text("Not used by any plant")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { viewModel.clearSelectedPhoto() }
                        ) {
                            Text("Close")
                        }
                    }
                )
            }
            
            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Photo") },
                    text = { 
                        Text("Are you sure you want to delete this photo?${if (selectedPhoto?.associatedPlantName != null) "\n\nThis photo is used by plant: ${selectedPhoto?.associatedPlantName}" else ""}")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedPhoto?.let {
                                    viewModel.deletePhoto(it)
                                }
                                showDeleteDialog = false
                                viewModel.clearSelectedPhoto()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: PhotoInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Plant image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            if (photo.associatedPlantName != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        .align(Alignment.BottomCenter)
                        .padding(4.dp)
                ) {
                    Text(
                        text = photo.associatedPlantName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPhotosView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Photos Found",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Photos will appear here when you add them to your plants.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatFileSize(size: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    
    return when {
        size < kb -> "$size B"
        size < mb -> String.format("%.2f KB", size / kb)
        size < gb -> String.format("%.2f MB", size / mb)
        else -> String.format("%.2f GB", size / gb)
    }
} 