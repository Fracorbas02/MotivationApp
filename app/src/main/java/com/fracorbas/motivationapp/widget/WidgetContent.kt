package com.fracorbas.motivationapp.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
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
import androidx.glance.unit.ColorProvider
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
    today: LocalDate,
    colors: WidgetColors
) {
    val todayLabel = today.format(
        DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    ).replaceFirstChar { it.titlecase() }

    Column(
        modifier = GlanceModifier
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = todayLabel,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                color = ColorProvider(colors.onSurfaceVariant)
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "$completed / $total",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp),
                color = ColorProvider(colors.primary)
            )
        )
        Text(
            text = "habitudes completees",
            style = TextStyle(
                color = ColorProvider(colors.onSurfaceVariant)
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
                    text = if (isDone) "OK" else "--",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(if (isDone) colors.success else colors.onSurfaceVariant)
                    ),
                    modifier = GlanceModifier.padding(end = 8.dp)
                )
                Text(
                    text = habit.title.take(24),
                    style = TextStyle(
                        color = ColorProvider(if (isDone) colors.onSurfaceVariant else colors.onSurface)
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
                color = ColorProvider(colors.onSurfaceVariant)
            ),
            modifier = GlanceModifier.padding(top = 4.dp)
        )
    }
}
