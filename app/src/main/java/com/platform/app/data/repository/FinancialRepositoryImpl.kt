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
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialAccount
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
    private val installmentDao: BillInstallmentDao
) : FinancialRepository {

    // --- Categories ---
    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCategory(category: Category) {
        categoryDao.insert(CategoryEntity.fromDomain(category))
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteById(categoryId)
    }

    override suspend fun seedInitialCategoriesIfEmpty() {
        if (categoryDao.count() == 0) {
            val defaults = listOf(
                Category(id = UUID.randomUUID().toString(), name = "Moradia", colorHex = "#3B82F6", iconName = "home", nature = ExpenseNature.OBRIGATORIO),
                Category(id = UUID.randomUUID().toString(), name = "Alimentação", colorHex = "#10B981", iconName = "shopping_cart", nature = ExpenseNature.NECESSARIO),
                Category(id = UUID.randomUUID().toString(), name = "Transporte", colorHex = "#F59E0B", iconName = "directions_car", nature = ExpenseNature.NECESSARIO),
                Category(id = UUID.randomUUID().toString(), name = "Assinaturas & Serviços", colorHex = "#8B5CF6", iconName = "subscriptions", nature = ExpenseNature.DESEJA),
                Category(id = UUID.randomUUID().toString(), name = "Saúde", colorHex = "#EF4444", iconName = "medical_services", nature = ExpenseNature.OBRIGATORIO),
                Category(id = UUID.randomUUID().toString(), name = "Lazer", colorHex = "#EC4899", iconName = "sports_esports", nature = ExpenseNature.DESEJA),
                Category(id = UUID.randomUUID().toString(), name = "Educação", colorHex = "#6366F1", iconName = "school", nature = ExpenseNature.OBRIGATORIO),
                Category(id = UUID.randomUUID().toString(), name = "Outros", colorHex = "#64748B", iconName = "more_horiz", nature = ExpenseNature.NENHUM)
            )
            categoryDao.insertAll(defaults.map { CategoryEntity.fromDomain(it) })
        }
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
                    nature = cat?.nature ?: ExpenseNature.NECESSARIO
                )
            }
        }
    }

    override suspend fun saveExpenseItem(item: ExpenseItem) {
        expenseItemDao.insert(ExpenseItemEntity.fromDomain(item))
    }

    override suspend fun deleteExpenseItem(itemId: String) {
        expenseItemDao.deleteById(itemId)
    }

    override suspend fun seedInitialExpenseItemsIfEmpty() {
        if (expenseItemDao.count() == 0) {
            val categories = getCategories().first()
            if (categories.isNotEmpty()) {
                val moradia = categories.find { it.name == "Moradia" }?.id ?: categories[0].id
                val alimentacao = categories.find { it.name == "Alimentação" }?.id ?: categories[0].id
                val transporte = categories.find { it.name == "Transporte" }?.id ?: categories[0].id

                val defaults = listOf(
                    ExpenseItem(name = "Aluguel / Condomínio", categoryId = moradia),
                    ExpenseItem(name = "Energia Elétrica", categoryId = moradia),
                    ExpenseItem(name = "Água & Saneamento", categoryId = moradia),
                    ExpenseItem(name = "Supermercado", categoryId = alimentacao),
                    ExpenseItem(name = "Feira & Hortifruti", categoryId = alimentacao),
                    ExpenseItem(name = "Combustível", categoryId = transporte),
                    ExpenseItem(name = "Manutenção Veicular", categoryId = transporte)
                )
                expenseItemDao.insertAll(defaults.map { ExpenseItemEntity.fromDomain(it) })
            }
        }
    }

    // --- Credit Cards & Invoices ---
    override fun getCreditCards(): Flow<List<CreditCard>> {
        return creditCardDao.getAllCards().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCreditCard(card: CreditCard) {
        creditCardDao.insertCard(CreditCardEntity.fromDomain(card))
    }

    override suspend fun deleteCreditCard(cardId: String) {
        creditCardDao.deleteCardById(cardId)
    }

    override suspend fun seedInitialCreditCardsIfEmpty() {
        if (creditCardDao.countCards() == 0) {
            val defaults = listOf(
                CreditCard(name = "Cartão Principal", totalLimitCents = 1000000L, closingDay = 25, dueDay = 5, colorHex = "#2563EB"),
                CreditCard(name = "Cartão Secundário", totalLimitCents = 500000L, closingDay = 15, dueDay = 25, colorHex = "#8B5CF6")
            )
            creditCardDao.insertCard(CreditCardEntity.fromDomain(defaults[0]))
            creditCardDao.insertCard(CreditCardEntity.fromDomain(defaults[1]))
        }
    }

    override fun getCreditCardInvoices(cardId: String): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForCard(cardId), getAllInstallments()) { invoices, installments ->
            invoices.map { invEntity ->
                val invoiceInsts = installments.filter { it.invoiceId == invEntity.id }
                val totalCents = invoiceInsts.sumOf { it.amountCents }
                invEntity.toDomain(totalAmountCents = totalCents)
            }
        }
    }

    override fun getAllCreditCardInvoices(): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getAllInvoices(), getAllInstallments()) { invoices, installments ->
            invoices.map { invEntity ->
                val invoiceInsts = installments.filter { it.invoiceId == invEntity.id }
                val totalCents = invoiceInsts.sumOf { it.amountCents }
                invEntity.toDomain(totalAmountCents = totalCents)
            }
        }
    }

    override fun getInvoicesForPeriod(startMillis: Long, endMillis: Long): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForDueDateRange(startMillis, endMillis), getAllInstallments()) { invoices, installments ->
            invoices.map { invEntity ->
                val invoiceInsts = installments.filter { it.invoiceId == invEntity.id }
                val totalCents = invoiceInsts.sumOf { it.amountCents }
                invEntity.toDomain(totalAmountCents = totalCents)
            }
        }
    }

    override suspend fun getOrCreateInvoiceForMonth(cardId: String, referenceMonth: String): CreditCardInvoice {
        val existing = creditCardDao.getInvoiceByMonth(cardId, referenceMonth)
        if (existing != null) {
            return existing.toDomain()
        }

        val card = creditCardDao.getCardById(cardId) ?: return CreditCardInvoice(
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
        return newInvoice
    }

    override suspend fun payInvoice(invoiceId: String) {
        database.withTransaction {
            creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.PAGA.name)
            val allInsts = installmentDao.getAllInstallmentsList()
            val invoiceInsts = allInsts.filter { it.invoiceId == invoiceId }
            val now = System.currentTimeMillis()
            invoiceInsts.forEach { inst ->
                installmentDao.updatePayment(inst.id, now, "PAID")
            }
        }
    }

    override suspend fun reopenInvoice(invoiceId: String) {
        database.withTransaction {
            creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.ABERTA.name)
            val allInsts = installmentDao.getAllInstallmentsList()
            val invoiceInsts = allInsts.filter { it.invoiceId == invoiceId }
            invoiceInsts.forEach { inst ->
                installmentDao.updatePayment(inst.id, null, "PENDING")
            }
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
        contactDao.insert(ContactEntity.fromDomain(contact))
    }

    override suspend fun deleteContact(contactId: String) {
        contactDao.deleteById(contactId)
    }

    override suspend fun seedInitialContactsIfEmpty() {
        if (contactDao.count() == 0) {
            val initial = listOf(
                Contact(id = UUID.randomUUID().toString(), name = "Supermercado"),
                Contact(id = UUID.randomUUID().toString(), name = "Farmácia"),
                Contact(id = UUID.randomUUID().toString(), name = "Posto de Combustível"),
                Contact(id = UUID.randomUUID().toString(), name = "Restaurante"),
                Contact(id = UUID.randomUUID().toString(), name = "Internet / Telefonia"),
                Contact(id = UUID.randomUUID().toString(), name = "Energia Elétrica"),
                Contact(id = UUID.randomUUID().toString(), name = "Água e Saneamento"),
                Contact(id = UUID.randomUUID().toString(), name = "Diversos")
            )
            contactDao.insertAll(initial.map { ContactEntity.fromDomain(it) })
        }
    }

    // --- Financial Accounts ---
    override fun getFinancialAccounts(): Flow<List<FinancialAccount>> {
        return financialAccountDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveFinancialAccount(account: FinancialAccount) {
        financialAccountDao.insert(FinancialAccountEntity.fromDomain(account))
    }

    override suspend fun deleteFinancialAccount(accountId: String) {
        financialAccountDao.deleteById(accountId)
    }

    override suspend fun seedInitialFinancialAccountsIfEmpty() {
        if (financialAccountDao.count() == 0) {
            val defaults = listOf(
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Conta Corrente", accountType = "CHECKING", colorHex = "#3B82F6"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Cartão de Crédito", accountType = "CREDIT_CARD", colorHex = "#8B5CF6"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Carteira / Dinheiro", accountType = "CASH", colorHex = "#10B981"),
                FinancialAccount(id = UUID.randomUUID().toString(), name = "Reserva de Emergência", accountType = "SAVINGS", colorHex = "#F59E0B")
            )
            financialAccountDao.insertAll(defaults.map { FinancialAccountEntity.fromDomain(it) })
        }
    }

    // --- Payment Methods ---
    override fun getPaymentMethods(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun savePaymentMethod(method: PaymentMethod) {
        paymentMethodDao.insert(PaymentMethodEntity.fromDomain(method))
    }

    override suspend fun deletePaymentMethod(methodId: String) {
        paymentMethodDao.deleteById(methodId)
    }

    override suspend fun seedInitialPaymentMethodsIfEmpty() {
        if (paymentMethodDao.count() == 0) {
            val defaults = listOf(
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Pix", iconName = "qr_code"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Boleto", iconName = "receipt"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Cartão de Crédito", iconName = "credit_card"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Cartão de Débito", iconName = "credit_card"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Dinheiro", iconName = "payments"),
                PaymentMethod(id = UUID.randomUUID().toString(), name = "Transferência Bancária", iconName = "account_balance")
            )
            paymentMethodDao.insertAll(defaults.map { PaymentMethodEntity.fromDomain(it) })
        }
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

    override suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>) {
        database.withTransaction {
            billDao.insert(BillEntity.fromDomain(bill))
            installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
        }
    }

    override suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?
    ) {
        val status = if (isPaid) "PAID" else "PENDING"
        installmentDao.updatePayment(installmentId, paidTimestamp, status)
    }

    override suspend fun deleteBill(billId: String) {
        billDao.deleteById(billId)
    }
}
