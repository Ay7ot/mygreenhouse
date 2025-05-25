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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.mygreenhouse.data.model.PlantGender

// Java Time
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

// UI specific
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPlantScreen(
    onNavigateBack: () -> Unit,
    onPlantUpdated: () -> Unit,
    viewModel: EditPlantViewModel = viewModel(factory = EditPlantViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    // Context for date picker
    val context = LocalContext.current

    // Date picker dialog for StartDate
    val datePickerDialog = remember(uiState.startDate) { 
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

    // Date picker dialog for Drying Start Date
    val dryingDatePickerDialog = remember(uiState.dryingStartDate) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateDryingStartDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            uiState.dryingStartDate?.year ?: LocalDate.now().year,
            uiState.dryingStartDate?.monthValue?.minus(1) ?: LocalDate.now().monthValue - 1,
            uiState.dryingStartDate?.dayOfMonth ?: LocalDate.now().dayOfMonth
        )
    }

    // Date picker dialog for Curing Start Date
    val curingDatePickerDialog = remember(uiState.curingStartDate) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateCuringStartDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            uiState.curingStartDate?.year ?: LocalDate.now().year,
            uiState.curingStartDate?.monthValue?.minus(1) ?: LocalDate.now().monthValue - 1,
            uiState.curingStartDate?.dayOfMonth ?: LocalDate.now().dayOfMonth
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plant", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface)
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
                currentRoute = NavDestination.EditPlant.route,
                navController = navController,
                darkTheme = darkTheme
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.originalPlant == null && !plantIdIsEmpty(viewModel)) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Plant not found or failed to load.", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.strainName,
                    onValueChange = { viewModel.updateStrainName(it) },
                    label = { Text("Strain Name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = uiState.batchNumber,
                    onValueChange = { viewModel.updateBatchNumber(it) },
                    label = { Text("Batch Number", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.updateQuantity(it) },
                    label = { Text("Quantity", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

                DropdownMenuField(
                    label = "Plant Gender",
                    selectedValue = uiState.plantGenderDisplay,
                    options = viewModel.plantGenderOptions,
                    onOptionSelected = { viewModel.updatePlantGender(it) },
                    enabled = true
                )

                if (uiState.source == PlantSource.SEED) {
                    DropdownMenuField(
                        label = "Plant Type",
                        selectedValue = uiState.plantTypeSelection,
                        options = viewModel.plantTypeSelectionOptions,
                        onOptionSelected = { selectedString ->
                            viewModel.updatePlantTypeSelection(selectedString)
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

                // Display Days in Drying/Curing or Days Until Harvest
                val daysInDrying = uiState.daysInDrying
                val daysInCuring = uiState.daysInCuring
                val daysUntilHarvestLocal = uiState.daysUntilHarvest

                if (uiState.growthStage == GrowthStage.DRYING && daysInDrying != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
                        Text(
                            text = "Days in Drying: $daysInDrying", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { dryingDatePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, "Change Drying Start Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (uiState.growthStage == GrowthStage.CURING && daysInCuring != null) {
                     Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
                        Text(
                            text = "Days in Curing: $daysInCuring", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { curingDatePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, "Change Curing Start Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
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
                
                if (uiState.showDurationField) {
                    OutlinedTextField(
                        value = uiState.durationText,
                        onValueChange = { viewModel.updateDurationText(it) },
                        label = { Text(uiState.durationLabel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.startDateText,
                        onValueChange = { /* Read-only */ },
                        label = { Text("Start Date", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { datePickerDialog.show() }
                    )
                }

                Text("Nutrients", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.currentNutrientInput,
                        onValueChange = { viewModel.updateCurrentNutrientInput(it) },
                        label = { Text("Add Nutrient", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors()
                    )
                    IconButton(onClick = { viewModel.addNutrient() }) {
                        Icon(Icons.Filled.Add, "Add Nutrient", tint = MaterialTheme.colorScheme.primary)
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
                            label = { Text(nutrient) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close, 
                                    contentDescription = "Remove $nutrient", 
                                    modifier = Modifier
                                        .clickable { viewModel.removeNutrient(nutrient) }
                                        .size(InputChipDefaults.IconSize)
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
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
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updatePlant()
                        onPlantUpdated()
                    },
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text("Update Plant", fontSize = 16.sp)
                }
            }
        }
    }
}

private fun plantIdIsEmpty(viewModel: EditPlantViewModel): Boolean {
    return viewModel.uiState.value.plantId.isEmpty() && viewModel.uiState.value.originalPlant == null
}

@Composable
private fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
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

private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 