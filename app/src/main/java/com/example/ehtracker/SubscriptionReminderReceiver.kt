package com.example.ehtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SubscriptionReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subId = intent.getStringExtra("subscription_id") ?: return
        val subName = intent.getStringExtra("subscription_name") ?: return
        val amount = intent.getDoubleExtra("subscription_amount", 0.0)
        val daysBefore = intent.getIntExtra("days_before", 3)
        val currencySymbol = intent.getStringExtra("currency_symbol") ?: "$"
        val notificationId = subId.hashCode() + (if (daysBefore == 3) 0 else 1)
        NotificationHelper.showSubscriptionReminderNotification(
            context, subName, amount, daysBefore, notificationId, currencySymbol
        )
    }
}