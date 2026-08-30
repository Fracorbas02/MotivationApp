package com.fracorbas.motivationapp.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily end-of-day reminder worker to run once a day near 21:00.
 *
 * Uses an existing periodic work policy so the schedule is updated (not duplicated)
 * when the user toggles the setting.
 */
object DailyReminderScheduler {

    private const val WORK_NAME = "daily_reminder_work"

    /**
     * Schedule the daily reminder if [enabled], otherwise cancel any existing work.
     */
    fun schedule(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        // Compute the delay until the next 21:00 (today if before, tomorrow otherwise).
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelayMinutes = ((target.timeInMillis - now.timeInMillis) / 60_000L)
            .coerceAtLeast(1L)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
