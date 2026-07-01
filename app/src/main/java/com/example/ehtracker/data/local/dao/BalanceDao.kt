package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.BalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balance WHERE id = 1")
    fun get(): Flow<BalanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(balance: BalanceEntity)
}
