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
import com.fracorbas.motivationapp.data.model.HabitFrequencyUtils
import com.fracorbas.motivationapp.data.model.TimeOfDay
import com.fracorbas.motivationapp.ui.components.HabitRow
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Timeline view: habits grouped by time of day with calm sticky section headers.
 * Only habits that are due today (or already completed today) are shown in the
 * active sections. Habits not due today are grouped in a separate "Plus tard"
 * section at the bottom.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimelineHabitsList(
    habits: List<Habit>,
    onToggleCompletion: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Habit) -> Unit,
    onToggleNotification: (Int, Boolean) -> Unit,
    onHabitClick: ((Int) -> Unit)? = null
) {
    val (dueHabits, notDueHabits) = remember(habits) {
        habits.partition { habit ->
            val isCompletedToday = habit.lastCompletedDate == java.time.LocalDate.now()
            isCompletedToday || HabitFrequencyUtils.isCompletionDayToday(habit)
        }
    }

    val grouped = remember(dueHabits) {
        TimeOfDay.inOrder().associateWith { tod ->
            dueHabits.filter { TimeOfDay.fromTime(it.reminderTime) == tod }
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
                        onToggleNotification = { onToggleNotification(habit.id, it) },
                        onClick = onHabitClick?.let { { it(habit.id) } }
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }

        if (notDueHabits.isNotEmpty()) {
            item {
                Text(
                    text = "Plus tard",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            items(notDueHabits) { habit ->
                HabitRow(
                    habit = habit,
                    leadingTime = habit.reminderTime?.format(timeFmt) ?: "—",
                    onToggleCompletion = { onToggleCompletion(habit.id) },
                    onEditClick = { onEditClick(habit.id) },
                    onDeleteClick = { onDeleteClick(habit) },
                    onToggleNotification = { onToggleNotification(habit.id, it) },
                    onClick = onHabitClick?.let { { it(habit.id) } }
                )
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
