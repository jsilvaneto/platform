package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.FinancialAccountType
import com.platform.app.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações do agregado de Catálogo (Categorias, Itens, Contatos, Contas Bancárias, Formas de Pagamento e Seeds).
 */
@Singleton
class CatalogDataSource @Inject constructor(
    private val database: PlatformDatabase,
    private val categoryDao: CategoryDao,
    private val expenseItemDao: ExpenseItemDao,
    private val contactDao: ContactDao,
    private val financialAccountDao: FinancialAccountDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val preferencesManager: PreferencesManager
) {

    suspend fun seedInitialData() {
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
    fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveCategory(category: Category) {
        database.withTransaction {
            categoryDao.upsert(CategoryEntity.fromDomain(category))
            database.budgetDao.updateCategoryInfo(category.id, category.name, category.colorHex)
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteById(categoryId)
    }

    // --- Expense Items ---
    fun getExpenseItems(): Flow<List<ExpenseItem>> {
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

    fun getExpenseItemsByCategory(categoryId: String): Flow<List<ExpenseItem>> {
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

    suspend fun saveExpenseItem(item: ExpenseItem) {
        expenseItemDao.upsert(ExpenseItemEntity.fromDomain(item))
    }

    suspend fun deleteExpenseItem(itemId: String) {
        expenseItemDao.deleteById(itemId)
    }

    // --- Contacts ---
    fun getContacts(): Flow<List<Contact>> {
        return contactDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getContactById(contactId: String): Flow<Contact?> {
        return contactDao.getByIdFlow(contactId).map { it?.toDomain() }
    }

    suspend fun saveContact(contact: Contact) {
        contactDao.upsert(ContactEntity.fromDomain(contact))
    }

    suspend fun deleteContact(contactId: String) {
        contactDao.deleteById(contactId)
    }

    // --- Financial Accounts ---
    fun getFinancialAccounts(): Flow<List<FinancialAccount>> {
        return financialAccountDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveFinancialAccount(account: FinancialAccount) {
        financialAccountDao.upsert(FinancialAccountEntity.fromDomain(account))
    }

    suspend fun deleteFinancialAccount(accountId: String) {
        financialAccountDao.deleteById(accountId)
    }

    // --- Payment Methods ---
    fun getPaymentMethods(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun savePaymentMethod(method: PaymentMethod) {
        paymentMethodDao.upsert(PaymentMethodEntity.fromDomain(method))
    }

    suspend fun deletePaymentMethod(methodId: String) {
        paymentMethodDao.deleteById(methodId)
    }
}
