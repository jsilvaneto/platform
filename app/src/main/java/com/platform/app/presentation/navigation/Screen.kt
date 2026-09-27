package com.platform.app.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard_screen", "Dashboard")
    object Bills : Screen("bills_screen", "Contas")
    object Contacts : Screen("contacts_screen", "Contatos")
    object Management : Screen("management_screen", "Cadastros")
    object ContactDetail : Screen("contact_detail/{contactId}", "Detalhe do Contato") {
        fun createRoute(contactId: String) = "contact_detail/$contactId"
    }
}
