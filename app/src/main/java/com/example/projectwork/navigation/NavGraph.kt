package com.example.projectwork.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.projectwork.screens.AddEditPlaceScreen
import com.example.projectwork.screens.PlaceDetailScreen
import com.example.projectwork.screens.GroceryListScreen
import com.example.projectwork.screens.EditGroceryListScreen
import com.example.projectwork.screens.RecipeScreen
import com.example.projectwork.screens.AnalyticsScreen
import com.example.projectwork.screens.BudgetScreen
import com.example.projectwork.screens.HomeScreen
import com.example.projectwork.screens.LoadingScreen
import com.example.projectwork.screens.OnboardingScreen
import com.example.projectwork.screens.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
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

        composable(Screen.Budget.route) {
            BudgetScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            // ChatScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }
    }
} 