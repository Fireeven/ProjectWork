package com.example.projectwork.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.utils.ChatActionType
import com.example.projectwork.utils.ChatMessage
import com.example.projectwork.utils.OpenAIHelper
import com.example.projectwork.utils.SimpleRecipe
import com.example.projectwork.viewmodel.GroceryListViewModel
import com.example.projectwork.viewmodel.GroceryListUiEvent
import com.example.projectwork.data.GroceryItem
import kotlinx.coroutines.launch

/**
 * A navigation bar with multiple buttons in a row
 * 
 * @param onBackClick Callback for when the back button is clicked
 * @param onWelcomeClick Callback for navigating to home screen (renamed for clarity)
 * @param showChatDialog MutableState to control the visibility of the chat dialog
 * @param modifier Optional modifier for customizing the layout
 * @param showBackButton Whether to show the back button (default: true)
 * @param showWelcomeButton Whether to show the home button (default: true) - renamed for clarity
 * @param groceryViewModel Optional grocery list view model for recipe integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationButtons(
    onBackClick: () -> Unit = {},
    onWelcomeClick: () -> Unit = {},
    showChatDialog: MutableState<Boolean>,
    showBackButton: Boolean = true,
    showWelcomeButton: Boolean = true,
    groceryViewModel: GroceryListViewModel? = null,
    modifier: Modifier = Modifier
) {
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isLoading by remember { mutableStateOf(false) }
    var suggestedIngredients by remember { mutableStateOf(listOf<String>()) }
    var suggestedRecipes by remember { mutableStateOf(listOf<SimpleRecipe>()) }
    var selectedCuisine by remember { mutableStateOf("") }
    var selectedDietaryRestriction by remember { mutableStateOf("") }
    
    // Recipe integration states
    var showRecipeActionDialog by remember { mutableStateOf(false) }
    var pendingRecipe by remember { mutableStateOf<SimpleRecipe?>(null) }
    var availablePlaces by remember { mutableStateOf(listOf<com.example.projectwork.data.PlaceEntity>()) }
    var selectedPlaceId by remember { mutableStateOf<Int?>(null) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    
    // Load available places if groceryViewModel is provided
    LaunchedEffect(groceryViewModel) {
        groceryViewModel?.let { vm ->
            vm.getAllPlaces().collect { places ->
                availablePlaces = places
                if (selectedPlaceId == null && places.isNotEmpty()) {
                    selectedPlaceId = places.first().id
                }
            }
        }
    }
    
    // Function to extract recipe from chatbot response
    fun extractRecipeFromResponse(response: String): SimpleRecipe? {
        return try {
            val lines = response.split("\n").map { it.trim() }
            var recipeName = "Generated Recipe"
            val ingredients = mutableListOf<String>()
            val instructions = mutableListOf<String>()
            
            var currentSection = ""
            var stepCounter = 1
            
            for (line in lines) {
                when {
                    line.startsWith("#") && !line.startsWith("##") -> {
                        recipeName = line.removePrefix("#").trim()
                    }
                    line.contains("ingredients", ignoreCase = true) && line.startsWith("##") -> {
                        currentSection = "ingredients"
                    }
                    line.contains("instructions", ignoreCase = true) && line.startsWith("##") -> {
                        currentSection = "instructions"
                    }
                    line.startsWith("-") || line.startsWith("*") || line.startsWith("•") -> {
                        when (currentSection) {
                            "ingredients" -> ingredients.add(line.removePrefix("-").removePrefix("*").removePrefix("•").trim())
                            "instructions" -> instructions.add("$stepCounter. ${line.removePrefix("-").removePrefix("*").removePrefix("•").trim()}")
                        }
                        if (currentSection == "instructions") stepCounter++
                    }
                    line.matches(Regex("^\\d+\\..*")) -> {
                        if (currentSection == "instructions") {
                            instructions.add(line.trim())
                        }
                    }
                    line.isNotBlank() && currentSection == "ingredients" && !line.startsWith("#") -> {
                        ingredients.add(line.trim())
                    }
                    line.isNotBlank() && currentSection == "instructions" && !line.startsWith("#") -> {
                        instructions.add("$stepCounter. ${line.trim()}")
                        stepCounter++
                    }
                }
            }
            
            if (ingredients.isNotEmpty()) {
                SimpleRecipe(
                    name = recipeName,
                    ingredients = ingredients,
                    instructions = if (instructions.isNotEmpty()) instructions else listOf(
                        "1. Prepare your ingredients",
                        "2. Follow the recipe steps",
                        "3. Cook according to requirements",
                        "4. Serve and enjoy!"
                    )
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Enhanced function to detect and handle recipes
    fun detectAndHandleRecipe(response: String) {
        // Simple recipe detection patterns
        val hasRecipeKeywords = response.contains("recipe", ignoreCase = true) ||
                response.contains("ingredients", ignoreCase = true) ||
                response.contains("instructions", ignoreCase = true) ||
                response.contains("cook", ignoreCase = true)
        
        if (hasRecipeKeywords) {
            // Try to extract recipe from response
            val extractedRecipe = extractRecipeFromResponse(response)
            if (extractedRecipe != null) {
                pendingRecipe = extractedRecipe
                showRecipeActionDialog = true
            }
        }
    }
    
    // Enhanced chatbot function
    fun handleEnhancedChat(message: String, actionType: String = "general") {
        if (message.isBlank()) return
        
        chatMessages = chatMessages + ChatMessage("user", message)
        userMessage = ""
        isLoading = true
        
        coroutineScope.launch {
            try {
                when (actionType) {
                    "ingredient_suggestions" -> {
                        val result = OpenAIHelper.getIngredientSuggestions(
                            cuisine = selectedCuisine,
                            dietaryRestrictions = selectedDietaryRestriction
                        )
                        if (result.isSuccess) {
                            val ingredients = result.getOrNull() ?: emptyList()
                            suggestedIngredients = ingredients
                            chatMessages = chatMessages + ChatMessage("assistant", "Here are some ingredient suggestions: ${ingredients.joinToString(", ")}")
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't get ingredient suggestions right now.")
                        }
                    }
                    "recipe_from_ingredients" -> {
                        val result = OpenAIHelper.getRecipeFromIngredients(suggestedIngredients)
                        if (result.isSuccess) {
                            val recipes = result.getOrNull() ?: emptyList()
                            suggestedRecipes = recipes
                            val recipeNames = recipes.map { it.name }
                            val responseText = "I found these recipes you can make: ${recipeNames.joinToString(", ")}"
                            chatMessages = chatMessages + ChatMessage("assistant", responseText)
                            
                            // Check if any recipe was found and trigger action dialog
                            if (recipes.isNotEmpty()) {
                                pendingRecipe = recipes.first()
                                showRecipeActionDialog = true
                            }
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't find recipes with those ingredients.")
                        }
                    }
                    "nutrition_info" -> {
                        val result = OpenAIHelper.getNutritionalInfo(suggestedIngredients)
                        if (result.isSuccess) {
                            val info = result.getOrNull() ?: "No nutritional information available."
                            chatMessages = chatMessages + ChatMessage("assistant", info)
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't get nutritional information right now.")
                        }
                    }
                    else -> {
                        val result = OpenAIHelper.getChatbotResponse(
                            userMessage = message,
                            conversationHistory = chatMessages.takeLast(5),
                            existingIngredients = suggestedIngredients
                        )
                        if (result.isSuccess) {
                            val response = result.getOrNull()!!
                            chatMessages = chatMessages + ChatMessage("assistant", response.message)
                            
                            // Update suggestions based on response
                            if (response.suggestedIngredients.isNotEmpty()) {
                                suggestedIngredients = response.suggestedIngredients
                            }
                            if (response.suggestedRecipes.isNotEmpty()) {
                                suggestedRecipes = response.suggestedRecipes
                            }
                            
                            // Auto-detect recipe in response
                            detectAndHandleRecipe(response.message)
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I'm having trouble responding right now. Please try again.")
                        }
                    }
                }
            } catch (e: Exception) {
                chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I encountered an error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Back button (if enabled)
            if (showBackButton) {
                FloatingActionButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Home/Welcome button (if enabled)
            if (showWelcomeButton) {
                FloatingActionButton(
                    onClick = onWelcomeClick,
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Main Chat button (always present and centered)
            FloatingActionButton(
                onClick = { showChatDialog.value = true },
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
    
    // Enhanced Chat Dialog
    if (showChatDialog.value) {
        Dialog(
            onDismissRequest = { showChatDialog.value = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Smart Assistant",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { showChatDialog.value = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    // Quick Action Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { handleEnhancedChat("Suggest ingredients for Italian cuisine", "ingredient_suggestions") },
                                label = { Text("Italian Ingredients") },
                                selected = false,
                                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                onClick = { handleEnhancedChat("Show me healthy recipes", "recipe_from_ingredients") },
                                label = { Text("Healthy Recipes") },
                                selected = false,
                                leadingIcon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                onClick = { handleEnhancedChat("Give me a quick pasta recipe") },
                                label = { Text("Quick Recipe") },
                                selected = false,
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                onClick = { handleEnhancedChat("Nutrition information", "nutrition_info") },
                                label = { Text("Nutrition Info") },
                                selected = false,
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                    
                    // Chat Messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Loading indicator
                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        modifier = Modifier.widthIn(max = 200.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Thinking...")
                                        }
                                    }
                                }
                            }
                        }
                        
                        items(chatMessages.reversed()) { message ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    modifier = Modifier.widthIn(max = 280.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (message.role == "user") 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = if (message.role == "user") 12.dp else 4.dp,
                                        topEnd = if (message.role == "user") 4.dp else 12.dp,
                                        bottomStart = 12.dp,
                                        bottomEnd = 12.dp
                                    )
                                ) {
                                    Text(
                                        text = message.content,
                                        modifier = Modifier.padding(12.dp),
                                        color = if (message.role == "user") 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Welcome message
                        if (chatMessages.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Smart Grocery Assistant",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Ask me about recipes, ingredients, nutrition, or cooking tips! I can help you create shopping lists and discover new recipes.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Input field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = userMessage,
                            onValueChange = { userMessage = it },
                            placeholder = { Text("Ask about recipes, ingredients, or nutrition...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    handleEnhancedChat(userMessage)
                                    keyboardController?.hide()
                                }
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        
                        FloatingActionButton(
                            onClick = {
                                handleEnhancedChat(userMessage)
                                keyboardController?.hide()
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Recipe Action Dialog
    if (showRecipeActionDialog && pendingRecipe != null) {
        Dialog(onDismissRequest = { showRecipeActionDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recipe Found!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showRecipeActionDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recipe preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                pendingRecipe!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Ingredients: ${pendingRecipe!!.ingredients.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                "Cooking Time: ${pendingRecipe!!.cookingTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Ingredients:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 120.dp)
                            ) {
                                items(pendingRecipe!!.ingredients.take(5)) { ingredient ->
                                    Text(
                                        "• $ingredient",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                                if (pendingRecipe!!.ingredients.size > 5) {
                                    item {
                                        Text(
                                            "... and ${pendingRecipe!!.ingredients.size - 5} more",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Place selection (if grocery view model is available)
                    if (groceryViewModel != null && availablePlaces.isNotEmpty()) {
                        Text(
                            "Add ingredients to which list?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp)
                        ) {
                            items(availablePlaces) { place ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPlaceId = place.id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedPlaceId == place.id) 
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedPlaceId == place.id,
                                            onClick = { selectedPlaceId = place.id }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            place.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (groceryViewModel != null && selectedPlaceId != null) {
                            Button(
                                onClick = {
                                    // Add ingredients to selected grocery list
                                    pendingRecipe!!.ingredients.forEach { ingredient ->
                                        groceryViewModel.onEvent(
                                            GroceryListUiEvent.OnAddItem(
                                                name = ingredient,
                                                quantity = 1
                                            )
                                        )
                                    }
                                    showRecipeActionDialog = false
                                    pendingRecipe = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add All Ingredients to Shopping List")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // TODO: Save recipe to recipe collection
                                // This would integrate with a recipe repository/database
                                showRecipeActionDialog = false
                                pendingRecipe = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Recipe for Later")
                        }
                        
                        TextButton(
                            onClick = {
                                showRecipeActionDialog = false
                                pendingRecipe = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Not Now")
                        }
                    }
                }
            }
        }
    }
}

/**
 * A standalone back button
 */
@Composable
fun BackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onBackClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go Back"
        )
    }
}

/**
 * A standalone welcome screen button
 */
@Composable
fun WelcomeButton(
    onWelcomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onWelcomeClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "Back to Welcome Screen"
        )
    }
}

/**
 * A standalone chatbot button
 */
@Composable
fun ChatBotButton(
    showChatDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { showChatDialog.value = true },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Open Recipe Chatbot"
        )
    }
} 