package com.platform.app.domain.model

data class CardDependencies(
    val invoiceCount: Int,
    val installmentCount: Int,
    val hasActiveDependencies: Boolean
)
