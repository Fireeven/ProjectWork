package com.example.projectwork.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
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
    object Detail : Screen("place/{placeId}")
} 