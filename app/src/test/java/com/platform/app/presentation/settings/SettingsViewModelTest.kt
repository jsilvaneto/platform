package com.platform.app.presentation.settings

import app.cash.turbine.test
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesManager = mockk(relaxed = true)
        biometricAuthManager = mockk(relaxed = true)

        every { preferencesManager.isBiometricEnabled } returns flowOf(false)
        every { preferencesManager.isDarkMode } returns flowOf(null)
        every { preferencesManager.lastOfflineBackupTimestamp } returns flowOf(1700000000000L)
        every { biometricAuthManager.canAuthenticate() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects preferences and biometric capability`() = runTest {
        viewModel = SettingsViewModel(preferencesManager, biometricAuthManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isBiometricEnabled)
        assertEquals(null, state.isDarkMode)
        assertEquals(1700000000000L, state.lastBackupTimestamp)
        assertTrue(state.isBiometricSupported)
    }

    @Test
    fun `ToggleBiometric requests auth when biometric is supported`() = runTest {
        viewModel = SettingsViewModel(preferencesManager, biometricAuthManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ToggleBiometric(true))
            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.RequestBiometricAuthForToggle)
            assertEquals(true, (effect as SettingsUiEffect.RequestBiometricAuthForToggle).targetState)
        }
    }

    @Test
    fun `ToggleBiometric shows snackbar error when biometric is not supported`() = runTest {
        every { biometricAuthManager.canAuthenticate() } returns false
        viewModel = SettingsViewModel(preferencesManager, biometricAuthManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ToggleBiometric(true))
            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.ShowSnackbar)
            assertTrue((effect as SettingsUiEffect.ShowSnackbar).message.contains("não possui biometria"))
        }
    }

    @Test
    fun `confirmBiometricToggle updates preferences and emits confirmation snackbar`() = runTest {
        coEvery { preferencesManager.setBiometricEnabled(true) } returns Unit
        viewModel = SettingsViewModel(preferencesManager, biometricAuthManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.confirmBiometricToggle(true)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.ShowSnackbar)
            assertTrue((effect as SettingsUiEffect.ShowSnackbar).message.contains("ativado"))
            coVerify(exactly = 1) { preferencesManager.setBiometricEnabled(true) }
        }
    }

    @Test
    fun `SetThemeMode calls preferencesManager setDarkMode`() = runTest {
        coEvery { preferencesManager.setDarkMode(true) } returns Unit
        viewModel = SettingsViewModel(preferencesManager, biometricAuthManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.SetThemeMode(true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { preferencesManager.setDarkMode(true) }
    }
}
