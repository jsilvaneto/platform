package com.platform.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard_screen", "Início")
    object Bills : Screen("bills_screen", "Registros")
    object RecurringInstallments : Screen("recurring_installments_screen", "Recorrentes e Parcelados")
    object Statistics : Screen("statistics_screen", "Estatísticas")
    object Goals : Screen("goals_screen", "Metas")
    object Budgets : Screen("budgets_screen", "Orçamentos")
    object Contacts : Screen("contacts_screen", "Contatos")
    object Accounts : Screen("accounts_screen", "Contas")
    object PaymentMethods : Screen("payment_methods_screen", "Formas de Pagamento")
    object Categories : Screen("categories_screen", "Categorias")
    object Management : Screen("management_screen", "Estrutura Financeira")
    object Settings : Screen("settings_screen", "Configurações")
    object ContactDetail : Screen("contact_detail/{contactId}", "Detalhe do Contato") {
        fun createRoute(contactId: String) = "contact_detail/$contactId"
    }
}
