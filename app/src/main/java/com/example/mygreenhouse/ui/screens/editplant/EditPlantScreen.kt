package com.example.mygreenhouse.ui.screens.editplant

// Foundation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown

// Material 3
import androidx.compose.material3.*

// Runtime
import androidx.compose.runtime.*

// AndroidX Lifecycle & ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// UI Components & Navigation
import com.example.mygreenhouse.ui.composables.ImagePicker
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.screens.addplant.DropdownMenuField

// Theme
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite

// Data Models
import com.example.mygreenhouse.data.model.PlantSource
import com.example.mygreenhouse.data.model.PlantType
import com.example.mygreenhouse.data.model.GrowthStage

// Java Time
import java.time.Instant
import java.time.ZoneId

// UI specific
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPlantScreen(
    onNavigateBack: () -> Unit,
    onPlantUpdated: () -> Unit,
    viewModel: EditPlantViewModel = viewModel(factory = EditPlantViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plant", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
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
                currentRoute = NavDestination.EditPlant.route,
                onNavItemClick = { route ->
                    if (route != NavDestination.EditPlant.route) {
                        onNavigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (uiState.originalPlant == null && !plantIdIsEmpty(viewModel)) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(DarkBackground).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Plant not found or failed to load.", color = TextWhite)
            }
        } else {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                    .background(DarkBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.strainName,
                    onValueChange = { viewModel.updateStrainName(it) },
                    label = { Text("Strain Name", color = TextWhite.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = uiState.batchNumber,
                    onValueChange = { viewModel.updateBatchNumber(it) },
                    label = { Text("Batch Number", color = TextWhite.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors()
                )

                ImagePicker(
                    imageUri = uiState.imageUri,
                    onImageSelected = { viewModel.updateImageUri(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Plant Image"
                )

                DropdownMenuField(
                    label = "Seed or Clone",
                    selectedValue = uiState.sourceDisplay,
                    options = PlantSource.values().map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
                    onOptionSelected = { selectedString ->
                        val source = PlantSource.values().find { it.name.equals(selectedString, ignoreCase = true) }
                        source?.let { viewModel.updateSource(it) }
                    },
                    enabled = true
                )

                if (uiState.source == PlantSource.SEED) {
                    DropdownMenuField(
                        label = "Plant Type",
                        selectedValue = uiState.typeDisplay,
                        options = PlantType.values().map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
                        onOptionSelected = { selectedString ->
                            val type = PlantType.values().find { it.name.equals(selectedString, ignoreCase = true) }
                            viewModel.updatePlantType(type)
                        },
                        enabled = true
                    )
                }

                DropdownMenuField(
                    label = "Growth Stage",
                    selectedValue = uiState.growthStageDisplay,
                    options = uiState.availableGrowthStages.map { it.name.replace("_", " ").lowercase().capitalizeWords() },
                    onOptionSelected = { selectedString ->
                        val stage = uiState.availableGrowthStages.find { 
                            it.name.replace("_", " ").lowercase().capitalizeWords().equals(selectedString, ignoreCase = true) 
                        }
                        stage?.let { viewModel.updateGrowthStage(it) }
                    },
                    enabled = uiState.availableGrowthStages.isNotEmpty()
                )
                
                if (uiState.showDurationField) {
                    OutlinedTextField(
                        value = uiState.durationText,
                        onValueChange = { viewModel.updateDurationText(it) },
                        label = { Text(uiState.durationLabel, color = TextWhite.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showStartDatePickerDialog() }
                ) {
                    OutlinedTextField(
                        value = uiState.startDateText,
                        onValueChange = { /* Read-only */ },
                        label = { Text("Start Date", color = TextWhite.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false, // Important for click on Box to work
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
                            disabledTextColor = TextWhite.copy(alpha = 0.8f),
                            disabledLabelColor = TextWhite.copy(alpha = 0.7f),
                            disabledBorderColor = DarkSurface.copy(alpha = 0.4f),
                            disabledTrailingIconColor = TextWhite.copy(alpha = 0.7f)
                        )
                    )
                }

                if (uiState.showStartDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                    DatePickerDialog(
                        onDismissRequest = { viewModel.dismissStartDatePickerDialog() },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                        viewModel.updateStartDate(selectedDate)
                                    }
                                    viewModel.dismissStartDatePickerDialog()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen)
                            ) {
                                Text("OK")
                            }
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
                            selectedDayContentColor = DarkBackground, 
                            disabledSelectedDayContentColor = DarkBackground.copy(alpha = 0.5f),
                            selectedDayContainerColor = PrimaryGreen, 
                            disabledSelectedDayContainerColor = PrimaryGreen.copy(alpha = 0.3f),
                            todayContentColor = PrimaryGreen, 
                            todayDateBorderColor = PrimaryGreen,
                            dayInSelectionRangeContentColor = DarkBackground,
                            dayInSelectionRangeContainerColor = PrimaryGreen.copy(alpha = 0.6f)
                        )
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Text("Nutrients", style = MaterialTheme.typography.labelLarge, color = TextWhite.copy(alpha = 0.9f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.currentNutrientInput,
                        onValueChange = { viewModel.updateCurrentNutrientInput(it) },
                        label = { Text("Add Nutrient", color = TextWhite.copy(alpha = 0.8f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors()
                    )
                    IconButton(onClick = { viewModel.addNutrient() }) {
                        Icon(Icons.Filled.Add, "Add Nutrient", tint = PrimaryGreen)
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.nutrientsList.forEach { nutrient ->
                        InputChip(
                            selected = false,
                            onClick = { /* Can be used for selection if needed */ },
                            label = { Text(nutrient, color = TextWhite) },
                            colors = InputChipDefaults.inputChipColors(containerColor = DarkSurface),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close, 
                                    contentDescription = "Remove $nutrient", 
                                    tint = TextWhite.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { viewModel.removeNutrient(nutrient) }
                                        .size(InputChipDefaults.IconSize)
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
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
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updatePlant()
                        onPlantUpdated()
                    },
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, disabledContainerColor = DarkSurface)
                ) {
                    Text("Update Plant", fontSize = 16.sp, color = if (uiState.isValid) Color.White else TextWhite.copy(alpha = 0.7f))
                }
            }
        }
    }
}

private fun plantIdIsEmpty(viewModel: EditPlantViewModel): Boolean {
    return viewModel.uiState.value.plantId.isEmpty() && viewModel.uiState.value.originalPlant == null
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
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

private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 