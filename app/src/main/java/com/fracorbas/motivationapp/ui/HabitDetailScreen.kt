package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitFrequencyUtils
import com.fracorbas.motivationapp.ui.components.AppTopBar
import com.fracorbas.motivationapp.ui.components.EmptyState
import com.fracorbas.motivationapp.ui.components.SectionLabel
import com.fracorbas.motivationapp.ui.components.StreakPill
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.ui.theme.successColor
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Int,
    onBack: () -> Unit,
    onEditHabit: (Int) -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    var habit by remember { mutableStateOf<Habit?>(null) }
    var completions by remember { mutableStateOf<List<LocalDate>>(emptyList()) }
    var month by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(habitId) {
        habit = viewModel.getHabitById(habitId)
        completions = viewModel.getCompletionsForHabit(habitId)
    }

    Scaffold(
        topBar = {
            AppTopBar("Détail de l'habitude", onBack, actions = {
                IconButton(onClick = { onEditHabit(habitId) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier")
                }
            })
        }
    ) { padding ->
        val h = habit
        if (h == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chargement…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        val completedSet = completions.toSet()
        val totalCompletions = completions.size
        val today = LocalDate.now()
        val isCompletedToday = h.lastCompletedDate == today

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Header card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = h.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (h.streak > 0) StreakPill(streak = h.streak)
                    }
                    if (h.description != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = h.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Key figures
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FigureTile(
                        value = h.streak.toString(),
                        label = "Série actuelle",
                        icon = Icons.Default.LocalFireDepartment,
                        accent = successColor(),
                        modifier = Modifier.weight(1f)
                    )
                    FigureTile(
                        value = totalCompletions.toString(),
                        label = "Complétions",
                        icon = Icons.Default.Check,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FigureTile(
                        value = if (isCompletedToday) "Oui" else "Non",
                        label = "Aujourd'hui",
                        icon = Icons.Default.CalendarMonth,
                        accent = if (isCompletedToday) successColor() else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Attributes
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Détails")
                    AttributeRow(Icons.Default.Schedule, "Déclencheur", h.trigger)
                    h.reminderTime?.let {
                        AttributeRow(
                            Icons.Default.Notifications,
                            "Rappel",
                            it.format(DateTimeFormatter.ofPattern("HH:mm"))
                        )
                    }
                    val freq = h.notificationFrequency?.let { f ->
                        "${h.notificationFrequencyUnit ?: "days"} × $f"
                    } ?: "Quotidienne"
                    AttributeRow(Icons.Default.CalendarMonth, "Fréquence", freq)
                }
            }

            // Calendar
            item {
                SectionLabel("Calendrier de complétion")
                Spacer(Modifier.height(10.dp))
                CompletionCalendar(
                    month = month,
                    completions = completedSet,
                    onPreviousMonth = { month = month.minusMonths(1) },
                    onNextMonth = { if (month.isBefore(YearMonth.now())) month = month.plusMonths(1) }
                )
            }

            // Hint
            item {
                if (!HabitFrequencyUtils.isCompletionDayToday(h) && !isCompletedToday) {
                    Text(
                        text = "Cette habitude n'est pas à compléter aujourd'hui selon sa fréquence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Button(
                    onClick = { onEditHabit(habitId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Modifier l'habitude")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FigureTile(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = accent)
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent.copy(alpha = 0.75f))
    }
}

@Composable
private fun AttributeRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CompletionCalendar(
    month: YearMonth,
    completions: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val today = LocalDate.now()
    val firstOfMonth = month.atDay(1)
    // Monday-first layout
    val leadingDays = (firstOfMonth.dayOfWeek.value - 1)
    val daysInMonth = month.lengthOfMonth()

    Column {
        // Month navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPreviousMonth) {
                Text("‹", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
                    .replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNextMonth, enabled = month.isBefore(YearMonth.now())) {
                Text("›", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(8.dp))

        // Weekday header
        val weekdays = DayOfWeek.values().toList()
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.NARROW, Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Day grid
        val totalSlots = leadingDays + daysInMonth
        val rows = (totalSlots + 6) / 7
        val success = successColor()
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    val dayNumber = index - leadingDays + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = month.atDay(dayNumber)
                            val completed = date in completions
                            val isFuture = date.isAfter(today)
                            val isToday = date == today
                            val container = when {
                                completed -> success
                                isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            }
                            val content = if (completed) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(container)
                                    .then(
                                        if (isToday && !completed) Modifier
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = content
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitDetailScreenPreview() {
    MotivationAppTheme {
        HabitDetailScreen(habitId = 0, onBack = {}, onEditHabit = {})
    }
}
