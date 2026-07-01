package com.example.ehtracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class AiInsight(
    val text: String,
    val type: InsightType
)

enum class InsightType { SAVING, TIP, WARNING }

enum class DateRange(val label: String) {
    WEEK("Week"),
    MONTH("Month")
}

data class HabitSummary(
    val habit: Habit,
    val completionRateThisWeek: Float,
    val streak: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedRange = MutableStateFlow(DateRange.MONTH)
    val selectedRange: StateFlow<DateRange> = _selectedRange.asStateFlow()

    private fun rangeStartFor(range: DateRange): LocalDate {
        val today = LocalDate.now()
        return when (range) {
            DateRange.WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            DateRange.MONTH -> today.withDayOfMonth(1)
        }
    }

    private fun rangeEndFor(range: DateRange): LocalDate {
        return when (range) {
            DateRange.WEEK -> rangeStartFor(range).plusDays(6)
            DateRange.MONTH -> rangeStartFor(range).withDayOfMonth(
                rangeStartFor(range).lengthOfMonth()
            )
        }
    }

    val dailyExpenses: StateFlow<Map<Int, Double>> = _selectedRange.flatMapLatest { range ->
        repository.dailyExpensesForRange(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expensesByCategory: StateFlow<Map<ExpenseCategory, Double>> = _selectedRange.flatMapLatest { range ->
        repository.expensesByCategoryForRange(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val rangeTotal: StateFlow<Double> = _selectedRange.flatMapLatest { range ->
        repository.expensesForRange(rangeStartFor(range), rangeEndFor(range)).map { list -> list.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val habitCompletionRate: StateFlow<Float> = repository.habitCompletionRate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val monthTotal: StateFlow<Double> = repository.thisMonthExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weekTotal: StateFlow<Double> = repository.thisWeekExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    val habitsSummary: StateFlow<List<HabitSummary>> = repository.habitsWithCompletions().map { habits ->
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        habits.map { habit ->
            val completedThisWeek = habit.completedDates.count { it in startOfWeek..today }
            val rate = completedThisWeek.toFloat() / habit.targetDaysPerWeek.coerceAtLeast(1)
            HabitSummary(
                habit = habit,
                completionRateThisWeek = rate.coerceIn(0f, 1f),
                streak = habit.currentStreak
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            dailyExpenses.collect {
                _isLoading.value = false
            }
        }
    }

    fun selectRange(range: DateRange) {
        _selectedRange.value = range
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(300)
            _isRefreshing.value = false
        }
    }

    fun aiInsights(): List<AiInsight> {
        val totalVal = rangeTotal.value
        val categoryData = expensesByCategory.value
        val sym = currency.value.symbol
        val topCategory = categoryData.maxByOrNull { it.value }

        val insights = mutableListOf<AiInsight>()

        if (topCategory != null && topCategory.value > 0) {
            insights.add(
                AiInsight(
                    "${topCategory.key.displayName} is your highest category at $sym${"%.0f".format(topCategory.value)} this ${_selectedRange.value.label.lowercase()}.",
                    InsightType.TIP
                )
            )
        }

        val habits = habitsSummary.value
        val topStreak = habits.maxByOrNull { it.streak }
        if (topStreak != null && topStreak.streak > 1) {
            insights.add(
                AiInsight(
                    "Your habit streak for ${topStreak.habit.name} is ${topStreak.streak} days. Keep it up!",
                    InsightType.TIP
                )
            )
        }

        if (totalVal > 0) {
            insights.add(
                AiInsight(
                    "You spent $sym${"%.0f".format(totalVal)} this ${_selectedRange.value.label.lowercase()}.",
                    InsightType.SAVING
                )
            )
        }

        if (monthTotal.value > 0) {
            insights.add(
                AiInsight(
                    "Consider budgeting $sym${"%.0f".format(monthTotal.value * 0.1)} for savings this month.",
                    InsightType.WARNING
                )
            )
        }

        return insights
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repository) as T
        }
    }
}
