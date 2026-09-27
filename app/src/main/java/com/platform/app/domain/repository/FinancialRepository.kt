package com.platform.app.domain.repository

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
    suspend fun seedInitialCategoriesIfEmpty()

    // Subcategories
    fun getSubcategories(categoryId: String): Flow<List<Subcategory>>
    fun getAllSubcategories(): Flow<List<Subcategory>>
    suspend fun saveSubcategory(subcategory: Subcategory)
    suspend fun deleteSubcategory(subcategoryId: String)

    // Contacts
    fun getContacts(): Flow<List<Contact>>
    fun getContactById(contactId: String): Flow<Contact?>
    suspend fun saveContact(contact: Contact)
    suspend fun deleteContact(contactId: String)

    // Financial Accounts
    fun getFinancialAccounts(): Flow<List<FinancialAccount>>
    suspend fun saveFinancialAccount(account: FinancialAccount)
    suspend fun deleteFinancialAccount(accountId: String)
    suspend fun seedInitialFinancialAccountsIfEmpty()

    // Payment Methods
    fun getPaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun savePaymentMethod(method: PaymentMethod)
    suspend fun deletePaymentMethod(methodId: String)
    suspend fun seedInitialPaymentMethodsIfEmpty()

    // Bills & Installments
    fun getBills(): Flow<List<Bill>>
    fun getBillsByContact(contactId: String): Flow<List<Bill>>
    fun getInstallmentsByContact(contactId: String): Flow<List<BillInstallment>>
    fun getPlannedInstallmentsByContact(contactId: String): Flow<List<BillInstallment>>
    fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>>
    fun getAllInstallments(): Flow<List<BillInstallment>>

    suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>)
    suspend fun toggleInstallmentPayment(installmentId: String, isPaid: Boolean, paidTimestamp: Long? = null)
    suspend fun deleteBill(billId: String)
}
