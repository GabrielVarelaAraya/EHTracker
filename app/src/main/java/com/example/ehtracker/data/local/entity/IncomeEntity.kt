package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "incomes",
    indices = [Index(value = ["date"])]
)
data class IncomeEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val note: String,
    val date: Long
)
