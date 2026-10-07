package com.platform.app.domain.model

enum class FinancialAccountType(val displayName: String) {
    CORRENTE("Conta Corrente"),
    CARTEIRA("Carteira / Dinheiro"),
    POUPANCA("Poupança"),
    INVESTIMENTO("Investimento");

    companion object {
        fun fromString(value: String?): FinancialAccountType {
            if (value.isNullOrBlank()) return CORRENTE
            return when (value.trim().uppercase()) {
                "CORRENTE", "CONTA CORRENTE", "CHECKING", "CREDIT_CARD", "CARTÃO DE CRÉDITO", "OUTRO" -> CORRENTE
                "CARTEIRA", "DINHEIRO", "CASH", "CARTEIRA / DINHEIRO", "DINHEIRO / CARTEIRA" -> CARTEIRA
                "POUPANCA", "POUPANÇA", "SAVINGS" -> POUPANCA
                "INVESTIMENTO", "INVESTMENT", "INVESTIMENTO / RESERVA", "RESERVA", "RESERVA DE EMERGÊNCIA", "RESERVA DE EMERGENCIA" -> INVESTIMENTO
                else -> entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: CORRENTE
            }
        }
    }
}
