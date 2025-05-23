package com.example.projectwork.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.PlaceCategory
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class AddEditPlaceState(
    val id: Int? = null,
    val name: String = "",
    val address: String = "",
    val category: PlaceCategory = PlaceCategory.SUPERMARKET,
    val nameError: String? = null,
    val addressError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddEditPlaceEvent {
    data class NameChanged(val text: String) : AddEditPlaceEvent()
    data class AddressChanged(val text: String) : AddEditPlaceEvent()
    data class CategoryChanged(val category: PlaceCategory) : AddEditPlaceEvent()
    object SaveClicked : AddEditPlaceEvent()
}

sealed class UiEvent {
    object Saved : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

// ViewModel for the Add/Edit Place screen
class AddEditPlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaceRepository
    var uiState by mutableStateOf(AddEditPlaceState())
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlaceRepository(database.placeDao())
    }

    // Handles UI events like text input changes or save button click
    fun onEvent(event: AddEditPlaceEvent) {
        when (event) {
            is AddEditPlaceEvent.NameChanged -> {
                uiState = uiState.copy(
                    name = event.text,
                    nameError = null
                )
            }
            is AddEditPlaceEvent.AddressChanged -> {
                uiState = uiState.copy(
                    address = event.text,
                    addressError = null
                )
            }
            is AddEditPlaceEvent.CategoryChanged -> {
                uiState = uiState.copy(category = event.category)
            }
            AddEditPlaceEvent.SaveClicked -> validateAndSave()
        }
    }

    // Validates input and saves or updates the place in the database
    private fun validateAndSave() {
        val currentState = uiState

        if (currentState.name.isBlank()) {
            uiState = currentState.copy(nameError = "Name is required")
            return
        }

        if (currentState.address.isBlank()) {
            uiState = currentState.copy(addressError = "Address is required")
            return
        }

        viewModelScope.launch {
            uiState = currentState.copy(isSaving = true)
            try {
                val place = PlaceEntity(
                    id = currentState.id ?: 0,
                    name = currentState.name.trim(),
                    address = currentState.address.trim(),
                    category = currentState.category
                )
                repository.upsertPlace(place)
                _uiEvent.emit(UiEvent.Saved)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError(e.message ?: "Unknown error occurred"))
            } finally {
                uiState = currentState.copy(isSaving = false)
            }
        }
    }

    fun loadPlace(id: Int) {
        viewModelScope.launch {
            val place = repository.getPlaceById(id)
            place?.let {
                uiState = uiState.copy(
                    id = it.id,
                    name = it.name,
                    address = it.address,
                    category = it.category
                )
            }
        }
    }
} 