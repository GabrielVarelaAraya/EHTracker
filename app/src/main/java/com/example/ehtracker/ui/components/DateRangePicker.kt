package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class PresetRange(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    QUARTER("This Quarter"),
    YEAR("This Year"),
    CUSTOM("Custom")
}

data class SelectedRange(
    val start: LocalDate,
    val end: LocalDate,
    val preset: PresetRange
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    currentRange: SelectedRange,
    onRangeSelected: (SelectedRange) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    var selectedPreset by remember { mutableStateOf(currentRange.preset) }

    EHTBottomSheet(
        onDismissRequest = onDismiss
    ) {
            Text(
                text = "Select Range",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            PresetRange.entries.forEach { preset ->
                val isSelected = preset == selectedPreset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable {
                            selectedPreset = preset
                            val range = when (preset) {
                                PresetRange.WEEK -> SelectedRange(today.minusDays(today.dayOfWeek.value.toLong() - 1), today, preset)
                                PresetRange.MONTH -> SelectedRange(today.withDayOfMonth(1), today, preset)
                                PresetRange.QUARTER -> SelectedRange(today.minusMonths(2).withDayOfMonth(1), today, preset)
                                PresetRange.YEAR -> SelectedRange(today.withDayOfMonth(1).withMonth(1), today, preset)
                                PresetRange.CUSTOM -> SelectedRange(today.minusDays(30), today, preset)
                            }
                            onRangeSelected(range)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (preset != PresetRange.CUSTOM) {
                        val (start, end) = when (preset) {
                            PresetRange.WEEK -> Pair(today.minusDays(today.dayOfWeek.value.toLong() - 1), today)
                            PresetRange.MONTH -> Pair(today.withDayOfMonth(1), today)
                            PresetRange.QUARTER -> Pair(today.minusMonths(2).withDayOfMonth(1), today)
                            PresetRange.YEAR -> Pair(today.withDayOfMonth(1).withMonth(1), today)
                            else -> Pair(today, today)
                        }
                        Text(
                            text = "${start.format(DateTimeFormatter.ofPattern("M/d"))} - ${end.format(DateTimeFormatter.ofPattern("M/d"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
