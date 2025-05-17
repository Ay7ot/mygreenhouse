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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    viewModel: TaskViewModel,
    existingTask: Task? = null // Optional task parameter for editing
) {
    val context = LocalContext.current
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
                .padding(16.dp),
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

            Text("Repeat", fontSize = 18.sp, color = TextWhite, fontWeight = FontWeight.Bold)
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

            Text("Notes:", fontSize = 18.sp, color = TextWhite, modifier = Modifier.align(Alignment.Start))
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

            Spacer(modifier = Modifier.weight(1f)) // Pushes Save button to bottom

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
                            plantId = existingTask.plantId
                        )
                    } else {
                        // Handle new task creation
                        viewModel.saveTask(taskType, selectedTime, selectedDayIds, notes)
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

