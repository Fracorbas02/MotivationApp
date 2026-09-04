package com.fracorbas.motivationapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fracorbas.motivationapp.MainActivity
import com.fracorbas.motivationapp.MotivationApp
import com.fracorbas.motivationapp.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * BroadcastReceiver for habit reminder notifications.
 * 
 * This receiver is triggered by alarms set for each habit's reminder time.
 * It displays the notification to the user.
 */
@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1)
        val title = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Habitude"
        val trigger = intent.getStringExtra(EXTRA_HABIT_TRIGGER) ?: ""
        val frequency = intent.getIntExtra(EXTRA_HABIT_FREQUENCY, 1)
        val frequencyUnit = intent.getStringExtra(EXTRA_HABIT_FREQUENCY_UNIT) ?: "days"
        val reminderHour = intent.getIntExtra(EXTRA_REMINDER_HOUR, -1)
        val reminderMinute = intent.getIntExtra(EXTRA_REMINDER_MINUTE, -1)

        if (habitId == -1) return

        showNotification(context, habitId, title, trigger)

        // Always reschedule for the next occurrence
        rescheduleAlarm(context, habitId, title, trigger, frequency, frequencyUnit, reminderHour, reminderMinute)
    }

    private fun rescheduleAlarm(
        context: Context,
        habitId: Int,
        title: String,
        trigger: String,
        frequency: Int,
        frequencyUnit: String,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val scheduler = HabitAlarmScheduler(context)
        val reminderTime = if (reminderHour >= 0 && reminderMinute >= 0) {
            java.time.LocalTime.of(reminderHour, reminderMinute)
        } else {
            java.time.LocalTime.now()
        }
        scheduler.scheduleHabitReminder(
            habitId = habitId,
            title = title,
            trigger = trigger,
            reminderTime = reminderTime,
            frequency = frequency,
            frequencyUnit = frequencyUnit
        )
    }

    /**
     * Show notification for a habit reminder
     */
    private fun showNotification(
        context: Context,
        habitId: Int,
        title: String,
        trigger: String
    ) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        
        // Create notification channel (required for Android 8.0+)
        createNotificationChannel(context, notificationManager)
        
        // Create pending intent for clicking the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, MotivationApp.CHANNEL_HABIT_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ Rappel: $title")
            .setContentText("As-tu fait \"$title\" après \"$trigger\" ?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .build()
        
        // Show notification with unique ID per habit
        notificationManager.notify(
            MotivationApp.NOTIFICATION_ID_HABIT_REMINDER + habitId,
            notification
        )
    }

    /**
     * Create notification channel if it doesn't exist
     */
    private fun createNotificationChannel(
        context: Context,
        notificationManager: NotificationManager
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MotivationApp.CHANNEL_HABIT_REMINDER,
                "Rappels d'habitudes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les rappels d'habitudes"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_HABIT_TRIGGER = "extra_habit_trigger"
        const val EXTRA_HABIT_FREQUENCY = "extra_habit_frequency"
        const val EXTRA_HABIT_FREQUENCY_UNIT = "extra_habit_frequency_unit"
        const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
    }
}
