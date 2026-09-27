package com.platform.app.data.repository

import com.platform.app.data.remote.CepLookupService
import com.platform.app.domain.repository.AddressInfo
import com.platform.app.domain.repository.AddressLookupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressLookupRepositoryImpl @Inject constructor(
    private val cepLookupService: CepLookupService
) : AddressLookupRepository {

    override suspend fun lookupCep(cep: String): AddressInfo? {
        val result = cepLookupService.lookupCep(cep) ?: return null
        return AddressInfo(
            street = result.street,
            neighborhood = result.neighborhood,
            city = result.city,
            state = result.state
        )
    }
}
