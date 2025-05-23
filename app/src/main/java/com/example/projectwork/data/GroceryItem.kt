package com.example.projectwork.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Declare a Room Entity representing a table named "grocery_items" in the local database
@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeId: Int,
    val name: String,
    val isChecked: Boolean = false,
    val quantity: Int = 1
) 