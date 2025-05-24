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
    val uniqueStrainCount: Int = 0,
    val autoflowerRegularSeedCount: Int = 0,
    val autoflowerFeminizedSeedCount: Int = 0,
    val photoperiodRegularSeedCount: Int = 0,
    val photoperiodFeminizedSeedCount: Int = 0,
    
    // Search and filter
    val searchQuery: String = "",
    val showDryingOnly: Boolean = false,
    val showCuringOnly: Boolean = false,
    val showCompletedOnly: Boolean = false,
    val selectedSeedType: SeedType? = null
)

/**
 * Enum to define harvest filter types
 */
enum class HarvestFilterType {
    ALL,
    DRYING,
    CURING,
    COMPLETED
}

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
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _harvestFilter = MutableStateFlow(HarvestFilterType.ALL)
    val harvestFilter: StateFlow<HarvestFilterType> = _harvestFilter.asStateFlow()
    
    private val _seedTypeFilter = MutableStateFlow<SeedType?>(null)
    val seedTypeFilter: StateFlow<SeedType?> = _seedTypeFilter.asStateFlow()

    // Data states
    val allHarvests: StateFlow<List<Harvest>?> = harvestRepository.allHarvests
        .map { it as List<Harvest>? } // Ensure it's nullable for stateIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Changed to null
        )
        
    // Filtered harvests based on search query and filter type
    val filteredHarvests: StateFlow<List<Harvest>?> = combine(
        allHarvests,
        _searchQuery,
        _harvestFilter
    ) { harvests, query, filterType ->
        harvests?.let { nonNullHarvests ->
            var filtered = nonNullHarvests
            
            // Apply search query filter
            if (query.isNotBlank()) {
                filtered = filtered.filter { 
                    it.strainName.contains(query, ignoreCase = true) || 
                    it.batchNumber.contains(query, ignoreCase = true) || 
                    it.notes.contains(query, ignoreCase = true)
                }
            }
            
            // Apply filter type
            filtered = when (filterType) {
                HarvestFilterType.DRYING -> filtered.filter { it.isDrying }
                HarvestFilterType.CURING -> filtered.filter { it.isCuring }
                HarvestFilterType.COMPLETED -> filtered.filter { it.isCompleted }
                HarvestFilterType.ALL -> filtered
            }
            filtered
        } // If harvests is null, combine returns null implicitly if not handled, or we ensure null propagation
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // Changed to null
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
        
    val allSeeds: StateFlow<List<Seed>?> = seedRepository.allSeeds
        .map { it as List<Seed>? } // Ensure it's nullable for stateIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Changed to null
        )
        
    // Filtered seeds based on search query and filter type
    val filteredSeeds: StateFlow<List<Seed>?> = combine(
        allSeeds,
        _searchQuery,
        _seedTypeFilter
    ) { seeds, query, seedType ->
        seeds?.let { nonNullSeeds ->
            var filtered = nonNullSeeds
            
            // Apply search query filter
            if (query.isNotBlank()) {
                filtered = filtered.filter { 
                    it.strainName.contains(query, ignoreCase = true) || 
                    it.batchNumber.contains(query, ignoreCase = true) || 
                    it.breeder.contains(query, ignoreCase = true) ||
                    it.notes.contains(query, ignoreCase = true)
                }
            }
            
            // Apply seed type filter
            if (seedType != null) {
                filtered = filtered.filter { it.seedType == seedType }
            }
            filtered
        } // If seeds is null, combine returns null implicitly
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // Changed to null
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
        dryingHarvests, // Stays List<Harvest>, initial emptyList()
        curingHarvests,  // Stays List<Harvest>, initial emptyList()
        completedHarvests, // Stays List<Harvest>, initial emptyList()
        totalSeedCount,
        uniqueStrainCount,
        _searchQuery,
        _harvestFilter,
        _seedTypeFilter,
        allSeeds // This is now List<Seed>?
    ) { values ->
        val isLoading = values[0] as Boolean
        val selectedTab = values[1] as Int
        val totalHarvestedWeight = values[2] as Double
        val dryingHarvestsList = values[3] as List<Harvest>
        val curingHarvestsList = values[4] as List<Harvest>
        val completedHarvestsList = values[5] as List<Harvest>
        val totalSeedCount = values[6] as Int
        val uniqueStrainCount = values[7] as Int
        val searchQuery = values[8] as String
        val harvestFilter = values[9] as HarvestFilterType
        val seedTypeFilter = values[10] as SeedType?
        val currentAllSeeds = values[11] as List<Seed>? // Now nullable
        
        // Calculate seed counts by type, handling nullable currentAllSeeds
        val autoflowerRegularSeedCount = currentAllSeeds?.filter { it.seedType == SeedType.AUTOFLOWER_REGULAR }?.sumOf { it.seedCount } ?: 0
        val autoflowerFeminizedSeedCount = currentAllSeeds?.filter { it.seedType == SeedType.AUTOFLOWER_FEMINIZED }?.sumOf { it.seedCount } ?: 0
        val photoperiodRegularSeedCount = currentAllSeeds?.filter { it.seedType == SeedType.PHOTOPERIOD_REGULAR }?.sumOf { it.seedCount } ?: 0
        val photoperiodFeminizedSeedCount = currentAllSeeds?.filter { it.seedType == SeedType.PHOTOPERIOD_FEMINIZED }?.sumOf { it.seedCount } ?: 0

        DankBankUiState(
            isLoading = isLoading,
            selectedTab = selectedTab,
            totalHarvestedWeight = totalHarvestedWeight,
            dryingCount = dryingHarvestsList.size,
            curingCount = curingHarvestsList.size,
            completedCount = completedHarvestsList.size,
            totalSeedCount = totalSeedCount,
            uniqueStrainCount = uniqueStrainCount,
            autoflowerRegularSeedCount = autoflowerRegularSeedCount,
            autoflowerFeminizedSeedCount = autoflowerFeminizedSeedCount,
            photoperiodRegularSeedCount = photoperiodRegularSeedCount,
            photoperiodFeminizedSeedCount = photoperiodFeminizedSeedCount,
            searchQuery = searchQuery,
            showDryingOnly = harvestFilter == HarvestFilterType.DRYING,
            showCuringOnly = harvestFilter == HarvestFilterType.CURING,
            showCompletedOnly = harvestFilter == HarvestFilterType.COMPLETED,
            selectedSeedType = seedTypeFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DankBankUiState() // DankBankUiState.isLoading is true by default
    )

    init {
        viewModelScope.launch {
            delay(1000) // Simulate initial loading if needed, or remove
            _isLoading.value = false
        }
    }
    
    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Set harvest filter
     */
    fun setHarvestFilter(filterType: HarvestFilterType) {
        _harvestFilter.value = filterType
    }
    
    /**
     * Set seed type filter
     */
    fun setSeedTypeFilter(seedType: SeedType?) {
        _seedTypeFilter.value = seedType
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
     * Update basic harvest information
     */
    fun updateHarvestBasicInfo(
        harvestId: String,
        strainName: String,
        batchNumber: String,
        harvestDate: LocalDate,
        wetWeight: Double?,
        notes: String
    ) {
        viewModelScope.launch {
            val harvest = harvestRepository.getHarvestById(harvestId).first()
            if (harvest == null) return@launch
            
            // Update only the basic info, preserving the processing state and other details
            val updatedHarvest = harvest.copy(
                strainName = strainName,
                batchNumber = batchNumber,
                harvestDate = harvestDate,
                wetWeight = wetWeight,
                notes = notes
            )
            
            harvestRepository.updateHarvest(updatedHarvest)
        }
    }
    
    /**
     * Get harvest by ID
     */
    fun getHarvestById(harvestId: String) = harvestRepository.getHarvestById(harvestId)
    
    /**
     * Get seed by ID
     */
    fun getSeedById(seedId: String) = seedRepository.getSeedById(seedId)
    
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
        seedType: SeedType = SeedType.AUTOFLOWER_REGULAR,
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