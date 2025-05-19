package com.example.mygreenhouse.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPinScreen(
    onNavigateBack: () -> Unit,
    darkTheme: Boolean,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val isPinSet by viewModel.isPinSet.collectAsState()
    
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isPinSet) "Change PIN" else "Set Up PIN",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Icon and description
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = if (isPinSet) "Change your PIN code to secure your greenhouse data"
                       else "Set up a PIN code to secure your greenhouse data",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // PIN Entry
            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        pin = it
                        error = null
                    }
                },
                label = { Text("PIN Code (4-8 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword
                ),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                isError = error != null
            )
            
            // Confirm PIN
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        confirmPin = it
                        error = null
                    }
                },
                label = { Text("Confirm PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword
                ),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                isError = error != null
            )
            
            // Error message if any
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    when {
                        pin.length < 4 -> {
                            error = "PIN must be at least 4 digits"
                        }
                        pin != confirmPin -> {
                            error = "PINs do not match"
                        }
                        else -> {
                            val result = viewModel.setPin(pin)
                            result.fold(
                                onSuccess = {
                                    // PIN successfully set
                                    onNavigateBack()
                                },
                                onFailure = {
                                    error = it.message ?: "Failed to set PIN"
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save PIN")
            }
            
            // Remove PIN button (only show if PIN is already set)
            if (isPinSet) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearPin()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove PIN")
                }
            }
        }
    }
} 