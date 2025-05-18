package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

/**
 * Enum representing different types of plant-related tasks
 */
enum class TaskType {
    WATERING,
    FEEDING,
    PEST_CONTROL,
    SOIL_TEST,
    WATER_TEST,
    CO2_SUPPLEMENTATION,
    LIGHT_CYCLE_CHANGE,
    OTHER
}

/**
 * Entity class representing a plant-related task
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["plantId"])]
)
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Task details
    val type: TaskType,
    val description: String,
    val scheduledDateTime: LocalDateTime,
    
    // Status
    val isCompleted: Boolean = false,
    val completedDateTime: LocalDateTime? = null,
    
    // Related plant (optional - some tasks might be general)
    val plantId: String? = null
) 