package com.fracorbas.motivationapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom Application class for MotivationApp.
 * 
 * This class is used for:
 * - Initializing Hilt (dependency injection)
 * - Creating notification channels
 * - Configuring WorkManager
 */
@HiltAndroidApp
class MotivationApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() = 
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val habitReminderChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDER,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for habit reminders"
                enableVibration = true
                enableLights = true
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(habitReminderChannel)
        }
    }

    companion object {
        const val CHANNEL_HABIT_REMINDER = "habit_reminder_channel"
        const val NOTIFICATION_ID_HABIT_REMINDER = 1001
    }
}
