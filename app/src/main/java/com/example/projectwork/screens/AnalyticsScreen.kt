package com.example.projectwork.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwork.data.GroceryItem
import com.example.projectwork.ui.components.NavigationButtons
import com.example.projectwork.viewmodel.GroceryListViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import java.text.NumberFormat
import java.util.*

// Enhanced data classes for analytics
data class SpendingInsight(
    val title: String,
    val description: String,
    val value: String,
    val trend: String, // "up", "down", "stable"
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val itemCount: Int
)

data class MonthlySpending(
    val month: String,
    val amount: Double,
    val itemCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    groceryViewModel: GroceryListViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by groceryViewModel.uiState.collectAsState()
    val showChatDialog = remember { mutableStateOf(false) }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf("This Month") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var showDetailedView by remember { mutableStateOf(false) }
    
    // Launch animation
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }
    
    // Calculate analytics data from available grocery items
    val groceryItems = uiState.items
    val purchasedItems = groceryItems.filter { it.isPurchased }
    val totalSpent = purchasedItems.sumOf { it.actualPrice ?: it.price }
    val totalItems = purchasedItems.size
    val averageItemPrice = if (totalItems > 0) totalSpent / totalItems else 0.0
    
    // Generate insights
    val insights = generateSpendingInsights(purchasedItems, totalSpent, totalItems)
    
    // Category spending data (mock since we don't have categories in GroceryItem)
    val categorySpending = calculateCategorySpending(purchasedItems)
    
    // Monthly spending data
    val monthlySpending = calculateMonthlySpending(purchasedItems)
    
    // Currency formatter
    val currencyFormatter = remember(selectedCurrency) {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance(selectedCurrency)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with enhanced design
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
                                text = "Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Smart spending insights",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Currency selector
                        FilterChip(
                            onClick = { 
                                selectedCurrency = when(selectedCurrency) {
                                    "USD" -> "EUR"
                                    "EUR" -> "GBP"
                                    "GBP" -> "CAD"
                                    else -> "USD"
                                }
                            },
                            label = { Text(selectedCurrency) },
                            selected = true,
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.AttachMoney, 
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Time range selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("This Week", "This Month", "Last 3 Months", "This Year")) { range ->
                            FilterChip(
                                onClick = { selectedTimeRange = range },
                                label = { Text(range) },
                                selected = selectedTimeRange == range,
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
                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Total Spent",
                                value = currencyFormatter.format(totalSpent),
                                icon = Icons.Default.AttachMoney,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Items Bought",
                                value = totalItems.toString(),
                                icon = Icons.Default.ShoppingCart,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Total Items",
                                value = groceryItems.size.toString(),
                                icon = Icons.Default.Store,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Avg. Item Price",
                                value = currencyFormatter.format(averageItemPrice),
                                icon = Icons.Default.TrendingUp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Smart Insights
                    if (insights.isNotEmpty()) {
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Smart Insights",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    insights.forEach { insight ->
                                        InsightItem(insight = insight)
                                        if (insight != insights.last()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Enhanced Pie Chart
                    if (categorySpending.isNotEmpty()) {
                        item {
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
                                        Text(
                                            "Spending by Category",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        
                                        IconButton(
                                            onClick = { showDetailedView = !showDetailedView }
                                        ) {
                                            Icon(
                                                if (showDetailedView) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "Toggle detailed view"
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Enhanced Pie Chart
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EnhancedPieChart(
                                            data = categorySpending,
                                            modifier = Modifier.size(200.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Category Legend
                                    categorySpending.forEach { category ->
                                        CategoryLegendItem(
                                            category = category,
                                            currencyFormatter = currencyFormatter
                                        )
                                    }
                                    
                                    // Detailed breakdown
                                    AnimatedVisibility(visible = showDetailedView) {
                                        Column {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Text(
                                                "Detailed Breakdown",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            categorySpending.forEach { category ->
                                                DetailedCategoryItem(
                                                    category = category,
                                                    currencyFormatter = currencyFormatter
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Monthly Trend Chart
                    if (monthlySpending.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        "Monthly Spending Trend",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    MonthlyTrendChart(
                                        data = monthlySpending,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Item Performance
                    item {
                        ItemPerformanceCard(
                            groceryItems = purchasedItems,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
        
        // Navigation buttons
        NavigationButtons(
            onBackClick = onBackClick,
            showChatDialog = showChatDialog,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightItem(insight: SpendingInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = insight.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = insight.value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = when (insight.trend) {
                "up" -> MaterialTheme.colorScheme.error
                "down" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun EnhancedPieChart(
    data: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        var startAngle = -90f
        
        data.forEach { category ->
            val sweepAngle = category.percentage * 360f
            
            drawArc(
                color = category.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
        
        // Draw center circle for donut effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.4f,
            center = center
        )
    }
}

@Composable
private fun CategoryLegendItem(
    category: CategorySpending,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(category.color)
        )
        
        Text(
            text = category.category,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${(category.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = currencyFormatter.format(category.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DetailedCategoryItem(
    category: CategorySpending,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${category.itemCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(category.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
                Text(
                    text = "${(category.percentage * 100).toInt()}% of total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthlyTrendChart(
    data: List<MonthlySpending>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxAmount = data.maxOfOrNull { it.amount } ?: 0.0
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val stepY = size.height / maxAmount.coerceAtLeast(1.0)
        
        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.3f)
        for (i in 0..4) {
            val y = size.height * i / 4
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw line chart
        val points = data.mapIndexed { index, spending ->
            Offset(
                x = index * stepX,
                y = size.height - (spending.amount * stepY).toFloat()
            )
        }
        
        // Draw line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Blue,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx()
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = Color.Blue,
                radius = 6.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun ItemPerformanceCard(
    groceryItems: List<GroceryItem>,
    currencyFormatter: NumberFormat
) {
    val itemData = groceryItems
        .groupBy { it.name }
        .map { (name, items) ->
            Triple(name, items.sumOf { it.actualPrice ?: it.price }, items.size)
        }
        .sortedByDescending { it.second }
        .take(5) // Show top 5 items
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Top Items by Spending",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (itemData.isNotEmpty()) {
                itemData.forEach { (name, amount, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$count purchases",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = currencyFormatter.format(amount),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (name != itemData.last().first) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            } else {
                Text(
                    "No purchase data available yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Helper functions
private fun generateSpendingInsights(
    items: List<GroceryItem>,
    totalSpent: Double,
    totalItems: Int
): List<SpendingInsight> {
    val insights = mutableListOf<SpendingInsight>()
    
    if (totalItems > 0) {
        val avgPrice = totalSpent / totalItems
        insights.add(
            SpendingInsight(
                title = "Average Item Cost",
                description = "Your typical grocery item costs",
                value = "$${String.format("%.2f", avgPrice)}",
                trend = "stable",
                icon = Icons.Default.TrendingUp
            )
        )
    }
    
    val mostExpensiveItem = items.maxByOrNull { it.actualPrice ?: it.price }
    if (mostExpensiveItem != null) {
        insights.add(
            SpendingInsight(
                title = "Most Expensive Item",
                description = "Your priciest purchase was ${mostExpensiveItem.name}",
                value = "$${String.format("%.2f", mostExpensiveItem.actualPrice ?: mostExpensiveItem.price)}",
                trend = "up",
                icon = Icons.Default.TrendingUp
            )
        )
    }
    
    return insights
}

private fun calculateCategorySpending(items: List<GroceryItem>): List<CategorySpending> {
    // Since GroceryItem doesn't have categories, we'll create mock categories based on item names
    val categoryTotals = items.groupBy { categorizeItem(it.name) }
        .map { (category, categoryItems) ->
            val amount = categoryItems.sumOf { it.actualPrice ?: it.price }
            val itemCount = categoryItems.size
            Triple(category, amount, itemCount)
        }
    
    val totalAmount = categoryTotals.sumOf { it.second }
    
    val colors = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Yellow
        Color(0xFFEF4444), // Red
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFF97316), // Orange
        Color(0xFFEC4899)  // Pink
    )
    
    return categoryTotals.mapIndexed { index, (category, amount, itemCount) ->
        CategorySpending(
            category = category,
            amount = amount,
            percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f,
            color = colors[index % colors.size],
            itemCount = itemCount
        )
    }.sortedByDescending { it.amount }
}

private fun categorizeItem(itemName: String): String {
    val name = itemName.lowercase()
    return when {
        name.contains("milk") || name.contains("cheese") || name.contains("yogurt") -> "Dairy"
        name.contains("apple") || name.contains("banana") || name.contains("orange") -> "Fruits"
        name.contains("bread") || name.contains("pasta") || name.contains("rice") -> "Grains"
        name.contains("chicken") || name.contains("beef") || name.contains("fish") -> "Meat"
        name.contains("carrot") || name.contains("lettuce") || name.contains("tomato") -> "Vegetables"
        else -> "Other"
    }
}

private fun calculateMonthlySpending(items: List<GroceryItem>): List<MonthlySpending> {
    // For now, return mock data since we don't have date tracking
    return listOf(
        MonthlySpending("Jan", 245.50, 23),
        MonthlySpending("Feb", 312.75, 28),
        MonthlySpending("Mar", 189.25, 19),
        MonthlySpending("Apr", 267.80, 25),
        MonthlySpending("May", 298.45, 31)
    )
} 