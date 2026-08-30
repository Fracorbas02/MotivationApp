package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.ui.components.AppTopBar
import com.fracorbas.motivationapp.ui.components.EmptyState
import com.fracorbas.motivationapp.ui.components.SectionLabel
import com.fracorbas.motivationapp.ui.components.StatTile
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.ui.theme.successColor
import com.fracorbas.motivationapp.viewmodel.StatisticsSummary
import com.fracorbas.motivationapp.viewmodel.StatisticsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class StatisticsPeriod { WEEKLY, MONTHLY, YEARLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val habits by viewModel.allHabits.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatisticsPeriod.WEEKLY) }

    val today = LocalDate.now()
    val stats = when (selectedPeriod) {
        StatisticsPeriod.WEEKLY -> viewModel.getWeeklyStats(habits)
        StatisticsPeriod.MONTHLY -> viewModel.getMonthlyStats(habits)
        StatisticsPeriod.YEARLY -> viewModel.getYearlyStats(habits)
    }
    val summary = viewModel.getStatisticsSummary(habits)

    Scaffold(
        topBar = { AppTopBar("Statistiques", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { SummaryGrid(summary = summary) }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedPeriod == StatisticsPeriod.WEEKLY,
                        onClick = { selectedPeriod = StatisticsPeriod.WEEKLY },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Semaine") }
                    SegmentedButton(
                        selected = selectedPeriod == StatisticsPeriod.MONTHLY,
                        onClick = { selectedPeriod = StatisticsPeriod.MONTHLY },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Mois") }
                    SegmentedButton(
                        selected = selectedPeriod == StatisticsPeriod.YEARLY,
                        onClick = { selectedPeriod = StatisticsPeriod.YEARLY },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Année") }
                }
            }

            item {
                BarChart(
                    stats = stats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionLabel("Vos habitudes")
                Spacer(Modifier.height(8.dp))
            }

            if (habits.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Check,
                        title = "Aucune habitude",
                        hint = "Ajoutez des habitudes pour voir vos statistiques",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                val startDate = when (selectedPeriod) {
                    StatisticsPeriod.WEEKLY -> today.minusDays(6)
                    StatisticsPeriod.MONTHLY -> today.withDayOfMonth(1)
                    StatisticsPeriod.YEARLY -> today.withDayOfYear(1)
                }
                items(habits) { habit ->
                    val percentage = viewModel.getHabitCompletionPercentage(habit, startDate, today)
                    HabitStatCard(habit = habit, percentage = percentage, startDate = startDate, endDate = today)
                }
            }
        }
    }
}

@Composable
private fun SummaryGrid(summary: StatisticsSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(summary.todayCompletions, "Aujourd'hui", Icons.Default.Check, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatTile(summary.weekCompletions, "Semaine", Icons.Default.Check, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            StatTile(summary.monthCompletions, "Mois", Icons.Default.Check, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(summary.yearCompletions, "Année", Icons.Default.Check, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatTile(summary.longestStreak, "Meilleure série", Icons.Default.LocalFireDepartment, successColor(), Modifier.weight(1f))
            StatTile(summary.activeHabits, "Actives", Icons.Default.Check, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BarChart(stats: Map<LocalDate, Int>, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofPattern("dd")
    val maxValue = (stats.values.maxOrNull() ?: 1).coerceAtLeast(1)
    val dates = stats.keys.toList()
    val labelStep = if (dates.size > 8) (dates.size / 6).coerceAtLeast(1) else 1

    Column(modifier = modifier) {
        SectionLabel("Complétions sur la période")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            dates.forEach { date ->
                val fraction = (stats[date] ?: 0).toFloat() / maxValue
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((fraction.coerceAtLeast(0.04f) * 120).toInt().dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dates.forEachIndexed { index, date ->
                Text(
                    text = if (index % labelStep == 0) date.format(formatter) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HabitStatCard(
    habit: Habit,
    percentage: Double,
    startDate: LocalDate,
    endDate: LocalDate
) {
    val daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1
    val completions = if (habit.lastCompletedDate != null &&
        !habit.lastCompletedDate.isBefore(startDate) &&
        !habit.lastCompletedDate.isAfter(endDate)) 1 else 0

    val progressColor = when {
        percentage >= 80 -> successColor()
        percentage >= 50 -> MaterialTheme.colorScheme.secondary
        percentage >= 20 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$completions / $daysInPeriod",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((percentage / 100.0).toFloat().coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressColor)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${String.format("%.0f", percentage)} %",
                style = MaterialTheme.typography.labelSmall,
                color = progressColor,
                modifier = Modifier.align(Alignment.End),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenPreview() {
    MotivationAppTheme {
        StatisticsScreen(onBack = {})
    }
}
