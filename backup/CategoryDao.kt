package com.example.projectwork.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Int): Category?
    
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
} 