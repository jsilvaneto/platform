package com.platform.app.data.repository

import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class FinancialRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao
) : FinancialRepository {

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCategory(category: Category) {
        categoryDao.insert(CategoryEntity.fromDomain(category))
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
