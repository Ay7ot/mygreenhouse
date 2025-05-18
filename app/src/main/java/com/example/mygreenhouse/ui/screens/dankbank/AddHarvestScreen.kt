package com.example.mygreenhouse.ui.screens.dankbank

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
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
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHarvestScreen(
    onNavigateBack: () -> Unit,
    onHarvestAdded: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController
) {
    // State for form fields
    var selectedPlantId by remember { mutableStateOf<String?>(null) }
    var selectedPlantName by remember { mutableStateOf("Select Plant (Optional)") }
    var strainName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf(LocalDate.now()) }
    var wetWeight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Plants for dropdown selection
    val plants by viewModel.plantRepository.allActivePlants.collectAsState(initial = emptyList())
    
    // UI state for plant dropdown
    var isPlantDropdownExpanded by remember { mutableStateOf(false) }
    
    // Context for date picker
    val context = LocalContext.current
    
    // Formatting for date display
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    // Form validation
    val isFormValid = strainName.isNotBlank() && batchNumber.isNotBlank()
    
    // Date picker dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                harvestDate = LocalDate.of(year, month + 1, day)
            },
            harvestDate.year,
            harvestDate.monthValue - 1,
            harvestDate.dayOfMonth
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Harvest", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                currentRoute = NavDestination.DankBank.route,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Plant selection dropdown
            ExposedDropdownMenuBox(
                expanded = isPlantDropdownExpanded,
                onExpandedChange = { isPlantDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPlantName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Plant", color = TextWhite.copy(alpha = 0.8f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = isPlantDropdownExpanded
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
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = isPlantDropdownExpanded,
                    onDismissRequest = { isPlantDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    // None option
                    DropdownMenuItem(
                        text = { Text("None (Manual Entry)", color = TextWhite) },
                        onClick = {
                            selectedPlantId = null
                            selectedPlantName = "Manual Entry"
                            isPlantDropdownExpanded = false
                        },
                        modifier = Modifier.background(DarkSurface)
                    )
                    
                    // Plant options
                    plants.forEach { plant ->
                        DropdownMenuItem(
                            text = { Text(plant.strainName, color = TextWhite) },
                            onClick = {
                                selectedPlantId = plant.id
                                selectedPlantName = plant.strainName
                                strainName = plant.strainName
                                batchNumber = plant.batchNumber
                                isPlantDropdownExpanded = false
                            },
                            modifier = Modifier.background(DarkSurface)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Strain name field
            OutlinedTextField(
                value = strainName,
                onValueChange = { strainName = it },
                label = { Text("Strain Name", color = TextWhite.copy(alpha = 0.8f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Batch number field
            OutlinedTextField(
                value = batchNumber,
                onValueChange = { batchNumber = it },
                label = { Text("Batch Number", color = TextWhite.copy(alpha = 0.8f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Harvest date picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
            ) {
                OutlinedTextField(
                    value = harvestDate.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Harvest Date", color = TextWhite.copy(alpha = 0.8f)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = TextWhite.copy(alpha = 0.7f)
                        )
                    },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                        unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                        unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                        disabledTextColor = TextWhite.copy(alpha = 0.8f),
                        disabledLabelColor = TextWhite.copy(alpha = 0.7f),
                        disabledBorderColor = DarkSurface.copy(alpha = 0.4f),
                        disabledTrailingIconColor = TextWhite.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wet weight field
            OutlinedTextField(
                value = wetWeight,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        wetWeight = it
                    }
                },
                label = { Text("Wet Weight (grams)", color = TextWhite.copy(alpha = 0.8f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes", color = TextWhite.copy(alpha = 0.8f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkSurface.copy(alpha = 0.4f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = TextWhite.copy(alpha = 0.9f),
                    unfocusedLabelColor = TextWhite.copy(alpha = 0.7f),
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Button(
                onClick = {
                    viewModel.addHarvest(
                        plantId = selectedPlantId,
                        strainName = strainName,
                        batchNumber = batchNumber,
                        harvestDate = harvestDate,
                        wetWeight = wetWeight.toDoubleOrNull(),
                        notes = notes
                    )
                    onHarvestAdded()
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = TextWhite,
                    disabledContainerColor = DarkSurface
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Harvest")
            }
        }
    }
} 