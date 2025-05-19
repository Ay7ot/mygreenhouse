package com.example.mygreenhouse.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.repository.UserPreferencesRepository
import com.example.mygreenhouse.ui.settings.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.app.Application // Added for Application context

class SettingsViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    private val _themePreference = MutableStateFlow(ThemePreference.SYSTEM) // Default until loaded
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    private val _isLoadingTheme = MutableStateFlow(true)
    val isLoadingTheme: StateFlow<Boolean> = _isLoadingTheme.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.themePreferenceFlow.collectLatest {
                preference -> 
                _themePreference.value = preference
                _isLoadingTheme.value = false // Theme is now loaded
            }
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(preference)
            // The flow collection in init block will update _themePreference.value automatically
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Get the Application context to create the repository
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)
                SettingsViewModel(userPreferencesRepository)
            }
        }
    }
} 