package com.example.mygreenhouse.ui.screens.dankbank

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.mygreenhouse.data.model.SeedType
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeedScreen(
    onNavigateBack: () -> Unit,
    onSeedAdded: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController
) {
    // Form state
    var strainName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var seedCount by remember { mutableStateOf("") }
    var breeder by remember { mutableStateOf("") }
    var selectedSeedType by remember { mutableStateOf(SeedType.REGULAR) }
    var acquisitionDate by remember { mutableStateOf(LocalDate.now()) }
    var source by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // UI state for seed type dropdown
    var isSeedTypeExpanded by remember { mutableStateOf(false) }
    
    // Context for date picker
    val context = LocalContext.current
    
    // Date formatter
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    // Form validation
    val isFormValid = strainName.isNotBlank() && 
                     batchNumber.isNotBlank() && 
                     seedCount.isNotBlank() && 
                     seedCount.toIntOrNull() != null && 
                     seedCount.toIntOrNull() ?: 0 > 0
    
    // Date picker dialog
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
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
                title = { Text("Add Seeds", color = TextWhite) },
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
            // Strain name field
            OutlinedTextField(
                value = strainName,
                onValueChange = { strainName = it },
                label = { Text("Strain Name", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Batch number field
            OutlinedTextField(
                value = batchNumber,
                onValueChange = { batchNumber = it },
                label = { Text("Batch Number", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
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
                label = { Text("Seed Count", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Breeder field
            OutlinedTextField(
                value = breeder,
                onValueChange = { breeder = it },
                label = { Text("Breeder (Optional)", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
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
                    value = selectedSeedType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Seed Type", color = TextWhite.copy(alpha = 0.8f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSeedTypeExpanded)
                    },
                    colors = textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = isSeedTypeExpanded,
                    onDismissRequest = { isSeedTypeExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    SeedType.values().forEach { seedType ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    seedType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = TextWhite
                                ) 
                            },
                            onClick = {
                                selectedSeedType = seedType
                                isSeedTypeExpanded = false
                            },
                            modifier = Modifier.background(DarkSurface)
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
                    label = { Text("Acquisition Date", color = TextWhite.copy(alpha = 0.8f)) },
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
            
            // Source field
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source (Optional)", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)", color = TextWhite.copy(alpha = 0.8f)) },
                colors = textFieldColors(),
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Button(
                onClick = {
                    viewModel.addSeed(
                        strainName = strainName,
                        batchNumber = batchNumber,
                        seedCount = seedCount.toIntOrNull() ?: 0,
                        breeder = breeder,
                        seedType = selectedSeedType,
                        acquisitionDate = acquisitionDate,
                        source = source,
                        notes = notes
                    )
                    onSeedAdded()
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
                Text("Save Seeds")
            }
        }
    }
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
    unfocusedTextColor = TextWhite
) 