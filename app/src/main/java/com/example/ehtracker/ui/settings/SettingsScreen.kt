package com.example.ehtracker.ui.settings

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ehtracker.lock.AppLockManager
import com.example.ehtracker.ui.components.SectionHeader
import com.example.ehtracker.ui.components.AppTextField
import com.example.ehtracker.ui.theme.BluePrimary
import com.example.ehtracker.ui.theme.BluePrimaryDark
import com.example.ehtracker.ui.theme.BrownPrimary
import com.example.ehtracker.ui.theme.BrownPrimaryDark
import com.example.ehtracker.ui.theme.CoralPrimary
import com.example.ehtracker.ui.theme.CoralPrimaryDark
import com.example.ehtracker.ui.theme.CyanPrimary
import com.example.ehtracker.ui.theme.CyanPrimaryDark
import com.example.ehtracker.ui.theme.GruvboxPrimary
import com.example.ehtracker.ui.theme.GruvboxPrimaryDark
import com.example.ehtracker.ui.theme.IndigoPrimary
import com.example.ehtracker.ui.theme.IndigoPrimaryDark
import com.example.ehtracker.ui.theme.LightAccent
import com.example.ehtracker.ui.theme.NordPrimary
import com.example.ehtracker.ui.theme.NordPrimaryDark
import com.example.ehtracker.ui.theme.OneDarkPrimary
import com.example.ehtracker.ui.theme.OneDarkPrimaryDark
import com.example.ehtracker.ui.theme.PurplePrimary
import com.example.ehtracker.ui.theme.PurplePrimaryDark
import com.example.ehtracker.ui.theme.RedPrimary
import com.example.ehtracker.ui.theme.RedPrimaryDark
import com.example.ehtracker.ui.theme.RosePrimary
import com.example.ehtracker.ui.theme.RosePrimaryDark
import com.example.ehtracker.ui.theme.SlatePrimary
import com.example.ehtracker.ui.theme.SlatePrimaryDark
import com.example.ehtracker.ui.theme.SolarizedPrimary
import com.example.ehtracker.ui.theme.SolarizedPrimaryDark
import com.example.ehtracker.ui.theme.TealPrimary
import com.example.ehtracker.ui.theme.TokyoNightPrimary
import com.example.ehtracker.ui.theme.TokyoNightPrimaryDark

data class PaletteOption(val name: String, val label: String, val lightColor: Color, val darkColor: Color)

