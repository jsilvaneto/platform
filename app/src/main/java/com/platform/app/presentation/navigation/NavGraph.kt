package com.platform.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.platform.app.presentation.bills.NewExpenseScreen
import com.platform.app.presentation.bills.NewExpenseViewModel
import com.platform.app.presentation.budgets.BudgetsScreen
import com.platform.app.presentation.budgets.BudgetsViewModel
import com.platform.app.presentation.contacts.ContactDetailScreen
import com.platform.app.presentation.contacts.ContactsScreen
import com.platform.app.presentation.contacts.ContactsViewModel
import com.platform.app.presentation.creditcards.CreditCardsScreen
import com.platform.app.presentation.creditcards.CreditCardsViewModel
import com.platform.app.presentation.dashboard.DashboardViewModel
import com.platform.app.presentation.expenseitems.ExpenseItemsScreen
import com.platform.app.presentation.expenseitems.ExpenseItemsViewModel
import com.platform.app.presentation.goals.GoalsScreen
import com.platform.app.presentation.goals.GoalsViewModel
import com.platform.app.presentation.home.HomeScreen
import com.platform.app.presentation.home.HomeViewModel
import com.platform.app.presentation.management.AccountsScreen
import com.platform.app.presentation.management.CategoriesScreen
import com.platform.app.presentation.management.ManagementScreen
import com.platform.app.presentation.management.ManagementViewModel
import com.platform.app.presentation.management.PaymentMethodsScreen
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
        modifier = Modifier.padding(paddingValues),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(260))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(260))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(260))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(260))
        }
    ) {
        composable(route = Screen.Dashboard.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                uiEffect = viewModel.uiEffect,
                onAction = viewModel::onAction,
                onOpenDrawer = onOpenDrawer,
                onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.createRoute()) }
            )
        }

        composable(
            route = Screen.NewExpense.route,
            arguments = listOf(
                navArgument("duplicateBillId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: NewExpenseViewModel = hiltViewModel()
            NewExpenseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
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
                onOpenDrawer = onOpenDrawer,
                onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.createRoute()) },
                onNavigateToDuplicate = { billId -> navController.navigate(Screen.NewExpense.createRoute(billId)) }
            )
        }

        composable(route = Screen.RecurringInstallments.route) {
            val viewModel: RecurringInstallmentsViewModel = hiltViewModel()

            RecurringInstallmentsScreen(
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.route) }
            )
        }

        composable(route = Screen.CreditCards.route) {
            val viewModel: CreditCardsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CreditCardsScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onOpenDrawer = onOpenDrawer,
                onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.route) }
            )
        }

        composable(route = Screen.ExpenseItems.route) {
            val viewModel: ExpenseItemsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            ExpenseItemsScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onNavigateBack = { navController.popBackStack() }
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

        composable(route = Screen.Accounts.route) {
            val viewModel: ManagementViewModel = hiltViewModel()
            AccountsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.PaymentMethods.route) {
            val viewModel: ManagementViewModel = hiltViewModel()
            PaymentMethodsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Categories.route) {
            val viewModel: ManagementViewModel = hiltViewModel()
            CategoriesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Management.route,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            val viewModel: ManagementViewModel = hiltViewModel()

            ManagementScreen(
                viewModel = viewModel,
                initialTab = tab,
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
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route)
                },
                onNavigateToPaymentMethods = {
                    navController.navigate(Screen.PaymentMethods.route)
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToExpenseItems = {
                    navController.navigate(Screen.ExpenseItems.route)
                }
            )
        }
    }
}
