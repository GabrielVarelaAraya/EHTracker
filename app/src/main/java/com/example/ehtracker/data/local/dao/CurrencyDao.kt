package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency WHERE id = 1")
    fun get(): Flow<CurrencyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(currency: CurrencyEntity)
}
