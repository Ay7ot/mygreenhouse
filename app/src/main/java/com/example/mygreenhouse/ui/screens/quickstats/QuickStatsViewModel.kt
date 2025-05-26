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

        val dryingCount = activeDisplayPlants.count { it.growthStage == GrowthStage.DRYING }
        val curingCount = activeDisplayPlants.count { it.growthStage == GrowthStage.CURING }
        val totalActivePlants = activeDisplayPlants.count { 
            it.growthStage != GrowthStage.DRYING && it.growthStage != GrowthStage.CURING 
        }
        
        val plantsByStage = GrowthStage.values().associateWith { stage ->
            activeDisplayPlants.count { it.growthStage == stage }
        }
        
        val strainOptions = mutableListOf("Across All Strains")
        strainOptions.addAll(activeDisplayPlants.map { it.strainName }.distinct().sorted())

        val plantsForAvgCalc = if (selectedStrainFilter == "Across All Strains") {
            activeDisplayPlants
        } else {
            activeDisplayPlants.filter { it.strainName == selectedStrainFilter }
        }

        val today = LocalDate.now()
        val relevantStages = GrowthStage.values().filter { 
            it !in listOf(GrowthStage.DRYING, GrowthStage.CURING)
        }

        val stageDurations = mutableMapOf<GrowthStage, MutableList<Long>>()

        for (plant in plantsForAvgCalc) {
            val transitions = stageTransitionRepository.getTransitionsForPlantOnce(plant.id)
            if (transitions.isNotEmpty()) {
                for (i in transitions.indices) {
                    val currentTransition = transitions[i]
                    if (currentTransition.stage in relevantStages) {
                        val startDate = currentTransition.transitionDate
                        val endDate = if (i + 1 < transitions.size) {
                            transitions[i + 1].transitionDate
                        } else {
                            when (plant.growthStage) {
                                GrowthStage.DRYING -> plant.dryingStartDate ?: today
                                GrowthStage.CURING -> plant.curingStartDate ?: today
                                else -> today
                            }
                        }
                        val duration = ChronoUnit.DAYS.between(startDate, endDate)
                        if (duration >= 0) {
                             stageDurations.computeIfAbsent(currentTransition.stage) { mutableListOf() }.add(duration)
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
            totalActivePlants = totalActivePlants,
            dryingCount = dryingCount,
            curingCount = curingCount,
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