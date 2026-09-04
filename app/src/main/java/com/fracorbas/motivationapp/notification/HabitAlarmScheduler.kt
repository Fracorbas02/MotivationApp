package com.fracorbas.motivationapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules exact alarms for habit reminders using AlarmManager.
 * 
 * This provides precise notifications at the exact time specified by the user,
 * even when the app is closed or the device has rebooted (with BootCompleteReceiver).
 */
@Singleton
class HabitAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule a reminder for a specific habit.
     * The alarm will trigger at the habit's reminder time with optional frequency.
     * 
     * @param habitId The ID of the habit
     * @param title The habit title for the notification
     * @param trigger The habit trigger text
     * @param reminderTime The exact time to trigger the notification
     * @param frequency Optional frequency (e.g., 2 for every 2 days)
     * @param frequencyUnit Optional frequency unit (e.g., "days", "weeks")
     */
    fun scheduleHabitReminder(
        habitId: Int,
        title: String,
        trigger: String,
        reminderTime: LocalTime,
        frequency: Int? = null,
        frequencyUnit: String? = null,
        targetDayOfWeek: Int? = null,
        targetDayOfMonth: Int? = null
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminderTime.hour)
            set(Calendar.MINUTE, reminderTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val triggerTime = calculateNextTriggerTime(calendar, frequency, frequencyUnit, targetDayOfWeek, targetDayOfMonth)

        createAlarm(habitId, title, trigger, triggerTime, frequency, frequencyUnit, reminderTime)
    }

    /**
     * Calculate the next trigger time based on frequency settings and optional target day.
     */
    private fun calculateNextTriggerTime(
        calendar: Calendar,
        frequency: Int?,
        frequencyUnit: String?,
        targetDayOfWeek: Int? = null,
        targetDayOfMonth: Int? = null
    ): Long {
        val now = System.currentTimeMillis()

        // If target day is set, adjust the calendar to the next matching day
        if (targetDayOfWeek != null && frequencyUnit == "weeks") {
            val javaCalDayOfWeek = when (targetDayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            val diff = (javaCalDayOfWeek - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7
            calendar.add(Calendar.DAY_OF_MONTH, diff)
        } else if (targetDayOfMonth != null && frequencyUnit == "months") {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            // Treat 31 (or any value exceeding month length) as last day of month
            val day = if (targetDayOfMonth >= maxDay) maxDay else targetDayOfMonth
            if (calendar.get(Calendar.DAY_OF_MONTH) > day) {
                calendar.add(Calendar.MONTH, 1)
            }
            // Re-read maxDay in case we moved to a new month
            val maxDayAdjusted = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val finalDay = if (targetDayOfMonth >= maxDayAdjusted) maxDayAdjusted else targetDayOfMonth
            calendar.set(Calendar.DAY_OF_MONTH, finalDay)
        }

        if (calendar.timeInMillis > now) {
            return calendar.timeInMillis
        }

        // Time has passed, schedule for next occurrence
        val freq = frequency ?: 1
        when (frequencyUnit) {
            "weeks" -> calendar.add(Calendar.WEEK_OF_YEAR, freq)
            "months" -> calendar.add(Calendar.MONTH, freq)
            else -> calendar.add(Calendar.DAY_OF_YEAR, freq)
        }

        return calendar.timeInMillis
    }

    /**
     * Cancel the reminder for a specific habit.
     * 
     * @param habitId The ID of the habit
     */
    fun cancelHabitReminder(habitId: Int) {
        val intent = createAlarmIntent(habitId, "", "")
        val pendingIntent = getPendingIntent(habitId, intent)
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    /**
     * Reschedule all alarms for the given habits.
     * Used after device reboot or when restoring habits.
     * 
     * @param habits List of habits with reminder times
     */
    fun rescheduleAllAlarms(habits: List<com.fracorbas.motivationapp.data.model.Habit>) {
        habits.filter { it.notificationEnabled && it.reminderTime != null }
            .forEach { habit ->
                scheduleHabitReminder(
                    habitId = habit.id,
                    title = habit.title,
                    trigger = habit.trigger,
                    reminderTime = habit.reminderTime!!,
                    frequency = habit.notificationFrequency,
                    frequencyUnit = habit.notificationFrequencyUnit,
                    targetDayOfWeek = habit.targetDayOfWeek,
                    targetDayOfMonth = habit.targetDayOfMonth
                )
            }
    }

    /**
     * Update an existing habit reminder with new time.
     * Cancels the old alarm and creates a new one.
     */
    fun updateHabitReminder(
        habitId: Int,
        title: String,
        trigger: String,
        oldReminderTime: LocalTime?,
        newReminderTime: LocalTime
    ) {
        // Cancel old alarm if it exists
        oldReminderTime?.let { cancelHabitReminder(habitId) }
        
        // Schedule new alarm
        scheduleHabitReminder(habitId, title, trigger, newReminderTime)
    }

    /**
     * Check if an alarm is already scheduled for a habit.
     */
    fun isAlarmScheduled(habitId: Int): Boolean {
        val intent = createAlarmIntent(habitId, "", "")
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        return PendingIntent.getBroadcast(context, habitId, intent, flags) != null
    }

    // ========== Private helpers ==========

    private fun createAlarmIntent(
        habitId: Int,
        title: String,
        trigger: String,
        frequency: Int? = null,
        frequencyUnit: String? = null,
        reminderTime: LocalTime? = null
    ): Intent {
        return Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TITLE, title)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TRIGGER, trigger)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_FREQUENCY, frequency)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_FREQUENCY_UNIT, frequencyUnit)
            if (reminderTime != null) {
                putExtra(HabitReminderReceiver.EXTRA_REMINDER_HOUR, reminderTime.hour)
                putExtra(HabitReminderReceiver.EXTRA_REMINDER_MINUTE, reminderTime.minute)
            }
        }
    }

    private fun getPendingIntent(
        habitId: Int,
        intent: Intent
    ): PendingIntent? {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return try {
            PendingIntent.getBroadcast(context, habitId, intent, flags)
        } catch (e: Exception) {
            null
        }
    }

    private fun createAlarm(
        habitId: Int,
        title: String,
        trigger: String,
        triggerTime: Long,
        frequency: Int? = null,
        frequencyUnit: String? = null,
        reminderTime: LocalTime? = null
    ) {
        val intent = createAlarmIntent(habitId, title, trigger, frequency, frequencyUnit, reminderTime)
        val pendingIntent = getPendingIntent(habitId, intent) ?: return
        
        // Use setExactAndAllowWhileIdle for precise alarms that work even in Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            // Fallback for older versions
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
