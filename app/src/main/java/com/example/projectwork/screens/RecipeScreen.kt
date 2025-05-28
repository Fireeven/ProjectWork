package com.example.projectwork.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.ui.components.NavigationButtons
import com.example.projectwork.utils.OpenAIHelper
import com.example.projectwork.utils.SimpleRecipe
import com.example.projectwork.viewmodel.GroceryListViewModel
import com.example.projectwork.viewmodel.GroceryListUiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enhanced data classes for recipes
data class RecipeCategory(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val description: String
)

data class EnhancedRecipe(
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val category: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val cookingTime: String,
    val servings: Int,
    val tags: List<String>,
    val nutritionInfo: String? = null,
    val isFavorite: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    onBackClick: () -> Unit,
    groceryViewModel: GroceryListViewModel = viewModel()
) {
    val uiState by groceryViewModel.uiState.collectAsState()
    val showChatDialog = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    
    // State management
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf(listOf<EnhancedRecipe>()) }
    var suggestedRecipes by remember { mutableStateOf(listOf<SimpleRecipe>()) }
    var showContent by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }
    
    // Recipe categories
    val categories = listOf(
        RecipeCategory("All", Icons.Default.Restaurant, MaterialTheme.colorScheme.primary, "All recipes"),
        RecipeCategory("Breakfast", Icons.Default.FreeBreakfast, Color(0xFFF59E0B), "Start your day right"),
        RecipeCategory("Lunch", Icons.Default.LunchDining, Color(0xFF10B981), "Midday meals"),
        RecipeCategory("Dinner", Icons.Default.DinnerDining, Color(0xFF8B5CF6), "Evening delights"),
        RecipeCategory("Dessert", Icons.Default.Cake, Color(0xFFEC4899), "Sweet treats"),
        RecipeCategory("Snacks", Icons.Default.Cookie, Color(0xFF06B6D4), "Quick bites"),
        RecipeCategory("Healthy", Icons.Default.FavoriteBorder, Color(0xFF10B981), "Nutritious options")
    )
    
    // Launch animation
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
        // Load initial recipes
        loadSampleRecipes { recipes = it }
    }
    
    // Get available ingredients for smart suggestions
    val availableIngredients = uiState.items.map { it.name }
    
    // Function to search recipes with AI
    fun searchRecipesWithAI(query: String) {
        if (query.isBlank()) return
        
        isLoading = true
        coroutineScope.launch {
            try {
                val result = OpenAIHelper.getRecipeFromIngredients(
                    if (query == "random recipe") 
                        listOf("surprise me with a random recipe") 
                    else 
                        availableIngredients + listOf(query)
                )
                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()
                    suggestedRecipes = recipes
                    if (recipes.isNotEmpty()) {
                        // Show success message
                        coroutineScope.launch {
                            // You can add a snackbar here if needed
                        }
                    }
                } else {
                    // Handle API error
                    coroutineScope.launch {
                        // Show error message
                    }
                }
            } catch (e: Exception) {
                // Handle error
                coroutineScope.launch {
                    // Show error message
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    // Function to get recipes based on available ingredients
    fun getRecipesFromIngredients() {
        if (availableIngredients.isEmpty()) {
            // Show message that no ingredients are available
            return
        }
        
        isLoading = true
        coroutineScope.launch {
            try {
                val result = OpenAIHelper.getRecipeFromIngredients(availableIngredients)
                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()
                    suggestedRecipes = recipes
                } else {
                    // Handle API error
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    // Function to add ingredients to grocery list
    fun addIngredientsToGroceryList(ingredients: List<String>) {
        coroutineScope.launch {
            try {
                // Get the first available place
                groceryViewModel.getAllPlaces().collect { places ->
                    if (places.isNotEmpty()) {
                        val targetPlaceId = places.first().id
                        
                        // Load items for the target place first
                        groceryViewModel.loadItems(targetPlaceId)
                        
                        // Add each missing ingredient
                        ingredients.forEach { ingredient ->
                            val ingredientTrimmed = ingredient.trim()
                            val alreadyExists = availableIngredients.any { existing ->
                                existing.equals(ingredientTrimmed, ignoreCase = true)
                            }
                            if (!alreadyExists) {
                                groceryViewModel.onEvent(
                                    GroceryListUiEvent.OnAddItem(
                                        name = ingredientTrimmed,
                                        quantity = 1
                                    )
                                )
                            }
                        }
                    }
                    return@collect // Exit after first collection
                }
                
                // Show success feedback
                // You can add snackbar or toast here
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Enhanced Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recipe Hub",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Discover delicious recipes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        
                        IconButton(
                            onClick = { showFilters = !showFilters }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search recipes or ingredients...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f),
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                searchRecipesWithAI(searchQuery)
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true
                    )
                    
                    // Filters
                    AnimatedVisibility(visible = showFilters) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Difficulty filter
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listOf("All", "Easy", "Medium", "Hard")) { difficulty ->
                                    FilterChip(
                                        onClick = { selectedDifficulty = difficulty },
                                        label = { Text(difficulty) },
                                        selected = selectedDifficulty == difficulty,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = Color.Transparent,
                                            labelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut() + shrinkVertically()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Quick Actions
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    "Quick Actions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    QuickActionButton(
                                        title = "Use My Ingredients",
                                        subtitle = "${availableIngredients.size} available",
                                        icon = Icons.Default.Kitchen,
                                        onClick = { getRecipesFromIngredients() },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    QuickActionButton(
                                        title = "Random Recipe",
                                        subtitle = "Surprise me!",
                                        icon = Icons.Default.Shuffle,
                                        onClick = { searchRecipesWithAI("random recipe") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Category Selection
                    item {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(categories) { category ->
                                CategoryCard(
                                    category = category,
                                    isSelected = selectedCategory == category.name,
                                    onClick = { selectedCategory = category.name }
                                )
                            }
                        }
                    }
                    
                    // AI Suggested Recipes
                    if (suggestedRecipes.isNotEmpty()) {
                        item {
                            Text(
                                "AI Suggested Recipes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        
                        items(suggestedRecipes) { recipe ->
                            AIRecipeCard(
                                recipe = recipe,
                                availableIngredients = availableIngredients,
                                onAddToGroceryList = { ingredients ->
                                    addIngredientsToGroceryList(ingredients)
                                }
                            )
                        }
                    }
                    
                    // Loading indicator
                    if (isLoading) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Finding perfect recipes...")
                                }
                            }
                        }
                    }
                    
                    // Sample Recipes
                    item {
                        Text(
                            "Popular Recipes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    
                    items(
                        recipes.filter { recipe ->
                            (selectedCategory == "All" || recipe.category == selectedCategory) &&
                            (selectedDifficulty == "All" || recipe.difficulty == selectedDifficulty) &&
                            (searchQuery.isEmpty() || recipe.name.contains(searchQuery, ignoreCase = true) ||
                             recipe.ingredients.any { it.contains(searchQuery, ignoreCase = true) })
                        }
                    ) { recipe ->
                        EnhancedRecipeCard(
                            recipe = recipe,
                            onAddToGroceryList = { ingredients ->
                                addIngredientsToGroceryList(ingredients)
                            }
                        )
                    }
                    
                    // Empty state
                    if (recipes.isEmpty() && suggestedRecipes.isEmpty() && !isLoading) {
                        item {
                            EmptyRecipeState(
                                onSearchRecipes = { searchRecipesWithAI("popular recipes") }
                            )
                        }
                    }
                }
            }
        }
        
        // Navigation buttons
        NavigationButtons(
            onBackClick = onBackClick,
            showChatDialog = showChatDialog,
            groceryViewModel = groceryViewModel,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: RecipeCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, category.color) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AIRecipeCard(
    recipe: SimpleRecipe,
    availableIngredients: List<String>,
    onAddToGroceryList: (List<String>) -> Unit
) {
    val missingIngredients = recipe.ingredients.filter { !availableIngredients.contains(it) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI Suggested",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Ingredients:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            recipe.ingredients.forEach { ingredient ->
                val isAvailable = availableIngredients.contains(ingredient)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isAvailable) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (missingIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onAddToGroceryList(missingIngredients) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add ${missingIngredients.size} Missing Ingredients")
                }
            }
        }
    }
}

@Composable
private fun EnhancedRecipeCard(
    recipe: EnhancedRecipe,
    onAddToGroceryList: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Chip(
                            text = recipe.difficulty,
                            color = when (recipe.difficulty) {
                                "Easy" -> Color(0xFF10B981)
                                "Medium" -> Color(0xFFF59E0B)
                                "Hard" -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Chip(text = recipe.cookingTime, color = MaterialTheme.colorScheme.secondary)
                        Chip(text = "${recipe.servings} servings", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Ingredients:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    recipe.ingredients.forEach { ingredient ->
                        Text(
                            text = "• $ingredient",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    
                    if (recipe.instructions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Instructions:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        recipe.instructions.forEachIndexed { index, instruction ->
                            Text(
                                text = "${index + 1}. $instruction",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onAddToGroceryList(recipe.ingredients) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add All Ingredients to List")
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyRecipeState(
    onSearchRecipes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "No recipes found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Try searching for recipes or let AI suggest some based on your ingredients",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onSearchRecipes) {
                Text("Discover Recipes")
            }
        }
    }
}

// Helper function to load sample recipes
private fun loadSampleRecipes(onLoaded: (List<EnhancedRecipe>) -> Unit) {
    val sampleRecipes = listOf(
        EnhancedRecipe(
            id = "1",
            name = "Classic Spaghetti Carbonara",
            ingredients = listOf("Spaghetti", "Eggs", "Parmesan cheese", "Pancetta", "Black pepper", "Salt"),
            instructions = listOf(
                "Cook spaghetti according to package directions",
                "Fry pancetta until crispy",
                "Whisk eggs with parmesan and pepper",
                "Combine hot pasta with egg mixture",
                "Add pancetta and serve immediately"
            ),
            category = "Dinner",
            difficulty = "Medium",
            cookingTime = "20 min",
            servings = 4,
            tags = listOf("Italian", "Pasta", "Quick")
        ),
        EnhancedRecipe(
            id = "2",
            name = "Avocado Toast",
            ingredients = listOf("Bread", "Avocado", "Lemon", "Salt", "Pepper", "Olive oil"),
            instructions = listOf(
                "Toast bread until golden",
                "Mash avocado with lemon juice",
                "Spread on toast",
                "Season with salt and pepper",
                "Drizzle with olive oil"
            ),
            category = "Breakfast",
            difficulty = "Easy",
            cookingTime = "5 min",
            servings = 2,
            tags = listOf("Healthy", "Quick", "Vegetarian")
        ),
        EnhancedRecipe(
            id = "3",
            name = "Chocolate Chip Cookies",
            ingredients = listOf("Flour", "Butter", "Sugar", "Brown sugar", "Eggs", "Vanilla", "Chocolate chips"),
            instructions = listOf(
                "Cream butter and sugars",
                "Add eggs and vanilla",
                "Mix in flour",
                "Fold in chocolate chips",
                "Bake at 375°F for 10 minutes"
            ),
            category = "Dessert",
            difficulty = "Easy",
            cookingTime = "25 min",
            servings = 24,
            tags = listOf("Sweet", "Baking", "Classic")
        )
    )
    
    onLoaded(sampleRecipes)
} 