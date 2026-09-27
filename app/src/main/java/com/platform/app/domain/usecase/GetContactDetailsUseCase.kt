package com.platform.app.domain.usecase

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Contact
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ContactDetails(
    val contact: Contact,
    val bills: List<Bill>,
    val plannedInstallments: List<BillInstallment>,
    val totalPendingAmountCents: Long,
    val totalPaidAmountCents: Long
)

class GetContactDetailsUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    operator fun invoke(contactId: String): Flow<ContactDetails?> {
        return combine(
            repository.getContactById(contactId),
            repository.getBillsByContact(contactId),
            repository.getPlannedInstallmentsByContact(contactId),
            repository.getInstallmentsByContact(contactId)
        ) { contact, bills, plannedInstallments, allInstallments ->
            if (contact == null) null
            else {
                val pending = allInstallments.filter { it.paidAt == null }.sumOf { it.amountCents }
                val paid = allInstallments.filter { it.paidAt != null }.sumOf { it.amountCents }
                ContactDetails(
                    contact = contact,
                    bills = bills,
                    plannedInstallments = plannedInstallments,
                    totalPendingAmountCents = pending,
                    totalPaidAmountCents = paid
                )
            }
        }
    }
}
