package com.example.ehtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ehtracker.data.local.dao.AccountDao
import com.example.ehtracker.data.local.dao.BudgetDao
import com.example.ehtracker.data.local.dao.CategoryDao
import com.example.ehtracker.data.local.dao.CurrencyDao
import com.example.ehtracker.data.local.dao.ExpenseDao
import com.example.ehtracker.data.local.dao.HabitCompletionDao
import com.example.ehtracker.data.local.dao.HabitDao
import com.example.ehtracker.data.local.dao.IncomeDao
import com.example.ehtracker.data.local.dao.PreferencesDao
import com.example.ehtracker.data.local.dao.QuickAddDao
import com.example.ehtracker.data.local.dao.SavingsGoalDao
import com.example.ehtracker.data.local.dao.SubscriptionDao
import com.example.ehtracker.data.local.entity.AccountEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CategoryEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity
import com.example.ehtracker.data.local.entity.PreferencesEntity
import com.example.ehtracker.data.local.entity.QuickAddEntity
import com.example.ehtracker.data.local.entity.SavingsGoalEntity
import com.example.ehtracker.data.local.entity.SubscriptionEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        CurrencyEntity::class,
        BudgetEntity::class,
        PreferencesEntity::class,
        SubscriptionEntity::class,
        AccountEntity::class,
        QuickAddEntity::class,
        SavingsGoalEntity::class,
        CategoryEntity::class
    ],
    version = 21,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun budgetDao(): BudgetDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun accountDao(): AccountDao
    abstract fun quickAddDao(): QuickAddDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun categoryDao(): CategoryDao

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

        val MIGRATION_9_10 = Migration(9, 10) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `subscriptions` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `billingDay` INTEGER NOT NULL, `notes` TEXT NOT NULL DEFAULT '', `isActive` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`id`))")
        }

        val MIGRATION_10_11 = Migration(10, 11) { db ->
            db.execSQL("ALTER TABLE preferences ADD COLUMN biometricEnabled INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_11_12 = Migration(11, 12) { db ->
            db.execSQL("ALTER TABLE subscriptions ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'")
        }

        val MIGRATION_12_13 = Migration(12, 13) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` TEXT NOT NULL, `initialBalance` REAL NOT NULL, `currencyCode` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("ALTER TABLE expenses ADD COLUMN accountId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE incomes ADD COLUMN accountId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE subscriptions ADD COLUMN accountId TEXT DEFAULT NULL")
            db.execSQL("DROP TABLE IF EXISTS balance")
        }

        val MIGRATION_13_14 = Migration(13, 14) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `quick_add` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `notes` TEXT NOT NULL DEFAULT '', `type` TEXT NOT NULL DEFAULT 'expense', `accountId` TEXT, PRIMARY KEY(`id`))")
        }

        val MIGRATION_14_15 = Migration(14, 15) { db ->
            db.execSQL("ALTER TABLE subscriptions ADD COLUMN lastCreatedMonth TEXT DEFAULT NULL")
        }

        val MIGRATION_15_16 = Migration(15, 16) { db ->
            db.execSQL("ALTER TABLE preferences ADD COLUMN lastSelectedAccountId TEXT DEFAULT NULL")
        }

        val MIGRATION_16_17 = Migration(16, 17) { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `savings_goals` (" +
                    "`id` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`targetAmount` REAL NOT NULL, " +
                    "`currentAmount` REAL NOT NULL DEFAULT 0, " +
                    "`accountId` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
        }

        val MIGRATION_17_18 = Migration(17, 18) { db ->
            db.execSQL("ALTER TABLE expenses ADD COLUMN goalId TEXT DEFAULT NULL")
        }

        val MIGRATION_18_19 = Migration(18, 19) { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `categories` (" +
                    "`id` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`icon` TEXT NOT NULL, " +
                    "`isBuiltIn` INTEGER NOT NULL DEFAULT 0, " +
                    "`archived` INTEGER NOT NULL DEFAULT 0, " +
                    "`createdAt` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`id`))"
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
            val seed = listOf(
                "FOOD" to ("Food" to "\uD83C\uDF5C"),
                "TRANSPORT" to ("Transport" to "\uD83D\uDE97"),
                "SHOPPING" to ("Shopping" to "\uD83D\uDECD"),
                "BILLS" to ("Bills" to "\uD83D\uDCC4"),
                "HEALTH" to ("Health" to "\uD83D\uDC8A"),
                "ENTERTAINMENT" to ("Fun" to "\uD83C\uDFAC"),
                "OTHER" to ("Other" to "\uD83D\uDCE6"),
                "SAVINGS" to ("Savings" to "\uD83D\uDC37")
            )
            seed.forEach { (id, pair) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO categories (id, name, icon, isBuiltIn, archived, createdAt) VALUES (?, ?, ?, 1, 0, 0)",
                    arrayOf(id, pair.first, pair.second)
                )
            }
        }

        val MIGRATION_19_20 = Migration(19, 20) { db ->
            db.execSQL("ALTER TABLE preferences ADD COLUMN hasSeenOnboarding INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_20_21 = Migration(20, 21) { db ->
            db.execSQL("ALTER TABLE preferences ADD COLUMN dashboardOrder TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE preferences ADD COLUMN dashboardHidden TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE preferences ADD COLUMN isCompactMode INTEGER NOT NULL DEFAULT 0")
        }
    }
}
