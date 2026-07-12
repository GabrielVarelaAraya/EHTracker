package com.example.ehtracker.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.ui.calendar.CalendarViewModel
import com.example.ehtracker.ui.components.ExpenseSummaryCard
import com.example.ehtracker.ui.components.HabitStreakRow
import com.example.ehtracker.ui.components.SavingsCard
import com.example.ehtracker.ui.components.ShimmerBox
import com.example.ehtracker.ui.components.ShimmerCard
import com.example.ehtracker.ui.components.ShimmerHabitRow
import com.example.ehtracker.ui.components.ShimmerExpenseRow
import com.example.ehtracker.ui.components.ShimmerInsightRow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    calendarViewModel: CalendarViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val sym = state.currency.symbol

    val today = LocalDate.now()
    val greeting = when (today.dayOfWeek.value) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> ""
    }

    Scaffold(
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                ShimmerCard()
                Spacer(modifier = Modifier.height(20.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerHabitRow()
                ShimmerHabitRow()
                Spacer(modifier = Modifier.height(20.dp))
                ShimmerCard()
                Spacer(modifier = Modifier.height(20.dp))
                ShimmerInsightRow()
                ShimmerInsightRow()
                ShimmerInsightRow()
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SavingsCard(
                savings = state.savings,
                totalExpenses = state.totalExpenses,
                totalIncome = state.totalIncome,
                currentBalance = state.currentBalance,
                currencySymbol = sym
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Habits",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (state.habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "\uD83D\uDCCB",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No habits yet \u2014 tap + to add your first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val today = LocalDate.now()
                val thisMonday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                val totalPages = 520
                val initialPage = totalPages / 2
                val pagerState = rememberPagerState(initialPage = initialPage) { totalPages }

                val weekOffset = pagerState.currentPage - initialPage
                val weekStart = thisMonday.plusWeeks(weekOffset.toLong())
                val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }
                val weekLabel = "${weekDates.first().format(DateTimeFormatter.ofPattern("MMM d"))} - ${weekDates.last().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"

                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pageAlpha by animateFloatAsState(
                        targetValue = if (page == pagerState.currentPage) 1f else 0.5f,
                        animationSpec = tween(300),
                        label = "habitPageFade"
                    )
                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = pageAlpha
                        }
                    ) {
                        val offset = page - initialPage
                        val start = thisMonday.plusWeeks(offset.toLong())
                        val dates = (0..6).map { start.plusDays(it.toLong()) }
                    HabitStreakRow(
                        habits = state.habits,
                        onToggle = { habitId, date -> viewModel.toggleHabit(habitId, date) },
                        onSetValue = { habitId, date, value -> viewModel.setHabitValue(habitId, date, value) },
                        weekDates = dates
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.todayTotal > 0 || state.weekTotal > 0 || state.monthTotal > 0) {
                ExpenseSummaryCard(
                    todayTotal = state.todayTotal,
                    weekTotal = state.weekTotal,
                    monthTotal = state.monthTotal,
                    currencySymbol = sym
                )
            } else {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "\uD83D\uDCB3",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CalendarMonthGrid(
                calendarViewModel = calendarViewModel,
                today = today,
                sym = sym
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
        }
        }
    }

    if (state.showEditSavings) {
        EditSavingsSheet(
            currentSavings = state.savings,
            currentCurrency = state.currency,
            onDismiss = { viewModel.dismissEditSavings() },
            onSave = { amount, selectedCurrency ->
                viewModel.setSavings(amount, selectedCurrency)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSavingsSheet(
    currentSavings: Double,
    currentCurrency: Currency,
    onDismiss: () -> Unit,
    onSave: (Double, Currency) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember {
        mutableStateOf(if (currentSavings > 0) "%.2f".format(currentSavings) else "")
    }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    var currencyExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Total Savings") },
                prefix = { Text(selectedCurrency.symbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${selectedCurrency.symbol}  ${selectedCurrency.displayName} (${selectedCurrency.code})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    Currency.entries.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${currency.symbol}  ${currency.displayName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                selectedCurrency = currency
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    onClick = {
                        val parsed = amount.toDoubleOrNull()
                        if (parsed != null && parsed >= 0) {
                            onSave(parsed, selectedCurrency)
                        }
                    },
                    enabled = amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: -1.0) >= 0
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    calendarViewModel: CalendarViewModel,
    today: LocalDate,
    sym: String
) {
    val state by calendarViewModel.uiState.collectAsState()
    val month = state.currentMonth
    val summaries = state.dailySummaries

    AnimatedContent(
        targetState = month,
        transitionSpec = {
            val direction = if (targetState > initialState) -1 else 1
            (slideInVertically { it * direction } + fadeIn(tween(300)))
                .togetherWith(slideOutVertically { it * -direction } + fadeOut(tween(200)))
        },
        label = "monthTransition"
    ) { animatedMonth ->
        Column {
            Text(
                text = animatedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val weeks = computeMonthWeeks(animatedMonth)
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (date != null) {
                                val summary = summaries[date]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (date == today) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable {
                                            calendarViewModel.toggleHabit(state.habits.firstOrNull()?.id ?: "", date)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (date == today) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (summary != null) {
                                        val rate = summary.completionRate
                                        Text(
                                            text = "${(rate * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.75.sp),
                                            color = when {
                                                rate >= 1f -> MaterialTheme.colorScheme.primary
                                                rate > 0f -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        val net = summary.netAmount
                                        Text(
                                            text = "${if (net >= 0) "+" else ""}${"%.0f".format(net)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.75.sp),
                                            color = if (net >= 0) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.error,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
