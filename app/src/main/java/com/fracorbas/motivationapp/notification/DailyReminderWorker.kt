package com.fracorbas.motivationapp.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fracorbas.motivationapp.MainActivity
import com.fracorbas.motivationapp.MotivationApp
import com.fracorbas.motivationapp.R
import com.fracorbas.motivationapp.data.model.HabitFrequencyUtils
import com.fracorbas.motivationapp.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * Daily end-of-day worker. If the user has active habits that are due today
 * but not completed, posts a single summary notification listing them.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: HabitRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val habits = repository.getAllHabitsList()
        val pending = habits.filter { habit ->
            habit.isActive &&
                habit.lastCompletedDate != today &&
                HabitFrequencyUtils.isCompletionDayToday(habit)
        }

        if (pending.isEmpty()) return Result.success()

        val titles = pending.joinToString("\n") { "• ${it.title}" }
        val count = pending.size
        showSummaryNotification(applicationContext, count, titles)
        return Result.success()
    }

    private fun showSummaryNotification(context: Context, count: Int, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, MotivationApp.CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Il reste $count habitude${if (count > 1) "s" else ""} à compléter")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(MotivationApp.NOTIFICATION_ID_DAILY_SUMMARY, notification)
    }
}
