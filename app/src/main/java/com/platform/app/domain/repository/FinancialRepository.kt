package com.platform.app.domain.repository

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
    suspend fun seedInitialCategoriesIfEmpty()

    fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>>
    fun getAllInstallments(): Flow<List<BillInstallment>>

    suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>)
    suspend fun toggleInstallmentPayment(installmentId: String, isPaid: Boolean, paidTimestamp: Long? = null)
    suspend fun deleteBill(billId: String)
}
