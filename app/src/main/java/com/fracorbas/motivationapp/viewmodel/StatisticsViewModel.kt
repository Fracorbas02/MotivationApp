package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import com.fracorbas.motivationapp.data.model.StreakUtils
import com.fracorbas.motivationapp.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class StatisticsPeriod { WEEKLY, MONTHLY, YEARLY }

/**
 * Statistics computed from the completion history, exposed reactively.
 *
 * All derived state is built from [HabitRepository.getAllHabits] combined with
 * [HabitRepository.observeAllCompletions], so the UI never blocks on a query.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val allHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val completions: StateFlow<List<HabitCompletion>> = repository.observeAllCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeriod = MutableStateFlow(StatisticsPeriod.WEEKLY)
    val selectedPeriod: StateFlow<StatisticsPeriod> = _selectedPeriod.asStateFlow()

    fun setPeriod(period: StatisticsPeriod) { _selectedPeriod.value = period }

    /** Summary figures (today / week / month / year / longest streak / active). */
    val summary: StateFlow<StatisticsSummary> = combine(allHabits, completions) { habits, comps ->
        computeSummary(habits, comps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsSummary(0, 0, 0, 0, 0, 0))

    /** Per-date completion counts for the selected period (zeros filled). */
    val periodStats: StateFlow<Map<LocalDate, Int>> =
        combine(allHabits, completions, _selectedPeriod) { _, comps, period ->
            val (start, end) = periodBounds(period)
            computePeriodCounts(comps, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Per-habit completion stats for the selected period. */
    val habitStats: StateFlow<List<HabitStat>> =
        combine(allHabits, completions, _selectedPeriod) { habits, comps, period ->
            val (start, end) = periodBounds(period)
            habits.map { habit ->
                val count = comps.count { it.habitId == habit.id && it.completedDate in start..end }
                val daysInPeriod = ChronoUnit.DAYS.between(start, end) + 1
                HabitStat(
                    habit = habit,
                    completions = count,
                    percentage = completionPercentage(habit, count, start, end),
                    daysInPeriod = daysInPeriod.toInt()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun periodBounds(period: StatisticsPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            StatisticsPeriod.WEEKLY -> today.minusDays(6) to today
            StatisticsPeriod.MONTHLY -> today.withDayOfMonth(1) to today
            StatisticsPeriod.YEARLY -> today.withDayOfYear(1) to today
        }
    }

    private fun computePeriodCounts(
        comps: List<HabitCompletion>,
        start: LocalDate,
        end: LocalDate
    ): Map<LocalDate, Int> {
        val raw = comps.asSequence()
            .filter { it.completedDate in start..end }
            .groupingBy { it.completedDate }
            .eachCount()
        val result = LinkedHashMap<LocalDate, Int>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            result[cursor] = raw[cursor] ?: 0
            cursor = cursor.plusDays(1)
        }
        return result
    }

    private fun completionPercentage(
        habit: Habit,
        actual: Int,
        start: LocalDate,
        end: LocalDate
    ): Double {
        if (actual <= 0) return 0.0
        val totalDays = ChronoUnit.DAYS.between(start, end) + 1
        if (totalDays <= 0) return 0.0

        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        val expected = when (unit) {
            "days" -> totalDays / frequency.toDouble()
            "weeks" -> totalDays / (frequency * 7.0)
            "months" -> (ChronoUnit.MONTHS.between(start, end) + 1) / frequency.toDouble()
            else -> totalDays.toDouble()
        }
        return if (expected > 0) (actual / expected * 100.0).coerceAtMost(100.0) else 0.0
    }

    private fun computeSummary(habits: List<Habit>, comps: List<HabitCompletion>): StatisticsSummary {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val monthStart = today.withDayOfMonth(1)
        val yearStart = today.withDayOfYear(1)

        val todayCompletions = comps.count { it.completedDate == today }
        val weekCompletions = comps.count { it.completedDate in weekStart..today }
        val monthCompletions = comps.count { it.completedDate in monthStart..today }
        val yearCompletions = comps.count { it.completedDate in yearStart..today }
        val longestStreak = habits.mapNotNull { habit ->
            val history = comps.filter { it.habitId == habit.id }.map { it.completedDate }.sorted()
            if (history.isEmpty()) 0 else StreakUtils.longestStreak(history)
        }.maxOrNull() ?: 0

        return StatisticsSummary(
            todayCompletions = todayCompletions,
            weekCompletions = weekCompletions,
            monthCompletions = monthCompletions,
            yearCompletions = yearCompletions,
            longestStreak = longestStreak,
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

data class HabitStat(
    val habit: Habit,
    val completions: Int,
    val percentage: Double,
    val daysInPeriod: Int
)
