package com.example.projectwork.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.projectwork.data.Store
import com.example.projectwork.screens.DetailScreen
import com.example.projectwork.screens.EditScreen
import com.example.projectwork.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{storeId}") {
        fun createRoute(storeId: Int) = "detail/$storeId"
    }
    object Edit : Screen("edit/{storeId}") {
        fun createRoute(storeId: Int) = "edit/$storeId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")?.toIntOrNull()
            DetailScreen(navController, storeId)
        }
        composable(Screen.Edit.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")?.toIntOrNull()
            EditScreen(navController, storeId)
        }
    }
} 