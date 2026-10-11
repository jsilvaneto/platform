package com.platform.app.presentation.dashboard

import app.cash.turbine.test
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.usecase.GetFinancialDashboardUseCase
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDashboardUseCase: GetFinancialDashboardUseCase
    private lateinit var togglePaymentUseCase: ToggleInstallmentPaymentUseCase
    private lateinit var viewModel: DashboardViewModel

    private val mockMetrics = mockk<FinancialDashboardMetrics>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getDashboardUseCase = mockk(relaxed = true)
        togglePaymentUseCase = mockk(relaxed = true)

        every { getDashboardUseCase(any()) } returns flowOf(mockMetrics)

        viewModel = DashboardViewModel(getDashboardUseCase, togglePaymentUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load sets default state with PRESENT tab and MONTH period`() = runTest {
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsTab.PRESENT, state.selectedTab)
            assertEquals(StatisticsPeriod.MONTH, state.selectedPeriod)
            assertEquals(mockMetrics, state.metrics)
            assertFalse(state.isPrivacyMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectTab updates tab selection`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(DashboardUiAction.SelectTab(StatisticsTab.FUTURE))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsTab.FUTURE, state.selectedTab)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(DashboardUiAction.SelectTab(StatisticsTab.PAST))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsTab.PAST, state.selectedTab)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectPeriod updates period selection`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(DashboardUiAction.SelectPeriod(StatisticsPeriod.QUARTER))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsPeriod.QUARTER, state.selectedPeriod)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(DashboardUiAction.SelectPeriod(StatisticsPeriod.YEAR))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(StatisticsPeriod.YEAR, state.selectedPeriod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TogglePrivacyMode flips privacy flag`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(DashboardUiAction.TogglePrivacyMode)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isPrivacyMode)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(DashboardUiAction.TogglePrivacyMode)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isPrivacyMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
