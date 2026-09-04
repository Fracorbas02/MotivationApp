package com.fracorbas.motivationapp.data.repository

import androidx.room.withTransaction
import com.fracorbas.motivationapp.data.local.HabitCompletionDao
import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import com.fracorbas.motivationapp.data.model.StreakUtils
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
        database.withTransaction {
            // Insert the completion first, then compute the streak from the full
            // completion history — the source of truth — so it is always correct
            // regardless of any cached streak value on the Habit entity.
            completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
            val history = completionDao.getAllCompletionsForHabit(habitId)
            val newStreak = StreakUtils.currentStreak(history)
            val current = habitDao.getHabitById(habitId)
            if (current != null) {
                habitDao.updateHabit(current.copy(streak = newStreak, lastCompletedDate = today))
            }
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
            val newStreak = computeCurrentStreak(history)
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
     * Compute the current streak from the completion history.
     */
    private fun computeCurrentStreak(history: List<LocalDate>): Int =
        StreakUtils.currentStreak(history)

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
            if (habit.lastCompletedDate == today || !habit.isActive) return@forEach
            if (habit.lastCompletedDate == null) return@forEach

            val shouldReset = com.fracorbas.motivationapp.data.model.HabitFrequencyUtils
                .shouldResetHabitToday(habit)

            if (shouldReset) {
                val history = completionDao.getAllCompletionsForHabit(habit.id)
                val newStreak = StreakUtils.currentStreak(history)
                if (newStreak != habit.streak) {
                    habitDao.updateHabit(habit.copy(streak = newStreak))
                }
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
        start: LocalDate,
        end: LocalDate
    ): Int = getCompletionCountsByDate(start, end).values.sum()

    /**
     * All completion dates for a habit, oldest first (for history/streak views).
     */
    suspend fun getAllCompletionsForHabit(habitId: Int): List<LocalDate> =
        completionDao.getAllCompletionsForHabit(habitId)

    /**
     * Reactive stream of all completion records (newest first), so ViewModels can
     * recompute statistics without blocking the UI thread.
     */
    fun observeAllCompletions(): Flow<List<HabitCompletion>> =
        completionDao.observeAllCompletions()
}
