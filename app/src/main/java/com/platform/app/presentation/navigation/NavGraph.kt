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
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.BillsViewModel
import com.platform.app.presentation.budgets.BudgetsScreen
import com.platform.app.presentation.budgets.BudgetsViewModel
import com.platform.app.presentation.contacts.ContactDetailScreen
import com.platform.app.presentation.contacts.ContactsScreen
import com.platform.app.presentation.contacts.ContactsViewModel
import com.platform.app.presentation.dashboard.DashboardScreen
import com.platform.app.presentation.dashboard.DashboardViewModel
import com.platform.app.presentation.goals.GoalsScreen
import com.platform.app.presentation.goals.GoalsViewModel
import com.platform.app.presentation.management.ManagementScreen
import com.platform.app.presentation.management.ManagementViewModel
import com.platform.app.presentation.recurring.RecurringInstallmentsScreen
import com.platform.app.presentation.recurring.RecurringInstallmentsViewModel
import com.platform.app.presentation.settings.SettingsScreen
import com.platform.app.presentation.settings.SettingsViewModel
import com.platform.app.presentation.statistics.StatisticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    biometricAuthManager: BiometricAuthManager,
    onOpenDrawer: () -> Unit,
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
                onAction = viewModel::onAction,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Statistics.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            StatisticsScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Bills.route) {
            val viewModel: BillsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            BillsScreen(
                uiState = uiState,
                uiEffect = viewModel.uiEffect,
                onAction = viewModel::onAction,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.RecurringInstallments.route) {
            val viewModel: RecurringInstallmentsViewModel = hiltViewModel()

            RecurringInstallmentsScreen(
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Goals.route) {
            val viewModel: GoalsViewModel = hiltViewModel()

            GoalsScreen(
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Budgets.route) {
            val viewModel: BudgetsViewModel = hiltViewModel()

            BudgetsScreen(
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Contacts.route) {
            val viewModel: ContactsViewModel = hiltViewModel()

            ContactsScreen(
                viewModel = viewModel,
                onNavigateToDetail = { contactId ->
                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                },
                onOpenDrawer = onOpenDrawer
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
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()

            SettingsScreen(
                viewModel = viewModel,
                biometricAuthManager = biometricAuthManager,
                onOpenDrawer = onOpenDrawer,
                onNavigateToManagement = {
                    navController.navigate(Screen.Management.route)
                }
            )
        }
    }
}
