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
import com.example.projectwork.data.Category
import com.example.projectwork.data.CategoryDao
import com.example.projectwork.repository.GroceryItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

/**
 * UI State for the grocery list screen.
 * Contains all the data needed to render the UI.
 *
 * @property items List of grocery items for the current place
 * @property newItemName Name of the new item being added
 * @property isLoading Loading state indicator
 * @property error Error message if any
 * @property place Current place details
 * @property category Current place's category
 * @property sortOrder Current sort order
 */
data class GroceryListUiState(
    val items: List<GroceryItem> = emptyList(),
    val newItemName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val place: PlaceEntity? = null,
    val category: Category? = null,
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    QUANTITY_ASC,
    QUANTITY_DESC
}

/**
 * Events that can be triggered by user interactions in the grocery list.
 */
sealed class GroceryListEvent {
    data class NewItemNameChanged(val text: String) : GroceryListEvent()
    data class ItemCheckedChanged(val itemId: Int, val isChecked: Boolean) : GroceryListEvent()
    object AddItemClicked : GroceryListEvent()
    data class DeleteItemClicked(val item: GroceryItem) : GroceryListEvent()
}

/**
 * UI Events that can occur in the grocery list screen.
 * These events are processed by the ViewModel to update the UI state.
 */
sealed class GroceryListUiEvent {
    data class ShowError(val message: String) : GroceryListUiEvent()
    data class OnItemCheckedChanged(val itemId: Int, val isChecked: Boolean) : GroceryListUiEvent()
    data class OnNewItemNameChanged(val name: String) : GroceryListUiEvent()
    data class OnAddItem(val name: String, val quantity: Int = 1) : GroceryListUiEvent()
    data class OnDeleteItem(val itemId: Int) : GroceryListUiEvent()
    data class OnQuantityChanged(val itemId: Int, val newQuantity: Int) : GroceryListUiEvent()
    data class OnSortOrderChanged(val sortOrder: SortOrder) : GroceryListUiEvent()
    object PlaceDeleted : GroceryListUiEvent()
}

/**
 * ViewModel for managing grocery list functionality.
 * Handles all business logic and data operations for the grocery list feature.
 *
 * @property database Room database instance
 * @property groceryItemDao DAO for grocery item operations
 * @property placeDao DAO for place operations
 * @property categoryDao DAO for category operations
 */
class GroceryListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val groceryItemDao: GroceryItemDao = database.groceryItemDao()
    private val placeDao: PlaceDao = database.placeDao()
    private val categoryDao: CategoryDao = database.categoryDao()
    private var placeId: Int = 0

    private val _uiState = MutableStateFlow(GroceryListUiState())
    val uiState: StateFlow<GroceryListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GroceryListUiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadData()
    }

    /**
     * Loads grocery items for a specific place.
     * Updates the UI state with the loaded items and place details.
     *
     * @param placeId ID of the place to load items for
     */
    fun loadItems(placeId: Int) {
        this.placeId = placeId
        loadData()
    }

    /**
     * Internal function to load all necessary data.
     * Fetches place details and associated grocery items.
     * Updates UI state and handles errors.
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { currentState -> 
                    currentState.copy(isLoading = true, error = null)
                }
                val place = placeDao.getById(placeId)
                val category = place?.categoryId?.let { categoryDao.getById(it) }
                groceryItemDao.getItemsForPlace(placeId).collect { itemsList ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            place = place,
                            category = category,
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

    /**
     * Handles all UI events for the grocery list.
     * This is the main entry point for all user interactions.
     *
     * @param event The UI event to process
     */
    fun onEvent(event: GroceryListUiEvent) {
        viewModelScope.launch {
            try {
                when (event) {
                    is GroceryListUiEvent.OnItemCheckedChanged -> {
                        groceryItemDao.updateItemCheckedStatus(event.itemId, event.isChecked)
                    }
                    is GroceryListUiEvent.OnAddItem -> {
                        val newItem = GroceryItem(
                            placeId = placeId,
                            name = event.name,
                            quantity = event.quantity
                        )
                        groceryItemDao.upsertItem(newItem)
                    }
                    is GroceryListUiEvent.OnDeleteItem -> {
                        val item = uiState.value.items.find { it.id == event.itemId }
                        item?.let { groceryItemDao.deleteItem(it) }
                    }
                    is GroceryListUiEvent.OnQuantityChanged -> {
                        groceryItemDao.updateItemQuantity(event.itemId, event.newQuantity)
                    }
                    is GroceryListUiEvent.PlaceDeleted -> {
                        deletePlace()
                    }
                    is GroceryListUiEvent.OnSortOrderChanged -> {
                        _uiState.update { currentState ->
                            currentState.copy(sortOrder = event.sortOrder)
                        }
                        val sortedItems = when (event.sortOrder) {
                            SortOrder.NAME_ASC -> uiState.value.items.sortedBy { it.name }
                            SortOrder.NAME_DESC -> uiState.value.items.sortedByDescending { it.name }
                            SortOrder.QUANTITY_ASC -> uiState.value.items.sortedBy { it.quantity }
                            SortOrder.QUANTITY_DESC -> uiState.value.items.sortedByDescending { it.quantity }
                        }
                        _uiState.update { currentState ->
                            currentState.copy(items = sortedItems)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    fun deletePlace() {
        viewModelScope.launch {
            try {
                uiState.value.place?.let { place ->
                    placeDao.delete(place)
                    _events.emit(GroceryListUiEvent.PlaceDeleted)
                }
            } catch (e: Exception) {
                _events.emit(GroceryListUiEvent.ShowError(e.message ?: "Failed to delete place"))
            }
        }
    }
}