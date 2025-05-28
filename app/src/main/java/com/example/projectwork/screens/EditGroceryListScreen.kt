package com.example.projectwork.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.viewmodel.GroceryListViewModel
import com.example.projectwork.viewmodel.GroceryListUiEvent
import com.example.projectwork.ui.components.GroceryItemRow
import com.example.projectwork.ui.components.NavigationButtons
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroceryListScreen(
    placeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: GroceryListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var newItemName by remember { mutableStateOf("") }
    
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

    LaunchedEffect(placeId) {
        viewModel.loadItems(placeId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
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
                title = { Text("Edit List/Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Add new item section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { 
                            newItemName = it
                            viewModel.onEvent(GroceryListUiEvent.OnNewItemNameChanged(it))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Add new item") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newItemName.isNotBlank()) {
                                        viewModel.onEvent(GroceryListUiEvent.OnAddItem(newItemName))
                                        newItemName = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add item")
                            }
                        }
                    )
                }

                // List of items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items) { item ->
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
                            showQuantityControls = true
                        )
                    }
                }
                
                // Add spacing to ensure content doesn't get covered by navigation buttons
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Add the navigation buttons
            NavigationButtons(
                onBackClick = onNavigateBack,
                showChatDialog = chatDialogState,
                showBackButton = true,
                showWelcomeButton = false,
                groceryViewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
} 