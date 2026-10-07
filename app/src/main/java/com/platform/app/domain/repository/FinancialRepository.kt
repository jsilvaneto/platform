package com.platform.app.domain.repository

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CardDependencies
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    // Initial Data Seed (Startup)
    suspend fun seedInitialData()

    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
    suspend fun seedInitialCategoriesIfEmpty()

    // Expense Items (replaces Subcategories)
    fun getExpenseItems(): Flow<List<ExpenseItem>>
    fun getExpenseItemsByCategory(categoryId: String): Flow<List<ExpenseItem>>
    suspend fun saveExpenseItem(item: ExpenseItem)
    suspend fun deleteExpenseItem(itemId: String)
    suspend fun seedInitialExpenseItemsIfEmpty()

    // Credit Cards & Invoices
    fun getCreditCards(): Flow<List<CreditCard>>
    suspend fun saveCreditCard(card: CreditCard)
    suspend fun deleteCreditCard(cardId: String)

    fun getCreditCardInvoices(cardId: String): Flow<List<CreditCardInvoice>>
    fun getAllCreditCardInvoices(): Flow<List<CreditCardInvoice>>
    fun getInvoicesForPeriod(startMillis: Long, endMillis: Long): Flow<List<CreditCardInvoice>>
    fun getInstallmentsForInvoice(invoiceId: String): Flow<List<BillInstallment>>
    suspend fun getOrCreateInvoiceForMonth(cardId: String, referenceMonth: String): CreditCardInvoice
    suspend fun payInvoice(invoiceId: String)
    suspend fun reopenInvoice(invoiceId: String)
    suspend fun getCardDependencies(cardId: String): CardDependencies

    // Contacts
    fun getContacts(): Flow<List<Contact>>
    fun getContactById(contactId: String): Flow<Contact?>
    suspend fun saveContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
    suspend fun seedInitialContactsIfEmpty()

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
    suspend fun getBillById(billId: String): Bill?
    fun getBillsByContact(contactId: String): Flow<List<Bill>>
    fun getInstallmentsByContact(contactId: String): Flow<List<BillInstallment>>
    fun getPlannedInstallmentsByContact(contactId: String): Flow<List<BillInstallment>>
    fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>>
    fun getAllInstallments(): Flow<List<BillInstallment>>
    suspend fun getInstallmentsByBillId(billId: String): List<BillInstallment>

    suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>)
    suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>)
    suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long? = null,
        actualPaymentDate: Long? = null
    )
    suspend fun updateInstallment(installmentId: String, newAmountCents: Long, newDueDate: Long)
    suspend fun updateBillAndInstallment(
        installmentId: String,
        billId: String,
        title: String,
        description: String,
        amountCents: Long,
        dueDate: Long,
        categoryId: String?,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?
    )
    suspend fun updateFutureInstallmentsAmount(billId: String, fromDueDate: Long, newAmountCents: Long)
    suspend fun deleteSingleInstallment(installmentId: String)
    suspend fun deleteFutureInstallments(billId: String, fromDueDate: Long)
    suspend fun pauseRecurringBill(billId: String, isPaused: Boolean)
    suspend fun stopRecurringBill(billId: String)
    suspend fun deleteBill(billId: String)
}
