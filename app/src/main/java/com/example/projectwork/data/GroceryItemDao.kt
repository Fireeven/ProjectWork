package com.example.projectwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Data Access Object (DAO) for interacting with the grocery_items table
@Dao
interface GroceryItemDao {

    // Returns grocery items for a specific place, ordered by name
    @Query("SELECT * FROM grocery_items WHERE placeId = :placeId ORDER BY name")
    fun getItemsForPlace(placeId: Int): Flow<List<GroceryItem>>

    // Inserts a new item or updates it if it already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: GroceryItem)

    // Deletes a grocery item from the database
    @Delete
    suspend fun deleteItem(item: GroceryItem)

    // Updates the isChecked status of a grocery item
    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean)

    // Updates the quantity of a grocery item
    @Query("UPDATE grocery_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateItemQuantity(itemId: Int, quantity: Int)
} 