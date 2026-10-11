package com.platform.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.platform.app.core.notification.DueReminderManager
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.presentation.MainViewModel
import com.platform.app.presentation.navigation.AppModalDrawerSheet
import com.platform.app.presentation.navigation.AppPermanentDrawerSheet
import com.platform.app.presentation.navigation.NavGraph
import com.platform.app.presentation.navigation.Screen
import com.platform.app.presentation.security.BiometricLockOverlay
import com.platform.app.presentation.theme.PlatformTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private var pendingDeepLinkRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
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
                        val mainViewModel: MainViewModel = hiltViewModel()
                        val drawerOverview by mainViewModel.drawerOverview.collectAsState()
                        val configuration = LocalConfiguration.current
                        val isWideScreen = configuration.screenWidthDp >= 600

                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        val coroutineScope = rememberCoroutineScope()

                        // Processa deep links das notificações após o desbloqueio
                        LaunchedEffect(pendingDeepLinkRoute, isLocked) {
                            val route = pendingDeepLinkRoute
                            if (!isLocked && !route.isNullOrBlank()) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                pendingDeepLinkRoute = null
                            }
                        }

                        val onNavigate: (Screen) -> Unit = { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }

                        val onOpenDrawer: () -> Unit = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }

                        val startDestinationRoute = if (drawerOverview.initialScreen == "today") {
                            Screen.Dashboard.route
                        } else {
                            Screen.Overview.route
                        }

                        if (isWideScreen) {
                            PermanentNavigationDrawer(
                                drawerContent = {
                                    AppPermanentDrawerSheet(
                                        currentRoute = currentRoute,
                                        monthRemainingCents = drawerOverview.remainingMonthCents,
                                        overdueCount = drawerOverview.overdueCount,
                                        lastBackupTimestamp = drawerOverview.lastBackupTimestamp,
                                        onNavigate = onNavigate
                                    )
                                }
                            ) {
                                NavGraph(
                                    navController = navController,
                                    paddingValues = PaddingValues(0.dp),
                                    biometricAuthManager = biometricAuthManager,
                                    onOpenDrawer = onOpenDrawer,
                                    startDestination = startDestinationRoute
                                )
                            }
                        } else {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                gesturesEnabled = true,
                                drawerContent = {
                                    AppModalDrawerSheet(
                                        currentRoute = currentRoute,
                                        monthRemainingCents = drawerOverview.remainingMonthCents,
                                        overdueCount = drawerOverview.overdueCount,
                                        lastBackupTimestamp = drawerOverview.lastBackupTimestamp,
                                        onNavigate = onNavigate,
                                        onCloseDrawer = {
                                            coroutineScope.launch {
                                                drawerState.close()
                                            }
                                        }
                                    )
                                }
                            ) {
                                NavGraph(
                                    navController = navController,
                                    paddingValues = PaddingValues(0.dp),
                                    biometricAuthManager = biometricAuthManager,
                                    onOpenDrawer = onOpenDrawer,
                                    startDestination = startDestinationRoute
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val route = intent?.getStringExtra("target_route")
            ?: intent?.data?.getQueryParameter("route")
            ?: intent?.data?.path?.removePrefix("/")
        if (!route.isNullOrBlank()) {
            pendingDeepLinkRoute = route
        }
    }
}
