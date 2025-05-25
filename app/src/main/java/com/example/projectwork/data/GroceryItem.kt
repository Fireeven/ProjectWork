package com.example.projectwork.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("placeId")]
)
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeId: Int,
    val name: String,
    val quantity: Int = 1,
    val isChecked: Boolean = false,
    val price: Double = 0.0,
    val recipeId: String? = null,
    val recipeTitle: String? = null,
    val isPurchased: Boolean = false,
    val purchaseDate: Long? = null,
    val actualPrice: Double? = null // Price actually paid when purchased
) 