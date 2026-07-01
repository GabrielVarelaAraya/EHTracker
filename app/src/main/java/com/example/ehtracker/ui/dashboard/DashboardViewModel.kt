package com.example.ehtracker.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "DashboardVM"

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val savings: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val currentBalance: Double = 0.0,
    val currency: Currency = Currency.USD,
    val showEditSavings: Boolean = false,
    val showAddSheet: Boolean = false,
    val lastExpenseAmount: Double = 0.0
)

class DashboardViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.habitsWithCompletions().collect { habits ->
                _uiState.update { it.copy(habits = habits, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.todayTotalExpenses().collect { v ->
                _uiState.update { it.copy(todayTotal = v) }
            }
        }
        viewModelScope.launch {
            repository.thisWeekExpenses().collect { v ->
                _uiState.update { it.copy(weekTotal = v) }
            }
        }
        viewModelScope.launch {
            repository.thisMonthExpenses().collect { v ->
                _uiState.update { it.copy(monthTotal = v) }
            }
        }
        viewModelScope.launch {
            repository.balance().collect { v ->
                _uiState.update { it.copy(savings = v) }
            }
        }
        viewModelScope.launch {
            repository.totalAllExpenses().collect { v ->
                _uiState.update { it.copy(totalExpenses = v) }
            }
        }
        viewModelScope.launch {
            repository.totalIncome().collect { v ->
                _uiState.update { it.copy(totalIncome = v) }
            }
        }
        viewModelScope.launch {
            repository.currentBalance().collect { v ->
                _uiState.update { it.copy(currentBalance = v) }
            }
        }
        viewModelScope.launch {
            repository.currency().collect { v ->
                _uiState.update { it.copy(currency = v) }
            }
        }
        viewModelScope.launch {
            repository.expenses().map { expenses -> expenses.firstOrNull()?.amount ?: 0.0 }.collect { v ->
                _uiState.update { it.copy(lastExpenseAmount = v) }
            }
        }
    }

    fun toggleHabit(habitId: String, date: LocalDate) {
        viewModelScope.launch {
            try { repository.toggleHabitCompletion(habitId, date) }
            catch (e: Exception) { Log.e(TAG, "toggleHabit failed", e) }
        }
    }

    fun addExpense(amount: Double, category: ExpenseCategory, note: String, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            try {
                repository.addExpense(amount, category, note, date)
                _uiState.update { it.copy(showAddSheet = false) }
            } catch (e: Exception) {
                Log.e(TAG, "addExpense failed", e)
            }
        }
    }

    fun addHabit(name: String, icon: String, targetDays: Int = 7) {
        viewModelScope.launch {
            try {
                repository.addHabit(name, icon, targetDays)
                _uiState.update { it.copy(showAddSheet = false) }
            } catch (e: Exception) {
                Log.e(TAG, "addHabit failed", e)
            }
        }
    }

    fun addIncome(amount: Double, note: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.addIncome(amount, note, date)
                _uiState.update { it.copy(showAddSheet = false) }
            } catch (e: Exception) {
                Log.e(TAG, "addIncome failed", e)
            }
        }
    }

    fun setSavings(amount: Double, currency: Currency) {
        viewModelScope.launch {
            try {
                repository.setBalance(amount)
                repository.setCurrency(currency)
                _uiState.update { it.copy(showEditSavings = false) }
            } catch (e: Exception) {
                Log.e(TAG, "setSavings failed", e)
            }
        }
    }

    fun showAdd() { _uiState.update { it.copy(showAddSheet = true) } }
    fun dismissAdd() { _uiState.update { it.copy(showAddSheet = false) } }
    fun showEditSavings() { _uiState.update { it.copy(showEditSavings = true) } }
    fun dismissEditSavings() { _uiState.update { it.copy(showEditSavings = false) } }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(300)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
