package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.data.PlaceWithItemCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class PlaceExpense(
    val id: Int,
    val name: String,
    val totalExpense: Double,
    val itemCount: Int
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalExpense: Double = 0.0,
    val formattedTotalExpense: String = "",
    val placeExpenses: List<PlaceExpense> = emptyList(),
    val selectedCurrency: String = "USD"
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val groceryItemDao = database.groceryItemDao()
    private val placeDao = database.placeDao()
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    init {
        loadAnalyticsData()
    }
    
    fun changeCurrency(currencyCode: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currencyCode)
        formatCurrency()
    }
    
    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Combine data from places and total spending
            combine(
                placeDao.getPlacesWithItemCount(),
                groceryItemDao.getTotalSpending()
            ) { places, totalSpending ->
                // Calculate expense for each place
                val placeExpenses = mutableListOf<PlaceExpense>()
                
                for (place in places) {
                    // Get total spending for the place
                    var placeSpending = 0.0
                    groceryItemDao.getTotalSpendingForPlace(place.id).collect { spending ->
                        placeSpending = spending ?: 0.0
                        
                        placeExpenses.add(
                            PlaceExpense(
                                id = place.id,
                                name = place.name,
                                totalExpense = placeSpending,
                                itemCount = place.itemCount
                            )
                        )
                    }
                }
                
                // Sort places by total expense (highest first)
                val sortedPlaceExpenses = placeExpenses.sortedByDescending { it.totalExpense }
                
                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalExpense = totalSpending ?: 0.0,
                    placeExpenses = sortedPlaceExpenses
                )
                
                // Format currency values
                formatCurrency()
            }.collect {}
        }
    }
    
    private fun formatCurrency() {
        val currencyFormat = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(_uiState.value.selectedCurrency)
        }
        
        _uiState.value = _uiState.value.copy(
            formattedTotalExpense = currencyFormat.format(_uiState.value.totalExpense)
        )
    }
    
    fun refreshData() {
        loadAnalyticsData()
    }
} 