private val palettes = listOf(
    PaletteOption("dynamic", "Dynamic", Color(0xFF6750A4), Color(0xFFD0BCFF)),
    PaletteOption("red", "Red", RedPrimary, RedPrimaryDark),
    PaletteOption("coral", "Coral", CoralPrimary, CoralPrimaryDark),
    PaletteOption("amber", "Amber", Color(0xFFFF8F00), Color(0xFFFFD54F)),
    PaletteOption("green", "Green", LightAccent, Color(0xFF6EC690)),
    PaletteOption("teal", "Teal", TealPrimary, Color(0xFF80CBC4)),
    PaletteOption("cyan", "Cyan", CyanPrimary, CyanPrimaryDark),
    PaletteOption("blue", "Blue", BluePrimary, BluePrimaryDark),
    PaletteOption("indigo", "Indigo", IndigoPrimary, IndigoPrimaryDark),
    PaletteOption("purple", "Purple", PurplePrimary, PurplePrimaryDark),
    PaletteOption("rose", "Rose", RosePrimary, RosePrimaryDark),
    PaletteOption("brown", "Brown", BrownPrimary, BrownPrimaryDark),
    PaletteOption("slate", "Slate", SlatePrimary, SlatePrimaryDark),
    PaletteOption("solarized", "Solarized", SolarizedPrimary, SolarizedPrimaryDark),
    PaletteOption("onedark", "One Dark", OneDarkPrimary, OneDarkPrimaryDark),
    PaletteOption("nord", "Nord", NordPrimary, NordPrimaryDark),
    PaletteOption("tokyonight", "Tokyo Night", TokyoNightPrimary, TokyoNightPrimaryDark),
    PaletteOption("gruvbox", "Gruvbox", GruvboxPrimary, GruvboxPrimaryDark),
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    lockManager: AppLockManager,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val notificationMinute by viewModel.notificationMinute.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    var showPinSetup by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val selectedPalette = when {
        themeMode.startsWith("dynamic_") -> "dynamic"
        themeMode.startsWith("teal_") -> "teal"
        themeMode.startsWith("amber_") -> "amber"
        themeMode.startsWith("indigo_") -> "indigo"
        themeMode.startsWith("rose_") -> "rose"
        themeMode.startsWith("purple_") -> "purple"
        themeMode.startsWith("coral_") -> "coral"
        themeMode.startsWith("slate_") -> "slate"
        themeMode.startsWith("blue_") -> "blue"
        themeMode.startsWith("cyan_") -> "cyan"
        themeMode.startsWith("red_") -> "red"
        themeMode.startsWith("brown_") -> "brown"
        themeMode.startsWith("solarized_") -> "solarized"
        themeMode.startsWith("onedark_") -> "onedark"
        themeMode.startsWith("nord_") -> "nord"
        themeMode.startsWith("tokyonight_") -> "tokyonight"
        themeMode.startsWith("gruvbox_") -> "gruvbox"
        else -> "green"
    }
    val selectedMode = themeMode.removePrefix("${selectedPalette}_").ifEmpty { themeMode }
    val context = LocalContext.current
    fun requestNotificationPermission() {
        val activity = context as? Activity ?: return
        NotificationPermissionBridge.onResult = { granted ->
            if (granted) {
                viewModel.setNotificationsEnabled(true)
                viewModel.rescheduleDailyReminder(context, notificationHour, notificationMinute)
                viewModel.rescheduleSubscriptionReminders(context)
            }
        }
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            com.example.ehtracker.MainActivity.REQUEST_CODE_POST_NOTIFICATIONS
        )
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            notificationsEnabled &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
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
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestNotificationPermission()
                            return@Switch
                        }
                        viewModel.setNotificationsEnabled(true)
                        viewModel.rescheduleDailyReminder(context, notificationHour, notificationMinute)
                        viewModel.rescheduleSubscriptionReminders(context)
                    } else {
                        viewModel.setNotificationsEnabled(false)
                        viewModel.cancelDailyReminder(context)
                        viewModel.cancelSubscriptionReminders(context)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (notificationsEnabled) {
            Spacer(modifier = Modifier.height(4.dp))
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
                                viewModel.rescheduleDailyReminder(context, hour, minute)
                                viewModel.rescheduleSubscriptionReminders(context)
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

        Spacer(modifier = Modifier.height(4.dp))
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
                    text = "Lock with fingerprint",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (biometricEnabled) "Locked. Uses fingerprint + 4-digit PIN fallback"
                        else "Require biometric unlock when opening the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = biometricEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        pinInput = ""
                        pinConfirm = ""
                        pinError = null
                        showPinSetup = true
                    } else {
                        viewModel.setBiometricEnabled(false)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = "Data", modifier = Modifier.padding(bottom = 8.dp))

        val isExporting by viewModel.isExporting.collectAsState()
        LaunchedEffect(Unit) {
            viewModel.exportResult.collect { result ->
                when (result) {
                    is ExportResult.Success -> viewModel.shareExport(context, result.file, result.mimeType)
                    is ExportResult.Error -> { /* snackbar would be ideal */ }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = !isExporting) { viewModel.exportToJson(context) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Export as JSON",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isExporting) "Exporting…" else "Full backup of all your data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = !isExporting) { viewModel.exportToCsv(context) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Export as CSV",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isExporting) "Exporting…" else "Spreadsheet-friendly format for transactions",
                    style = MaterialTheme.typography.bodySmall,
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

    if (showPinSetup) {
        AlertDialog(
            onDismissRequest = { showPinSetup = false },
            title = { Text("Set unlock PIN") },
            text = {
                Column {
                    Text(
                        text = if (lockManager.biometricAvailable())
                            "Choose a 4-digit PIN to use as backup when your fingerprint is unavailable."
                        else
                            "No biometric sensor detected. Choose a 4-digit PIN to lock the app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppTextField(
                        value = pinInput,
                        onValueChange = { newVal ->
                            if (newVal.length <= 4) pinInput = newVal.filter { it.isDigit() }
                        },
                        label = "PIN (4 digits)",
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = pinConfirm,
                        onValueChange = { newVal ->
                            if (newVal.length <= 4) pinConfirm = newVal.filter { it.isDigit() }
                        },
                        label = "Confirm PIN",
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        )
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        pinInput.length != 4 -> pinError = "PIN must be 4 digits"
                        pinConfirm != pinInput -> pinError = "PINs do not match"
                        else -> {
                            lockManager.savePin(pinInput)
                            viewModel.setBiometricEnabled(true)
                            pinError = null
                            showPinSetup = false
                        }
                    }
                }) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetup = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteSelector(
    palettes: List<PaletteOption>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPalette = palettes.firstOrNull { it.name == selected }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        AppTextField(
            value = selectedPalette?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Palette",
            prefix = {
                if (selectedPalette != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(selectedPalette.lightColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(selectedPalette.darkColor)
                        )
                    }
                }
            },
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
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            palettes.forEach { palette ->
                val isSelected = palette.name == selected
                val swatch: @Composable () -> Unit = {
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
                }
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            swatch()
                            Text(
                                text = palette.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    onClick = {
                        onSelect(palette.name)
                        expanded = false
                    }
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
