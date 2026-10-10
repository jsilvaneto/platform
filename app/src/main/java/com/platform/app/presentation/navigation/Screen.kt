package com.platform.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard_screen", "Hoje")
    object Bills : Screen("bills_screen", "Contas")
    object RecurringInstallments : Screen("recurring_installments_screen", "Recorrentes e parceladas")
    object CreditCards : Screen("credit_cards_screen", "Cartões")
    object Statistics : Screen("statistics_screen", "Análises")
    object Goals : Screen("goals_screen", "Metas")
    object Budgets : Screen("budgets_screen", "Orçamentos")
    object Contacts : Screen("contacts_screen", "Contatos")
    object Accounts : Screen("accounts_screen", "Contas Bancárias")
    object PaymentMethods : Screen("payment_methods_screen", "Formas de Pagamento")
    object Categories : Screen("categories_screen", "Categorias")
    object ExpenseItems : Screen("expense_items_screen", "Itens")
    object Management : Screen("management_screen", "Cadastros")
    object Settings : Screen("settings_screen", "Configurações")
    object More : Screen("more_screen", "Mais")
    object NewExpense : Screen("new_expense_screen?duplicateBillId={duplicateBillId}", "Nova Despesa") {
        fun createRoute(duplicateBillId: String? = null): String {
            return if (duplicateBillId != null) "new_expense_screen?duplicateBillId=$duplicateBillId" else "new_expense_screen"
        }
    }
    object Home : Screen("dashboard_screen", "Hoje")
    object ContactDetail : Screen("contact_detail/{contactId}", "Detalhe do Contato") {
        fun createRoute(contactId: String) = "contact_detail/$contactId"
    }
}
