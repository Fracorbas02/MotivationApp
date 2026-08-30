package com.fracorbas.motivationapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fracorbas.motivationapp.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for [HabitCompletion] history records.
 */
@Dao
interface HabitCompletionDao {

    /**
     * Record a completion for a habit on a date. IGNORE so that toggling twice
     * on the same day does not create duplicates.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    /**
     * Remove a completion record (used when undoing today's completion).
     */
    @Query("""
        DELETE FROM habit_completions
        WHERE habitId = :habitId AND completedDate = :date
    """)
    suspend fun deleteCompletion(habitId: Int, date: LocalDate)

    /**
     * Check whether a habit was completed on a given date.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM habit_completions
            WHERE habitId = :habitId AND completedDate = :date
        )
    """)
    suspend fun isCompletedOn(habitId: Int, date: LocalDate): Boolean

    /**
     * All completion dates for a habit within [start, end] (inclusive), oldest first.
     */
    @Query("""
        SELECT completedDate FROM habit_completions
        WHERE habitId = :habitId
          AND completedDate >= :start
          AND completedDate <= :end
        ORDER BY completedDate ASC
    """)
    suspend fun getCompletionsBetween(
        habitId: Int,
        start: LocalDate,
        end: LocalDate
    ): List<LocalDate>

    /**
     * Count of completions for a habit within [start, end] (inclusive).
     */
    @Query("""
        SELECT COUNT(*) FROM habit_completions
        WHERE habitId = :habitId
          AND completedDate >= :start
          AND completedDate <= :end
    """)
    suspend fun countCompletionsBetween(
        habitId: Int,
        start: LocalDate,
        end: LocalDate
    ): Int

    /**
     * Per-date completion counts across all habits within [start, end].
     * One row per date that has at least one completion.
     */
    @Query("""
        SELECT completedDate AS date, COUNT(*) AS count
        FROM habit_completions
        WHERE completedDate >= :start AND completedDate <= :end
        GROUP BY completedDate
    """)
    suspend fun countCompletionsByDate(
        start: LocalDate,
        end: LocalDate
    ): List<DateCount>

    /**
     * All completion dates for a habit (for streak computation and history views).
     */
    @Query("""
        SELECT completedDate FROM habit_completions
        WHERE habitId = :habitId
        ORDER BY completedDate ASC
    """)
    suspend fun getAllCompletionsForHabit(habitId: Int): List<LocalDate>

    /**
     * All completions, as a flow, so statistics screens can react to changes.
     */
    @Query("SELECT * FROM habit_completions ORDER BY completedDate DESC")
    fun observeAllCompletions(): Flow<List<HabitCompletion>>
}

/** Aggregate row: a date and how many habits were completed on it. */
data class DateCount(
    val date: LocalDate,
    val count: Int
)
