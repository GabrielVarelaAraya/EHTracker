package com.example.ehtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Income
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val habits: StateFlow<List<Habit>> = repository.habitsWithCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Double> = repository.todayTotalExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weekTotal: StateFlow<Double> = repository.thisWeekExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotal: StateFlow<Double> = repository.thisMonthExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val savings: StateFlow<Double> = repository.balance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = repository.totalAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = repository.totalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentBalance: StateFlow<Double> = repository.currentBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    private val _showEditSavings = MutableStateFlow(false)
    val showEditSavings: StateFlow<Boolean> = _showEditSavings.asStateFlow()

    init {
        viewModelScope.launch {
            habits.collect {
                _isLoading.value = false
            }
        }
    }

    fun toggleHabit(habitId: String, date: LocalDate) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date)
        }
    }

    fun addExpense(amount: Double, category: ExpenseCategory, note: String, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.addExpense(amount, category, note, date)
            _showAddSheet.value = false
        }
    }

    fun addHabit(name: String, icon: String) {
        viewModelScope.launch {
            repository.addHabit(name, icon)
            _showAddSheet.value = false
        }
    }

    fun addIncome(amount: Double, note: String) {
        viewModelScope.launch {
            repository.addIncome(amount, note)
            _showAddSheet.value = false
        }
    }

    fun setSavings(amount: Double) {
        viewModelScope.launch {
            repository.setBalance(amount)
            _showEditSavings.value = false
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch {
            repository.setCurrency(currency)
        }
    }

    fun showAdd() { _showAddSheet.value = true }
    fun dismissAdd() { _showAddSheet.value = false }
    fun showEditSavings() { _showEditSavings.value = true }
    fun dismissEditSavings() { _showEditSavings.value = false }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
