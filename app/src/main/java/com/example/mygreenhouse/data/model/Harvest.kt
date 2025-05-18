package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

/**
 * Entity class representing a plant harvest record
 */
@Entity(
    tableName = "harvests",
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["plantId"])]
)
data class Harvest(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Which plant was harvested
    val plantId: String?,
    val strainName: String,
    val batchNumber: String,
    
    // Harvest details
    val harvestDate: LocalDate,
    val dryingCompleteDate: LocalDate? = null,
    val curingCompleteDate: LocalDate? = null,
    
    // Weight information
    val wetWeight: Double? = null, // in grams
    val dryWeight: Double? = null, // in grams
    val finalCuredWeight: Double? = null, // in grams
    
    // Notes and quality
    val notes: String = "",
    val qualityRating: Int? = null, // e.g., 1-5 rating
    
    // Status tracking
    val isDrying: Boolean = false,
    val isCuring: Boolean = false,
    val isCompleted: Boolean = false
) 