package com.fracorbas.motivationapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fracorbas.motivationapp.data.repository.SettingsRepository
import com.fracorbas.motivationapp.notification.DailyReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Custom Application class for MotivationApp.
 *
 * This class is used for:
 * - Initializing Hilt (dependency injection)
 * - Creating notification channels
 * - Configuring WorkManager
 * - Scheduling the daily end-of-day reminder based on settings
 */
@HiltAndroidApp
class MotivationApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDailyReminderFromSettings()
    }

    /**
     * Read the initial daily-reminder setting and schedule (or cancel) the worker.
     */
    private fun scheduleDailyReminderFromSettings() {
        appScope.launch {
            val enabled = settingsRepository.settings.first().dailyReminderEnabled
            DailyReminderScheduler.schedule(this@MotivationApp, enabled)
        }
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
                setShowBadge(true)
            }

            val dailySummaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "Rappel de fin de journée",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Récapitulatif du soir des habitudes non complétées"
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            notificationManager.createNotificationChannel(habitReminderChannel)
            notificationManager.createNotificationChannel(dailySummaryChannel)
        }
    }

    companion object {
        const val CHANNEL_HABIT_REMINDER = "habit_reminder_channel"
        const val CHANNEL_DAILY_SUMMARY = "daily_summary_channel"
        const val NOTIFICATION_ID_HABIT_REMINDER = 1001
        const val NOTIFICATION_ID_DAILY_SUMMARY = 2001
    }
}
