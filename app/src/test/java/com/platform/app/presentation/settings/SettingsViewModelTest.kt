package com.platform.app.presentation.settings

import android.content.Context
import app.cash.turbine.test
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.ExportBackupUseCase
import com.platform.app.domain.usecase.RestoreBackupUseCase
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
    private lateinit var financialRepository: FinancialRepository
    private lateinit var exportBackupUseCase: ExportBackupUseCase
    private lateinit var restoreBackupUseCase: RestoreBackupUseCase
    private lateinit var context: Context
    private lateinit var viewModel: SettingsViewModel

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            preferencesManager = preferencesManager,
            biometricAuthManager = biometricAuthManager,
            financialRepository = financialRepository,
            exportBackupUseCase = exportBackupUseCase,
            restoreBackupUseCase = restoreBackupUseCase,
            context = context
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesManager = mockk(relaxed = true)
        biometricAuthManager = mockk(relaxed = true)
        financialRepository = mockk(relaxed = true)
        exportBackupUseCase = mockk(relaxed = true)
        restoreBackupUseCase = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { preferencesManager.isBiometricEnabled } returns flowOf(false)
        every { preferencesManager.isDarkMode } returns flowOf(null)
        every { preferencesManager.lastOfflineBackupTimestamp } returns flowOf(1700000000000L)
        every { biometricAuthManager.canAuthenticate() } returns true

        every { financialRepository.getFinancialAccounts() } returns flowOf(emptyList())
        every { financialRepository.getPaymentMethods() } returns flowOf(emptyList())
        every { financialRepository.getCategories() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects preferences and biometric capability`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isBiometricEnabled)
        assertEquals(null, state.isDarkMode)
        assertEquals(1700000000000L, state.lastBackupTimestamp)
        assertTrue(state.isBiometricSupported)
        assertEquals(0, state.accountsCount)
        assertEquals(0, state.paymentMethodsCount)
        assertEquals(0, state.categoriesCount)
    }

    @Test
    fun `financial entity counts are reflected in uiState`() = runTest {
        val fakeAccount = mockk<FinancialAccount>()
        val fakeMethod = mockk<PaymentMethod>()
        val fakeCategory = mockk<Category>()

        every { financialRepository.getFinancialAccounts() } returns flowOf(listOf(fakeAccount, fakeAccount))
        every { financialRepository.getPaymentMethods() } returns flowOf(listOf(fakeMethod))
        every { financialRepository.getCategories() } returns flowOf(listOf(fakeCategory, fakeCategory, fakeCategory))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.accountsCount)
        assertEquals(1, state.paymentMethodsCount)
        assertEquals(3, state.categoriesCount)
    }

    @Test
    fun `ToggleBiometric requests auth when biometric is supported`() = runTest {
        viewModel = createViewModel()
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
        viewModel = createViewModel()
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
        viewModel = createViewModel()
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
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.SetThemeMode(true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { preferencesManager.setDarkMode(true) }
    }

    @Test
    fun `ExportBackupToUri emits snackbar error when usecase fails`() = runTest {
        coEvery { exportBackupUseCase() } returns Result.failure(RuntimeException("Falha de disco"))
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<android.net.Uri>()
        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ExportBackupToUri(mockUri))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.ShowSnackbar)
            assertTrue((effect as SettingsUiEffect.ShowSnackbar).message.contains("Erro ao gerar dados do backup"))
        }
    }

    @Test
    fun `RestoreBackupFromUri emits snackbar error when stream cannot be opened`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<android.net.Uri>()
        every { context.contentResolver.openInputStream(mockUri) } returns null

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.RestoreBackupFromUri(mockUri))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.ShowSnackbar)
            assertTrue((effect as SettingsUiEffect.ShowSnackbar).message.contains("Erro ao abrir arquivo") || (effect as SettingsUiEffect.ShowSnackbar).message.contains("Não foi possível ler"))
        }
    }
}
