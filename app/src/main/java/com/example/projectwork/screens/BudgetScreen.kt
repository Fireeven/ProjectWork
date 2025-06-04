package com.example.projectwork.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.viewmodel.GroceryListViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

data class BudgetCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val budgetAmount: Double,
    val spentAmount: Double,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBackClick: () -> Unit,
    groceryViewModel: GroceryListViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var totalBudget by remember { mutableStateOf("") }
    var monthlyGoal by remember { mutableStateOf("2500.00") }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "content_animation"
    )
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // Collect grocery data
    val allGroceryItems by groceryViewModel.getAllGroceryItems().collectAsState(initial = emptyList())
    val purchasedItems = allGroceryItems.filter { it.isPurchased }
    val totalSpent = purchasedItems.sumOf { (it.actualPrice ?: it.price) * it.quantity }
    val totalPlanned = allGroceryItems.sumOf { it.price * it.quantity }
    val monthlyGoalAmount = monthlyGoal.toDoubleOrNull() ?: 2500.0
    
    // Currency formatter
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    // Sample budget categories
    val budgetCategories = remember {
        listOf(
            BudgetCategory(
                id = "groceries",
                name = "Groceries",
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFF4ADE80),
                budgetAmount = 800.0,
                spentAmount = totalSpent,
                description = "Food and household items"
            ),
            BudgetCategory(
                id = "dining",
                name = "Dining Out",
                icon = Icons.Default.Restaurant,
                color = Color(0xFF06B6D4),
                budgetAmount = 300.0,
                spentAmount = 145.50,
                description = "Restaurants and takeout"
            ),
            BudgetCategory(
                id = "transport",
                name = "Transportation",
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFF8B5CF6),
                budgetAmount = 400.0,
                spentAmount = 320.75,
                description = "Gas, public transport, parking"
            ),
            BudgetCategory(
                id = "entertainment",
                name = "Entertainment",
                icon = Icons.Default.MovieFilter,
                color = Color(0xFFF59E0B),
                budgetAmount = 200.0,
                spentAmount = 89.25,
                description = "Movies, games, subscriptions"
            ),
            BudgetCategory(
                id = "utilities",
                name = "Utilities",
                icon = Icons.Default.ElectricBolt,
                color = Color(0xFFEF4444),
                budgetAmount = 350.0,
                spentAmount = 287.90,
                description = "Electricity, water, internet"
            )
        )
    }
    
    val totalBudgetAmount = budgetCategories.sumOf { it.budgetAmount }
    val totalSpentAmount = budgetCategories.sumOf { it.spentAmount }
    val remainingBudget = totalBudgetAmount - totalSpentAmount
    val budgetProgress = if (totalBudgetAmount > 0) (totalSpentAmount / totalBudgetAmount).toFloat() else 0f
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        ) {
            // Enhanced Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Budget Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Take control of your spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = { showAddBudgetDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Budget",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Monthly Overview Card
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                    ) {
                        MonthlyOverviewCard(
                            monthlyGoal = monthlyGoalAmount,
                            totalSpent = totalSpentAmount,
                            totalBudget = totalBudgetAmount,
                            currencyFormatter = currencyFormatter,
                            onEditGoal = { showEditDialog = true }
                        )
                    }
                }
                
                // Quick Stats Row
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(
                            animationSpec = tween(durationMillis = 300, delayMillis = 100)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickStatCard(
                                title = "Remaining",
                                value = currencyFormatter.format(remainingBudget.coerceAtLeast(0.0)),
                                icon = Icons.Default.Savings,
                                color = if (remainingBudget >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            QuickStatCard(
                                title = "Progress",
                                value = "${(budgetProgress * 100).toInt()}%",
                                icon = Icons.Default.TrendingUp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Budget Categories Header
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 300, delayMillis = 200)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Budget Categories",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${budgetCategories.size} categories",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Budget Categories
                items(budgetCategories) { category ->
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(
                            animationSpec = tween(durationMillis = 300, delayMillis = 300)
                        )
                    ) {
                        BudgetCategoryCard(
                            category = category,
                            currencyFormatter = currencyFormatter,
                            onClick = {
                                selectedCategory = category
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Budget Dialog
    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { name, amount ->
                // Handle adding new budget category
                showAddBudgetDialog = false
            }
        )
    }
    
    // Edit Goal Dialog
    if (showEditDialog) {
        EditBudgetDialog(
            category = selectedCategory,
            currentGoal = monthlyGoal,
            onDismiss = { 
                showEditDialog = false
                selectedCategory = null
            },
            onConfirm = { newGoal ->
                monthlyGoal = newGoal
                showEditDialog = false
                selectedCategory = null
            }
        )
    }
}

@Composable
private fun MonthlyOverviewCard(
    monthlyGoal: Double,
    totalSpent: Double,
    totalBudget: Double,
    currencyFormatter: NumberFormat,
    onEditGoal: () -> Unit
) {
    val progress = if (monthlyGoal > 0) (totalSpent / monthlyGoal).toFloat().coerceAtMost(1f) else 0f
    val progressColor = when {
        progress < 0.7f -> Color(0xFF10B981)
        progress < 0.9f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Monthly Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currencyFormatter.format(monthlyGoal),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onEditGoal,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Goal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${currencyFormatter.format(totalSpent)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (totalSpent <= monthlyGoal) {
                            "Remaining: ${currencyFormatter.format(monthlyGoal - totalSpent)}"
                        } else {
                            "Over budget by: ${currencyFormatter.format(totalSpent - monthlyGoal)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = progressColor
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BudgetCategoryCard(
    category: BudgetCategory,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val progress = if (category.budgetAmount > 0) {
        (category.spentAmount / category.budgetAmount).toFloat().coerceAtMost(1f)
    } else 0f
    
    val progressColor = when {
        progress < 0.7f -> Color(0xFF10B981)
        progress < 0.9f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = category.color
                        )
                    }
                    
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(category.spentAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = "of ${currencyFormatter.format(category.budgetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% used",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currencyFormatter.format((category.budgetAmount - category.spentAmount).coerceAtLeast(0.0)) + " left",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.spentAmount <= category.budgetAmount) progressColor else Color(0xFFEF4444)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Budget Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it },
                    label = { Text("Budget Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetAmount.toDoubleOrNull()
                    if (categoryName.isNotBlank() && amount != null && amount > 0) {
                        onConfirm(categoryName, amount)
                    }
                },
                enabled = categoryName.isNotBlank() && budgetAmount.toDoubleOrNull() != null
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBudgetDialog(
    category: BudgetCategory?,
    currentGoal: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var goalAmount by remember(currentGoal) { mutableStateOf(currentGoal) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (category != null) "Edit ${category.name}" else "Edit Monthly Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = goalAmount,
                onValueChange = { goalAmount = it },
                label = { Text(if (category != null) "Budget Amount" else "Monthly Goal") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (goalAmount.toDoubleOrNull() != null) {
                        onConfirm(goalAmount)
                    }
                },
                enabled = goalAmount.toDoubleOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 