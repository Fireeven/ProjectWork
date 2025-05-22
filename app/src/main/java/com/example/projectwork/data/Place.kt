package com.example.projectwork.data

import androidx.room.TypeConverter

enum class PlaceCategory {
    SUPERMARKET,
    DRUGSTORE,
    CONVENIENCE_STORE,
    OTHER;

    companion object {
        fun fromString(value: String): PlaceCategory = 
            values().find { it.name == value } ?: OTHER
    }
}

class Converters {
    @TypeConverter
    fun fromCategory(category: PlaceCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): PlaceCategory = PlaceCategory.fromString(value)
} 