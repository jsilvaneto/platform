package com.platform.app.data.local

import androidx.room.Query
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalContributionDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.PaymentMethodDao
import org.junit.Assert.fail
import org.junit.Test

/**
 * Guard Rail Automatizado (Prompt 65):
 * Varre todos os métodos anotados com @Query em DAOs de tabelas sincronizáveis e garante
 * que nenhuma query de leitura, agregação, totais ou mutação omita a cláusula 'deletedAt IS NULL'.
 * Queries de purga explícita ('deletedAt IS NOT NULL') e de reset de testes ('DELETE FROM <table>' sem WHERE)
 * são as únicas exceções permitidas.
 */
class SynchronizableQueriesGuardTest {

    private val synchronizableDaos = listOf(
        BillDao::class.java,
        BillInstallmentDao::class.java,
        BudgetDao::class.java,
        CategoryDao::class.java,
        ContactDao::class.java,
        CreditCardDao::class.java,
        ExpenseItemDao::class.java,
        FinancialAccountDao::class.java,
        GoalDao::class.java,
        GoalContributionDao::class.java,
        PaymentDao::class.java,
        PaymentMethodDao::class.java
    )

    private val synchronizableTables = setOf(
        "bills",
        "bill_installments",
        "budgets",
        "categories",
        "contacts",
        "credit_cards",
        "credit_card_invoices",
        "expense_items",
        "financial_accounts",
        "goals",
        "goal_contributions",
        "payments"
    )

    @Test
    fun `all queries on synchronizable tables must filter deletedAt IS NULL`() {
        val violations = mutableListOf<String>()

        for (dao in synchronizableDaos) {
            for (method in dao.methods) {
                val queryAnnotation = method.getAnnotation(Query::class.java) ?: continue
                val rawSql = queryAnnotation.value.trim()
                val normalizedSql = rawSql.replace("\\s+".toRegex(), " ")
                val upperSql = normalizedSql.uppercase()

                // Verificar se a query referencia qualquer tabela sincronizável
                val referencesSyncTable = synchronizableTables.any { table ->
                    val upperTable = table.uppercase()
                    upperSql.contains("FROM $upperTable") ||
                            upperSql.contains("JOIN $upperTable") ||
                            upperSql.contains("UPDATE $upperTable") ||
                            upperSql.contains("DELETE FROM $upperTable") ||
                            upperSql.contains("INTO $upperTable")
                }

                if (!referencesSyncTable) continue

                // Exceção 1: Queries de purga definitiva após confirmação de sincronização
                val isPurgeQuery = upperSql.contains("DELETEDAT IS NOT NULL")
                if (isPurgeQuery) continue

                // Exceção 2: Queries de limpeza geral do banco de teste em memória (ex: DELETE FROM bills sem WHERE)
                val isTestResetQuery = upperSql.matches("^DELETE FROM [A-Z_]+$".toRegex())
                if (isTestResetQuery) continue

                // Regra: Toda query de leitura/agregação/atualização DEVE conter 'deletedAt IS NULL'
                if (!upperSql.contains("DELETEDAT IS NULL")) {
                    violations.add(
                        """
                        [VIOLAÇÃO] ${dao.simpleName}.${method.name}:
                        SQL: $normalizedSql
                        Motivo: Query em tabela sincronizável não filtra 'deletedAt IS NULL'.
                        """.trimIndent()
                    )
                }
            }
        }

        if (violations.isNotEmpty()) {
            fail(
                "Encontradas ${violations.size} queries em tabelas sincronizáveis sem filtro 'deletedAt IS NULL':\n\n" +
                        violations.joinToString("\n\n")
            )
        }
    }
}
