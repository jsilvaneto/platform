package com.platform.app.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.BillsViewModel
import com.platform.app.presentation.categories.CategoriesScreen
import com.platform.app.presentation.categories.CategoriesViewModel
import com.platform.app.presentation.dashboard.DashboardScreen
import com.platform.app.presentation.dashboard.DashboardViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            DashboardScreen(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        }

        composable(route = Screen.Bills.route) {
            val viewModel: BillsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            BillsScreen(
                uiState = uiState,
                uiEffect = viewModel.uiEffect,
                onAction = viewModel::onAction
            )
        }

        composable(route = Screen.Categories.route) {
            val viewModel: CategoriesViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CategoriesScreen(
                uiState = uiState,
                onAddCategory = { name, color -> viewModel.addCategory(name, color) }
            )
        }
    }
}
