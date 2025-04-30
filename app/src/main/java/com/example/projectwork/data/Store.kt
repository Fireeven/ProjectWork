package com.example.projectwork.data

data class Store(
    val id: Int,
    val name: String,
    val items: List<StoreItem> = emptyList()
)

data class StoreItem(
    val id: Int,
    val name: String,
    val quantity: Int
) 