package com.example.mygreenhouse.ui.screens.datamanagement

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.service.DataExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val dataExportService = DataExportService(application.applicationContext)
    
    // State variables
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Initial)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Initial)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Export data to a file
    fun exportData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uri = dataExportService.exportAllData()
                if (uri != null) {
                    _exportState.value = ExportState.Success(uri)
                } else {
                    _exportState.value = ExportState.Error("Failed to export data")
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Import data from a file
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = dataExportService.importData(uri)
                if (success) {
                    _importState.value = ImportState.Success
                } else {
                    _importState.value = ImportState.Error("Failed to import data")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Clear all data
    fun clearAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = dataExportService.clearAllData()
                if (success) {
                    _exportState.value = ExportState.Initial
                    _importState.value = ImportState.Initial
                } else {
                    _exportState.value = ExportState.Error("Failed to clear data")
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Reset export state
    fun resetExportState() {
        _exportState.value = ExportState.Initial
    }
    
    // Reset import state
    fun resetImportState() {
        _importState.value = ImportState.Initial
    }
    
    // Export state sealed class
    sealed class ExportState {
        object Initial : ExportState()
        data class Success(val uri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
    }
    
    // Import state sealed class
    sealed class ImportState {
        object Initial : ImportState()
        object Success : ImportState()
        data class Error(val message: String) : ImportState()
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                DataManagementViewModel(application)
            }
        }
    }
} 