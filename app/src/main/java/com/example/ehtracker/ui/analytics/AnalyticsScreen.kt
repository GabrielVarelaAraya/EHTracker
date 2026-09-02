package com.example.ehtracker.ui.analytics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.ui.components.SectionHeader
import com.example.ehtracker.ui.components.BudgetVsRealChart
import com.example.ehtracker.ui.components.AiInsightsCard
import com.example.ehtracker.ui.components.BudgetSetupSheet
import com.example.ehtracker.ui.components.CategoryBar
import com.example.ehtracker.ui.components.DonutChart
import com.example.ehtracker.ui.components.MiniLineChart
import com.example.ehtracker.ui.components.SavingsRateChart
import com.example.ehtracker.ui.components.ProgressRing
import com.example.ehtracker.ui.components.ShimmerBox
import com.example.ehtracker.ui.components.ShimmerCard
import com.example.ehtracker.ui.components.ShimmerInsightRow
import androidx.compose.material.icons.filled.PieChart
import com.example.ehtracker.ui.components.EmptyState
import com.example.ehtracker.ui.theme.CategoryOther
import com.example.ehtracker.ui.theme.categoryColor
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.resolveCategory
import com.example.ehtracker.ui.theme.HabitIcon

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val dailyData by viewModel.dailyExpenses.collectAsState()
    val dailySavingsRate by viewModel.dailySavingsRate.collectAsState()
    val savingsRateInsights by viewModel.savingsRateInsights.collectAsState()
    val categoryData by viewModel.expensesByCategory.collectAsState()
    val habitRate by viewModel.habitCompletionRate.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val habitsSummary by viewModel.habitsSummary.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val insights by viewModel.aiInsights.collectAsState()
    val catalog by viewModel.categories.collectAsState()
    val sym = currency.symbol
    var showBudgetSetup by remember { mutableStateOf(false) }

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            RangeDropdown(
                selected = selectedRange,
                onSelect = viewModel::selectRange
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = selectedRange,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val enter = slideInHorizontally(animationSpec = tween(280)) { width ->
                        if (forward) width / 4 else -width / 4
                    } + fadeIn(animationSpec = tween(280))
                    val exit = slideOutHorizontally(animationSpec = tween(200)) { width ->
                        if (forward) -width / 4 else width / 4
                    } + fadeOut(animationSpec = tween(200))
                    (enter togetherWith exit)
                },
                label = "rangeTransition"
            ) { range ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isLoading) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(28.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                cornerRadius = 8.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            ShimmerCard()
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerCard(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                ShimmerCard(modifier = Modifier.weight(1.2f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            ShimmerInsightRow()
            ShimmerInsightRow()
            ShimmerInsightRow()
        }

        if (!isLoading) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .combinedClickable(onClick = {}, onLongClick = { showBudgetSetup = true })
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val total = categoryData.values.sum().coerceAtLeast(1.0)
                    if (categoryData.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.PieChart,
                            title = "No spending data",
                            subtitle = "Log expenses to see category breakdown, budgets and trends. Your donut chart will appear here once you have transactions in this range."
                        )
                    } else {
                        val sortedEntries = categoryData.entries.sortedByDescending { it.value }
                        val segments = sortedEntries.map { (cat, amt) ->
                            categoryColor(cat) to (amt / total).toFloat()
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DonutChart(
                                segments = segments,
                                modifier = Modifier.size(120.dp),
                                strokeWidth = 22,
                                centerText = "$sym${"%.0f".format(total)}",
                                centerSubText = "Total"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                categoryData.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                                    CategoryBar(
                                        label = resolveCategory(cat, catalog).name,
                                        amount = amt,
                                        fraction = (amt / total).toFloat(),
                                        color = categoryColor(cat),
                                        currencySymbol = sym
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (budgetStatus.isEmpty()) "Hold to set budgets" else "Hold to edit budgets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            if (budgetStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Budget vs Actual")
                Spacer(modifier = Modifier.height(8.dp))
                BudgetVsRealChart(
                    budgets = budgetStatus,
                    actuals = categoryData,
                    currencySymbol = sym,
                    catalog = catalog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader("Daily Trend")
            Spacer(modifier = Modifier.height(8.dp))
            MiniLineChart(
                data = dailyData,
                preselectCurrentDay = true,
                currencySymbol = sym,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Savings Rate")
            Spacer(modifier = Modifier.height(8.dp))
            SavingsRateChart(
                data = dailySavingsRate,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            )

            if (savingsRateInsights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    savingsRateInsights.forEach { insight ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (insight.isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = insight.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (habitsSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("Habits This ${range.label}")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    habitsSummary.forEach { summary ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                HabitIcon(emoji = summary.habit.icon, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = summary.habit.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${summary.streak} day streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(summary.completionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader("Insights")
            Spacer(modifier = Modifier.height(8.dp))
            AiInsightsCard(insights = insights)

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader("Habits")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProgressRing(progress = habitRate, size = 80, strokeWidth = 6)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
            }
            }
            }
            }
            }
        }
    }

    if (showBudgetSetup) {
        val existingBudgets = budgetStatus.mapValues { it.value.second }
        BudgetSetupSheet(
            categories = catalog,
            budgets = existingBudgets,
            currencySymbol = sym,
            onDismiss = { showBudgetSetup = false },
            onSave = { cat, limit -> viewModel.setBudget(cat, limit) },
            onDelete = { cat -> viewModel.deleteBudget(cat) }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RangeDropdown(
    selected: DateRange,
    onSelect: (DateRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Range") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DateRange.entries.forEach { range ->
                val isSelected = range == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = {
                        onSelect(range)
                        expanded = false
                    }
                )
            }
        }
    }
}


