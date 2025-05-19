package com.example.mygreenhouse.ui.screens.photomanagement

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.service.PhotoManagementService
import com.example.mygreenhouse.data.service.PhotoManagementService.PhotoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val photoManagementService = PhotoManagementService(application.applicationContext)
    
    // State variables
    private val _photos = MutableStateFlow<List<PhotoInfo>>(emptyList())
    val photos: StateFlow<List<PhotoInfo>> = _photos.asStateFlow()
    
    private val _totalPhotoSize = MutableStateFlow(0L)
    val totalPhotoSize: StateFlow<Long> = _totalPhotoSize.asStateFlow()
    
    private val _selectedPhoto = MutableStateFlow<PhotoInfo?>(null)
    val selectedPhoto: StateFlow<PhotoInfo?> = _selectedPhoto.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadPhotos()
    }
    
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allPhotos = photoManagementService.getAllPhotos()
                _photos.value = allPhotos
                _totalPhotoSize.value = allPhotos.sumOf { it.fileSize }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deletePhoto(photo: PhotoInfo) {
        viewModelScope.launch {
            try {
                val success = photoManagementService.deletePhoto(photo.uri)
                if (success) {
                    // Reload photos after delete
                    loadPhotos()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectPhoto(photo: PhotoInfo) {
        _selectedPhoto.value = photo
    }
    
    fun clearSelectedPhoto() {
        _selectedPhoto.value = null
    }
    
    fun formatFileSize(size: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            size < kb -> "$size B"
            size < mb -> String.format("%.2f KB", size / kb)
            size < gb -> String.format("%.2f MB", size / mb)
            else -> String.format("%.2f GB", size / gb)
        }
    }
    
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                PhotoManagementViewModel(application)
            }
        }
    }
} 