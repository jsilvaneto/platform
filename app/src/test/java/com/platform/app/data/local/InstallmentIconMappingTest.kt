package com.platform.app.data.local

import com.platform.app.data.local.dao.InstallmentWithDetails
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseNature
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallmentIconMappingTest {

    @Test
    fun `InstallmentWithDetails toDomain should propagate category_icon_name`() {
        val entity = BillInstallmentEntity(
            id = "inst-100",
            billId = "bill-100",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 25000L,
            dueDate = 1700000000000L,
            paidAt = null,
            status = "PENDING"
        )

        val details = InstallmentWithDetails(
            installment = entity,
            billTitle = "Academia Smart Fit",
            billType = BillType.SINGLE.name,
            categoryId = "cat-fitness",
            categoryName = "Saúde & Lazer",
            categoryColorHex = "#10B981",
            categoryNature = ExpenseNature.NECESSARIO.name,
            categoryIconName = "fitness_center",
            itemName = "Mensalidade",
            contactName = null,
            financialAccountName = "Itaú",
            paymentMethodName = "Cartão de Crédito"
        )

        val domain = details.toDomain()

        assertEquals("fitness_center", domain.categoryIconName)
        assertEquals("Saúde & Lazer", domain.categoryName)
        assertEquals("#10B981", domain.categoryColorHex)
    }

    @Test
    fun `ExpenseItemEntity toDomain should propagate categoryIconName`() {
        val entity = ExpenseItemEntity(
            id = "item-1",
            name = "Cinema",
            categoryId = "cat-lazer"
        )

        val domain = entity.toDomain(
            categoryName = "Lazer",
            categoryColorHex = "#EC4899",
            categoryIconName = "movie",
            nature = ExpenseNature.DESEJA
        )

        assertEquals("movie", domain.categoryIconName)
        assertEquals("Lazer", domain.categoryName)
    }
}
