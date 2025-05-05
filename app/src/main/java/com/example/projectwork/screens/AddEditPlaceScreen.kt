package com.example.projectwork.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.data.Category
import com.example.projectwork.navigation.Screen
import com.example.projectwork.ui.components.NavigationButtons
import com.example.projectwork.viewmodel.AddEditPlaceEvent
import com.example.projectwork.viewmodel.AddEditPlaceViewModel
import com.example.projectwork.viewmodel.UiEvent
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlaceScreen(
    placeId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditPlaceViewModel = viewModel(),
    onNavigateToWelcome: () -> Unit = {}
) {
    val state = viewModel.uiState
    var showCategoryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
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
        placeId?.let { viewModel.loadPlace(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Saved -> onNavigateBack()
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (placeId != null) "Edit Place" else "Add Place") },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AddEditPlaceEvent.NameChanged(it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.onEvent(AddEditPlaceEvent.AddressChanged(it)) },
                    label = { Text("Address") },
                    singleLine = true,
                    isError = state.addressError != null,
                    supportingText = state.addressError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.category?.name ?: "Select Category",
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(Icons.Filled.ArrowDropDown, "Select category")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (showCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        title = { Text("Select Category") },
                        text = {
                            Column {
                                state.categories.forEach { category ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                viewModel.onEvent(AddEditPlaceEvent.CategoryChanged(category))
                                                showCategoryDialog = false
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = category.id == state.category?.id,
                                            onClick = {
                                                viewModel.onEvent(AddEditPlaceEvent.CategoryChanged(category))
                                                showCategoryDialog = false
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Button(
                    onClick = { viewModel.onEvent(AddEditPlaceEvent.SaveClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save")
                    }
                }
                
                // Add spacing to ensure content doesn't get covered by navigation buttons
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Add the navigation buttons
            NavigationButtons(
                onBackClick = onNavigateBack,
                onWelcomeClick = onNavigateToWelcome,
                showChatDialog = chatDialogState,
                showBackButton = true,
                showWelcomeButton = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
    
    // Add chat dialog
    if (showChatDialog) {
        Dialog(onDismissRequest = { showChatDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.6f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Recipe Chatbot",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Ask me about recipes and I'll help you create a grocery list!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(onClick = { showChatDialog = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }
} 