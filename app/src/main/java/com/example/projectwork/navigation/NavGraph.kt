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

sealed class Screen(val route: String) {
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
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
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
                    type = NavType.IntType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            AddEditPlaceScreen(
                placeId = entry.arguments?.getInt("placeId"),
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
                    navController.navigate(Screen.AddEditPlace.createRoute(placeId))
                }
            )
        }
    }
} 