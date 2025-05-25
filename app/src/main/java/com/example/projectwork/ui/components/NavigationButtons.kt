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
import kotlinx.coroutines.launch

/**
 * A navigation bar with multiple buttons in a row
 * 
 * @param onBackClick Callback for when the back button is clicked
 * @param onWelcomeClick Callback for navigating to welcome screen
 * @param showChatDialog MutableState to control the visibility of the chat dialog
 * @param modifier Optional modifier for customizing the layout
 * @param showBackButton Whether to show the back button (default: true)
 * @param showWelcomeButton Whether to show the welcome button (default: false)
 * @param showChatButton Whether to show the chatbot button (default: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationButtons(
    onBackClick: () -> Unit,
    onWelcomeClick: () -> Unit = {},
    showChatDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    showWelcomeButton: Boolean = false,
    showChatButton: Boolean = true
) {
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isLoading by remember { mutableStateOf(false) }
    var suggestedIngredients by remember { mutableStateOf(listOf<String>()) }
    var suggestedRecipes by remember { mutableStateOf(listOf<SimpleRecipe>()) }
    var selectedCuisine by remember { mutableStateOf("") }
    var selectedDietaryRestriction by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    
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
                            dietaryRestrictions = selectedDietaryRestriction,
                            mealType = "dinner"
                        )
                        if (result.isSuccess) {
                            val ingredients = result.getOrNull() ?: emptyList()
                            suggestedIngredients = ingredients
                            chatMessages = chatMessages + ChatMessage(
                                "assistant", 
                                "Here are some essential ingredients for ${if (selectedCuisine.isNotEmpty()) "$selectedCuisine cuisine" else "cooking"}:\n\n${ingredients.joinToString("\n") { "• $it" }}\n\nWould you like me to suggest recipes using these ingredients?"
                            )
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't get ingredient suggestions right now.")
                        }
                    }
                    "recipe_from_ingredients" -> {
                        val result = OpenAIHelper.getRecipeFromIngredients(suggestedIngredients)
                        if (result.isSuccess) {
                            val recipes = result.getOrNull() ?: emptyList()
                            suggestedRecipes = recipes
                            val recipeText = recipes.joinToString("\n\n") { recipe ->
                                "**${recipe.name}**\nIngredients: ${recipe.ingredients.joinToString(", ")}"
                            }
                            chatMessages = chatMessages + ChatMessage(
                                "assistant",
                                "Here are some recipes you can make:\n\n$recipeText\n\nWould you like me to add any of these to your grocery list?"
                            )
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't generate recipes right now.")
                        }
                    }
                    "nutrition_info" -> {
                        val result = OpenAIHelper.getNutritionalInfo(suggestedIngredients.take(5))
                        if (result.isSuccess) {
                            val nutritionInfo = result.getOrNull() ?: "No nutrition information available."
                            chatMessages = chatMessages + ChatMessage("assistant", nutritionInfo)
                        } else {
                            chatMessages = chatMessages + ChatMessage("assistant", "Sorry, I couldn't get nutrition information right now.")
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
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = if (showBackButton || showWelcomeButton) {
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            } else {
                modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side button(s)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    FloatingActionButton(
                        onClick = onBackClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
                
                if (showWelcomeButton) {
                    FloatingActionButton(
                        onClick = onWelcomeClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Back to Welcome Screen"
                        )
                    }
                }
            }
            
            // Right side - chat button
            if (showChatButton) {
                FloatingActionButton(
                    onClick = { showChatDialog.value = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Open Recipe Chatbot"
                    )
                }
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
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text("Thinking...")
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Messages
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
                                            "I can help you with:\n• Recipe suggestions\n• Ingredient recommendations\n• Nutrition information\n• Smart shopping lists",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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