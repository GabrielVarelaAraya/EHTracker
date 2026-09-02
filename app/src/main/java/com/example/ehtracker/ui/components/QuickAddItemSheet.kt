package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.QuickAddItem
import com.example.ehtracker.data.model.TransactionType
import com.example.ehtracker.ui.theme.CategoryIcon
import com.example.ehtracker.ui.components.CategoryPicker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAddItemEditSheet(
    initial: QuickAddItem,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    categories: List<Category> = emptyList(),
    onSave: (String, Double, String, String, TransactionType) -> Unit,
    onDelete: () -> Unit,
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCategory: (String) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(initial.type) }
    var name by remember { mutableStateOf(initial.name) }
    var amount by remember { mutableStateOf(if (initial.amount > 0) "%.2f".format(initial.amount) else "") }
    var selectedCategory by remember { mutableStateOf(initial.category) }
    var notes by remember { mutableStateOf(initial.notes) }

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = "Edit recurring",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(value = name, onValueChange = { name = it }, label = "Name")
            Spacer(modifier = Modifier.height(12.dp))

            AmountField(value = amount, onValueChange = { amount = it }, currencySymbol = currencySymbol)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemFilterChip("Expense", selectedType == TransactionType.EXPENSE) { selectedType = TransactionType.EXPENSE }
                ItemFilterChip("Income", selectedType == TransactionType.INCOME) { selectedType = TransactionType.INCOME }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedType == TransactionType.EXPENSE) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                CategoryPicker(
                    categories = categories.filter { !it.isSavings },
                    selectedId = selectedCategory,
                    onSelect = { selectedCategory = it },
                    onAddCategory = onAddCategory,
                    onUpdateCategory = onUpdateCategory,
                    onDeleteCategory = onDeleteCategory
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            AppTextField(value = notes, onValueChange = { notes = it }, label = "Notes (optional)")
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            if (name.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                                onSave(name.trim(), parsedAmount, selectedCategory, notes.trim(), selectedType)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
