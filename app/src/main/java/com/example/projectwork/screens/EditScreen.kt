package com.example.projectwork.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectwork.viewmodel.StoreViewModel
import com.example.projectwork.ui.components.NavigationButtons
import androidx.compose.runtime.collectAsState
import com.example.projectwork.utils.OpenAIHelper
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    storeId: Int?,
    viewModel: StoreViewModel = viewModel()
) {
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    
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
                title = { Text("Add Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (storeId != null && itemName.isNotBlank() && quantity.isNotBlank()) {
                                viewModel.addItemToStore(
                                    storeId,
                                    com.example.projectwork.data.StoreItem(
                                        id = System.currentTimeMillis().toInt(),
                                        name = itemName,
                                        quantity = quantity.toIntOrNull() ?: 1
                                    )
                                )
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .padding(bottom = 80.dp), // Space for navigation buttons
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            if (current > 1) {
                                quantity = (current - 1).toString()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Remove, "Decrease")
                    }
                    
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                quantity = it
                            }
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            quantity = (current + 1).toString()
                        }
                    ) {
                        Icon(Icons.Filled.Add, "Increase")
                    }
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