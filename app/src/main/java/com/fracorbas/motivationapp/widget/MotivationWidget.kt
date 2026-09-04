package com.fracorbas.motivationapp.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.repository.SettingsRepository
import com.fracorbas.motivationapp.data.repository.ThemeMode
import com.fracorbas.motivationapp.ui.theme.CalmOnSurface
import com.fracorbas.motivationapp.ui.theme.CalmOnSurfaceDark
import com.fracorbas.motivationapp.ui.theme.CalmOnSurfaceVariant
import com.fracorbas.motivationapp.ui.theme.CalmOnSurfaceVariantDark
import com.fracorbas.motivationapp.ui.theme.CalmPrimary
import com.fracorbas.motivationapp.ui.theme.CalmPrimaryDark
import com.fracorbas.motivationapp.ui.theme.CalmSurface
import com.fracorbas.motivationapp.ui.theme.CalmSurfaceDark
import com.fracorbas.motivationapp.ui.theme.Success
import com.fracorbas.motivationapp.ui.theme.SuccessDark
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class MotivationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = HabitDatabase.getDatabase(context)
        val settingsRepo = SettingsRepository(context)
        val settings = settingsRepo.settings.first()
        val isDark = when (settings.themeMode) {
            ThemeMode.SYSTEM -> android.content.res.Configuration.UI_MODE_NIGHT_YES ==
                (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK)
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }

        val today = LocalDate.now()
        val habits = database.habitDao.getAllHabitsList()
        val activeHabits = habits.filter { it.isActive }
        val completedToday = activeHabits.count { it.lastCompletedDate == today }

        val colors = if (isDark) WidgetColors.dark() else WidgetColors.light()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surface)
                    .cornerRadius(20.dp)
                    .padding(12.dp)
            ) {
                WidgetContent(
                    completed = completedToday,
                    total = activeHabits.size,
                    habits = activeHabits.take(5),
                    today = today,
                    colors = colors
                )
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "widget_habit_id"
    }
}

data class WidgetColors(
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val success: Color
) {
    companion object {
        fun light() = WidgetColors(
            surface = CalmSurface,
            onSurface = CalmOnSurface,
            onSurfaceVariant = CalmOnSurfaceVariant,
            primary = CalmPrimary,
            success = Success
        )
        fun dark() = WidgetColors(
            surface = CalmSurfaceDark,
            onSurface = CalmOnSurfaceDark,
            onSurfaceVariant = CalmOnSurfaceVariantDark,
            primary = CalmPrimaryDark,
            success = SuccessDark
        )
    }
}

class MotivationWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MotivationWidget()
}
