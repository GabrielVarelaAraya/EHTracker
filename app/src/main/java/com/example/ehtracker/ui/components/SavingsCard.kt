package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import com.example.ehtracker.util.formatMoney
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.SavingsGoal

@Composable
fun SavingsCard(
    totalExpenses: Double,
    totalIncome: Double,
    currentBalance: Double,
    currencySymbol: String = "$",
    goals: List<SavingsGoal> = emptyList(),
    onGoalsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val balanceColor = if (currentBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    val animatedIncome by animateFloatAsState(targetValue = totalIncome.toFloat(), animationSpec = tween(200), label = "income")
    val animatedExpenses by animateFloatAsState(targetValue = totalExpenses.toFloat(), animationSpec = tween(200), label = "expenses")
    val animatedBalance by animateFloatAsState(targetValue = currentBalance.toFloat(), animationSpec = tween(200), label = "balance")

    val netIncome = totalIncome - totalExpenses
    val fraction = if (totalIncome > 0) (netIncome / totalIncome).toFloat() else 0f
    val showNegative = netIncome < 0 && totalIncome > 0

    val cardPad = if (isCompact) 6.dp else 12.dp
    val sectionGap = if (isCompact) 3.dp else 6.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(cardPad)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Savings",
                style = (if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall).copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Goals \u203A",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onGoalsClick)
            )
        }

        Spacer(modifier = Modifier.height(sectionGap))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = formatMoney(animatedIncome.toDouble()),
                    transitionSpec = { (slideInVertically { it / 4 } + fadeIn()).togetherWith(slideOutVertically { -it / 4 } + fadeOut()) },
                    label = "incomeAnim"
                ) { formatted ->
                    Text(
                        text = "+$currencySymbol$formatted",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = formatMoney(animatedExpenses.toDouble()),
                    transitionSpec = { (slideInVertically { it / 4 } + fadeIn()).togetherWith(slideOutVertically { -it / 4 } + fadeOut()) },
                    label = "expenseAnim"
                ) { formatted ->
                    Text(
                        text = "-$currencySymbol$formatted",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(sectionGap))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompact) 3.dp else 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(kotlin.math.abs(fraction).coerceIn(0f, 1f))
                    .height(if (isCompact) 3.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (showNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(sectionGap))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedContent(
                targetState = formatMoney(animatedBalance.toDouble()),
                transitionSpec = { (slideInVertically { it / 4 } + fadeIn()).togetherWith(slideOutVertically { -it / 4 } + fadeOut()) },
                label = "balanceAnim"
            ) { formatted ->
                Text(
                    text = "$currencySymbol$formatted",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = balanceColor
                )
            }
        }

        if (goals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(sectionGap))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(sectionGap))

            Column(modifier = Modifier.clickable(onClick = onGoalsClick)) {
                goals.take(3).forEach { goal ->
                    GoalMiniRow(goal = goal)
                    Spacer(modifier = Modifier.height(if (isCompact) 3.dp else 4.dp))
                }

                if (goals.size > 3) {
                    Text(
                        text = "+${goals.size - 3} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(sectionGap))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(sectionGap))

            Text(
                text = "No goals yet \u2014 tap Goals to set up",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onGoalsClick)
            )
        }
    }
}

@Composable
private fun GoalMiniRow(goal: SavingsGoal) {
    val complete = goal.progress >= 1f
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(200),
        label = "goalProgress"
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            AnimatedContent(
                targetState = (goal.progress * 100).toInt(),
                transitionSpec = {
                    (slideInVertically(tween(180)) { it / 3 } + fadeIn(tween(180)))
                        .togetherWith(slideOutVertically(tween(180)) { -it / 3 } + fadeOut(tween(180)))
                },
                label = "goalPct"
            ) { pct ->
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
