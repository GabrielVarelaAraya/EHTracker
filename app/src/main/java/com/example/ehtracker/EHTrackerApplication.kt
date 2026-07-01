package com.example.ehtracker

import android.app.Application
import androidx.room.Room
import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class EHTrackerApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ehtracker.db"
        )        .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
            .build()
    }

    val repository: TrackerRepository by lazy {
        TrackerRepository(database)
    }

    var cachedThemeMode: String = "system"
        private set
    var cachedNotificationHour: Int = 20
        private set
    var cachedNotificationMinute: Int = 0
        private set
    var cachedNotificationsEnabled: Boolean = true
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        runBlocking(Dispatchers.IO) {
            val prefs = repository.getNotificationPrefs()
            cachedNotificationHour = prefs.first
            cachedNotificationMinute = prefs.second
            cachedNotificationsEnabled = prefs.third
            cachedThemeMode = repository.themeMode().first()
        }

        if (cachedNotificationsEnabled) {
            NotificationHelper.scheduleDailyReminder(this, cachedNotificationHour, cachedNotificationMinute)
        }
    }
}
