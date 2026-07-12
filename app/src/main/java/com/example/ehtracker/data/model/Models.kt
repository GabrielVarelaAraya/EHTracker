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

data class Expense(
    val id: String,
    val amount: Double,
    val category: ExpenseCategory,
    val note: String,
    val date: LocalDate
)

data class Income(
    val id: String,
    val amount: Double,
    val note: String,
    val date: LocalDate
)

sealed class Transaction {
    abstract val id: String
    abstract val amount: Double
    abstract val note: String
    abstract val date: LocalDate

    data class Expense(
        override val id: String,
        override val amount: Double,
        val category: ExpenseCategory,
        override val note: String,
        override val date: LocalDate
    ) : Transaction()

    data class Income(
        override val id: String,
        override val amount: Double,
        override val note: String,
        override val date: LocalDate
    ) : Transaction()
}

fun Expense.toTransaction() = Transaction.Expense(id, amount, category, note, date)
fun Income.toTransaction() = Transaction.Income(id, amount, note, date)

fun Transaction.Expense.toExpense() = Expense(id, amount, category, note, date)
fun Transaction.Income.toIncome() = Income(id, amount, note, date)

enum class ExpenseCategory(val displayName: String, val icon: String) {
    FOOD("Food", "🍜"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍"),
    BILLS("Bills", "📄"),
    HEALTH("Health", "💊"),
    ENTERTAINMENT("Fun", "🎬"),
    OTHER("Other", "📦")
}
