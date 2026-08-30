package com.fracorbas.motivationapp.data.repository

import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Repository for habit-related operations.
 * 
 * This class abstracts the data layer and provides a clean API for the ViewModel.
 * 
 * @property habitDao Data Access Object for habits
 */
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
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
     * Mark a habit as completed for today
     */
    suspend fun markHabitCompleted(habitId: Int) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        habitDao.markHabitCompleted(habitId, today, yesterday)
    }

    /**
     * Toggle habit completion status for today
     */
    suspend fun toggleHabitCompletion(habitId: Int) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val today = LocalDate.now()
        
        if (habit.lastCompletedDate == today) {
            // Undo completion: set streak back to what it was before today
            val yesterday = today.minusDays(1)
            val newStreak = if (habit.lastCompletedDate == today) {
                if (habit.streak > 1) habit.streak - 1 else 0
            } else {
                0
            }
            habitDao.updateHabit(
                habit.copy(
                    lastCompletedDate = null,
                    streak = newStreak
                )
            )
        } else {
            // Mark as completed
            val yesterday = today.minusDays(1)
            habitDao.markHabitCompleted(habitId, today, yesterday)
        }
    }

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
}
