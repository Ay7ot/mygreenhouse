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
 * UI state for the Add Plant screen
 */
data class AddPlantUiState(
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
    val growMediumDisplay: String = "Select",
    val selectedGrowMedium: String? = null,
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
    
    val growMediumOptions = listOf(
        "Select", 
        "Coco Coir", 
        "Soil", 
        "Hydroponic: Other",
        "Aeroponics", 
        "Deep Water Culture", 
        "Nutrient Film Technique", 
        "Wick System", 
        "Ebb and Flow", 
        "Drip System",
        "Other"
    )
    
    val plantTypeSelectionOptions = listOf("Select", "Autoflower Regular", "Autoflower Feminized", "Photoperiod Regular", "Photoperiod Feminized")
    
    val plantGenderOptions = PlantGender.values().map { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
    
    private fun calculateDaysUntilHarvest(startDate: LocalDate, durationText: String, plantType: PlantType?, plantSource: PlantSource?): Long? {
        val seedToHarvestDays = durationText.toIntOrNull()
        return if (plantType == PlantType.AUTOFLOWER && seedToHarvestDays != null && plantSource == PlantSource.SEED) {
            val harvestDate = startDate.plusDays(seedToHarvestDays.toLong())
            ChronoUnit.DAYS.between(LocalDate.now(), harvestDate).coerceAtLeast(0) // Display 0 if past due for new plants
        } else null
    }
    
    fun updateStrainName(name: String) {
        _uiState.update { 
            it.copy(
                strainName = name,
                isValid = validateForm(
                    strainName = name,
                    batchNumber = it.batchNumber,
                    source = it.source,
                    type = it.type,
                    stage = it.growthStage,
                    quantity = it.quantity
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
                    stage = it.growthStage,
                    quantity = it.quantity
                )
            )
        }
    }
    
    fun updateQuantity(quantityStr: String) {
        val quantity = quantityStr.toIntOrNull() ?: 1
        _uiState.update { 
            it.copy(
                quantity = quantityStr,
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = it.batchNumber,
                    source = it.source,
                    type = it.type,
                    stage = it.growthStage,
                    quantity = quantityStr
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
                daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, if (source == PlantSource.CLONE) "" else it.durationText, null, source),
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = it.batchNumber,
                    source = source,
                    type = null,
                    stage = null,
                    quantity = it.quantity
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
                    daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, "", null, it.source),
                    isValid = validateForm(
                        strainName = it.strainName,
                        batchNumber = it.batchNumber,
                        source = it.source,
                        type = null,
                        stage = it.growthStage,
                        quantity = it.quantity
                    )
                )
            } else {
                it.copy(
                    plantTypeSelection = selectedDisplayString,
                    type = newPlantType,
                    durationLabel = durationLabelText,
                    showDurationField = showField,
                    durationText = if (!showField) "" else it.durationText,
                    daysUntilHarvest = calculateDaysUntilHarvest(it.startDate, if (!showField) "" else it.durationText, newPlantType, it.source),
                    isValid = validateForm(
                        strainName = it.strainName,
                        batchNumber = it.batchNumber,
                        source = it.source,
                        type = newPlantType,
                        stage = it.growthStage,
                        quantity = it.quantity
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
                isValid = validateForm(
                    strainName = currentState.strainName,
                    batchNumber = currentState.batchNumber,
                    source = currentState.source,
                    type = currentState.type,
                    stage = stage,
                    quantity = currentState.quantity
                )
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
    
    fun updateGrowMedium(growMedium: String) {
        _uiState.update {
            it.copy(
                growMediumDisplay = growMedium,
                selectedGrowMedium = if (growMedium == "Select") null else growMedium
            )
        }
    }
    
    fun updateImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }
    
    fun updatePlantGender(genderString: String) {
        val gender = PlantGender.values().find { it.name.equals(genderString, ignoreCase = true) } ?: PlantGender.UNKNOWN
        _uiState.update {
            it.copy(
                plantGender = gender,
                plantGenderDisplay = genderString.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                isValid = validateForm(
                    strainName = it.strainName,
                    batchNumber = it.batchNumber,
                    source = it.source,
                    type = it.type,
                    stage = it.growthStage,
                    quantity = it.quantity
                )
            )
        }
    }
    
    fun savePlant() {
        val currentState = uiState.value
        
        if (!validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity)) return
        
        val seedToHarvestDays = if (currentState.type == PlantType.AUTOFLOWER && currentState.source == PlantSource.SEED) {
            currentState.durationText.toIntOrNull()
        } else null
        
        val flowerDurationDays = if (currentState.type == PlantType.PHOTOPERIOD && currentState.source == PlantSource.SEED) {
            currentState.durationText.toIntOrNull()
        } else null

        val finalDryingStartDate = if (currentState.growthStage == GrowthStage.DRYING) currentState.dryingStartDate ?: LocalDate.now() else null
        val finalCuringStartDate = if (currentState.growthStage == GrowthStage.CURING) currentState.curingStartDate ?: LocalDate.now() else null
        
        val plant = Plant(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source ?: PlantSource.SEED,
            type = currentState.type,
            growthStage = currentState.growthStage ?: GrowthStage.GERMINATION,
            startDate = currentState.startDate,
            dryingStartDate = finalDryingStartDate,
            curingStartDate = finalCuringStartDate,
            lastUpdated = LocalDate.now(),
            seedToHarvestDays = seedToHarvestDays,
            flowerDurationDays = flowerDurationDays,
            growMedium = currentState.selectedGrowMedium,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri,
            quantity = currentState.quantity.toIntOrNull() ?: 1,
            gender = currentState.plantGender
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
                AddPlantViewModel(application)
            }
        }
    }
} 