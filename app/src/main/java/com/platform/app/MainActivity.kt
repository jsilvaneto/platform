package com.platform.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.platform.app.core.notification.DueReminderManager
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.navigation.NavGraph
import com.platform.app.presentation.navigation.Screen
import com.platform.app.presentation.security.BiometricLockOverlay
import com.platform.app.presentation.theme.PlatformTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var appLockState: com.platform.app.core.security.AppLockState

    @Inject
    lateinit var financialRepository: FinancialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DueReminderManager.scheduleDailyReminder(this)
        setContent {
            val isDarkModePref by preferencesManager.isDarkMode.collectAsState(initial = null)
            val isAmoledPref by preferencesManager.isAmoledMode.collectAsState(initial = false)
            val isDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            PlatformTheme(darkTheme = isDarkTheme, isAmoled = isAmoledPref) {
                val isBiometricEnabled by preferencesManager.isBiometricEnabled.collectAsState(initial = null)
                val hideContentInRecents by preferencesManager.hideContentInRecents.collectAsState(initial = true)
                val isUnlocked by appLockState.isUnlocked.collectAsState()
                var unlockError by remember { mutableStateOf<String?>(null) }

                // FLAG_SECURE para ocultar conteúdo nos apps recentes e capturas de tela
                LaunchedEffect(isBiometricEnabled, hideContentInRecents) {
                    if (isBiometricEnabled == true && hideContentInRecents) {
                        window.setFlags(
                            android.view.WindowManager.LayoutParams.FLAG_SECURE,
                            android.view.WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                val isLocked = isBiometricEnabled == true && !isUnlocked

                // Notificações locais de contas/faturas vencendo hoje após desbloqueio
                LaunchedEffect(isLocked, isBiometricEnabled) {
                    if (isBiometricEnabled != null && !isLocked) {
                        DueReminderManager.checkAndNotifyDueExpenses(this@MainActivity, financialRepository)
                    }
                }

                // Aciona a autenticação caso a proteção esteja habilitada e a tela bloqueada
                LaunchedEffect(isBiometricEnabled, isUnlocked) {
                    if (isBiometricEnabled == true && !isUnlocked) {
                        biometricAuthManager.promptAuthentication(
                            activity = this@MainActivity,
                            title = "Desbloquear Platform",
                            subtitle = "Use biometria ou a senha do celular para acessar seus dados",
                            onSuccess = {
                                appLockState.unlock()
                                unlockError = null
                            },
                            onError = { err ->
                                unlockError = err
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBiometricEnabled == null) {
                        // Tela neutra até o DataStore responder, sem compor conteúdo privado
                    } else if (isLocked) {
                        BiometricLockOverlay(
                            onUnlockRequest = {
                                unlockError = null
                                biometricAuthManager.promptAuthentication(
                                    activity = this@MainActivity,
                                    title = "Desbloquear Platform",
                                    subtitle = "Use biometria ou a senha do celular para acessar seus dados",
                                    onSuccess = {
                                        appLockState.unlock()
                                        unlockError = null
                                    },
                                    onError = { err ->
                                        unlockError = err
                                    }
                                )
                            },
                            errorMessage = unlockError
                        )
                    } else {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val bottomNavItems = listOf(
                            BottomNavItem(Screen.Dashboard, AppStrings.Navigation.TODAY, Icons.Default.Home),
                            BottomNavItem(Screen.Bills, AppStrings.Navigation.BILLS, Icons.AutoMirrored.Filled.ReceiptLong),
                            BottomNavItem(Screen.CreditCards, AppStrings.Navigation.CREDIT_CARDS, Icons.Default.CreditCard),
                            BottomNavItem(Screen.Statistics, AppStrings.Navigation.ANALYTICS, Icons.Default.BarChart),
                            BottomNavItem(Screen.More, AppStrings.Navigation.MORE, Icons.Default.MoreHoriz)
                        )

                        val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 3.dp
                                    ) {
                                        bottomNavItems.forEach { item ->
                                            val selected = currentRoute == item.screen.route
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = {
                                                    if (currentRoute != item.screen.route) {
                                                        navController.navigate(item.screen.route) {
                                                            popUpTo(Screen.Dashboard.route) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.title
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = item.title,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavGraph(
                                navController = navController,
                                paddingValues = innerPadding,
                                biometricAuthManager = biometricAuthManager
                            )
                        }
                    }
                }
            }
        }
    }
}
