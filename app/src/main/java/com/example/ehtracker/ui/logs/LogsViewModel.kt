package com.example.ehtracker.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val expenses: StateFlow<List<Expense>> = _searchQuery.flatMapLatest { query ->
        repository.expensesFiltered(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = repository.habitsWithCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense.asStateFlow()

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    init {
        viewModelScope.launch {
            expenses.collect {
                _isLoading.value = false
            }
        }
    }

    fun selectTab(index: Int) { _selectedTab.value = index }

    fun updateSearch(query: String) { _searchQuery.value = query }

    fun deleteExpense(id: String) {
        viewModelScope.launch { repository.deleteExpense(id) }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch { repository.deleteHabit(id) }
    }

    fun showEditExpense(expense: Expense) {
        _editingExpense.value = expense
    }

    fun dismissEditExpense() {
        _editingExpense.value = null
    }

    fun updateExpense(id: String, amount: Double, category: ExpenseCategory, note: String, date: LocalDate) {
        viewModelScope.launch {
            repository.updateExpense(id, amount, category, note, date)
            _editingExpense.value = null
        }
    }

    fun showEditHabit(habit: Habit) {
        _editingHabit.value = habit
    }

    fun dismissEditHabit() {
        _editingHabit.value = null
    }

    fun updateHabit(id: String, name: String, icon: String, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            repository.updateHabit(id, name, icon, targetDaysPerWeek)
            _editingHabit.value = null
        }
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogsViewModel(repository) as T
        }
    }
}
