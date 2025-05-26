package com.example.mygreenhouse.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "plant_stage_transitions",
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE // If a plant is deleted, its history is also deleted
        )
    ],
    indices = [Index(value = ["plantId"])]
)
data class PlantStageTransition(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,
    val stage: GrowthStage,
    val transitionDate: LocalDate
) 