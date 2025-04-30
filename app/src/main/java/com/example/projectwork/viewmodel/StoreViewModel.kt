package com.example.projectwork.viewmodel

import androidx.lifecycle.ViewModel
import com.example.projectwork.data.Store
import com.example.projectwork.data.StoreItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreViewModel : ViewModel() {
    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    val stores: StateFlow<List<Store>> = _stores.asStateFlow()

    private val _selectedStore = MutableStateFlow<Store?>(null)
    val selectedStore: StateFlow<Store?> = _selectedStore.asStateFlow()

    init {
        // Initialize with some sample data
        _stores.value = listOf(
            Store(1, "Grocery Store", listOf(
                StoreItem(1, "Milk", 2),
                StoreItem(2, "Bread", 1),
                StoreItem(3, "Eggs", 12),
                StoreItem(4, "Cheese", 1),
                StoreItem(5, "Butter", 1)
            )),
            Store(2, "Drugstore", listOf(
                StoreItem(1, "Toothpaste", 1),
                StoreItem(2, "Shampoo", 1),
                StoreItem(3, "Soap", 2)
            ))
        )
    }

    fun selectStore(store: Store) {
        _selectedStore.value = store
    }

    fun addStore(store: Store) {
        _stores.value = _stores.value + store
    }

    fun deleteStore(store: Store) {
        _stores.value = _stores.value.filter { it.id != store.id }
    }

    fun addItemToStore(storeId: Int, item: StoreItem) {
        _stores.value = _stores.value.map { store ->
            if (store.id == storeId) {
                store.copy(items = store.items + item)
            } else {
                store
            }
        }
    }

    fun deleteItemFromStore(storeId: Int, itemId: Int) {
        _stores.value = _stores.value.map { store ->
            if (store.id == storeId) {
                store.copy(items = store.items.filter { it.id != itemId })
            } else {
                store
            }
        }
    }
} 