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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.PlantSource
import com.example.mygreenhouse.data.model.PlantType
import com.example.mygreenhouse.data.model.GrowthStage
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
            label = { Text(label, color = if (enabled) TextWhite.copy(alpha = 0.8f) else TextWhite.copy(alpha = 0.5f)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    // Custom tint handling for enabled/disabled state
                    // tint = if (enabled) TextWhite.copy(alpha = 0.7f) else TextWhite.copy(alpha = 0.4f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                cursorColor = PrimaryGreen,
                focusedTextColor = TextWhite,
                unfocusedTextColor = if (enabled) TextWhite else TextWhite.copy(alpha = 0.7f),
                disabledTextColor = TextWhite.copy(alpha = 0.7f),
                disabledLabelColor = TextWhite.copy(alpha = 0.5f),
                disabledBorderColor = DarkSurface.copy(alpha = 0.3f),
                // disabledTrailingIconColor = TextWhite.copy(alpha = 0.5f) // Use default handling or customize if needed
                // For ExposedDropdownMenu, the icon color is handled by ExposedDropdownMenuDefaults.TrailingIcon
                // However, we might need to adjust the TextField's disabledTrailingIconColor if we weren't using ExposedDropdownMenuDefaults
                 disabledTrailingIconColor = TextWhite.copy(alpha = 0.4f), // Explicitly set for disabled state
                 focusedTrailingIconColor = TextWhite.copy(alpha = 0.7f),
                 unfocusedTrailingIconColor = TextWhite.copy(alpha = 0.7f)
            ),
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
            modifier = Modifier.background(DarkSurface) // Set background for the dropdown menu itself
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption, color = TextWhite) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    modifier = Modifier.background(DarkSurface) // Ensure item background matches
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
    onPlantAdded: () -> Unit,
    viewModel: AddPlantViewModel = viewModel(factory = AddPlantViewModel.Factory),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add a Plant",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.AddPlant.route,
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Strain Name
            OutlinedTextField(
                value = uiState.strainName,
                onValueChange = { viewModel.updateStrainName(it) },
                label = { Text("Enter Strain Name", color = TextWhite.copy(alpha = 0.8f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    disabledTextColor = TextWhite.copy(alpha = 0.7f),
                    disabledLabelColor = TextWhite.copy(alpha = 0.5f),
                    disabledBorderColor = DarkSurface.copy(alpha = 0.3f),
                    disabledTrailingIconColor = TextWhite.copy(alpha = 0.5f)
                )
            )
            
            // Batch Number
            OutlinedTextField(
                value = uiState.batchNumber,
                onValueChange = { viewModel.updateBatchNumber(it) },
                label = { Text("Enter Batch Number", color = TextWhite.copy(alpha = 0.8f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    disabledTextColor = TextWhite.copy(alpha = 0.7f),
                    disabledLabelColor = TextWhite.copy(alpha = 0.5f),
                    disabledBorderColor = DarkSurface.copy(alpha = 0.3f),
                    disabledTrailingIconColor = TextWhite.copy(alpha = 0.5f)
                )
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
            
            // Plant Type (Autoflower/Photoperiod) Dropdown
            DropdownMenuField(
                label = "Plant Type",
                selectedValue = uiState.typeDisplay, // Using typeDisplay from ViewModel
                options = listOf("Select", "Autoflower", "Photoperiod"), // "Select" as the first option
                onOptionSelected = { selectedString ->
                    val type = when(selectedString) {
                        "Autoflower" -> PlantType.AUTOFLOWER
                        "Photoperiod" -> PlantType.PHOTOPERIOD
                        else -> null
                    }
                    viewModel.updatePlantType(type)
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
            
            // Seed to Harvest / Flower Duration (Conditionally Displayed)
            if (uiState.showDurationField) {
                OutlinedTextField(
                    value = uiState.durationText,
                    onValueChange = { viewModel.updateDurationText(it) },
                    label = { Text(uiState.durationLabel, color = TextWhite.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                        unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                        unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                        cursorColor = PrimaryGreen,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        disabledTextColor = TextWhite.copy(alpha = 0.7f),
                        disabledLabelColor = TextWhite.copy(alpha = 0.5f),
                        disabledBorderColor = DarkSurface.copy(alpha = 0.3f),
                        disabledTrailingIconColor = TextWhite.copy(alpha = 0.5f)
                    )
                )
            }
            
            // Start Date
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showStartDatePickerDialog() } // Apply clickable to the Box
            ) {
                OutlinedTextField(
                    value = uiState.startDateText,
                    onValueChange = { /* Read-only, value updated via dialog */ },
                    label = { Text("Start Date", color = TextWhite.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(), // TextField takes full width of the Box
                    readOnly = true,
                    enabled = false, // Disable the TextField to prevent focus but allow Box to be clickable
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = TextWhite.copy(alpha = 0.7f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                        unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                        unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                        cursorColor = PrimaryGreen,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        disabledTextColor = TextWhite.copy(alpha = 0.8f), // Adjust for better visibility when disabled
                        disabledLabelColor = TextWhite.copy(alpha = 0.7f), // Adjust for better visibility
                        disabledBorderColor = DarkSurface.copy(alpha = 0.4f), // Keep border similar
                        disabledTrailingIconColor = TextWhite.copy(alpha = 0.7f)
                    )
                )
            }
            
            // DatePickerDialog
            if (uiState.showStartDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = Instant.now().toEpochMilli() // Default to today
                )
                DatePickerDialog(
                    onDismissRequest = { viewModel.dismissStartDatePickerDialog() },
                    confirmButton = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.updateStartDate(selectedDate)
                        }
                        viewModel.dismissStartDatePickerDialog()
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissStartDatePickerDialog() },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextWhite.copy(alpha = 0.7f))
                        ) {
                            Text("Cancel")
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = DarkSurface,
                        titleContentColor = TextWhite,
                        headlineContentColor = TextWhite,
                        weekdayContentColor = TextWhite.copy(alpha = 0.7f),
                        subheadContentColor = TextWhite.copy(alpha = 0.8f),
                        yearContentColor = TextWhite,
                        currentYearContentColor = PrimaryGreen,
                        selectedYearContentColor = TextWhite,
                        selectedYearContainerColor = PrimaryGreen.copy(alpha = 0.8f),
                        dayContentColor = TextWhite,
                        disabledDayContentColor = TextWhite.copy(alpha = 0.3f),
                        selectedDayContentColor = DarkBackground, // Text color on selected day
                        disabledSelectedDayContentColor = DarkBackground.copy(alpha = 0.5f),
                        selectedDayContainerColor = PrimaryGreen, // Background of selected day
                        disabledSelectedDayContainerColor = PrimaryGreen.copy(alpha = 0.3f),
                        todayContentColor = PrimaryGreen, // Today's date (not selected)
                        todayDateBorderColor = PrimaryGreen,
                        dayInSelectionRangeContentColor = DarkBackground,
                        dayInSelectionRangeContainerColor = PrimaryGreen.copy(alpha = 0.6f)
                    )
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Nutrients Input Field
            Text("Nutrients", style = MaterialTheme.typography.labelLarge, color = TextWhite.copy(alpha = 0.9f), modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.currentNutrientInput,
                    onValueChange = { viewModel.updateCurrentNutrientInput(it) },
                    label = { Text("Add Nutrient", color = TextWhite.copy(alpha = 0.8f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                        unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                        unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                        cursorColor = PrimaryGreen,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        disabledTextColor = TextWhite.copy(alpha = 0.7f),
                        disabledLabelColor = TextWhite.copy(alpha = 0.5f),
                        disabledBorderColor = DarkSurface.copy(alpha = 0.3f)
                    )
                )
                IconButton(onClick = { viewModel.addNutrient() }, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Nutrient",
                        tint = PrimaryGreen // Matching EditPlantScreen's add button color
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
                            label = { Text(nutrient, color = TextWhite) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = DarkSurface.copy(alpha = 0.8f),
                                labelColor = TextWhite,
                                trailingIconColor = TextWhite.copy(alpha = 0.7f)
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $nutrient",
                                    tint = TextWhite.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { viewModel.removeNutrient(nutrient) }
                                        .size(InputChipDefaults.IconSize) // Ensures IconSize is applied to Modifier
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = null // No border or customize as needed
                        )
                    }
                }
            }
            
            // Soil Type Dropdown
            DropdownMenuField(
                label = "Soil Type",
                selectedValue = uiState.soilTypeDisplay,
                options = viewModel.soilTypeOptions,
                onOptionSelected = { viewModel.updateSoilType(it) },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )
            
            // Save Button
            Button(
                onClick = {
                    viewModel.savePlant()
                    onPlantAdded()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = TextWhite,
                    disabledContainerColor = DarkSurface.copy(alpha = 0.5f),
                    disabledContentColor = TextWhite.copy(alpha = 0.5f)
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
} 