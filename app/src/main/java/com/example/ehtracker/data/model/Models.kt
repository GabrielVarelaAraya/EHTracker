package com.example.ehtracker.data.model

import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val completedDates: List<LocalDate>,
    val targetDaysPerWeek: Int = 7,
    val isNumeric: Boolean = false,
    val unit: String = "",
    val completionValues: Map<LocalDate, Double> = emptyMap()
) {
    val currentStreak: Int
        get() {
            if (completedDates.isEmpty()) return 0
            val sorted = completedDates.sortedDescending()
            var streak = 1
            var current = sorted[0]
            for (i in 1 until sorted.size) {
                if (sorted[i] == current.minusDays(1)) {
                    streak++
                    current = sorted[i]
                } else break
            }
            return streak
        }

    val completionRate: Float
        get() {
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val completedThisWeek = completedDates.count { it in startOfWeek..today }
            val daysElapsed = today.toEpochDay() - startOfWeek.toEpochDay() + 1
            return completedThisWeek.toFloat() / daysElapsed.toFloat()
        }

}

data class Account(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val initialBalance: Double,
    val currency: Currency,
    val createdAt: Long
)

data class Expense(
    val id: String,
    val amount: Double,
    val category: String,
    val note: String,
    val date: LocalDate,
    val accountId: String? = null
)

data class Income(
    val id: String,
    val amount: Double,
    val note: String,
    val date: LocalDate,
    val accountId: String? = null
)

data class Subscription(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val billingDay: Int,
    val notes: String = "",
    val isActive: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null
)

data class QuickAddItem(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val notes: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null
)

data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
}

enum class TransactionType { EXPENSE, INCOME }

sealed class Transaction {
    abstract val id: String
    abstract val amount: Double
    abstract val note: String
    abstract val date: LocalDate

    data class Expense(
        override val id: String,
        override val amount: Double,
        val category: String,
        override val note: String,
        override val date: LocalDate,
        val accountId: String? = null
    ) : Transaction()

    data class Income(
        override val id: String,
        override val amount: Double,
        override val note: String,
        override val date: LocalDate,
        val accountId: String? = null
    ) : Transaction()
}

fun Expense.toTransaction() = Transaction.Expense(id, amount, category, note, date, accountId)
fun Income.toTransaction() = Transaction.Income(id, amount, note, date, accountId)

fun Transaction.Expense.toExpense() = Expense(id, amount, category, note, date, accountId)
fun Transaction.Income.toIncome() = Income(id, amount, note, date, accountId)

const val CATEGORY_SAVINGS = "SAVINGS"

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val isBuiltIn: Boolean = false
) {
    val isSavings: Boolean
        get() = id == CATEGORY_SAVINGS
}

val defaultCatalog: List<Category> = listOf(
    Category("FOOD", "Food", "\uD83C\uDF5C", true),
    Category("TRANSPORT", "Transport", "\uD83D\uDE97", true),
    Category("SHOPPING", "Shopping", "\uD83D\uDECD", true),
    Category("BILLS", "Bills", "\uD83D\uDCC4", true),
    Category("HEALTH", "Health", "\uD83D\uDC8A", true),
    Category("ENTERTAINMENT", "Fun", "\uD83C\uDFAC", true),
    Category("OTHER", "Other", "\uD83D\uDCE6", true),
    Category(CATEGORY_SAVINGS, "Savings", "\uD83D\uDC37", true)
)

fun resolveCategory(id: String, catalog: List<Category> = emptyList()): Category =
    catalog.firstOrNull { it.id == id }
        ?: defaultCatalog.firstOrNull { it.id == id }
        ?: Category(id, id.lowercase().replaceFirstChar { it.uppercase() }, "\uD83D\uDCE6")
