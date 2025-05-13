package com.example.projectwork.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.projectwork.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
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
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

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
                    navController.navigate(Screen.EditGroceryList.createRoute(placeId))
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
    }
}