package com.example.ehtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ehtracker.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY isBuiltIn DESC, name COLLATE NOCASE ASC")
    fun getAllActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE (id = :id OR name = :name) AND id != :excludeId")
    suspend fun countByIdOrNameExcluding(id: String, name: String, excludeId: String): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE category = :id")
    suspend fun countExpensesUsing(id: String): Int

    @Query("SELECT COUNT(*) FROM budgets WHERE category = :id")
    suspend fun countBudgetsUsing(id: String): Int

    @Query("SELECT COUNT(*) FROM subscriptions WHERE category = :id")
    suspend fun countSubscriptionsUsing(id: String): Int

    @Query("SELECT COUNT(*) FROM quick_add WHERE category = :id")
    suspend fun countQuickItemsUsing(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET name = :newName WHERE id = :id")
    suspend fun renameDisplay(id: String, newName: String)

    @Query("UPDATE categories SET icon = :icon WHERE id = :id")
    suspend fun updateIcon(id: String, icon: String)

    @Query("UPDATE categories SET id = :newId, name = :newName WHERE id = :oldId")
    suspend fun changeId(oldId: String, newId: String, newName: String)

    @Query("UPDATE expenses SET category = :newId WHERE category = :oldId")
    suspend fun cascadeExpenses(oldId: String, newId: String)

    @Query("UPDATE budgets SET category = :newId WHERE category = :oldId")
    suspend fun cascadeBudgets(oldId: String, newId: String)

    @Query("UPDATE subscriptions SET category = :newId WHERE category = :oldId")
    suspend fun cascadeSubscriptions(oldId: String, newId: String)

    @Query("UPDATE quick_add SET category = :newId WHERE category = :oldId")
    suspend fun cascadeQuickItems(oldId: String, newId: String)

    @Query("DELETE FROM categories WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteById(id: String)
}
