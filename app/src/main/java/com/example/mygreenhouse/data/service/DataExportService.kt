package com.example.mygreenhouse.data.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.data.model.Seed
import com.example.mygreenhouse.data.model.PlantSource
import com.example.mygreenhouse.data.model.PlantType
import com.example.mygreenhouse.data.model.GrowthStage
import com.example.mygreenhouse.data.model.TaskType as ActualTaskType
import com.example.mygreenhouse.data.model.SeedType as ActualSeedType

import com.example.mygreenhouse.data.repository.PlantRepository
import com.example.mygreenhouse.data.repository.TaskRepository
import com.example.mygreenhouse.data.repository.HarvestRepository
import com.example.mygreenhouse.data.repository.SeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for exporting and importing app data
 */
class DataExportService(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val plantRepository = PlantRepository(database.plantDao())
    private val taskRepository = TaskRepository(database.taskDao())
    private val harvestRepository = HarvestRepository(database.harvestDao())
    private val seedRepository = SeedRepository(database.seedDao())
    
    private val TAG = "DataExportService"
    private val json = Json { 
        prettyPrint = true 
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }
    
    /**
     * Export all app data to a JSON file in Downloads directory
     * @return The URI of the exported file or null if there was an error
     */
    suspend fun exportAllData(): Uri? = withContext(Dispatchers.IO) {
        try {
            // Get all data
            val plants = plantRepository.getAllPlantsOneShot()
            val tasks = taskRepository.getAllTasksOneShot()
            val harvests = harvestRepository.getAllHarvestsOneShot()
            val seeds = seedRepository.getAllSeedsOneShot()
            
            // Build json objects
            val exportData = buildJsonObject {
                put("exportDate", JsonPrimitive(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                put("exportVersion", JsonPrimitive("1.0"))
                
                // Export plants
                put("plants", buildJsonArray {
                    plants.forEach { plant ->
                        add(serializePlant(plant))
                    }
                })
                
                // Export tasks
                put("tasks", buildJsonArray {
                    tasks.forEach { task ->
                        add(serializeTask(task))
                    }
                })
                
                // Export harvests
                put("harvests", buildJsonArray {
                    harvests.forEach { harvest ->
                        add(serializeHarvest(harvest))
                    }
                })
                
                // Export seeds
                put("seeds", buildJsonArray {
                    seeds.forEach { seed ->
                        add(serializeSeed(seed))
                    }
                })
            }
            
            // Create a file in the Downloads directory
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "greenhouse_export_$timestamp.json"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val exportFile = File(downloadsDir, fileName)
            
            // Write the data to the file
            FileOutputStream(exportFile).use { fileOutputStream ->
                OutputStreamWriter(fileOutputStream).use { writer ->
                    writer.write(json.encodeToString(exportData))
                }
            }
            
            Log.d(TAG, "Data exported to ${exportFile.absolutePath}")
            return@withContext Uri.fromFile(exportFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data", e)
            return@withContext null
        }
    }
    
    /**
     * Import data from a JSON file
     * @param uri The URI of the file to import
     * @return True if import was successful, false otherwise
     */
    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val importText = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            val importData = json.parseToJsonElement(importText).jsonObject
            
            // Clear existing data
            val clearedSuccessfully = clearAllData()
            if (!clearedSuccessfully) {
                Log.e(TAG, "Failed to clear data before import.")
                return@withContext false
            }
            
            // Import plants
            importData["plants"]?.jsonArray?.forEach { plantJsonEl ->
                if (plantJsonEl is JsonObject) {
                    val plant = deserializePlant(plantJsonEl)
                    plantRepository.insertPlant(plant)
                }
            }
            
            // Import tasks
            importData["tasks"]?.jsonArray?.forEach { taskJsonEl ->
                if (taskJsonEl is JsonObject) {
                    val task = deserializeTask(taskJsonEl)
                    taskRepository.insertTask(task)
                }
            }
            
            // Import harvests
            importData["harvests"]?.jsonArray?.forEach { harvestJsonEl ->
                if (harvestJsonEl is JsonObject) {
                    val harvest = deserializeHarvest(harvestJsonEl)
                    harvestRepository.insertHarvest(harvest)
                }
            }
            
            // Import seeds
            importData["seeds"]?.jsonArray?.forEach { seedJsonEl ->
                if (seedJsonEl is JsonObject) {
                    val seed = deserializeSeed(seedJsonEl)
                    seedRepository.insertSeed(seed)
                }
            }
            
            Log.d(TAG, "Data imported successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data", e)
            return@withContext false
        }
    }
    
    /**
     * Clear all data from the database
     */
    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        try {
            database.clearAllTables()
            Log.d(TAG, "All data cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data", e)
            false
        }
    }
    
    // Serialization helper methods
    private fun serializePlant(plant: Plant): JsonObject {
        return buildJsonObject {
            put("id", JsonPrimitive(plant.id))
            put("strainName", JsonPrimitive(plant.strainName))
            put("batchNumber", JsonPrimitive(plant.batchNumber))
            put("source", JsonPrimitive(plant.source.name))
            plant.type?.let { put("type", JsonPrimitive(it.name)) }
            put("growthStage", JsonPrimitive(plant.growthStage.name))
            put("startDate", JsonPrimitive(plant.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            put("lastUpdated", JsonPrimitive(plant.lastUpdated.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            plant.seedToHarvestDays?.let { put("seedToHarvestDays", JsonPrimitive(it)) }
            plant.flowerDurationDays?.let { put("flowerDurationDays", JsonPrimitive(it)) }
            plant.growMedium?.let { put("growMedium", JsonPrimitive(it)) }
            put("nutrients", buildJsonArray { plant.nutrients.forEach { add(JsonPrimitive(it)) } })
            plant.imagePath?.let { put("imagePath", JsonPrimitive(it)) }
            put("isArchived", JsonPrimitive(plant.isArchived))
        }
    }
    
    private fun serializeTask(task: Task): JsonObject {
        return buildJsonObject {
            put("id", JsonPrimitive(task.id))
            put("type", JsonPrimitive(task.type.name))
            put("description", JsonPrimitive(task.description))
            put("scheduledDateTime", JsonPrimitive(task.scheduledDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            put("isCompleted", JsonPrimitive(task.isCompleted))
            task.completedDateTime?.let { put("completedDateTime", JsonPrimitive(it.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))) }
            task.plantId?.let { put("plantId", JsonPrimitive(it)) }
        }
    }
    
    private fun serializeHarvest(harvest: Harvest): JsonObject {
        return buildJsonObject {
            put("id", JsonPrimitive(harvest.id))
            harvest.plantId?.let { put("plantId", JsonPrimitive(it)) }
            put("strainName", JsonPrimitive(harvest.strainName))
            put("batchNumber", JsonPrimitive(harvest.batchNumber))
            put("harvestDate", JsonPrimitive(harvest.harvestDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            harvest.dryingCompleteDate?.let { put("dryingCompleteDate", JsonPrimitive(it.format(DateTimeFormatter.ISO_LOCAL_DATE))) }
            harvest.curingCompleteDate?.let { put("curingCompleteDate", JsonPrimitive(it.format(DateTimeFormatter.ISO_LOCAL_DATE))) }
            harvest.wetWeight?.let { put("wetWeight", JsonPrimitive(it)) }
            harvest.dryWeight?.let { put("dryWeight", JsonPrimitive(it)) }
            harvest.finalCuredWeight?.let { put("finalCuredWeight", JsonPrimitive(it)) }
            put("notes", JsonPrimitive(harvest.notes))
            harvest.qualityRating?.let { put("qualityRating", JsonPrimitive(it)) }
            put("isDrying", JsonPrimitive(harvest.isDrying))
            put("isCuring", JsonPrimitive(harvest.isCuring))
            put("isCompleted", JsonPrimitive(harvest.isCompleted))
        }
    }
    
    private fun serializeSeed(seed: Seed): JsonObject {
        return buildJsonObject {
            put("id", JsonPrimitive(seed.id))
            put("strainName", JsonPrimitive(seed.strainName))
            put("batchNumber", JsonPrimitive(seed.batchNumber))
            put("seedCount", JsonPrimitive(seed.seedCount))
            put("breeder", JsonPrimitive(seed.breeder))
            put("seedType", JsonPrimitive(seed.seedType.name))
            put("acquisitionDate", JsonPrimitive(seed.acquisitionDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            put("source", JsonPrimitive(seed.source))
            put("notes", JsonPrimitive(seed.notes))
        }
    }
    
    // Deserialization helper methods
    private fun deserializePlant(jsonObj: JsonObject): Plant {
        return Plant(
            id = jsonObj["id"]!!.jsonPrimitive.content,
            strainName = jsonObj["strainName"]!!.jsonPrimitive.content,
            batchNumber = jsonObj["batchNumber"]!!.jsonPrimitive.content,
            source = PlantSource.valueOf(jsonObj["source"]!!.jsonPrimitive.content),
            type = jsonObj["type"]?.jsonPrimitive?.content?.let { PlantType.valueOf(it) },
            growthStage = GrowthStage.valueOf(jsonObj["growthStage"]!!.jsonPrimitive.content),
            startDate = LocalDate.parse(jsonObj["startDate"]!!.jsonPrimitive.content, DateTimeFormatter.ISO_LOCAL_DATE),
            lastUpdated = LocalDate.parse(jsonObj["lastUpdated"]!!.jsonPrimitive.content, DateTimeFormatter.ISO_LOCAL_DATE),
            seedToHarvestDays = jsonObj["seedToHarvestDays"]?.jsonPrimitive?.content?.toIntOrNull(),
            flowerDurationDays = jsonObj["flowerDurationDays"]?.jsonPrimitive?.content?.toIntOrNull(),
            growMedium = jsonObj["growMedium"]?.jsonPrimitive?.content,
            nutrients = jsonObj["nutrients"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            imagePath = jsonObj["imagePath"]?.jsonPrimitive?.content,
            isArchived = jsonObj["isArchived"]!!.jsonPrimitive.content.toBoolean()
        )
    }
    
    private fun deserializeTask(jsonObj: JsonObject): Task {
        return Task(
            id = jsonObj["id"]!!.jsonPrimitive.content,
            type = ActualTaskType.valueOf(jsonObj["type"]!!.jsonPrimitive.content),
            description = jsonObj["description"]!!.jsonPrimitive.content,
            scheduledDateTime = LocalDateTime.parse(jsonObj["scheduledDateTime"]!!.jsonPrimitive.content, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isCompleted = jsonObj["isCompleted"]!!.jsonPrimitive.content.toBoolean(),
            completedDateTime = jsonObj["completedDateTime"]?.jsonPrimitive?.content?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) },
            plantId = jsonObj["plantId"]?.jsonPrimitive?.content
        )
    }
    
    private fun deserializeHarvest(jsonObj: JsonObject): Harvest {
        return Harvest(
            id = jsonObj["id"]!!.jsonPrimitive.content,
            plantId = jsonObj["plantId"]?.jsonPrimitive?.content,
            strainName = jsonObj["strainName"]!!.jsonPrimitive.content,
            batchNumber = jsonObj["batchNumber"]!!.jsonPrimitive.content,
            harvestDate = LocalDate.parse(jsonObj["harvestDate"]!!.jsonPrimitive.content, DateTimeFormatter.ISO_LOCAL_DATE),
            dryingCompleteDate = jsonObj["dryingCompleteDate"]?.jsonPrimitive?.content?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
            curingCompleteDate = jsonObj["curingCompleteDate"]?.jsonPrimitive?.content?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
            wetWeight = jsonObj["wetWeight"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            dryWeight = jsonObj["dryWeight"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            finalCuredWeight = jsonObj["finalCuredWeight"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            notes = jsonObj["notes"]!!.jsonPrimitive.content,
            qualityRating = jsonObj["qualityRating"]?.jsonPrimitive?.content?.toIntOrNull(),
            isDrying = jsonObj["isDrying"]!!.jsonPrimitive.content.toBoolean(),
            isCuring = jsonObj["isCuring"]!!.jsonPrimitive.content.toBoolean(),
            isCompleted = jsonObj["isCompleted"]!!.jsonPrimitive.content.toBoolean()
        )
    }
    
    private fun deserializeSeed(jsonObj: JsonObject): Seed {
        return Seed(
            id = jsonObj["id"]!!.jsonPrimitive.content,
            strainName = jsonObj["strainName"]!!.jsonPrimitive.content,
            batchNumber = jsonObj["batchNumber"]!!.jsonPrimitive.content,
            seedCount = jsonObj["seedCount"]!!.jsonPrimitive.content.toInt(),
            breeder = jsonObj["breeder"]!!.jsonPrimitive.content,
            seedType = ActualSeedType.valueOf(jsonObj["seedType"]!!.jsonPrimitive.content),
            acquisitionDate = LocalDate.parse(jsonObj["acquisitionDate"]!!.jsonPrimitive.content, DateTimeFormatter.ISO_LOCAL_DATE),
            source = jsonObj["source"]!!.jsonPrimitive.content,
            notes = jsonObj["notes"]!!.jsonPrimitive.content
        )
    }
} 