package com.example.projectwork.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.data.PlaceEntity
import java.text.NumberFormat
import java.util.*

/**
 * A reusable composable that displays a single grocery item in a card format.
 * Features include:
 * - Checkbox for marking items as complete
 * - Item name display with purchase status
 * - Price input and display
 * - Quantity display and controls (optional)
 * - Purchase tracking with actual price
 * - Move to different place functionality
 * - Delete functionality
 * 
 * The component has two modes:
 * 1. View mode (showQuantityControls = false): Shows just the quantity value
 * 2. Edit mode (showQuantityControls = true): Shows +/- buttons for quantity adjustment
 *
 * @param item The grocery item to display
 * @param onCheckedChange Callback when the checkbox state changes
 * @param onDelete Callback when the delete button is clicked
 * @param onQuantityChange Callback when the quantity is changed
 * @param onPriceChange Callback when the price is changed
 * @param onPurchaseToggle Callback when item is marked as purchased
 * @param onMoveToPlace Callback when item is moved to another place
 * @param availablePlaces List of places where item can be moved
 * @param showQuantityControls Whether to show quantity adjustment controls
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit = {},
    onPurchaseToggle: (Boolean, Double?) -> Unit = { _, _ -> },
    onMoveToPlace: (Int) -> Unit = {},
    availablePlaces: List<PlaceEntity> = emptyList(),
    showQuantityControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showPriceDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Main row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Checkbox and Item Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = onCheckedChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                                fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.Medium
                            ),
                            color = if (item.isPurchased) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Enhanced price display with quantity calculations
                        if (item.price > 0 || item.actualPrice != null) {
                            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                            val unitPrice = if (item.isPurchased && item.actualPrice != null) {
                                item.actualPrice!!
                            } else {
                                item.price
                            }
                            val totalAmount = unitPrice * item.quantity
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Unit price
                                Text(
                                    text = if (item.isPurchased && item.actualPrice != null) {
                                        "Paid: ${currencyFormatter.format(unitPrice)}"
                                    } else {
                                        "Est: ${currencyFormatter.format(unitPrice)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.isPurchased) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // Show total if quantity > 1
                                if (item.quantity > 1) {
                                    Surface(
                                        color = if (item.isPurchased)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Total: ${currencyFormatter.format(totalAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (item.isPurchased)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right side: Quantity Controls and Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (showQuantityControls) {
                        // Decrease quantity button
                        FilledIconButton(
                            onClick = { 
                                if (item.quantity > 1) {
                                    onQuantityChange(item.quantity - 1)
                                }
                            },
                            enabled = item.quantity > 1,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // Quantity display
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.widthIn(min = 24.dp)
                    )
                    
                    if (showQuantityControls) {
                        // Increase quantity button
                        FilledIconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Purchase toggle button
                    IconButton(
                        onClick = { 
                            if (!item.isPurchased) {
                                showPriceDialog = true
                            } else {
                                onPurchaseToggle(false, null)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isPurchased) Icons.Default.ShoppingCartCheckout else Icons.Default.ShoppingCart,
                            contentDescription = if (item.isPurchased) "Mark as not purchased" else "Mark as purchased",
                            tint = if (item.isPurchased) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Expanded content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Set Price button
                        OutlinedButton(
                            onClick = { showPriceDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Price", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        // Move to Place button
                        if (availablePlaces.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showMoveDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    Icons.Default.MoveToInbox,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Move", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    // Recipe info if available
                    if (!item.recipeTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "From: ${item.recipeTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Price input dialog
    if (showPriceDialog) {
        PriceInputDialog(
            currentPrice = if (item.isPurchased) item.actualPrice ?: item.price else item.price,
            isPurchasing = !item.isPurchased,
            onDismiss = { showPriceDialog = false },
            onConfirm = { price, isPurchasing ->
                if (isPurchasing) {
                    onPurchaseToggle(true, price)
                } else {
                    onPriceChange(price)
                }
                showPriceDialog = false
            }
        )
    }
    
    // Move to place dialog
    if (showMoveDialog) {
        MoveToPlaceDialog(
            availablePlaces = availablePlaces,
            currentPlaceId = item.placeId,
            onDismiss = { showMoveDialog = false },
            onConfirm = { placeId ->
                onMoveToPlace(placeId)
                showMoveDialog = false
            }
        )
    }
}

@Composable
private fun PriceInputDialog(
    currentPrice: Double,
    isPurchasing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean) -> Unit
) {
    var priceText by remember { mutableStateOf(if (currentPrice > 0) currentPrice.toString() else "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isPurchasing) "Enter Purchase Price" else "Set Estimated Price",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        // Allow only valid decimal numbers
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            priceText = newValue
                        }
                    },
                    label = { Text("Price") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isPurchasing) {
                    Text(
                        text = "This will mark the item as purchased",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            onConfirm(price, isPurchasing)
                        },
                        enabled = priceText.isNotBlank()
                    ) {
                        Text(if (isPurchasing) "Purchase" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveToPlaceDialog(
    availablePlaces: List<PlaceEntity>,
    currentPlaceId: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val filteredPlaces = availablePlaces.filter { it.id != currentPlaceId }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Move to Place",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (filteredPlaces.isEmpty()) {
                    Text(
                        text = "No other places available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredPlaces.forEach { place ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onConfirm(place.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
} 