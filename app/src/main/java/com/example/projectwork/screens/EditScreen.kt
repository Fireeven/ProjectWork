package com.example.projectwork.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectwork.viewmodel.StoreViewModel

@Composable
fun EditScreen(
    navController: NavController,
    storeId: Int?,
    viewModel: StoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
        }
    }
} 