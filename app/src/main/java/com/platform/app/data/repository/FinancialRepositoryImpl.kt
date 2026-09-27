package com.platform.app.data.repository

import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.SubcategoryDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.data.local.entity.SubcategoryEntity
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class FinancialRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubcategoryDao,
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
                Category(id = UUID.randomUUID().toString(), name = "Moradia", colorHex = "#3B82F6", iconName = "home"),
                Category(id = UUID.randomUUID().toString(), name = "Alimentação", colorHex = "#10B981", iconName = "shopping_cart"),
                Category(id = UUID.randomUUID().toString(), name = "Transporte", colorHex = "#F59E0B", iconName = "directions_car"),
                Category(id = UUID.randomUUID().toString(), name = "Assinaturas & Serviços", colorHex = "#8B5CF6", iconName = "subscriptions"),
                Category(id = UUID.randomUUID().toString(), name = "Saúde", colorHex = "#EF4444", iconName = "medical_services"),
                Category(id = UUID.randomUUID().toString(), name = "Lazer", colorHex = "#EC4899", iconName = "sports_esports"),
                Category(id = UUID.randomUUID().toString(), name = "Educação", colorHex = "#6366F1", iconName = "school"),
                Category(id = UUID.randomUUID().toString(), name = "Outros", colorHex = "#64748B", iconName = "more_horiz")
            )
            categoryDao.insertAll(defaults.map { CategoryEntity.fromDomain(it) })
        }
    }

    // --- Subcategories ---
    override fun getSubcategories(categoryId: String): Flow<List<Subcategory>> {
        return subcategoryDao.getByCategory(categoryId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllSubcategories(): Flow<List<Subcategory>> {
        return subcategoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveSubcategory(subcategory: Subcategory) {
        subcategoryDao.insert(SubcategoryEntity.fromDomain(subcategory))
    }

    override suspend fun deleteSubcategory(subcategoryId: String) {
        subcategoryDao.deleteById(subcategoryId)
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
        billDao.insert(BillEntity.fromDomain(bill))
        installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
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
