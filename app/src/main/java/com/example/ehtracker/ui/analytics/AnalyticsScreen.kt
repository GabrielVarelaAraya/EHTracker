package com.example.ehtracker.ui.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.ui.components.SectionHeader
import com.example.ehtracker.ui.components.BudgetVsRealChart
import com.example.ehtracker.ui.components.AiInsightsCard
import com.example.ehtracker.ui.components.BudgetSetupSheet
import com.example.ehtracker.ui.components.CategoryBar
import com.example.ehtracker.ui.components.MiniLineChart
import com.example.ehtracker.ui.components.SavingsRateChart
import com.example.ehtracker.ui.components.ProgressRing
import com.example.ehtracker.ui.components.ShimmerBox
import com.example.ehtracker.ui.components.ShimmerCard
import com.example.ehtracker.ui.components.ShimmerInsightRow
import com.example.ehtracker.ui.theme.CategoryBills
import com.example.ehtracker.ui.theme.CategoryEntertainment
import com.example.ehtracker.ui.theme.CategoryFood
import com.example.ehtracker.ui.theme.CategoryHealth
import com.example.ehtracker.ui.theme.CategoryOther
import com.example.ehtracker.ui.theme.CategoryShopping
import com.example.ehtracker.ui.theme.CategoryTransport
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.ui.theme.HabitIcon

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val dailyData by viewModel.dailyExpenses.collectAsState()
    val dailySavingsRate by viewModel.dailySavingsRate.collectAsState()
    val savingsRateInsights by viewModel.savingsRateInsights.collectAsState()
    val categoryData by viewModel.expensesByCategory.collectAsState()
    val habitRate by viewModel.habitCompletionRate.collectAsState()
    val rangeTotal by viewModel.rangeTotal.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val habitsSummary by viewModel.habitsSummary.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val insights by viewModel.aiInsights.collectAsState()
    val sym = currency.symbol
    var showBudgetSetup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // Range selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DateRange.entries.forEach { range ->
                    val isSelected = range == selectedRange
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.selectRange(range) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val pagerState = rememberPagerState(
                initialPage = DateRange.entries.indexOf(selectedRange),
                pageCount = { DateRange.entries.size }
            )
            var isProgrammaticScroll by remember { mutableStateOf(false) }

            LaunchedEffect(selectedRange) {
                val targetPage = DateRange.entries.indexOf(selectedRange)
                val distance = kotlin.math.abs(targetPage - pagerState.currentPage)
                if (pagerState.currentPage != targetPage) {
                    isProgrammaticScroll = true
                    pagerState.animateScrollToPage(
                        page = targetPage,
                        animationSpec = tween(durationMillis = (distance * 250).coerceAtMost(600))
                    )
                    isProgrammaticScroll = false
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (!isProgrammaticScroll) {
                    viewModel.selectRange(DateRange.entries[pagerState.currentPage])
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
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

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(400, delayMillis = 100))
        ) {
            Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val total = categoryData.values.sum().coerceAtLeast(1.0)
                    val categoryColors = mapOf(
                        ExpenseCategory.FOOD to CategoryFood,
                        ExpenseCategory.TRANSPORT to CategoryTransport,
                        ExpenseCategory.SHOPPING to CategoryShopping,
                        ExpenseCategory.BILLS to CategoryBills,
                        ExpenseCategory.HEALTH to CategoryHealth,
                        ExpenseCategory.ENTERTAINMENT to CategoryEntertainment,
                        ExpenseCategory.OTHER to CategoryOther
                    )
                    if (categoryData.isEmpty()) {
                        Text(
                            text = "No data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categoryData.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                            CategoryBar(
                                label = cat.displayName,
                                amount = amt,
                                fraction = (amt / total).toFloat(),
                                color = categoryColors[cat] ?: CategoryOther,
                                currencySymbol = sym
                            )
                            val budget = budgetStatus[cat]
                            if (budget != null) {
                                val (spent, limit) = budget
                                val budgetFraction = (spent / limit).coerceIn(0.0, 1.0).toFloat()
                                val budgetColor = if (spent > limit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 70.dp, end = 0.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Canvas(modifier = Modifier.weight(1f).height(4.dp)) {
                                        drawRoundRect(
                                            color = trackColor,
                                            size = size,
                                            cornerRadius = CornerRadius(2.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = budgetColor,
                                            size = Size(size.width * budgetFraction, size.height),
                                            cornerRadius = CornerRadius(2.dp.toPx())
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$sym${"%.0f".format(spent)}/$sym${"%.0f".format(limit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = budgetColor
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { showBudgetSetup = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = if (budgetStatus.isEmpty()) "Set budgets" else "Edit budgets",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (budgetStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Budget vs Actual")
                Spacer(modifier = Modifier.height(8.dp))
                BudgetVsRealChart(
                    budgets = budgetStatus,
                    actuals = categoryData,
                    currencySymbol = sym,
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

                SectionHeader("Habits This ${selectedRange.label}")
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

            Spacer(modifier = Modifier.height(80.dp))
        }
        }
        }
        }
        }
    }

    if (showBudgetSetup) {
        val existingBudgets = budgetStatus.mapValues { it.value.second }
        BudgetSetupSheet(
            budgets = existingBudgets,
            currencySymbol = sym,
            onDismiss = { showBudgetSetup = false },
            onSave = { cat, limit -> viewModel.setBudget(cat, limit) },
            onDelete = { cat -> viewModel.deleteBudget(cat) }
        )
    }
}


