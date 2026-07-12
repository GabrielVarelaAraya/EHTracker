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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val showSuccessDialog: Boolean = false,
    val successMessage: String = ""
)

class DashboardViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.habitsWithCompletions().collect { habits ->
                _uiState.update { it.copy(habits = habits, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.expenseTotals().collect { totals ->
                _uiState.update { it.copy(todayTotal = totals.today, weekTotal = totals.week, monthTotal = totals.month) }
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
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun setHabitValue(habitId: String, date: LocalDate, value: Double) {
        viewModelScope.launch {
            try { repository.setHabitValue(habitId, date, value) }
            catch (e: Exception) {
                Log.e(TAG, "setHabitValue failed", e)
                _snackbarEvent.emit("Failed to update habit")
            }
        }
    }

    fun toggleHabit(habitId: String, date: LocalDate) {
        viewModelScope.launch {
            try { repository.toggleHabitCompletion(habitId, date) }
            catch (e: Exception) {
                Log.e(TAG, "toggleHabit failed", e)
                _snackbarEvent.emit("Failed to update habit")
            }
        }
    }

    fun addExpense(amount: Double, category: ExpenseCategory, note: String, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            try {
                repository.addExpense(amount, category, note, date)
                _uiState.update { it.copy(showAddSheet = false, showSuccessDialog = true, successMessage = "Expense added") }
                _snackbarEvent.emit("Expense added")
            } catch (e: Exception) {
                Log.e(TAG, "addExpense failed", e)
                _snackbarEvent.emit("Failed to add expense")
            }
        }
    }

    fun addHabit(name: String, icon: String, targetDays: Int = 7, isNumeric: Boolean = false, unit: String = "") {
        viewModelScope.launch {
            try {
                repository.addHabit(name, icon, targetDays, isNumeric, unit)
                _uiState.update { it.copy(showAddSheet = false, showSuccessDialog = true, successMessage = "Habit added") }
                _snackbarEvent.emit("Habit added")
            } catch (e: Exception) {
                Log.e(TAG, "addHabit failed", e)
                _snackbarEvent.emit("Failed to add habit")
            }
        }
    }

    fun addIncome(amount: Double, note: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.addIncome(amount, note, date)
                _uiState.update { it.copy(showAddSheet = false, showSuccessDialog = true, successMessage = "Income added") }
                _snackbarEvent.emit("Income added")
            } catch (e: Exception) {
                Log.e(TAG, "addIncome failed", e)
                _snackbarEvent.emit("Failed to add income")
            }
        }
    }

    fun setSavings(amount: Double, currency: Currency) {
        viewModelScope.launch {
            try {
                repository.setBalance(amount)
                repository.setCurrency(currency)
                _uiState.update { it.copy(showEditSavings = false) }
                _snackbarEvent.emit("Settings saved")
            } catch (e: Exception) {
                Log.e(TAG, "setSavings failed", e)
                _snackbarEvent.emit("Failed to save settings")
            }
        }
    }

    fun showAdd() { _uiState.update { it.copy(showAddSheet = true) } }
    fun dismissAdd() { _uiState.update { it.copy(showAddSheet = false) } }
    fun dismissSuccessDialog() { _uiState.update { it.copy(showSuccessDialog = false) } }
    fun showEditSavings() { _uiState.update { it.copy(showEditSavings = true) } }
    fun dismissEditSavings() { _uiState.update { it.copy(showEditSavings = false) } }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
