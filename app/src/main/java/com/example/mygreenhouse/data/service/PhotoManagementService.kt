package com.example.mygreenhouse.data.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.repository.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service for managing photos in the app
 */
class PhotoManagementService(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val plantRepository = PlantRepository(database.plantDao())
    
    private val TAG = "PhotoManagementService"
    
    /**
     * Data class to represent a photo with its details
     */
    data class PhotoInfo(
        val uri: Uri,
        val filename: String,
        val associatedPlantId: String? = null,
        val associatedPlantName: String? = null,
        val fileSize: Long = 0L,
        val lastModified: Long = 0L
    )
    
    /**
     * Get all photos used in the app with associated plant information
     */
    suspend fun getAllPhotos(): List<PhotoInfo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoInfo>()
        val plants = plantRepository.getAllPlantsOneShot()
        
        // Get all plant photos
        plants.forEach { plant ->
            if (plant.imagePath != null) {
                try {
                    val file = File(plant.imagePath)
                    if (file.exists()) {
                        photos.add(
                            PhotoInfo(
                                uri = Uri.fromFile(file),
                                filename = file.name,
                                associatedPlantId = plant.id,
                                associatedPlantName = plant.strainName,
                                fileSize = file.length(),
                                lastModified = file.lastModified()
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting photo info for plant ${plant.id}", e)
                }
            }
        }
        
        // Check for orphaned photos in the app's files directory
        val filesDir = context.filesDir
        val imageDir = File(filesDir, "plant_images")
        if (imageDir.exists() && imageDir.isDirectory) {
            val allImageFiles = imageDir.listFiles { file -> 
                file.isFile && (file.name.endsWith(".jpg", true) || 
                        file.name.endsWith(".jpeg", true) || 
                        file.name.endsWith(".png", true)) 
            } ?: emptyArray()
            
            // Get list of paths that are associated with plants
            val associatedPaths = plants
                .mapNotNull { it.imagePath }
                .toSet()
            
            // Add orphaned photos (ones that exist in the directory but aren't referenced by any plant)
            allImageFiles.forEach { file ->
                val filePath = file.absolutePath
                if (filePath !in associatedPaths) {
                    photos.add(
                        PhotoInfo(
                            uri = Uri.fromFile(file),
                            filename = file.name,
                            associatedPlantId = null,
                            associatedPlantName = null,
                            fileSize = file.length(),
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }
        
        // Sort by last modified date (newest first)
        return@withContext photos.sortedByDescending { it.lastModified }
    }
    
    /**
     * Delete a photo from the app's storage
     * @param photoUri URI of the photo to delete
     * @param updatePlant Whether to update the plant record if this photo is associated with a plant
     * @return true if the photo was successfully deleted
     */
    suspend fun deletePhoto(photoUri: Uri, updatePlant: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(photoUri.path ?: return@withContext false)
            if (!file.exists()) return@withContext false
            
            val filePath = file.absolutePath
            
            // If updatePlant is true, find any plants using this image and update them
            if (updatePlant) {
                val plants = plantRepository.getAllPlantsOneShot()
                plants.filter { it.imagePath == filePath }.forEach { plant ->
                    val updatedPlant = plant.copy(imagePath = null)
                    plantRepository.updatePlant(updatedPlant)
                }
            }
            
            // Delete the file
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Successfully deleted photo: $filePath")
            } else {
                Log.e(TAG, "Failed to delete photo: $filePath")
            }
            return@withContext deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo", e)
            return@withContext false
        }
    }
    
    /**
     * Get details about a specific photo
     * @param photoUri URI of the photo to get details for
     * @return PhotoInfo object with details, or null if the photo doesn't exist
     */
    suspend fun getPhotoDetails(photoUri: Uri): PhotoInfo? = withContext(Dispatchers.IO) {
        try {
            val file = File(photoUri.path ?: return@withContext null)
            if (!file.exists()) return@withContext null
            
            val filePath = file.absolutePath
            
            // Check if this photo is associated with any plants
            val plants = plantRepository.getAllPlantsOneShot()
            val associatedPlant = plants.find { it.imagePath == filePath }
            
            return@withContext PhotoInfo(
                uri = photoUri,
                filename = file.name,
                associatedPlantId = associatedPlant?.id,
                associatedPlantName = associatedPlant?.strainName,
                fileSize = file.length(),
                lastModified = file.lastModified()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting photo details", e)
            return@withContext null
        }
    }
    
    /**
     * Calculate the total size of all photos in the app
     * @return total size in bytes
     */
    suspend fun calculateTotalPhotoSize(): Long = withContext(Dispatchers.IO) {
        val photos = getAllPhotos()
        return@withContext photos.sumOf { it.fileSize }
    }
} 