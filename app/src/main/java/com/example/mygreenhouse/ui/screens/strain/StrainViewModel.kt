package com.example.mygreenhouse.ui.screens.strain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Strain
import com.example.mygreenhouse.data.repository.StrainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for strain management operations
 */
class StrainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val strainRepository = StrainRepository(AppDatabase.getDatabase(application).strainDao())
    
    // Exposed data
    val allStrains: StateFlow<List<Strain>> = strainRepository.allStrains
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val strainCount: StateFlow<Int> = strainRepository.strainCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
    val customStrainCount: StateFlow<Int> = strainRepository.customStrainCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    /**
     * Add a new strain to the archive
     */
    fun addStrain(strainName: String, isCustomStrain: Boolean = false, notes: String = "") {
        viewModelScope.launch {
            strainRepository.archiveStrainName(strainName, isCustomStrain)
        }
    }
    
    /**
     * Update an existing strain
     */
    fun updateStrain(strain: Strain) {
        viewModelScope.launch {
            strainRepository.updateStrain(strain)
        }
    }
    
    /**
     * Delete a strain from the archive
     */
    fun deleteStrain(strain: Strain) {
        viewModelScope.launch {
            strainRepository.deleteStrain(strain)
        }
    }
    
    /**
     * Archive a strain name (used when a plant is saved)
     */
    fun archiveStrainName(strainName: String, isCustomStrain: Boolean = false) {
        viewModelScope.launch {
            strainRepository.archiveStrainName(strainName, isCustomStrain)
        }
    }
    
    /**
     * Get strain by ID
     */
    fun getStrainById(id: String) = strainRepository.getStrainById(id)
    
    /**
     * Search strains by name
     */
    fun searchStrainsByName(query: String) = strainRepository.searchStrainsByName(query)
    
    /**
     * Reset strain usage count to 1 (for fixing incorrect counts)
     */
    fun resetStrainUsageCount(strainName: String) {
        viewModelScope.launch {
            strainRepository.resetStrainUsageCount(strainName)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                StrainViewModel(application)
            }
        }
    }
} 