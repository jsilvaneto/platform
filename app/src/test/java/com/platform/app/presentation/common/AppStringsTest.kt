package com.platform.app.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStringsTest {

    @Test
    fun `status strings match established UI terms`() {
        assertEquals("Pendente", AppStrings.Status.PENDING)
        assertEquals("Pago", AppStrings.Status.PAID)
        assertEquals("Paga", AppStrings.Status.PAID_FEMALE)
        assertEquals("Já Paga", AppStrings.Status.ALREADY_PAID)
        assertEquals("A Pagar", AppStrings.Status.TO_PAY)
        assertEquals("Vencida", AppStrings.Status.OVERDUE)
        assertEquals("Vencido", AppStrings.Status.OVERDUE_MALE)
        assertEquals("Atrasado", AppStrings.Status.OVERDUE_PAST)
        assertEquals("Liquidado", AppStrings.Status.SETTLED)
        assertEquals("Tudo quitado", AppStrings.Status.ALL_PAID)
        assertEquals("100% quitado", AppStrings.Status.ALL_PAID_PERCENT)
        assertEquals("Aberta", AppStrings.Status.OPEN)
        assertEquals("Fechada", AppStrings.Status.CLOSED)
        assertEquals("Pausada", AppStrings.Status.PAUSED)
    }

    @Test
    fun `nature strings match 50-30-20 domain standards`() {
        assertEquals("Obrigatório", AppStrings.Nature.MANDATORY)
        assertEquals("Necessário", AppStrings.Nature.NECESSARY)
        assertEquals("Desejo", AppStrings.Nature.WANTS)
        assertEquals("Nenhum", AppStrings.Nature.NONE)
        assertEquals("Poupança", AppStrings.Nature.SAVINGS)
        assertEquals("Natureza do Gasto", AppStrings.Nature.LABEL_NATURE)
        assertEquals("Natureza do Gasto:", AppStrings.Nature.LABEL_NATURE_COLON)
        assertTrue(AppStrings.Nature.DESC_MANDATORY.isNotBlank())
        assertTrue(AppStrings.Nature.DESC_NECESSARY.isNotBlank())
        assertTrue(AppStrings.Nature.DESC_WANTS.isNotBlank())
        assertTrue(AppStrings.Nature.DESC_NONE.isNotBlank())
    }

    @Test
    fun `account type strings match financial account models`() {
        assertEquals("Conta Corrente", AppStrings.AccountType.CHECKING)
        assertEquals("Carteira / Dinheiro", AppStrings.AccountType.WALLET)
        assertEquals("Poupança", AppStrings.AccountType.SAVINGS)
        assertEquals("Investimento", AppStrings.AccountType.INVESTMENT)
    }

    @Test
    fun `contact type strings match contact categorization models`() {
        assertEquals("Pessoa Física", AppStrings.ContactType.INDIVIDUAL)
        assertEquals("Fornecedor", AppStrings.ContactType.SUPPLIER)
        assertEquals("Órgão Público", AppStrings.ContactType.PUBLIC_ENTITY)
        assertEquals("Pessoa", AppStrings.ContactType.INDIVIDUAL_SHORT)
        assertEquals("Empresa", AppStrings.ContactType.SUPPLIER_SHORT)
        assertEquals("Público", AppStrings.ContactType.PUBLIC_ENTITY_SHORT)
        assertEquals("Salvar Contato", AppStrings.ContactType.SAVE_CONTACT)
        assertEquals("Atualizar Contato", AppStrings.ContactType.UPDATE_CONTACT)
        assertEquals("Excluir Contato", AppStrings.ContactType.DELETE_CONTACT)
    }

    @Test
    fun `action strings match common interaction buttons`() {
        assertEquals("Salvar", AppStrings.Actions.SAVE)
        assertEquals("Salvar Alterações", AppStrings.Actions.SAVE_CHANGES)
        assertEquals("Atualizar", AppStrings.Actions.UPDATE)
        assertEquals("Cancelar", AppStrings.Actions.CANCEL)
        assertEquals("Excluir", AppStrings.Actions.DELETE)
        assertEquals("Editar", AppStrings.Actions.EDIT)
        assertEquals("Confirmar", AppStrings.Actions.CONFIRM)
        assertEquals("Fechar", AppStrings.Actions.CLOSE)
        assertEquals("Voltar", AppStrings.Actions.BACK)
        assertEquals("Filtrar", AppStrings.Actions.FILTER)
        assertEquals("Todos", AppStrings.Actions.ALL)
        assertEquals("Todas", AppStrings.Actions.ALL_FEMALE)
        assertEquals("Adicionar", AppStrings.Actions.ADD)
        assertEquals("Novo", AppStrings.Actions.NEW)
        assertEquals("Nova", AppStrings.Actions.NEW_FEMALE)
        assertEquals("Compartilhar", AppStrings.Actions.SHARE)
        assertEquals("Restaurar", AppStrings.Actions.RESTORE)
        assertEquals("Fazer Backup", AppStrings.Actions.BACKUP)
        assertEquals("Aplicar", AppStrings.Actions.APPLY)
        assertEquals("Limpar", AppStrings.Actions.CLEAR)
    }

    @Test
    fun `home dashboard labels communicate remaining forecast correctly`() {
        assertEquals("Restante a Pagar no Mês", AppStrings.Home.FORECAST_REMAINING)
        assertEquals("Total vencido:", AppStrings.Home.TOTAL_OVERDUE)
    }

    @Test
    fun `dialog titles are well defined`() {
        assertEquals("Confirmação", AppStrings.Dialogs.CONFIRM_TITLE)
        assertEquals("Excluir Registro", AppStrings.Dialogs.DELETE_REGISTRATION_TITLE)
        assertEquals("Excluir Contato", AppStrings.Dialogs.DELETE_CONTACT_TITLE)
        assertEquals("Atenção", AppStrings.Dialogs.WARNING)
    }
}
