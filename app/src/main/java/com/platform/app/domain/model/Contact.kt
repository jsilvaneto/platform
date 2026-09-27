package com.platform.app.domain.model

data class Contact(
    val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "Brasil",
    val zipCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullAddress: String
        get() {
            val parts = listOfNotNull(
                street.takeIf { it.isNotBlank() },
                city.takeIf { it.isNotBlank() },
                state.takeIf { it.isNotBlank() },
                country.takeIf { it.isNotBlank() },
                zipCode.takeIf { it.isNotBlank() }?.let { "CEP $it" }
            )
            return if (parts.isEmpty()) "Endereço não informado" else parts.joinToString(", ")
        }
}
