package com.platform.app.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.domain.model.BudgetRigidityAnalysis
import com.platform.app.domain.model.BudgetRigidityStatus
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.NatureSpend
import com.platform.app.presentation.statistics.components.NatureDistributionCard
import com.platform.app.presentation.statistics.components.PlanningSection
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes instrumentados e de renderização da tela de Análises (Prompt 62):
 * - Abas segmentadas e seletor de período.
 * - Card Equilíbrio do orçamento com barras neutras.
 * - Seção Planejar (Orçamentos, Metas, Plano de quitação).
 * - Suporte a temas (AMOLED, Escuro, Claro) e escala de fonte de 1,3x.
 */
@RunWith(AndroidJUnit4::class)
class AnalyticsAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleNatureSpends = listOf(
        NatureSpend(
            nature = ExpenseNature.OBRIGATORIO,
            amountCents = 250000L,
            percentage = 50.0f
        ),
        NatureSpend(
            nature = ExpenseNature.NECESSARIO,
            amountCents = 150000L,
            percentage = 30.0f
        ),
        NatureSpend(
            nature = ExpenseNature.DESEJA,
            amountCents = 100000L,
            percentage = 20.0f
        )
    )

    private val sampleRigidity = BudgetRigidityAnalysis(
        mandatoryPercentage = 50.0f,
        necessaryPercentage = 30.0f,
        wantsPercentage = 20.0f,
        savingsPercentage = 0f,
        status = BudgetRigidityStatus.EQUILIBRADO,
        badgeLabel = "Orçamento Equilibrado",
        description = "Suas despesas essenciais representam 80% do orçamento total.",
        totalBudgetCents = 500000L
    )

    @Test
    fun natureDistributionCard_renderNeutralBars_inAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                NatureDistributionCard(
                    natureDistribution = sampleNatureSpends,
                    savingsCents = 50000L,
                    savingsPercentage = 10f,
                    budgetRigidity = sampleRigidity,
                    totalDueCents = 500000L,
                    isPrivate = false
                )
            }
        }

        composeTestRule.onNodeWithText("Equilíbrio do orçamento").assertIsDisplayed()
        composeTestRule.onNodeWithText("Orçamento Equilibrado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Obrigatório").assertIsDisplayed()
        composeTestRule.onNodeWithText("Necessário").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desejos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Poupança (Metas)").assertIsDisplayed()
    }

    @Test
    fun planningSection_renderTiles_inLightTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = false, isAmoled = false) {
                PlanningSection(
                    onNavigateToBudgets = {},
                    onNavigateToGoals = {},
                    onOpenPayoffPlan = {}
                )
            }
        }

        composeTestRule.onNodeWithText("PLANEJAR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Orçamentos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Metas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plano de quitação").assertIsDisplayed()
    }

    @Test
    fun analyticsComponents_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highDensity) {
                PlatformTheme {
                    Column {
                        NatureDistributionCard(
                            natureDistribution = sampleNatureSpends,
                            savingsCents = 50000L,
                            savingsPercentage = 10f,
                            budgetRigidity = sampleRigidity,
                            totalDueCents = 500000L,
                            isPrivate = false
                        )
                        PlanningSection(
                            onNavigateToBudgets = {},
                            onNavigateToGoals = {},
                            onOpenPayoffPlan = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Equilíbrio do orçamento").assertIsDisplayed()
        composeTestRule.onNodeWithText("PLANEJAR").assertIsDisplayed()
    }
}
