package com.example.projectwork.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectwork.data.GroceryItem

/**
 * A reusable composable that displays a single grocery item in a card format.
 * Features include:
 * - Checkbox for marking items as complete
 * - Item name display
 * - Quantity display and controls (optional)
 * - Delete functionality
 * 
 * The component has two modes:
 * 1. View mode (showQuantityControls = false): Shows just the quantity value
 * 2. Edit mode (showQuantityControls = true): Shows +/- buttons for quantity adjustment
 *
 * The layout adapts to different screen sizes and maintains proper spacing
 * between elements. All interactive elements have proper accessibility labels.
 *
 * @param item The grocery item to display
 * @param onCheckedChange Callback when the checkbox state changes
 * @param onDelete Callback when the delete button is clicked
 * @param onQuantityChange Callback when the quantity is changed (only in edit mode)
 * @param showQuantityControls Whether to show quantity adjustment controls
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    showQuantityControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Right side: Quantity Controls and Delete Button
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
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity"
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
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 