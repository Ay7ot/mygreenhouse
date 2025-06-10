package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.StrainDao
import com.example.mygreenhouse.data.model.Strain
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository class for accessing strain archive data
 */
class StrainRepository(private val strainDao: StrainDao) {
    
    val allStrains: Flow<List<Strain>> = strainDao.getAllStrains()
    val strainCount: Flow<Int> = strainDao.getStrainCount()
    val customStrainCount: Flow<Int> = strainDao.getCustomStrainCount()
    
    suspend fun insertStrain(strain: Strain) {
        strainDao.insertStrain(strain)
    }
    
    suspend fun updateStrain(strain: Strain) {
        strainDao.updateStrain(strain)
    }
    
    suspend fun deleteStrain(strain: Strain) {
        strainDao.deleteStrain(strain)
    }
    
    fun getStrainById(id: String): Flow<Strain?> {
        return strainDao.getStrainById(id)
    }
    
    suspend fun getStrainByIdOnce(id: String): Strain? {
        return strainDao.getStrainByIdOnce(id)
    }
    
    suspend fun getStrainByName(name: String): Strain? {
        return strainDao.getStrainByName(name)
    }
    
    fun searchStrainsByName(searchQuery: String): Flow<List<Strain>> {
        return strainDao.searchStrainsByName(searchQuery)
    }
    
    suspend fun getAllStrainsOneShot(): List<Strain> {
        return strainDao.getAllStrainsOneShot()
    }
    
    /**
     * Archive a strain name or update its usage if it already exists
     */
    suspend fun archiveStrainName(strainName: String, isCustomStrain: Boolean = false) {
        val existingStrain = strainDao.getStrainByName(strainName)
        if (existingStrain != null) {
            // Update usage count and last used date
            strainDao.incrementStrainUsage(strainName, LocalDate.now())
        } else {
            // Create new strain entry
            val newStrain = Strain(
                name = strainName,
                isCustomStrain = isCustomStrain,
                firstUsedDate = LocalDate.now(),
                lastUsedDate = LocalDate.now(),
                usageCount = 1
            )
            strainDao.insertStrain(newStrain)
        }
    }
    
    /**
     * Reset strain usage count to 1 (for testing/debugging purposes)
     */
    suspend fun resetStrainUsageCount(strainName: String) {
        strainDao.resetStrainUsageCount(strainName)
    }
    
    /**
     * Delete all strains (for testing/debugging purposes)
     */
    suspend fun deleteAllStrains() {
        strainDao.deleteAllStrains()
    }
} 