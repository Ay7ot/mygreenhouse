package com.example.mygreenhouse.ui.screens.dankbank

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.SeedType
import com.example.mygreenhouse.data.repository.HarvestRepository
import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.SeedRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * UI state for the Dank Bank screen
 */
data class DankBankUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0 for Harvests, 1 for Seeds
    
    // Harvests
    val totalHarvestedWeight: Double = 0.0,
    val dryingCount: Int = 0,
    val curingCount: Int = 0,
    val completedCount: Int = 0,
    
    // Seeds
    val totalSeedCount: Int = 0,
    val uniqueStrainCount: Int = 0
)

/**
 * ViewModel for the Dank Bank screen
 */
class DankBankViewModel(application: Application) : AndroidViewModel(application) {
    
    private val harvestRepository = HarvestRepository(AppDatabase.getDatabase(application).harvestDao())
    private val seedRepository = SeedRepository(AppDatabase.getDatabase(application).seedDao())
    val plantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    
    // Loading states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Selected tab
    private val _selectedTab = MutableStateFlow(0) // 0 for Harvests, 1 for Seeds
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Data states
    val allHarvests: StateFlow<List<Harvest>> = harvestRepository.allHarvests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val dryingHarvests: StateFlow<List<Harvest>> = harvestRepository.dryingHarvests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val curingHarvests: StateFlow<List<Harvest>> = harvestRepository.curingHarvests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val completedHarvests: StateFlow<List<Harvest>> = harvestRepository.completedHarvests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val totalHarvestedWeight: StateFlow<Double> = harvestRepository.totalHarvestedWeight
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
        
    val allSeeds: StateFlow<List<Seed>> = seedRepository.allSeeds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val totalSeedCount: StateFlow<Int> = seedRepository.totalSeedCount
        .map { it ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
    val uniqueStrainCount: StateFlow<Int> = seedRepository.uniqueStrainCount
        .map { it ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
    // Combined UI state
    val uiState: StateFlow<DankBankUiState> = combine(
        _isLoading,
        _selectedTab,
        totalHarvestedWeight,
        dryingHarvests,
        curingHarvests,
        completedHarvests,
        totalSeedCount,
        uniqueStrainCount
    ) { values ->
        val isLoading = values[0] as Boolean
        val selectedTab = values[1] as Int
        val totalHarvestedWeight = values[2] as Double
        val dryingHarvests = values[3] as List<Harvest>
        val curingHarvests = values[4] as List<Harvest>
        val completedHarvests = values[5] as List<Harvest>
        val totalSeedCount = values[6] as Int
        val uniqueStrainCount = values[7] as Int

        DankBankUiState(
            isLoading = isLoading,
            selectedTab = selectedTab,
            totalHarvestedWeight = totalHarvestedWeight,
            dryingCount = dryingHarvests.size,
            curingCount = curingHarvests.size,
            completedCount = completedHarvests.size,
            totalSeedCount = totalSeedCount,
            uniqueStrainCount = uniqueStrainCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DankBankUiState()
    )

    init {
        viewModelScope.launch {
            delay(1000) // Simulate initial loading if needed, or remove
            _isLoading.value = false
        }
    }
    
    /**
     * Set selected tab
     */
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }
    
    /**
     * Add a new harvest
     */
    fun addHarvest(
        plantId: String?,
        strainName: String,
        batchNumber: String,
        harvestDate: LocalDate,
        wetWeight: Double?,
        notes: String
    ) {
        viewModelScope.launch {
            val harvest = Harvest(
                plantId = plantId,
                strainName = strainName,
                batchNumber = batchNumber,
                harvestDate = harvestDate,
                wetWeight = wetWeight,
                notes = notes,
                isDrying = true
            )
            harvestRepository.insertHarvest(harvest)
        }
    }
    
    /**
     * Update a harvest with dry weight
     */
    fun updateHarvestWithDryWeight(
        harvestId: String,
        dryWeight: Double,
        dryingCompleteDate: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            val harvest = harvestRepository.getHarvestById(harvestId).first()
            if (harvest == null) return@launch
            val updatedHarvest = harvest.copy(
                dryWeight = dryWeight,
                dryingCompleteDate = dryingCompleteDate,
                isDrying = false,
                isCuring = true
            )
            harvestRepository.updateHarvest(updatedHarvest)
        }
    }
    
    /**
     * Complete a harvest with final cured weight
     */
    fun completeHarvest(
        harvestId: String,
        finalCuredWeight: Double,
        curingCompleteDate: LocalDate = LocalDate.now(),
        qualityRating: Int? = null
    ) {
        viewModelScope.launch {
            val harvest = harvestRepository.getHarvestById(harvestId).first()
            if (harvest == null) return@launch
            val updatedHarvest = harvest.copy(
                finalCuredWeight = finalCuredWeight,
                curingCompleteDate = curingCompleteDate,
                qualityRating = qualityRating,
                isDrying = false,
                isCuring = false,
                isCompleted = true
            )
            harvestRepository.updateHarvest(updatedHarvest)
        }
    }
    
    /**
     * Delete a harvest
     */
    fun deleteHarvest(harvest: Harvest) {
        viewModelScope.launch {
            harvestRepository.deleteHarvest(harvest)
        }
    }
    
    /**
     * Add a new seed entry
     */
    fun addSeed(
        strainName: String,
        batchNumber: String,
        seedCount: Int,
        breeder: String = "",
        seedType: SeedType = SeedType.REGULAR,
        acquisitionDate: LocalDate = LocalDate.now(),
        source: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val seed = Seed(
                strainName = strainName,
                batchNumber = batchNumber,
                seedCount = seedCount,
                breeder = breeder,
                seedType = seedType,
                acquisitionDate = acquisitionDate,
                source = source,
                notes = notes
            )
            seedRepository.insertSeed(seed)
        }
    }
    
    /**
     * Update a seed entry
     */
    fun updateSeed(seed: Seed) {
        viewModelScope.launch {
            seedRepository.updateSeed(seed)
        }
    }
    
    /**
     * Delete a seed entry
     */
    fun deleteSeed(seed: Seed) {
        viewModelScope.launch {
            seedRepository.deleteSeed(seed)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                DankBankViewModel(application)
            }
        }
    }
} 