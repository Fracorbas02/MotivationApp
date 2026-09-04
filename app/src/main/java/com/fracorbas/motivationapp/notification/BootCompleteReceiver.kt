package com.fracorbas.motivationapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * BroadcastReceiver that reschedules all habit alarms after device reboot.
 *
 * This ensures that notifications continue to work even after the device restarts.
 *
 * Note: This receiver doesn't use Hilt injection to avoid complexity with BroadcastReceiver.
 * Instead, it creates its own dependencies when needed.
 */
class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        val database = HabitDatabase.getDatabase(context)
        val habitRepository = HabitRepository(database.habitDao, database.habitCompletionDao, database)
        val alarmScheduler = com.fracorbas.motivationapp.notification.HabitAlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = habitRepository.getAllActiveHabitsWithReminders()
                alarmScheduler.rescheduleAllAlarms(habits)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
