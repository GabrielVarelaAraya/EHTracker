package com.example.ehtracker.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.ui.unit.dp
import com.example.ehtracker.BuildConfig
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.ui.components.SectionHeader
import com.example.ehtracker.ui.components.AppTextField
import com.example.ehtracker.ui.theme.AmberPrimary
import com.example.ehtracker.ui.theme.AmberPrimaryDark
import com.example.ehtracker.ui.theme.CoralPrimary
import com.example.ehtracker.ui.theme.CoralPrimaryDark
import com.example.ehtracker.ui.theme.IndigoPrimary
import com.example.ehtracker.ui.theme.IndigoPrimaryDark
import com.example.ehtracker.ui.theme.LightAccent
import com.example.ehtracker.ui.theme.PurplePrimary
import com.example.ehtracker.ui.theme.PurplePrimaryDark
import com.example.ehtracker.ui.theme.RosePrimary
import com.example.ehtracker.ui.theme.RosePrimaryDark
import com.example.ehtracker.ui.theme.SlatePrimary
import com.example.ehtracker.ui.theme.SlatePrimaryDark
import com.example.ehtracker.ui.theme.TealPrimary

data class PaletteOption(val name: String, val label: String, val lightColor: Color, val darkColor: Color)

private val palettes = listOf(
    PaletteOption("green", "Green", LightAccent, Color(0xFF6EC690)),
    PaletteOption("teal", "Teal", TealPrimary, Color(0xFF80CBC4)),
    PaletteOption("indigo", "Indigo", IndigoPrimary, IndigoPrimaryDark),
    PaletteOption("rose", "Rose", RosePrimary, RosePrimaryDark),
    PaletteOption("purple", "Purple", PurplePrimary, PurplePrimaryDark),
    PaletteOption("coral", "Coral", CoralPrimary, CoralPrimaryDark),
    PaletteOption("slate", "Slate", SlatePrimary, SlatePrimaryDark),
    PaletteOption("amber", "Amber", Color(0xFFFF8F00), Color(0xFFFFD54F)),
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val notificationMinute by viewModel.notificationMinute.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    val selectedPalette = when {
        themeMode.startsWith("teal_") -> "teal"
        themeMode.startsWith("amber_") -> "amber"
        themeMode.startsWith("indigo_") -> "indigo"
        themeMode.startsWith("rose_") -> "rose"
        themeMode.startsWith("purple_") -> "purple"
        themeMode.startsWith("coral_") -> "coral"
        themeMode.startsWith("slate_") -> "slate"
        else -> "green"
    }
    val selectedMode = themeMode.removePrefix("${selectedPalette}_").ifEmpty { themeMode }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(text = "Palette", modifier = Modifier.padding(bottom = 12.dp))

        PaletteSelector(
            palettes = palettes,
            selected = selectedPalette,
            onSelect = { palette ->
                viewModel.setThemeMode(
                    if (palette == "green") selectedMode else "${palette}_$selectedMode"
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(text = "Mode", modifier = Modifier.padding(bottom = 12.dp))

        ThemeOption("System", "system", selectedMode) {
            val prefix = if (selectedPalette == "green") "" else "${selectedPalette}_"
            viewModel.setThemeMode("$prefix$it")
        }
        ThemeOption("Light", "light", selectedMode) {
            val prefix = if (selectedPalette == "green") "" else "${selectedPalette}_"
            viewModel.setThemeMode("$prefix$it")
        }
        ThemeOption("Dark", "dark", selectedMode) {
            val prefix = if (selectedPalette == "green") "" else "${selectedPalette}_"
            viewModel.setThemeMode("$prefix$it")
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = "Currency", modifier = Modifier.padding(bottom = 12.dp))

        CurrencySelector(
            selectedCurrency = currency,
            onCurrencyChange = { viewModel.setCurrency(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = "Notification", modifier = Modifier.padding(bottom = 12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily reminder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${"%02d".format(notificationHour)}:${"%02d".format(notificationMinute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (notificationsEnabled) {
            Spacer(modifier = Modifier.height(4.dp))
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.setNotificationHour(hour)
                                viewModel.setNotificationMinute(minute)
                            },
                            notificationHour,
                            notificationMinute,
                            true
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${"%02d".format(notificationHour)}:${"%02d".format(notificationMinute)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = "About", modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Version",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PaletteSelector(
    palettes: List<PaletteOption>,
    selected: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palettes.forEach { palette ->
            val isSelected = palette.name == selected
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(palette.name) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(palette.lightColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(palette.darkColor)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = palette.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    value: String,
    currentValue: String,
    onSelect: (String) -> Unit
) {
    val isSelected = value == currentValue
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(
    selectedCurrency: Currency,
    onCurrencyChange: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        AppTextField(
            value = "${selectedCurrency.symbol}  ${selectedCurrency.displayName} (${selectedCurrency.code})",
            onValueChange = {},
            readOnly = true,
            label = "Currency",
            trailing = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${currency.symbol}  ${currency.displayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = currency.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    onClick = {
                        onCurrencyChange(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}
