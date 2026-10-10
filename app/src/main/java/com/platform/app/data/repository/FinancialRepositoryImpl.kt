package com.platform.app.data.repository

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
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada consolidada do [FinancialRepository] que delega para os data sources
 * especializados por agregado:
 * - [BillDataSource]: Contas e Parcelas
 * - [CreditCardDataSource]: Cartões de Crédito e Faturas
 * - [CatalogDataSource]: Categorias, Itens, Contatos, Contas Bancárias e Métodos de Pagamento
 */
@Singleton
class FinancialRepositoryImpl @Inject constructor(
    private val billDataSource: BillDataSource,
    private val creditCardDataSource: CreditCardDataSource,
    private val catalogDataSource: CatalogDataSource
) : FinancialRepository {

    // --- Startup Seeds ---
    override suspend fun seedInitialData() =
        catalogDataSource.seedInitialData()

    // --- Categories ---
    override fun getCategories(): Flow<List<Category>> =
        catalogDataSource.getCategories()

    override suspend fun saveCategory(category: Category) =
        catalogDataSource.saveCategory(category)

    override suspend fun deleteCategory(categoryId: String) =
        catalogDataSource.deleteCategory(categoryId)

    // --- Expense Items ---
    override fun getExpenseItems(): Flow<List<ExpenseItem>> =
        catalogDataSource.getExpenseItems()

    override fun getExpenseItemsByCategory(categoryId: String): Flow<List<ExpenseItem>> =
        catalogDataSource.getExpenseItemsByCategory(categoryId)

    override suspend fun saveExpenseItem(item: ExpenseItem) =
        catalogDataSource.saveExpenseItem(item)

    override suspend fun deleteExpenseItem(itemId: String) =
        catalogDataSource.deleteExpenseItem(itemId)

    // --- Credit Cards & Invoices ---
    override fun getCreditCards(): Flow<List<CreditCard>> =
        creditCardDataSource.getCreditCards()

    override suspend fun saveCreditCard(card: CreditCard) =
        creditCardDataSource.saveCreditCard(card)

    override suspend fun deleteCreditCard(cardId: String) =
        creditCardDataSource.deleteCreditCard(cardId)

    override fun getCreditCardInvoices(cardId: String): Flow<List<CreditCardInvoice>> =
        creditCardDataSource.getCreditCardInvoices(cardId)

    override fun getAllCreditCardInvoices(): Flow<List<CreditCardInvoice>> =
        creditCardDataSource.getAllCreditCardInvoices()

    override fun getInvoicesForPeriod(startMillis: Long, endMillis: Long): Flow<List<CreditCardInvoice>> =
        creditCardDataSource.getInvoicesForPeriod(startMillis, endMillis)

    override fun getInstallmentsForInvoice(invoiceId: String): Flow<List<BillInstallment>> =
        creditCardDataSource.getInstallmentsForInvoice(invoiceId)

    override suspend fun getOrCreateInvoiceForMonth(cardId: String, referenceMonth: String): CreditCardInvoice =
        creditCardDataSource.getOrCreateInvoiceForMonth(cardId, referenceMonth)

    override suspend fun materializeRecurringForInvoice(cardId: String, invoiceId: String, referenceMonth: String): Int =
        creditCardDataSource.materializeRecurringForInvoice(cardId, invoiceId, referenceMonth)

    override suspend fun materializeRecurringCardInvoices(referenceTimeMillis: Long): Int =
        creditCardDataSource.materializeRecurringCardInvoices(referenceTimeMillis)

    override suspend fun payInvoice(invoiceId: String, actualPaymentDate: Long?) =
        creditCardDataSource.payInvoice(invoiceId, actualPaymentDate)

    override suspend fun reopenInvoice(invoiceId: String) =
        creditCardDataSource.reopenInvoice(invoiceId)

    override suspend fun getCardDependencies(cardId: String): CardDependencies =
        creditCardDataSource.getCardDependencies(cardId)

    // --- Contacts ---
    override fun getContacts(): Flow<List<Contact>> =
        catalogDataSource.getContacts()

    override fun getContactById(contactId: String): Flow<Contact?> =
        catalogDataSource.getContactById(contactId)

    override suspend fun saveContact(contact: Contact) =
        catalogDataSource.saveContact(contact)

    override suspend fun deleteContact(contactId: String) =
        catalogDataSource.deleteContact(contactId)

    // --- Financial Accounts ---
    override fun getFinancialAccounts(): Flow<List<FinancialAccount>> =
        catalogDataSource.getFinancialAccounts()

    override suspend fun saveFinancialAccount(account: FinancialAccount) =
        catalogDataSource.saveFinancialAccount(account)

    override suspend fun deleteFinancialAccount(accountId: String) =
        catalogDataSource.deleteFinancialAccount(accountId)

    // --- Payment Methods ---
    override fun getPaymentMethods(): Flow<List<PaymentMethod>> =
        catalogDataSource.getPaymentMethods()

    override suspend fun savePaymentMethod(method: PaymentMethod) =
        catalogDataSource.savePaymentMethod(method)

    override suspend fun deletePaymentMethod(methodId: String) =
        catalogDataSource.deletePaymentMethod(methodId)

    // --- Bills & Installments ---
    override fun getBills(): Flow<List<Bill>> =
        billDataSource.getBills()

    override suspend fun getBillById(billId: String): Bill? =
        billDataSource.getBillById(billId)

    override fun getBillsByContact(contactId: String): Flow<List<Bill>> =
        billDataSource.getBillsByContact(contactId)

    override fun getInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> =
        billDataSource.getInstallmentsByContact(contactId)

    override fun getPlannedInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> =
        billDataSource.getPlannedInstallmentsByContact(contactId)

    override fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>> =
        billDataSource.getInstallmentsForPeriod(startMillis, endMillis)

    override fun getAllInstallments(): Flow<List<BillInstallment>> =
        billDataSource.getAllInstallments()

    override suspend fun getInstallmentsByBillId(billId: String): List<BillInstallment> =
        billDataSource.getInstallmentsByBillId(billId)

    override suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>) =
        billDataSource.saveBillWithInstallments(bill, installments)

    override suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>) =
        billDataSource.addInstallments(bill, installments)

    override suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?,
        actualPaymentDate: Long?
    ) = billDataSource.toggleInstallmentPayment(installmentId, isPaid, paidTimestamp, actualPaymentDate)

    override suspend fun updateInstallmentsActualPaymentDateBatch(
        installmentIds: List<String>,
        actualPaymentDate: Long
    ) = billDataSource.updateInstallmentsActualPaymentDateBatch(installmentIds, actualPaymentDate)

    override suspend fun updateInstallment(installmentId: String, newAmountCents: Long, newDueDate: Long) =
        billDataSource.updateInstallment(installmentId, newAmountCents, newDueDate)

    override suspend fun updateBillAndInstallment(
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
    ) = billDataSource.updateBillAndInstallment(
        installmentId, billId, title, description, amountCents, dueDate,
        categoryId, itemId, contactId, financialAccountId, paymentMethodId
    )

    override suspend fun updateFutureInstallmentsAmount(billId: String, fromDueDate: Long, newAmountCents: Long) =
        billDataSource.updateFutureInstallmentsAmount(billId, fromDueDate, newAmountCents)

    override suspend fun deleteSingleInstallment(installmentId: String) =
        billDataSource.deleteSingleInstallment(installmentId)

    override suspend fun deleteFutureInstallments(billId: String, fromDueDate: Long) =
        billDataSource.deleteFutureInstallments(billId, fromDueDate)

    override suspend fun pauseRecurringBill(billId: String, isPaused: Boolean) =
        billDataSource.pauseRecurringBill(billId, isPaused)

    override suspend fun stopRecurringBill(billId: String) =
        billDataSource.stopRecurringBill(billId)

    override suspend fun deleteBill(billId: String) =
        billDataSource.deleteBill(billId)
}
