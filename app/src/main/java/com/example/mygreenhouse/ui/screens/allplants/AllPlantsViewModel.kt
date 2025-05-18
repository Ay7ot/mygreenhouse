package com.example.mygreenhouse.ui.screens.allplants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.data.model.Plant
import com.example.mygreenhouse.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AllPlantsViewModel(application: Application) : AndroidViewModel(application) {

    private val plantRepository: PlantRepository = PlantRepository(AppDatabase.getDatabase(application).plantDao())

    val allPlants: StateFlow<List<Plant>> = plantRepository.allActivePlants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantRepository.deletePlant(plant)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY] as Application
                AllPlantsViewModel(application)
            }
        }
    }
} 