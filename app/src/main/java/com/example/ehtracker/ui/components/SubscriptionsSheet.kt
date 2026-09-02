package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ehtracker.util.formatMoney
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.Subscription
import com.example.ehtracker.data.model.TransactionType
import com.example.ehtracker.data.model.resolveCategory
import com.example.ehtracker.ui.theme.CategoryIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubscriptionsSheet(
    subscriptions: List<Subscription>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, Int, String, TransactionType) -> Unit,
    onUpdate: (String, String, Double, String, Int, String, TransactionType) -> Unit,
    onDelete: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    categories: List<Category> = emptyList(),
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCategory: (String) -> Unit = {}
) {
    var showForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf<TransactionType?>(null) }

    val filteredSubs = if (filterType != null) subscriptions.filter { it.type == filterType } else subscriptions

    if (showForm) {
        val editingSub = editingId?.let { id -> subscriptions.find { it.id == id } }
        SubscriptionFormSheet(
            initial = editingSub,
            currencySymbol = currencySymbol,
            categories = categories.filter { !it.isSavings },
            onAddCategory = onAddCategory,
            onUpdateCategory = onUpdateCategory,
            onDeleteCategory = onDeleteCategory,
            onDismiss = { showForm = false; editingId = null },
            onSave = { name, amount, category, billingDay, notes, type ->
                if (editingSub != null) {
                    onUpdate(editingSub.id, name, amount, category, billingDay, notes, type)
                } else {
                    onAdd(name, amount, category, billingDay, notes, type)
                }
                showForm = false
                editingId = null
            },
            onDelete = if (editingSub != null) {{ onDelete(editingSub.id); showForm = false; editingId = null }} else null
        )
    } else {
        EHTBottomSheet(onDismissRequest = onDismiss) {
            Text(
                text = "Recurring",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip("All", filterType == null) { filterType = null }
                FilterChip("Expenses", filterType == TransactionType.EXPENSE) { filterType = TransactionType.EXPENSE }
                FilterChip("Income", filterType == TransactionType.INCOME) { filterType = TransactionType.INCOME }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total: $currencySymbol${formatMoney(filteredSubs.filter { it.isActive }.sumOf { it.amount })}/mo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (filteredSubs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recurring items yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    filteredSubs.forEach { sub ->
                        SubscriptionRow(
                            subscription = sub,
                            currencySymbol = currencySymbol,
                            onEdit = {
                                editingId = sub.id
                                showForm = true
                            },
                            onToggle = { onToggle(sub.id, it) },
                            catalog = categories
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showForm = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Add recurring", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    currencySymbol: String,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    catalog: List<Category> = emptyList()
) {
    val cat = resolveCategory(subscription.category, catalog)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onEdit)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (subscription.type == TransactionType.INCOME) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Income",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                CategoryIcon(emoji = cat.icon, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (subscription.isActive) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (subscription.type == TransactionType.INCOME) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "${cat.name} \u2022 Day ${subscription.billingDay}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "$currencySymbol${formatMoney(subscription.amount)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (subscription.isActive) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Switch(
            checked = subscription.isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubscriptionFormSheet(
    initial: Subscription?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Int, String, TransactionType) -> Unit,
    onDelete: (() -> Unit)?,
    categories: List<Category> = emptyList(),
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCategory: (String) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(initial?.type ?: TransactionType.EXPENSE) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(if (initial != null) "%.2f".format(initial.amount) else "") }
    var selectedCategory by remember { mutableStateOf(initial?.category ?: "OTHER") }
    var billingDay by remember { mutableIntStateOf(initial?.billingDay ?: 1) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = if (initial != null) "Edit recurring" else "New recurring",
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
                FilterChip("Expense", selectedType == TransactionType.EXPENSE) { selectedType = TransactionType.EXPENSE }
                FilterChip("Income", selectedType == TransactionType.INCOME) { selectedType = TransactionType.INCOME }
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
                    categories = categories,
                    selectedId = selectedCategory,
                    onSelect = { selectedCategory = it },
                    onAddCategory = onAddCategory,
                    onUpdateCategory = onUpdateCategory,
                    onDeleteCategory = onDeleteCategory
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Billing day: $billingDay",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (1..31).forEach { day ->
                    val isSelected = day == billingDay
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { billingDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(value = notes, onValueChange = { notes = it }, label = "Notes (optional)")
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = {
                            val parsedAmount = com.example.ehtracker.util.parseAmount(amount)
                            if (name.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
                                onSave(name.trim(), parsedAmount, selectedCategory, billingDay, notes.trim(), selectedType)
                            }
                        },
                        enabled = name.isNotBlank() && com.example.ehtracker.util.parseAmount(amount) != null && (com.example.ehtracker.util.parseAmount(amount) ?: 0.0) > 0
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
