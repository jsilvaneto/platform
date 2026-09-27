package com.platform.app.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.BillsViewModel
import com.platform.app.presentation.contacts.ContactDetailScreen
import com.platform.app.presentation.contacts.ContactsScreen
import com.platform.app.presentation.contacts.ContactsViewModel
import com.platform.app.presentation.dashboard.DashboardScreen
import com.platform.app.presentation.dashboard.DashboardViewModel
import com.platform.app.presentation.management.ManagementScreen
import com.platform.app.presentation.management.ManagementViewModel

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

        composable(route = Screen.Contacts.route) {
            val viewModel: ContactsViewModel = hiltViewModel()

            ContactsScreen(
                viewModel = viewModel,
                onNavigateToDetail = { contactId ->
                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                }
            )
        }

        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
            val viewModel: ContactsViewModel = hiltViewModel()

            ContactDetailScreen(
                contactId = contactId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Management.route) {
            val viewModel: ManagementViewModel = hiltViewModel()

            ManagementScreen(
                viewModel = viewModel
            )
        }
    }
}
