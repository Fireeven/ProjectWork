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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.viewmodel.GroceryListViewModel
import com.example.projectwork.viewmodel.GroceryListUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    placeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: GroceryListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(placeId) {
        viewModel.loadItems(placeId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GroceryListUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Add new item section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var newItemText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { text ->
                        newItemText = text
                        viewModel.onEvent(GroceryListUiEvent.OnNewItemNameChanged(text))
                    },
                    label = { Text("Add new item") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.onEvent(GroceryListUiEvent.OnAddItem(newItemText))
                            newItemText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add item")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Grocery items list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { it.id }
                ) { item ->
                    GroceryItemRow(
                        item = item,
                        onCheckedChange = { isChecked ->
                            viewModel.onEvent(GroceryListUiEvent.OnItemCheckedChanged(item.id, isChecked))
                        },
                        onDelete = {
                            viewModel.onEvent(GroceryListUiEvent.OnDeleteItem(item.id))
                        },
                        onQuantityChange = { newQuantity ->
                            viewModel.onEvent(GroceryListUiEvent.OnQuantityChanged(item.id, newQuantity))
                        },
                        showQuantityControls = true // ← this line fixes the error
                    )

                }
            }
        }
    }
}

@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    showQuantityControls: Boolean, // <-- Add this line
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            if (showQuantityControls) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        enabled = item.quantity > 1
                    ) {
                        Text("-")
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) }
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

