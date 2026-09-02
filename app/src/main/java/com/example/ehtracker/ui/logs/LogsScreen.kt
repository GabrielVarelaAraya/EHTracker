package com.example.ehtracker.ui.logs

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import com.example.ehtracker.ui.components.EmptyState
import com.example.ehtracker.util.formatMoney
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ehtracker.data.model.Category
import com.example.ehtracker.data.model.Expense
import com.example.ehtracker.data.model.Habit
import com.example.ehtracker.data.model.HabitIcons
import com.example.ehtracker.ui.theme.CategoryIcon
import com.example.ehtracker.ui.theme.HabitIcon
import com.example.ehtracker.data.model.Income
import com.example.ehtracker.data.model.Transaction
import com.example.ehtracker.data.model.resolveCategory
import com.example.ehtracker.data.model.toExpense
import com.example.ehtracker.data.model.toIncome
import com.example.ehtracker.ui.components.CategoryPicker
import com.example.ehtracker.ui.components.EHTBottomSheet
import com.example.ehtracker.ui.components.ShimmerExpenseRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(viewModel: LogsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Habits", "Transactions")
    var showFilterSheet by remember { mutableStateOf(false) }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SecondaryTabRow(
                    selectedTabIndex = state.selectedTab,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (state.selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LogsSearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::updateSearch,
                hint = if (state.selectedTab == 0) "Search habits…" else "Search transactions…",
                showFilterButton = state.selectedTab == 1,
                activeFilters = state.activeFilterCount,
                onOpenFilters = { showFilterSheet = true }
            )

            if (state.selectedTab == 1 && !state.isLoading &&
                (state.activeFilterCount > 0 || state.searchQuery.isNotBlank())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                ActiveFiltersRow(
                    typeFilter = state.typeFilter,
                    selectedCategories = state.selectedCategories,
                    datePreset = state.datePreset,
                    customStart = state.customStart,
                    customEnd = state.customEnd,
                    query = state.searchQuery,
                    catalog = state.categories,
                    onRemoveType = { viewModel.setTypeFilter(TransactionTypeFilter.ALL) },
                    onRemoveCategory = { viewModel.toggleCategoryFilter(it) },
                    onRemoveDate = { viewModel.setDatePreset(DateRangePreset.ALL_TIME) },
                    onClearQuery = { viewModel.updateSearch("") },
                    onClearAll = {
                        viewModel.clearFilters()
                        viewModel.updateSearch("")
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "\u2190 Swipe to delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\u00B7",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap to edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (state.isLoading) {
                repeat(5) {
                    ShimmerExpenseRow()
                    Spacer(modifier = Modifier.height(4.dp))
                }
            } else {
                when (state.selectedTab) {
                    0 -> HabitList(
                        habits = state.habits,
                        onEdit = { viewModel.showEditHabit(it) },
                        onDelete = { viewModel.deleteHabit(it) }
                    )
                    1 -> {
                        val filtersActive =
                            state.activeFilterCount > 0 || state.searchQuery.isNotBlank()
                        if (state.transactions.isEmpty() && filtersActive) {
                            NoResultsBlock(onClear = {
                                viewModel.clearFilters()
                                viewModel.updateSearch("")
                            })
                        } else {
                            TransactionList(
                                transactions = state.transactions,
                                onEdit = { viewModel.showEditTransaction(it) },
                                onDelete = { transaction ->
                                    when (transaction) {
                                        is Transaction.Expense -> viewModel.deleteExpense(transaction.id)
                                        is Transaction.Income -> viewModel.deleteIncome(transaction.id)
                                    }
                                },
                                currencySymbol = state.currency.symbol,
                                catalog = state.categories
                            )
                        }
                    }
                }
            }
        }
    }

    state.editingTransaction?.let { transaction ->
        when (transaction) {
            is Transaction.Expense -> EditExpenseSheet(
                expense = transaction.toExpense(),
                onDismiss = { viewModel.dismissEditExpense() },
                onSave = { amount, category, note, date ->
                    viewModel.updateExpense(transaction.id, amount, category, note, date)
                },
                currencySymbol = state.currency.symbol,
                categories = state.categories,
                onAddCategory = viewModel::addCategory,
                onUpdateCategory = viewModel::updateCategory,
                onDeleteCategory = viewModel::deleteCategory
            )
            is Transaction.Income -> EditIncomeSheet(
                income = transaction.toIncome(),
                onDismiss = { viewModel.dismissEditIncome() },
                onSave = { amount, note, date ->
                    viewModel.updateIncome(transaction.id, amount, note, date)
                },
                currencySymbol = state.currency.symbol
            )
        }
    }

    state.editingHabit?.let { habit ->
        EditHabitSheet(
            habit = habit,
            onDismiss = { viewModel.dismissEditHabit() },
            onSave = { name, icon, targetDays, isNumeric, unit ->
                viewModel.updateHabit(habit.id, name, icon, targetDays, isNumeric, unit)
            }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            typeFilter = state.typeFilter,
            selectedCategories = state.selectedCategories,
            datePreset = state.datePreset,
            customStart = state.customStart,
            customEnd = state.customEnd,
            catalog = state.categories,
            onDismiss = { showFilterSheet = false },
            onTypeSelect = viewModel::setTypeFilter,
            onToggleCategory = viewModel::toggleCategoryFilter,
            onPresetSelect = viewModel::setDatePreset,
            onCustomRange = viewModel::setCustomDateRange,
            onClearAll = {
                viewModel.clearFilters()
                viewModel.updateSearch("")
            }
        )
    }
}

@Composable
private fun HabitList(
    habits: List<Habit>,
    onEdit: (Habit) -> Unit,
    onDelete: (String) -> Unit
) {
    var swipeConfirmHabit by remember { mutableStateOf<Habit?>(null) }

    swipeConfirmHabit?.let { habit ->
        AlertDialog(
            onDismissRequest = { swipeConfirmHabit = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconContentColor = MaterialTheme.colorScheme.primary,
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete \"${habit.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(habit.id)
                    swipeConfirmHabit = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { swipeConfirmHabit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (habits.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.EventNote,
            title = "No habits yet",
            subtitle = "Create your first habit from the Home tab — tap + to track daily, numeric or weekly goals."
        )
        return
    }

    LazyColumn {
        items(habits, key = { it.id }) { habit ->
            val habitIndex = habits.indexOf(habit)
            Box(modifier = Modifier.animateItem()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = habitIndex * 40)) +
                            slideInVertically(animationSpec = tween(300, delayMillis = habitIndex * 40)) { it / 4 }
                ) {
                    Column {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    swipeConfirmHabit = habit
                                    false
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            modifier = Modifier.clip(RoundedCornerShape(9.dp)),
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.error)
                            .padding(end = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onEdit(habit) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        HabitIcon(
                            emoji = habit.icon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${habit.currentStreak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(habit.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { swipeConfirmHabit = habit }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                        }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    currencySymbol: String = "$",
    catalog: List<Category> = emptyList()
) {
    var swipeConfirmTransaction by remember { mutableStateOf<Transaction?>(null) }

    swipeConfirmTransaction?.let { transaction ->
        val label = when (transaction) {
            is Transaction.Expense -> "expense"
            is Transaction.Income -> "income"
        }
        AlertDialog(
            onDismissRequest = { swipeConfirmTransaction = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconContentColor = MaterialTheme.colorScheme.primary,
            title = { Text("Delete $label") },
            text = { Text("Are you sure you want to delete this $label?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    swipeConfirmTransaction = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { swipeConfirmTransaction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (transactions.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.CreditCard,
            title = "No transactions yet",
            subtitle = "Your history will appear here. Tap + to log an expense, income or habit and start building insights."
        )
        return
    }

    val grouped = transactions.groupBy { it.date }.toSortedMap(compareByDescending { it })

    LazyColumn {
        grouped.forEach { (date, dayTransactions) ->
            val netTotal = dayTransactions.sumOf {
                when (it) {
                    is Transaction.Income -> it.amount
                    is Transaction.Expense -> -it.amount
                }
            }
            item(key = "txn_header_$date") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${if (netTotal >= 0) "+" else ""}$currencySymbol${formatMoney(netTotal)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (netTotal >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            items(dayTransactions, key = {
                when (it) {
                    is Transaction.Expense -> "exp_${it.id}"
                    is Transaction.Income -> "inc_${it.id}"
                }
            }) { transaction ->
                val txnIndex = dayTransactions.indexOf(transaction)
                Box(modifier = Modifier.animateItem()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = txnIndex * 30)) +
                                slideInVertically(animationSpec = tween(300, delayMillis = txnIndex * 30)) { it / 4 }
                    ) {
                        Column {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        swipeConfirmTransaction = transaction
                                        false
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                modifier = Modifier.clip(RoundedCornerShape(9.dp)),
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.error)
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onEdit(transaction) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isWithdrawal = transaction is Transaction.Expense && transaction.amount < 0
                        when (transaction) {
                            is Transaction.Expense -> {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowDownward,
                                        contentDescription = "Expense",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = transaction.note,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = resolveCategory(transaction.category, catalog).name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is Transaction.Income -> {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowUpward,
                                        contentDescription = "Income",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = transaction.note,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Income",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isWithdrawal) {
                                "+$currencySymbol${formatMoney(-transaction.amount)}"
                            } else "$currencySymbol${formatMoney(transaction.amount)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isWithdrawal) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { swipeConfirmTransaction = transaction }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseSheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onSave: (Double, String, String, LocalDate) -> Unit,
    currencySymbol: String = "$",
    categories: List<Category> = emptyList(),
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCategory: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf("%.2f".format(expense.amount)) }
    var note by remember { mutableStateOf(expense.note) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var selectedDate by remember { mutableStateOf(expense.date) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit Expense",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newVal ->
                    amount = newVal.filterIndexed { index, c ->
                        c.isDigit() || (c == '.' && newVal.take(index).none { it == '.' })
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

            Spacer(modifier = Modifier.height(12.dp))

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

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        selectedDate = LocalDate.of(year, month + 1, day)
                                    },
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                ).show()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null && parsedAmount > 0) {
                            val resolvedName = resolveCategory(selectedCategory, categories).name
                            onSave(parsedAmount, selectedCategory, note.ifBlank { resolvedName }, selectedDate)
                        }
                    },
                    enabled = amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditHabitSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Boolean, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(habit.name) }
    var selectedIcon by remember { mutableStateOf(habit.icon) }
    var targetDays by remember { mutableIntStateOf(habit.targetDaysPerWeek) }
    var isNumeric by remember { mutableStateOf(habit.isNumeric) }
    var unit by remember { mutableStateOf(habit.unit) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit Habit",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Icon",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HabitIcons.take(8).forEach { icon ->
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
                        Text(text = icon, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            .clip(CircleShape)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track numeric value",
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), selectedIcon, targetDays, isNumeric, unit.trim())
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIncomeSheet(
    income: Income,
    onDismiss: () -> Unit,
    onSave: (Double, String, LocalDate) -> Unit,
    currencySymbol: String = "$"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf("%.2f".format(income.amount)) }
    var note by remember { mutableStateOf(income.note) }
    var selectedDate by remember { mutableStateOf(income.date) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit Income",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newVal ->
                    amount = newVal.filterIndexed { index, c ->
                        c.isDigit() || (c == '.' && newVal.take(index).none { it == '.' })
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        selectedDate = LocalDate.of(year, month + 1, day)
                                    },
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                ).show()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null && parsedAmount > 0) {
                            onSave(parsedAmount, note.ifBlank { "Income" }, selectedDate)
                        }
                    },
                    enabled = amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LogsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    showFilterButton: Boolean,
    activeFilters: Int,
    onOpenFilters: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (showFilterButton) {
                    BadgedBox(
                        badge = {
                            if (activeFilters > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(activeFilters.toString())
                                }
                            }
                        }
                    ) {}
                    IconButton(onClick = onOpenFilters) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Filters",
                            tint = if (activeFilters > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ActiveFiltersRow(
    typeFilter: TransactionTypeFilter,
    selectedCategories: Set<String>,
    datePreset: DateRangePreset,
    customStart: LocalDate?,
    customEnd: LocalDate?,
    query: String,
    catalog: List<Category>,
    onRemoveType: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onRemoveDate: () -> Unit,
    onClearQuery: () -> Unit,
    onClearAll: () -> Unit
) {
    val dateRangeFormatter = DateTimeFormatter.ofPattern("MMM d")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (query.isNotBlank()) {
            FilterPill(label = "\"$query\"", onRemove = onClearQuery)
        }
        if (typeFilter != TransactionTypeFilter.ALL) {
            FilterPill(label = typeFilter.label, onRemove = onRemoveType)
        }
        selectedCategories.forEach { name ->
            val cat = resolveCategory(name, catalog)
            FilterPill(
                label = cat.name,
                icon = cat.icon,
                onRemove = { onRemoveCategory(name) }
            )
        }
        if (datePreset != DateRangePreset.ALL_TIME) {
            val label = when (datePreset) {
                DateRangePreset.CUSTOM ->
                    if (customStart != null && customEnd != null)
                        "${customStart.format(dateRangeFormatter)} \u2013 ${customEnd.format(dateRangeFormatter)}"
                    else DateRangePreset.CUSTOM.label
                else -> datePreset.label
            }
            FilterPill(label = label, onRemove = onRemoveDate)
        }
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "Clear all",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClearAll)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FilterPill(label: String, onRemove: () -> Unit, icon: String? = null) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        if (icon != null) {
            CompositionLocalProvider(LocalContentColor provides accent) {
                CategoryIcon(
                    emoji = icon,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accent
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onRemove)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove filter",
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    typeFilter: TransactionTypeFilter,
    selectedCategories: Set<String>,
    datePreset: DateRangePreset,
    customStart: LocalDate?,
    customEnd: LocalDate?,
    catalog: List<Category>,
    onDismiss: () -> Unit,
    onTypeSelect: (TransactionTypeFilter) -> Unit,
    onToggleCategory: (String) -> Unit,
    onPresetSelect: (DateRangePreset) -> Unit,
    onCustomRange: (LocalDate, LocalDate) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onClearAll) {
                Text("Reset", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilterSectionLabel("Type")
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TransactionTypeFilter.entries.forEachIndexed { index, filter ->
                SegmentedButton(
                    selected = typeFilter == filter,
                    onClick = { onTypeSelect(filter) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionTypeFilter.entries.size)
                ) {
                    Text(filter.label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionLabel("Categories")
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            catalog.filter { !it.isSavings }.forEach { cat ->
                FilterChip(
                    selected = cat.id in selectedCategories,
                    onClick = { onToggleCategory(cat.id) },
                    leadingIcon = {
                        CompositionLocalProvider(
                            LocalContentColor provides if (cat.id in selectedCategories)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            CategoryIcon(
                                emoji = cat.icon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    label = {
                        Text(cat.name)
                    },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionLabel("Date range")
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRangePreset.entries.forEach { preset ->
                FilterChip(
                    selected = datePreset == preset,
                    onClick = {
                        when (preset) {
                            DateRangePreset.CUSTOM -> {
                                val base = customStart ?: LocalDate.now()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val start = LocalDate.of(year, month + 1, day)
                                        val endBase = customEnd ?: start
                                        DatePickerDialog(
                                            context,
                                            { _, year2, month2, day2 ->
                                                onCustomRange(start, LocalDate.of(year2, month2 + 1, day2))
                                            },
                                            endBase.year,
                                            endBase.monthValue - 1,
                                            endBase.dayOfMonth
                                        ).show()
                                    },
                                    base.year,
                                    base.monthValue - 1,
                                    base.dayOfMonth
                                ).show()
                            }
                            else -> onPresetSelect(preset)
                        }
                    },
                    label = { Text(preset.label) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NoResultsBlock(onClear: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No results found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Try adjusting your search or filters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClear) {
                Text("Clear all", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
