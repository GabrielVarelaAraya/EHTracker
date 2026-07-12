package com.example.ehtracker.data.repository

import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.local.entity.BalanceEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity
import com.example.ehtracker.data.local.entity.PreferencesEntity
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Income
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.model.toTransaction
import android.util.Log
import kotlinx.coroutines.flow.Flow
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
    private val balanceDao = database.balanceDao()
    private val currencyDao = database.currencyDao()
    private val budgetDao = database.budgetDao()
    private val preferencesDao = database.preferencesDao()

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

    fun expenses(): Flow<List<Expense>> {
        return expenseDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun expensesFiltered(query: String): Flow<List<Expense>> {
        return expenseDao.getAll().map { list ->
            list.map { it.toDomain() }.filter { expense ->
                query.isBlank() ||
                expense.note.contains(query, ignoreCase = true) ||
                expense.category.displayName.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun addExpense(amount: Double, category: ExpenseCategory, note: String, date: LocalDate = LocalDate.now()) {
        expenseDao.insert(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category.name,
                note = note,
                date = date.toEpochMillis()
            )
        )
    }

    suspend fun updateExpense(id: String, amount: Double, category: ExpenseCategory, note: String, date: LocalDate) {
        expenseDao.insert(
            ExpenseEntity(
                id = id,
                amount = amount,
                category = category.name,
                note = note,
                date = date.toEpochMillis()
            )
        )
    }

    suspend fun deleteExpense(id: String) {
        expenseDao.deleteById(id)
    }

    // --- Income ---

    fun incomes(): Flow<List<Income>> {
        return incomeDao.getAll().map { list ->
            list.map { entity ->
                Income(
                    id = entity.id,
                    amount = entity.amount,
                    note = entity.note,
                    date = entity.date.toLocalDate()
                )
            }
        }
    }

    fun totalIncome(): Flow<Double> {
        return incomeDao.totalAll().map { it ?: 0.0 }
    }

    suspend fun addIncome(amount: Double, note: String, date: LocalDate) {
        incomeDao.insert(
            IncomeEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                note = note,
                date = date.toEpochMillis()
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
        return balanceDao.get().map { it?.amount ?: 0.0 }
    }

    fun totalAllExpenses(): Flow<Double> {
        return expenseDao.totalAll().map { it ?: 0.0 }
    }

    fun currentBalance(): Flow<Double> {
        return combine(balance(), totalAllExpenses(), totalIncome()) { bal, expenses, income ->
            bal + income - expenses
        }
    }

    suspend fun setBalance(amount: Double) {
        balanceDao.set(BalanceEntity(amount = amount))
    }

    // --- Currency ---

    fun currency(): Flow<Currency> {
        return currencyDao.get().map { Currency.fromCode(it?.code ?: "USD") }
    }

    suspend fun setCurrency(currency: Currency) {
        currencyDao.set(CurrencyEntity(code = currency.code))
    }

    // --- Budgets ---

    fun budgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAll()
    }

    suspend fun setBudget(category: ExpenseCategory, monthlyLimit: Double) {
        budgetDao.insert(BudgetEntity(category = category.name, monthlyLimit = monthlyLimit))
    }

    suspend fun deleteBudget(category: ExpenseCategory) {
        budgetDao.deleteByCategory(category.name)
    }

    fun budgetStatus(): Flow<Map<ExpenseCategory, Pair<Double, Double>>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())
        return combine(
            budgetDao.getAll(),
            expenseDao.getByDateRange(startOfMonth.toEpochMillis(), endOfMonth.toEpochMillis())
        ) { budgets, expenses ->
            val spentByCategory = expenses.groupBy { ExpenseCategory.valueOf(it.category) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            budgets.associate { budget ->
                val cat = ExpenseCategory.valueOf(budget.category)
                cat to Pair(spentByCategory[cat] ?: 0.0, budget.monthlyLimit)
            }
        }
    }

    // --- Preferences ---

    fun themeMode(): Flow<String> {
        return preferencesDao.get().map { it?.themeMode ?: "system" }
    }

    suspend fun setThemeMode(mode: String) {
        val current = preferencesDao.get().first()
        preferencesDao.set((current ?: PreferencesEntity()).copy(themeMode = mode))
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

    // --- Analytics ---

    data class ExpenseTotals(val today: Double, val week: Double, val month: Double)

    fun expenseTotals(): Flow<ExpenseTotals> {
        return expenseDao.getAll().map { expenses ->
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
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            list.map { it.toDomain() }
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
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            val grouped = list.groupBy { entity ->
                entity.date.toLocalDate()
            }.mapValues { entry -> entry.value.sumOf { it.amount } }
            fillMissingDates(grouped, startDate, endDate)
        }
    }

    fun dailyIncomesForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, Double>> {
        return incomeDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            val grouped = list.groupBy { entity ->
                entity.date.toLocalDate()
            }.mapValues { entry -> entry.value.sumOf { it.amount } }
            fillMissingDates(grouped, startDate, endDate)
        }
    }

    fun expensesByCategoryForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<ExpenseCategory, Double>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            list.groupBy { ExpenseCategory.valueOf(it.category) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    fun dailyExpensesForMonth(): Flow<Map<LocalDate, Double>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return dailyExpensesForRange(startOfMonth, today)
    }

    fun expensesByCategory(): Flow<Map<ExpenseCategory, Double>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return expensesByCategoryForRange(startOfWeek, today)
    }

    fun transactionsForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return combine(
            expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()),
            incomeDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis())
        ) { expenses, incomes ->
            buildList<Transaction> {
                expenses.forEach { add(it.toDomain().toTransaction()) }
                incomes.forEach {
                    val inc = Income(it.id, it.amount, it.note, it.date.toLocalDate())
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

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            category = ExpenseCategory.valueOf(category),
            note = note,
            date = date.toLocalDate()
        )
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
