package com.example.mygreenhouse.data.model

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Type converters for Room to handle custom types
 */
class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    
    // List<String> converters (for nutrients)
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return data?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    // Enum converters
    @TypeConverter
    fun fromPlantSource(source: PlantSource?): String? {
        return source?.name
    }
    
    @TypeConverter
    fun toPlantSource(value: String?): PlantSource? {
        return value?.let { PlantSource.valueOf(it) }
    }
    
    @TypeConverter
    fun fromPlantType(type: PlantType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toPlantType(value: String?): PlantType? {
        return value?.let { PlantType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromGrowthStage(stage: GrowthStage?): String? {
        return stage?.name
    }
    
    @TypeConverter
    fun toGrowthStage(value: String?): GrowthStage? {
        return value?.let { GrowthStage.valueOf(it) }
    }
    
    @TypeConverter
    fun fromTaskType(type: TaskType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toTaskType(value: String?): TaskType? {
        return value?.let { TaskType.valueOf(it) }
    }
} 