package com.platform.app.domain.model

enum class ExpenseNature(val displayName: String, val colorHex: String) {
    OBRIGATORIO("Obrigatório", "#EF4444"),
    NECESSARIO("Necessário", "#F59E0B"),
    DESEJA("Deseja", "#3B82F6"),
    NENHUM("Nenhum", "#64748B")
}
