package com.example.ehtracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ehtracker.data.model.Currency
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: TrackerRepository) : ViewModel() {

    val themeMode: StateFlow<String> = repository.themeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val currency: StateFlow<Currency> = repository.currency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.USD)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { repository.setCurrency(currency) }
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

    class Factory(private val repository: TrackerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
