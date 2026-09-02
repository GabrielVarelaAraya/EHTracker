package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.ui.theme.HabitIcon
import java.time.LocalDate

@Composable
fun HabitProgressSheet(
    habit: Habit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(15) }
    val today = LocalDate.now()

    val data = remember(habit, selectedDays) {
        val start = today.minusDays((selectedDays - 1).toLong())
        if (habit.isNumeric) {
            habit.completionValues.entries
                .filter { it.key in start..today }
                .sortedBy { it.key }
                .map { it.key to it.value }
        } else {
            (0 until selectedDays).map { offset ->
                val date = start.plusDays(offset.toLong())
                val value = if (date in habit.completedDates) 1.0 else 0.0
                date to value
            }
        }
    }

    val completedCount = if (habit.isNumeric) data.size else data.count { it.second > 0 }
    val avg = if (habit.isNumeric) {
        val vals = data.map { it.second }
        if (vals.isNotEmpty()) vals.average() else 0.0
    } else 0.0
    val maxVal = data.maxOfOrNull { it.second } ?: 0.0
    val minVal = data.minOfOrNull { it.second } ?: 0.0

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                HabitIcon(emoji = habit.icon, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (habit.isNumeric) {
                        if (habit.unit.isNotBlank()) "Numeric • ${habit.unit}" else "Numeric"
                    } else "Check • ${habit.targetDaysPerWeek} days/week",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // streak badge
            if (!habit.isNumeric && habit.currentStreak > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${habit.currentStreak} day streak",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(7, 15, 30).forEachIndexed { index, days ->
                SegmentedButton(
                    selected = selectedDays == days,
                    onClick = { selectedDays = days },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        activeBorderColor = MaterialTheme.colorScheme.primary,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("$days d")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (data.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not enough data yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            HabitProgressChart(
                data = data,
                isNumeric = habit.isNumeric,
                unit = habit.unit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (habit.isNumeric) {
                StatBox(label = "Records", value = "$completedCount")
                StatBox(label = "Avg", value = if (avg == 0.0) "—" else String.format("%.1f", avg))
                StatBox(label = "Max", value = if (maxVal == 0.0) "—" else String.format("%.1f", maxVal).trimEnd('0').trimEnd('.'))
                if (minVal != maxVal) {
                    StatBox(label = "Min", value = String.format("%.1f", minVal).trimEnd('0').trimEnd('.'))
                }
            } else {
                StatBox(label = "Done", value = "$completedCount / $selectedDays")
                val pct = if (selectedDays > 0) (completedCount * 100 / selectedDays) else 0
                StatBox(label = "Rate", value = "$pct%")
                StatBox(label = "Target", value = "${habit.targetDaysPerWeek}/wk")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (habit.isNumeric) {
                if (data.isEmpty()) "No records in this range"
                else {
                    val minStr = String.format("%.1f", minVal).trimEnd('0').trimEnd('.')
                    val maxStr = String.format("%.1f", maxVal).trimEnd('0').trimEnd('.')
                    if (minVal == maxVal) "Value: $maxStr ${habit.unit}".trim() else "Range: $minStr – $maxStr ${habit.unit}".trim()
                }
            } else "0 = missed, 1 = completed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
