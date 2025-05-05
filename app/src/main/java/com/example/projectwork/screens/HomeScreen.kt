package com.example.projectwork.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.data.PlaceWithItemCount
import com.example.projectwork.viewmodel.HomeViewModel
import com.example.projectwork.ui.components.NavigationButtons
import kotlin.math.sin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlaceClick: (Int) -> Unit,
    onAddPlaceClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val places by viewModel.places.collectAsState(initial = emptyList())
    // var selectedPlace by remember { mutableStateOf<PlaceWithItemCount?>(null) }
    
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

    // Continuous rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "My Places",
                        modifier = Modifier.graphicsLayer {
                            rotationX = sin(rotation * Math.PI / 180).toFloat() * 10f
                        }
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlaceClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .scale(1f + (sin(rotation * Math.PI / 180) * 0.1f).toFloat())
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Place")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = padding,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(places, key = { it.id }) { place ->
                    PlaceCard(
                        place = place,
                        onClick = { onPlaceClick(place.id) },
                        rotation = rotation
                    )
                }
            }
            
            // Add the navigation buttons - no back button needed on home screen
            NavigationButtons(
                onBackClick = { /* Not used on home screen */ },
                showChatDialog = chatDialogState,
                showBackButton = false,
                showWelcomeButton = false,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
    
    // Chat dialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceCard(
    place: PlaceWithItemCount,
    onClick: () -> Unit,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    // var isHovered by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = sin(rotation * Math.PI / 180).toFloat() * 5f
                rotationX = sin(rotation * Math.PI / 180 + place.id + 90).toFloat() * 5f
                cameraDistance = 12f * density
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 10.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Category icon with rotation
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .rotate(rotation + place.id)
                    .graphicsLayer {
                        alpha = 0.6f
                    },
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Place name and details
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationY = sin(rotation * Math.PI / 180 + place.id).toFloat() * 8f
                    }
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.category ?: "Uncategorized",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                if (!place.address.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

            // Items count badge
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        rotationZ = sin(rotation * Math.PI / 180 + place.id).toFloat() * 15f
                    }
            ) {
                Text(
                    text = "${place.itemCount} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
} 