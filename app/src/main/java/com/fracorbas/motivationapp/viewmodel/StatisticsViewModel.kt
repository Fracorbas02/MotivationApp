package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for statistics screen.
 * 
 * Provides data for habit completion statistics and tracking.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    // Get all habits for statistics
    val allHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Get completion statistics for a specific period
     */
    fun getCompletionStatsForPeriod(
        habits: List<Habit>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Int> {
        val stats = mutableMapOf<LocalDate, Int>()
        
        // Initialize all dates in the period with 0
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            stats[currentDate] = 0
            currentDate = currentDate.plusDays(1)
        }
        
        // Count completions for each date
        habits.forEach { habit ->
            val lastCompleted = habit.lastCompletedDate
            if (lastCompleted != null && !lastCompleted.isBefore(startDate) && !lastCompleted.isAfter(endDate)) {
                stats[lastCompleted] = stats[lastCompleted]!! + 1
            }
        }
        
        return stats
    }

    /**
     * Get weekly statistics (last 7 days)
     */
    fun getWeeklyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6) // 7 days total
        return getCompletionStatsForPeriod(habits, startDate, endDate)
    }

    /**
     * Get monthly statistics (current month)
     */
    fun getMonthlyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val startDate = today.withDayOfMonth(1)
        return getCompletionStatsForPeriod(habits, startDate, today)
    }

    /**
     * Get yearly statistics (current year)
     */
    fun getYearlyStats(habits: List<Habit>): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val startDate = today.withDayOfYear(1)
        return getCompletionStatsForPeriod(habits, startDate, today)
    }

    /**
     * Get total completions for a period
     */
    fun getTotalCompletionsForPeriod(
        habits: List<Habit>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int {
        return habits.count { habit ->
            val lastCompleted = habit.lastCompletedDate
            lastCompleted != null && !lastCompleted.isBefore(startDate) && !lastCompleted.isAfter(endDate)
        }
    }

    /**
     * Get completion percentage for a habit in a period
     */
    fun getHabitCompletionPercentage(
        habit: Habit,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (totalDays <= 0) return 0.0
        
        // Count how many days this habit was completed in the period
        // This is a simplified calculation - for exact percentage we'd need completion history
        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        
        val expectedCompletions = when (unit) {
            "days" -> totalDays / frequency.toDouble()
            "weeks" -> totalDays / (frequency * 7.0)
            "months" -> {
                val months = ChronoUnit.MONTHS.between(startDate, endDate) + 1
                months / frequency.toDouble()
            }
            else -> totalDays.toDouble()
        }
        
        // Count actual completions - for now just check if completed in period
        val actualCompletions = if (habit.lastCompletedDate != null && 
            !habit.lastCompletedDate.isBefore(startDate) && 
            !habit.lastCompletedDate.isAfter(endDate)) {
            1
        } else {
            0
        }
        
        return if (expectedCompletions > 0) {
            (actualCompletions / expectedCompletions) * 100
        } else {
            0.0
        }
    }

    /**
     * Get the longest current streak
     */
    fun getLongestStreak(habits: List<Habit>): Int {
        return habits.maxOfOrNull { it.streak } ?: 0
    }

    /**
     * Get the most frequently completed habit
     */
    fun getMostCompletedHabit(habits: List<Habit>): Habit? {
        // For now, just return the one with highest streak
        // In a real app, we'd track completion history
        return habits.maxByOrNull { it.streak }
    }

    /**
     * Get statistics summary for dashboard
     */
    fun getStatisticsSummary(habits: List<Habit>): StatisticsSummary {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val monthStart = today.withDayOfMonth(1)
        val yearStart = today.withDayOfYear(1)
        
        val todayCompletions = habits.count { it.lastCompletedDate == today }
        val weekCompletions = habits.count { habit ->
            habit.lastCompletedDate != null && !habit.lastCompletedDate.isBefore(weekStart)
        }
        val monthCompletions = habits.count { habit ->
            habit.lastCompletedDate != null && !habit.lastCompletedDate.isBefore(monthStart)
        }
        val yearCompletions = habits.count { habit ->
            habit.lastCompletedDate != null && !habit.lastCompletedDate.isBefore(yearStart)
        }
        
        val longestStreak = getLongestStreak(habits)
        val activeHabits = habits.count { it.isActive }
        
        return StatisticsSummary(
            todayCompletions = todayCompletions,
            weekCompletions = weekCompletions,
            monthCompletions = monthCompletions,
            yearCompletions = yearCompletions,
            longestStreak = longestStreak,
            activeHabits = activeHabits
        )
    }
}

/**
 * Data class for statistics summary
 */
data class StatisticsSummary(
    val todayCompletions: Int,
    val weekCompletions: Int,
    val monthCompletions: Int,
    val yearCompletions: Int,
    val longestStreak: Int,
    val activeHabits: Int
)
