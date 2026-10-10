package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CardDependencies
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.FinancialAccountType
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class FinancialRepositoryImpl @Inject constructor(
    private val database: PlatformDatabase,
    private val categoryDao: CategoryDao,
    private val expenseItemDao: ExpenseItemDao,
    private val creditCardDao: CreditCardDao,
    private val contactDao: ContactDao,
    private val financialAccountDao: FinancialAccountDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao,
    private val preferencesManager: PreferencesManager
) : FinancialRepository {

    override suspend fun seedInitialData() {
        val applied = preferencesManager.areSeedsApplied.first()
        if (applied) return

        if (categoryDao.count() > 0 || financialAccountDao.count() > 0) {
            preferencesManager.setSeedsApplied(true)
            return
        }

        database.withTransaction {
            val moradiaId = UUID.randomUUID().toString()
            val alimentacaoId = UUID.randomUUID().toString()
            val transporteId = UUID.randomUUID().toString()

            val defaultCategories = listOf(
                Category(id = moradiaId, name = "Moradia", colorHex = "#3B82F6", iconName = "home", nature = ExpenseNature.OBRIGATORIO),
                Category(id = alimentacaoId, name = "Alimentação", colorHex = "#10B981", iconName = "shopping_cart", nature = ExpenseNature.NECESSARIO),
                Category(id = transporteId, name = "Transporte", colorHex = "#F59E0B", iconName = "directions_car", nature = ExpenseNature.NECESSARIO),
                Category(id = UUID.randomUUID().toString(), name = "Assinaturas & Serviços", colorHex = "#8B5CF6", iconName = "subscriptions", nature = ExpenseNature.DESEJA),
                Category(id = UUID.randomUUID().toString(), name = "Saúde", colorHex = "#EF4444", iconName = "medical_services", nature = ExpenseNature.OBRIGATORIO),
                Category(id = UUID.randomUUID().toString(), name = "Lazer", colorHex = "#EC4899", iconName = "sports_esports", nature = ExpenseNature.DESEJA),
                Category(id = UUID.randomUUID().toString(), name = "Educação", colorHex = "#6366F1", iconName = "school", nature = ExpenseNature.OBRIGATORIO),
                Category(id = UUID.randomUUID().toString(), name = "Outros", colorHex = "#64748B", iconName = "more_horiz", nature = ExpenseNature.NENHUM)
            )
            categoryDao.insertAll(defaultCategories.map { CategoryEntity.fromDomain(it) })

            val defaultItems = listOf(
                ExpenseItem(name = "Aluguel / Condomínio", categoryId = moradiaId),
                ExpenseItem(name = "Energia Elétrica", categoryId = moradiaId),
                ExpenseItem(name = "Água & Saneamento", categoryId = moradiaId),
                ExpenseItem(name = "Supermercado", categoryId = alimentacaoId),
                ExpenseItem(name = "Feira & Hortifruti", categoryId = alimentacaoId),
                ExpenseItem(name = "Combustível", categoryId = transporteId),
                ExpenseItem(name = "Manutenção Veicular", categoryId = transporteId)
            )
            expenseItemDao.insertAll(defaultItems.map { ExpenseItemEntity.fromDomain(it) })

            val defaultContacts = listOf(
                Contact(id = UUID.randomUUID().toString(), name = "Supermercado"),
                Contact(id = UUID.randomUUID().toString(), name = "Farmácia"),
                Contact(id = UUID.randomUUID().toString(), name = "Posto de Combustível"),
                Contact(id = UUID.randomUUID().toString(), name = "Restaurante"),
                Contact(id = UUID.randomUUID().toString(), name = "Internet / Telefonia"),
                Contact(id = UUID.randomUUID().toString(), name = "Energia Elétrica"),
                Contact(id = UUID.randomUUID().toString(), name = "Água e Saneamento"),
                Contact(id = UUID.randomUUID().toString(), name = "Diversos")
            )
            contactDao.insertAll(defaultContacts.map { ContactEntity.fromDomain(it) })

            val defaultAccounts = listOf(
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Conta Corrente", accountType = FinancialAccountType.CORRENTE, colorHex = "#3B82F6"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Carteira / Dinheiro", accountType = FinancialAccountType.CARTEIRA, colorHex = "#10B981"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Reserva de Emergência", accountType = FinancialAccountType.POUPANCA, colorHex = "#F59E0B"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Investimentos", accountType = FinancialAccountType.INVESTIMENTO, colorHex = "#8B5CF6")
            )
            financialAccountDao.insertAll(defaultAccounts.map { FinancialAccountEntity.fromDomain(it) })

            val defaultMethods = listOf(
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Pix", iconName = "qr_code"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Boleto", iconName = "receipt"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Cartão de Crédito", iconName = "credit_card"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Cartão de Débito", iconName = "credit_card"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Dinheiro", iconName = "payments"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Transferência Bancária", iconName = "account_balance")
            )
            paymentMethodDao.insertAll(defaultMethods.map { PaymentMethodEntity.fromDomain(it) })
        }

        preferencesManager.setSeedsApplied(true)
    }

    // --- Categories ---
    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCategory(category: Category) {
        database.withTransaction {
            categoryDao.upsert(CategoryEntity.fromDomain(category))
            database.budgetDao.updateCategoryInfo(category.id, category.name, category.colorHex)
        }
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteById(categoryId)
    }

    // --- Expense Items ---
    override fun getExpenseItems(): Flow<List<ExpenseItem>> {
        return combine(expenseItemDao.getAll(), getCategories()) { items, categories ->
            val categoryMap = categories.associateBy { it.id }
            items.map { item ->
                val cat = categoryMap[item.categoryId]
                item.toDomain(
                    categoryName = cat?.name ?: "Geral",
                    categoryColorHex = cat?.colorHex ?: "#64748B",
                    categoryIconName = cat?.iconName ?: "category",
                    nature = cat?.nature ?: ExpenseNature.NECESSARIO
                )
            }
        }
    }

    override fun getExpenseItemsByCategory(categoryId: String): Flow<List<ExpenseItem>> {
        return combine(expenseItemDao.getByCategoryId(categoryId), getCategories()) { items, categories ->
            val cat = categories.find { it.id == categoryId }
            items.map { item ->
                item.toDomain(
                    categoryName = cat?.name ?: "Geral",
                    categoryColorHex = cat?.colorHex ?: "#64748B",
                    categoryIconName = cat?.iconName ?: "category",
                    nature = cat?.nature ?: ExpenseNature.NECESSARIO
                )
            }
        }
    }

    override suspend fun saveExpenseItem(item: ExpenseItem) {
        expenseItemDao.upsert(ExpenseItemEntity.fromDomain(item))
    }

    override suspend fun deleteExpenseItem(itemId: String) {
        expenseItemDao.deleteById(itemId)
    }

    // --- Credit Cards & Invoices ---
    override fun getCreditCards(): Flow<List<CreditCard>> {
        return creditCardDao.getAllCards().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCreditCard(card: CreditCard) {
        creditCardDao.upsertCard(CreditCardEntity.fromDomain(card))
    }

    override suspend fun deleteCreditCard(cardId: String) {
        creditCardDao.deleteCardById(cardId)
    }

    override fun getCreditCardInvoices(cardId: String): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForCard(cardId), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    override fun getAllCreditCardInvoices(): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getAllInvoices(), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    override fun getInvoicesForPeriod(startMillis: Long, endMillis: Long): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForDueDateRange(startMillis, endMillis), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    override suspend fun getOrCreateInvoiceForMonth(cardId: String, referenceMonth: String): CreditCardInvoice {
        return database.withTransaction {
            val existing = creditCardDao.getInvoiceByMonth(cardId, referenceMonth)
            val invoice = if (existing != null) {
                existing.toDomain()
            } else {
                val card = creditCardDao.getCardById(cardId) ?: return@withTransaction CreditCardInvoice(
                    creditCardId = cardId,
                    referenceMonth = referenceMonth,
                    closingDate = System.currentTimeMillis(),
                    dueDate = System.currentTimeMillis()
                )

                val cal = Calendar.getInstance()
                val parts = referenceMonth.split("-")
                val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
                val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1

                cal.set(year, month, card.closingDay, 23, 59, 59)
                val closingDate = cal.timeInMillis

                cal.set(year, month, card.dueDay, 23, 59, 59)
                if (card.dueDay <= card.closingDay) {
                    cal.add(Calendar.MONTH, 1)
                }
                val dueDate = cal.timeInMillis

                val newInvoice = CreditCardInvoice(
                    creditCardId = cardId,
                    referenceMonth = referenceMonth,
                    closingDate = closingDate,
                    dueDate = dueDate,
                    status = InvoiceStatus.ABERTA
                )
                creditCardDao.insertInvoice(CreditCardInvoiceEntity.fromDomain(newInvoice))
                newInvoice
            }

            // Materialização sob demanda: anexar ocorrências RECURRING do cartão cujo ciclo cai neste referenceMonth
            val card = creditCardDao.getCardById(cardId)
            if (card != null) {
                val unattached = installmentDao.getUnattachedRecurringInstallmentsForCard(cardId)
                val matchingIds = unattached.filter { inst ->
                    CreditCardCalculator.determineInvoiceReferenceMonth(inst.dueDate, card.closingDay) == referenceMonth
                }.map { it.id }

                if (matchingIds.isNotEmpty()) {
                    installmentDao.attachInstallmentsToInvoice(
                        installmentIds = matchingIds,
                        invoiceId = invoice.id,
                        invoiceDueDate = invoice.dueDate
                    )
                }
            }

            invoice
        }
    }

    override suspend fun materializeRecurringCardInvoices(referenceTimeMillis: Long): Int {
        val cards = creditCardDao.getAllCardsList()
        var totalAttached = 0

        for (card in cards) {
            val currentRefMonth = CreditCardCalculator.determineInvoiceReferenceMonth(
                purchaseTimestamp = referenceTimeMillis,
                closingDay = card.closingDay
            )

            val unattached = installmentDao.getUnattachedRecurringInstallmentsForCard(card.id)
            if (unattached.isEmpty()) continue

            val byRefMonth = unattached.groupBy {
                CreditCardCalculator.determineInvoiceReferenceMonth(it.dueDate, card.closingDay)
            }

            for ((refMonth, installmentsInCycle) in byRefMonth) {
                if (refMonth <= currentRefMonth) {
                    getOrCreateInvoiceForMonth(card.id, refMonth)
                    totalAttached += installmentsInCycle.size
                }
            }
        }

        return totalAttached
    }

    override suspend fun payInvoice(invoiceId: String, actualPaymentDate: Long?) {
        val now = System.currentTimeMillis()
        val effectiveActualDate = actualPaymentDate ?: now
        database.withTransaction {
            creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.PAGA.name)
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = invoiceId,
                paidAt = now,
                actualPaymentDate = effectiveActualDate,
                status = "PAID"
            )
        }
    }

    override suspend fun reopenInvoice(invoiceId: String) {
        database.withTransaction {
            creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.ABERTA.name)
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = invoiceId,
                paidAt = null,
                actualPaymentDate = null,
                status = "PENDING"
            )
        }
    }

    override fun getInstallmentsForInvoice(invoiceId: String): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsForInvoice(invoiceId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCardDependencies(cardId: String): CardDependencies {
        val invCount = creditCardDao.countInvoicesForCard(cardId)
        val instCount = installmentDao.countInstallmentsForCard(cardId)
        return CardDependencies(
            invoiceCount = invCount,
            installmentCount = instCount,
            hasActiveDependencies = invCount > 0 || instCount > 0
        )
    }

    // --- Contacts ---
    override fun getContacts(): Flow<List<Contact>> {
        return contactDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getContactById(contactId: String): Flow<Contact?> {
        return contactDao.getByIdFlow(contactId).map { it?.toDomain() }
    }

    override suspend fun saveContact(contact: Contact) {
        contactDao.upsert(ContactEntity.fromDomain(contact))
    }

    override suspend fun deleteContact(contactId: String) {
        contactDao.deleteById(contactId)
    }

    // --- Financial Accounts ---
    override fun getFinancialAccounts(): Flow<List<FinancialAccount>> {
        return financialAccountDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveFinancialAccount(account: FinancialAccount) {
        financialAccountDao.upsert(FinancialAccountEntity.fromDomain(account))
    }

    override suspend fun deleteFinancialAccount(accountId: String) {
        financialAccountDao.deleteById(accountId)
    }

    // --- Payment Methods ---
    override fun getPaymentMethods(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun savePaymentMethod(method: PaymentMethod) {
        paymentMethodDao.upsert(PaymentMethodEntity.fromDomain(method))
    }

    override suspend fun deletePaymentMethod(methodId: String) {
        paymentMethodDao.deleteById(methodId)
    }

    // --- Bills & Installments ---
    override fun getBills(): Flow<List<Bill>> {
        return billDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getBillById(billId: String): Bill? {
        return billDao.getById(billId)?.toDomain()
    }

    override fun getBillsByContact(contactId: String): Flow<List<Bill>> {
        return billDao.getBillsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPlannedInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> {
        return installmentDao.getPlannedInstallmentsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsForPeriod(startMillis, endMillis).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllInstallments(): Flow<List<BillInstallment>> {
        return installmentDao.getAllWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getInstallmentsByBillId(billId: String): List<BillInstallment> {
        return installmentDao.getInstallmentsWithDetailsByBillId(billId).map { it.toDomain() }
    }

    override suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>) {
        database.withTransaction {
            billDao.insert(BillEntity.fromDomain(bill))
            installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
        }
    }

    override suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>) {
        if (installments.isEmpty()) return
        database.withTransaction {
            installmentDao.insertAllIgnore(installments.map { BillInstallmentEntity.fromDomain(it) })
            val allForBill = installmentDao.getInstallmentsByBillId(bill.id)
            val maxDueDate = allForBill.maxOfOrNull { it.dueDate }
            billDao.updateBillEndDateAndTotalInstallments(
                id = bill.id,
                recurrenceEndDate = maxDueDate,
                totalInstallments = allForBill.size
            )
        }
    }

    override suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?,
        actualPaymentDate: Long?
    ) {
        val status = if (isPaid) "PAID" else "PENDING"
        val effectivePaidAt = if (isPaid) (paidTimestamp ?: System.currentTimeMillis()) else null
        val effectiveActualDate = if (isPaid) (actualPaymentDate ?: effectivePaidAt) else null
        installmentDao.updatePayment(installmentId, effectivePaidAt, effectiveActualDate, status)
    }

    override suspend fun updateInstallmentsActualPaymentDateBatch(
        installmentIds: List<String>,
        actualPaymentDate: Long
    ) {
        if (installmentIds.isEmpty()) return
        installmentDao.updateActualPaymentDateBatch(installmentIds, actualPaymentDate)
    }

    override suspend fun updateInstallment(
        installmentId: String,
        newAmountCents: Long,
        newDueDate: Long
    ) {
        database.withTransaction {
            installmentDao.updateInstallmentAmountAndDate(installmentId, newAmountCents, newDueDate)
            val entity = installmentDao.getEntityById(installmentId)
            if (entity != null) {
                // Se for parcela única, atualiza o valor total da conta também
                val bill = billDao.getById(entity.billId)
                if (bill != null && bill.totalInstallments == 1) {
                    billDao.updateBillDetails(
                        id = bill.id,
                        title = bill.title,
                        description = bill.description,
                        totalAmountCents = newAmountCents,
                        categoryId = bill.categoryId,
                        itemId = bill.itemId,
                        contactId = bill.contactId,
                        financialAccountId = bill.financialAccountId,
                        paymentMethodId = bill.paymentMethodId
                    )
                }
            }
        }
    }

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
    ) {
        database.withTransaction {
            billDao.updateBillDetails(
                id = billId,
                title = title.trim(),
                description = description.trim(),
                totalAmountCents = amountCents,
                categoryId = categoryId,
                itemId = itemId,
                contactId = contactId,
                financialAccountId = financialAccountId,
                paymentMethodId = paymentMethodId
            )
            installmentDao.updateInstallmentDetails(
                id = installmentId,
                amountCents = amountCents,
                dueDate = dueDate,
                itemId = itemId,
                contactId = contactId,
                financialAccountId = financialAccountId,
                paymentMethodId = paymentMethodId
            )
        }
    }

    override suspend fun updateFutureInstallmentsAmount(
        billId: String,
        fromDueDate: Long,
        newAmountCents: Long
    ) {
        database.withTransaction {
            installmentDao.updateAmountForPendingFromDueDate(billId, fromDueDate, newAmountCents)
            billDao.updateBillTotalAmount(billId, newAmountCents)
        }
    }

    override suspend fun deleteSingleInstallment(installmentId: String) {
        database.withTransaction {
            val entity = installmentDao.getEntityById(installmentId)
            if (entity != null) {
                installmentDao.deleteById(installmentId)
                val remaining = installmentDao.getInstallmentsByBillId(entity.billId)
                if (remaining.isEmpty()) {
                    billDao.deleteById(entity.billId)
                } else {
                    val maxDueDate = remaining.maxOfOrNull { it.dueDate }
                    billDao.updateBillEndDateAndTotalInstallments(
                        id = entity.billId,
                        recurrenceEndDate = maxDueDate,
                        totalInstallments = remaining.size
                    )
                }
            }
        }
    }

    override suspend fun deleteFutureInstallments(billId: String, fromDueDate: Long) {
        database.withTransaction {
            installmentDao.deletePendingFromDueDate(billId, fromDueDate)
            val remaining = installmentDao.getInstallmentsByBillId(billId)
            if (remaining.isEmpty()) {
                billDao.deleteById(billId)
            } else {
                val maxDueDate = remaining.maxOfOrNull { it.dueDate }
                billDao.updateBillEndDateAndTotalInstallments(
                    id = billId,
                    recurrenceEndDate = maxDueDate,
                    totalInstallments = remaining.size
                )
            }
        }
    }

    override suspend fun pauseRecurringBill(billId: String, isPaused: Boolean) {
        database.withTransaction {
            billDao.updatePausedStatus(billId, isPaused)
            val targetStatus = if (isPaused) "PAUSED" else "PENDING"
            installmentDao.updateStatusForPending(billId, targetStatus)
        }
    }

    override suspend fun stopRecurringBill(billId: String) {
        database.withTransaction {
            installmentDao.deletePendingFromDueDate(billId, 0L)
            val remaining = installmentDao.getInstallmentsByBillId(billId)
            if (remaining.isEmpty()) {
                billDao.deleteById(billId)
            } else {
                val maxDueDate = remaining.maxOfOrNull { it.dueDate }
                billDao.updateBillEndDateAndTotalInstallments(
                    id = billId,
                    recurrenceEndDate = maxDueDate,
                    totalInstallments = remaining.size
                )
            }
        }
    }

    override suspend fun deleteBill(billId: String) {
        billDao.deleteById(billId)
    }
}
