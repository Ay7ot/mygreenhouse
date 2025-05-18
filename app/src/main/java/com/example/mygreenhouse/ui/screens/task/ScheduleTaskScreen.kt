package com.example.mygreenhouse.ui.screens.task

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.TaskType
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkerGreenButton
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.PrimaryGreenLight
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayOfWeek(val id: String, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTaskScreen(
    taskType: TaskType,
    onNavigateBack: () -> Unit,
    onSaveTask: (taskType: TaskType, time: Calendar, repeatDays: List<String>, notes: String) -> Unit,
    viewModel: TaskViewModel = viewModel(),
    existingTask: Task? = null // Optional task parameter for editing
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Plants state
    val plants by viewModel.plants.collectAsState(initial = emptyList())
    val isLoadingPlants by viewModel.isLoadingPlants.collectAsState(initial = true)
    
    var selectedTime by remember { 
        mutableStateOf(
            if (existingTask != null) {
                // Convert LocalDateTime to Calendar
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, existingTask.scheduledDateTime.hour)
                    set(Calendar.MINUTE, existingTask.scheduledDateTime.minute)
                }
            } else {
                Calendar.getInstance()
            }
        ) 
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Plant selection state
    var selectedPlantId by remember { mutableStateOf(existingTask?.plantId) }
    var selectedPlantName by remember { mutableStateOf("Select a plant (optional)") }
    var expandPlantDropdown by remember { mutableStateOf(false) }
    
    // Update selected plant name if we have plants and a selected ID
    LaunchedEffect(plants, selectedPlantId) {
        if (selectedPlantId != null && plants.isNotEmpty()) {
            val plant = plants.find { it.id == selectedPlantId }
            if (plant != null) {
                selectedPlantName = plant.strainName
            }
        }
    }

    val daysOfWeek = remember {
        listOf(
            DayOfWeek("MON", "M"),
            DayOfWeek("TUE", "T"),
            DayOfWeek("WED", "W"),
            DayOfWeek("THU", "T"),
            DayOfWeek("FRI", "F"),
            DayOfWeek("SAT", "Sa"),
            DayOfWeek("SUN", "Su")
        )
    }
    // For now, we'll set an empty implementation for repeat days
    // In a real implementation, you'd parse this from the task's data
    var selectedDayIds by remember { mutableStateOf(emptyList<String>()) }
    
    // Initialize notes with existing task description if available
    var notes by remember { mutableStateOf(existingTask?.description ?: "") }

    if (showTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                showTimePicker = false
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            false // false for 12-hour format with AM/PM
        )
        timePickerDialog.setOnCancelListener { showTimePicker = false }
        timePickerDialog.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (existingTask != null) "Edit ${taskType.displayName()}" 
                        else taskType.displayName(), 
                        color = TextWhite
                    ) 
                },
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = DarkerGreenButton, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = timeFormatter.format(selectedTime.time), fontSize = 18.sp, color = TextWhite)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Plant selection dropdown
            Text(
                "Associate with plant", 
                fontSize = 18.sp, 
                color = TextWhite, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedPlantName,
                    onValueChange = { /* Read only */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandPlantDropdown = true },
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = PrimaryGreenLight.copy(alpha = 0.1f),
                        disabledTextColor = if (selectedPlantId != null) TextWhite else TextGrey,
                        disabledIndicatorColor = if (selectedPlantId != null) PrimaryGreen else Color.Transparent,
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand plant selection",
                            tint = PrimaryGreen
                        )
                    }
                )
                
                // Invisible clickable overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = { expandPlantDropdown = true })
                )
                
                DropdownMenu(
                    expanded = expandPlantDropdown,
                    onDismissRequest = { expandPlantDropdown = false },
                    modifier = Modifier.background(DarkBackground)
                ) {
                    // Option to clear selection
                    DropdownMenuItem(
                        text = { Text("No plant (general task)", color = TextGrey) },
                        onClick = {
                            selectedPlantId = null
                            selectedPlantName = "Select a plant (optional)"
                            expandPlantDropdown = false
                        }
                    )
                    
                    if (isLoadingPlants) {
                        DropdownMenuItem(
                            text = { Text("Loading plants...", color = TextGrey) },
                            onClick = { /* No action */ }
                        )
                    } else if (plants.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No plants in greenhouse", color = TextGrey) },
                            onClick = { /* No action */ }
                        )
                    } else {
                        plants.forEach { plant ->
                            DropdownMenuItem(
                                text = { Text(plant.strainName, color = TextWhite) },
                                onClick = {
                                    selectedPlantId = plant.id
                                    selectedPlantName = plant.strainName
                                    expandPlantDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Repeat", 
                fontSize = 18.sp, 
                color = TextWhite, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dayInfo ->
                    key(dayInfo.id) {
                        DayToggleButton(
                            day = dayInfo.displayName,
                            isSelected = selectedDayIds.contains(dayInfo.id),
                            onDaySelected = {
                                selectedDayIds = if (selectedDayIds.contains(dayInfo.id)) {
                                    selectedDayIds - dayInfo.id
                                } else {
                                    selectedDayIds + dayInfo.id
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Notes:", 
                fontSize = 18.sp, 
                color = TextWhite, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PrimaryGreenLight.copy(alpha = 0.1f),
                    unfocusedContainerColor = PrimaryGreenLight.copy(alpha = 0.1f),
                    disabledContainerColor = PrimaryGreenLight.copy(alpha = 0.1f),
                    focusedIndicatorColor = PrimaryGreen,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                placeholder = { Text("Add notes here...", color = TextGrey) }
            )

            Spacer(modifier = Modifier.height(32.dp)) // Added more space

            Button(
                onClick = {
                    if (existingTask != null) {
                        // Handle update of existing task
                        viewModel.updateTask(
                            existingTaskId = existingTask.id,
                            taskType = taskType,
                            time = selectedTime,
                            repeatDays = selectedDayIds,
                            notes = notes,
                            plantId = selectedPlantId
                        )
                    } else {
                        // Handle new task creation
                        viewModel.saveTask(
                            taskType = taskType,
                            time = selectedTime,
                            repeatDays = selectedDayIds,
                            notes = notes,
                            plantId = selectedPlantId
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = if (existingTask != null) "Update" else "Save", 
                    fontSize = 18.sp, 
                    color = TextWhite
                )
            }
        }
    }
}

@Composable
fun DayToggleButton(
    day: String,
    isSelected: Boolean,
    onDaySelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onDaySelected),
        shape = CircleShape,
        color = if (isSelected) DarkerGreenButton else PrimaryGreenLight.copy(alpha = 0.2f),
        contentColor = if (isSelected) TextWhite else TextGrey
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = day, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// Ensure TaskType.displayName() is accessible, e.g. by moving it to a common file or making TaskScreen internal if not already
// Or redefine it here if it's specific to this UI package context and not globally needed in that exact form.
// For simplicity, assuming it's accessible. If not, it would need to be defined or imported appropriately.

