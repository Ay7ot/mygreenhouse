package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

/**
 * Entity class representing seed inventory in the seed bank
 */
@Entity(tableName = "seeds")
data class Seed(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Basic information
    val strainName: String,
    val batchNumber: String,
    val seedCount: Int,
    
    // Strain information
    val breeder: String = "",
    val seedType: SeedType = SeedType.REGULAR,
    
    // Acquisition information
    val acquisitionDate: LocalDate = LocalDate.now(),
    val source: String = "",
    
    // Notes
    val notes: String = ""
)

/**
 * Enum representing types of seeds
 */
enum class SeedType {
    REGULAR, FEMINIZED, AUTOFLOWER
} 