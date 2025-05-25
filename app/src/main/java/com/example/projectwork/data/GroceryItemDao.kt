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
    
    // Purchase tracking methods
    @Query("UPDATE grocery_items SET isPurchased = :isPurchased, purchaseDate = :purchaseDate, actualPrice = :actualPrice WHERE id = :itemId")
    suspend fun markItemAsPurchased(itemId: Int, isPurchased: Boolean, purchaseDate: Long?, actualPrice: Double?)
    
    @Query("SELECT * FROM grocery_items WHERE isPurchased = 1 ORDER BY purchaseDate DESC")
    fun getPurchasedItems(): Flow<List<GroceryItem>>
    
    @Query("SELECT * FROM grocery_items WHERE isPurchased = 1 AND placeId = :placeId ORDER BY purchaseDate DESC")
    fun getPurchasedItemsForPlace(placeId: Int): Flow<List<GroceryItem>>
    
    // Analytics methods
    @Query("SELECT SUM(actualPrice * quantity) FROM grocery_items WHERE isPurchased = 1")
    fun getTotalActualSpending(): Flow<Double?>
    
    @Query("SELECT SUM(actualPrice * quantity) FROM grocery_items WHERE isPurchased = 1 AND placeId = :placeId")
    fun getTotalActualSpendingForPlace(placeId: Int): Flow<Double?>
    
    @Query("SELECT SUM(price * quantity) FROM grocery_items WHERE placeId = :placeId")
    fun getTotalEstimatedSpendingForPlace(placeId: Int): Flow<Double?>
    
    @Query("SELECT SUM(price * quantity) FROM grocery_items WHERE isChecked = 1")
    fun getTotalSpending(): Flow<Double?>
    
    @Query("SELECT DISTINCT recipeTitle FROM grocery_items WHERE recipeTitle IS NOT NULL")
    fun getAllRecipes(): Flow<List<String>>
    
    @Query("DELETE FROM grocery_items WHERE recipeId = :recipeId")
    suspend fun deleteRecipeItems(recipeId: String)
    
    // Weekly/Monthly analytics
    @Query("SELECT * FROM grocery_items WHERE isPurchased = 1 AND purchaseDate >= :startDate AND purchaseDate <= :endDate ORDER BY purchaseDate DESC")
    fun getPurchasedItemsInDateRange(startDate: Long, endDate: Long): Flow<List<GroceryItem>>
    
    @Query("SELECT SUM(actualPrice * quantity) FROM grocery_items WHERE isPurchased = 1 AND purchaseDate >= :startDate AND purchaseDate <= :endDate")
    fun getTotalSpendingInDateRange(startDate: Long, endDate: Long): Flow<Double?>
} 