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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import java.text.NumberFormat
import java.util.*

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
    val availablePlaces by viewModel.getAllPlaces().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
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

    val coroutineScope = rememberCoroutineScope()

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

    // Filter and sort items based on search query and sort order
    val filteredItems = remember(uiState.items, searchQuery, uiState.sortOrder) {
        val filtered = if (searchQuery.isBlank()) {
            uiState.items
        } else {
            uiState.items.filter { item ->
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.recipeTitle?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        // Apply sorting to filtered items
        when (uiState.sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOrder.QUANTITY_ASC -> filtered.sortedBy { it.quantity }
            SortOrder.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity }
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
                    // Sort buttons - directly accessible
                    IconButton(
                        onClick = {
                            viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.NAME_ASC))
                        }
                    ) {
                        Icon(
                            Icons.Filled.SortByAlpha,
                            contentDescription = "Sort A-Z",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            viewModel.onEvent(GroceryListUiEvent.OnSortOrderChanged(SortOrder.QUANTITY_ASC))
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort by Quantity",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // More options menu
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
                            HorizontalDivider()
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
                            HorizontalDivider()
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
                        items = uiState.items,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .graphicsLayer {
                                translationY = sin(headerElevation * Math.PI.toFloat() / 5) * 5
                                shadowElevation = headerElevation
                            }
                    )
                }
                
                // Static Search bar - always visible
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onDismiss = { 
                            searchQuery = "" // Only clear the search, don't hide bar
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Grocery List Items (using filtered items)
                items(
                    items = filteredItems,
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
                        onPriceChange = { newPrice ->
                            viewModel.onEvent(GroceryListUiEvent.OnPriceChanged(item.id, newPrice))
                        },
                        onPurchaseToggle = { isPurchased, actualPrice ->
                            viewModel.onEvent(GroceryListUiEvent.OnPurchaseToggle(item.id, isPurchased, actualPrice))
                        },
                        onMoveToPlace = { newPlaceId ->
                            viewModel.onEvent(GroceryListUiEvent.OnMoveItemToPlace(item.id, newPlaceId))
                        },
                        availablePlaces = availablePlaces,
                        showQuantityControls = false,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Total Amount Summary Card - at the end of the list
                if (filteredItems.isNotEmpty()) {
                    item {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        val totalEstimated = filteredItems.sumOf { (it.price * it.quantity) }
                        val totalActualSpent = filteredItems.filter { it.isPurchased }.sumOf { (it.actualPrice ?: it.price) * it.quantity }
                        val totalItems = filteredItems.size
                        val purchasedCount = filteredItems.count { it.isPurchased }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Shopping Summary",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Items",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$totalItems items",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Purchased",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$purchasedCount / $totalItems",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                                
                                if (totalEstimated > 0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Estimated Total",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = currencyFormatter.format(totalEstimated),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                        
                                        if (totalActualSpent > 0) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Actual Spent",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = currencyFormatter.format(totalActualSpent),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Progress bar for completion
                                    if (totalItems > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Text(
                                            text = "Progress: ${(purchasedCount * 100 / totalItems)}% complete",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        LinearProgressIndicator(
                                            progress = { purchasedCount.toFloat() / totalItems },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Add the navigation buttons with welcome screen and chatbot functionality
            NavigationButtons(
                onBackClick = onNavigateBack,
                onWelcomeClick = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                showChatDialog = chatDialogState,
                showBackButton = true,
                showWelcomeButton = true,
                groceryViewModel = viewModel,
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
 * PlaceDetails component with enhanced spending analytics
 * 
 * @param place The place entity to display details for
 * @param category The category associated with the place
 * @param items The grocery items for spending calculations
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun PlaceDetails(
    place: PlaceEntity?,
    category: Category?,
    items: List<GroceryItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (place == null) return

    // Calculate spending analytics
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    val purchasedItems = items.filter { it.isPurchased }
    val unpurchasedItems = items.filter { !it.isPurchased }
    
    val totalEstimated = items.sumOf { (it.price * it.quantity) }
    val totalActualSpent = purchasedItems.sumOf { (it.actualPrice ?: it.price) * it.quantity }
    val remainingBudget = unpurchasedItems.sumOf { (it.price * it.quantity) }
    val savings = purchasedItems.sumOf { 
        val estimated = it.price * it.quantity
        val actual = (it.actualPrice ?: it.price) * it.quantity
        estimated - actual
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Place Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        
        // Spending Analytics Row (only show if there are items with prices)
        if (items.isNotEmpty() && totalEstimated > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Spent Amount Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = currencyFormatter.format(totalActualSpent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (purchasedItems.isNotEmpty()) {
                            Text(
                                text = "${purchasedItems.size} items bought",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Budget/Remaining Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingBudget > 0) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (remainingBudget > 0) Icons.Default.PendingActions else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (remainingBudget > 0) 
                                    MaterialTheme.colorScheme.tertiary 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (remainingBudget > 0) "Remaining" else "Complete",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (remainingBudget > 0) 
                                    MaterialTheme.colorScheme.tertiary 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (remainingBudget > 0) 
                                currencyFormatter.format(remainingBudget)
                            else 
                                "All purchased",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingBudget > 0) 
                                MaterialTheme.colorScheme.onTertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (unpurchasedItems.isNotEmpty()) {
                            Text(
                                text = "${unpurchasedItems.size} items left",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (remainingBudget > 0) 
                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Savings indicator (only if there are savings/losses)
            if (savings != 0.0 && purchasedItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (savings >= 0) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (savings >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (savings >= 0) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (savings >= 0) 
                                "You saved ${currencyFormatter.format(savings)} vs estimated!"
                            else 
                                "You spent ${currencyFormatter.format(-savings)} more than estimated",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (savings >= 0) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
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
    onPriceChange: (Double) -> Unit = {},
    onPurchaseToggle: (Boolean, Double?) -> Unit = { _, _ -> },
    onMoveToPlace: (Int) -> Unit = {},
    availablePlaces: List<PlaceEntity> = emptyList(),
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
            onPriceChange = onPriceChange,
            onPurchaseToggle = onPurchaseToggle,
            onMoveToPlace = onMoveToPlace,
            availablePlaces = availablePlaces,
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search items and recipes...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            if (query.isNotEmpty()) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 