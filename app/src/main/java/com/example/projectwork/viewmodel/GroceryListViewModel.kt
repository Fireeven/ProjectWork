package com.example.projectwork.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.repository.GroceryItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class GroceryListState(
    val items: List<GroceryItem> = emptyList(),
    val newItemName: String = "",
    val isSaving: Boolean = false
)

sealed class GroceryListEvent {
    data class NewItemNameChanged(val text: String) : GroceryListEvent()
    data class ItemCheckedChanged(val itemId: Int, val isChecked: Boolean) : GroceryListEvent()
    object AddItemClicked : GroceryListEvent()
    data class DeleteItemClicked(val item: GroceryItem) : GroceryListEvent()
}

sealed class GroceryListUiEvent {
    data class ShowError(val message: String) : GroceryListUiEvent()
}

class GroceryListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GroceryItemRepository
    var uiState by mutableStateOf(GroceryListState())
        private set

    private val _uiEvent = MutableSharedFlow<GroceryListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GroceryItemRepository(database.groceryItemDao())
    }

    fun loadItems(placeId: Int) {
        viewModelScope.launch {
            repository.getItemsForPlace(placeId).collect { items ->
                uiState = uiState.copy(items = items)
            }
        }
    }

    fun onEvent(event: GroceryListEvent) {
        when (event) {
            is GroceryListEvent.NewItemNameChanged -> {
                uiState = uiState.copy(newItemName = event.text)
            }
            is GroceryListEvent.ItemCheckedChanged -> {
                viewModelScope.launch {
                    repository.updateItemCheckedStatus(event.itemId, event.isChecked)
                }
            }
            GroceryListEvent.AddItemClicked -> addItem()
            is GroceryListEvent.DeleteItemClicked -> deleteItem(event.item)
        }
    }

    private fun addItem() {
        val currentState = uiState
        if (currentState.newItemName.isBlank()) return

        viewModelScope.launch {
            try {
                val item = GroceryItem(
                    name = currentState.newItemName.trim(),
                    placeId = currentState.items.firstOrNull()?.placeId ?: return@launch
                )
                repository.addItem(item)
                uiState = currentState.copy(newItemName = "")
            } catch (e: Exception) {
                _uiEvent.emit(GroceryListUiEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    private fun deleteItem(item: GroceryItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
            } catch (e: Exception) {
                _uiEvent.emit(GroceryListUiEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }
} 