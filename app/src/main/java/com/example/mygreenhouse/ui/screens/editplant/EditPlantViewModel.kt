package com.example.mygreenhouse.ui.screens.editplant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
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
 * UI state for the Edit Plant screen
 */
data class EditPlantUiState(
    val plantId: String = "",
    val strainName: String = "",
    val batchNumber: String = "",
    val source: PlantSource? = null,
    val sourceDisplay: String = "Select",
    val type: PlantType? = null,
    val typeDisplay: String = "Select",
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
    val originalPlant: Plant? = null, // To compare for changes or use for update
    val isLoading: Boolean = true,
    val isValid: Boolean = false,
    val showStartDatePicker: Boolean = false // Added for DatePickerDialog visibility
)

/**
 * ViewModel for the Edit Plant screen
 */
class EditPlantViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val repository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    private val plantId: String = savedStateHandle.get<String>("plantId") ?: ""

    private val _uiState = MutableStateFlow(EditPlantUiState(plantId = plantId))
    val uiState: StateFlow<EditPlantUiState> = _uiState.asStateFlow()

    val soilTypeOptions = listOf("Select", "Coco Coir", "Soil", "Hydroponics", "Aeroponics", "Other")

    init {
        if (plantId.isNotEmpty()) {
            loadPlantDetails(plantId)
        } else {
            // Handle error: plantId is missing
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadPlantDetails(id: String) {
        viewModelScope.launch {
            val plant = repository.getPlantByIdOnce(id)
            if (plant != null) {
                val sourceDisplay = when (plant.source) {
                    PlantSource.SEED -> "Seed"
                    PlantSource.CLONE -> "Clone"
                    else -> "Select"
                }
                val typeDisplay = when (plant.type) {
                    PlantType.AUTOFLOWER -> "Autoflower"
                    PlantType.PHOTOPERIOD -> "Photoperiod"
                    null -> "Select"
                }
                val growthStageDisplay = plant.growthStage.name.replace("_", " ").lowercase().capitalizeWords()
                val availableStages = determineAvailableGrowthStages(plant.source)
                val (durationLabel, showDuration) = determineDurationFieldVisibilityAndLabel(plant.source, plant.type)
                val durationTextValue = when (plant.type) {
                    PlantType.AUTOFLOWER -> plant.seedToHarvestDays?.toString() ?: ""
                    PlantType.PHOTOPERIOD -> plant.flowerDurationDays?.toString() ?: ""
                    else -> ""
                }

                _uiState.update {
                    it.copy(
                        originalPlant = plant,
                        strainName = plant.strainName,
                        batchNumber = plant.batchNumber,
                        source = plant.source,
                        sourceDisplay = sourceDisplay,
                        type = plant.type,
                        typeDisplay = typeDisplay,
                        growthStage = plant.growthStage,
                        growthStageDisplay = growthStageDisplay,
                        availableGrowthStages = availableStages,
                        durationLabel = durationLabel,
                        showDurationField = showDuration,
                        durationText = durationTextValue,
                        startDate = plant.startDate,
                        startDateText = plant.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        nutrientsList = plant.nutrients,
                        soilTypeDisplay = plant.soilType ?: "Select",
                        selectedSoilType = plant.soilType,
                        imageUri = plant.imagePath,
                        isLoading = false,
                        isValid = validateForm(
                            strainName = plant.strainName,
                            batchNumber = plant.batchNumber,
                            source = plant.source,
                            stage = plant.growthStage
                        )
                    )
                }
            } else {
                // Handle error: plant not found
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun determineAvailableGrowthStages(source: PlantSource?): List<GrowthStage> {
        return when (source) {
            PlantSource.SEED -> listOf(GrowthStage.GERMINATION, GrowthStage.SEEDLING, GrowthStage.VEGETATION, GrowthStage.FLOWER, GrowthStage.DRYING, GrowthStage.CURING)
            PlantSource.CLONE -> listOf(GrowthStage.NON_ROOTED, GrowthStage.ROOTED, GrowthStage.VEGETATION, GrowthStage.FLOWER, GrowthStage.DRYING, GrowthStage.CURING)
            else -> emptyList()
        }
    }

    private fun determineDurationFieldVisibilityAndLabel(source: PlantSource?, type: PlantType?): Pair<String, Boolean> {
        if (source == PlantSource.CLONE) return Pair("Duration (days)", false)
        return when (type) {
            PlantType.AUTOFLOWER -> Pair("Seed to Harvest (days)", true)
            PlantType.PHOTOPERIOD -> Pair("Flower Duration (days)", true)
            else -> Pair("Duration (days)", false)
        }
    }

    fun updateStrainName(name: String) {
        _uiState.update {
            it.copy(
                strainName = name,
                isValid = validateForm(name, it.batchNumber, it.source, it.growthStage)
            )
        }
    }

    fun updateBatchNumber(number: String) {
        _uiState.update {
            it.copy(
                batchNumber = number,
                isValid = validateForm(it.strainName, number, it.source, it.growthStage)
            )
        }
    }

    fun updateSource(source: PlantSource) {
        val displayText = when (source) {
            PlantSource.SEED -> "Seed"
            PlantSource.CLONE -> "Clone"
        }
        val newAvailableGrowthStages = determineAvailableGrowthStages(source)
        val (durationLabel, showDuration) = determineDurationFieldVisibilityAndLabel(source, if (source == PlantSource.CLONE) null else _uiState.value.type)
        
        _uiState.update {
            it.copy(
                source = source,
                sourceDisplay = displayText,
                growthStage = null, // Reset growth stage when source changes
                growthStageDisplay = "Select",
                availableGrowthStages = newAvailableGrowthStages,
                type = if (source == PlantSource.CLONE) null else it.type,
                typeDisplay = if (source == PlantSource.CLONE) "Select" else it.typeDisplay,
                showDurationField = showDuration,
                durationLabel = durationLabel,
                durationText = if (!showDuration || source == PlantSource.CLONE) "" else it.durationText,
                isValid = validateForm(it.strainName, it.batchNumber, source, null)
            )
        }
    }

    fun updatePlantType(type: PlantType?) {
         val (durationLabel, showDuration) = determineDurationFieldVisibilityAndLabel(_uiState.value.source, type)
         val typeDisplayText = when (type) {
            PlantType.AUTOFLOWER -> "Autoflower"
            PlantType.PHOTOPERIOD -> "Photoperiod"
            null -> "Select"
        }

        _uiState.update {
            if (it.source == PlantSource.CLONE) { // Clones don't have a type
                 it.copy(
                    type = null,
                    typeDisplay = "Select",
                    showDurationField = false,
                    durationLabel = "Duration (days)",
                    durationText = "",
                    isValid = validateForm(it.strainName, it.batchNumber, it.source, it.growthStage)
                )
            } else {
                it.copy(
                    type = type,
                    typeDisplay = typeDisplayText,
                    durationLabel = durationLabel,
                    showDurationField = showDuration,
                    durationText = if (!showDuration) "" else it.durationText,
                    isValid = validateForm(it.strainName, it.batchNumber, it.source, it.growthStage)
                )
            }
        }
    }

    fun updateGrowthStage(stage: GrowthStage) {
        val displayText = stage.name.replace("_", " ").lowercase().capitalizeWords()
        _uiState.update {
            it.copy(
                growthStage = stage,
                growthStageDisplay = displayText,
                isValid = validateForm(it.strainName, it.batchNumber, it.source, stage)
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

    fun showStartDatePickerDialog() {
        _uiState.update { it.copy(showStartDatePicker = true) }
    }

    fun dismissStartDatePickerDialog() {
        _uiState.update { it.copy(showStartDatePicker = false) }
    }
    
    fun updateImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
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

    fun updatePlant() {
        val currentState = uiState.value
        if (!currentState.isValid || currentState.originalPlant == null) return

        val seedToHarvest = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.AUTOFLOWER) currentState.durationText.toIntOrNull() else currentState.originalPlant.seedToHarvestDays
        val flowerDuration = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.PHOTOPERIOD) currentState.durationText.toIntOrNull() else currentState.originalPlant.flowerDurationDays

        val updatedPlant = currentState.originalPlant.copy(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source!!, // Validation ensures not null
            type = if(currentState.source == PlantSource.SEED) currentState.type else null,
            growthStage = currentState.growthStage!!, // Validation ensures not null
            startDate = currentState.startDate,
            lastUpdated = LocalDate.now(),
            seedToHarvestDays = seedToHarvest,
            flowerDurationDays = flowerDuration,
            soilType = currentState.selectedSoilType,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri
        )

        viewModelScope.launch {
            repository.updatePlant(updatedPlant)
            // Optionally, trigger navigation or show a success message
        }
    }

    private fun validateForm(
        strainName: String,
        batchNumber: String,
        source: PlantSource?,
        stage: GrowthStage?
    ): Boolean {
        return strainName.isNotEmpty() &&
                batchNumber.isNotEmpty() &&
                source != null &&
                stage != null
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val savedStateHandle = createSavedStateHandle()
                EditPlantViewModel(application, savedStateHandle)
            }
        }
    }
} 