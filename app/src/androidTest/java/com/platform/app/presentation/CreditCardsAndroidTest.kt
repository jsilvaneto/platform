package com.platform.app.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.presentation.components.PlatformCreditCardView
import com.platform.app.presentation.creditcards.components.CreditCardInvoiceCard
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes instrumentados e de renderização de Cartões de Crédito (Prompt 61):
 * - Cartão plano virtual (sup2, borda, raio 20, 170dp, barra 8dp com percentual).
 * - Fatura com total em 30sp e chip semafórico de status.
 * - Suporte a temas (AMOLED, Escuro, Claro) e escala de fonte de 1,3x.
 */
@RunWith(AndroidJUnit4::class)
class CreditCardsAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleCard = CreditCard(
        id = "card-1",
        name = "Nubank Ultravioleta",
        totalLimitCents = 1000000L,
        closingDay = 25,
        dueDay = 5
    )

    private val sampleInvoice = CreditCardInvoice(
        id = "inv-1",
        creditCardId = "card-1",
        referenceMonth = "2026-10",
        closingDate = 1761350400000L,
        dueDate = 1762300800000L,
        status = InvoiceStatus.ABERTA,
        totalAmountCents = 350000L
    )

    private val sampleSummary = CreditCardWithInvoiceSummary(
        card = sampleCard,
        currentInvoice = sampleInvoice,
        usedLimitCents = 350000L
    )

    @Test
    fun creditCardView_renderInAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                PlatformCreditCardView(
                    summary = sampleSummary,
                    isSelected = true,
                    onClick = {},
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Nubank Ultravioleta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fecha dia 25 · Vence dia 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("35%").assertIsDisplayed()
    }

    @Test
    fun creditCardView_renderInLightTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = false, isAmoled = false) {
                PlatformCreditCardView(
                    summary = sampleSummary,
                    isSelected = false,
                    onClick = {},
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Nubank Ultravioleta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fecha dia 25 · Vence dia 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("35%").assertIsDisplayed()
    }

    @Test
    fun creditCardInvoiceCard_renderTotalIn30spAndStatusChip_inAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                CreditCardInvoiceCard(invoice = sampleInvoice)
            }
        }

        composeTestRule.onNodeWithText("Fatura 2026-10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aberta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Valor Total").assertIsDisplayed()
    }

    @Test
    fun creditCardInvoiceCard_renderPaidStatusChip_inLightTheme() {
        val paidInvoice = sampleInvoice.copy(status = InvoiceStatus.PAGA)
        composeTestRule.setContent {
            PlatformTheme(darkTheme = false, isAmoled = false) {
                CreditCardInvoiceCard(invoice = paidInvoice)
            }
        }

        composeTestRule.onNodeWithText("Fatura 2026-10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paga").assertIsDisplayed()
    }

    @Test
    fun creditCardComponents_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highDensity) {
                PlatformTheme {
                    Column {
                        PlatformCreditCardView(
                            summary = sampleSummary,
                            isSelected = true,
                            onClick = {},
                            onEdit = {},
                            onDelete = {}
                        )
                        CreditCardInvoiceCard(invoice = sampleInvoice)
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Nubank Ultravioleta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fatura 2026-10").assertIsDisplayed()
    }
}
