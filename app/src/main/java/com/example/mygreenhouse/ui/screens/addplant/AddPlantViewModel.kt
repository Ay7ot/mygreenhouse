package com.example.mygreenhouse.ui.screens.addplant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.PlantSource
import com.example.mygreenhouse.data.model.PlantType
import com.example.mygreenhouse.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * UI state for the Add Plant screen
 */
data class AddPlantUiState(
    val strainName: String = "",
    val batchNumber: String = "",
    val source: PlantSource? = null,
    val sourceDisplay: String = "Select",
    val plantTypeSelection: String = "Select",
    val type: PlantType? = null,
    val growthStage: GrowthStage? = null,
    val growthStageDisplay: String = "Select",
    val availableGrowthStages: List<GrowthStage> = emptyList(),
    val durationLabel: String = "Duration (days)",
    val durationText: String = "",
    val showDurationField: Boolean = false,
    val startDate: LocalDate = LocalDate.now(),
    val startDateText: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val currentNutrientInput: String = "",
    val nutrientsList: List<String> = emptyList(),
    val soilTypeDisplay: String = "Select",
    val selectedSoilType: String? = null,
    val imageUri: String? = null,
    val isValid: Boolean = false,
    val showSaveConfirmationDialog: Boolean = false,
    val plantJustSaved: Boolean = false
)

/**
 * ViewModel for the Add Plant screen
 */
class AddPlantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    
    private val _uiState = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()
    
    val soilTypeOptions = listOf("Select", "Coco Coir", "Soil", "Hydroponics", "Aeroponics", "Other")
    
    val plantTypeSelectionOptions = listOf("Select", "Autoflower Regular", "Autoflower Feminized", "Photoperiod Regular", "Photoperiod Feminized")
    
    fun updateStrainName(name: String) {
        _uiState.update { 
            it.copy(
                strainName = name,
                isValid = validateForm(
                    strainName = name,
                    batchNumber = it.batchNumber,
                    source = it.source,
                    type = it.type,
                    stage = it.growthStage
                )
            )
        }
    }
    
    fun updateBatchNumber(number: String) {
        _uiState.update { 
            it.copy(
                batchNumber = number,
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = number,
                    source = it.source,
                    type = it.type,
                    stage = it.growthStage
                )
            )
        }
    }
    
    fun updateSource(source: PlantSource) {
        val displayText = when (source) {
            PlantSource.SEED -> "Seed"
            PlantSource.CLONE -> "Clone"
        }
        
        val newAvailableGrowthStages = when (source) {
            PlantSource.SEED -> listOf(
                GrowthStage.GERMINATION, 
                GrowthStage.SEEDLING, 
                GrowthStage.VEGETATION, 
                GrowthStage.FLOWER, 
                GrowthStage.DRYING, 
                GrowthStage.CURING
            )
            PlantSource.CLONE -> listOf(
                GrowthStage.NON_ROOTED,
                GrowthStage.ROOTED,
                GrowthStage.VEGETATION,
                GrowthStage.FLOWER,
                GrowthStage.DRYING,
                GrowthStage.CURING
            )
        }
        
        _uiState.update { 
            it.copy(
                source = source,
                sourceDisplay = displayText,
                growthStage = null,
                growthStageDisplay = "Select",
                availableGrowthStages = newAvailableGrowthStages,
                plantTypeSelection = "Select",
                type = null,
                showDurationField = if (source == PlantSource.CLONE) false else determineShowDurationField(null),
                durationLabel = determineDurationLabel(null, source),
                durationText = if (source == PlantSource.CLONE) "" else it.durationText,
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = it.batchNumber,
                    source = source,
                    type = null,
                    stage = null
                )
            )
        }
    }
    
    fun updatePlantTypeSelection(selectedDisplayString: String) {
        val newPlantType: PlantType? = when {
            selectedDisplayString.startsWith("Autoflower") -> PlantType.AUTOFLOWER
            selectedDisplayString.startsWith("Photoperiod") -> PlantType.PHOTOPERIOD
            else -> null
        }

        val showField = determineShowDurationField(newPlantType)
        val durationLabelText = determineDurationLabel(newPlantType, _uiState.value.source)
        
        _uiState.update { 
            if (it.source == PlantSource.CLONE) {
                 it.copy(
                    plantTypeSelection = "Select",
                    type = null,
                    showDurationField = false,
                    durationLabel = "Duration (days)",
                    durationText = "",
                    isValid = validateForm(
                        strainName = it.strainName,
                        batchNumber = it.batchNumber,
                        source = it.source,
                        type = null,
                        stage = it.growthStage
                    )
                )
            } else {
                it.copy(
                    plantTypeSelection = selectedDisplayString,
                    type = newPlantType,
                    durationLabel = durationLabelText,
                    showDurationField = showField,
                    durationText = if (!showField) "" else it.durationText,
                    isValid = validateForm(
                        strainName = it.strainName,
                        batchNumber = it.batchNumber,
                        source = it.source,
                        type = newPlantType,
                        stage = it.growthStage
                    )
                )
            }
        }
    }

    private fun determineShowDurationField(plantType: PlantType?): Boolean {
        return plantType != null
    }

    private fun determineDurationLabel(plantType: PlantType?, plantSource: PlantSource?): String {
        if (plantSource == PlantSource.CLONE) return "Duration (days)"
        return when (plantType) {
            PlantType.AUTOFLOWER -> "Seed to Harvest (days)"
            PlantType.PHOTOPERIOD -> "Flower Duration (days)"
            null -> "Duration (days)"
        }
    }
    
    fun updateGrowthStage(stage: GrowthStage) {
        val displayText = stage.name.replace("_", " ").lowercase().capitalize()
        
        _uiState.update { 
            it.copy(
                growthStage = stage,
                growthStageDisplay = displayText,
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = it.batchNumber,
                    source = it.source,
                    type = it.type,
                    stage = stage
                )
            )
        }
    }
    
    fun updateDurationText(duration: String) {
        _uiState.update { it.copy(durationText = duration) }
    }
    
    fun updateStartDate(date: LocalDate) {
        _uiState.update { 
            it.copy(
                startDate = date,
                startDateText = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }
    }
    
    fun updateCurrentNutrientInput(input: String) {
        _uiState.update { it.copy(currentNutrientInput = input) }
    }

    fun addNutrient() {
        _uiState.update { currentState ->
            val newNutrient = currentState.currentNutrientInput.trim()
            if (newNutrient.isNotEmpty() && !currentState.nutrientsList.contains(newNutrient)) {
                currentState.copy(
                    nutrientsList = currentState.nutrientsList + newNutrient,
                    currentNutrientInput = "" 
                )
            } else {
                currentState 
            }
        }
    }

    fun removeNutrient(nutrient: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nutrientsList = currentState.nutrientsList - nutrient
            )
        }
    }
    
    fun updateSoilType(soilType: String) {
        _uiState.update {
            it.copy(
                soilTypeDisplay = soilType,
                selectedSoilType = if (soilType == "Select") null else soilType
            )
        }
    }
    
    fun updateImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }
    
    fun savePlant() {
        val currentState = uiState.value
        
        if (!validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage)) return
        
        val seedToHarvestDays = if (currentState.type == PlantType.AUTOFLOWER) {
            currentState.durationText.toIntOrNull()
        } else null
        
        val flowerDurationDays = if (currentState.type == PlantType.PHOTOPERIOD) {
            currentState.durationText.toIntOrNull()
        } else null
        
        val plant = Plant(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source ?: PlantSource.SEED,
            type = currentState.type,
            growthStage = currentState.growthStage ?: GrowthStage.GERMINATION,
            startDate = currentState.startDate,
            lastUpdated = LocalDate.now(),
            seedToHarvestDays = seedToHarvestDays,
            flowerDurationDays = flowerDurationDays,
            soilType = currentState.selectedSoilType,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri
        )
        
        viewModelScope.launch {
            repository.insertPlant(plant)
            _uiState.update {
                it.copy(plantJustSaved = true, showSaveConfirmationDialog = true)
            }
        }
    }

    fun onConfirmAddAnother() {
        _uiState.update { AddPlantUiState() }
    }

    fun onDismissAddAnotherDialog(navigateToDashboard: Boolean) {
        _uiState.update { it.copy(showSaveConfirmationDialog = false, plantJustSaved = navigateToDashboard) }
    }

    fun resetPlantJustSavedFlag() {
        _uiState.update { it.copy(plantJustSaved = false) }
    }
    
    private fun validateForm(
        strainName: String,
        batchNumber: String,
        source: PlantSource?,
        type: PlantType?,
        stage: GrowthStage?
    ): Boolean {
        val typeValid = if (source == PlantSource.SEED) type != null else true
        
        return strainName.isNotEmpty() &&
                batchNumber.isNotEmpty() &&
                source != null &&
                typeValid &&
                stage != null
    }
    
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this
        else this[0].uppercaseChar() + this.substring(1)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                AddPlantViewModel(application)
            }
        }
    }
} 