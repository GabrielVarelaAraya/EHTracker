package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_add")
data class QuickAddEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val notes: String = "",
    val type: String = "expense",
    val accountId: String? = null
)
