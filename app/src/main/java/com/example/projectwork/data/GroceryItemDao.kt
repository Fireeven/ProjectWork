package com.example.projectwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items WHERE placeId = :placeId ORDER BY name")
    fun getItemsForPlace(placeId: Int): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: GroceryItem)

    @Delete
    suspend fun deleteItem(item: GroceryItem)

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean)
} 