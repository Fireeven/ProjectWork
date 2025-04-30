package com.example.projectwork.repository

import com.example.projectwork.data.GroceryItem
import com.example.projectwork.data.GroceryItemDao
import kotlinx.coroutines.flow.Flow

class GroceryItemRepository(private val groceryItemDao: GroceryItemDao) {
    fun getItemsForPlace(placeId: Int): Flow<List<GroceryItem>> = 
        groceryItemDao.getItemsForPlace(placeId)

    suspend fun addItem(item: GroceryItem) = groceryItemDao.upsertItem(item)

    suspend fun deleteItem(item: GroceryItem) = groceryItemDao.deleteItem(item)

    suspend fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean) =
        groceryItemDao.updateItemCheckedStatus(itemId, isChecked)
} 