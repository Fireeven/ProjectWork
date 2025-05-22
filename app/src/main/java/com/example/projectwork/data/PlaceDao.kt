package com.example.projectwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class PlaceWithCategory(
    @Embedded val place: PlaceEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY name")
    fun getAll(): Flow<List<PlaceEntity>>
    
    @Transaction
    @Query("SELECT * FROM places ORDER BY name")
    fun getAllWithCategory(): Flow<List<PlaceWithCategory>>
    
    @Transaction
    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getByIdWithCategory(id: Int): PlaceWithCategory?

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Int): PlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)

    @Delete
    suspend fun delete(place: PlaceEntity)
} 