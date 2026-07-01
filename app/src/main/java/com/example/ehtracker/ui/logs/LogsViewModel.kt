package com.example.ehtracker.ui.logs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Income
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.model.toExpense
import com.example.ehtracker.data.model.toIncome
import com.example.ehtracker.data.model.toTransaction
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class LogsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val habits: List<Habit> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val currency: Currency = Currency.USD,
    val editingExpense: Expense? = null,
    val editingHabit: Habit? = null,
    val editingIncome: Income? = null
) {
    val transactions: List<Transaction>
        get() {
            val query = searchQuery
            val filteredExpenses = if (query.isBlank()) expenses
                else expenses.filter { it.note.contains(query, ignoreCase = true) || it.category.displayName.contains(query, ignoreCase = true) }
            val filteredIncomes = if (query.isBlank()) incomes
                else incomes.filter { it.note.contains(query, ignoreCase = true) }
            return buildList {
                filteredExpenses.forEach { add(it.toTransaction()) }
                filteredIncomes.forEach { add(it.toTransaction()) }
            }.sortedByDescending { it.date }
        }

    val editingTransaction: Transaction?
        get() = editingExpense?.toTransaction() ?: editingIncome?.toTransaction()
}

class LogsViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    private val _searchInput = MutableStateFlow("")

    init {
        _searchInput
            .debounce(300)
            .onEach { query -> _uiState.update { it.copy(searchQuery = query) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            repository.expenses().collect { expenses ->
                _uiState.update { it.copy(expenses = expenses, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.habitsWithCompletions().collect { habits ->
                _uiState.update { state ->
                    val query = state.searchQuery
                    state.copy(habits = if (query.isBlank()) habits
                        else habits.filter { it.name.contains(query, ignoreCase = true) })
                }
            }
        }
        viewModelScope.launch {
            repository.incomes().collect { incomes ->
                _uiState.update { it.copy(incomes = incomes) }
            }
        }
        viewModelScope.launch {
            repository.currency().collect { currency ->
                _uiState.update { it.copy(currency = currency) }
            }
        }
    }

    fun selectTab(index: Int) { _uiState.update { it.copy(selectedTab = index) } }

    fun updateSearch(query: String) { _searchInput.value = query }

    fun deleteExpense(id: String) {
        viewModelScope.launch { repository.deleteExpense(id) }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch { repository.deleteHabit(id) }
    }

    fun deleteIncome(id: String) {
        viewModelScope.launch { repository.deleteIncome(id) }
    }

    fun showEditExpense(expense: Expense) {
        _uiState.update { it.copy(editingExpense = expense) }
    }

    fun showEditTransaction(transaction: Transaction) {
        when (transaction) {
            is Transaction.Expense -> showEditExpense(transaction.toExpense())
            is Transaction.Income -> showEditIncome(transaction.toIncome())
        }
    }

    fun dismissEditExpense() {
        _uiState.update { it.copy(editingExpense = null) }
    }

    fun updateExpense(id: String, amount: Double, category: ExpenseCategory, note: String, date: LocalDate) {
        viewModelScope.launch {
            repository.updateExpense(id, amount, category, note, date)
            _uiState.update { it.copy(editingExpense = null) }
        }
    }

    fun showEditHabit(habit: Habit) {
        _uiState.update { it.copy(editingHabit = habit) }
    }

    fun dismissEditHabit() {
        _uiState.update { it.copy(editingHabit = null) }
    }

    fun updateHabit(id: String, name: String, icon: String, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            repository.updateHabit(id, name, icon, targetDaysPerWeek)
            _uiState.update { it.copy(editingHabit = null) }
        }
    }

    fun showEditIncome(income: Income) {
        _uiState.update { it.copy(editingIncome = income) }
    }

    fun dismissEditIncome() {
        _uiState.update { it.copy(editingIncome = null) }
    }

    fun updateIncome(id: String, amount: Double, note: String, date: LocalDate) {
        viewModelScope.launch {
            repository.updateIncome(id, amount, note, date)
            _uiState.update { it.copy(editingIncome = null) }
        }
    }

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
            return LogsViewModel(repository) as T
        }
    }
}
