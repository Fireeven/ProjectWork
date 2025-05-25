package com.example.projectwork.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.projectwork.data.Store
import com.example.projectwork.screens.AddEditPlaceScreen
import com.example.projectwork.screens.DetailScreen
import com.example.projectwork.screens.EditScreen
import com.example.projectwork.screens.HomeScreen
import com.example.projectwork.screens.PlaceDetailScreen
import com.example.projectwork.screens.GroceryListScreen
import com.example.projectwork.screens.EditGroceryListScreen
import com.example.projectwork.screens.WelcomeScreen
import com.example.projectwork.screens.RecipeScreen
import com.example.projectwork.screens.AnalyticsScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object AddEditPlace : Screen("addEditPlace?placeId={placeId}") {
        fun createRoute(placeId: Int? = null) = if (placeId != null) {
            "addEditPlace?placeId=$placeId"
        } else {
            "addEditPlace"
        }
    }
    object PlaceDetail : Screen("placeDetail/{placeId}") {
        fun createRoute(placeId: Int) = "placeDetail/$placeId"
    }
    object GroceryList : Screen("groceryList/{placeId}") {
        fun createRoute(placeId: Int) = "groceryList/$placeId"
    }
    object EditGroceryList : Screen("editGroceryList/{placeId}") {
        fun createRoute(placeId: Int) = "editGroceryList/$placeId"
    }
    object Recipe : Screen("recipe")
    object Analytics : Screen("analytics")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToHome = { 
                    navController.navigate(Screen.Home.route) {
                        // Clear the back stack so user can't go back to welcome screen
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddPlaceClick = { navController.navigate(Screen.AddEditPlace.createRoute()) },
                onPlaceClick = { placeId -> 
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                },
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditPlace.route,
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            AddEditPlaceScreen(
                placeId = entry.arguments?.getString("placeId")?.toIntOrNull(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            PlaceDetailScreen(
                placeId = entry.arguments?.getInt("placeId") ?: return@composable,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { placeId ->
                    navController.navigate(Screen.AddEditPlace.createRoute(placeId))
                },
                navController = navController
            )
        }

        composable(
            route = Screen.GroceryList.route,
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            GroceryListScreen(
                placeId = entry.arguments?.getInt("placeId") ?: return@composable,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditGroceryList.route,
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            EditGroceryListScreen(
                placeId = entry.arguments?.getInt("placeId") ?: return@composable,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Recipe.route) {
            RecipeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            // ChatScreen()
        }

        composable(Screen.Settings.route) {
            // SettingsScreen()
        }
    }
} 