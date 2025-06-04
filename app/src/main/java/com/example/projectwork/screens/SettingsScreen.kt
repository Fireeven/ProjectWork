package com.example.projectwork.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.util.*

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

sealed class SettingsItem {
    data class Switch(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val isChecked: Boolean,
        val onToggle: (Boolean) -> Unit
    ) : SettingsItem()
    
    data class Action(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val action: () -> Unit,
        val hasChevron: Boolean = true
    ) : SettingsItem()
    
    data class Selection(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val currentValue: String,
        val options: List<String>,
        val onSelect: (String) -> Unit
    ) : SettingsItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // Settings state with persistence simulation
    var isDarkMode by remember { mutableStateOf(false) }
    var currencyCode by remember { mutableStateOf("USD") }
    var language by remember { mutableStateOf("English") }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) }
    var priceAlerts by remember { mutableStateOf(false) }
    var biometricAuth by remember { mutableStateOf(false) }
    var autoBackup by remember { mutableStateOf(true) }
    var cloudSync by remember { mutableStateOf(false) }
    var defaultSorting by remember { mutableStateOf("Name (A-Z)") }
    var compactView by remember { mutableStateOf(false) }
    var showPrices by remember { mutableStateOf(true) }
    
    // Dialog states
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDataDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showSortingDialog by remember { mutableStateOf(false) }
    var showExportConfirm by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // Define settings sections
    val settingsSections = listOf(
        SettingsSection(
            title = "Account & Profile",
            items = listOf(
                SettingsItem.Action(
                    title = "Profile Settings",
                    description = "Manage your account information",
                    icon = Icons.Default.Person,
                    action = { /* Navigate to profile */ }
                ),
                SettingsItem.Action(
                    title = "Privacy Settings",
                    description = "Control your data privacy",
                    icon = Icons.Default.Security,
                    action = { /* Navigate to privacy */ }
                )
            )
        ),
        SettingsSection(
            title = "Display & Interface",
            items = listOf(
                SettingsItem.Switch(
                    title = "Dark Mode",
                    description = "Use dark theme throughout the app",
                    icon = Icons.Default.DarkMode,
                    isChecked = isDarkMode,
                    onToggle = { isDarkMode = it }
                ),
                SettingsItem.Selection(
                    title = "Language",
                    description = "Choose your preferred language",
                    icon = Icons.Default.Language,
                    currentValue = language,
                    options = listOf("English", "Spanish", "French", "German", "Italian"),
                    onSelect = { 
                        language = it
                        showLanguageDialog = false 
                    }
                ),
                SettingsItem.Selection(
                    title = "Currency",
                    description = "Set default currency for prices",
                    icon = Icons.Default.AttachMoney,
                    currentValue = currencyCode,
                    options = listOf("USD", "EUR", "GBP", "CAD", "AUD", "JPY"),
                    onSelect = { 
                        currencyCode = it
                        showCurrencyDialog = false 
                    }
                ),
                SettingsItem.Switch(
                    title = "Compact View",
                    description = "Show more items on screen",
                    icon = Icons.Default.ViewList,
                    isChecked = compactView,
                    onToggle = { compactView = it }
                )
            )
        ),
        SettingsSection(
            title = "Shopping & Lists",
            items = listOf(
                SettingsItem.Selection(
                    title = "Default Sorting",
                    description = "How items are sorted by default",
                    icon = Icons.Default.Sort,
                    currentValue = defaultSorting,
                    options = listOf("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)", "Category", "Recently Added"),
                    onSelect = { 
                        defaultSorting = it
                        showSortingDialog = false 
                    }
                ),
                SettingsItem.Switch(
                    title = "Show Prices",
                    description = "Display price information in lists",
                    icon = Icons.Default.PriceCheck,
                    isChecked = showPrices,
                    onToggle = { showPrices = it }
                ),
                SettingsItem.Switch(
                    title = "Price Alerts",
                    description = "Get notified about price changes",
                    icon = Icons.Default.NotificationsActive,
                    isChecked = priceAlerts,
                    onToggle = { priceAlerts = it }
                )
            )
        ),
        SettingsSection(
            title = "Notifications",
            items = listOf(
                SettingsItem.Switch(
                    title = "Push Notifications",
                    description = "Receive notifications from the app",
                    icon = Icons.Default.Notifications,
                    isChecked = isNotificationsEnabled,
                    onToggle = { isNotificationsEnabled = it }
                ),
                SettingsItem.Switch(
                    title = "Budget Alerts",
                    description = "Get alerts when approaching budget limits",
                    icon = Icons.Default.MonetizationOn,
                    isChecked = budgetAlerts,
                    onToggle = { budgetAlerts = it }
                )
            )
        ),
        SettingsSection(
            title = "Security",
            items = listOf(
                SettingsItem.Switch(
                    title = "Biometric Authentication",
                    description = "Use fingerprint or face unlock",
                    icon = Icons.Default.Fingerprint,
                    isChecked = biometricAuth,
                    onToggle = { biometricAuth = it }
                ),
                SettingsItem.Action(
                    title = "Change PIN",
                    description = "Update your security PIN",
                    icon = Icons.Default.Lock,
                    action = { /* Navigate to PIN change */ }
                )
            )
        ),
        SettingsSection(
            title = "Data & Backup",
            items = listOf(
                SettingsItem.Switch(
                    title = "Auto Backup",
                    description = "Automatically backup your data",
                    icon = Icons.Default.Backup,
                    isChecked = autoBackup,
                    onToggle = { autoBackup = it }
                ),
                SettingsItem.Switch(
                    title = "Cloud Sync",
                    description = "Sync data across devices",
                    icon = Icons.Default.CloudSync,
                    isChecked = cloudSync,
                    onToggle = { cloudSync = it }
                ),
                SettingsItem.Action(
                    title = "Export Data",
                    description = "Export your lists and data",
                    icon = Icons.Default.Download,
                    action = { showExportConfirm = true }
                ),
                SettingsItem.Action(
                    title = "Data Management",
                    description = "Manage stored data and cache",
                    icon = Icons.Default.Storage,
                    action = { showDataDialog = true }
                )
            )
        ),
        SettingsSection(
            title = "Help & Support",
            items = listOf(
                SettingsItem.Action(
                    title = "Help Center",
                    description = "Get help and tutorials",
                    icon = Icons.Default.Help,
                    action = { /* Navigate to help */ }
                ),
                SettingsItem.Action(
                    title = "Contact Support",
                    description = "Get in touch with our team",
                    icon = Icons.Default.ContactSupport,
                    action = { /* Open contact form */ }
                ),
                SettingsItem.Action(
                    title = "Rate App",
                    description = "Rate us on the app store",
                    icon = Icons.Default.Star,
                    action = { /* Open store rating */ }
                ),
                SettingsItem.Action(
                    title = "About",
                    description = "App version and information",
                    icon = Icons.Default.Info,
                    action = { showAboutDialog = true }
                )
            )
        )
    )
    
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
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Customize your experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = { /* Search settings */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Settings Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                settingsSections.forEachIndexed { sectionIndex, section ->
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = sectionIndex * 50
                                )
                            ) + fadeIn()
                        ) {
                            SettingsSectionCard(
                                section = section,
                                onLanguageClick = { showLanguageDialog = true },
                                onCurrencyClick = { showCurrencyDialog = true },
                                onSortingClick = { showSortingDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    
    if (showDataDialog) {
        DataManagementDialog(
            onDismiss = { showDataDialog = false },
            onClearData = { showClearDataConfirm = true }
        )
    }
    
    if (showLanguageDialog) {
        SelectionDialog(
            title = "Language",
            options = listOf("English", "Spanish", "French", "German", "Italian"),
            currentSelection = language,
            onSelect = { language = it; showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    if (showCurrencyDialog) {
        SelectionDialog(
            title = "Currency",
            options = listOf("USD", "EUR", "GBP", "CAD", "AUD", "JPY"),
            currentSelection = currencyCode,
            onSelect = { currencyCode = it; showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    
    if (showSortingDialog) {
        SelectionDialog(
            title = "Default Sorting",
            options = listOf("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)", "Category", "Recently Added"),
            currentSelection = defaultSorting,
            onSelect = { defaultSorting = it; showSortingDialog = false },
            onDismiss = { showSortingDialog = false }
        )
    }
    
    if (showExportConfirm) {
        ConfirmationDialog(
            title = "Export Data",
            message = "This will export all your grocery lists and settings to a file.",
            confirmText = "Export",
            onConfirm = { 
                // Implement export functionality
                showExportConfirm = false 
            },
            onDismiss = { showExportConfirm = false }
        )
    }
    
    if (showClearDataConfirm) {
        ConfirmationDialog(
            title = "Clear All Data",
            message = "This will permanently delete all your lists and data. This action cannot be undone.",
            confirmText = "Clear Data",
            isDestructive = true,
            onConfirm = { 
                // Implement clear data functionality
                showClearDataConfirm = false
                showDataDialog = false
            },
            onDismiss = { showClearDataConfirm = false }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    section: SettingsSection,
    onLanguageClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
    onSortingClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            section.items.forEachIndexed { index, item ->
                when (item) {
                    is SettingsItem.Switch -> {
                        SettingsSwitch(
                            title = item.title,
                            description = item.description,
                            icon = item.icon,
                            checked = item.isChecked,
                            onCheckedChange = item.onToggle
                        )
                    }
                    is SettingsItem.Action -> {
                        SettingsAction(
                            title = item.title,
                            description = item.description,
                            icon = item.icon,
                            hasChevron = item.hasChevron,
                            onClick = item.action
                        )
                    }
                    is SettingsItem.Selection -> {
                        SettingsSelection(
                            title = item.title,
                            description = item.description,
                            icon = item.icon,
                            currentValue = item.currentValue,
                            onClick = {
                                when (item.title) {
                                    "Language" -> onLanguageClick()
                                    "Currency" -> onCurrencyClick()
                                    "Default Sorting" -> onSortingClick()
                                }
                            }
                        )
                    }
                }
                
                if (index < section.items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsAction(
    title: String,
    description: String,
    icon: ImageVector,
    hasChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (hasChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSelection(
    title: String,
    description: String,
    icon: ImageVector,
    currentValue: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = currentValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    currentSelection: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentSelection,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Smart Cart",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Version 2.1.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Smart shopping lists with AI-powered features to make grocery shopping easier and more efficient.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DataManagementDialog(
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Storage info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Storage Usage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("App Data: 2.3 MB")
                        Text("Images: 1.1 MB")  
                        Text("Cache: 512 KB")
                        Text("Total: 3.9 MB")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                OutlinedButton(
                    onClick = { /* Clear cache */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Cache")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onClearData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Data")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
} 