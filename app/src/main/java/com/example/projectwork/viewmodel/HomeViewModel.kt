package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.PlaceWithItemCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val placeDao = database.placeDao()

    private val _places = MutableStateFlow<List<PlaceWithItemCount>>(emptyList())
    val places: StateFlow<List<PlaceWithItemCount>> = _places

    init {
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            placeDao.getPlacesWithItemCount().collect { places ->
                _places.value = places
            }
        }
    }
} 