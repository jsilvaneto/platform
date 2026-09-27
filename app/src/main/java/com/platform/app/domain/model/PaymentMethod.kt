package com.platform.app.domain.model

data class PaymentMethod(
    val id: String,
    val name: String,
    val iconName: String = "credit_card"
)
