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
    GERMINATION, SEEDLING, VEGETATION, FLOWER, DRYING, CURING,
    // Clone growth stages
    NON_ROOTED, ROOTED
    // Note: Clones also go through VEGETATION, FLOWER, DRYING, CURING stages
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
    
    // Plant characteristics
    val source: PlantSource,
    val type: PlantType?,  // null for clones
    val growthStage: GrowthStage,
    
    // Dates
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    
    // Specific growth parameters based on plant type
    val seedToHarvestDays: Int? = null, // For autoflowers
    val flowerDurationDays: Int? = null, // For photoperiods
    
    // Other properties
    val soilType: String? = null,
    val nutrients: List<String> = emptyList(), // Reverted to original name
    
    // Image path (URI as String)
    val imagePath: String? = null,
    
    // Archive flag
    val isArchived: Boolean = false
) 