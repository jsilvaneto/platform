package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    // --- Credit Cards ---
    @Query("SELECT * FROM credit_cards WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getCardById(id: String): CreditCardEntity?

    @Upsert
    suspend fun upsertCard(card: CreditCardEntity)

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCards(cards: List<CreditCardEntity>)

    @Query("UPDATE credit_cards SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteCardById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM credit_cards WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeletedCards(olderThanMillis: Long)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAllCards()

    @Query("SELECT * FROM credit_cards WHERE deletedAt IS NULL")
    suspend fun getAllCardsList(): List<CreditCardEntity>

    @Query("SELECT COUNT(*) FROM credit_cards WHERE deletedAt IS NULL")
    suspend fun countCards(): Int

    // --- Invoices ---
    @Query("SELECT * FROM credit_card_invoices WHERE creditCardId = :creditCardId AND deletedAt IS NULL ORDER BY referenceMonth DESC")
    fun getInvoicesForCard(creditCardId: String): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE creditCardId = :creditCardId AND referenceMonth = :referenceMonth AND deletedAt IS NULL LIMIT 1")
    suspend fun getInvoiceByMonth(creditCardId: String, referenceMonth: String): CreditCardInvoiceEntity?

    @Query("SELECT * FROM credit_card_invoices WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getInvoiceById(id: String): CreditCardInvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: CreditCardInvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvoices(invoices: List<CreditCardInvoiceEntity>)

    @Query("UPDATE credit_card_invoices SET status = :status, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateInvoiceStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE credit_card_invoices SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteInvoiceById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM credit_card_invoices WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeletedInvoices(olderThanMillis: Long)

    @Query("DELETE FROM credit_card_invoices")
    suspend fun deleteAllInvoices()

    @Query("SELECT * FROM credit_card_invoices WHERE deletedAt IS NULL ORDER BY dueDate ASC")
    fun getAllInvoices(): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE referenceMonth = :referenceMonth AND deletedAt IS NULL")
    fun getInvoicesForReferenceMonth(referenceMonth: String): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE dueDate BETWEEN :startDate AND :endDate AND deletedAt IS NULL ORDER BY dueDate ASC")
    fun getInvoicesForDueDateRange(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE status != 'PAGA' AND dueDate BETWEEN :startDate AND :endDate AND deletedAt IS NULL ORDER BY dueDate ASC")
    suspend fun getPendingInvoicesInRange(startDate: java.time.LocalDate, endDate: java.time.LocalDate): List<CreditCardInvoiceEntity>

    @Query("SELECT * FROM credit_card_invoices WHERE status != 'PAGA' AND dueDate < :beforeDate AND deletedAt IS NULL ORDER BY dueDate ASC")
    suspend fun getOverdueInvoices(beforeDate: java.time.LocalDate): List<CreditCardInvoiceEntity>

    @Query("SELECT * FROM credit_card_invoices WHERE deletedAt IS NULL")
    suspend fun getAllInvoicesList(): List<CreditCardInvoiceEntity>

    @Query("SELECT COUNT(*) FROM credit_card_invoices WHERE creditCardId = :cardId AND deletedAt IS NULL")
    suspend fun countInvoicesForCard(cardId: String): Int
}
