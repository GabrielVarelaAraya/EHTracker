package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAll(): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amount) FROM incomes")
    fun totalAll(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteById(id: String)
}
