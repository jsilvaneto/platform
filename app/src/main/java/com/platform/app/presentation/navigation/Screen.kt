package com.platform.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Overview : Screen("overview_screen", "Visão geral")
    object Dashboard : Screen("dashboard_screen", "Hoje")
    object Bills : Screen("bills_screen", "Contas")
    object CreditCards : Screen("credit_cards_screen", "Cartões")
    object Statistics : Screen("statistics_screen", "Análises")
    object Goals : Screen("goals_screen", "Metas")
    object Budgets : Screen("budgets_screen", "Orçamentos")
    object Contacts : Screen("contacts_screen", "Contatos")
    object Management : Screen("management_screen?tab={tab}", "Cadastros") {
        fun createRoute(tab: Int = 0): String = "management_screen?tab=$tab"
    }
    object Settings : Screen("settings_screen", "Configurações")
    object NewExpense : Screen("new_expense_screen?duplicateBillId={duplicateBillId}", "Nova Despesa") {
        fun createRoute(duplicateBillId: String? = null): String {
            return if (duplicateBillId != null) "new_expense_screen?duplicateBillId=$duplicateBillId" else "new_expense_screen"
        }
    }
    object ContactDetail : Screen("contact_detail/{contactId}", "Detalhe do Contato") {
        fun createRoute(contactId: String) = "contact_detail/$contactId"
    }
}
