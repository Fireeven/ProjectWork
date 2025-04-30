package com.example.projectwork.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectwork.navigation.Screen
import com.example.projectwork.viewmodel.StoreViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val stores by viewModel.stores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Lists") },
                actions = {
                    IconButton(onClick = { /* TODO: Add new store */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Store")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(stores) { store ->
                StoreItem(
                    store = store,
                    onStoreClick = { viewModel.selectStore(store) },
                    onEditClick = { navController.navigate(Screen.Edit.createRoute(store.id)) },
                    onDeleteClick = { viewModel.deleteStore(store) }
                )
            }
        }
    }
}

@Composable
fun StoreItem(
    store: com.example.projectwork.data.Store,
    onStoreClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onStoreClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${store.items.size} items",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Store")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Store")
            }
        }
    }
} 