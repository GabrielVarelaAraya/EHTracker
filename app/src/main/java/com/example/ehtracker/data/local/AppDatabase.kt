package com.example.ehtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ehtracker.data.local.dao.BalanceDao
import com.example.ehtracker.data.local.dao.BudgetDao
import com.example.ehtracker.data.local.dao.CurrencyDao
import com.example.ehtracker.data.local.dao.ExpenseDao
import com.example.ehtracker.data.local.dao.HabitCompletionDao
import com.example.ehtracker.data.local.dao.HabitDao
import com.example.ehtracker.data.local.dao.IncomeDao
import com.example.ehtracker.data.local.dao.PreferencesDao
import com.example.ehtracker.data.local.entity.BalanceEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity
import com.example.ehtracker.data.local.entity.PreferencesEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        BalanceEntity::class,
        CurrencyEntity::class,
        BudgetEntity::class,
        PreferencesEntity::class
    ],
    version = 9,
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
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        val MIGRATION_5_6 = Migration(5, 6) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `preferences` (`id` INTEGER NOT NULL, `themeMode` TEXT NOT NULL DEFAULT 'system', PRIMARY KEY(`id`))")
            db.execSQL("INSERT OR IGNORE INTO preferences (id, themeMode) VALUES (1, 'system')")
        }

        val MIGRATION_6_7 = Migration(6, 7) { db ->
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_completions_habitId_date` ON `habit_completions`(`habitId`, `date`)")
            db.execSQL("DROP INDEX IF EXISTS `index_habit_completions_habitId`")
        }

        val MIGRATION_7_8 = Migration(7, 8) { db ->
            db.execSQL("ALTER TABLE preferences ADD COLUMN notificationHour INTEGER NOT NULL DEFAULT 20")
            db.execSQL("ALTER TABLE preferences ADD COLUMN notificationMinute INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE preferences ADD COLUMN notificationsEnabled INTEGER NOT NULL DEFAULT 1")
        }

        val MIGRATION_8_9 = Migration(8, 9) { db ->
            db.execSQL("ALTER TABLE habits ADD COLUMN isNumeric INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habits ADD COLUMN unit TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE habit_completions ADD COLUMN value REAL DEFAULT NULL")
        }
    }
}
