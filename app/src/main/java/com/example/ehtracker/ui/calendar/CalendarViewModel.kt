package com.example.ehtracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DaySummary(
    val date: LocalDate,
    val completionRate: Float,
    val totalCompleted: Int,
    val totalHabits: Int,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netAmount: Double
)

enum class CalendarViewMode { MONTH, WEEK }

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val selectedDate: LocalDate? = null,
    val habits: List<Habit> = emptyList(),
    val dailySummaries: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDayTransactions: List<Transaction> = emptyList(),
    val currency: Currency = Currency.USD
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val habits: StateFlow<List<Habit>> = repository.habitsWithCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    init {
        viewModelScope.launch {
            combine(_currentMonth, habits, currency) { month, h, c -> Triple(month, h, c) }
                .flatMapLatest { (month, h, c) ->
                    val start = month.atDay(1)
                    val end = month.atEndOfMonth()
                    combine(
                        repository.dailyExpensesForRange(start, end),
                        repository.dailyIncomesForRange(start, end)
                    ) { expenses, incomes ->
                        val summaries = buildDailySummaries(month, expenses, incomes, h)
                        CalendarUiState(
                            currentMonth = month,
                            dailySummaries = summaries,
                            habits = h,
                            currency = c
                        )
                    }
                }.collect { state ->
                    _uiState.update { it.copy(
                        currentMonth = state.currentMonth,
                        dailySummaries = state.dailySummaries,
                        habits = state.habits,
                        currency = state.currency
                    )}
                }
        }

        viewModelScope.launch {
            _selectedDate.flatMapLatest { date ->
                if (date != null) {
                    repository.transactionsForRange(date, date)
                } else {
                    flowOf(emptyList())
                }
            }.collect { transactions ->
                _uiState.update { it.copy(selectedDayTransactions = transactions) }
            }
        }
    }

    private fun buildDailySummaries(
        month: YearMonth,
        dailyExpenses: Map<LocalDate, Double>,
        dailyIncomes: Map<LocalDate, Double>,
        habits: List<Habit>
    ): Map<LocalDate, DaySummary> {
        val result = mutableMapOf<LocalDate, DaySummary>()
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        var current = start
        while (!current.isAfter(end)) {
            val exp = dailyExpenses[current] ?: 0.0
            val inc = dailyIncomes[current] ?: 0.0
            val completed = habits.count { current in it.completedDates }
            val total = habits.size
            val rate = if (total > 0) completed.toFloat() / total else 0f
            result[current] = DaySummary(
                date = current,
                completionRate = rate,
                totalCompleted = completed,
                totalHabits = total,
                totalExpenses = exp,
                totalIncome = inc,
                netAmount = inc - exp
            )
            current = current.plusDays(1)
        }
        return result
    }

    fun toggleHabit(habitId: String, date: LocalDate) {
        viewModelScope.launch { repository.toggleHabitCompletion(habitId, date) }
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}
