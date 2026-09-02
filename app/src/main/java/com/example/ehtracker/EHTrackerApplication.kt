package com.example.ehtracker

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.repository.TrackerRepository
import com.example.ehtracker.lock.AppLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EHTrackerApplication : Application(), LifecycleOwner {
    companion object {
        lateinit var instance: EHTrackerApplication
            private set
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ehtracker.db"
        )        .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17, AppDatabase.MIGRATION_17_18, AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21)
            .build()
    }

    val repository: TrackerRepository by lazy {
        TrackerRepository(database)
    }

    val lockManager: AppLockManager by lazy {
        AppLockManager(this, repository)
    }

    var cachedThemeMode: String = "system"
        private set
    var cachedNotificationHour: Int = 20
        private set
    var cachedNotificationMinute: Int = 0
        private set
    var cachedNotificationsEnabled: Boolean = true
        private set
    var cachedAppLockEnabled: Boolean = false
        private set

    fun refreshCachedNotificationPrefs() {
        appScope.launch(Dispatchers.IO) {
            val prefs = repository.getNotificationPrefs()
            cachedNotificationHour = prefs.first
            cachedNotificationMinute = prefs.second
            cachedNotificationsEnabled = prefs.third
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        NotificationHelper.createChannel(this)

        appScope.launch(Dispatchers.IO) {
            val prefs = repository.getNotificationPrefs()
            cachedNotificationHour = prefs.first
            cachedNotificationMinute = prefs.second
            cachedNotificationsEnabled = prefs.third
            cachedThemeMode = repository.themeMode().first()
            cachedAppLockEnabled = repository.biometricEnabled().first()
            repository.restoreCurrentAccount()
            val created = repository.checkAndCreateDueSubscriptions()
            if (created > 0) {
                android.util.Log.d("EHTrackerApp", "Auto-created $created subscription expenses")
            }
            repository.scheduleSubscriptionReminders(this@EHTrackerApplication)

            if (cachedNotificationsEnabled) {
                NotificationHelper.scheduleDailyReminder(
                    this@EHTrackerApplication,
                    cachedNotificationHour,
                    cachedNotificationMinute
                )
            }
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }
}
