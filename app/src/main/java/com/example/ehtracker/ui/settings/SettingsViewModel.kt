package com.example.ehtracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.NotificationHelper
import com.example.ehtracker.data.repository.TrackerRepository
import com.example.ehtracker.util.ExportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(private val repository: TrackerRepository) : ViewModel() {

    val themeMode: StateFlow<String> = repository.themeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    val notificationHour: StateFlow<Int> = repository.notificationHour()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val notificationMinute: StateFlow<Int> = repository.notificationMinute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setNotificationHour(hour: Int) {
        viewModelScope.launch { repository.setNotificationHour(hour) }
    }

    fun setNotificationMinute(minute: Int) {
        viewModelScope.launch { repository.setNotificationMinute(minute) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    val biometricEnabled: StateFlow<Boolean> = repository.biometricEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    fun rescheduleSubscriptionReminders(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.scheduleSubscriptionReminders(context)
        }
    }

    fun cancelSubscriptionReminders(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val subscriptions = repository.getActiveSubscriptionsOnce()
            for (sub in subscriptions) {
                NotificationHelper.cancelAllSubscriptionReminders(context, sub.id)
            }
        }
    }

    fun rescheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        NotificationHelper.scheduleDailyReminder(context, hour, minute)
    }

    fun cancelDailyReminder(context: Context) {
        NotificationHelper.cancelReminder(context)
    }

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult.asSharedFlow()

    fun exportToJson(context: Context) {
        viewModelScope.launch {
            _isExporting.value = true
            val file = ExportManager.exportToJson(context, repository)
            _isExporting.value = false
            if (file != null) {
                _exportResult.emit(ExportResult.Success(file, "application/json"))
            } else {
                _exportResult.emit(ExportResult.Error("Export failed"))
            }
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _isExporting.value = true
            val file = ExportManager.exportToCsv(context, repository)
            _isExporting.value = false
            if (file != null) {
                _exportResult.emit(ExportResult.Success(file, "text/csv"))
            } else {
                _exportResult.emit(ExportResult.Error("Export failed"))
            }
        }
    }

    fun shareExport(context: Context, file: File, mimeType: String) {
        ExportManager.shareFile(context, file, mimeType)
    }

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

sealed class ExportResult {
    data class Success(val file: File, val mimeType: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
