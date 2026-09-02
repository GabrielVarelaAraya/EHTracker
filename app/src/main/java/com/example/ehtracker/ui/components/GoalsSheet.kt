package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ehtracker.util.formatMoney
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Flag
import com.example.ehtracker.data.model.SavingsGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSheet(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit,
    onUpdate: (String, String, Double) -> Unit,
    onDelete: (String) -> Unit,
    onContribute: (String, Double) -> Unit,
    onWithdraw: (String, Double) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var contributeGoalId by remember { mutableStateOf<String?>(null) }
    var deleteGoalId by remember { mutableStateOf<String?>(null) }

    val editingGoal = editingId?.let { id -> goals.find { it.id == id } }

    if (showForm) {
        GoalFormSheet(
            initial = editingGoal,
            currencySymbol = currencySymbol,
            onDismiss = { showForm = false; editingId = null },
            onSave = { name, target ->
                if (editingGoal != null) {
                    onUpdate(editingGoal.id, name, target)
                } else {
                    onAdd(name, target)
                }
                showForm = false
                editingId = null
            },
            onDelete = if (editingGoal != null) {
                { onDelete(editingGoal.id); showForm = false; editingId = null }
            } else null
        )
    } else {
        EHTBottomSheet(onDismissRequest = onDismiss) {
            Text(
                text = "Savings goals",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a goal to add a contribution",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Flag,
                    title = "No goals yet",
                    subtitle = "Create a savings goal to track progress. Add contributions and watch the bar fill up as you save."
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    goals.forEach { goal ->
                        GoalRow(
                            goal = goal,
                            currencySymbol = currencySymbol,
                            onContribute = { contributeGoalId = goal.id },
                            onEdit = { editingId = goal.id; showForm = true },
                            onDelete = { deleteGoalId = goal.id }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showForm = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Add goal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    val contributeGoal = contributeGoalId?.let { id -> goals.find { it.id == id } }
    if (contributeGoal != null) {
        ContributionDialog(
            goal = contributeGoal,
            currencySymbol = currencySymbol,
            onDismiss = { contributeGoalId = null },
            onDeposit = { amount ->
                onContribute(contributeGoal.id, amount)
                contributeGoalId = null
            },
            onWithdraw = { amount ->
                onWithdraw(contributeGoal.id, amount)
                contributeGoalId = null
            }
        )
    }

    val deleteGoal = deleteGoalId?.let { id -> goals.find { it.id == id } }
    if (deleteGoal != null) {
        AlertDialog(
            onDismissRequest = { deleteGoalId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconContentColor = MaterialTheme.colorScheme.primary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete goal") },
            text = { Text("Delete \"${deleteGoal.name}\"? This can\u2019t be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(deleteGoal.id)
                        deleteGoalId = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteGoalId = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun GoalRow(
    goal: SavingsGoal,
    currencySymbol: String,
    onContribute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onContribute)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (goal.progress >= 1f) {
                        "Completed!"
                    } else {
                        "$currencySymbol${formatMoney(goal.currentAmount)} of $currencySymbol${formatMoney(goal.targetAmount)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.progress >= 1f) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (goal.progress >= 1f) Icons.Filled.CheckCircle else Icons.Filled.Edit,
                contentDescription = if (goal.progress >= 1f) "Completed" else "Edit",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEdit() },
                tint = if (goal.progress >= 1f) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        if (goal.progress < 1f) {
            Spacer(modifier = Modifier.height(6.dp))
            val animatedProgress by animateFloatAsState(
                targetValue = goal.progress,
                animationSpec = tween(200),
                label = "goalProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalFormSheet(
    initial: SavingsGoal?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var target by remember { mutableStateOf(if (initial != null) "%.2f".format(initial.targetAmount) else "") }
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = if (initial != null) "Edit goal" else "New goal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(value = name, onValueChange = { name = it }, label = "Name")
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = target,
                onValueChange = { newVal ->
                    var dotSeen = false
                    target = newVal.filter { c ->
                        when {
                            c.isDigit() -> true
                            c == '.' && !dotSeen -> { dotSeen = true; true }
                            c == ',' || c == ' ' -> true
                            else -> false
                        }
                    }
                },
                label = { Text("Target amount") },
                prefix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(onClick = {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDelete()
                    }) {
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
                            val parsedTarget = com.example.ehtracker.util.parseAmount(target)
                            if (name.isNotBlank() && parsedTarget != null && parsedTarget > 0) {
                                onSave(name.trim(), parsedTarget)
                            }
                        },
                        enabled = name.isNotBlank() && com.example.ehtracker.util.parseAmount(target) != null && (com.example.ehtracker.util.parseAmount(target) ?: 0.0) > 0
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionDialog(
    goal: SavingsGoal,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit,
    onWithdraw: (Double) -> Unit
) {
    var isWithdraw by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    val parsed = com.example.ehtracker.util.parseAmount(amount)
    val canConfirm = parsed != null && parsed > 0 && (!isWithdraw || parsed <= goal.currentAmount)
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconContentColor = MaterialTheme.colorScheme.primary,
        title = {
            Text(if (isWithdraw) "Withdraw from \"${goal.name}\"" else "Add to \"${goal.name}\"")
        },
        text = {
            Column {
                FilterChipDialog(isWithdraw = isWithdraw, onSelect = { isWithdraw = it })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newVal ->
                        var dotSeen = false
                        amount = newVal.filter { c ->
                            when {
                                c.isDigit() -> true
                                c == '.' && !dotSeen -> { dotSeen = true; true }
                                c == ',' || c == ' ' -> true
                                else -> false
                            }
                        }
                    },
                    label = { Text("Amount") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isWithdraw) {
                        "Saved $currencySymbol${formatMoney(goal.currentAmount)} of $currencySymbol${formatMoney(goal.targetAmount)} \u2014 max to withdraw: $currencySymbol${formatMoney(goal.currentAmount)}"
                    } else {
                        "Saved $currencySymbol${formatMoney(goal.currentAmount)} of $currencySymbol${formatMoney(goal.targetAmount)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    parsed?.let {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
                        if (isWithdraw) onWithdraw(it) else onDeposit(it)
                    }
                },
                enabled = canConfirm
            ) {
                Text(
                    text = if (isWithdraw) "Withdraw" else "Add",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun FilterChipDialog(
    isWithdraw: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (!isWithdraw) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onSelect(false) }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Deposit",
                style = MaterialTheme.typography.labelSmall,
                color = if (!isWithdraw) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isWithdraw) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onSelect(true) }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Withdraw",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWithdraw) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}