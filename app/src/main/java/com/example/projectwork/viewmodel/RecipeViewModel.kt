package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.utils.OpenAIHelper
import com.example.projectwork.utils.Recipe
import com.example.projectwork.utils.RecipeParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recipeQuery: String = "",
    val currentRecipe: Recipe? = null,
    val savedRecipes: List<String> = emptyList(),
    val isSuccess: Boolean = false
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val groceryItemDao = database.groceryItemDao()
    
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()
    
    init {
        loadSavedRecipes()
    }
    
    private fun loadSavedRecipes() {
        viewModelScope.launch {
            groceryItemDao.getAllRecipes().collect { recipeNames ->
                _uiState.value = _uiState.value.copy(savedRecipes = recipeNames)
            }
        }
    }
    
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(recipeQuery = query)
    }
    
    fun fetchRecipe() {
        val query = _uiState.value.recipeQuery
        if (query.isBlank()) return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
        
        viewModelScope.launch {
            try {
                val response = OpenAIHelper.getRecipeFromOpenAI(query)
                val firstChoice = response.choices.firstOrNull()
                
                if (firstChoice != null) {
                    val content = firstChoice.message.content
                    val recipe = RecipeParser.parseRecipe(content)
                    
                    if (recipe != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentRecipe = recipe,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Could not parse recipe. Please try a different query."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No recipe found. Please try a different query."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error fetching recipe: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun createGroceryListFromRecipe(placeId: Int) {
        val recipe = _uiState.value.currentRecipe ?: return
        
        viewModelScope.launch {
            try {
                // First delete any existing items with this recipe ID
                groceryItemDao.deleteRecipeItems(recipe.id)
                
                // Create grocery items from ingredients
                val groceryItems = recipe.ingredients.map { ingredient ->
                    val quantityValue = try {
                        ingredient.quantity.trim().toIntOrNull() ?: 1
                    } catch (e: Exception) {
                        1
                    }
                    
                    GroceryItem(
                        placeId = placeId,
                        name = if (ingredient.unit != null) "${ingredient.name} (${ingredient.unit})" else ingredient.name,
                        quantity = quantityValue,
                        recipeId = recipe.id,
                        recipeTitle = recipe.title
                    )
                }
                
                // Insert all items at once
                groceryItemDao.upsertItems(groceryItems)
                
                // Mark as success
                _uiState.value = _uiState.value.copy(isSuccess = true)
                
                // Refresh saved recipes
                loadSavedRecipes()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error creating grocery list: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun clearCurrentRecipe() {
        _uiState.value = _uiState.value.copy(
            currentRecipe = null,
            isSuccess = false
        )
    }
} 