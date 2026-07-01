package com.example.ehtracker.data.repository

import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.local.entity.BalanceEntity
import com.example.ehtracker.data.local.entity.BudgetEntity
import com.example.ehtracker.data.local.entity.CurrencyEntity
import com.example.ehtracker.data.local.entity.ExpenseEntity
import com.example.ehtracker.data.local.entity.HabitCompletionEntity
import com.example.ehtracker.data.local.entity.HabitEntity
import com.example.ehtracker.data.local.entity.IncomeEntity
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Income
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    suspend fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val dateLong = date.toEpochMillis()
        val existing = completionDao.getByHabitIdAndDate(habitId, dateLong)
        if (existing != null) {
            completionDao.deleteByHabitIdAndDate(habitId, dateLong)
        } else {
            completionDao.insert(
                HabitCompletionEntity(
                    habitId = habitId,
                    date = dateLong
                )
            )
        }
    }

    suspend fun addHabit(name: String, icon: String, targetDaysPerWeek: Int = 7) {
        habitDao.insert(
            HabitEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                targetDaysPerWeek = targetDaysPerWeek
            )
        )
    }

    suspend fun updateHabit(id: String, name: String, icon: String, targetDaysPerWeek: Int) {
        val existing = habitDao.getById(id) ?: return
        habitDao.insert(
            existing.copy(
                name = name,
                icon = icon,
                targetDaysPerWeek = targetDaysPerWeek
            )
        )
    }

    suspend fun deleteHabit(id: String) {
        habitDao.deleteById(id)
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
                    date = Instant.ofEpochMilli(entity.date).atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }
        }
    }

    fun totalIncome(): Flow<Double> {
        return incomeDao.totalAll().map { it ?: 0.0 }
    }

    suspend fun addIncome(amount: Double, note: String) {
        incomeDao.insert(
            IncomeEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                note = note,
                date = LocalDate.now().toEpochMillis()
            )
        )
    }

    suspend fun deleteIncome(id: String) {
        incomeDao.deleteById(id)
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
        return combine(
            budgetDao.getAll(),
            expenseDao.getByDateRange(startOfMonth.toEpochMillis(), today.toEpochMillis())
        ) { budgets, expenses ->
            val spentByCategory = expenses.groupBy { ExpenseCategory.valueOf(it.category) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            budgets.associate { budget ->
                val cat = ExpenseCategory.valueOf(budget.category)
                cat to Pair(spentByCategory[cat] ?: 0.0, budget.monthlyLimit)
            }
        }
    }

    // --- Analytics ---

    fun todayTotalExpenses(): Flow<Double> {
        val today = LocalDate.now()
        return expenseDao.sumByDateRange(today.toEpochMillis(), today.toEpochMillis()).map { it ?: 0.0 }
    }

    fun thisWeekExpenses(): Flow<Double> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return expenseDao.sumByDateRange(startOfWeek.toEpochMillis(), today.toEpochMillis()).map { it ?: 0.0 }
    }

    fun thisMonthExpenses(): Flow<Double> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return expenseDao.sumByDateRange(startOfMonth.toEpochMillis(), today.toEpochMillis()).map { it ?: 0.0 }
    }

    fun thisQuarterExpenses(): Flow<Double> {
        val today = LocalDate.now()
        val quarter = (today.monthValue - 1) / 3
        val startOfQuarter = LocalDate.of(today.year, quarter * 3 + 1, 1)
        return expenseDao.sumByDateRange(startOfQuarter.toEpochMillis(), today.toEpochMillis()).map { it ?: 0.0 }
    }

    fun expensesForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun dailyExpensesForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<Int, Double>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            list.groupBy { entity ->
                Instant.ofEpochMilli(entity.date).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
            }.mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    fun expensesByCategoryForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<ExpenseCategory, Double>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEpochMillis()).map { list ->
            list.groupBy { ExpenseCategory.valueOf(it.category) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    fun dailyExpensesForMonth(): Flow<Map<Int, Double>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return dailyExpensesForRange(startOfMonth, today)
    }

    fun expensesByCategory(): Flow<Map<ExpenseCategory, Double>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return expensesByCategoryForRange(startOfWeek, today)
    }

    fun habitCompletionRate(): Flow<Float> {
        return combine(
            habitDao.getAll(),
            completionDao.getAll()
        ) { habits, completions ->
            if (habits.isEmpty()) return@combine 0f
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val completionsThisWeek = completions.count {
                it.date >= startOfWeek.toEpochMillis() && it.date <= today.toEpochMillis()
            }
            completionsThisWeek.toFloat() / (habits.size * 7).coerceAtLeast(1)
        }
    }

    // --- Mapping ---

    private fun HabitEntity.toDomain(completions: List<HabitCompletionEntity>): Habit {
        val completedDates = completions.map { entity ->
            Instant.ofEpochMilli(entity.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        return Habit(
            id = id,
            name = name,
            icon = icon,
            completedDates = completedDates,
            targetDaysPerWeek = targetDaysPerWeek
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            category = ExpenseCategory.valueOf(category),
            note = note,
            date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        )
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
