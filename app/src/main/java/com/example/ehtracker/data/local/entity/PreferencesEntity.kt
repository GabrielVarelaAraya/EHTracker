package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "system",
    val notificationHour: Int = 20,
    val notificationMinute: Int = 0,
    val notificationsEnabled: Boolean = true
)
