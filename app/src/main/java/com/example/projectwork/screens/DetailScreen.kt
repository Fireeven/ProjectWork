package com.example.projectwork.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projectwork.viewmodel.StoreViewModel
import com.example.projectwork.ui.components.NavigationButtons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    storeId: Int?,
    viewModel: StoreViewModel = viewModel()
) {
    val stores by viewModel.stores.collectAsState()
    val store = stores.find { it.id == storeId }
    
    // Add state for chatbot dialog
    var showChatDialog by remember { mutableStateOf(false) }
    val chatDialogState = remember { mutableStateOf(false) }
    
    // Update the wrapped state when our local state changes
    LaunchedEffect(showChatDialog) {
        chatDialogState.value = showChatDialog
    }
    
    // And vice versa
    LaunchedEffect(chatDialogState.value) {
        showChatDialog = chatDialogState.value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(store?.name ?: "Store Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add new item */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Item")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (store != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(bottom = 80.dp) // Add space for navigation buttons
                ) {
                    items(store.items) { item ->
                        ItemCard(
                            item = item,
                            onDeleteClick = { viewModel.deleteItemFromStore(store.id, item.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Store not found")
                }
            }
            
            // Add navigation buttons
            NavigationButtons(
                onBackClick = { navController.navigateUp() },
                showChatDialog = chatDialogState,
                showBackButton = true,
                showWelcomeButton = false,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun ItemCard(
    item: com.example.projectwork.data.StoreItem,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Item")
            }
        }
    }
} 