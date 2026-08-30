package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for the statistics screen.
 *
 * Backed by the completion history table for accurate per-day counts,
 * completion percentages and streaks.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val allHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Per-date completion counts for the period, read from the history table.
     * Blocks on a coroutine query — called from a composable; fine for the
     * small data volumes involved here.
     */
    fun getCompletionStatsForPeriod(
        habits: List<Habit>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Int> = runBlocking {
        repository.getCompletionCountsByDate(startDate, endDate)
    }

    fun getWeeklyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val end = LocalDate.now()
        val start = end.minusDays(6)
        return getCompletionStatsForPeriod(habits, start, end)
    }

    fun getMonthlyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        return getCompletionStatsForPeriod(habits, start, today)
    }

    fun getYearlyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val start = today.withDayOfYear(1)
        return getCompletionStatsForPeriod(habits, start, today)
    }

    /**
     * Completion percentage for a habit over [startDate, endDate] based on real
     * completion count vs. the expected number of completions given its frequency.
     */
    fun getHabitCompletionPercentage(
        habit: Habit,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (totalDays <= 0) return 0.0

        val actual = runBlocking { repository.getHabitCompletionCount(habit.id, startDate, endDate) }
        if (actual <= 0) return 0.0

        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"

        val expected = when (unit) {
            "days" -> totalDays / frequency.toDouble()
            "weeks" -> totalDays / (frequency * 7.0)
            "months" -> {
                val months = ChronoUnit.MONTHS.between(startDate, endDate) + 1
                months / frequency.toDouble()
            }
            else -> totalDays.toDouble()
        }

        return if (expected > 0) (actual / expected * 100.0).coerceAtMost(100.0) else 0.0
    }

    /**
     * Real completion count for a habit in [startDate, endDate].
     */
    fun getHabitCompletionCount(habit: Habit, startDate: LocalDate, endDate: LocalDate): Int =
        runBlocking { repository.getHabitCompletionCount(habit.id, startDate, endDate) }

    fun getLongestStreak(habits: List<Habit>): Int =
        habits.maxOfOrNull { it.streak } ?: 0

    fun getStatisticsSummary(habits: List<Habit>): StatisticsSummary {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val monthStart = today.withDayOfMonth(1)
        val yearStart = today.withDayOfYear(1)

        val todayCompletions = runBlocking {
            repository.getHabitCompletionCountAllHabits(habits, today, today)
        }
        val weekCompletions = runBlocking {
            repository.getHabitCompletionCountAllHabits(habits, weekStart, today)
        }
        val monthCompletions = runBlocking {
            repository.getHabitCompletionCountAllHabits(habits, monthStart, today)
        }
        val yearCompletions = runBlocking {
            repository.getHabitCompletionCountAllHabits(habits, yearStart, today)
        }

        return StatisticsSummary(
            todayCompletions = todayCompletions,
            weekCompletions = weekCompletions,
            monthCompletions = monthCompletions,
            yearCompletions = yearCompletions,
            longestStreak = getLongestStreak(habits),
            activeHabits = habits.count { it.isActive }
        )
    }
}

data class StatisticsSummary(
    val todayCompletions: Int,
    val weekCompletions: Int,
    val monthCompletions: Int,
    val yearCompletions: Int,
    val longestStreak: Int,
    val activeHabits: Int
)
