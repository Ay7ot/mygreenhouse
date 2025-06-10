package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

/**
 * Entity class representing a strain name in the strain archive
 */
@Entity(tableName = "strains")
data class Strain(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Strain information
    val name: String,
    val isCustomStrain: Boolean = false,
    
    // Usage tracking
    val firstUsedDate: LocalDate = LocalDate.now(),
    val lastUsedDate: LocalDate = LocalDate.now(),
    val usageCount: Int = 1,
    
    // Notes for strain details
    val notes: String = ""
) 