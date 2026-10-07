package com.platform.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.platform.app.core.notification.DueReminderManager
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.presentation.navigation.AppDrawer
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
    lateinit var financialRepository: FinancialRepository

    @Inject
    lateinit var extendRecurringBillsUseCase: com.platform.app.domain.usecase.ExtendRecurringBillsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DueReminderManager.scheduleDailyReminder(this)
        setContent {
            val isDarkModePref by preferencesManager.isDarkMode.collectAsState(initial = null)
            val isAmoledPref by preferencesManager.isAmoledMode.collectAsState(initial = false)
            val isDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            PlatformTheme(darkTheme = isDarkTheme, isAmoled = isAmoledPref) {
                val isBiometricEnabled by preferencesManager.isBiometricEnabled.collectAsState(initial = false)
                var isUnlocked by rememberSaveable { mutableStateOf(false) }
                var unlockError by rememberSaveable { mutableStateOf<String?>(null) }

                val isLocked = isBiometricEnabled && !isUnlocked

                // Notificações locais de contas/faturas vencendo hoje após desbloqueio
                LaunchedEffect(isLocked) {
                    if (!isLocked) {
                        extendRecurringBillsUseCase()
                        DueReminderManager.checkAndNotifyDueExpenses(this@MainActivity, financialRepository)
                    }
                }

                // Aciona a autenticação caso a proteção esteja habilitada e a tela bloqueada
                LaunchedEffect(isBiometricEnabled) {
                    if (isBiometricEnabled && !isUnlocked) {
                        biometricAuthManager.promptAuthentication(
                            activity = this@MainActivity,
                            title = "Desbloquear Platform",
                            subtitle = "Use biometria ou a senha do celular para acessar seus dados",
                            onSuccess = {
                                isUnlocked = true
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
                    if (isLocked) {
                        BiometricLockOverlay(
                            onUnlockRequest = {
                                unlockError = null
                                biometricAuthManager.promptAuthentication(
                                    activity = this@MainActivity,
                                    title = "Desbloquear Platform",
                                    subtitle = "Use biometria ou a senha do celular para acessar seus dados",
                                    onSuccess = {
                                        isUnlocked = true
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
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        val coroutineScope = rememberCoroutineScope()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                AppDrawer(
                                    currentRoute = currentRoute,
                                    onNavigate = { screen ->
                                        coroutineScope.launch { drawerState.close() }
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Dashboard.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onCloseDrawer = {
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                        ) {
                            Scaffold { innerPadding ->
                                NavGraph(
                                    navController = navController,
                                    paddingValues = innerPadding,
                                    biometricAuthManager = biometricAuthManager,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
