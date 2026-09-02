package com.example.ehtracker

import android.app.AlarmManager
import android.app.NotificationChannel
import com.example.ehtracker.util.formatMoney
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ehtracker.data.local.AppDatabase
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "habit_reminders"
    const val SUB_CHANNEL_ID = "subscription_reminders"
    private const val NOTIFICATION_BASE_ID = 1000
    private const val SUB_NOTIFICATION_BASE_ID = 2000
    private const val REQ_BASE = 10000

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val habitChannel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders to complete your daily habits" }
        manager.createNotificationChannel(habitChannel)

        val subChannel = NotificationChannel(
            SUB_CHANNEL_ID,
            "Subscription Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders for upcoming subscription billing dates" }
        manager.createNotificationChannel(subChannel)
    }

    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showReminderNotification(context: Context, incompleteCount: Int = -1) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val title = "Habit Reminder"
        val text = if (incompleteCount < 0) {
            "Don't forget to complete your habits today!"
        } else if (incompleteCount == 0) {
            "All habits completed today! Great job!"
        } else {
            "$incompleteCount habit${if (incompleteCount != 1) "s" else ""} still incomplete today"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_BASE_ID, notification)
    }

    // --- Subscription Reminders ---

    fun scheduleSubscriptionReminder(
        context: Context,
        subscriptionId: String,
        subscriptionName: String,
        amount: Double,
        reminderDate: LocalDate,
        daysBefore: Int,
        hour: Int = 9,
        minute: Int = 0,
        currencySymbol: String = "$"
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, SubscriptionReminderReceiver::class.java).apply {
            putExtra("subscription_id", subscriptionId)
            putExtra("subscription_name", subscriptionName)
            putExtra("subscription_amount", amount)
            putExtra("days_before", daysBefore)
            putExtra("currency_symbol", currencySymbol)
        }
        val requestCode = subscriptionId.hashCode() * 2 + (if (daysBefore == 3) 0 else 1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminderDate.atTime(LocalTime.of(hour, minute))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerTime > System.currentTimeMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelSubscriptionReminder(context: Context, subscriptionId: String, daysBefore: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, SubscriptionReminderReceiver::class.java)
        val requestCode = subscriptionId.hashCode() * 2 + (if (daysBefore == 3) 0 else 1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllSubscriptionReminders(context: Context, subscriptionId: String) {
        cancelSubscriptionReminder(context, subscriptionId, 3)
        cancelSubscriptionReminder(context, subscriptionId, 1)
    }

    fun showSubscriptionReminderNotification(
        context: Context,
        subscriptionName: String,
        amount: Double,
        daysBefore: Int,
        notificationId: Int,
        currencySymbol: String = "$"
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val dayLabel = when (daysBefore) {
            3 -> "3 days"
            1 -> "tomorrow"
            else -> "in $daysBefore days"
        }
        val title = "Upcoming Subscription"
        val text = "$subscriptionName — $currencySymbol${formatMoney(amount)} is due $dayLabel"
        val notification = NotificationCompat.Builder(context, SUB_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }
}

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as EHTrackerApplication
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val count = try {
                    app.repository.incompleteHabitsTodayCount()
                } catch (_: Exception) { -1 }
                NotificationHelper.showReminderNotification(context, count)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
