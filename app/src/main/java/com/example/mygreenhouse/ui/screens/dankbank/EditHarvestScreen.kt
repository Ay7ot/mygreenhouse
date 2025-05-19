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
    
    // Form validation
    val isFormValid = !isLoading && strainName.isNotBlank() && batchNumber.isNotBlank()
    
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
                title = { Text("Edit Harvest", color = TextWhite) },
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
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
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
                        containerColor = PrimaryGreen,
                        contentColor = TextWhite,
                        disabledContainerColor = DarkSurface
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