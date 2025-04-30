package com.example.projectwork.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.data.PlaceEntity
import com.example.projectwork.viewmodel.PlaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onGroceryListClick: (Int) -> Unit,
    viewModel: PlaceViewModel = viewModel()
) {
    val places by viewModel.places.collectAsState()
    val place = remember(places, placeId) {
        places.find { it.id == placeId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place?.name ?: "Place Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { place?.let { onGroceryListClick(it.id) } }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Grocery List")
                    }
                    IconButton(onClick = { place?.let { onEditClick(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        if (place != null) {
            PlaceDetails(
                place = place,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Place not found")
            }
        }
    }
}

@Composable
fun PlaceDetails(
    place: PlaceEntity,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailCard(
                icon = Icons.Default.Category,
                title = "Category",
                value = place.category.name
            )
            
            DetailCard(
                icon = Icons.Default.LocationOn,
                title = "Address",
                value = place.address
            )
        }
    }
}

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