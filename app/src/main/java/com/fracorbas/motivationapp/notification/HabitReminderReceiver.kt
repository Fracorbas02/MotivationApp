package com.fracorbas.motivationapp.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fracorbas.motivationapp.MainActivity
import com.fracorbas.motivationapp.MotivationApp
import com.fracorbas.motivationapp.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver for habit reminder notifications.
 * 
 * This receiver is triggered by alarms set for each habit's reminder time.
 */
@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1)
        val title = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Habitude"
        val trigger = intent.getStringExtra(EXTRA_HABIT_TRIGGER) ?: ""
        
        if (habitId == -1) return
        
        showNotification(context, habitId, title, trigger)
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
            .setContentTitle("Rappel: $title")
            .setContentText("As-tu fait \"$title\" après \"$trigger\" ?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()
        
        // Show notification
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
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for habit reminders"
                enableVibration = true
                enableLights = true
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule a reminder for a habit
     */
    fun scheduleReminder(
        context: Context,
        habitId: Int,
        title: String,
        trigger: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_TITLE, title)
            putExtra(EXTRA_HABIT_TRIGGER, trigger)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set the alarm
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // Use exact alarm (requires special permission on Android 12+)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Cancel a reminder for a habit
     */
    fun cancelReminder(context: Context, habitId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_HABIT_TRIGGER = "extra_habit_trigger"
    }
}
