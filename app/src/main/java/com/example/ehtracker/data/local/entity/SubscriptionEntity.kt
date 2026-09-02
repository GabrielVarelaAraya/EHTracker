package com.example.ehtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val billingDay: Int,
    val notes: String = "",
    val isActive: Boolean = true,
    val type: String = "expense",
    val accountId: String? = null,
    val lastCreatedMonth: String? = null
)
