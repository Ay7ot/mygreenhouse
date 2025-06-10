package com.example.mygreenhouse.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.mygreenhouse.util.BiometricUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest

class AuthRepository(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
        
        private const val AUTH_PREFERENCES_FILENAME = "auth_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_IS_PIN_LOCK_ENABLED = "is_pin_lock_enabled"
        private const val KEY_IS_BIOMETRIC_ENABLED = "is_biometric_enabled"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        AUTH_PREFERENCES_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isPinLockEnabledFlow = MutableStateFlow(sharedPreferences.getBoolean(KEY_IS_PIN_LOCK_ENABLED, false))
    val isPinLockEnabledFlow: StateFlow<Boolean> = _isPinLockEnabledFlow
    
    private val _isBiometricEnabledFlow = MutableStateFlow(sharedPreferences.getBoolean(KEY_IS_BIOMETRIC_ENABLED, false))
    val isBiometricEnabledFlow: StateFlow<Boolean> = _isBiometricEnabledFlow

    // Session state tracking - reset when app is killed
    private val _isAuthenticatedInSession = MutableStateFlow(false)
    val isAuthenticatedInSession: StateFlow<Boolean> = _isAuthenticatedInSession

    private fun hashPin(pin: String): String {
        // In a real app, use a strong, salted hash. For this example, SHA-256 without salt.
        // Consider using a library like BouncyCastle or similar for robust hashing and salting.
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun setPin(pin: String) {
        if (pin.length < 4) {
            throw IllegalArgumentException("PIN must be at least 4 digits.")
        }
        val hashedPin = hashPin(pin)
        sharedPreferences.edit()
            .putString(KEY_PIN_HASH, hashedPin)
            .putBoolean(KEY_IS_PIN_LOCK_ENABLED, true)
            .apply()
        _isPinLockEnabledFlow.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        val isValid = hashPin(pin) == storedHash
        if (isValid) {
            _isAuthenticatedInSession.value = true
        }
        return isValid
    }

    fun isPinCurrentlySet(): Boolean {
        return sharedPreferences.getString(KEY_PIN_HASH, null) != null && getIsPinLockEnabled()
    }
    
    fun getIsPinLockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PIN_LOCK_ENABLED, false)
    }

    fun enablePinLock(enabled: Boolean) {
        // Only allow enabling if a PIN is actually set
        if (enabled && sharedPreferences.getString(KEY_PIN_HASH, null) == null) {
            _isPinLockEnabledFlow.value = false // Ensure flow reflects reality
            return // Or throw an exception: "Cannot enable PIN lock without a PIN set."
        }
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PIN_LOCK_ENABLED, enabled)
            .apply()
        _isPinLockEnabledFlow.value = enabled
    }

    fun clearPin() {
        sharedPreferences.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_IS_PIN_LOCK_ENABLED, false)
            .putBoolean(KEY_IS_BIOMETRIC_ENABLED, false) // Also disable biometric when PIN is cleared
            .apply()
        _isPinLockEnabledFlow.value = false
        _isBiometricEnabledFlow.value = false
        _isAuthenticatedInSession.value = false
    }

    /**
     * Mark user as authenticated in the current session (for biometric auth)
     */
    fun markAuthenticatedInSession() {
        _isAuthenticatedInSession.value = true
    }

    /**
     * Check if authentication is required (PIN is enabled but user hasn't authenticated this session)
     */
    fun isAuthenticationRequired(): Boolean {
        return getIsPinLockEnabled() && !_isAuthenticatedInSession.value
    }
    
    // Biometric related functions
    
    /**
     * Check if biometric authentication is available on this device
     */
    fun isBiometricAvailable(): Boolean {
        return BiometricUtil.isBiometricAvailable(context)
    }
    
    /**
     * Enable or disable biometric authentication
     * @param enabled true to enable, false to disable
     * @return true if successful, false if biometric is not available or PIN is not set
     */
    fun enableBiometric(enabled: Boolean): Boolean {
        // Only allow enabling if PIN is set and biometric is available
        if (enabled && (!isPinCurrentlySet() || !isBiometricAvailable())) {
            _isBiometricEnabledFlow.value = false // Ensure it's false if conditions fail
            return false
        }
        
        sharedPreferences.edit()
            .putBoolean(KEY_IS_BIOMETRIC_ENABLED, enabled)
            .apply()
        _isBiometricEnabledFlow.value = enabled
        return true
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BIOMETRIC_ENABLED, false)
    }

    /**
     * Force refresh the state flows with current values from SharedPreferences
     */
    private fun refreshStateFlows() {
        _isPinLockEnabledFlow.value = sharedPreferences.getBoolean(KEY_IS_PIN_LOCK_ENABLED, false)
        _isBiometricEnabledFlow.value = sharedPreferences.getBoolean(KEY_IS_BIOMETRIC_ENABLED, false)
    }
} 