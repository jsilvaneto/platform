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
    @Query("SELECT * FROM credit_cards ORDER BY name ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): CreditCardEntity?

    @Upsert
    suspend fun upsertCard(card: CreditCardEntity)

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCards(cards: List<CreditCardEntity>)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCardById(id: String)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAllCards()

    @Query("SELECT * FROM credit_cards")
    suspend fun getAllCardsList(): List<CreditCardEntity>

    @Query("SELECT COUNT(*) FROM credit_cards")
    suspend fun countCards(): Int

    // --- Invoices ---
    @Query("SELECT * FROM credit_card_invoices WHERE creditCardId = :creditCardId ORDER BY referenceMonth DESC")
    fun getInvoicesForCard(creditCardId: String): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE creditCardId = :creditCardId AND referenceMonth = :referenceMonth LIMIT 1")
    suspend fun getInvoiceByMonth(creditCardId: String, referenceMonth: String): CreditCardInvoiceEntity?

    @Query("SELECT * FROM credit_card_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): CreditCardInvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: CreditCardInvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvoices(invoices: List<CreditCardInvoiceEntity>)

    @Query("UPDATE credit_card_invoices SET status = :status WHERE id = :id")
    suspend fun updateInvoiceStatus(id: String, status: String)

    @Query("DELETE FROM credit_card_invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)

    @Query("DELETE FROM credit_card_invoices")
    suspend fun deleteAllInvoices()

    @Query("SELECT * FROM credit_card_invoices ORDER BY dueDate ASC")
    fun getAllInvoices(): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE referenceMonth = :referenceMonth")
    fun getInvoicesForReferenceMonth(referenceMonth: String): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getInvoicesForDueDateRange(startDate: Long, endDate: Long): Flow<List<CreditCardInvoiceEntity>>

    @Query("SELECT * FROM credit_card_invoices")
    suspend fun getAllInvoicesList(): List<CreditCardInvoiceEntity>

    @Query("SELECT COUNT(*) FROM credit_card_invoices WHERE creditCardId = :cardId")
    suspend fun countInvoicesForCard(cardId: String): Int
}
