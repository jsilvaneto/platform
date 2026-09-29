package com.platform.app.presentation.management

import app.cash.turbine.test
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.repository.FinancialRepository
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
class ManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var viewModel: ManagementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { repository.getPaymentMethods() } returns flowOf(emptyList())
        every { repository.getCategories() } returns flowOf(emptyList())
        every { repository.getAllInstallments() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SelectTab action should update selectedTab in state`() = runTest {
        viewModel = ManagementViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ManagementUiAction.SelectTab(2))
        assertEquals(2, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `SaveAccount should call repository saveFinancialAccount and emit ShowSnackbar and ItemSaved`() = runTest {
        val account = FinancialAccount(
            id = "acc-1",
            name = "Nubank PF",
            accountType = "Conta Corrente",
            colorHex = "#8B5CF6"
        )

        viewModel = ManagementViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(ManagementUiAction.SaveAccount(account))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.saveFinancialAccount(account) }

            val effect1 = awaitItem()
            assertTrue(effect1 is ManagementUiEffect.ShowSnackbar)
            assertTrue((effect1 as ManagementUiEffect.ShowSnackbar).message.contains("Nubank"))

            val effect2 = awaitItem()
            assertTrue(effect2 is ManagementUiEffect.ItemSaved)
        }
    }

    @Test
    fun `SavePaymentMethod should call repository savePaymentMethod`() = runTest {
        val method = PaymentMethod(id = "pm-1", name = "Pix", iconName = "qr_code")

        viewModel = ManagementViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(ManagementUiAction.SavePaymentMethod(method))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.savePaymentMethod(method) }

            val effect1 = awaitItem()
            assertTrue(effect1 is ManagementUiEffect.ShowSnackbar)

            val effect2 = awaitItem()
            assertTrue(effect2 is ManagementUiEffect.ItemSaved)
        }
    }
}
