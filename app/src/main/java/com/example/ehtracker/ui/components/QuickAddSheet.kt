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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.ui.theme.CategoryIcon
import com.example.ehtracker.ui.theme.HabitIcon
import com.example.ehtracker.data.model.ExpenseCategory
import com.example.ehtracker.data.model.HabitIcons
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAddExpense: (Double, ExpenseCategory, String, LocalDate) -> Unit,
    onAddIncome: (Double, String, LocalDate) -> Unit,
    onAddHabit: (String, String, Int, Boolean, String) -> Unit,
    currencySymbol: String = "$"
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Expense", "Income", "Habit")

    EHTBottomSheet(
        onDismissRequest = onDismiss
    ) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(380.dp)
            ) { page ->
                when (page) {
                    0 -> ExpenseTabContent(onAdd = onAddExpense, onDismiss = onDismiss, currencySymbol = currencySymbol)
                    1 -> IncomeTabContent(onAdd = onAddIncome, onDismiss = onDismiss, currencySymbol = currencySymbol)
                    2 -> HabitTabContent(onAdd = onAddHabit, onDismiss = onDismiss)
                }
            }
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseTabContent(
    onAdd: (Double, ExpenseCategory, String, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    currencySymbol: String = "$"
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AmountField(value = amount, onValueChange = { amount = it }, currencySymbol = currencySymbol)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExpenseCategory.entries.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(emoji = cat.icon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DateField(date = selectedDate, onDateChange = { selectedDate = it })

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(value = note, onValueChange = { note = it }, label = "Note (optional)")

        Spacer(modifier = Modifier.height(16.dp))

        FormActions(
            onCancel = onDismiss,
            onConfirm = {
                val parsedAmount = amount.toDoubleOrNull()
                if (parsedAmount != null && parsedAmount > 0) {
                    onAdd(parsedAmount, selectedCategory, note.ifBlank { selectedCategory.displayName }, selectedDate)
                }
            },
            confirmEnabled = amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0,
            confirmText = "Add"
        )
    }
}

@Composable
private fun IncomeTabContent(
    onAdd: (Double, String, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    currencySymbol: String = "$"
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AmountField(value = amount, onValueChange = { amount = it }, currencySymbol = currencySymbol)

        Spacer(modifier = Modifier.height(12.dp))

        DateField(date = selectedDate, onDateChange = { selectedDate = it })

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(value = note, onValueChange = { note = it }, label = "Note (optional)")

        Spacer(modifier = Modifier.height(16.dp))

        FormActions(
            onCancel = onDismiss,
            onConfirm = {
                val parsedAmount = amount.toDoubleOrNull()
                if (parsedAmount != null && parsedAmount > 0) {
                    onAdd(parsedAmount, note.ifBlank { "Income" }, selectedDate)
                }
            },
            confirmEnabled = amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0,
            confirmText = "Add"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitTabContent(
    onAdd: (String, String, Int, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(HabitIcons[0]) }
    var targetDays by remember { mutableStateOf(7) }
    var isNumeric by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(value = name, onValueChange = { name = it }, label = "Habit name")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Icon",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HabitIcons.forEach { icon ->
                val isSelected = icon == selectedIcon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedIcon = icon },
                    contentAlignment = Alignment.Center
                ) {
                    HabitIcon(emoji = icon, modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track numeric value (e.g. hours, kg, cups)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = isNumeric,
                onCheckedChange = { isNumeric = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (isNumeric) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (e.g. hours)") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Target: $targetDays days/week",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..7).forEach { day ->
                val isSelected = day == targetDays
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { targetDays = day },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$day",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormActions(
            onCancel = onDismiss,
            onConfirm = {
                if (name.isNotBlank()) {
                    onAdd(name.trim(), selectedIcon, targetDays, isNumeric, unit.trim())
                }
            },
            confirmEnabled = name.isNotBlank(),
            confirmText = "Add"
        )
    }
}
