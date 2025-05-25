package com.example.projectwork.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.Category
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class AddEditPlaceState(
    val id: Int? = null,
    val name: String = "",
    val address: String = "",
    val category: Category? = null,
    val categories: List<Category> = emptyList(),
    val nameError: String? = null,
    val addressError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddEditPlaceEvent {
    data class NameChanged(val text: String) : AddEditPlaceEvent()
    data class AddressChanged(val text: String) : AddEditPlaceEvent()
    data class CategoryChanged(val category: Category) : AddEditPlaceEvent()
    object SaveClicked : AddEditPlaceEvent()
}

sealed class UiEvent {
    object Saved : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

class AddEditPlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaceRepository
    private val database = AppDatabase.getDatabase(application)
    private val categoryDao = database.categoryDao()
    var uiState by mutableStateOf(AddEditPlaceState())
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        repository = PlaceRepository(database.placeDao())
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryDao.getAll().collect { categories ->
                    if (categories.isEmpty()) {
                        // If no categories exist, insert default ones
                        val defaultCategories = listOf(
                            Category(name = "Grocery Store"),
                            Category(name = "Supermarket"),
                            Category(name = "Convenience Store"),
                            Category(name = "Pharmacy"),
                            Category(name = "Hardware Store"),
                            Category(name = "Other")
                        )
                        defaultCategories.forEach { category ->
                            categoryDao.insert(category)
                        }
                    }
                    uiState = uiState.copy(categories = categories)
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to load categories: ${e.message}"))
            }
        }
    }

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
                    categoryId = currentState.category?.id
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
                val category = it.categoryId?.let { categoryId -> 
                    categoryDao.getById(categoryId)
                }
                uiState = uiState.copy(
                    id = it.id,
                    name = it.name,
                    address = it.address ?: "",
                    category = category
                )
            }
        }
    }
} 