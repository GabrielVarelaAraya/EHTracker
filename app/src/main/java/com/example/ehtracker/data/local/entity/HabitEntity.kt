package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val targetDaysPerWeek: Int = 7,
    val isNumeric: Boolean = false,
    val unit: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
