package com.example.ehtracker

import android.app.Application
import androidx.room.Room
import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.repository.TrackerRepository

class EHTrackerApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ehtracker.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    val repository: TrackerRepository by lazy {
        TrackerRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
