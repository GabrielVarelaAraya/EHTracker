package com.example.ehtracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.resolveCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

data class SavingsInsight(
    val text: String,
    val isPositive: Boolean
)

enum class InsightType { SAVING, TIP, WARNING }

enum class DateRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    YEAR("Year")
}

data class HabitSummary(
    val habit: Habit,
    val completionRate: Float,
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
            DateRange.QUARTER -> today.withMonth((today.monthValue - 1) / 3 * 3 + 1).withDayOfMonth(1)
            DateRange.YEAR -> today.withDayOfYear(1)
        }
    }

    private fun rangeEndFor(range: DateRange): LocalDate {
        return when (range) {
            DateRange.WEEK -> rangeStartFor(range).plusDays(6)
            DateRange.MONTH -> rangeStartFor(range).withDayOfMonth(
                rangeStartFor(range).lengthOfMonth()
            )
            DateRange.QUARTER -> rangeStartFor(range).plusMonths(3).minusDays(1)
            DateRange.YEAR -> rangeStartFor(range).plusYears(1).minusDays(1)
        }
    }

    val dailyExpenses: StateFlow<Map<LocalDate, Double>> = _selectedRange.flatMapLatest { range ->
        repository.dailyExpensesForRange(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailyIncomes: StateFlow<Map<LocalDate, Double>> = _selectedRange.flatMapLatest { range ->
        repository.dailyIncomesForRange(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailySavingsRate: StateFlow<Map<LocalDate, Double>> = combine(dailyExpenses, dailyIncomes) { expenses, incomes ->
        val allDates = (expenses.keys + incomes.keys).sorted()
        allDates.associateWith { date ->
            val expense = expenses[date] ?: 0.0
            val income = incomes[date] ?: 0.0
            when {
                income > 0 -> ((income - expense) / income * 100).coerceIn(-100.0, 100.0)
                expense > 0 -> -100.0
                else -> 0.0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val savingsRateInsights: StateFlow<List<SavingsInsight>> = dailySavingsRate.map { rateMap ->
        if (rateMap.isEmpty()) return@map emptyList()
        val rates = rateMap.values.toList()
        val activeRates = rates.filter { it != 0.0 }
        val avg = if (activeRates.isNotEmpty()) activeRates.average() else 0.0
        val bestDay = rateMap.maxByOrNull { it.value }
        val worstDay = rateMap.minByOrNull { it.value }
        val positiveDays = rates.count { it > 0 }
        val negativeDays = rates.count { it < 0 }

        buildList {
            when {
                avg >= 50 -> add(SavingsInsight("Excellent! You're saving an average of ${"%.1f".format(avg)}% of your income.", true))
                avg in 20.0..50.0 -> add(SavingsInsight("Good job! Your average savings rate is ${"%.1f".format(avg)}%.", true))
                avg in 0.0..20.0 -> add(SavingsInsight("Your average savings rate is ${"%.1f".format(avg)}%. Try to increase it above 20%.", false))
                else -> add(SavingsInsight("Warning: You're spending more than you earn. Average rate: ${"%.1f".format(avg)}%.", false))
            }
            if (positiveDays > negativeDays) {
                add(SavingsInsight("You saved money on $positiveDays out of ${rates.size} days.", true))
            } else if (negativeDays > 0) {
                add(SavingsInsight("You overspent on $negativeDays out of ${rates.size} days.", false))
            }
            if (bestDay != null && bestDay.value > 0) {
                add(SavingsInsight("Best day: ${bestDay.key.monthValue}/${bestDay.key.dayOfMonth} with ${"%.1f".format(bestDay.value)}% saved.", true))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesByCategory: StateFlow<Map<String, Double>> = _selectedRange.flatMapLatest { range ->
        repository.expensesByCategoryForRange(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categories: StateFlow<List<Category>> = repository.categories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rangeTotal: StateFlow<Double> = _selectedRange.flatMapLatest { range ->
        repository.expensesForRange(rangeStartFor(range), rangeEndFor(range)).map { list -> list.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val habitCompletionRate: StateFlow<Float> = _selectedRange.flatMapLatest { range ->
        val today = LocalDate.now()
        repository.habitCompletionRateByDays(rangeStartFor(range), today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val monthTotal: StateFlow<Double> = repository.thisMonthExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    val budgetStatus: StateFlow<Map<String, Pair<Double, Double>>> = _selectedRange.flatMapLatest { range ->
        repository.budgetStatus(rangeStartFor(range), rangeEndFor(range))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val habitsSummary: StateFlow<List<HabitSummary>> = _selectedRange.flatMapLatest { range ->
        val today = LocalDate.now()
        val startDate = rangeStartFor(range)
        val daysElapsed = (today.toEpochDay() - startDate.toEpochDay() + 1).toInt()
        repository.habitsWithCompletions().map { habits ->
            habits.map { habit ->
                val completedInRange = habit.completedDates.count { it in startDate..today }
                val rate = completedInRange.toFloat() / daysElapsed.coerceAtLeast(1)
                HabitSummary(
                    habit = habit,
                    completionRate = rate.coerceIn(0f, 1f),
                    streak = habit.currentStreak
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiInsights: StateFlow<List<AiInsight>> = combine(
        rangeTotal,
        expensesByCategory,
        currency,
        _selectedRange,
        habitsSummary,
        monthTotal
    ) { values: Array<Any> ->
        val totalVal = values[0] as Double
        val categoryData = values[1] as Map<String, Double>
        val cur = values[2] as Currency
        val range = values[3] as DateRange
        val habits = values[4] as List<HabitSummary>
        val monthTot = values[5] as Double
        val sym = cur.symbol
        val topCategory = categoryData.maxByOrNull { it.value }

        val insights = mutableListOf<AiInsight>()

        if (topCategory != null && topCategory.value > 0) {
            insights.add(
                AiInsight(
                    "${resolveCategory(topCategory.key, emptyList()).name} is your highest category at $sym${"%.0f".format(topCategory.value)} this ${range.label.lowercase()}.",
                    InsightType.TIP
                )
            )
        }

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
                    "You spent $sym${"%.0f".format(totalVal)} this ${range.label.lowercase()}.",
                    InsightType.SAVING
                )
            )
        }

        if (monthTot > 0) {
            insights.add(
                AiInsight(
                    "Consider budgeting $sym${"%.0f".format(monthTot * 0.1)} for savings this month.",
                    InsightType.WARNING
                )
            )
        }

        insights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _isLoading.value = false
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val current = _selectedRange.value
            _selectedRange.value = current
            kotlinx.coroutines.delay(300)
            _isRefreshing.value = false
        }
    }

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()
    private var pendingBudgetUndo: Pair<String, Double>? = null

    fun selectRange(range: DateRange) {
        _selectedRange.value = range
    }

    fun setBudget(category: String, limit: Double) {
        viewModelScope.launch { repository.setBudget(category, limit) }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            val current = budgetStatus.value[category]?.second ?: 0.0
            val limit = if (current > 0) current else repository.budgets().first().find { it.category == category }?.monthlyLimit ?: 0.0
            if (limit > 0) pendingBudgetUndo = category to limit
            repository.deleteBudget(category)
            _snackbarEvent.emit("Budget deleted")
        }
    }

    fun undoBudgetDelete() {
        val pending = pendingBudgetUndo ?: return
        viewModelScope.launch {
            repository.setBudget(pending.first, pending.second)
            pendingBudgetUndo = null
            _snackbarEvent.emit("Budget restored")
        }
    }

    fun clearPendingUndo() { pendingBudgetUndo = null }
    fun hasPendingBudgetUndo(): Boolean = pendingBudgetUndo != null

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repository) as T
        }
    }
}
