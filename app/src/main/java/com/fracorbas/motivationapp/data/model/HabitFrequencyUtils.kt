package com.fracorbas.motivationapp.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Utility functions for habit frequency management.
 *
 * Supports optional target-day selection:
 * - Weekly habits: [Habit.targetDayOfWeek] (1=Monday … 7=Sunday). When set,
 *   the habit is only "due" on that specific weekday each N weeks.
 * - Monthly habits: [Habit.targetDayOfMonth] (1-31). When set, the habit is
 *   only due on that day of the month, every N months.
 */
object HabitFrequencyUtils {

    /**
     * Check if a habit should be reset today based on its frequency.
     * @return true if the habit should be reset (i.e., it's time to complete it again)
     */
    fun shouldResetHabitToday(habit: Habit): Boolean {
        val lastCompleted = habit.lastCompletedDate ?: return true
        val today = LocalDate.now()

        if (lastCompleted == today) return false

        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        val daysSinceCompletion = ChronoUnit.DAYS.between(lastCompleted, today)

        return when (unit) {
            "days" -> daysSinceCompletion > frequency.toLong()
            "weeks" -> {
                if (habit.targetDayOfWeek != null) {
                    val nextDue = nextWeeklyDueDate(lastCompleted, habit.targetDayOfWeek, frequency)
                    today.isAfter(nextDue) || today == nextDue
                } else {
                    daysSinceCompletion > frequency.toLong() * 7L
                }
            }
            "months" -> {
                if (habit.targetDayOfMonth != null) {
                    val nextDue = nextMonthlyDueDate(lastCompleted, habit.targetDayOfMonth, frequency)
                    today.isAfter(nextDue) || today == nextDue
                } else {
                    val lastMonthStart = lastCompleted.withDayOfMonth(1)
                    val currentMonthStart = today.withDayOfMonth(1)
                    ChronoUnit.MONTHS.between(lastMonthStart, currentMonthStart) > frequency.toLong()
                }
            }
            else -> daysSinceCompletion > 1L
        }
    }

    /**
     * Get the next date when a habit should be completed.
     */
    fun getNextCompletionDate(habit: Habit): LocalDate {
        val lastCompleted = habit.lastCompletedDate ?: return LocalDate.now()
        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"

        return when (unit) {
            "days" -> lastCompleted.plusDays(frequency.toLong())
            "weeks" -> {
                if (habit.targetDayOfWeek != null) {
                    nextWeeklyDueDate(lastCompleted, habit.targetDayOfWeek, frequency)
                } else {
                    lastCompleted.plusWeeks(frequency.toLong())
                }
            }
            "months" -> {
                if (habit.targetDayOfMonth != null) {
                    nextMonthlyDueDate(lastCompleted, habit.targetDayOfMonth, frequency)
                } else {
                    lastCompleted.plusMonths(frequency.toLong())
                }
            }
            else -> lastCompleted.plusDays(1)
        }
    }

    /**
     * Check if today is the day to complete the habit based on frequency and
     * optional target day. This determines whether the toggle is clickable.
     */
    fun isCompletionDayToday(habit: Habit): Boolean {
        val today = LocalDate.now()
        val lastCompleted = habit.lastCompletedDate

        if (lastCompleted == null) {
            return when (habit.notificationFrequencyUnit ?: "days") {
                "weeks" -> habit.targetDayOfWeek == null || today.dayOfWeek.value == habit.targetDayOfWeek
                "months" -> habit.targetDayOfMonth == null ||
                    today.dayOfMonth == resolveDayOfMonth(habit.targetDayOfMonth, today)
                else -> true
            }
        }

        if (lastCompleted == today) return false

        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        val daysSinceCompletion = ChronoUnit.DAYS.between(lastCompleted, today)

        return when (unit) {
            "days" -> daysSinceCompletion >= frequency.toLong()
            "weeks" -> {
                if (habit.targetDayOfWeek != null) {
                    if (today.dayOfWeek.value != habit.targetDayOfWeek) return false
                    val lastTargetDay = lastCompleted.with(habit.targetDayOfWeek.toDayOfWeek())
                    val adjustedLast = if (lastTargetDay.isAfter(lastCompleted)) lastTargetDay.minusWeeks(1) else lastTargetDay
                    ChronoUnit.WEEKS.between(adjustedLast, today) >= frequency.toLong()
                } else {
                    daysSinceCompletion >= frequency.toLong() * 7L
                }
            }
            "months" -> {
                val lastMonthStart = lastCompleted.withDayOfMonth(1)
                val currentMonthStart = today.withDayOfMonth(1)
                val monthsBetween = ChronoUnit.MONTHS.between(lastMonthStart, currentMonthStart)
                if (habit.targetDayOfMonth != null) {
                    if (today.dayOfMonth != resolveDayOfMonth(habit.targetDayOfMonth, today)) return false
                    monthsBetween >= frequency.toLong()
                } else {
                    monthsBetween >= frequency.toLong()
                }
            }
            else -> daysSinceCompletion >= 1L
        }
    }

    private fun nextWeeklyDueDate(lastCompleted: LocalDate, targetDayOfWeek: Int, frequency: Int): LocalDate {
        var candidate = lastCompleted.plusWeeks(frequency.toLong())
            .with(targetDayOfWeek.toDayOfWeek())
        if (candidate.isBefore(lastCompleted.plusWeeks(frequency.toLong()))) {
            candidate = candidate.plusWeeks(1)
        }
        val today = LocalDate.now()
        while (candidate.isBefore(today)) {
            candidate = candidate.plusWeeks(frequency.toLong())
        }
        return candidate
    }

    private fun nextMonthlyDueDate(lastCompleted: LocalDate, targetDayOfMonth: Int, frequency: Int): LocalDate {
        var monthOffset = frequency
        var candidate = lastCompleted.plusMonths(monthOffset.toLong())
        candidate = candidate.withDayOfMonth(resolveDayOfMonth(targetDayOfMonth, candidate))

        val today = LocalDate.now()
        while (candidate.isBefore(today)) {
            monthOffset += frequency
            candidate = lastCompleted.plusMonths(monthOffset.toLong())
            candidate = candidate.withDayOfMonth(resolveDayOfMonth(targetDayOfMonth, candidate))
        }
        return candidate
    }

    /**
     * Resolve targetDayOfMonth to an actual day, treating 31 (or any value
     * exceeding the month length) as the last day of that month.
     */
    private fun resolveDayOfMonth(target: Int, date: LocalDate): Int =
        if (target >= date.lengthOfMonth()) date.lengthOfMonth() else target

    private fun Int.toDayOfWeek(): DayOfWeek = DayOfWeek.of(this)
}
