package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.TimeOfDay
import com.fracorbas.motivationapp.ui.components.HabitRow
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Timeline view: habits grouped by time of day with calm sticky section headers.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimelineHabitsList(
    habits: List<Habit>,
    onToggleCompletion: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Habit) -> Unit,
    onToggleNotification: (Int, Boolean) -> Unit
) {
    val grouped = remember(habits) {
        TimeOfDay.inOrder().associateWith { tod ->
            habits.filter { TimeOfDay.fromTime(it.reminderTime) == tod }
                .sortedBy { it.reminderTime }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (timeOfDay, timeHabits) ->
            if (timeHabits.isNotEmpty()) {
                stickyHeader {
                    TimelineHeader(timeOfDay = timeOfDay)
                }
                items(timeHabits) { habit ->
                    HabitRow(
                        habit = habit,
                        leadingTime = habit.reminderTime?.format(timeFmt) ?: "—",
                        onToggleCompletion = { onToggleCompletion(habit.id) },
                        onEditClick = { onEditClick(habit.id) },
                        onDeleteClick = { onDeleteClick(habit) },
                        onToggleNotification = { onToggleNotification(habit.id, it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun TimelineHeader(timeOfDay: TimeOfDay) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = timeOfDay.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${timeOfDay.startHour}h – ${timeOfDay.endHour}h",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
