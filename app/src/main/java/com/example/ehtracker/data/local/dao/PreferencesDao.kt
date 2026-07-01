package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.PreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM preferences WHERE id = 1")
    fun get(): Flow<PreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(preferences: PreferencesEntity)
}
