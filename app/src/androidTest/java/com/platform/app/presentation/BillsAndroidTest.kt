package com.platform.app.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.presentation.bills.BillPeriodFilter
import com.platform.app.presentation.bills.components.BillsFilterBar
import com.platform.app.presentation.bills.components.BillsInstallmentList
import com.platform.app.presentation.bills.components.QuickExpenseBottomSheet
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes instrumentados e de renderização da tela Contas e Lançamento Rápido (Prompt 60):
 * - Contas com abas e chips neutros.
 * - PlatformListRow na listagem.
 * - Lançamento rápido (raio 28, valor 52sp, chips neutros, botão fixo).
 * - Suporte a temas (AMOLED, Claro) e escala de fonte de 1,3x.
 */
@RunWith(AndroidJUnit4::class)
class BillsAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleItems = listOf(
        ExpenseItem(
            id = "item-1",
            name = "Aluguel",
            categoryId = "cat-1",
            categoryName = "Moradia",
            categoryIconName = "Home",
            categoryColorHex = "#34D399"
        ),
        ExpenseItem(
            id = "item-2",
            name = "Internet Fibra",
            categoryId = "cat-2",
            categoryName = "Utilidades",
            categoryIconName = "ReceiptLong",
            categoryColorHex = "#60A5FA"
        )
    )

    private val sampleInstallments = listOf(
        BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Aluguel Residencial",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 250000L,
            dueDate = 1759276800000L,
            categoryName = "Moradia",
            categoryIconName = "Home",
            categoryColorHex = "#34D399",
            type = BillType.SINGLE
        ),
        BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Notebook Dell",
            installmentNumber = 3,
            totalInstallments = 10,
            amountCents = 45000L,
            dueDate = 1759363200000L,
            categoryName = "Tecnologia",
            categoryIconName = "ReceiptLong",
            categoryColorHex = "#A78BFA",
            type = BillType.INSTALLMENT
        )
    )

    @Test
    fun billsScreen_renderFilterBarAndList_inAmoledTheme() {
        val grouped = mapOf("Outubro 2026" to sampleInstallments)

        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                Column {
                    BillsFilterBar(
                        periodFilter = BillPeriodFilter.ALL,
                        typeFilter = null,
                        statusFilter = null,
                        hasActiveFilters = false,
                        onOpenFilterBottomSheet = {},
                        onResetFilters = {}
                    )

                    BillsInstallmentList(
                        groupedByMonth = grouped,
                        expandedMonths = setOf("Outubro 2026"),
                        currentMonthLabel = "Outubro 2026",
                        isSelectionMode = false,
                        selectedInstallmentIds = emptySet(),
                        onToggleMonthExpand = {},
                        onToggleAllMonths = {},
                        onToggleSelect = {},
                        onLongClickSelect = {},
                        onTogglePayment = {},
                        onSelectInstallment = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Mês").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tipo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Filtros").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aluguel Residencial").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notebook Dell").assertIsDisplayed()
    }

    @Test
    fun quickExpenseBottomSheet_renderInAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                QuickExpenseBottomSheet(
                    expenseItems = sampleItems,
                    onDismiss = {},
                    onConfirm = { _, _, _, _ -> },
                    onMoreDetails = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Lançamento Rápido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aluguel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Internet Fibra").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amanhã").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dia 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Já paga").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mais detalhes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Informe o valor para salvar").assertIsDisplayed()
    }

    @Test
    fun quickExpenseBottomSheet_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highDensity) {
                PlatformTheme {
                    QuickExpenseBottomSheet(
                        expenseItems = sampleItems,
                        onDismiss = {},
                        onConfirm = { _, _, _, _ -> },
                        onMoreDetails = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Lançamento Rápido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mais detalhes").assertIsDisplayed()
    }
}
