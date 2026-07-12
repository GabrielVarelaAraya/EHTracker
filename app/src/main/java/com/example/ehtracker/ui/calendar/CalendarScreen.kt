package com.example.ehtracker.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.ui.theme.HabitIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsState()
    val today = LocalDate.now()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        item { MonthNavigationHeader(state.currentMonth, viewModel) }
        item { Spacer(Modifier.height(4.dp)) }
        item { ViewModeToggle(state.viewMode, viewModel) }
        item { Spacer(Modifier.height(4.dp)) }
        item { WeekdayHeaders() }
        item { Spacer(Modifier.height(2.dp)) }

        if (state.viewMode == CalendarViewMode.MONTH) {
            val weeks = computeMonthWeeks(state.currentMonth)
            weeks.forEach { week ->
                item {
                    WeekRow(
                        week = week,
                        summaries = state.dailySummaries,
                        selectedDate = state.selectedDate,
                        today = today,
                        onSelectDate = { viewModel.selectDate(it) }
                    )
                }
            }
        } else {
            item {
                val start = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                val dates = (0..6).map { start.plusDays(it.toLong()) }
                WeekRow(
                    week = dates,
                    summaries = state.dailySummaries,
                    selectedDate = state.selectedDate,
                    today = today,
                    onSelectDate = { viewModel.selectDate(it) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        state.selectedDate?.let { date ->
            val summary = state.dailySummaries[date]
            item { DayDetailHeader(date, today) }
            if (summary != null) {
                item { DaySummaryCard(summary, state.currency) }
            }
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (state.habits.isEmpty()) {
                item {
                    Text(
                        text = "No habits yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                state.habits.forEach { habit ->
                    item {
                        HabitToggleRow(
                            habit = habit,
                            date = date,
                            isCompleted = date in habit.completedDates,
                            onToggle = { viewModel.toggleHabit(habit.id, date) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (state.selectedDayTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions for this day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(state.selectedDayTransactions) { transaction ->
                    TransactionRow(transaction, state.currency)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MonthNavigationHeader(month: YearMonth, viewModel: CalendarViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.previousMonth() }) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { viewModel.nextMonth() }) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun ViewModeToggle(mode: CalendarViewMode, viewModel: CalendarViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = { viewModel.setViewMode(CalendarViewMode.MONTH) }) {
            Text(
                text = "Month",
                fontWeight = if (mode == CalendarViewMode.MONTH) FontWeight.SemiBold else FontWeight.Normal,
                color = if (mode == CalendarViewMode.MONTH) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = { viewModel.setViewMode(CalendarViewMode.WEEK) }) {
            Text(
                text = "Week",
                fontWeight = if (mode == CalendarViewMode.WEEK) FontWeight.SemiBold else FontWeight.Normal,
                color = if (mode == CalendarViewMode.WEEK) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeekdayHeaders() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeekRow(
    week: List<LocalDate?>,
    summaries: Map<LocalDate, DaySummary>,
    selectedDate: LocalDate?,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        week.forEach { date ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (date != null) {
                    val summary = summaries[date]
                    DayCell(
                        date = date,
                        summary = summary,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        onClick = { onSelectDate(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isToday || isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
        if (summary != null) {
            val rate = summary.completionRate
            Text(
                text = "${(rate * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = when {
                    rate >= 1f -> MaterialTheme.colorScheme.primary
                    rate > 0f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            val net = summary.netAmount
            Text(
                text = "${if (net >= 0) "+" else ""}${"%.0f".format(net)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                color = if (net >= 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DayDetailHeader(date: LocalDate, today: LocalDate) {
    val fmt = if (date == today) "Today"
        else date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    Text(
        text = fmt,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DaySummaryCard(summary: DaySummary, currency: com.example.ehtracker.data.model.Currency) {
    val sym = currency.symbol
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${summary.totalCompleted}/${summary.totalHabits} (${(summary.completionRate * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Net",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (summary.netAmount >= 0) "+" else ""}$sym${"%.2f".format(summary.netAmount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (summary.netAmount >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Income: $sym${"%.2f".format(summary.totalIncome)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Expenses: $sym${"%.2f".format(summary.totalExpenses)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HabitToggleRow(
    habit: Habit,
    date: LocalDate,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HabitIcon(emoji = habit.icon, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "\u2713",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    currency: com.example.ehtracker.data.model.Currency
) {
    val sym = currency.symbol
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (transaction) {
            is Transaction.Expense -> {
                val cat = transaction.category
                Text(
                    text = cat.icon,
                    fontSize = 14.sp,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.note.ifBlank { cat.displayName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "-$sym${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }
            is Transaction.Income -> {
                Text(
                    text = "\u2795",
                    fontSize = 14.sp,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.note.ifBlank { "Income" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "+$sym${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun computeMonthWeeks(month: YearMonth): List<List<LocalDate?>> {
    val firstOfMonth = month.atDay(1)
    val lastOfMonth = month.atEndOfMonth()
    val startDayOfWeek = firstOfMonth.dayOfWeek.value
    val weeks = mutableListOf<MutableList<LocalDate?>>()
    var currentWeek = mutableListOf<LocalDate?>()
    for (i in 1 until startDayOfWeek) {
        currentWeek.add(null)
    }
    for (day in 1..lastOfMonth.dayOfMonth) {
        currentWeek.add(month.atDay(day))
        if (currentWeek.size == 7) {
            weeks.add(currentWeek)
            currentWeek = mutableListOf()
        }
    }
    while (currentWeek.size < 7) {
        currentWeek.add(null)
    }
    if (currentWeek.isNotEmpty() && currentWeek.any { it != null }) {
        weeks.add(currentWeek)
    }
    return weeks
}
