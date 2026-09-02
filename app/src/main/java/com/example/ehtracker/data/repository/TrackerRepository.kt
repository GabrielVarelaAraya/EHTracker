package com.example.ehtracker.data.repository

import com.example.ehtracker.NotificationHelper
import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.local.dao.CategoryDao
import com.example.ehtracker.data.local.entity.AccountEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CategoryEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity
import com.example.ehtracker.data.local.entity.PreferencesEntity
import com.example.ehtracker.data.local.entity.QuickAddEntity
import com.example.ehtracker.data.local.entity.SavingsGoalEntity
import com.example.ehtracker.data.local.entity.SubscriptionEntity
import com.example.ehtracker.data.model.Account
import com.example.ehtracker.data.model.CATEGORY_SAVINGS
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Income
import com.example.ehtracker.data.model.QuickAddItem
import com.example.ehtracker.data.model.SavingsGoal
import com.example.ehtracker.data.model.Subscription
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.model.TransactionType
import com.example.ehtracker.data.model.toTransaction
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class TrackerRepository(private val database: AppDatabase) {

    private val habitDao = database.habitDao()
    private val completionDao = database.habitCompletionDao()
    private val expenseDao = database.expenseDao()
    private val incomeDao = database.incomeDao()
    private val currencyDao = database.currencyDao()
    private val budgetDao = database.budgetDao()
    private val preferencesDao = database.preferencesDao()
    private val subscriptionDao = database.subscriptionDao()
    private val accountDao = database.accountDao()
    private val quickAddDao = database.quickAddDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val categoryDao: CategoryDao = database.categoryDao()

    private val _currentAccountId = MutableStateFlow<String?>(null)
    val currentAccountId: StateFlow<String?> = _currentAccountId.asStateFlow()

    suspend fun setCurrentAccount(id: String?) {
        _currentAccountId.value = id
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(lastSelectedAccountId = id))
    }

    suspend fun restoreCurrentAccount() {
        val savedId = preferencesDao.get().first()?.lastSelectedAccountId
        if (savedId != null && accountDao.getById(savedId) != null) {
            _currentAccountId.value = savedId
        }
    }

    data class AccountSummary(val account: Account, val balance: Double)

    // --- Accounts ---

    fun accounts(): Flow<List<Account>> {
        return accountDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    fun currentAccount(): Flow<Account?> {
        return combine(_currentAccountId, accountDao.getAll()) { accountId, accounts ->
            accounts.firstOrNull { it.id == accountId }?.toDomain()
        }
    }

    fun accountSummaries(): Flow<List<AccountSummary>> {
        return combine(
            accountDao.getAll(),
            expenseDao.getAll(),
            incomeDao.getAll()
        ) { accounts, expenses, incomes ->
            accounts.map { account ->
                val exp = expenses.filter { it.accountId == account.id }.map { it.amount }
                val inc = incomes.filter { it.accountId == account.id && !it.id.startsWith("initial_") }.map { it.amount }
                AccountSummary(account.toDomain(), account.initialBalance + inc.sum() - exp.sum())
            }
        }
    }

    suspend fun addAccount(name: String, icon: String, color: String, initialBalance: Double, currency: Currency) {
        val id = UUID.randomUUID().toString()
        accountDao.insert(
            AccountEntity(
                id = id,
                name = name,
                icon = icon,
                color = color,
                initialBalance = initialBalance,
                currencyCode = currency.code,
                createdAt = System.currentTimeMillis()
            )
        )
        syncInitialBalanceIncome(id, initialBalance)
    }

    suspend fun updateAccount(id: String, name: String, icon: String, color: String, initialBalance: Double, currency: Currency) {
        val existing = accountDao.getById(id) ?: return
        accountDao.insert(
            existing.copy(
                name = name,
                icon = icon,
                color = color,
                initialBalance = initialBalance,
                currencyCode = currency.code
            )
        )
        if (initialBalance != existing.initialBalance) {
            syncInitialBalanceIncome(id, initialBalance)
        }
    }

    suspend fun updateAccountInitialBalance(id: String, initialBalance: Double) {
        val existing = accountDao.getById(id) ?: return
        accountDao.insert(existing.copy(initialBalance = initialBalance))
        syncInitialBalanceIncome(id, initialBalance)
    }

    private suspend fun syncInitialBalanceIncome(accountId: String, initialBalance: Double) {
        incomeDao.deleteById("initial_$accountId")
        if (initialBalance != 0.0) {
            incomeDao.insert(
                IncomeEntity(
                    id = "initial_$accountId",
                    amount = initialBalance,
                    note = "Initial balance",
                    date = LocalDate.now().toEpochMillis(),
                    accountId = accountId
                )
            )
        }
    }

    suspend fun updateAccountCurrency(id: String, currency: Currency) {
        val existing = accountDao.getById(id) ?: return
        accountDao.insert(existing.copy(currencyCode = currency.code))
    }

    suspend fun deleteAccount(id: String) {
        expenseDao.setAccountIdNull(id)
        incomeDao.setAccountIdNull(id)
        subscriptionDao.setAccountIdNull(id)
        quickAddDao.setAccountIdNull(id)
        incomeDao.deleteById("initial_$id")
        accountDao.deleteById(id)
        if (_currentAccountId.value == id) _currentAccountId.value = null
    }

    // --- Habits ---

    fun habitsWithCompletions(): Flow<List<Habit>> {
        return combine(
            habitDao.getAll(),
            completionDao.getAll()
        ) { habits, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }
            habits.map { entity ->
                entity.toDomain(completionsByHabit[entity.id] ?: emptyList())
            }
        }
    }

    suspend fun toggleHabitCompletion(habitId: String, date: LocalDate, value: Double? = null) {
        val dateLong = date.toEpochMillis()
        val existing = completionDao.getByHabitIdAndDate(habitId, dateLong)
        if (existing != null) {
            if (value != null) {
                completionDao.insert(existing.copy(value = value))
            } else {
                completionDao.deleteByHabitIdAndDate(habitId, dateLong)
            }
        } else {
            completionDao.insert(
                HabitCompletionEntity(
                    habitId = habitId,
                    date = dateLong,
                    value = if (value != null) value else null
                )
            )
        }
    }

    suspend fun setHabitValue(habitId: String, date: LocalDate, value: Double) {
        val dateLong = date.toEpochMillis()
        val existing = completionDao.getByHabitIdAndDate(habitId, dateLong)
        if (existing != null) {
            completionDao.insert(existing.copy(value = value))
        } else {
            completionDao.insert(
                HabitCompletionEntity(
                    habitId = habitId,
                    date = dateLong,
                    value = value
                )
            )
        }
    }

    suspend fun addHabit(name: String, icon: String, targetDaysPerWeek: Int = 7, isNumeric: Boolean = false, unit: String = "") {
        habitDao.insert(
            HabitEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                targetDaysPerWeek = targetDaysPerWeek,
                isNumeric = isNumeric,
                unit = unit
            )
        )
    }

    suspend fun updateHabit(id: String, name: String, icon: String, targetDaysPerWeek: Int, isNumeric: Boolean = false, unit: String = "") {
        habitDao.update(id, name, icon, targetDaysPerWeek, isNumeric, unit)
    }

    suspend fun deleteHabit(id: String) {
        habitDao.deleteById(id)
    }

    suspend fun incompleteHabitsTodayCount(): Int {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val habits = habitDao.getAll().first()
        val todayCompletions = completionDao.getByDateRange(startOfDay, endOfDay).first()
        val completedHabitIds = todayCompletions.map { it.habitId }.toSet()
        return habits.count { it.id !in completedHabitIds }
    }

    // --- Expenses ---

    private fun <T> Flow<List<T>>.scopedToAccount(accountIdOf: (T) -> String?): Flow<List<T>> {
        return combine(_currentAccountId, this) { accountId, list ->
            filterByAccount(list, accountId, accountIdOf)
        }
    }

    fun expenses(): Flow<List<Expense>> {
        return expenseDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map { it.toDomain() }
        }
    }

    fun expensesFiltered(query: String): Flow<List<Expense>> {
        return expenseDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map { it.toDomain() }.filter { expense ->
                query.isBlank() ||
                        expense.note.contains(query, ignoreCase = true) ||
                        expense.category.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun addExpense(amount: Double, category: String, note: String, date: LocalDate = LocalDate.now(), accountId: String? = null, goalId: String? = null) {
        expenseDao.insert(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category,
                note = note,
                date = date.toEpochMillis(),
                accountId = accountId ?: _currentAccountId.value,
                goalId = goalId
            )
        )
    }

    suspend fun updateExpense(id: String, amount: Double, category: String, note: String, date: LocalDate) {
        val existing = expenseDao.getById(id) ?: return
        if (existing.goalId != null) {
            val goal = savingsGoalDao.getById(existing.goalId)
            if (goal != null) {
                val newDelta = if (category == CATEGORY_SAVINGS) amount else 0.0
                val adjusted = (goal.currentAmount - existing.amount + newDelta).coerceAtLeast(0.0)
                savingsGoalDao.updateCurrentAmount(goal.id, adjusted)
            }
        }
        expenseDao.insert(
            ExpenseEntity(
                id = id,
                amount = amount,
                category = category,
                note = note,
                date = date.toEpochMillis(),
                accountId = existing.accountId ?: _currentAccountId.value,
                goalId = existing.goalId
            )
        )
    }

    suspend fun deleteExpense(id: String) {
        val existing = expenseDao.getById(id)
        if (existing?.goalId != null) {
            savingsGoalDao.getById(existing.goalId)?.let { goal ->
                val adjusted = (goal.currentAmount - existing.amount).coerceAtLeast(0.0)
                savingsGoalDao.updateCurrentAmount(goal.id, adjusted)
            }
        }
        expenseDao.deleteById(id)
    }

    // --- Income ---

    fun incomes(): Flow<List<Income>> {
        return incomeDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map { entity ->
                Income(
                    id = entity.id,
                    amount = entity.amount,
                    note = entity.note,
                    date = entity.date.toLocalDate(),
                    accountId = entity.accountId
                )
            }
        }
    }

    fun totalIncome(): Flow<Double> {
        return incomeDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.sumOf { it.amount }
        }
    }

    suspend fun addIncome(amount: Double, note: String, date: LocalDate, accountId: String? = null) {
        incomeDao.insert(
            IncomeEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                note = note,
                date = date.toEpochMillis(),
                accountId = accountId ?: _currentAccountId.value
            )
        )
    }

    suspend fun deleteIncome(id: String) {
        incomeDao.deleteById(id)
    }

    suspend fun updateIncome(id: String, amount: Double, note: String, date: LocalDate) {
        incomeDao.update(id, amount, note, date.toEpochMillis())
    }

    // --- Balance ---

    fun balance(): Flow<Double> {
        return combine(_currentAccountId, accountDao.getAll()) { accountId, accounts ->
            val targets = if (accountId == null) accounts else accounts.filter { it.id == accountId }
            targets.sumOf { it.initialBalance }
        }
    }

    fun totalAllExpenses(): Flow<Double> {
        return expenseDao.getAll().scopedToAccount { it.accountId }.map { list -> list.sumOf { it.amount } }
    }

    fun totalSpending(): Flow<Double> {
        return expenseDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.filter { it.category != CATEGORY_SAVINGS }.sumOf { it.amount }
        }
    }

    fun currentBalance(): Flow<Double> {
        return combine(totalAllExpenses(), totalIncome()) { expenses, income ->
            income - expenses
        }
    }

    suspend fun setBalance(amount: Double) {
        val currentAccountId = _currentAccountId.value ?: return
        updateAccountInitialBalance(currentAccountId, amount)
    }

    // --- Currency ---

    fun currency(): Flow<Currency> {
        return combine(_currentAccountId, accountDao.getAll(), currencyDao.get()) { accountId, accounts, global ->
            val account = accounts.firstOrNull { it.id == accountId }
            if (account != null) Currency.fromCode(account.currencyCode)
            else Currency.fromCode(global?.code ?: "USD")
        }
    }

    suspend fun setCurrency(currency: Currency) {
        val currentAccountId = _currentAccountId.value
        if (currentAccountId != null) {
            updateAccountCurrency(currentAccountId, currency)
        } else {
            currencyDao.set(CurrencyEntity(code = currency.code))
        }
    }

    // --- Budgets ---

    fun budgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAll()
    }

    suspend fun setBudget(category: String, monthlyLimit: Double) {
        budgetDao.insert(BudgetEntity(category = category, monthlyLimit = monthlyLimit))
    }

    suspend fun deleteBudget(category: String) {
        budgetDao.deleteByCategory(category)
    }

    fun budgetStatus(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Pair<Double, Double>>> {
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        val totalMonths = java.time.temporal.ChronoUnit.MONTHS.between(
            startDate.withDayOfMonth(1),
            endDate.withDayOfMonth(1)
        ) + 1
        val monthsFactor = if (totalDays >= startDate.lengthOfMonth().toDouble()) {
            totalMonths.toDouble()
        } else {
            totalDays / startDate.lengthOfMonth().toDouble()
        }
        return combine(
            budgetDao.getAll(),
            expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }
        ) { budgets, expenses ->
            val spentByCategory = expenses.filter { it.category != CATEGORY_SAVINGS }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            budgets.associate { budget ->
                budget.category to Pair(spentByCategory[budget.category] ?: 0.0, budget.monthlyLimit * monthsFactor)
            }
        }
    }

    // --- Subscriptions ---

    fun subscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map { it.toDomain() }
        }
    }

    fun activeSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getActive().scopedToAccount { it.accountId }.map { list ->
            list.map { it.toDomain() }
        }
    }

    fun totalMonthlySubscriptions(): Flow<Double> {
        return subscriptionDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.filter { it.isActive && it.type == "expense" }.sumOf { it.amount }
        }
    }

    suspend fun addSubscription(name: String, amount: Double, category: String, billingDay: Int, notes: String = "", type: TransactionType = TransactionType.EXPENSE) {
        val today = LocalDate.now()
        val targetDay = billingDay.coerceIn(1, today.lengthOfMonth())
        val billingPassed = today.withDayOfMonth(targetDay).isBefore(today)
        subscriptionDao.insert(
            SubscriptionEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                amount = amount,
                category = category,
                billingDay = billingDay,
                notes = notes,
                type = type.name.lowercase(),
                accountId = _currentAccountId.value,
                lastCreatedMonth = if (billingPassed) today.toString().substring(0, 7) else null
            )
        )
    }

    suspend fun updateSubscription(id: String, name: String, amount: Double, category: String, billingDay: Int, notes: String = "", type: TransactionType = TransactionType.EXPENSE) {
        val existing = subscriptionDao.getById(id) ?: return
        subscriptionDao.insert(
            existing.copy(
                name = name,
                amount = amount,
                category = category,
                billingDay = billingDay,
                notes = notes,
                type = type.name.lowercase()
            )
        )
    }

    suspend fun deleteSubscription(id: String) {
        subscriptionDao.deleteById(id)
    }

    suspend fun toggleSubscription(id: String, isActive: Boolean) {
        subscriptionDao.setActive(id, isActive)
    }

    // --- Quick Add (recurring buttons) ---

    fun quickAddItems(): Flow<List<QuickAddItem>> {
        return quickAddDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addQuickAddItem(name: String, amount: Double, category: String, notes: String = "", type: TransactionType = TransactionType.EXPENSE) {
        quickAddDao.insert(
            QuickAddEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                amount = amount,
                category = category,
                notes = notes,
                type = type.name.lowercase(),
                accountId = _currentAccountId.value
            )
        )
    }

    suspend fun updateQuickAddItem(id: String, name: String, amount: Double, category: String, notes: String = "", type: TransactionType = TransactionType.EXPENSE) {
        val existing = quickAddDao.getById(id) ?: return
        quickAddDao.insert(
            existing.copy(
                name = name,
                amount = amount,
                category = category,
                notes = notes,
                type = type.name.lowercase()
            )
        )
    }

    suspend fun deleteQuickAddItem(id: String) {
        quickAddDao.deleteById(id)
    }

    suspend fun createQuickAddTransaction(id: String): Boolean {
        val item = quickAddDao.getById(id) ?: return false
        val today = LocalDate.now()
        val note = item.name + if (item.notes.isNotBlank()) " - ${item.notes}" else ""
        if (item.type == "expense") {
            addExpense(
                amount = item.amount,
                category = item.category,
                note = note,
                date = today,
                accountId = item.accountId ?: _currentAccountId.value
            )
        } else {
            addIncome(
                amount = item.amount,
                note = note,
                date = today,
                accountId = item.accountId ?: _currentAccountId.value
            )
        }
        return true
    }

    // --- Savings Goals ---

    fun savingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAll().scopedToAccount { it.accountId }.map { list ->
            list.map {
                SavingsGoal(
                    id = it.id,
                    name = it.name,
                    targetAmount = it.targetAmount,
                    currentAmount = it.currentAmount
                )
            }
        }
    }

    suspend fun addSavingsGoal(name: String, targetAmount: Double) {
        savingsGoalDao.insert(
            SavingsGoalEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                accountId = _currentAccountId.value
            )
        )
    }

    suspend fun updateSavingsGoal(id: String, name: String, targetAmount: Double) {
        val existing = savingsGoalDao.getById(id) ?: return
        savingsGoalDao.insert(
            existing.copy(name = name, targetAmount = targetAmount)
        )
    }

    suspend fun addContribution(goalId: String, amount: Double) {
        val existing = savingsGoalDao.getById(goalId) ?: return
        savingsGoalDao.updateCurrentAmount(goalId, existing.currentAmount + amount)
        addExpense(
            amount = amount,
            category = CATEGORY_SAVINGS,
            note = "Goal: ${existing.name}",
            date = LocalDate.now(),
            accountId = existing.accountId ?: _currentAccountId.value,
            goalId = goalId
        )
    }

    suspend fun withdrawFromGoal(goalId: String, amount: Double) {
        val existing = savingsGoalDao.getById(goalId) ?: return
        val withdraw = amount.coerceIn(0.0, existing.currentAmount)
        if (withdraw <= 0.0) return
        savingsGoalDao.updateCurrentAmount(goalId, existing.currentAmount - withdraw)
        addExpense(
            amount = -withdraw,
            category = CATEGORY_SAVINGS,
            note = "Goal: ${existing.name}",
            date = LocalDate.now(),
            accountId = existing.accountId ?: _currentAccountId.value,
            goalId = goalId
        )
    }

    suspend fun deleteSavingsGoal(id: String) {
        savingsGoalDao.deleteById(id)
    }

    suspend fun restoreSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.insert(
            SavingsGoalEntity(
                id = goal.id,
                name = goal.name,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                accountId = _currentAccountId.value,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // --- Categories ---

    sealed class CategoryResult {
        data object Success : CategoryResult()
        data object DuplicateName : CategoryResult()
        data object InUse : CategoryResult()
        data object NotFound : CategoryResult()
    }

    fun categories(): Flow<List<Category>> {
        return categoryDao.getAllActive().map { list ->
            list.map { Category(id = it.id, name = it.name, icon = it.icon, isBuiltIn = it.isBuiltIn) }
        }
    }

    suspend fun addCategory(name: String, icon: String): CategoryResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return CategoryResult.DuplicateName
        if (categoryDao.countByIdOrNameExcluding(trimmed, trimmed, "") > 0) return CategoryResult.DuplicateName
        categoryDao.insert(
            CategoryEntity(
                id = trimmed,
                name = trimmed,
                icon = icon.ifBlank { "\uD83D\uDCE6" },
                isBuiltIn = false,
                archived = false,
                createdAt = System.currentTimeMillis()
            )
        )
        return CategoryResult.Success
    }

    suspend fun updateCategory(id: String, newName: String, newIcon: String): CategoryResult {
        val existing = categoryDao.getById(id) ?: return CategoryResult.NotFound
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return CategoryResult.DuplicateName
        val iconToUse = newIcon.ifBlank { existing.icon }

        if (existing.isBuiltIn) {
            if (trimmed != existing.name && categoryDao.countByIdOrNameExcluding(trimmed, trimmed, id) > 0) {
                return CategoryResult.DuplicateName
            }
            categoryDao.renameDisplay(id, trimmed)
            categoryDao.updateIcon(id, iconToUse)
            return CategoryResult.Success
        }

        val newId = trimmed
        if (newId != id) {
            if (categoryDao.countByIdOrNameExcluding(newId, newId, id) > 0) return CategoryResult.DuplicateName
            categoryDao.cascadeExpenses(id, newId)
            categoryDao.cascadeBudgets(id, newId)
            categoryDao.cascadeSubscriptions(id, newId)
            categoryDao.cascadeQuickItems(id, newId)
            categoryDao.changeId(id, newId, trimmed)
            categoryDao.updateIcon(newId, iconToUse)
        } else {
            categoryDao.renameDisplay(id, trimmed)
            categoryDao.updateIcon(id, iconToUse)
        }
        return CategoryResult.Success
    }

    suspend fun deleteCategory(id: String): CategoryResult {
        val existing = categoryDao.getById(id) ?: return CategoryResult.NotFound
        if (existing.isBuiltIn || id == CATEGORY_SAVINGS) return CategoryResult.NotFound
        val inUse = categoryDao.countExpensesUsing(id) > 0 ||
                categoryDao.countBudgetsUsing(id) > 0 ||
                categoryDao.countSubscriptionsUsing(id) > 0 ||
                categoryDao.countQuickItemsUsing(id) > 0
        if (inUse) return CategoryResult.InUse
        categoryDao.deleteById(id)
        return CategoryResult.Success
    }

    suspend fun scheduleSubscriptionReminders(context: Context) {
        val today = LocalDate.now()
        val prefs = getNotificationPrefs()
        val hour = prefs.first
        val minute = prefs.second
        val subscriptions = subscriptionDao.getActiveByType("expense").first()
        val currencySymbol = currency().first().symbol
        for (sub in subscriptions) {
            for (monthOffset in 0..1) {
                val month = today.plusMonths(monthOffset.toLong())
                val billingDay = sub.billingDay.coerceIn(1, month.lengthOfMonth())
                val billingDate = month.withDayOfMonth(billingDay)
                val reminder3 = billingDate.minusDays(3)
                val reminder1 = billingDate.minusDays(1)
                if (!reminder3.isBefore(today)) {
                    NotificationHelper.scheduleSubscriptionReminder(
                        context, sub.id, sub.name, sub.amount, reminder3, 3, hour, minute, currencySymbol
                    )
                }
                if (!reminder1.isBefore(today)) {
                    NotificationHelper.scheduleSubscriptionReminder(
                        context, sub.id, sub.name, sub.amount, reminder1, 1, hour, minute, currencySymbol
                    )
                }
            }
        }
    }

    suspend fun checkAndCreateDueSubscriptions(): Int {
        val today = LocalDate.now()
        val monthKey = today.toString().substring(0, 7)
        val subscriptions = subscriptionDao.getActive().first()
        var created = 0
        for (sub in subscriptions) {
            if (sub.lastCreatedMonth == monthKey) continue
            val billingDay = sub.billingDay.coerceIn(1, today.lengthOfMonth())
            val billingDate = today.withDayOfMonth(billingDay)
            if (billingDate.isAfter(today)) continue
            val note = sub.name + if (sub.notes.isNotBlank()) " - ${sub.notes}" else ""
            if (sub.type == "expense") {
                addExpense(
                    amount = sub.amount,
                    category = sub.category,
                    note = note,
                    date = billingDate,
                    accountId = sub.accountId ?: _currentAccountId.value
                )
            } else {
                addIncome(
                    amount = sub.amount,
                    note = note,
                    date = billingDate,
                    accountId = sub.accountId ?: _currentAccountId.value
                )
            }
            subscriptionDao.insert(sub.copy(lastCreatedMonth = monthKey))
            created++
        }
        return created
    }

    // --- Preferences ---

    fun themeMode(): Flow<String> {
        return preferencesDao.get().map { it?.themeMode ?: "system" }
    }

    suspend fun setThemeMode(mode: String) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(themeMode = mode))
    }

    suspend fun getActiveSubscriptionsOnce(): List<Subscription> {
        return subscriptionDao.getActive().first().map { it.toDomain() }
    }

    suspend fun getNotificationPrefs(): Triple<Int, Int, Boolean> {
        val prefs = preferencesDao.get().first()
        return Triple(
            prefs?.notificationHour ?: 20,
            prefs?.notificationMinute ?: 0,
            prefs?.notificationsEnabled ?: true
        )
    }

    fun notificationHour(): Flow<Int> {
        return preferencesDao.get().map { it?.notificationHour ?: 20 }
    }

    fun notificationMinute(): Flow<Int> {
        return preferencesDao.get().map { it?.notificationMinute ?: 0 }
    }

    fun notificationsEnabled(): Flow<Boolean> {
        return preferencesDao.get().map { it?.notificationsEnabled ?: true }
    }

    suspend fun setNotificationHour(hour: Int) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(notificationHour = hour))
    }

    suspend fun setNotificationMinute(minute: Int) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(notificationMinute = minute))
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(notificationsEnabled = enabled))
    }

    fun biometricEnabled(): Flow<Boolean> {
        return preferencesDao.get().map { it?.biometricEnabled ?: false }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(biometricEnabled = enabled))
    }

    fun hasSeenOnboarding(): Flow<Boolean> {
        return preferencesDao.get().map { it?.hasSeenOnboarding ?: false }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(hasSeenOnboarding = seen))
    }

    fun dashboardOrder(): Flow<List<String>?> {
        return preferencesDao.get().map { prefs ->
            prefs?.dashboardOrder?.split(",")?.filter { it.isNotBlank() }
        }
    }

    suspend fun setDashboardOrder(order: List<String>) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(dashboardOrder = order.joinToString(",")))
    }

    fun dashboardHidden(): Flow<Set<String>> {
        return preferencesDao.get().map { prefs ->
            prefs?.dashboardHidden?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        }
    }

    suspend fun setDashboardHidden(hidden: Set<String>) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(dashboardHidden = hidden.joinToString(",")))
    }

    fun isCompactMode(): Flow<Boolean> {
        return preferencesDao.get().map { it?.isCompactMode ?: false }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(isCompactMode = enabled))
    }

    // --- Analytics ---

    data class ExpenseTotals(val today: Double, val week: Double, val month: Double)

    fun expenseTotals(): Flow<ExpenseTotals> {
        return expenseDao.getAll().scopedToAccount { it.accountId }.map { allExpenses ->
            val expenses = allExpenses.filter { it.category != CATEGORY_SAVINGS }
            val today = LocalDate.now()
            val todayMillis = today.toEpochMillis()
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val endOfWeek = startOfWeek.plusDays(6)
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())

            Log.d("TrackerRepo", "=== expenseTotals ===")
            Log.d("TrackerRepo", "today=$today (epoch=$todayMillis) dayOfWeek=${today.dayOfWeek}")
            Log.d("TrackerRepo", "weekRange=$startOfWeek..$endOfWeek monthRange=$startOfMonth..$endOfMonth")
            Log.d("TrackerRepo", "expense count=${expenses.size}")
            expenses.forEach { e ->
                val date = e.date.toLocalDate()
                Log.d("TrackerRepo", "  expense id=${e.id} amount=${e.amount} storedEpoch=${e.date} date=$date")
            }

            val todaySum = expenses.filter { it.date == todayMillis }.sumOf { it.amount }
            val weekSum = expenses.filter {
                val d = it.date
                d >= startOfWeek.toEpochMillis() && d <= endOfWeek.toEpochMillis()
            }.sumOf { it.amount }
            val monthSum = expenses.filter {
                val d = it.date
                d >= startOfMonth.toEpochMillis() && d <= endOfMonth.toEpochMillis()
            }.sumOf { it.amount }

            Log.d("TrackerRepo", "todaySum=$todaySum weekSum=$weekSum monthSum=$monthSum")
            ExpenseTotals(todaySum, weekSum, monthSum)
        }
    }

    fun thisWeekExpenses(): Flow<Double> = expenseTotals().map { it.week }

    fun thisMonthExpenses(): Flow<Double> = expenseTotals().map { it.month }

    fun expensesForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }.map { list ->
            list.filter { it.category != CATEGORY_SAVINGS }.map { it.toDomain() }
        }
    }

    private fun fillMissingDates(data: Map<LocalDate, Double>, start: LocalDate, end: LocalDate): Map<LocalDate, Double> {
        val result = mutableMapOf<LocalDate, Double>()
        var current = start
        while (!current.isAfter(end)) {
            result[current] = data[current] ?: 0.0
            current = current.plusDays(1)
        }
        return result
    }

    fun dailyExpensesForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, Double>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }.map { list ->
            val grouped = list.filter { it.category != CATEGORY_SAVINGS }.groupBy { entity ->
                entity.date.toLocalDate()
            }.mapValues { entry -> entry.value.sumOf { it.amount } }
            fillMissingDates(grouped, startDate, endDate)
        }
    }

    fun dailyIncomesForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, Double>> {
        return incomeDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }.map { list ->
            val grouped = list.filterNot { it.id.startsWith("initial_") }.groupBy { entity ->
                entity.date.toLocalDate()
            }.mapValues { entry -> entry.value.sumOf { it.amount } }
            fillMissingDates(grouped, startDate, endDate)
        }
    }

    fun expensesByCategoryForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Double>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }.map { list ->
            list.filter { it.category != CATEGORY_SAVINGS }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    fun dailyExpensesForMonth(): Flow<Map<LocalDate, Double>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return dailyExpensesForRange(startOfMonth, today)
    }

    fun expensesByCategory(): Flow<Map<String, Double>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return expensesByCategoryForRange(startOfWeek, today)
    }

    fun transactionsForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return combine(
            expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId },
            incomeDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).scopedToAccount { it.accountId }
        ) { expenses, incomes ->
            buildList<Transaction> {
                expenses.forEach { add(it.toDomain().toTransaction()) }
                incomes.forEach {
                    val inc = Income(it.id, it.amount, it.note, it.date.toLocalDate(), it.accountId)
                    add(inc.toTransaction())
                }
            }.sortedByDescending { it.date }
        }
    }

    fun habitCompletionRateByDays(startDate: LocalDate, endDate: LocalDate): Flow<Float> {
        return combine(
            habitDao.getAll(),
            completionDao.getAll()
        ) { habits, completions ->
            if (habits.isEmpty()) return@combine 0f
            val startMillis = startDate.toEpochMillis()
            val endMillis = endDate.toEpochMillis()
            val completionsInRange = completions.count {
                it.date >= startMillis && it.date <= endMillis
            }
            val daysInRange = endDate.toEpochDay() - startDate.toEpochDay() + 1
            val totalPossible = habits.size * daysInRange.toInt()
            completionsInRange.toFloat() / totalPossible.coerceAtLeast(1)
        }
    }

    // --- Mapping ---

    private fun HabitEntity.toDomain(completions: List<HabitCompletionEntity>): Habit {
        val completedDates = completions.map { entity ->
            entity.date.toLocalDate()
        }
        val completionValues = completions.filter { it.value != null }.associate { entity ->
            entity.date.toLocalDate() to (entity.value ?: 0.0)
        }
        return Habit(
            id = id,
            name = name,
            icon = icon,
            completedDates = completedDates,
            targetDaysPerWeek = targetDaysPerWeek,
            isNumeric = isNumeric,
            unit = unit,
            completionValues = completionValues
        )
    }

    private fun AccountEntity.toDomain(): Account {
        return Account(
            id = id,
            name = name,
            icon = icon,
            color = color,
            initialBalance = initialBalance,
            currency = Currency.fromCode(currencyCode),
            createdAt = createdAt
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            category = category,
            note = note,
            date = date.toLocalDate(),
            accountId = accountId
        )
    }

    private fun SubscriptionEntity.toDomain(): Subscription {
        return Subscription(
            id = id,
            name = name,
            amount = amount,
            category = category,
            billingDay = billingDay,
            notes = notes,
            isActive = isActive,
            type = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
            accountId = accountId
        )
    }

    private fun QuickAddEntity.toDomain(): QuickAddItem {
        return QuickAddItem(
            id = id,
            name = name,
            amount = amount,
            category = category,
            notes = notes,
            type = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
            accountId = accountId
        )
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
