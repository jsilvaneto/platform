package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoryDao {
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getByCategory(categoryId: String): Flow<List<SubcategoryEntity>>

    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    fun getAll(): Flow<List<SubcategoryEntity>>

    @Query("SELECT COUNT(*) FROM subcategories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subcategory: SubcategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subcategories: List<SubcategoryEntity>)

    @Query("DELETE FROM subcategories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM subcategories")
    suspend fun getAllList(): List<SubcategoryEntity>

    @Query("DELETE FROM subcategories")
    suspend fun deleteAll()
}
