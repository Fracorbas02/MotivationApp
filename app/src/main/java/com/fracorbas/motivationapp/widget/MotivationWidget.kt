package com.fracorbas.motivationapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.repository.HabitRepository
import java.time.LocalDate

/**
 * Home-screen widget showing today's habit progress and allowing
 * quick toggle of completion without opening the app.
 */
class MotivationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = HabitDatabase.getDatabase(context)

        val today = LocalDate.now()
        val habits = database.habitDao.getAllHabitsList()
        val activeHabits = habits.filter { it.isActive }
        val completedToday = activeHabits.count { it.lastCompletedDate == today }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    completed = completedToday,
                    total = activeHabits.size,
                    habits = activeHabits.take(5),
                    today = today
                )
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "widget_habit_id"
    }
}

class MotivationWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MotivationWidget()
}
