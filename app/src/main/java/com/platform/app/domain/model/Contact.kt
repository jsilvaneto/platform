package com.platform.app.domain.model

data class Contact(
    val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "Brasil",
    val zipCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullAddress: String
        get() {
            val streetWithNumber = buildString {
                if (street.isNotBlank()) append(street)
                if (number.isNotBlank()) append(", nº $number")
                if (complement.isNotBlank()) append(" ($complement)")
            }
            val parts = listOfNotNull(
                streetWithNumber.takeIf { it.isNotBlank() },
                neighborhood.takeIf { it.isNotBlank() },
                city.takeIf { it.isNotBlank() },
                state.takeIf { it.isNotBlank() },
                country.takeIf { it.isNotBlank() },
                zipCode.takeIf { it.isNotBlank() }?.let { "CEP $it" }
            )
            return if (parts.isEmpty()) "Endereço não informado" else parts.joinToString(", ")
        }
}
