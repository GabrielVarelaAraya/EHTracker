package com.example.ehtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ehtracker.data.local.dao.BalanceDao
import com.example.ehtracker.data.local.dao.BudgetDao
import com.example.ehtracker.data.local.dao.CurrencyDao
import com.example.ehtracker.data.local.dao.ExpenseDao
import com.example.ehtracker.data.local.dao.HabitCompletionDao
import com.example.ehtracker.data.local.dao.HabitDao
import com.example.ehtracker.data.local.dao.IncomeDao
import com.example.ehtracker.data.local.entity.BalanceEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        BalanceEntity::class,
        CurrencyEntity::class,
        BudgetEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun balanceDao(): BalanceDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun budgetDao(): BudgetDao
}
