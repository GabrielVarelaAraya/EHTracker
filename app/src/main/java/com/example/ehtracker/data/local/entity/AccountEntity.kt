package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val initialBalance: Double,
    val currencyCode: String,
    val createdAt: Long
)
