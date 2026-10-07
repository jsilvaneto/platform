package com.platform.app.data.local.backup

import com.google.gson.annotations.SerializedName
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.BudgetEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.GoalContributionEntity
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.data.local.entity.TransactionEntity

data class BackupDataDto(
    @SerializedName("version") val version: Int = CURRENT_VERSION,
    @SerializedName("exportedAt") val exportedAt: Long = System.currentTimeMillis(),
    @SerializedName("categories") val categories: List<CategoryEntity> = emptyList(),
    @SerializedName("expenseItems") val expenseItems: List<ExpenseItemEntity> = emptyList(),
    @SerializedName("creditCards") val creditCards: List<CreditCardEntity> = emptyList(),
    @SerializedName("creditCardInvoices") val creditCardInvoices: List<CreditCardInvoiceEntity> = emptyList(),
    @SerializedName("financialAccounts") val financialAccounts: List<FinancialAccountEntity> = emptyList(),
    @SerializedName("paymentMethods") val paymentMethods: List<PaymentMethodEntity> = emptyList(),
    @SerializedName("contacts") val contacts: List<ContactEntity> = emptyList(),
    @SerializedName("bills") val bills: List<BillEntity> = emptyList(),
    @SerializedName("installments") val installments: List<BillInstallmentEntity> = emptyList(),
    @SerializedName("budgets") val budgets: List<BudgetEntity> = emptyList(),
    @SerializedName("goals") val goals: List<GoalEntity> = emptyList(),
    @SerializedName("goalContributions") val goalContributions: List<GoalContributionEntity> = emptyList(),
    @SerializedName("transactions") val transactions: List<TransactionEntity> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}
