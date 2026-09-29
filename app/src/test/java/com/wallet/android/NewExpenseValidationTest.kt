package com.wallet.android

import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.presentation.bills.NewExpenseUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewExpenseValidationTest {

    @Test
    fun `should require both expense item and contact as mandatory fields`() {
        val baseState = NewExpenseUiState(
            description = "Energia Elétrica",
            amountCents = 25000L, // R$ 250,00
            dueDate = 1727611200000L,
            selectedItemId = null,
            selectedContactId = null
        )

        // 1. Sem item e sem contato -> Inválido
        assertFalse("Sem item e contato deve ser inválido", baseState.isValid)

        // 2. Com item mas sem contato -> Inválido
        val withItemOnly = baseState.copy(selectedItemId = "item-1")
        assertFalse("Apenas com item deve ser inválido", withItemOnly.isValid)

        // 3. Com contato mas sem item -> Inválido
        val withContactOnly = baseState.copy(selectedContactId = "contact-1")
        assertFalse("Apenas com contato deve ser inválido", withContactOnly.isValid)

        // 4. Com ambos preenchidos -> Válido
        val withBoth = baseState.copy(
            selectedItemId = "item-1",
            selectedContactId = "contact-1"
        )
        assertTrue("Com item e contato preenchidos deve ser válido", withBoth.isValid)
    }

    @Test
    fun `should require credit card selection when credit card payment is active`() {
        val validBase = NewExpenseUiState(
            description = "Supermercado",
            amountCents = 15000L,
            selectedItemId = "item-mercado",
            selectedContactId = "contact-mercado",
            isCreditCard = true,
            selectedCreditCardId = null
        )

        // Com cartão ativo mas sem selecionar qual cartão -> Inválido
        assertFalse(validBase.isValid)

        // Ao selecionar o cartão -> Válido
        val withCard = validBase.copy(selectedCreditCardId = "card-nubank")
        assertTrue(withCard.isValid)
    }

    @Test
    fun `should reject invalid description or non positive amount`() {
        val state = NewExpenseUiState(
            description = "  ",
            amountCents = 0L,
            selectedItemId = "item-1",
            selectedContactId = "contact-1"
        )
        assertFalse(state.isValid)

        val withDescOnly = state.copy(description = "Farmácia", amountCents = 0L)
        assertFalse(withDescOnly.isValid)

        val withAmountOnly = state.copy(description = "Farmácia", amountCents = 5000L)
        assertTrue(withAmountOnly.isValid)
    }

    @Test
    fun `should calculate preview amount for installment purchase with exact cents`() {
        val totalAmount = 10000L // R$ 100,00
        val installmentsCount = 3

        val state = NewExpenseUiState(
            description = "Celular",
            amountCents = totalAmount,
            selectedItemId = "item-eletronico",
            selectedContactId = "contact-loja",
            expenseType = BillType.INSTALLMENT,
            installmentsCount = installmentsCount
        )

        // Divisão exata com resto na primeira parcela
        val baseAmount = totalAmount / installmentsCount
        val remainder = totalAmount % installmentsCount
        val installment1 = baseAmount + remainder
        val installment2 = baseAmount
        val installment3 = baseAmount

        assertEquals(3334L, installment1)
        assertEquals(3333L, installment2)
        assertEquals(3333L, installment3)
        assertEquals(totalAmount, installment1 + installment2 + installment3)

        // Preview de valor no State
        assertEquals(3333L, state.installmentPreviewAmount)
    }

    @Test
    fun `should inherit ExpenseNature directly from selected expense item`() {
        val itemObrigatorio = ExpenseItem(
            id = "item-aluguel",
            name = "Aluguel Residencial",
            categoryId = "cat-moradia",
            categoryName = "Moradia",
            nature = ExpenseNature.OBRIGATORIO
        )

        val state = NewExpenseUiState(
            description = "Aluguel Outubro",
            amountCents = 150000L,
            selectedItemId = "item-aluguel",
            selectedContactId = "contact-imobiliaria",
            allExpenseItems = listOf(itemObrigatorio)
        )

        assertEquals(ExpenseNature.OBRIGATORIO, state.inheritedNature)
    }

    @Test
    fun `should permit empty description when expense item is selected and use item as effective title`() {
        val item = ExpenseItem(
            id = "item-mercado",
            name = "Compras de Supermercado",
            categoryId = "cat-alim",
            categoryName = "Alimentação",
            nature = ExpenseNature.NECESSARIO
        )

        val stateWithoutDescription = NewExpenseUiState(
            description = "",
            amountCents = 15000L,
            selectedItemId = "item-mercado",
            selectedContactId = "contact-1",
            allExpenseItems = listOf(item)
        )

        assertTrue("Mesmo com descrição vazia, deve ser válido pois o item foi selecionado", stateWithoutDescription.isValid)
        assertEquals("Compras de Supermercado", stateWithoutDescription.effectiveTitle)
    }
}
