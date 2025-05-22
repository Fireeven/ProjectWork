package com.example.projectwork.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.projectwork.navigation.Screen
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            title = "Home",
            selectedIcon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            unselectedIcon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
        ),
        BottomNavItem(
            route = Screen.Recipe.route,
            title = "Recipes",
            selectedIcon = { Icon(Icons.Filled.RestaurantMenu, contentDescription = "Recipes") },
            unselectedIcon = { Icon(Icons.Outlined.RestaurantMenu, contentDescription = "Recipes") }
        ),
        BottomNavItem(
            route = Screen.Analytics.route,
            title = "Analytics",
            selectedIcon = { Icon(Icons.Filled.Analytics, contentDescription = "Analytics") },
            unselectedIcon = { Icon(Icons.Outlined.Analytics, contentDescription = "Analytics") }
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            title = "Settings",
            selectedIcon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            unselectedIcon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") }
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Don't show bottom nav on welcome screen
    if (currentDestination?.route == Screen.Welcome.route) {
        return
    }

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { 
                it.route == item.route 
            } == true
            
            NavigationBarItem(
                icon = { 
                    if (selected) item.selectedIcon() else item.unselectedIcon() 
                },
                label = { 
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
} 