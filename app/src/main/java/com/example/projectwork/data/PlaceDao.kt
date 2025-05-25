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

data class PlaceWithItemCount(
    val id: Int,
    val name: String,
    val category: String?,
    val address: String?,
    val itemCount: Int
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

    @Query("""
        SELECT 
            p.id,
            p.name,
            c.name as category,
            p.address,
            (SELECT COUNT(*) FROM grocery_items gi WHERE gi.placeId = p.id) as itemCount
        FROM places p
        LEFT JOIN categories c ON p.categoryId = c.id
        GROUP BY p.id, p.name, c.name, p.address
        ORDER BY p.name
    """)
    fun getPlacesWithItemCount(): Flow<List<PlaceWithItemCount>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Int): PlaceEntity

    @Upsert
    suspend fun upsert(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity): Long

    @Delete
    suspend fun delete(place: PlaceEntity)

    @Update
    suspend fun update(place: PlaceEntity)
} 