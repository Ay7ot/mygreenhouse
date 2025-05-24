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
import com.example.mygreenhouse.data.model.PlantGender
import com.example.mygreenhouse.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * UI state for the Edit Plant screen
 */
data class EditPlantUiState(
    val plantId: String = "",
    val strainName: String = "",
    val batchNumber: String = "",
    val quantity: String = "1",
    val source: PlantSource? = null,
    val sourceDisplay: String = "Select",
    val plantGender: PlantGender = PlantGender.UNKNOWN,
    val plantGenderDisplay: String = "Unknown",
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
    val dryingStartDate: LocalDate? = null,
    val curingStartDate: LocalDate? = null,
    val daysInDrying: Long? = null,
    val daysInCuring: Long? = null,
    val daysUntilHarvest: Long? = null,
    val currentNutrientInput: String = "",
    val nutrientsList: List<String> = emptyList(),
    val soilTypeDisplay: String = "Select",
    val selectedSoilType: String? = null,
    val imageUri: String? = null,
    val originalPlant: Plant? = null, // To compare for changes or use for update
    val isLoading: Boolean = true,
    val isValid: Boolean = false,
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
    val plantTypeSelectionOptions = listOf("Select", "Autoflower Regular", "Autoflower Feminized", "Photoperiod Regular", "Photoperiod Feminized")
    val plantGenderOptions = PlantGender.values().map { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

    private fun calculateDaysUntilHarvest(startDate: LocalDate, durationText: String, plantType: PlantType?, plantSource: PlantSource?): Long? {
        val seedToHarvestDays = durationText.toIntOrNull()
        return if (plantType == PlantType.AUTOFLOWER && seedToHarvestDays != null && plantSource == PlantSource.SEED) {
            val harvestDate = startDate.plusDays(seedToHarvestDays.toLong())
            ChronoUnit.DAYS.between(LocalDate.now(), harvestDate).coerceAtLeast(0)
        } else null
    }

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
                }
                val initialPlantTypeSelection = when (plant.type) {
                    PlantType.AUTOFLOWER -> "Autoflower Regular"
                    PlantType.PHOTOPERIOD -> "Photoperiod Regular"
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
                val plantGenderDisplay = plant.gender.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val daysInDrying = plant.dryingStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) }
                val daysInCuring = plant.curingStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) }
                val daysUntilHarvest = calculateDaysUntilHarvest(plant.startDate, durationTextValue, plant.type, plant.source)

                _uiState.update {
                    it.copy(
                        originalPlant = plant,
                        strainName = plant.strainName,
                        batchNumber = plant.batchNumber,
                        quantity = plant.quantity.toString(),
                        source = plant.source,
                        sourceDisplay = sourceDisplay,
                        plantGender = plant.gender,
                        plantGenderDisplay = plantGenderDisplay,
                        type = plant.type,
                        plantTypeSelection = initialPlantTypeSelection,
                        growthStage = plant.growthStage,
                        growthStageDisplay = growthStageDisplay,
                        availableGrowthStages = availableStages,
                        durationLabel = durationLabel,
                        showDurationField = showDuration,
                        durationText = durationTextValue,
                        startDate = plant.startDate,
                        startDateText = plant.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        dryingStartDate = plant.dryingStartDate,
                        curingStartDate = plant.curingStartDate,
                        daysInDrying = daysInDrying,
                        daysInCuring = daysInCuring,
                        daysUntilHarvest = daysUntilHarvest,
                        nutrientsList = plant.nutrients,
                        soilTypeDisplay = plant.soilType ?: "Select",
                        selectedSoilType = plant.soilType,
                        imageUri = plant.imagePath,
                        isLoading = false,
                        isValid = validateForm(
                            strainName = plant.strainName,
                            batchNumber = plant.batchNumber,
                            source = plant.source,
                            type = plant.type,
                            stage = plant.growthStage,
                            quantity = plant.quantity.toString()
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
                isValid = validateForm(name, it.batchNumber, it.source, it.type, it.growthStage, it.quantity)
            )
        }
    }

    fun updateBatchNumber(number: String) {
        _uiState.update {
            it.copy(
                batchNumber = number,
                isValid = validateForm(it.strainName, number, it.source, it.type, it.growthStage, it.quantity)
            )
        }
    }

    fun updateQuantity(quantityStr: String) {
        _uiState.update { 
            it.copy(
                quantity = quantityStr,
                isValid = validateForm(it.strainName, it.batchNumber, it.source, it.type, it.growthStage, quantityStr)
            )
        }
    }

    fun updateSource(source: PlantSource) {
        val displayText = when (source) {
            PlantSource.SEED -> "Seed"
            PlantSource.CLONE -> "Clone"
        }
        val newAvailableGrowthStages = determineAvailableGrowthStages(source)
        val (durationLabel, showDuration) = determineDurationFieldVisibilityAndLabel(source, null)
        
        _uiState.update {
            it.copy(
                source = source,
                sourceDisplay = displayText,
                growthStage = null,
                growthStageDisplay = "Select",
                availableGrowthStages = newAvailableGrowthStages,
                plantTypeSelection = "Select",
                type = null,
                showDurationField = showDuration,
                durationLabel = durationLabel,
                durationText = if (!showDuration || source == PlantSource.CLONE) "" else it.durationText,
                daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, if (!showDuration || source == PlantSource.CLONE) "" else it.durationText, null, source),
                isValid = validateForm(it.strainName, it.batchNumber, source, null, null, it.quantity)
            )
        }
    }

    fun updatePlantTypeSelection(selectedDisplayString: String) {
        val newPlantType: PlantType? = when {
            selectedDisplayString.startsWith("Autoflower") -> PlantType.AUTOFLOWER
            selectedDisplayString.startsWith("Photoperiod") -> PlantType.PHOTOPERIOD
            else -> null
        }

        val showField = determineDurationFieldVisibilityAndLabel(_uiState.value.source, newPlantType).second
        val durationLabelText = determineDurationFieldVisibilityAndLabel(_uiState.value.source, newPlantType).first
        
        _uiState.update {
            if (it.source == PlantSource.CLONE) {
                 it.copy(
                    plantTypeSelection = "Select",
                    type = null,
                    showDurationField = false,
                    durationLabel = "Duration (days)",
                    durationText = "",
                    daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, "", null, it.source),
                    isValid = validateForm(it.strainName, it.batchNumber, it.source, null, it.growthStage, it.quantity)
                )
            } else {
                it.copy(
                    plantTypeSelection = selectedDisplayString,
                    type = newPlantType,
                    durationLabel = durationLabelText,
                    showDurationField = showField,
                    durationText = if (!showField) "" else it.durationText,
                    daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, if(!showField) "" else it.durationText, newPlantType, it.source),
                    isValid = validateForm(it.strainName, it.batchNumber, it.source, newPlantType, it.growthStage, it.quantity)
                )
            }
        }
    }

    fun updateGrowthStage(stage: GrowthStage) {
        val displayText = stage.name.replace("_", " ").lowercase().capitalizeWords()
        _uiState.update { currentState ->
            val newDryingStartDate = if (stage == GrowthStage.DRYING && currentState.growthStage != GrowthStage.DRYING) LocalDate.now() else if (stage != GrowthStage.DRYING) null else currentState.dryingStartDate
            val newCuringStartDate = if (stage == GrowthStage.CURING && currentState.growthStage != GrowthStage.CURING) LocalDate.now() else if (stage != GrowthStage.CURING) null else currentState.curingStartDate
            
            val seedToHarvestDays = currentState.durationText.toIntOrNull()
            val daysUntilHarvest = calculateDaysUntilHarvest(currentState.startDate, currentState.durationText, currentState.type, currentState.source)

            currentState.copy(
                growthStage = stage,
                growthStageDisplay = displayText,
                dryingStartDate = newDryingStartDate,
                curingStartDate = newCuringStartDate,
                daysInDrying = newDryingStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) },
                daysInCuring = newCuringStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) },
                daysUntilHarvest = daysUntilHarvest,
                isValid = validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, stage, currentState.quantity)
            )
        }
    }

    fun updatePlantGender(genderString: String) {
        val gender = PlantGender.values().find { it.name.equals(genderString, ignoreCase = true) } ?: PlantGender.UNKNOWN
        _uiState.update {
            it.copy(
                plantGender = gender,
                plantGenderDisplay = genderString.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                isValid = validateForm(it.strainName, it.batchNumber, it.source, it.type, it.growthStage, it.quantity)
            )
        }
    }

    fun updateDurationText(duration: String) {
        _uiState.update { 
            it.copy(
                durationText = duration,
                daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, duration, it.type, it.source)
            ) 
        }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { 
            it.copy(
                startDate = date,
                startDateText = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                daysUntilHarvest = calculateDaysUntilHarvest(date, it.durationText, it.type, it.source)
            ) 
        }
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

    fun updateDryingStartDate(date: LocalDate) {
        _uiState.update { currentState ->
            currentState.copy(
                dryingStartDate = date,
                daysInDrying = ChronoUnit.DAYS.between(date, LocalDate.now()),
                isValid = validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity)
            )
        }
    }

    fun updateCuringStartDate(date: LocalDate) {
        _uiState.update { currentState ->
            currentState.copy(
                curingStartDate = date,
                daysInCuring = ChronoUnit.DAYS.between(date, LocalDate.now()),
                isValid = validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity)
            )
        }
    }

    fun updatePlant() {
        val currentState = uiState.value
        if (!validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity) || currentState.originalPlant == null) return

        val flowerDuration = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.PHOTOPERIOD) currentState.durationText.toIntOrNull() else currentState.originalPlant.flowerDurationDays

        val finalDryingStartDate = if (currentState.growthStage == GrowthStage.DRYING) currentState.dryingStartDate ?: LocalDate.now() else currentState.originalPlant.dryingStartDate // Preserve if not changed, or set if new
        val finalCuringStartDate = if (currentState.growthStage == GrowthStage.CURING) currentState.curingStartDate ?: LocalDate.now() else currentState.originalPlant.curingStartDate // Preserve if not changed, or set if new

        val seedToHarvest = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.AUTOFLOWER) currentState.durationText.toIntOrNull() else currentState.originalPlant.seedToHarvestDays // Ensure this line is present

        val updatedPlant = currentState.originalPlant.copy(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source!!,
            type = if(currentState.source == PlantSource.SEED) currentState.type else null,
            gender = currentState.plantGender,
            growthStage = currentState.growthStage!!,
            startDate = currentState.startDate,
            lastUpdated = LocalDate.now(),
            flowerDurationDays = flowerDuration,
            soilType = currentState.selectedSoilType,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri,
            quantity = currentState.quantity.toIntOrNull() ?: 1,
            dryingStartDate = finalDryingStartDate,
            curingStartDate = finalCuringStartDate,
            seedToHarvestDays = seedToHarvest, // Ensure this is assigned
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
        type: PlantType?,
        stage: GrowthStage?,
        quantity: String
    ): Boolean {
        val typeValid = if (source == PlantSource.SEED) type != null else true
        val quantityValid = quantity.toIntOrNull()?.let { it > 0 } ?: false
        
        return strainName.isNotEmpty() &&
                batchNumber.isNotEmpty() &&
                source != null &&
                typeValid &&
                stage != null &&
                quantityValid
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