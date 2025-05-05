package com.example.projectwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items WHERE placeId = :placeId ORDER BY name")
    fun getItemsForPlace(placeId: Int): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE recipeId = :recipeId")
    fun getItemsForRecipe(recipeId: String): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: GroceryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<GroceryItem>)

    @Delete
    suspend fun deleteItem(item: GroceryItem)

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean)

    @Query("UPDATE grocery_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateItemQuantity(itemId: Int, quantity: Int)

    @Query("UPDATE grocery_items SET price = :price WHERE id = :itemId")
    suspend fun updateItemPrice(itemId: Int, price: Double)
    
    @Query("SELECT SUM(price * quantity) FROM grocery_items WHERE placeId = :placeId")
    fun getTotalSpendingForPlace(placeId: Int): Flow<Double?>
    
    @Query("SELECT SUM(price * quantity) FROM grocery_items WHERE isChecked = 1")
    fun getTotalSpending(): Flow<Double?>
    
    @Query("SELECT DISTINCT recipeTitle FROM grocery_items WHERE recipeTitle IS NOT NULL")
    fun getAllRecipes(): Flow<List<String>>
    
    @Query("DELETE FROM grocery_items WHERE recipeId = :recipeId")
    suspend fun deleteRecipeItems(recipeId: String)
} 