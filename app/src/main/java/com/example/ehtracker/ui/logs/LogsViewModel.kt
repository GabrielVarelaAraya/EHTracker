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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate

sealed class DeletedItem {
    data class Expense(val id: String, val amount: Double, val category: ExpenseCategory, val note: String, val date: LocalDate) : DeletedItem()
    data class Income(val id: String, val amount: Double, val note: String, val date: LocalDate) : DeletedItem()
    data class Habit(val id: String, val name: String, val icon: String, val targetDaysPerWeek: Int) : DeletedItem()
}

data class LogsUiState(
    val isLoading: Boolean = true,
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
            val filteredExpenses = expenses.filter { e ->
                query.isBlank() || e.note.contains(query, ignoreCase = true) || e.category.displayName.contains(query, ignoreCase = true)
            }
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

    private val _undoEvent = MutableSharedFlow<DeletedItem>()
    val undoEvent: SharedFlow<DeletedItem> = _undoEvent.asSharedFlow()

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val expense = _uiState.value.expenses.find { it.id == id } ?: return@launch
            try {
                repository.deleteExpense(id)
                _undoEvent.emit(DeletedItem.Expense(expense.id, expense.amount, expense.category, expense.note, expense.date))
            } catch (e: Exception) { Log.e("LogsVM", "deleteExpense failed", e) }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            val habit = _uiState.value.habits.find { it.id == id } ?: return@launch
            try {
                repository.deleteHabit(id)
                _undoEvent.emit(DeletedItem.Habit(habit.id, habit.name, habit.icon, habit.targetDaysPerWeek))
            } catch (e: Exception) { Log.e("LogsVM", "deleteHabit failed", e) }
        }
    }

    fun deleteIncome(id: String) {
        viewModelScope.launch {
            val income = _uiState.value.incomes.find { it.id == id } ?: return@launch
            try {
                repository.deleteIncome(id)
                _undoEvent.emit(DeletedItem.Income(income.id, income.amount, income.note, income.date))
            } catch (e: Exception) { Log.e("LogsVM", "deleteIncome failed", e) }
        }
    }

    fun undoDelete(item: DeletedItem) {
        viewModelScope.launch {
            try {
                when (item) {
                    is DeletedItem.Expense -> repository.addExpense(item.amount, item.category, item.note, item.date)
                    is DeletedItem.Income -> repository.addIncome(item.amount, item.note, item.date)
                    is DeletedItem.Habit -> repository.addHabit(item.name, item.icon, item.targetDaysPerWeek)
                }
            } catch (e: Exception) { Log.e("LogsVM", "undo failed", e) }
        }
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
            try {
                repository.updateExpense(id, amount, category, note, date)
                _uiState.update { it.copy(editingExpense = null) }
            } catch (e: Exception) { Log.e("LogsVM", "updateExpense failed", e) }
        }
    }

    fun showEditHabit(habit: Habit) {
        _uiState.update { it.copy(editingHabit = habit) }
    }

    fun dismissEditHabit() {
        _uiState.update { it.copy(editingHabit = null) }
    }

    fun updateHabit(id: String, name: String, icon: String, targetDaysPerWeek: Int, isNumeric: Boolean = false, unit: String = "") {
        viewModelScope.launch {
            try {
                repository.updateHabit(id, name, icon, targetDaysPerWeek, isNumeric, unit)
                _uiState.update { it.copy(editingHabit = null) }
            } catch (e: Exception) { Log.e("LogsVM", "updateHabit failed", e) }
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
            try {
                repository.updateIncome(id, amount, note, date)
                _uiState.update { it.copy(editingIncome = null) }
            } catch (e: Exception) { Log.e("LogsVM", "updateIncome failed", e) }
        }
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogsViewModel(repository) as T
        }
    }
}
