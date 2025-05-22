package com.example.projectwork.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectwork.data.GroceryItem

@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    showQuantityControls: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = onCheckedChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (showQuantityControls) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { 
                                    if (item.quantity > 1) {
                                        onQuantityChange(item.quantity - 1)
                                    }
                                }
                            ) {
                                Text("-")
                            }
                            Text(
                                text = item.quantity.toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { onQuantityChange(item.quantity + 1) }
                            ) {
                                Text("+")
                            }
                        }
                    } else {
                        Text(
                            text = "Quantity: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 