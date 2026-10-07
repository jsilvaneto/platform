package com.platform.app.domain.model

enum class ContactType(val displayName: String) {
    PESSOA_FISICA("Pessoa Física"),
    FORNECEDOR("Fornecedor"),
    ORGAO_PUBLICO("Órgão Público");

    companion object {
        fun fromString(value: String?): ContactType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: FORNECEDOR
        }
    }
}
