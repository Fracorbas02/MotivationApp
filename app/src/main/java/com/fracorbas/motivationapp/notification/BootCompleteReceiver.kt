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
        // Only handle boot completed and package replaced events
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        // Create dependencies manually (without Hilt)
        val database = HabitDatabase.getDatabase(context)
        val habitRepository = HabitRepository(database.habitDao)
        val alarmScheduler = com.fracorbas.motivationapp.notification.HabitAlarmScheduler(context)

        // Use a coroutine scope to launch async work
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get all active habits with notifications enabled
                val habits = habitRepository.getAllActiveHabitsWithReminders()
                
                // Reschedule all alarms
                alarmScheduler.rescheduleAllAlarms(habits)
            } catch (e: Exception) {
                // Log error - in production you might want to use proper logging
                e.printStackTrace()
            }
        }
    }
}
