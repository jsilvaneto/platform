package com.platform.app.domain.usecase

import com.platform.app.domain.repository.AddressInfo
import com.platform.app.domain.repository.AddressLookupRepository
import javax.inject.Inject

class LookupAddressByCepUseCase @Inject constructor(
    private val repository: AddressLookupRepository
) {
    suspend operator fun invoke(cep: String): AddressInfo? {
        val clean = cep.replace(Regex("[^0-9]"), "")
        if (clean.length != 8) return null
        return repository.lookupCep(clean)
    }
}
