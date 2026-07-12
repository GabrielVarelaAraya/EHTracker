package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.ehtracker.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAll(): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amount) FROM incomes")
    fun totalAll(): Flow<Double?>

    @Query("SELECT * FROM incomes WHERE date BETWEEN :startMillis AND :endMillis ORDER BY date")
    fun getByDateRange(startMillis: Long, endMillis: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getById(id: String): IncomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun getAndDelete(id: String): IncomeEntity? {
        val entity = getById(id) ?: return null
        deleteById(id)
        return entity
    }

    @Transaction
    suspend fun update(id: String, amount: Double, note: String, date: Long) {
        val existing = getById(id) ?: return
        insert(existing.copy(amount = amount, note = note, date = date))
    }
}
