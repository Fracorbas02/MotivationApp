package com.fracorbas.motivationapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fracorbas.motivationapp.data.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Habit entities.
 */
@Dao
interface HabitDao {

    /**
     * Get all habits, ordered by creation date (newest first)
     */
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    /**
     * Get all active habits
     */
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    /**
     * Get a habit by ID
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Int): Habit?

    /**
     * Insert a new habit
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    /**
     * Update an existing habit
     */
    @Update
    suspend fun updateHabit(habit: Habit)

    /**
     * Delete a habit
     */
    @Delete
    suspend fun deleteHabit(habit: Habit)

    /**
     * Toggle habit completion for today
     */
    @Query("""
        UPDATE habits 
        SET 
            streak = CASE 
                WHEN lastCompletedDate = :yesterday THEN streak + 1
                WHEN lastCompletedDate IS NULL OR lastCompletedDate < :yesterday THEN 1
                ELSE 1
            END,
            lastCompletedDate = :today
        WHERE id = :habitId
    """)
    suspend fun markHabitCompleted(
        habitId: Int,
        today: LocalDate,
        yesterday: LocalDate
    )

    /**
     * Reset streak if habit not completed today (for daily habits)
     */
    @Query("""
        UPDATE habits 
        SET streak = 0
        WHERE id = :habitId AND lastCompletedDate != :today
    """)
    suspend fun resetStreakIfNeeded(habitId: Int, today: LocalDate)

    /**
     * Get habits that need notification at a specific time
     */
    @Query("""
        SELECT * FROM habits 
        WHERE notificationEnabled = 1 
        AND reminderTime IS NOT NULL
        AND isActive = 1
    """)
    fun getHabitsWithNotifications(): Flow<List<Habit>>

    /**
     * Get today's completed habits
     */
    @Query("SELECT * FROM habits WHERE lastCompletedDate = :today")
    fun getTodayCompletedHabits(today: LocalDate): Flow<List<Habit>>

    /**
     * Search habits by title or trigger
     */
    @Query("""
        SELECT * FROM habits 
        WHERE title LIKE '%' || :query || '%' 
        OR trigger LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchHabits(query: String): Flow<List<Habit>>

    /**
     * Get all habits as a list (non-Flow version for one-time queries)
     */
    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<Habit>

    /**
     * Get all active habits with notifications enabled and reminder time set
     * (non-Flow version for one-time queries like after boot)
     */
    @Query("""
        SELECT * FROM habits
        WHERE notificationEnabled = 1
        AND reminderTime IS NOT NULL
        AND isActive = 1
    """)
    suspend fun getAllActiveHabitsWithReminders(): List<Habit>

    /**
     * Delete all habits (used by backup import).
     */
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
