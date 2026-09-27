package com.platform.app.domain.model

data class FinancialAccount(
    val id: String,
    val name: String,
    val accountType: String = "Corrente", // Corrente, Carteira, Poupança, Investimento
    val colorHex: String = "#2563EB"
)
