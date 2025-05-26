package com.example.mygreenhouse.ui.screens.dankbank

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.SeedType
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSeedScreen(
    seedId: String,
    onNavigateBack: () -> Unit,
    onSeedUpdated: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    // Form state
    var strainName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var seedCount by remember { mutableStateOf("") }
    var breeder by remember { mutableStateOf("") }
    var selectedSeedType by remember { mutableStateOf(SeedType.AUTOFLOWER_REGULAR) }
    var acquisitionDate by remember { mutableStateOf(LocalDate.now()) }
    var source by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isCustomStrain by remember { mutableStateOf(false) }
    
    // UI state for seed type dropdown
    var isSeedTypeExpanded by remember { mutableStateOf(false) }
    
    // Context for date picker
    val context = LocalContext.current
    
    // Date formatter
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    // Fetch seed data
    LaunchedEffect(seedId) {
        val seed = viewModel.getSeedById(seedId).firstOrNull()
        if (seed != null) {
            strainName = seed.strainName
            batchNumber = seed.batchNumber
            seedCount = seed.seedCount.toString()
            breeder = seed.breeder
            selectedSeedType = seed.seedType
            acquisitionDate = seed.acquisitionDate
            source = seed.source
            notes = seed.notes
            isLoading = false
            isCustomStrain = seed.isCustomStrain
        }
    }
    
    // Form validation
    val isFormValid = !isLoading && 
                      strainName.isNotBlank() && 
                      batchNumber.isNotBlank() && 
                      seedCount.isNotBlank() && 
                      seedCount.toIntOrNull() != null && 
                      seedCount.toIntOrNull() ?: 0 > 0
    
    // Date picker dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                acquisitionDate = LocalDate.of(year, month + 1, day)
            },
            acquisitionDate.year,
            acquisitionDate.monthValue - 1,
            acquisitionDate.dayOfMonth
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Seeds", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
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
                androidx.compose.material3.CircularProgressIndicator(color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Strain name field
                OutlinedTextField(
                    value = strainName,
                    onValueChange = { strainName = it },
                    label = { Text("Strain Name", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCustomStrain,
                        onCheckedChange = { isCustomStrain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            uncheckedColor = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.6f),
                            checkmarkColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        text = "Custom Strain",
                        modifier = Modifier.padding(start = 4.dp),
                        color = (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Batch number field
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seed count field
                OutlinedTextField(
                    value = seedCount,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            seedCount = it
                        }
                    },
                    label = { Text("Seed Count", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Breeder field
                OutlinedTextField(
                    value = breeder,
                    onValueChange = { breeder = it },
                    label = { Text("Breeder (Optional)", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seed Type dropdown
                ExposedDropdownMenuBox(
                    expanded = isSeedTypeExpanded,
                    onExpandedChange = { isSeedTypeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSeedType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Seed Type", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSeedTypeExpanded)
                        },
                        colors = editSeedTextFieldColors(darkTheme = darkTheme),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isSeedTypeExpanded,
                        onDismissRequest = { isSeedTypeExpanded = false },
                        modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        SeedType.values().forEach { seedType ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        seedType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                },
                                onClick = {
                                    selectedSeedType = seedType
                                    isSeedTypeExpanded = false
                                },
                                modifier = Modifier.background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Acquisition date picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = acquisitionDate.format(dateFormatter),
                        onValueChange = { },
                        label = { Text("Acquisition Date", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant,
                            focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            disabledLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Source field
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (Optional)", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editSeedTextFieldColors(darkTheme = darkTheme),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Update button
                Button(
                    onClick = {
                        val updatedSeed = Seed(
                            id = seedId,
                            strainName = strainName,
                            batchNumber = batchNumber,
                            seedCount = seedCount.toIntOrNull() ?: 0,
                            breeder = breeder,
                            seedType = selectedSeedType,
                            acquisitionDate = acquisitionDate,
                            source = source,
                            notes = notes,
                            isCustomStrain = isCustomStrain
                        )
                        viewModel.updateSeed(updatedSeed)
                        onSeedUpdated()
                    },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update Seeds")
                }
            }
        }
    }
}

@Composable
private fun editSeedTextFieldColors(darkTheme: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    unfocusedBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
    focusedContainerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant,
    focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
    focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
) 