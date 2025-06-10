package com.example.mygreenhouse.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import android.content.ContextWrapper
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    darkTheme: Boolean,
    activity: FragmentActivity,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    val maxAttempts = 5
    
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricAvailable = viewModel.isBiometricAvailable
    
    // Automatically show biometric prompt when biometric is enabled and available
    LaunchedEffect(isBiometricEnabled, isBiometricAvailable, activity) {
        if (isBiometricEnabled && isBiometricAvailable && activity != null) {
            viewModel.authenticateWithBiometric(
                activity = activity,
                onSuccess = { onUnlockSuccess() },
                onError = { _, _ -> /* Just fall back to PIN */ }
            )
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon with lock
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "My Greenhouse",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Enter your PIN to unlock",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // PIN Entry
            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        pin = it
                        error = null
                    }
                },
                label = { Text("Enter PIN") },
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
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Attempts remaining (only show if there have been failed attempts)
            if (attempts > 0) {
                Text(
                    text = "Attempts remaining: ${maxAttempts - attempts}",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Unlock button
            Button(
                onClick = {
                    if (viewModel.verifyPin(pin)) {
                        // PIN is correct
                        onUnlockSuccess()
                    } else {
                        // PIN is incorrect
                        attempts++
                        pin = ""
                        if (attempts >= maxAttempts) {
                            error = "Too many failed attempts. Try again later."
                            // In a real app, you might want to implement a timeout here
                        } else {
                            error = "Incorrect PIN. Please try again."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.isNotEmpty() && attempts < maxAttempts
            ) {
                Text("Unlock")
            }
            
            // Biometric option (if available and enabled)
            if (isBiometricEnabled && isBiometricAvailable && activity != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.authenticateWithBiometric(
                                activity = activity,
                                onSuccess = { onUnlockSuccess() },
                                onError = { code, message ->
                                    if (code != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                        code != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED) {
                                        error = "Biometric error: $message"
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Use biometric authentication",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Biometric")
                    }
                }
            }
        }
    }
} 