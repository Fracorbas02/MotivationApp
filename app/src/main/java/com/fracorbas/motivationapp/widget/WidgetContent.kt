package com.fracorbas.motivationapp.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fracorbas.motivationapp.MainActivity
import com.fracorbas.motivationapp.data.model.Habit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WidgetContent(
    completed: Int,
    total: Int,
    habits: List<Habit>,
    today: LocalDate
) {
    val todayLabel = today.format(
        DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    ).replaceFirstChar { it.titlecase() }

    Column(
        modifier = GlanceModifier
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = todayLabel,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "$completed / $total",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp),
                color = GlanceTheme.colors.primary
            )
        )
        Text(
            text = "habitudes complétées",
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        habits.forEach { habit ->
            val isDone = habit.lastCompletedDate == today
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDone) "✓" else "○",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) GlanceTheme.colors.primary
                        else GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.padding(end = 8.dp)
                )
                Text(
                    text = habit.title.take(24),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = "Tap pour ouvrir",
            style = TextStyle(
                fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier.padding(top = 4.dp)
        )
    }
}
