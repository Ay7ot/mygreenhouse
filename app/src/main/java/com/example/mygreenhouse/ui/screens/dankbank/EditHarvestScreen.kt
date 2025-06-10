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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import com.example.mygreenhouse.data.model.Harvest
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
fun EditHarvestScreen(
    harvestId: String,
    onNavigateBack: () -> Unit,
    onHarvestUpdated: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    navController: NavController,
    darkTheme: Boolean
) {
    // State for form fields
    var strainName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf(LocalDate.now()) }
    var wetWeight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    // Context for date picker
    val context = LocalContext.current
    
    // Formatting for date display
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    // Fetch harvest data
    LaunchedEffect(harvestId) {
        val harvest = viewModel.getHarvestById(harvestId).firstOrNull()
        if (harvest != null) {
            strainName = harvest.strainName
            batchNumber = harvest.batchNumber
            harvestDate = harvest.harvestDate
            wetWeight = harvest.wetWeight?.toString() ?: ""
            notes = harvest.notes
            isLoading = false
        }
    }
    
    // Form validation (strain and batch are read-only, so just check if not loading)
    val isFormValid = !isLoading
    
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
                title = { Text("Edit Harvest", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
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
                // Strain name field (read-only)
                OutlinedTextField(
                    value = strainName,
                    onValueChange = { }, // Read-only
                    label = { Text("Strain Name", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                        disabledContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Batch number field (read-only)
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { }, // Read-only
                    label = { Text("Batch Number", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = if (darkTheme) DarkSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                        disabledContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                        label = { Text("Harvest Date", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
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
                
                // Wet weight field
                OutlinedTextField(
                    value = wetWeight,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            wetWeight = it
                        }
                    },
                    label = { Text("Wet Weight (grams)", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editTextFieldColors(darkTheme = darkTheme),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes", color = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                    colors = editTextFieldColors(darkTheme = darkTheme),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Update button
                Button(
                    onClick = {
                        viewModel.updateHarvestBasicInfo(
                            harvestId = harvestId,
                            strainName = strainName,
                            batchNumber = batchNumber,
                            harvestDate = harvestDate,
                            wetWeight = wetWeight.toDoubleOrNull(),
                            notes = notes
                        )
                        onHarvestUpdated()
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
                    Text("Update Harvest")
                }
            }
        }
    }
}

@Composable
private fun editTextFieldColors(darkTheme: Boolean) = OutlinedTextFieldDefaults.colors(
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