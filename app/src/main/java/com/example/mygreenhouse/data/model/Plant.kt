package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

/**
 * Enum representing the source of a plant (Seed or Clone)
 */
enum class PlantSource {
    SEED, CLONE
}

/**
 * Enum representing the type of a plant (Autoflower or Photoperiod)
 */
enum class PlantType {
    AUTOFLOWER, PHOTOPERIOD
}

/**
 * Enum representing the growth stage of a plant
 */
enum class GrowthStage {
    // Seed growth stages
    GERMINATION, SEEDLING, VEGETATION, FLOWER, HARVEST_PLANT, DRYING, CURING,
    // Clone growth stages
    NON_ROOTED, ROOTED
    // Note: Clones also go through VEGETATION, FLOWER, HARVEST_PLANT, DRYING, CURING stages
}

/**
 * Enum representing the gender of a plant
 */
enum class PlantGender {
    MALE, FEMALE, UNKNOWN
}

/**
 * Entity class representing a plant in the greenhouse
 */
@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Basic information
    val strainName: String,
    val batchNumber: String,
    val quantity: Int = 1,
    
    // Plant characteristics
    val source: PlantSource,
    val type: PlantType?,  // null for clones
    val gender: PlantGender = PlantGender.UNKNOWN,
    val growthStage: GrowthStage,
    val isCustomStrain: Boolean = false,
    
    // Dates
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    val dryingStartDate: LocalDate? = null,
    val curingStartDate: LocalDate? = null,
    
    // Stage-specific start dates for more accurate tracking
    val germinationStartDate: LocalDate? = null,
    val seedlingStartDate: LocalDate? = null,
    val nonRootedStartDate: LocalDate? = null,
    val rootedStartDate: LocalDate? = null,
    val vegetationStartDate: LocalDate? = null,
    val flowerStartDate: LocalDate? = null,
    
    // Specific growth parameters based on plant type
    val seedToHarvestDays: Int? = null, // For autoflowers
    val flowerDurationDays: Int? = null, // For photoperiods
    
    // Other properties
    val growMedium: String? = null,
    val nutrients: List<String> = emptyList(), // Reverted to original name
    
    // Image path (URI as String)
    val imagePath: String? = null,
    
    // Archive flag
    val isArchived: Boolean = false
) 