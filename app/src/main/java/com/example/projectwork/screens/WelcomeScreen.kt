package com.example.projectwork.screens

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.projectwork.utils.OpenAIHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.PaddingValues

// Data class for the chatbot
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    // Chatbot state
    var showChatDialog by remember { mutableStateOf(false) }
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isSearching by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Button animation
    val buttonScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    // Get fixed colors from MaterialTheme only once at composition time
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Add API connection status
    var apiConnectionStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isApiCheckInProgress by remember { mutableStateOf(false) }
    
    // Check API connection when the chat dialog first opens
    LaunchedEffect(showChatDialog) {
        if (showChatDialog && apiConnectionStatus == null && !isApiCheckInProgress) {
            isApiCheckInProgress = true
            apiConnectionStatus = try {
                OpenAIHelper.testAPIConnection()
            } catch (e: Exception) {
                Pair(false, "Error testing API connection: ${e.message ?: "Unknown error"}")
            } finally {
                isApiCheckInProgress = false
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
        delay(1000)
        showButton = true
    }

    // Function to handle chatbot interaction with OpenAI API
    fun handleChatbotQuery(query: String) {
        if (query.isBlank()) return
        
        // Add user message to chat
        chatMessages = chatMessages + ChatMessage(query, true)
        userMessage = ""
        isSearching = true
        
        coroutineScope.launch {
            try {
                // Call OpenAI API through our helper with better error handling
                val openAIResponse = try {
                    OpenAIHelper.getRecipeInfo(query)
                } catch (e: Exception) {
                    Log.e("WelcomeScreen", "Error calling OpenAI API: ${e.message}", e)
                    null
                }
                
                if (openAIResponse == null || openAIResponse.isFailure) {
                    // Handle null or failed response
                    chatMessages = chatMessages + ChatMessage(
                        "I'm having trouble connecting to the recipe service. Using simulated response instead.", 
                        false
                    )
                    
                    // Create fake response for demonstration
                    val simulatedResponse = """
                        I couldn't reach the API, but here's a simple recipe:
                        
                        ## Ingredients
                        - Whatever you have in your fridge
                        - Imagination
                        - Cooking skills
                    """.trimIndent()
                    
                    chatMessages = chatMessages + ChatMessage(simulatedResponse, false)
                } else {
                    // Process the response
                    val botResponse = openAIResponse.getOrNull() ?: "No response received"
                    
                    // Add the bot response
                    chatMessages = chatMessages + ChatMessage(botResponse, false)
                    
                    // Extract ingredients if possible and offer to create grocery list
                    val ingredients = OpenAIHelper.extractIngredients(botResponse)
                    if (ingredients != null && ingredients.isNotEmpty()) {
                        // Add option to create grocery list
                        chatMessages = chatMessages + ChatMessage(
                            "Would you like me to create a grocery list with these ingredients?", false
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions
                chatMessages = chatMessages + ChatMessage(
                    "Error: ${e.message ?: "Unknown error"}", 
                    false
                )
                
                // Add a more detailed error message
                chatMessages = chatMessages + ChatMessage(
                    "Please check your internet connection and make sure the API key is correctly configured.", 
                    false
                )
            } finally {
                isSearching = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon/logo
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = yOffset.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App title
            Text(
                text = "GROCERY LIST",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your smart shopping assistant",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // PROMINENTLY DISPLAYED GET STARTED BUTTON
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "GET STARTED",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Get Started",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
    
    // Chatbot floating button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tertiaryColor,
                            tertiaryColor.copy(alpha = 0.9f)
                        )
                    )
                )
                .border(2.dp, tertiaryColor.copy(alpha = 0.5f), CircleShape)
                .clickable { showChatDialog = true }
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Recipe Assistant",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
    
    // Add a test button in the bottom corner
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomStart
    ) {
        Button(
            onClick = {
                val intent = Intent(context, Class.forName("com.example.projectwork.ApiTestActivity"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Text("API", style = MaterialTheme.typography.labelSmall)
        }
    }
    
    // Chatbot dialog
    if (showChatDialog) {
        Dialog(
            onDismissRequest = { showChatDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Dialog header
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
                                imageVector = Icons.Filled.Assistant,
                                contentDescription = null,
                                tint = primaryColor
                            )
                            Column {
                                Text(
                                    "Recipe Assistant",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                // Display API connection status
                                apiConnectionStatus?.let { (isConnected, message) ->
                                    Text(
                                        text = if (isConnected) "API Connected" else "Using Simulated Responses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isConnected) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { showChatDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // Chat messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        // Loading indicator
                        if (isSearching) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 320.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = 16.dp,
                                                    bottomEnd = 16.dp
                                                )
                                            )
                                            .background(primaryColor.copy(alpha = 0.1f))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Searching", color = primaryColor)
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Messages
                        items(chatMessages.reversed()) { message ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 320.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = if (message.isFromUser) 16.dp else 4.dp,
                                                topEnd = if (message.isFromUser) 4.dp else 16.dp,
                                                bottomStart = 16.dp,
                                                bottomEnd = 16.dp
                                            )
                                        )
                                        .background(
                                            if (message.isFromUser) tertiaryColor.copy(alpha = 0.8f)
                                            else primaryColor.copy(alpha = 0.1f)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = message.content,
                                        color = if (message.isFromUser) Color.White else Color.Black,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Welcome message
                        if (chatMessages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            "👨‍🍳 Recipe Assistant",
                                            style = MaterialTheme.typography.headlineSmall,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "Ask me about recipes and I'll create a grocery list with all the ingredients you need.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "Try: \"What ingredients do I need for pasta carbonara?\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = tertiaryColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = userMessage,
                            onValueChange = { userMessage = it },
                            placeholder = { Text("Ask about a recipe...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = surfaceColor,
                                focusedContainerColor = surfaceColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    try {
                                        handleChatbotQuery(userMessage)
                                    } catch (e: Exception) {
                                        Log.e("WelcomeScreen", "Error handling chat query: ${e.message}", e)
                                    }
                                    keyboardController?.hide()
                                }
                            ),
                            singleLine = true
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                                .clickable {
                                    try {
                                        handleChatbotQuery(userMessage)
                                    } catch (e: Exception) {
                                        Log.e("WelcomeScreen", "Error handling chat query: ${e.message}", e)
                                    }
                                    keyboardController?.hide()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
} 