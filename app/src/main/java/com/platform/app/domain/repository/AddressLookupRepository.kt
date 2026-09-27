package com.platform.app.domain.repository

data class AddressInfo(
    val street: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = ""
)

interface AddressLookupRepository {
    suspend fun lookupCep(cep: String): AddressInfo?
}
