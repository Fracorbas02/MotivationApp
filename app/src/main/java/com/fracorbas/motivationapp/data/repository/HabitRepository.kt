package com.fracorbas.motivationapp.data.repository

import androidx.room.withTransaction
import com.fracorbas.motivationapp.data.local.HabitCompletionDao
import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Repository for habit-related operations.
 *
 * @property habitDao Data Access Object for habits
 * @property completionDao Data Access Object for completion history
 * @property database Room database, used for transactional updates that span both DAOs
 */
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val database: HabitDatabase
) {

    /**
     * Get all habits as a flow (observes changes)
     */
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    /**
     * Get all active habits
     */
    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()

    /**
     * Get a habit by its ID
     */
    suspend fun getHabitById(id: Int): Habit? = habitDao.getHabitById(id)

    /**
     * Create a new habit
     */
    suspend fun createHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    /**
     * Delete a habit
     */
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    /**
     * Mark a habit as completed for today (streak + lastCompletedDate + history record).
     */
    private suspend fun markCompleted(habitId: Int) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        database.withTransaction {
            // Compute the new streak from history for robustness.
            val previousDate = completionDao.getCompletionsBetween(habitId, today.minusDays(365), yesterday)
                .lastOrNull()
            val newStreak = if (previousDate == yesterday) {
                // Continued the chain: previous streak + 1. Recover it from the habit.
                val current = habitDao.getHabitById(habitId)
                (current?.streak ?: 0) + 1
            } else {
                1
            }
            habitDao.markHabitCompleted(habitId, today, yesterday)
            // Overwrite streak with the computed value (markHabitCompleted may set it differently).
            val updated = habitDao.getHabitById(habitId)
            if (updated != null && updated.streak != newStreak) {
                habitDao.updateHabit(updated.copy(streak = newStreak, lastCompletedDate = today))
            }
            completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        }
    }

    /**
     * Undo today's completion for a habit (streak + lastCompletedDate + history record).
     */
    private suspend fun undoCompleted(habitId: Int) {
        val today = LocalDate.now()
        database.withTransaction {
            completionDao.deleteCompletion(habitId, today)
            // Recompute lastCompletedDate and streak from the history.
            val history = completionDao.getAllCompletionsForHabit(habitId)
            val newLast = history.lastOrNull()
            val newStreak = computeCurrentStreak(newLast, history)
            val current = habitDao.getHabitById(habitId) ?: return@withTransaction
            habitDao.updateHabit(current.copy(lastCompletedDate = newLast, streak = newStreak))
        }
    }

    /**
     * Toggle habit completion status for today.
     */
    suspend fun toggleHabitCompletion(habitId: Int) {
        val habit = habitDao.getHabitById(habitId) ?: return
        if (habit.lastCompletedDate == LocalDate.now()) {
            undoCompleted(habitId)
        } else {
            markCompleted(habitId)
        }
    }

    /**
     * Compute the current streak (consecutive days ending today or yesterday)
     * from a sorted list of completion dates. Delegates to [StreakUtils].
     */
    private fun computeCurrentStreak(lastCompleted: LocalDate?, history: List<LocalDate>): Int =
        com.fracorbas.motivationapp.data.model.StreakUtils.currentStreak(history)

    /**
     * Search habits by query
     */
    fun searchHabits(query: String): Flow<List<Habit>> = habitDao.searchHabits(query)

    /**
     * Get habits that have notifications enabled
     */
    fun getHabitsWithNotifications(): Flow<List<Habit>> = habitDao.getHabitsWithNotifications()

    /**
     * Get all active habits with notifications enabled and reminder time set
     * (non-Flow version for one-time queries like after boot)
     */
    suspend fun getAllActiveHabitsWithReminders(): List<Habit> = 
        habitDao.getAllActiveHabitsWithReminders()

    /**
     * Get today's completed habits
     */
    fun getTodayCompletedHabits(): Flow<List<Habit>> = 
        habitDao.getTodayCompletedHabits(LocalDate.now())

    /**
     * Enable or disable notification for a habit
     */
    suspend fun setNotificationEnabled(habitId: Int, enabled: Boolean) {
        val habit = habitDao.getHabitById(habitId) ?: return
        habitDao.updateHabit(habit.copy(notificationEnabled = enabled))
    }

    /**
     * Toggle habit active status
     */
    suspend fun toggleHabitActive(habitId: Int) {
        val habit = habitDao.getHabitById(habitId) ?: return
        habitDao.updateHabit(habit.copy(isActive = !habit.isActive))
    }

    /**
     * Reset habits that should be reset today based on their frequency.
     * Called at app startup to reset daily habits.
     */
    suspend fun resetHabitsForNewDay() {
        val today = java.time.LocalDate.now()
        val habits = habitDao.getAllHabitsList()
        
        habits.forEach { habit ->
            // Skip if already completed today or not active
            if (habit.lastCompletedDate == today || !habit.isActive) {
                return@forEach
            }
            
            val lastCompleted = habit.lastCompletedDate ?: return@forEach
            val frequency = habit.notificationFrequency ?: 1
            val unit = habit.notificationFrequencyUnit ?: "days"
            
            val shouldReset = when (unit) {
                "days" -> {
                    val daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastCompleted, today)
                    daysSince >= frequency
                }
                "weeks" -> {
                    val daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastCompleted, today)
                    daysSince >= frequency * 7L
                }
                "months" -> {
                    val lastMonthStart = lastCompleted.withDayOfMonth(1)
                    val currentMonthStart = today.withDayOfMonth(1)
                    val monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(lastMonthStart, currentMonthStart)
                    monthsBetween >= frequency
                }
                else -> true // Default to daily reset
            }
            
            if (shouldReset) {
                // Reset the habit
                habitDao.updateHabit(
                    habit.copy(
                        lastCompletedDate = null,
                        streak = 0
                    )
                )
            }
        }
    }

    /**
     * Get all habits as a list (non-Flow version)
     */
    suspend fun getAllHabitsList(): List<Habit> = habitDao.getAllHabitsList()

    /**
     * Check if a habit should be reset today based on its frequency
     */
    fun shouldResetHabitToday(habit: Habit): Boolean {
        return com.fracorbas.motivationapp.data.model.HabitFrequencyUtils.shouldResetHabitToday(habit)
    }

    // ==================== Completion history (statistics) ====================

    /**
     * Per-date completion counts across all habits within [start, end] (inclusive).
     * Dates with zero completions are still present with count 0.
     */
    suspend fun getCompletionCountsByDate(
        start: LocalDate,
        end: LocalDate
    ): Map<LocalDate, Int> {
        val raw = completionDao.countCompletionsByDate(start, end).associate { it.date to it.count }
        val result = mutableMapOf<LocalDate, Int>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            result[cursor] = raw[cursor] ?: 0
            cursor = cursor.plusDays(1)
        }
        return result
    }

    /**
     * Number of times a habit was completed within [start, end] (inclusive).
     */
    suspend fun getHabitCompletionCount(
        habitId: Int,
        start: LocalDate,
        end: LocalDate
    ): Int = completionDao.countCompletionsBetween(habitId, start, end)

    /**
     * Total completions across all habits within [start, end] (inclusive).
     */
    suspend fun getHabitCompletionCountAllHabits(
        habits: List<Habit>,
        start: LocalDate,
        end: LocalDate
    ): Int = getCompletionCountsByDate(start, end).values.sum()

    /**
     * All completion dates for a habit, oldest first (for history/streak views).
     */
    suspend fun getAllCompletionsForHabit(habitId: Int): List<LocalDate> =
        completionDao.getAllCompletionsForHabit(habitId)
}
