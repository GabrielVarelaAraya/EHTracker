package com.example.ehtracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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

@Composable
fun ExpenseSummaryCard(
    todayTotal: Double,
    weekTotal: Double,
    monthTotal: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(if (isCompact) 6.dp else 12.dp)
    ) {
        Text(
            text = "Expenses",
            style = (if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall).copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExpenseMetric("Today", todayTotal, currencySymbol, isCompact)
            ExpenseMetric("Week", weekTotal, currencySymbol, isCompact)
            ExpenseMetric("Month", monthTotal, currencySymbol, isCompact)
        }
    }
}

@Composable
private fun ExpenseMetric(label: String, value: Double, symbol: String, isCompact: Boolean = false) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(180),
        label = "$label expense"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.animation.AnimatedContent(
            targetState = "%.0f".format(animatedValue),
            transitionSpec = {
                (androidx.compose.animation.slideInVertically(tween(180)) { it / 3 } + androidx.compose.animation.fadeIn(tween(180)))
                    .togetherWith(androidx.compose.animation.slideOutVertically(tween(180)) { -it / 3 } + androidx.compose.animation.fadeOut(tween(180)))
            },
            label = "$label-animated"
        ) { formatted ->
            Text(
                text = "$symbol$formatted",
                style = (if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium).copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
