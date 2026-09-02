package com.example.ehtracker.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Account
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.NotificationHelper
import com.example.ehtracker.EHTrackerApplication
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.QuickAddItem
import com.example.ehtracker.data.model.SavingsGoal
import com.example.ehtracker.data.model.Subscription
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.model.TransactionType
import com.example.ehtracker.data.model.resolveCategory
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.delay
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

enum class DashboardCard(val label: String) {
    BALANCE("Balance"),
    SUBSCRIPTIONS("Subscriptions"),
    QUICK_ADD("Quick Add"),
    HABITS("Habits"),
    EXPENSES("Expenses"),
    CALENDAR("Calendar")
}

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
    val subscriptions: List<Subscription> = emptyList(),
    val showSubscriptionsSheet: Boolean = false,
    val currentAccount: Account? = null,
    val quickAddItems: List<QuickAddItem> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val showGoalsSheet: Boolean = false,
    val categories: List<Category> = emptyList(),
    val hasSeenOnboarding: Boolean = true,
    val pendingUndo: UndoData? = null,
    val dashboardOrder: List<String> = DashboardCard.entries.map { it.name },
    val dashboardHidden: Set<String> = emptySet(),
    val isCompactMode: Boolean = false,
    val showCustomizationSheet: Boolean = false,
    val showSettings: Boolean = false,
    val showCalendarSheet: Boolean = false,
    val showAccountSwitcher: Boolean = false,
    val allAccounts: List<Account> = emptyList()
)

sealed class UndoData {
    data class BudgetUndo(val category: String, val limit: Double) : UndoData()
    data class GoalUndo(val goal: SavingsGoal) : UndoData()
    data class CategoryUndo(val id: String, val name: String, val icon: String) : UndoData()
    data class SubscriptionUndo(val subscription: Subscription) : UndoData()
    data class QuickAddUndo(val item: QuickAddItem) : UndoData()
}

class DashboardViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.hasSeenOnboarding().collect { seen ->
                    _uiState.update { state -> state.copy(hasSeenOnboarding = seen) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.isCompactMode().collect { compact ->
                    _uiState.update { state -> state.copy(isCompactMode = compact) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.dashboardOrder().collect { order ->
                    if (order != null && order.isNotEmpty()) {
                        _uiState.update { state -> state.copy(dashboardOrder = order) }
                    }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.dashboardHidden().collect { hidden ->
                    _uiState.update { state -> state.copy(dashboardHidden = hidden) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.categories().collect { cats ->
                    _uiState.update { state -> state.copy(categories = cats) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.habitsWithCompletions().collect { habits ->
                    _uiState.update { state -> state.copy(habits = habits, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.expenseTotals().collect { totals ->
                    _uiState.update { state -> state.copy(todayTotal = totals.today, weekTotal = totals.week, monthTotal = totals.month) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.balance().collect { v ->
                    _uiState.update { state -> state.copy(savings = v) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.totalSpending().collect { v ->
                    _uiState.update { state -> state.copy(totalExpenses = v) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.totalIncome().collect { v ->
                    _uiState.update { state -> state.copy(totalIncome = v) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.currentBalance().collect { v ->
                    _uiState.update { state -> state.copy(currentBalance = v) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.currency().collect { v ->
                    _uiState.update { state -> state.copy(currency = v) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.subscriptions().collect { list ->
                    _uiState.update { state -> state.copy(subscriptions = list) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.currentAccount().collect { account ->
                    _uiState.update { state -> state.copy(currentAccount = account) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.quickAddItems().collect { list ->
                    _uiState.update { state -> state.copy(quickAddItems = list) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.savingsGoals().collect { list ->
                    _uiState.update { state -> state.copy(goals = list) }
                }
            }
        }
        viewModelScope.launch {
            _refreshTrigger.collect {
                repository.accounts().collect { accounts ->
                    _uiState.update { state -> state.copy(allAccounts = accounts) }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            _refreshTrigger.value = System.currentTimeMillis()
            delay(300)
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

    fun addExpense(amount: Double, category: String, note: String, date: LocalDate = LocalDate.now(), recurring: Boolean = false) {
        viewModelScope.launch {
            try {
                if (recurring) {
                    repository.addQuickAddItem(
                        name = note.ifBlank { resolveCategory(category, _uiState.value.categories).name },
                        amount = amount,
                        category = category,
                        notes = "",
                        type = TransactionType.EXPENSE
                    )
                    _uiState.update { it.copy(showAddSheet = false) }
                    _snackbarEvent.emit("Recurring expense saved")
                } else {
                    repository.addExpense(amount, category, note, date)
                    _uiState.update { it.copy(showAddSheet = false) }
                    _snackbarEvent.emit("Expense added")
                }
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
                _uiState.update { it.copy(showAddSheet = false) }
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
                _uiState.update { it.copy(showAddSheet = false) }
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
    fun showEditSavings() { _uiState.update { it.copy(showEditSavings = true) } }
    fun dismissEditSavings() { _uiState.update { it.copy(showEditSavings = false) } }

    fun showSubscriptionsSheet() { _uiState.update { it.copy(showSubscriptionsSheet = true) } }
    fun dismissSubscriptionsSheet() { _uiState.update { it.copy(showSubscriptionsSheet = false) } }

    fun showGoalsSheet() { _uiState.update { it.copy(showGoalsSheet = true) } }
    fun dismissGoalsSheet() { _uiState.update { it.copy(showGoalsSheet = false) } }

    fun addGoal(name: String, targetAmount: Double) {
        viewModelScope.launch {
            try {
                repository.addSavingsGoal(name, targetAmount)
                _snackbarEvent.emit("Goal added")
            } catch (e: Exception) {
                Log.e(TAG, "addGoal failed", e)
                _snackbarEvent.emit("Failed to add goal")
            }
        }
    }

    fun updateGoal(id: String, name: String, targetAmount: Double) {
        viewModelScope.launch {
            try {
                repository.updateSavingsGoal(id, name, targetAmount)
                _snackbarEvent.emit("Goal updated")
            } catch (e: Exception) {
                Log.e(TAG, "updateGoal failed", e)
                _snackbarEvent.emit("Failed to update goal")
            }
        }
    }

    fun addContribution(goalId: String, amount: Double) {
        viewModelScope.launch {
            try {
                repository.addContribution(goalId, amount)
                _snackbarEvent.emit("Contribution added")
            } catch (e: Exception) {
                Log.e(TAG, "addContribution failed", e)
                _snackbarEvent.emit("Failed to add contribution")
            }
        }
    }

    fun withdrawFromGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            try {
                repository.withdrawFromGoal(goalId, amount)
                _snackbarEvent.emit("Withdrawal added")
            } catch (e: Exception) {
                Log.e(TAG, "withdrawFromGoal failed", e)
                _snackbarEvent.emit("Failed to withdraw")
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            try {
                val goal = _uiState.value.goals.find { it.id == id }
                repository.deleteSavingsGoal(id)
                if (goal != null) {
                    _uiState.update { it.copy(pendingUndo = UndoData.GoalUndo(goal)) }
                }
                _snackbarEvent.emit("Goal deleted")
            } catch (e: Exception) {
                Log.e(TAG, "deleteGoal failed", e)
                _snackbarEvent.emit("Failed to delete goal")
            }
        }
    }

    fun markOnboardingSeen() {
        viewModelScope.launch {
            repository.setHasSeenOnboarding(true)
        }
    }

    fun showCustomization() { _uiState.update { it.copy(showCustomizationSheet = true) } }
    fun dismissCustomization() { _uiState.update { it.copy(showCustomizationSheet = false) } }

    fun showSettings() { _uiState.update { it.copy(showSettings = true) } }
    fun dismissSettings() { _uiState.update { it.copy(showSettings = false) } }

    fun showCalendarSheet() { _uiState.update { it.copy(showCalendarSheet = true) } }
    fun dismissCalendarSheet() { _uiState.update { it.copy(showCalendarSheet = false) } }

    fun showAccountSwitcher() { _uiState.update { it.copy(showAccountSwitcher = true) } }
    fun dismissAccountSwitcher() { _uiState.update { it.copy(showAccountSwitcher = false) } }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            repository.setCurrentAccount(accountId)
            _uiState.update { it.copy(showAccountSwitcher = false) }
        }
    }

    fun moveCard(from: Int, to: Int) {
        val current = _uiState.value.dashboardOrder.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            _uiState.update { it.copy(dashboardOrder = current) }
            viewModelScope.launch { repository.setDashboardOrder(current) }
        }
    }

    fun toggleCardVisibility(card: String) {
        val hidden = _uiState.value.dashboardHidden.toMutableSet()
        if (hidden.contains(card)) hidden.remove(card) else hidden.add(card)
        _uiState.update { it.copy(dashboardHidden = hidden) }
        viewModelScope.launch { repository.setDashboardHidden(hidden) }
    }

    fun setCompactMode(enabled: Boolean) {
        _uiState.update { it.copy(isCompactMode = enabled) }
        viewModelScope.launch { repository.setCompactMode(enabled) }
    }

    fun undoLastDeletion() {
        val pending = _uiState.value.pendingUndo ?: return
        viewModelScope.launch {
            try {
                when (pending) {
                    is UndoData.BudgetUndo -> repository.setBudget(pending.category, pending.limit)
                    is UndoData.GoalUndo -> {
                        // Re-insert with same ID via repository
                        repository.restoreSavingsGoal(pending.goal)
                    }
                    is UndoData.CategoryUndo -> repository.addCategory(pending.name, pending.icon)
                    is UndoData.SubscriptionUndo -> repository.addSubscription(
                        pending.subscription.name,
                        pending.subscription.amount,
                        pending.subscription.category,
                        pending.subscription.billingDay,
                        pending.subscription.notes,
                        pending.subscription.type
                    )
                    is UndoData.QuickAddUndo -> repository.addQuickAddItem(
                        pending.item.name,
                        pending.item.amount,
                        pending.item.category,
                        pending.item.notes,
                        pending.item.type
                    )
                }
                _uiState.update { it.copy(pendingUndo = null) }
                _snackbarEvent.emit("Restored")
            } catch (e: Exception) {
                Log.e(TAG, "undo failed", e)
                _snackbarEvent.emit("Failed to restore")
            }
        }
    }

    fun clearPendingUndo() {
        _uiState.update { it.copy(pendingUndo = null) }
    }

    fun addSubscription(name: String, amount: Double, category: String, billingDay: Int, notes: String, type: TransactionType = TransactionType.EXPENSE) {
        viewModelScope.launch {
            try {
                repository.addSubscription(name, amount, category, billingDay, notes, type)
                repository.scheduleSubscriptionReminders(EHTrackerApplication.instance)
                _snackbarEvent.emit(if (type == TransactionType.INCOME) "Recurring income added" else "Recurring expense added")
            } catch (e: Exception) {
                Log.e(TAG, "addSubscription failed", e)
                _snackbarEvent.emit("Failed to add recurring item")
            }
        }
    }

    fun updateSubscription(id: String, name: String, amount: Double, category: String, billingDay: Int, notes: String, type: TransactionType = TransactionType.EXPENSE) {
        viewModelScope.launch {
            try {
                repository.updateSubscription(id, name, amount, category, billingDay, notes, type)
                repository.scheduleSubscriptionReminders(EHTrackerApplication.instance)
                _snackbarEvent.emit("Recurring item updated")
            } catch (e: Exception) {
                Log.e(TAG, "updateSubscription failed", e)
                _snackbarEvent.emit("Failed to update recurring item")
            }
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            try {
                val sub = _uiState.value.subscriptions.find { it.id == id }
                repository.deleteSubscription(id)
                if (sub != null) {
                    _uiState.update { it.copy(pendingUndo = UndoData.SubscriptionUndo(sub)) }
                }
                NotificationHelper.cancelAllSubscriptionReminders(EHTrackerApplication.instance, id)
                repository.scheduleSubscriptionReminders(EHTrackerApplication.instance)
                _snackbarEvent.emit("Recurring item deleted")
            } catch (e: Exception) {
                Log.e(TAG, "deleteSubscription failed", e)
                _snackbarEvent.emit("Failed to delete recurring item")
            }
        }
    }

    fun toggleSubscription(id: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleSubscription(id, isActive)
                if (!isActive) {
                    NotificationHelper.cancelAllSubscriptionReminders(EHTrackerApplication.instance, id)
                }
                repository.scheduleSubscriptionReminders(EHTrackerApplication.instance)
            } catch (e: Exception) {
                Log.e(TAG, "toggleSubscription failed", e)
            }
        }
    }

    fun createQuickAddTransaction(id: String) {
        viewModelScope.launch {
            try {
                val success = repository.createQuickAddTransaction(id)
                if (success) {
                    _snackbarEvent.emit("Transaction added")
                } else {
                    _snackbarEvent.emit("Failed to add transaction")
                }
            } catch (e: Exception) {
                Log.e(TAG, "createQuickAddTransaction failed", e)
                _snackbarEvent.emit("Failed to add transaction")
            }
        }
    }

    fun updateQuickAddItem(id: String, name: String, amount: Double, category: String, notes: String, type: TransactionType = TransactionType.EXPENSE) {
        viewModelScope.launch {
            try {
                repository.updateQuickAddItem(id, name, amount, category, notes, type)
                _snackbarEvent.emit("Recurring button updated")
            } catch (e: Exception) {
                Log.e(TAG, "updateQuickAddItem failed", e)
                _snackbarEvent.emit("Failed to update recurring button")
            }
        }
    }

    fun deleteQuickAddItem(id: String) {
        viewModelScope.launch {
            try {
                val item = _uiState.value.quickAddItems.find { it.id == id }
                repository.deleteQuickAddItem(id)
                if (item != null) {
                    _uiState.update { it.copy(pendingUndo = UndoData.QuickAddUndo(item)) }
                }
                _snackbarEvent.emit("Recurring button deleted")
            } catch (e: Exception) {
                Log.e(TAG, "deleteQuickAddItem failed", e)
                _snackbarEvent.emit("Failed to delete recurring button")
            }
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            when (repository.addCategory(name, icon)) {
                TrackerRepository.CategoryResult.Success -> {}
                TrackerRepository.CategoryResult.DuplicateName -> _snackbarEvent.emit("A category with that name already exists")
                else -> _snackbarEvent.emit("Failed to create category")
            }
        }
    }

    fun updateCategory(id: String, name: String, icon: String) {
        viewModelScope.launch {
            when (repository.updateCategory(id, name, icon)) {
                TrackerRepository.CategoryResult.Success -> _snackbarEvent.emit("Category updated")
                TrackerRepository.CategoryResult.DuplicateName -> _snackbarEvent.emit("A category with that name already exists")
                else -> _snackbarEvent.emit("Failed to update category")
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            val cat = _uiState.value.categories.find { it.id == id }
            when (repository.deleteCategory(id)) {
                TrackerRepository.CategoryResult.Success -> {
                    if (cat != null) {
                        _uiState.update { it.copy(pendingUndo = UndoData.CategoryUndo(cat.id, cat.name, cat.icon)) }
                    }
                    _snackbarEvent.emit("Category deleted")
                }
                TrackerRepository.CategoryResult.InUse -> _snackbarEvent.emit("Category is in use and cannot be deleted")
                else -> _snackbarEvent.emit("Failed to delete category")
            }
        }
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
