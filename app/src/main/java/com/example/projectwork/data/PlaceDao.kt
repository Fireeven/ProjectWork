package com.example.projectwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object) interface for interacting with the "places" table in the database
@Dao
interface PlaceDao {

    // Returns a Flow (reactive stream) of all places, sorted alphabetically by name
    @Query("SELECT * FROM places ORDER BY name")
    fun getAll(): Flow<List<PlaceEntity>>

    // Retrieves a specific place by its unique ID; returns null if not found
    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Int): PlaceEntity?

    // Inserts a new place or updates an existing one with the same primary key
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)

    // Deletes a specific place from the database
    @Delete
    suspend fun delete(place: PlaceEntity)
} 