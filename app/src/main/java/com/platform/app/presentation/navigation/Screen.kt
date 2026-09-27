package com.platform.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard_screen", "Dashboard")
    object Bills : Screen("bills_screen", "Contas")
    object Categories : Screen("categories_screen", "Categorias")
}
