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
import com.example.mygreenhouse.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * UI state for the Quick Stats screen
 */
data class QuickStatsUiState(
    val isLoading: Boolean = true,
    val totalActivePlants: Int = 0, // Renamed for clarity
    val dryingCount: Int = 0,
    val curingCount: Int = 0,
    val plantsByStage: Map<GrowthStage, Int> = emptyMap(),
    val averageDaysInStage: Map<GrowthStage, Int> = emptyMap(),
    val plantsCreatedByMonth: Map<String, Int> = emptyMap() // Month name to count
)

/**
 * ViewModel for the Quick Stats screen
 */
class QuickStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    
    private val _uiState = MutableStateFlow(QuickStatsUiState())
    val uiState: StateFlow<QuickStatsUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadStatistics()
        }
    }
    
    private suspend fun loadStatistics() {
        repository.allPlants.collect { plants -> // Still collect all plants for other stats
            // Filter out archived plants for general display unless explicitly needed for historical data
            val activeDisplayPlants = plants.filter { !it.isArchived }

            // Basic counts
            val dryingCount = activeDisplayPlants.count { it.growthStage == GrowthStage.DRYING }
            val curingCount = activeDisplayPlants.count { it.growthStage == GrowthStage.CURING }
            // Total plants EXCLUDING drying and curing, and also EXCLUDING archived
            val totalActivePlants = activeDisplayPlants.count { 
                it.growthStage != GrowthStage.DRYING && it.growthStage != GrowthStage.CURING 
            }
            
            // Plants by growth stage (based on active, non-archived plants)
            val plantsByStage = GrowthStage.values().associateWith { stage ->
                activeDisplayPlants.count { it.growthStage == stage }
            }
            
            // Calculate average days in current growth stage (based on active, non-archived plants)
            val today = LocalDate.now()
            val averageDaysInStage = GrowthStage.values().associateWith { stage ->
                val plantsInStage = activeDisplayPlants.filter { it.growthStage == stage }
                if (plantsInStage.isEmpty()) 0 else {
                    plantsInStage.sumOf { 
                        ChronoUnit.DAYS.between(it.lastUpdated, today).toInt().coerceAtLeast(0)
                    } / plantsInStage.size
                }
            }
            
            // Plants created by month (can use all plants, including archived, for historical trend)
            val plantsCreatedByMonth = calculatePlantsCreatedByMonth(plants)
            
            _uiState.value = QuickStatsUiState(
                isLoading = false,
                totalActivePlants = totalActivePlants,
                dryingCount = dryingCount,
                curingCount = curingCount,
                plantsByStage = plantsByStage,
                averageDaysInStage = averageDaysInStage,
                plantsCreatedByMonth = plantsCreatedByMonth
            )
        }
    }
    
    /**
     * Calculate number of plants created by month (last 6 months)
     */
    private fun calculatePlantsCreatedByMonth(plants: List<Plant>): Map<String, Int> {
        val today = LocalDate.now()
        val result = mutableMapOf<String, Int>()
        
        // Create entries for the last 6 months
        for (i in 5 downTo 0) {
            val month = today.minusMonths(i.toLong())
            val monthKey = "${month.month.name.lowercase().capitalize()} ${month.year}"
            result[monthKey] = 0
        }
        
        // Count plants per month
        plants.forEach { plant ->
            val monthKey = "${plant.startDate.month.name.lowercase().capitalize()} ${plant.startDate.year}"
            if (result.containsKey(monthKey)) {
                result[monthKey] = result[monthKey]!! + 1
            }
        }
        
        return result
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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