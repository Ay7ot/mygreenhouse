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
import com.example.mygreenhouse.data.model.PlantStageTransition
import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.PlantStageTransitionRepository
import com.example.mygreenhouse.data.repository.StrainRepository
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
    
    // Stage-specific start dates
    val germinationStartDate: LocalDate? = null,
    val seedlingStartDate: LocalDate? = null,
    val nonRootedStartDate: LocalDate? = null,
    val rootedStartDate: LocalDate? = null,
    val vegetationStartDate: LocalDate? = null,
    val flowerStartDate: LocalDate? = null,
    
    // Completed stages (stages the plant has been through)
    val completedStages: List<GrowthStage> = emptyList(),
    val currentNutrientInput: String = "",
    val nutrientsList: List<String> = emptyList(),
    val growMediumDisplay: String = "Select",
    val selectedGrowMedium: String? = null,
    val imageUri: String? = null,
    val originalPlant: Plant? = null, // To compare for changes or use for update
    val isLoading: Boolean = true,
    val isValid: Boolean = false,
    val isCustomStrain: Boolean = false,
    val showHarvestConfirmationDialog: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showExitConfirmationDialog: Boolean = false
)

/**
 * ViewModel for the Edit Plant screen
 */
class EditPlantViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val plantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())
    private val stageTransitionRepository = PlantStageTransitionRepository(AppDatabase.getDatabase(application).plantStageTransitionDao())
    private val strainRepository = StrainRepository(AppDatabase.getDatabase(application).strainDao())
    private val plantId: String = savedStateHandle.get<String>("plantId") ?: ""

    private val _uiState = MutableStateFlow(EditPlantUiState(plantId = plantId))
    val uiState: StateFlow<EditPlantUiState> = _uiState.asStateFlow()

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
            val plant = plantRepository.getPlantByIdOnce(id)
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
                
                // Get transitions to determine completed stages
                val transitions = stageTransitionRepository.getTransitionsForPlantOnce(plant.id)
                val completedStages = transitions.map { it.stage }.distinct().filter { it != plant.growthStage }

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
                        germinationStartDate = plant.germinationStartDate,
                        seedlingStartDate = plant.seedlingStartDate,
                        nonRootedStartDate = plant.nonRootedStartDate,
                        rootedStartDate = plant.rootedStartDate,
                        vegetationStartDate = plant.vegetationStartDate,
                        flowerStartDate = plant.flowerStartDate,
                        completedStages = completedStages,
                        nutrientsList = plant.nutrients,
                        growMediumDisplay = plant.growMedium ?: "Select",
                        selectedGrowMedium = plant.growMedium,
                        imageUri = plant.imagePath,
                        isLoading = false,
                        isValid = validateForm(
                            strainName = plant.strainName,
                            batchNumber = plant.batchNumber,
                            source = plant.source,
                            type = plant.type,
                            stage = plant.growthStage,
                            quantity = plant.quantity.toString()
                        ),
                        isCustomStrain = plant.isCustomStrain
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
            PlantSource.SEED -> listOf(GrowthStage.GERMINATION, GrowthStage.SEEDLING, GrowthStage.VEGETATION, GrowthStage.FLOWER, GrowthStage.HARVEST_PLANT)
            PlantSource.CLONE -> listOf(GrowthStage.NON_ROOTED, GrowthStage.ROOTED, GrowthStage.VEGETATION, GrowthStage.FLOWER, GrowthStage.HARVEST_PLANT)
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
            it.copy(
                plantTypeSelection = selectedDisplayString,
                type = newPlantType,
                durationLabel = durationLabelText,
                showDurationField = if (it.source == PlantSource.CLONE) false else showField,
                durationText = if (it.source == PlantSource.CLONE || !showField) "" else it.durationText,
                daysUntilHarvest = if (it.source == PlantSource.CLONE) null else calculateDaysUntilHarvest(it.startDate, if(!showField) "" else it.durationText, newPlantType, it.source),
                isValid = validateForm(it.strainName, it.batchNumber, it.source, newPlantType, it.growthStage, it.quantity)
            )
        }
    }

    fun updateGrowthStage(stage: GrowthStage) {
        val displayText = stage.name.replace("_", " ").lowercase().capitalizeWords()
        
        // If user selects HARVEST_PLANT, show confirmation dialog
        if (stage == GrowthStage.HARVEST_PLANT) {
            _uiState.update { it.copy(showHarvestConfirmationDialog = true) }
            return
        }
        
        _uiState.update { currentState ->
            val newDryingStartDate = if (stage == GrowthStage.DRYING && currentState.growthStage != GrowthStage.DRYING) LocalDate.now() else if (stage != GrowthStage.DRYING) null else currentState.dryingStartDate
            val newCuringStartDate = if (stage == GrowthStage.CURING && currentState.growthStage != GrowthStage.CURING) LocalDate.now() else if (stage != GrowthStage.CURING) null else currentState.curingStartDate
            
            // Auto-set stage start dates when moving to a new stage
            val today = LocalDate.now()
            val newGerminationStartDate = if (stage == GrowthStage.GERMINATION && currentState.growthStage != GrowthStage.GERMINATION && currentState.germinationStartDate == null) today else currentState.germinationStartDate
            val newSeedlingStartDate = if (stage == GrowthStage.SEEDLING && currentState.growthStage != GrowthStage.SEEDLING && currentState.seedlingStartDate == null) today else currentState.seedlingStartDate
            val newNonRootedStartDate = if (stage == GrowthStage.NON_ROOTED && currentState.growthStage != GrowthStage.NON_ROOTED && currentState.nonRootedStartDate == null) today else currentState.nonRootedStartDate
            val newRootedStartDate = if (stage == GrowthStage.ROOTED && currentState.growthStage != GrowthStage.ROOTED && currentState.rootedStartDate == null) today else currentState.rootedStartDate
            val newVegetationStartDate = if (stage == GrowthStage.VEGETATION && currentState.growthStage != GrowthStage.VEGETATION && currentState.vegetationStartDate == null) today else currentState.vegetationStartDate
            val newFlowerStartDate = if (stage == GrowthStage.FLOWER && currentState.growthStage != GrowthStage.FLOWER && currentState.flowerStartDate == null) today else currentState.flowerStartDate
            
            val daysUntilHarvest = calculateDaysUntilHarvest(currentState.startDate, currentState.durationText, currentState.type, currentState.source)

            currentState.copy(
                growthStage = stage,
                growthStageDisplay = displayText,
                dryingStartDate = newDryingStartDate,
                curingStartDate = newCuringStartDate,
                daysInDrying = newDryingStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) },
                daysInCuring = newCuringStartDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) },
                daysUntilHarvest = daysUntilHarvest,
                germinationStartDate = newGerminationStartDate,
                seedlingStartDate = newSeedlingStartDate,
                nonRootedStartDate = newNonRootedStartDate,
                rootedStartDate = newRootedStartDate,
                vegetationStartDate = newVegetationStartDate,
                flowerStartDate = newFlowerStartDate,
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

    fun updateGrowMedium(growMedium: String) {
        _uiState.update {
            it.copy(
                growMediumDisplay = growMedium,
                selectedGrowMedium = if (growMedium == "Select") null else growMedium
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

    fun updateGerminationStartDate(date: LocalDate) {
        _uiState.update { it.copy(germinationStartDate = date) }
    }

    fun updateSeedlingStartDate(date: LocalDate) {
        _uiState.update { it.copy(seedlingStartDate = date) }
    }

    fun updateNonRootedStartDate(date: LocalDate) {
        _uiState.update { it.copy(nonRootedStartDate = date) }
    }

    fun updateRootedStartDate(date: LocalDate) {
        _uiState.update { it.copy(rootedStartDate = date) }
    }

    fun updateVegetationStartDate(date: LocalDate) {
        _uiState.update { it.copy(vegetationStartDate = date) }
    }

    fun updateFlowerStartDate(date: LocalDate) {
        _uiState.update { it.copy(flowerStartDate = date) }
    }

    fun updateIsCustomStrain(isCustom: Boolean) {
        _uiState.update { it.copy(isCustomStrain = isCustom) }
    }
    
    fun onHarvestConfirmationDismiss() {
        _uiState.update { it.copy(showHarvestConfirmationDialog = false) }
    }
    
    fun onHarvestConfirmationConfirm(): Triple<String, String, String>? {
        val currentState = uiState.value
        if (currentState.strainName.isNotEmpty() && currentState.batchNumber.isNotEmpty() && currentState.plantId.isNotEmpty()) {
            _uiState.update { it.copy(showHarvestConfirmationDialog = false) }
            return Triple(currentState.strainName, currentState.batchNumber, currentState.plantId)
        }
        return null
    }
    
    fun onHarvestConfirmationCancel() {
        _uiState.update { it.copy(showHarvestConfirmationDialog = false) }
    }

    fun updatePlant() {
        val currentState = uiState.value
        if (!validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity) || currentState.originalPlant == null) return

        val flowerDuration = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.PHOTOPERIOD) currentState.durationText.toIntOrNull() else currentState.originalPlant.flowerDurationDays

        val finalDryingStartDate = if (currentState.growthStage == GrowthStage.DRYING) currentState.dryingStartDate ?: LocalDate.now() else currentState.originalPlant.dryingStartDate
        val finalCuringStartDate = if (currentState.growthStage == GrowthStage.CURING) currentState.curingStartDate ?: LocalDate.now() else currentState.originalPlant.curingStartDate

        val seedToHarvest = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.AUTOFLOWER) currentState.durationText.toIntOrNull() else currentState.originalPlant.seedToHarvestDays

        val updatedPlant = currentState.originalPlant.copy(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source!!,
            type = currentState.type, // Allow clones to have plant types
            gender = currentState.plantGender,
            growthStage = currentState.growthStage!!,
            startDate = currentState.startDate,
            lastUpdated = LocalDate.now(),
            flowerDurationDays = flowerDuration,
            growMedium = currentState.selectedGrowMedium,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri,
            quantity = currentState.quantity.toIntOrNull() ?: 1,
            dryingStartDate = finalDryingStartDate,
            curingStartDate = finalCuringStartDate,
            seedToHarvestDays = seedToHarvest,
            isCustomStrain = currentState.isCustomStrain,
            germinationStartDate = currentState.germinationStartDate,
            seedlingStartDate = currentState.seedlingStartDate,
            nonRootedStartDate = currentState.nonRootedStartDate,
            rootedStartDate = currentState.rootedStartDate,
            vegetationStartDate = currentState.vegetationStartDate,
            flowerStartDate = currentState.flowerStartDate
        )

        viewModelScope.launch {
            plantRepository.updatePlant(updatedPlant)
            if (currentState.originalPlant.growthStage != currentState.growthStage) {
                val transitionDate = when(currentState.growthStage) {
                    GrowthStage.DRYING -> finalDryingStartDate ?: LocalDate.now()
                    GrowthStage.CURING -> finalCuringStartDate ?: LocalDate.now()
                    else -> LocalDate.now()
                }
                stageTransitionRepository.insertTransition(
                    PlantStageTransition(
                        plantId = updatedPlant.id,
                        stage = currentState.growthStage,
                        transitionDate = transitionDate
                    )
                )
            }
            
            // Archive the strain name for future use (only if strain name changed)
            if (currentState.originalPlant.strainName != currentState.strainName) {
                strainRepository.archiveStrainName(currentState.strainName, currentState.isCustomStrain)
            }
        }
    }

    // Exit confirmation methods
    fun checkForUnsavedChanges(): Boolean {
        val currentState = uiState.value
        val original = currentState.originalPlant ?: return false
        
        return currentState.strainName != original.strainName ||
               currentState.batchNumber != original.batchNumber ||
               currentState.quantity != original.quantity.toString() ||
               currentState.source != original.source ||
               currentState.plantGender != original.gender ||
               currentState.type != original.type ||
               currentState.growthStage != original.growthStage ||
               currentState.durationText != (original.seedToHarvestDays?.toString() ?: original.flowerDurationDays?.toString() ?: "") ||
               currentState.startDate != original.startDate ||
               currentState.nutrientsList != original.nutrients ||
               currentState.selectedGrowMedium != original.growMedium ||
               currentState.imageUri != original.imagePath ||
               currentState.isCustomStrain != original.isCustomStrain
    }
    
    fun showExitConfirmationDialog() {
        _uiState.update { it.copy(showExitConfirmationDialog = true) }
    }
    
    fun onExitConfirmationDismiss() {
        _uiState.update { it.copy(showExitConfirmationDialog = false) }
    }
    
    fun onExitConfirmationSave(onComplete: (Boolean) -> Unit) {
        if (validateForm(
            uiState.value.strainName,
            uiState.value.batchNumber,
            uiState.value.source,
            uiState.value.type,
            uiState.value.growthStage,
            uiState.value.quantity
        )) {
            viewModelScope.launch {
                try {
                    updatePlantSuspending()
                    _uiState.update { it.copy(showExitConfirmationDialog = false) }
                    onComplete(true)
                } catch (e: Exception) {
                    // Handle error if needed
                    onComplete(false)
                }
            }
        } else {
            onComplete(false)
        }
    }
    
    private suspend fun updatePlantSuspending() {
        val currentState = uiState.value
        if (!validateForm(currentState.strainName, currentState.batchNumber, currentState.source, currentState.type, currentState.growthStage, currentState.quantity) || currentState.originalPlant == null) return

        val flowerDuration = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.PHOTOPERIOD) currentState.durationText.toIntOrNull() else currentState.originalPlant.flowerDurationDays

        val finalDryingStartDate = if (currentState.growthStage == GrowthStage.DRYING) currentState.dryingStartDate ?: LocalDate.now() else currentState.originalPlant.dryingStartDate
        val finalCuringStartDate = if (currentState.growthStage == GrowthStage.CURING) currentState.curingStartDate ?: LocalDate.now() else currentState.originalPlant.curingStartDate

        val seedToHarvest = if (currentState.source == PlantSource.SEED && currentState.type == PlantType.AUTOFLOWER) currentState.durationText.toIntOrNull() else currentState.originalPlant.seedToHarvestDays

        val updatedPlant = currentState.originalPlant.copy(
            strainName = currentState.strainName,
            batchNumber = currentState.batchNumber,
            source = currentState.source!!,
            type = currentState.type, // Allow clones to have plant types
            gender = currentState.plantGender,
            growthStage = currentState.growthStage!!,
            startDate = currentState.startDate,
            lastUpdated = LocalDate.now(),
            flowerDurationDays = flowerDuration,
            growMedium = currentState.selectedGrowMedium,
            nutrients = currentState.nutrientsList,
            imagePath = currentState.imageUri,
            quantity = currentState.quantity.toIntOrNull() ?: 1,
            dryingStartDate = finalDryingStartDate,
            curingStartDate = finalCuringStartDate,
            seedToHarvestDays = seedToHarvest,
            isCustomStrain = currentState.isCustomStrain,
            germinationStartDate = currentState.germinationStartDate,
            seedlingStartDate = currentState.seedlingStartDate,
            nonRootedStartDate = currentState.nonRootedStartDate,
            rootedStartDate = currentState.rootedStartDate,
            vegetationStartDate = currentState.vegetationStartDate,
            flowerStartDate = currentState.flowerStartDate
        )

        plantRepository.updatePlant(updatedPlant)
        if (currentState.originalPlant.growthStage != currentState.growthStage) {
            val transitionDate = when(currentState.growthStage) {
                GrowthStage.DRYING -> finalDryingStartDate ?: LocalDate.now()
                GrowthStage.CURING -> finalCuringStartDate ?: LocalDate.now()
                else -> LocalDate.now()
            }
            stageTransitionRepository.insertTransition(
                PlantStageTransition(
                    plantId = updatedPlant.id,
                    stage = currentState.growthStage,
                    transitionDate = transitionDate
                )
            )
        }
        

    }
    
    fun onExitConfirmationDontSave() {
        _uiState.update { it.copy(showExitConfirmationDialog = false) }
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