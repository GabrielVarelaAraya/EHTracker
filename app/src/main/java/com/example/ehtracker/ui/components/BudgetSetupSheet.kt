package com.example.ehtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupSheet(
    categories: List<Category>,
    budgets: Map<String, Double>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit,
    onDelete: (String) -> Unit
) {
    val spending = remember(categories) { categories.filter { !it.isSavings } }
    val budgetInputs = remember(budgets, categories) {
        mutableStateOf(
            spending.associate { cat ->
                cat.id to (budgets[cat.id]?.let { "%.2f".format(it) } ?: "")
            }
        )
    }

    EHTBottomSheet(
        onDismissRequest = onDismiss
    ) {
            Text(
                text = "Monthly Budgets",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set a monthly spending limit per category",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                spending.forEach { cat ->
                    val value = budgetInputs.value[cat.id] ?: ""
                    val hasBudget = budgets.containsKey(cat.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.icon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = value,
                            onValueChange = { newVal ->
                                var dotSeen = false
                                val filtered = newVal.filter { c ->
                                    when {
                                        c.isDigit() -> true
                                        c == '.' && !dotSeen -> { dotSeen = true; true }
                                        c == ',' || c == ' ' -> true
                                        else -> false
                                    }
                                }
                                budgetInputs.value = budgetInputs.value + (cat.id to filtered)
                            },
                            label = "",
                            prefix = { Text(currencySymbol) },
                            trailing = if (hasBudget) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Delete budget",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onDelete(cat.id) }
                                    )
                                }
                            } else null,
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormActions(
                onCancel = onDismiss,
                onConfirm = {
                    budgetInputs.value.forEach { (catId, value) ->
                        val parsed = com.example.ehtracker.util.parseAmount(value)
                        if (parsed != null && parsed > 0) {
                            onSave(catId, parsed)
                        } else if (budgets.containsKey(catId) && (parsed == null || parsed <= 0)) {
                            onDelete(catId)
                        }
                    }
                    onDismiss()
                },
                confirmEnabled = true
            )
        }
}
