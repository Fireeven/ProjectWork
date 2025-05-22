package com.example.projectwork.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    
    // Settings state
    var isDarkMode by remember { mutableStateOf(false) }
    var showCategoryColors by remember { mutableStateOf(true) }
    var defaultSorting by remember { mutableStateOf("Name (A-Z)") }
    var groupBySections by remember { mutableStateOf(true) }
    var useBiometricAuth by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var backupFrequency by remember { mutableStateOf("Weekly") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                initialAlpha = 0f,
                animationSpec = tween(durationMillis = 500)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Appearance Section
                SettingsSection(title = "Appearance") {
                    SettingsSwitch(
                        title = "Dark Mode",
                        description = "Use dark theme throughout the app",
                        icon = Icons.Default.DarkMode,
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )
                    
                    SettingsSwitch(
                        title = "Color-code Categories",
                        description = "Use different colors for store categories",
                        icon = Icons.Default.Palette,
                        checked = showCategoryColors,
                        onCheckedChange = { showCategoryColors = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // List Preferences Section
                SettingsSection(title = "List Preferences") {
                    SettingsOptionMenu(
                        title = "Default Sorting",
                        description = "Choose how items are sorted by default",
                        icon = Icons.Default.Sort,
                        selectedOption = defaultSorting,
                        options = listOf("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)"),
                        onOptionSelected = { defaultSorting = it }
                    )
                    
                    SettingsSwitch(
                        title = "Group by Sections",
                        description = "Group items by categories on grocery lists",
                        icon = Icons.Default.Category,
                        checked = groupBySections,
                        onCheckedChange = { groupBySections = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Security Section
                SettingsSection(title = "Security") {
                    SettingsSwitch(
                        title = "Biometric Authentication",
                        description = "Use fingerprint or face unlock for app access",
                        icon = Icons.Default.Fingerprint,
                        checked = useBiometricAuth,
                        onCheckedChange = { useBiometricAuth = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notifications Section
                SettingsSection(title = "Notifications") {
                    SettingsSwitch(
                        title = "Enable Notifications",
                        description = "Show reminders and alerts from the app",
                        icon = Icons.Default.Notifications,
                        checked = isNotificationsEnabled,
                        onCheckedChange = { isNotificationsEnabled = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Backup & Sync Section
                SettingsSection(title = "Backup & Sync") {
                    SettingsOptionMenu(
                        title = "Backup Frequency",
                        description = "How often to backup your grocery data",
                        icon = Icons.Default.Backup,
                        selectedOption = backupFrequency,
                        options = listOf("Never", "Daily", "Weekly", "Monthly"),
                        onOptionSelected = { backupFrequency = it }
                    )
                    
                    SettingsButton(
                        title = "Export Data",
                        description = "Export all grocery lists as CSV file",
                        icon = Icons.Default.Download,
                        onClick = { /* TODO: Implement export functionality */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Help & Support Section
                SettingsSection(title = "Help & Support") {
                    SettingsButton(
                        title = "Tutorial",
                        description = "Learn how to use the app's features",
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { /* TODO: Show tutorial */ }
                    )
                    
                    SettingsButton(
                        title = "About",
                        description = "App version and legal information",
                        icon = Icons.Default.Info,
                        onClick = { /* TODO: Show about screen */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for BottomNavBar
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
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
fun SettingsButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight, 
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsOptionMenu(
    title: String,
    description: String,
    icon: ImageVector,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box {
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        leadingIcon = {
                            if (option == selectedOption) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
} 