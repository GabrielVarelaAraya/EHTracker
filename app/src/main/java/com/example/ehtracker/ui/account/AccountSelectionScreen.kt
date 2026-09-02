package com.example.ehtracker.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import com.example.ehtracker.util.formatMoney
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ehtracker.data.model.Account
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.repository.TrackerRepository
import com.example.ehtracker.lock.AppLockManager
import com.example.ehtracker.ui.components.EHTBottomSheet
import com.example.ehtracker.ui.settings.SettingsScreen
import com.example.ehtracker.ui.settings.SettingsViewModel
import com.example.ehtracker.ui.theme.AccountIcon

val AccountIconOptions = listOf(
    "credit_card",
    "card",
    "assignment",
    "inventory2",
    "receipt",
    "directions_car",
    "food",
    "shopping_cart",
    "coffee",
    "fitness_center",
    "menu_book",
    "music_note",
    "movie",
    "local_pharmacy",
    "lunch_dining",
    "waving_hand"
)

fun Color.toHexString(): String {
    return "#%06X".format(toArgb() and 0xFFFFFF)
}

@Composable
fun AccountSelectionScreen(
    repository: TrackerRepository,
    lockManager: AppLockManager,
    onSelectAccount: (String) -> Unit
) {
    val summaries by repository.accountSummaries().collectAsState(initial = emptyList())
    var showForm by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(repository)
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your accounts",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose an account to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Hold to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

        items(summaries, key = { it.account.id }) { summary ->
            AccountCard(
                name = summary.account.name,
                icon = summary.account.icon,
                currency = summary.account.currency,
                balance = summary.balance,
                onClick = { onSelectAccount(summary.account.id) },
                onEdit = {
                    editingAccount = summary.account
                    showForm = true
                }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        editingAccount = null
                        showForm = true
                    }
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ New account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            androidx.activity.compose.BackHandler { showSettings = false }
            SettingsScreen(
                viewModel = settingsViewModel,
                lockManager = lockManager,
                onBack = { showSettings = false }
            )
        }
    }

    if (showForm) {
        AccountFormSheet(
            account = editingAccount,
            onDismiss = {
                showForm = false
                editingAccount = null
            },
            onSave = { name, icon, colorHex, initialBalance, currency ->
                val account = editingAccount
                if (account == null) {
                    scope.launch { repository.addAccount(name, icon, colorHex, initialBalance, currency) }
                } else {
                    scope.launch { repository.updateAccount(account.id, name, icon, colorHex, initialBalance, currency) }
                }
                showForm = false
                editingAccount = null
            },
            onDelete = editingAccount?.let { account ->
                {
                    scope.launch { repository.deleteAccount(account.id) }
                    showForm = false
                    editingAccount = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountCard(
    name: String,
    icon: String,
    currency: Currency,
    balance: Double,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val cardBackground = MaterialTheme.colorScheme.surface
    val cardForeground = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onEdit)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AccountIcon(
                    icon = icon,
                    modifier = Modifier.size(26.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = cardForeground
                )
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = cardForeground.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${currency.symbol}${formatMoney(balance)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = cardForeground
                )
                Text(
                    text = if (balance >= 0) "Available" else "Negative",
                    style = MaterialTheme.typography.labelSmall,
                    color = cardForeground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountFormSheet(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Currency) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var icon by remember { mutableStateOf(account?.icon ?: AccountIconOptions.first()) }
    var initialBalance by remember {
        mutableStateOf(if (account != null && account.initialBalance != 0.0) "%.2f".format(account.initialBalance) else "")
    }
    var currency by remember { mutableStateOf(account?.currency ?: Currency.USD) }
    var currencyExpanded by remember { mutableStateOf(false) }
    val themeColorHex = MaterialTheme.colorScheme.primary.toHexString()

    EHTBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = if (account == null) "New account" else "Edit account",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Account name") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Icon",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountIconOptions.forEach { option ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = if (option == icon) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { icon = option },
                    contentAlignment = Alignment.Center
                ) {
                    AccountIcon(
                        icon = option,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null,
                        tint = if (option == icon) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = initialBalance,
            onValueChange = { newVal ->
                initialBalance = newVal.filterIndexed { index, c ->
                    c.isDigit() || (c == '.' && newVal.take(index).none { it == '.' })
                }
            },
            label = { Text("Initial balance") },
            prefix = { Text(currency.symbol) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box {
            OutlinedTextField(
                value = "${currency.symbol}  ${currency.displayName} (${currency.code})",
                onValueChange = {},
                readOnly = true,
                label = { Text("Currency") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { currencyExpanded = !currencyExpanded }) {
                        Text(if (currencyExpanded) "▲" else "▼")
                    }
                }
            )
            DropdownMenu(
                expanded = currencyExpanded,
                onDismissRequest = { currencyExpanded = false },
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(8.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Currency.entries.forEach { option ->
                    val isSelected = option == currency
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${option.symbol}  ${option.displayName} (${option.code})",
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            currency = option
                            currencyExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val balance = initialBalance.toDoubleOrNull() ?: 0.0
                    onSave(name.trim().ifEmpty { "Account" }, icon, themeColorHex, balance, currency)
                },
                enabled = initialBalance.isEmpty() || initialBalance.toDoubleOrNull() != null,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (account == null) "Create" else "Save")
            }
        }
    }
}
