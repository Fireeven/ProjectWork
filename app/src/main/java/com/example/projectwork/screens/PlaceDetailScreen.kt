package com.example.projectwork.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.data.Category
import com.example.projectwork.viewmodel.GroceryListViewModel
import com.example.projectwork.viewmodel.GroceryListUiEvent
import com.example.projectwork.viewmodel.SortOrder
import com.example.projectwork.ui.components.GroceryItemRow
import kotlin.math.sin
import com.example.projectwork.ui.components.NavigationButtons
import com.example.projectwork.navigation.Screen
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort

/**
 * Main screen for displaying and managing a place's grocery list.
 * This screen shows place details and its associated grocery items.
 *
 * Features:
 * - Display place information (name, category, address)
 * - Show list of grocery items with their quantities
 * - Add new items with quantity controls
 * - Delete existing items
 * - Check/uncheck items
 * - Navigate back to previous screen
 * - Edit place details
 *
 * @param placeId The ID of the place to display
 * @param onNavigateBack Callback for handling back navigation
 * @param onEditClick Callback for handling place edit action
 * @param viewModel The ViewModel instance for managing the grocery list state
 * @param navController The navigation controller to use for navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: GroceryListViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Add state for chatbot dialog
    var showChatDialog by remember { mutableStateOf(false) }
    // Create a MutableState wrapper for the showChatDialog to pass to the NavigationButtons
    val chatDialogState = remember { mutableStateOf(false) }
    
    // Update the wrapped state when our local state changes
    LaunchedEffect(showChatDialog) {
        chatDialogState.value = showChatDialog
    }
    
    // And vice versa
    LaunchedEffect(chatDialogState.value) {
        showChatDialog = chatDialogState.value
    }

    // Animation for FAB
    val fabRotation by animateFloatAsState(
        targetValue = if (showAddDialog) 45f else 0f,
        label = "fab_rotation"
    )

    // Continuous animation for cards
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val headerElevation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header_elevation"
    )

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
                is GroceryListUiEvent.PlaceDeleted -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Place") },
            text = { Text("Are you sure you want to delete this place? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlace()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddDialog) {
        AnimatedAddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, quantity ->
                viewModel.onEvent(GroceryListUiEvent.OnAddItem(name, quantity))
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        uiState.place?.name ?: "",
                        modifier = Modifier.graphicsLayer {
                            translationY = sin(headerElevation * Math.PI.toFloat() / 5) * 3
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Place") },
                                onClick = {
                                    onEditClick(placeId)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Grocery List") },
                                onClick = {
                                    navController.navigate(Screen.EditGroceryList.createRoute(placeId))
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Name (A-Z)") },
                                onClick = {
                                    viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.NAME_ASC))
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.SortByAlpha, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Name (Z-A)") },
                                onClick = {
                                    viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.NAME_DESC))
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.SortByAlpha, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Quantity (Low-High)") },
                                onClick = {
                                    viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.QUANTITY_ASC))
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Quantity (High-Low)") },
                                onClick = {
                                    viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.QUANTITY_DESC))
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Place") },
                                onClick = {
                                    showDeleteConfirmation = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.rotate(fabRotation)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PlaceDetails(
                        place = uiState.place,
                        category = uiState.category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .graphicsLayer {
                                translationY = sin(headerElevation * Math.PI.toFloat() / 5) * 5
                                shadowElevation = headerElevation
                            }
                    )
                }

                // Grocery List Items
                items(
                    items = uiState.items,
                    key = { item -> item.id }
                ) { item ->
                    AnimatedItemRow(
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
                        showQuantityControls = false,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Add the navigation buttons with welcome screen and chatbot functionality
            NavigationButtons(
                onBackClick = onNavigateBack,
                onWelcomeClick = { 
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                showChatDialog = chatDialogState,
                showBackButton = true,
                showWelcomeButton = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
    
    // Add simple chat dialog (or a placeholder for now)
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

/**
 * Displays detailed information about a place in a card format.
 * Shows category and address information with animations.
 *
 * @param place The place entity containing the details to display
 * @param category The category of the place
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun PlaceDetails(
    place: PlaceEntity?,
    category: Category?,
    modifier: Modifier = Modifier
) {
    if (place == null) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = category?.name ?: "Uncategorized",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Address Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Address",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = place.address ?: "No address",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * A reusable card component for displaying place information.
 * Features a bouncy scale animation on entry.
 *
 * @param icon The icon to display in the card
 * @param title The title text for the card
 * @param value The value text to display
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(0.8f) }
    
    LaunchedEffect(Unit) {
        animate(0.8f, 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )) { value, _ -> scale = value }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnimatedItemRow(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    showQuantityControls: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        GroceryItemRow(
            item = item,
            onCheckedChange = onCheckedChange,
            onDelete = onDelete,
            onQuantityChange = onQuantityChange,
            showQuantityControls = showQuantityControls,
            modifier = modifier
        )
    }
}

@Composable
private fun AnimatedAddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    
    Dialog(onDismissRequest = onDismiss) {
        var isVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            isVisible = true
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = if (isVisible) 1f else 0.8f
                    scaleY = if (isVisible) 1f else 0.8f
                    alpha = if (isVisible) 1f else 0f
                }
                .animateContentSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Add New Item",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val current = itemQuantity.toIntOrNull() ?: 1
                                if (current > 1) {
                                    itemQuantity = (current - 1).toString()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Remove, "Decrease")
                        }
                        
                        OutlinedTextField(
                            value = itemQuantity,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    itemQuantity = it
                                }
                            },
                            label = { Text("Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = {
                                val current = itemQuantity.toIntOrNull() ?: 1
                                itemQuantity = (current + 1).toString()
                            }
                        ) {
                            Icon(Icons.Filled.Add, "Increase")
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (itemName.isNotBlank()) {
                                    onConfirm(
                                        itemName,
                                        itemQuantity.toIntOrNull() ?: 1
                                    )
                                }
                            },
                            enabled = itemName.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
} 