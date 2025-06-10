package com.example.mygreenhouse.ui.screens.editplant

// Foundation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit

// Material 3
import androidx.compose.material3.*

// Runtime
import androidx.compose.runtime.*

// Layout
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

// AndroidX Lifecycle & ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// UI Components & Navigation
import com.example.mygreenhouse.ui.composables.ImagePicker
import com.example.mygreenhouse.ui.composables.StrainSelector
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
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.repository.TaskRepository
import com.example.mygreenhouse.data.AppDatabase

// Java Time
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// UI specific
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPlantScreen(
    onNavigateBack: () -> Unit,
    onPlantUpdated: () -> Unit,
    onNavigateToHarvest: (String, String, String) -> Unit = { _, _, _ -> }, // strain, batch, plantId
    viewModel: EditPlantViewModel = viewModel(factory = EditPlantViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Focus manager for keyboard navigation
    val focusManager = LocalFocusManager.current

    // Tasks for this plant
    var plantTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    
    // Load tasks for this plant
    LaunchedEffect(uiState.plantId) {
        if (uiState.plantId.isNotEmpty()) {
            val taskRepository = TaskRepository(AppDatabase.getDatabase(context).taskDao())
            taskRepository.getTasksForPlant(uiState.plantId).collect { tasks ->
                plantTasks = tasks
            }
        }
    }

    // Date picker dialogs
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
                title = { 
                    Text(
                        "Plant Details", 
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.checkForUnsavedChanges()) {
                            viewModel.showExitConfirmationDialog()
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background), 
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.originalPlant == null && !plantIdIsEmpty(viewModel)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                    .padding(16.dp), 
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Plant not found or failed to load.", 
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Plant Image Section
                item {
                    PlantImageSection(
                        imageUri = uiState.imageUri,
                        onImageSelected = { viewModel.updateImageUri(it) },
                        darkTheme = darkTheme
                    )
                }
                
                // Basic Plant Information Section
                item {
                    BasicPlantInfoSection(
                        strainName = uiState.strainName,
                        batchNumber = uiState.batchNumber,
                        quantity = uiState.quantity,
                        onStrainSelected = { strainName, isCustomStrain ->
                            viewModel.updateStrainName(strainName)
                            viewModel.updateIsCustomStrain(isCustomStrain)
                        },
                        onBatchNumberChanged = { viewModel.updateBatchNumber(it) },
                        onQuantityChanged = { viewModel.updateQuantity(it) },
                        darkTheme = darkTheme,
                        focusManager = focusManager
                    )
                }
                
                // Plant Type & Growth Information Section
                item {
                    PlantTypeGrowthSection(
                        uiState = uiState,
                        viewModel = viewModel,
                        datePickerDialog = datePickerDialog,
                        dryingDatePickerDialog = dryingDatePickerDialog,
                        curingDatePickerDialog = curingDatePickerDialog,
                        darkTheme = darkTheme,
                        focusManager = focusManager
                    )
                }
                
                // Growth Stage Timeline Section
                item {
                    GrowthStageTimelineSection(
                        uiState = uiState,
                        viewModel = viewModel,
                        darkTheme = darkTheme
                    )
                }
                
                // Tasks Section
                if (plantTasks.isNotEmpty()) {
                    item {
                        PlantTasksSection(
                            tasks = plantTasks,
                            darkTheme = darkTheme
                        )
                    }
                }
                
                // Growing Environment Section
                item {
                    GrowingEnvironmentSection(
                        uiState = uiState,
                        viewModel = viewModel,
                        darkTheme = darkTheme,
                        focusManager = focusManager
                    )
                }
                
                // Update Button
                item {
                    Button(
                        onClick = {
                            viewModel.updatePlant()
                            onPlantUpdated()
                        },
                        enabled = uiState.isValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Plant", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (uiState.showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onExitConfirmationDismiss() },
            title = { Text("Unsaved Changes") },
            text = { Text("Do you wish to save any changes?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.onExitConfirmationSave { saved ->
                            if (saved) {
                                onPlantUpdated()
                                onNavigateBack()
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { 
                            viewModel.onExitConfirmationDontSave()
                            onNavigateBack()
                        }
                    ) {
                        Text("Don't Save")
                    }
                    TextButton(
                        onClick = { viewModel.onExitConfirmationDismiss() }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Harvest Confirmation Dialog
    if (uiState.showHarvestConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onHarvestConfirmationDismiss() },
            title = { Text("Move Batch to Your Dank Bank") },
            text = { Text("Do you want to remove this batch from your Greenhouse and move it to your Dank Bank for harvest tracking?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        val harvestData = viewModel.onHarvestConfirmationConfirm()
                        if (harvestData != null) {
                            onNavigateToHarvest(harvestData.first, harvestData.second, harvestData.third)
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onHarvestConfirmationCancel() }
                ) {
                    Text("No")
                }
            }
        )
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

@Composable
private fun StageStartDateField(
    stage: GrowthStage,
    currentDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    darkTheme: Boolean
) {
    val context = LocalContext.current
    val datePickerDialog = remember(currentDate) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            currentDate?.year ?: LocalDate.now().year,
            currentDate?.monthValue?.minus(1) ?: LocalDate.now().monthValue - 1,
            currentDate?.dayOfMonth ?: LocalDate.now().dayOfMonth
        )
    }

    val stageName = stage.name.replace("_", " ").capitalizeWords()
    val dateText = currentDate?.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) ?: "Not Set"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = dateText,
            onValueChange = { /* Read-only */ },
            label = { Text("$stageName Start Date", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
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
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
    }
}

private fun getStageStartDate(uiState: EditPlantUiState, stage: GrowthStage): LocalDate? {
    return when (stage) {
        GrowthStage.GERMINATION -> uiState.germinationStartDate
        GrowthStage.SEEDLING -> uiState.seedlingStartDate
        GrowthStage.NON_ROOTED -> uiState.nonRootedStartDate
        GrowthStage.ROOTED -> uiState.rootedStartDate
        GrowthStage.VEGETATION -> uiState.vegetationStartDate
        GrowthStage.FLOWER -> uiState.flowerStartDate
        GrowthStage.DRYING -> uiState.dryingStartDate
        GrowthStage.CURING -> uiState.curingStartDate
        GrowthStage.HARVEST_PLANT -> null // HARVEST_PLANT doesn't have a start date
    }
}

private fun updateStageStartDate(viewModel: EditPlantViewModel, stage: GrowthStage, date: LocalDate) {
    when (stage) {
        GrowthStage.GERMINATION -> viewModel.updateGerminationStartDate(date)
        GrowthStage.SEEDLING -> viewModel.updateSeedlingStartDate(date)
        GrowthStage.NON_ROOTED -> viewModel.updateNonRootedStartDate(date)
        GrowthStage.ROOTED -> viewModel.updateRootedStartDate(date)
        GrowthStage.VEGETATION -> viewModel.updateVegetationStartDate(date)
        GrowthStage.FLOWER -> viewModel.updateFlowerStartDate(date)
        GrowthStage.DRYING -> viewModel.updateDryingStartDate(date)
        GrowthStage.CURING -> viewModel.updateCuringStartDate(date)
        GrowthStage.HARVEST_PLANT -> { /* HARVEST_PLANT doesn't have a start date to update */ }
    }
}

// Section Composables

@Composable
private fun PlantImageSection(
    imageUri: String?,
    onImageSelected: (String?) -> Unit,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Plant Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ImagePicker(
                imageUri = imageUri,
                onImageSelected = onImageSelected,
                modifier = Modifier.fillMaxWidth(),
                label = ""
            )
        }
    }
}

@Composable
private fun BasicPlantInfoSection(
    strainName: String,
    batchNumber: String,
    quantity: String,
    onStrainSelected: (String, Boolean) -> Unit,
    onBatchNumberChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    darkTheme: Boolean,
    focusManager: FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            }
            
            StrainSelector(
                selectedStrainName = strainName,
                onStrainSelected = onStrainSelected,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = onBatchNumberChanged,
                    label = { Text("Batch Number*", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChanged,
                    label = { Text("Quantity*", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }
        }
    }
}

@Composable
private fun PlantTypeGrowthSection(
    uiState: EditPlantUiState,
    viewModel: EditPlantViewModel,
    datePickerDialog: android.app.DatePickerDialog,
    dryingDatePickerDialog: android.app.DatePickerDialog,
    curingDatePickerDialog: android.app.DatePickerDialog,
    darkTheme: Boolean,
    focusManager: FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Plant Type & Growth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownMenuField(
                    label = "Seed or Clone*",
                    selectedValue = uiState.sourceDisplay,
                    options = PlantSource.values().map { it.name.lowercase().replaceFirstChar(Char::titlecase) },
                    onOptionSelected = { selectedString ->
                        val source = PlantSource.values().find { it.name.equals(selectedString, ignoreCase = true) }
                        source?.let { viewModel.updateSource(it) }
                    },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                )

                DropdownMenuField(
                    label = "Plant Gender",
                    selectedValue = uiState.plantGenderDisplay,
                    options = viewModel.plantGenderOptions,
                    onOptionSelected = { viewModel.updatePlantGender(it) },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownMenuField(
                label = "Plant Type",
                selectedValue = uiState.plantTypeSelection,
                options = viewModel.plantTypeSelectionOptions,
                onOptionSelected = { selectedString ->
                    viewModel.updatePlantTypeSelection(selectedString)
                },
                enabled = uiState.source != null
            )

            DropdownMenuField(
                label = "Current Growth Stage",
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

            // Start Date Field
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.startDateText,
                    onValueChange = { /* Read-only */ },
                    label = { Text("Start Date*", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
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

            // Duration fields and status displays
            if (uiState.showDurationField) {
                OutlinedTextField(
                    value = uiState.durationText,
                    onValueChange = { viewModel.updateDurationText(it) },
                    label = { Text(uiState.durationLabel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // Status displays for different growth stages
            val daysInDrying = uiState.daysInDrying
            val daysInCuring = uiState.daysInCuring
            val daysUntilHarvestLocal = uiState.daysUntilHarvest

            if (uiState.growthStage == GrowthStage.DRYING && daysInDrying != null) {
                StatusCard(
                    title = "Days in Drying",
                    value = daysInDrying.toString(),
                    action = "Change Date",
                    onActionClick = { dryingDatePickerDialog.show() },
                    darkTheme = darkTheme
                )
            }

            if (uiState.growthStage == GrowthStage.CURING && daysInCuring != null) {
                StatusCard(
                    title = "Days in Curing",
                    value = daysInCuring.toString(),
                    action = "Change Date",
                    onActionClick = { curingDatePickerDialog.show() },
                    darkTheme = darkTheme
                )
            }

            if (uiState.type == PlantType.AUTOFLOWER && uiState.source == PlantSource.SEED && daysUntilHarvestLocal != null) {
                StatusCard(
                    title = if (daysUntilHarvestLocal >= 0) "Days Until Harvest" else "Harvest Overdue",
                    value = if (daysUntilHarvestLocal >= 0) daysUntilHarvestLocal.toString() else "${-daysUntilHarvestLocal} days ago",
                    darkTheme = darkTheme
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) PrimaryGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                )
            }
            
            if (action != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(
                        text = action,
                        color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthStageTimelineSection(
    uiState: EditPlantUiState,
    viewModel: EditPlantViewModel,
    darkTheme: Boolean
) {
    if (uiState.completedStages.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Growth Stage Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = "Tap any date to edit if corrections are needed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                uiState.completedStages.forEach { stage ->
                    StageStartDateField(
                        stage = stage,
                        currentDate = getStageStartDate(uiState, stage),
                        onDateSelected = { date -> updateStageStartDate(viewModel, stage, date) },
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantTasksSection(
    tasks: List<Task>,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Task,
                    contentDescription = null,
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scheduled Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            tasks.take(3).forEach { task ->
                TaskItem(task = task, darkTheme = darkTheme)
            }
            
            if (tasks.size > 3) {
                Text(
                    text = "...and ${tasks.size - 3} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    darkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkBackground.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (task.isCompleted) {
                            if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                        } else {
                            if (darkTheme) Color(0xFFFFA726) else MaterialTheme.colorScheme.tertiary
                        },
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.type.name.replace("_", " ").lowercase().replaceFirstChar(Char::titlecase),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = if (task.isCompleted) "✓" else "⏰",
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isCompleted) {
                    if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                } else {
                    if (darkTheme) Color(0xFFFFA726) else MaterialTheme.colorScheme.tertiary
                }
            )
        }
    }
}

@Composable
private fun GrowingEnvironmentSection(
    uiState: EditPlantUiState,
    viewModel: EditPlantViewModel,
    darkTheme: Boolean,
    focusManager: FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Growing Environment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            }
            
            DropdownMenuField(
                label = "Grow Medium",
                selectedValue = uiState.growMediumDisplay,
                options = viewModel.growMediumOptions,
                onOptionSelected = { viewModel.updateGrowMedium(it) },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )
            
            Text(
                "Nutrients", 
                style = MaterialTheme.typography.labelLarge, 
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.currentNutrientInput,
                    onValueChange = { viewModel.updateCurrentNutrientInput(it) },
                    label = { Text("Add Nutrient", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            viewModel.addNutrient()
                        }
                    )
                )
                IconButton(onClick = { viewModel.addNutrient() }) {
                    Icon(Icons.Filled.Add, "Add Nutrient", tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
                }
            }
            
            @OptIn(ExperimentalLayoutApi::class)
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
                            containerColor = if (darkTheme) PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSecondaryContainer,
                            trailingIconColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
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
        }
    }
}