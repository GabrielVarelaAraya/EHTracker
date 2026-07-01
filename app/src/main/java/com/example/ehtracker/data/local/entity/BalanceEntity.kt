package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance")
data class BalanceEntity(
    @PrimaryKey val id: Int = 1,
    val amount: Double = 0.0
)
