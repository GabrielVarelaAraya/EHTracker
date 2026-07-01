package com.example.ehtracker.data.model

import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val completedDates: List<LocalDate>,
    val targetDaysPerWeek: Int = 7
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
            return completedThisWeek.toFloat() / targetDaysPerWeek
        }

    val isCompletedToday: Boolean
        get() = completedDates.contains(LocalDate.now())
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

enum class ExpenseCategory(val displayName: String, val icon: String) {
    FOOD("Food", "🍜"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍"),
    BILLS("Bills", "📄"),
    HEALTH("Health", "💊"),
    ENTERTAINMENT("Fun", "🎬"),
    OTHER("Other", "📦")
}
