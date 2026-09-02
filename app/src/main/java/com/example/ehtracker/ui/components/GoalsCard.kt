package com.example.ehtracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import com.example.ehtracker.util.formatMoney
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.SavingsGoal

@Composable
fun GoalsCard(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(if (isCompact) 6.dp else 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (goals.isEmpty()) "Set up" else "Manage",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

            if (goals.isEmpty()) {
                Text(
                    text = "No goals yet \u2014 tap to set up",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                goals.take(3).forEach { goal ->
                    GoalProgressRow(goal = goal, currencySymbol = currencySymbol, compact = isCompact)
                    Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))
                }
            }
        }
    }
}

@Composable
fun GoalProgressRow(
    goal: SavingsGoal,
    currencySymbol: String,
    compact: Boolean = false
) {
    val barHeight = if (compact) 4.dp else 6.dp
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(200),
        label = "goalProgress"
    )
    val complete = goal.progress >= 1f

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
            androidx.compose.animation.AnimatedContent(
                targetState = (goal.progress * 100).toInt(),
                transitionSpec = {
                    (androidx.compose.animation.slideInVertically(tween(180)) { it / 3 } + androidx.compose.animation.fadeIn(tween(180)))
                        .togetherWith(androidx.compose.animation.slideOutVertically(tween(180)) { -it / 3 } + androidx.compose.animation.fadeOut(tween(180)))
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
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$currencySymbol${formatMoney(goal.currentAmount)} of $currencySymbol${formatMoney(goal.targetAmount)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}