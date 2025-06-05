package com.example.mygreenhouse.ui.screens.quickstats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.PlantStageTransition
import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.PlantStageTransitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
    import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * UI state for the Quick Stats screen
 */
data class QuickStatsUiState(
    val isLoading: Boolean = true,
    val totalActivePlants: Int = 0,
    val dryingCount: Int = 0,
    val curingCount: Int = 0,
    val plantsByStage: Map<GrowthStage, Int> = emptyMap(),
    val averageDaysInStage: Map<GrowthStage, Float> = emptyMap(),
    val selectedStrain: String = "Across All Strains",
    val strainNameOptions: List<String> = emptyList()
)

/**
 * ViewModel for the Quick Stats screen
 */
class QuickStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val plantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    private val stageTransitionRepository = PlantStageTransitionRepository(AppDatabase.getDatabase(application).plantStageTransitionDao())
    
    private val _uiState = MutableStateFlow(QuickStatsUiState())
    val uiState: StateFlow<QuickStatsUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            plantRepository.allActivePlants.combine(_uiState.map { it.selectedStrain }.distinctUntilChanged()) { plants, strain ->
                Pair(plants, strain)
            }.collect { (plants, strain) -> 
                loadStatistics(plants, strain)
            }
        }
    }
    
    fun updateSelectedStrain(strain: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedStrain = strain)
        }
    }

    private suspend fun loadStatistics(plants: List<Plant>?, selectedStrainFilter: String) {
        if (plants == null) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            return
        }
        val activeDisplayPlants = plants

        // Calculate quantities instead of counts
        val dryingQuantity = activeDisplayPlants
            .filter { it.growthStage == GrowthStage.DRYING }
            .sumOf { it.quantity }
        val curingQuantity = activeDisplayPlants
            .filter { it.growthStage == GrowthStage.CURING }
            .sumOf { it.quantity }
        val totalActivePlantQuantity = activeDisplayPlants
            .filter { it.growthStage != GrowthStage.DRYING && it.growthStage != GrowthStage.CURING }
            .sumOf { it.quantity }
        
        // Calculate plants by stage using quantities
        val plantsByStage = GrowthStage.values().associateWith { stage ->
            activeDisplayPlants
                .filter { it.growthStage == stage }
                .sumOf { it.quantity }
        }
        
        // Build strain options with strain name + batch number combinations
        val strainOptions = mutableListOf("Across All Strains")
        val strainBatchCombinations = activeDisplayPlants
            .map { "${it.strainName} - ${it.batchNumber}" }
            .distinct()
            .sorted()
        strainOptions.addAll(strainBatchCombinations)

        // Filter plants for average calculation based on strain + batch combination
        val plantsForAvgCalc = if (selectedStrainFilter == "Across All Strains") {
            activeDisplayPlants
        } else {
            activeDisplayPlants.filter { "${it.strainName} - ${it.batchNumber}" == selectedStrainFilter }
        }

        val today = LocalDate.now()
        val relevantStages = GrowthStage.values().filter { 
            it !in listOf(GrowthStage.DRYING, GrowthStage.CURING)
        }

        val stageDurations = mutableMapOf<GrowthStage, MutableList<Long>>()

        for (plant in plantsForAvgCalc) {
            // Use new stage-specific start dates if available, otherwise fall back to transitions
            val stageStartDates = mapOf(
                GrowthStage.GERMINATION to plant.germinationStartDate,
                GrowthStage.SEEDLING to plant.seedlingStartDate,
                GrowthStage.NON_ROOTED to plant.nonRootedStartDate,
                GrowthStage.ROOTED to plant.rootedStartDate,
                GrowthStage.VEGETATION to plant.vegetationStartDate,
                GrowthStage.FLOWER to plant.flowerStartDate
            )
            
            // Get transitions for fallback calculation
            val transitions = stageTransitionRepository.getTransitionsForPlantOnce(plant.id)
            val sortedTransitions = transitions.sortedBy { it.transitionDate }
            
            // Calculate stage durations using the new stage start dates when available
            for (stage in relevantStages) {
                val stageStartDate = stageStartDates[stage]
                
                if (stageStartDate != null) {
                    // Find the next stage date
                    val nextStageDate = when (stage) {
                        GrowthStage.GERMINATION -> plant.seedlingStartDate
                        GrowthStage.SEEDLING -> plant.vegetationStartDate
                        GrowthStage.NON_ROOTED -> plant.rootedStartDate
                        GrowthStage.ROOTED -> plant.vegetationStartDate
                        GrowthStage.VEGETATION -> plant.flowerStartDate
                        GrowthStage.FLOWER -> plant.dryingStartDate
                        else -> null
                    }
                    
                    val endDate = when {
                        nextStageDate != null -> nextStageDate
                        stage == plant.growthStage -> null // Don't count current incomplete stage
                        plant.growthStage == GrowthStage.DRYING && stage == GrowthStage.FLOWER -> plant.dryingStartDate
                        plant.growthStage == GrowthStage.CURING && stage == GrowthStage.FLOWER -> plant.dryingStartDate
                        else -> {
                            // Find end date from transitions as fallback
                            val nextTransition = sortedTransitions.find { 
                                it.transitionDate.isAfter(stageStartDate) && it.stage != stage 
                            }
                            nextTransition?.transitionDate
                        }
                    }
                    
                    if (endDate != null) {
                        val duration = ChronoUnit.DAYS.between(stageStartDate, endDate)
                        if (duration >= 0) {
                            stageDurations.computeIfAbsent(stage) { mutableListOf() }.add(duration)
                        }
                    }
                }
            }
            
            // Fallback to transition-based calculation if no stage dates are set
            if (stageStartDates.values.all { it == null } && sortedTransitions.isNotEmpty()) {
                for (i in sortedTransitions.indices) {
                    val currentTransition = sortedTransitions[i]
                    if (currentTransition.stage in relevantStages) {
                        val startDate = currentTransition.transitionDate
                        val endDate = if (i + 1 < sortedTransitions.size) {
                            sortedTransitions[i + 1].transitionDate
                        } else {
                            when (plant.growthStage) {
                                GrowthStage.DRYING -> plant.dryingStartDate ?: currentTransition.transitionDate
                                GrowthStage.CURING -> plant.curingStartDate ?: currentTransition.transitionDate
                                else -> {
                                    if (currentTransition.stage == plant.growthStage) {
                                        null // Skip current stage as it's incomplete
                                    } else {
                                        today // For completed stages, calculate until now
                                    }
                                }
                            }
                        }
                        
                        if (endDate != null) {
                            val duration = ChronoUnit.DAYS.between(startDate, endDate)
                            if (duration >= 0) {
                                stageDurations.computeIfAbsent(currentTransition.stage) { mutableListOf() }.add(duration)
                            }
                        }
                    }
                }
            }
        }

        val averageDaysInStage = relevantStages.associateWith { stage ->
            val durations = stageDurations[stage]
            if (durations.isNullOrEmpty()) 0f else {
                durations.average().toFloat()
            }
        }
        
        _uiState.value = QuickStatsUiState(
            isLoading = false,
            totalActivePlants = totalActivePlantQuantity,
            dryingCount = dryingQuantity,
            curingCount = curingQuantity,
            plantsByStage = plantsByStage,
            averageDaysInStage = averageDaysInStage,
            selectedStrain = selectedStrainFilter,
            strainNameOptions = strainOptions
        )
    }
    
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY] as Application
                QuickStatsViewModel(application)
            }
        }
    }
} 