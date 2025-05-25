package com.example.mygreenhouse.ui.screens.addplant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.PlantSource
import com.example.mygreenhouse.data.model.PlantType
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.PlantGender
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.mygreenhouse.ui.composables.ImagePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog

@Composable
fun myAppTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DropdownMenuField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { }, // Not used, as selection happens via dropdown
            label = { Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = myAppTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Important for ExposedDropdownMenuBox
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant) // Set background for the dropdown menu itself
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant) // Ensure item background matches
                )
            }
        }
    }
}

/**
 * Screen for adding a new plant
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPlantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AddPlantViewModel = viewModel(factory = AddPlantViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Context for date picker
    val context = LocalContext.current
    
    // Date picker dialog
    val datePickerDialog = remember(uiState.startDate) { // Re-remember if initial date changes
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateStartDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            uiState.startDate.year,
            uiState.startDate.monthValue - 1,
            uiState.startDate.dayOfMonth
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add a Plant",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) DarkBackground else MaterialTheme.colorScheme.surface,
                    titleContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.AddPlant.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Strain Name
            OutlinedTextField(
                value = uiState.strainName,
                onValueChange = { viewModel.updateStrainName(it) },
                label = { Text("Enter Strain Name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = myAppTextFieldColors()
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isCustomStrain,
                    onCheckedChange = { viewModel.updateIsCustomStrain(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    text = "Custom Strain",
                    modifier = Modifier.padding(start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            // Batch Number
            OutlinedTextField(
                value = uiState.batchNumber,
                onValueChange = { viewModel.updateBatchNumber(it) },
                label = { Text("Enter Batch Number", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = myAppTextFieldColors()
            )

            // Quantity
            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = { viewModel.updateQuantity(it) },
                label = { Text("Quantity", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = myAppTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Image Picker
            ImagePicker(
                imageUri = uiState.imageUri,
                onImageSelected = { viewModel.updateImageUri(it) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Seed or Clone Dropdown
            DropdownMenuField(
                label = "Seed or Clone",
                selectedValue = uiState.sourceDisplay,
                options = PlantSource.values().map { it.name.lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }, // E.g., "Seed", "Clone"
                onOptionSelected = { selectedString ->
                    val source = PlantSource.values().find { it.name.equals(selectedString, ignoreCase = true) }
                    source?.let { viewModel.updateSource(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )
            
            // Plant Gender Dropdown
            DropdownMenuField(
                label = "Plant Gender",
                selectedValue = uiState.plantGenderDisplay,
                options = viewModel.plantGenderOptions,
                onOptionSelected = { selectedString ->
                    viewModel.updatePlantGender(selectedString)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )
            
            // Plant Type (Autoflower/Photoperiod) Dropdown
            DropdownMenuField(
                label = "Plant Type",
                selectedValue = uiState.plantTypeSelection,
                options = viewModel.plantTypeSelectionOptions,
                onOptionSelected = { selectedString ->
                    viewModel.updatePlantTypeSelection(selectedString)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.source == PlantSource.SEED // Only enable if source is SEED
            )
            
            // Growth Stage Dropdown
            DropdownMenuField(
                label = "Growth Stage",
                selectedValue = uiState.growthStageDisplay,
                options = uiState.availableGrowthStages.map { 
                    it.name.replace("_", " ").lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                }, // Use availableGrowthStages from uiState
                onOptionSelected = { selectedString ->
                    // Find the GrowthStage enum from the display string
                    val stage = uiState.availableGrowthStages.find { 
                        it.name.replace("_", " ").equals(selectedString, ignoreCase = true) 
                    }
                    stage?.let { viewModel.updateGrowthStage(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.source != null // Enable only if a source is selected
            )

            // Display Days in Drying/Curing or Days Until Harvest
            val daysInDrying = uiState.daysInDrying
            val daysInCuring = uiState.daysInCuring
            val daysUntilHarvestLocal = uiState.daysUntilHarvest

            if (uiState.growthStage == GrowthStage.DRYING && daysInDrying != null) {
                Text(
                    text = "Days in Drying: $daysInDrying",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            if (uiState.growthStage == GrowthStage.CURING && daysInCuring != null) {
                Text(
                    text = "Days in Curing: $daysInCuring",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            if (uiState.type == PlantType.AUTOFLOWER && uiState.source == PlantSource.SEED && daysUntilHarvestLocal != null) {
                if (daysUntilHarvestLocal >= 0) {
                    Text(
                        text = "Days Until Harvest: $daysUntilHarvestLocal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = "Harvest was ${-daysUntilHarvestLocal} days ago",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }
            
            // Seed to Harvest / Flower Duration (Conditionally Displayed)
            if (uiState.showDurationField) {
                OutlinedTextField(
                    value = uiState.durationText,
                    onValueChange = { viewModel.updateDurationText(it) },
                    label = { Text(uiState.durationLabel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = myAppTextFieldColors()
                )
            }
            
            // Start Date
            Box( // Outer Box for layout and to contain the TextField and clickable overlay
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.startDateText,
                    onValueChange = { /* Read-only, value updated via dialog */ },
                    label = { Text("Start Date", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(), // TextField takes full width of the Box
                    readOnly = true,
                    enabled = false, // Keep TextField visually disabled and non-interactive
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // Ensure this matches other text
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Box( // Clickable overlay
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePickerDialog.show() } // Show the Android DatePickerDialog
                )
            }
            
            // Nutrients Input Field
            Text("Nutrients", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.currentNutrientInput,
                    onValueChange = { viewModel.updateCurrentNutrientInput(it) },
                    label = { Text("Add Nutrient", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = myAppTextFieldColors()
                )
                IconButton(onClick = { viewModel.addNutrient() }, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Nutrient",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Display Added Nutrients as Chips
            if (uiState.nutrientsList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) 
                ) {
                    uiState.nutrientsList.forEach { nutrient ->
                        InputChip(
                            selected = false, // Not selectable, just for display and removal
                            onClick = { /* Chips are not clickable for selection here */ },
                            label = { Text(nutrient) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $nutrient",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { viewModel.removeNutrient(nutrient) }
                                        .size(InputChipDefaults.IconSize)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = null
                        )
                    }
                }
            }
            
            // Soil Type Dropdown
            DropdownMenuField(
                label = "Grow Medium",
                selectedValue = uiState.growMediumDisplay,
                options = viewModel.growMediumOptions,
                onOptionSelected = { viewModel.updateGrowMedium(it) },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )
            
            // Save Button
            Button(
                onClick = {
                    viewModel.savePlant()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                enabled = uiState.isValid
            ) {
                Text(
                    text = "Save",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // Confirmation Dialog
    if (uiState.showSaveConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissAddAnotherDialog(navigateToDashboard = false) }, // Or handle as cancel
            title = { Text("Plant Saved") },
            text = { Text("Would you like to add another Plant?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.onConfirmAddAnother() 
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.onDismissAddAnotherDialog(navigateToDashboard = true) 
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

    // Effect to handle navigation after "No" is clicked on the dialog
    LaunchedEffect(uiState.plantJustSaved, uiState.showSaveConfirmationDialog) {
        if (uiState.plantJustSaved && !uiState.showSaveConfirmationDialog) {
            onNavigateToDashboard()
            viewModel.resetPlantJustSavedFlag() // Reset flag after navigation
        }
    }
} 