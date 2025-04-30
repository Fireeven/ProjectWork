package com.example.projectwork.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.data.GroceryItemDao
import com.example.projectwork.data.PlaceDao
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.repository.GroceryItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

data class GroceryListUiState(
    val items: List<GroceryItem> = emptyList(),
    val newItemName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val place: PlaceEntity? = null
)

sealed class GroceryListEvent {
    data class NewItemNameChanged(val text: String) : GroceryListEvent()
    data class ItemCheckedChanged(val itemId: Int, val isChecked: Boolean) : GroceryListEvent()
    object AddItemClicked : GroceryListEvent()
    data class DeleteItemClicked(val item: GroceryItem) : GroceryListEvent()
}

sealed class GroceryListUiEvent {
    data class ShowError(val message: String) : GroceryListUiEvent()
    data class OnItemCheckedChanged(val itemId: Int, val isChecked: Boolean) : GroceryListUiEvent()
    data class OnNewItemNameChanged(val name: String) : GroceryListUiEvent()
    data class OnAddItem(val name: String, val quantity: Int = 1) : GroceryListUiEvent()
    data class OnDeleteItem(val itemId: Int) : GroceryListUiEvent()
    data class OnQuantityChanged(val itemId: Int, val newQuantity: Int) : GroceryListUiEvent()
}

class GroceryListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val groceryItemDao: GroceryItemDao = database.groceryItemDao()
    private val placeDao: PlaceDao = database.placeDao()
    private var placeId: Int = 0

    private val _uiState = MutableStateFlow(GroceryListUiState())
    val uiState: StateFlow<GroceryListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GroceryListUiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadData()
    }

    fun loadItems(placeId: Int) {
        this.placeId = placeId
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { currentState -> 
                    currentState.copy(isLoading = true, error = null)
                }
                val place = placeDao.getById(placeId)
                groceryItemDao.getItemsForPlace(placeId).collect { itemsList ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            place = place,
                            items = itemsList,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState -> 
                    currentState.copy(error = e.message, isLoading = false)
                }
                _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    fun onEvent(event: GroceryListUiEvent) {
        when (event) {
            is GroceryListUiEvent.OnItemCheckedChanged -> {
                viewModelScope.launch {
                    try {
                        groceryItemDao.updateItemCheckedStatus(event.itemId, event.isChecked)
                    } catch (e: Exception) {
                        _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Failed to update item status"))
                    }
                }
            }
            is GroceryListUiEvent.OnNewItemNameChanged -> {
                _uiState.update { currentState -> 
                    currentState.copy(newItemName = event.name)
                }
            }
            is GroceryListUiEvent.OnAddItem -> {
                if (event.name.isNotBlank()) {
                    viewModelScope.launch {
                        try {
                            val newItem = GroceryItem(
                                placeId = placeId,
                                name = event.name.trim(),
                                quantity = event.quantity
                            )
                            groceryItemDao.upsertItem(newItem)
                        } catch (e: Exception) {
                            _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Failed to add item"))
                        }
                    }
                }
            }
            is GroceryListUiEvent.OnDeleteItem -> {
                viewModelScope.launch {
                    try {
                        val item = _uiState.value.items.find { it.id == event.itemId }
                        item?.let { groceryItemDao.deleteItem(it) }
                    } catch (e: Exception) {
                        _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Failed to delete item"))
                    }
                }
            }
            is GroceryListUiEvent.OnQuantityChanged -> {
                viewModelScope.launch {
                    try {
                        val validQuantity = maxOf(1, event.newQuantity)
                        groceryItemDao.updateItemQuantity(event.itemId, validQuantity)
                    } catch (e: Exception) {
                        _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Failed to update quantity"))
                    }
                }
            }
            is GroceryListUiEvent.ShowError -> {
                viewModelScope.launch {
                    _events.emit(event)
                }
            }
        }
    }
} 