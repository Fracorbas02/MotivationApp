package com.fracorbas.motivationapp.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fracorbas.motivationapp.MainActivity
import com.fracorbas.motivationapp.MotivationApp
import java.time.LocalTime

/**
 * Helper class for managing habit notifications.
 */
object NotificationHelper {

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedule a notification for a habit
     */
    fun scheduleHabitNotification(
        context: Context,
        habitId: Int,
        title: String,
        trigger: String,
        reminderTime: LocalTime
    ) {
        val receiver = HabitReminderReceiver()
        receiver.scheduleReminder(
            context = context,
            habitId = habitId,
            title = title,
            trigger = trigger,
            hour = reminderTime.hour,
            minute = reminderTime.minute
        )
    }

    /**
     * Cancel a scheduled notification for a habit
     */
    fun cancelHabitNotification(
        context: Context,
        habitId: Int
    ) {
        val receiver = HabitReminderReceiver()
        receiver.cancelReminder(context, habitId)
    }

    /**
     * Show a simple notification
     */
    fun showSimpleNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MotivationApp.CHANNEL_HABIT_REMINDER,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, MotivationApp.CHANNEL_HABIT_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
