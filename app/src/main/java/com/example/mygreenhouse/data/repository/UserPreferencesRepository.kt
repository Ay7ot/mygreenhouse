package com.example.mygreenhouse.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mygreenhouse.ui.settings.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }

    val themePreferenceFlow: Flow<ThemePreference> = context.dataStore.data
        .map {
            preferences ->
            val themeName = preferences[PreferencesKeys.THEME_PREFERENCE] ?: ThemePreference.SYSTEM.name
            try {
                ThemePreference.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                // Fallback to system default if the stored value is invalid
                ThemePreference.SYSTEM
            }
        }

    suspend fun updateThemePreference(themePreference: ThemePreference) {
        try {
            context.dataStore.edit {
                preferences ->
                preferences[PreferencesKeys.THEME_PREFERENCE] = themePreference.name
            }
        } catch (e: IOException) {
            // Handle error, e.g., log it or inform the user
            // For now, we'll just let it fail silently in this example
        }
    }
} 