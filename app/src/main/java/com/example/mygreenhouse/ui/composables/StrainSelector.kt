package com.example.mygreenhouse.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.data.model.Strain
import com.example.mygreenhouse.ui.screens.strain.StrainViewModel
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.PrimaryGreenLight
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite

@Composable
fun StrainSelector(
    selectedStrainName: String,
    onStrainSelected: (String, Boolean) -> Unit, // (strainName, isCustomStrain)
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    strainViewModel: StrainViewModel = viewModel(factory = StrainViewModel.Factory)
) {
    val strains by strainViewModel.allStrains.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var showAddStrainDialog by remember { mutableStateOf(false) }
    var showEditStrainDialog by remember { mutableStateOf<Strain?>(null) }
    
    Column(modifier = modifier) {
        Text(
            "Strain Name *", 
            style = MaterialTheme.typography.labelLarge,
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedStrainName,
                onValueChange = { /* Read only */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = if (darkTheme) PrimaryGreenLight.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    disabledTextColor = if (selectedStrainName.isNotEmpty()) (if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) else (if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant),
                    disabledIndicatorColor = if (selectedStrainName.isNotEmpty()) (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary) else Color.Transparent,
                ),
                placeholder = { Text("Select strain name") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand strain selection",
                        tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                    )
                }
            )
            
            // Invisible clickable overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = { expanded = true })
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface)
                    .fillMaxWidth(0.9f)
            ) {
                // Add new strain option
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add new strain",
                                tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add New Strain",
                                color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        showAddStrainDialog = true
                    }
                )
                
                if (strains.isNotEmpty()) {
                    // Existing strains
                    strains.forEach { strain ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                strain.name,
                                                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (strain.isCustomStrain) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "(Custom)",
                                                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                        Text(
                                            "Used ${strain.usageCount} times",
                                            color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            expanded = false
                                            showEditStrainDialog = strain
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit strain",
                                            tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onStrainSelected(strain.name, strain.isCustomStrain)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add new strain dialog
    if (showAddStrainDialog) {
        AddStrainDialog(
            onDismiss = { showAddStrainDialog = false },
            onConfirm = { strainName, isCustom ->
                // Don't archive the strain here - it will be archived when the plant/seed is saved
                // This prevents double counting of strain usage
                onStrainSelected(strainName, isCustom)
                showAddStrainDialog = false
            },
            darkTheme = darkTheme
        )
    }
    
    // Edit strain dialog
    showEditStrainDialog?.let { strain ->
        EditStrainDialog(
            strain = strain,
            onDismiss = { showEditStrainDialog = null },
            onConfirm = { updatedStrain ->
                strainViewModel.updateStrain(updatedStrain)
                // Update selection if the current strain was edited
                if (selectedStrainName == strain.name) {
                    onStrainSelected(updatedStrain.name, updatedStrain.isCustomStrain)
                }
                showEditStrainDialog = null
            },
            onDelete = { strainToDelete ->
                strainViewModel.deleteStrain(strainToDelete)
                // Clear selection if the current strain was deleted
                if (selectedStrainName == strainToDelete.name) {
                    onStrainSelected("", false)
                }
                showEditStrainDialog = null
            },
            onResetUsage = { strainName ->
                strainViewModel.resetStrainUsageCount(strainName)
            },
            darkTheme = darkTheme
        )
    }
}

@Composable
fun AddStrainDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit,
    darkTheme: Boolean
) {
    var strainName by remember { mutableStateOf("") }
    var isCustomStrain by remember { mutableStateOf(false) }
    val isValid = strainName.trim().isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Add New Strain",
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column {
                TextField(
                    value = strainName,
                    onValueChange = { strainName = it },
                    label = { Text("Strain Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isCustomStrain = !isCustomStrain }
                ) {
                    Checkbox(
                        checked = isCustomStrain,
                        onCheckedChange = { isCustomStrain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            uncheckedColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            checkmarkColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        "Custom Strain",
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(strainName.trim(), isCustomStrain) },
                enabled = isValid
            ) {
                Text(
                    "Add",
                    color = if (isValid) (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary) 
                          else (if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
    )
}

@Composable
fun EditStrainDialog(
    strain: Strain,
    onDismiss: () -> Unit,
    onConfirm: (Strain) -> Unit,
    onDelete: (Strain) -> Unit,
    onResetUsage: (String) -> Unit,
    darkTheme: Boolean
) {
    var strainName by remember { mutableStateOf(strain.name) }
    var isCustomStrain by remember { mutableStateOf(strain.isCustomStrain) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isValid = strainName.trim().isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Strain",
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column {
                TextField(
                    value = strainName,
                    onValueChange = { strainName = it },
                    label = { Text("Strain Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isCustomStrain = !isCustomStrain }
                ) {
                    Checkbox(
                        checked = isCustomStrain,
                        onCheckedChange = { isCustomStrain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                            uncheckedColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            checkmarkColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        "Custom Strain",
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Usage: ${strain.usageCount} times",
                        color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (strain.usageCount > 1) {
                        TextButton(
                            onClick = {
                                // Reset usage count to 1 for strains with incorrect counts
                                onResetUsage(strain.name)
                                // Close the dialog to show the updated count
                                onDismiss()
                            }
                        ) {
                            Text(
                                "Reset to 1",
                                color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                TextButton(
                    onClick = { 
                        onConfirm(strain.copy(
                            name = strainName.trim(),
                            isCustomStrain = isCustomStrain
                        ))
                    },
                    enabled = isValid
                ) {
                    Text(
                        "Save",
                        color = if (isValid) (if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary) 
                              else (if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
    )
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { 
                Text(
                    "Delete Strain",
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete the strain \"${strain.name}\"? This action cannot be undone.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onDelete(strain)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(
                        "Cancel",
                        color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
        )
    }
} 