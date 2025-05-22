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

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            com.example.projectwork.screens.HomeScreen(
                onAddPlace = { navController.navigate(Screen.AddEditPlace.createRoute()) },
                onPlaceClick = { placeId -> 
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                }
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
                onNavigateBack = { navController.popBackStack() }
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
                    navController.navigate(Screen.GroceryList.createRoute(placeId))
                }
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
    }
} 