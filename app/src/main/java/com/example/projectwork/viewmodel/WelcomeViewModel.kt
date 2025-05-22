package com.example.projectwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectwork.data.AppDatabase
import com.example.projectwork.data.Category
import com.example.projectwork.data.PlaceEntity
import kotlinx.coroutines.launch

/**
 * ViewModel for the Welcome screen
 * Handles functionality like store creation
 */
class WelcomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val placeDao = database.placeDao()
    private val categoryDao = database.categoryDao()
    
    /**
     * Creates a new store/place with the given name and category
     * 
     * @param name The name of the store
     * @param categoryName The category name (will be created if it doesn't exist)
     * @param address The address of the store
     */
    fun createStore(name: String, categoryName: String, address: String = "") {
        viewModelScope.launch {
            try {
                // First, get or create the category
                var category = categoryDao.getCategoryByName(categoryName)
                
                if (category == null) {
                    // Create a new category if it doesn't exist
                    val newCategoryId = categoryDao.insert(Category(name = categoryName))
                    category = categoryDao.getById(newCategoryId.toInt())
                }
                
                // Create the place/store
                if (category != null) {
                    val place = PlaceEntity(
                        name = name,
                        address = address,
                        categoryId = category.id
                    )
                    placeDao.upsert(place)
                }
            } catch (e: Exception) {
                // In a real app, we'd report this error to the UI
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Factory for creating WelcomeViewModel instances
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WelcomeViewModel(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                )
            }
        }
    }
} 