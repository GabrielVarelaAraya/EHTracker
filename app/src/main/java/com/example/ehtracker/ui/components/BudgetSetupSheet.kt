package com.example.ehtracker.ui.components

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
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.ui.theme.CategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupSheet(
    budgets: Map<ExpenseCategory, Double>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategory, Double) -> Unit,
    onDelete: (ExpenseCategory) -> Unit
) {
    val budgetInputs = remember(budgets) {
        mutableStateOf(
            ExpenseCategory.entries.associate { cat ->
                cat to (budgets[cat]?.let { "%.2f".format(it) } ?: "")
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

            ExpenseCategory.entries.forEach { cat ->
                val value = budgetInputs.value[cat] ?: ""
                val hasBudget = budgets.containsKey(cat)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryIcon(
                        emoji = cat.icon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                        AppTextField(
                            value = value,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { c -> c.isDigit() || (c == '.' && !newVal.contains('.')) }
                                budgetInputs.value = budgetInputs.value + (cat to filtered)
                            },
                            label = "",
                            prefix = { Text(currencySymbol) },
                            modifier = Modifier.width(120.dp)
                        )
                    if (hasBudget) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = { onDelete(cat) },
                            modifier = Modifier.padding(start = 0.dp)
                        ) {
                            Text(
                                "X",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormActions(
                onCancel = onDismiss,
                onConfirm = {
                    budgetInputs.value.forEach { (cat, value) ->
                        val parsed = value.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onSave(cat, parsed)
                        } else if (budgets.containsKey(cat) && (parsed == null || parsed <= 0)) {
                            onDelete(cat)
                        }
                    }
                    onDismiss()
                },
                confirmEnabled = true
            )
        }
    }
