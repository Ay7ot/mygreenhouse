package com.example.mygreenhouse.ui.screens.auth

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.repository.AuthRepository
import com.example.mygreenhouse.util.BiometricUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application.applicationContext)

    val isPinLockEnabled: StateFlow<Boolean> = authRepository.isPinLockEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.getIsPinLockEnabled() // Initial check
        )

    val isPinSet: StateFlow<Boolean> = authRepository.isPinLockEnabledFlow
        .map { isEnabled -> isEnabled && authRepository.isPinCurrentlySet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.isPinCurrentlySet()
        )
        
    val isBiometricEnabled: StateFlow<Boolean> = authRepository.isBiometricEnabledFlow

    val isAuthenticatedInSession: StateFlow<Boolean> = authRepository.isAuthenticatedInSession
        
    val isBiometricAvailable: Boolean
        get() = authRepository.isBiometricAvailable()

    /**
     * Check if authentication is required (PIN enabled but not authenticated this session)
     */
    fun isAuthenticationRequired(): Boolean {
        return authRepository.isAuthenticationRequired()
    }

    fun setPin(pin: String): Result<Unit> {
        return try {
            authRepository.setPin(pin)
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e) // e.g., PIN too short
        } catch (e: Exception) {
            Result.failure(e) // Other unexpected errors
        }
    }

    fun verifyPin(pin: String): Boolean {
        return authRepository.verifyPin(pin)
    }

    fun enablePinLock(enable: Boolean) {
        viewModelScope.launch {
            authRepository.enablePinLock(enable)
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            authRepository.clearPin()
        }
    }
    
    /**
     * Enable or disable biometric authentication
     * @return true if successful, false if biometric is not available or PIN is not set
     */
    fun enableBiometric(enable: Boolean): Boolean {
        return authRepository.enableBiometric(enable)
    }

    /**
     * Mark user as authenticated in the current session
     */
    fun markAuthenticatedInSession() {
        authRepository.markAuthenticatedInSession()
    }
    
    /**
     * Show biometric authentication prompt
     * @param activity The activity where the prompt will be shown
     * @param onSuccess Callback for successful authentication
     * @param onError Callback for authentication error
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit
    ) {
        BiometricUtil.showBiometricPrompt(
            activity = activity,
            title = "Biometric Authentication",
            subtitle = "Use your biometric to unlock the app",
            description = "Authentication is required to access your greenhouse data",
            negativeButtonText = "Cancel",
            onAuthSuccess = {
                markAuthenticatedInSession()
                onSuccess()
            },
            onAuthError = onError
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                AuthViewModel(application)
            }
        }
    }
} 