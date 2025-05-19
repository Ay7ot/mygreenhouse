package com.example.mygreenhouse.util

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Utility class for biometric authentication operations
 */
class BiometricUtil {
    companion object {
        private const val TAG = "BiometricUtil"
        
        /**
         * Check if the device supports biometric authentication
         * @return true if the device supports biometric authentication and has enrolled biometrics
         */
        fun isBiometricAvailable(context: Context): Boolean {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            
            return when (canAuthenticate) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d(TAG, "Biometric features are available")
                    true
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Log.e(TAG, "No biometric features available on this device")
                    false
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Log.e(TAG, "Biometric features are currently unavailable")
                    false
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Log.e(TAG, "User hasn't enrolled any biometrics")
                    false
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    Log.e(TAG, "Security update required for biometric authentication")
                    false
                }
                else -> {
                    Log.e(TAG, "Unknown biometric error: $canAuthenticate")
                    false
                }
            }
        }
        
        /**
         * Show the biometric prompt for authentication
         * @param activity The activity where the prompt will be shown
         * @param title The title of the prompt
         * @param subtitle The subtitle of the prompt
         * @param description The description of the prompt
         * @param negativeButtonText The text for the negative button
         * @param onAuthSuccess Callback for successful authentication
         * @param onAuthError Callback for authentication error
         */
        fun showBiometricPrompt(
            activity: FragmentActivity,
            title: String,
            subtitle: String,
            description: String,
            negativeButtonText: String,
            onAuthSuccess: () -> Unit,
            onAuthError: (Int, String) -> Unit
        ) {
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Biometric authentication succeeded")
                    onAuthSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Biometric authentication error [$errorCode]: $errString")
                    onAuthError(errorCode, errString.toString())
                }
            }
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                .build()
            
            BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
        }
    }
} 