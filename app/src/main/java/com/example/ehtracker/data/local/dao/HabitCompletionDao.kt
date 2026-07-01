package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    fun getByHabitId(habitId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    suspend fun getByHabitIdOnce(habitId: String): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE date >= :startDate AND date <= :endDate")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitIdAndDate(habitId: String, date: Long): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteByHabitIdAndDate(habitId: String, date: Long)

    @Query("SELECT * FROM habit_completions")
    fun getAll(): Flow<List<HabitCompletionEntity>>
}
