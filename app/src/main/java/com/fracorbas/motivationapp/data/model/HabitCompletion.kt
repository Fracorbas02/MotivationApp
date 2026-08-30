package com.fracorbas.motivationapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Records one completion of a habit on a given date.
 *
 * One row per (habitId, completedDate). This history powers accurate statistics:
 * per-day completion counts, completion percentages over a period, and streaks.
 * The [Habit] entity still carries [Habit.lastCompletedDate] and [Habit.streak]
 * for fast UI display; this table is the source of truth for past completions.
 */
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId", "completedDate"], unique = true)]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val completedDate: LocalDate
)
