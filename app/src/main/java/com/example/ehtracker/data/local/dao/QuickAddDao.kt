package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.QuickAddEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickAddDao {
    @Query("SELECT * FROM quick_add ORDER BY name ASC")
    fun getAll(): Flow<List<QuickAddEntity>>

    @Query("SELECT * FROM quick_add WHERE id = :id")
    suspend fun getById(id: String): QuickAddEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QuickAddEntity)

    @Query("DELETE FROM quick_add WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE quick_add SET accountId = NULL WHERE accountId = :accountId")
    suspend fun setAccountIdNull(accountId: String)
}
