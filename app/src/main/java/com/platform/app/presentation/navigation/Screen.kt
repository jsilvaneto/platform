package com.platform.app.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Details : Screen("details_screen/{itemId}") {
        fun createRoute(itemId: String) = "details_screen/$itemId"
    }
}
