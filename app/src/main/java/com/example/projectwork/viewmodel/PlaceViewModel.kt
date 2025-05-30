package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.data.PlaceWithCategory
import com.example.projectwork.repository.PlaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaceRepository
    val places: StateFlow<List<PlaceWithCategory>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlaceRepository(database.placeDao())
        places = repository.getAllPlacesWithCategory().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun deletePlace(placeWithCategory: PlaceWithCategory) {
        viewModelScope.launch {
            repository.deletePlace(placeWithCategory.place)
        }
    }
} 