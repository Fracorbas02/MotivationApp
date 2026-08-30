package com.fracorbas.motivationapp.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Utility functions for habit frequency management.
 */
object HabitFrequencyUtils {

    /**
     * Check if a habit should be reset today based on its frequency.
     * 
     * @param habit The habit to check
     * @return true if the habit should be reset (i.e., it's time to complete it again)
     */
    fun shouldResetHabitToday(habit: Habit): Boolean {
        val lastCompleted = habit.lastCompletedDate ?: return true // Never completed, should be active
        val today = LocalDate.now()
        
        // If it's a different day than last completed, check frequency
        if (lastCompleted == today) {
            return false // Already completed today
        }
        
        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        
        val daysSinceCompletion = ChronoUnit.DAYS.between(lastCompleted, today)
        val frequencyLong = frequency.toLong()
        
        return when (unit) {
            "days" -> daysSinceCompletion >= frequencyLong
            "weeks" -> daysSinceCompletion >= frequencyLong * 7L
            "months" -> {
                // For months, we need to check calendar months
                val lastMonth = lastCompleted.withDayOfMonth(1)
                val currentMonth = today.withDayOfMonth(1)
                val monthsBetween = ChronoUnit.MONTHS.between(lastMonth, currentMonth)
                monthsBetween >= frequencyLong
            }
            else -> daysSinceCompletion >= 1L // Default to daily
        }
    }
    
    /**
     * Get the next date when a habit should be completed.
     * 
     * @param habit The habit
     * @return The next date when the habit should be completed
     */
    fun getNextCompletionDate(habit: Habit): LocalDate {
        val lastCompleted = habit.lastCompletedDate ?: return LocalDate.now()
        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        
        return when (unit) {
            "days" -> lastCompleted.plusDays(frequency.toLong())
            "weeks" -> lastCompleted.plusWeeks(frequency.toLong())
            "months" -> lastCompleted.plusMonths(frequency.toLong())
            else -> lastCompleted.plusDays(1)
        }
    }
    
    /**
     * Check if today is the day to complete the habit based on frequency.
     * 
     * @param habit The habit to check
     * @return true if today is the day to complete the habit
     */
    fun isCompletionDayToday(habit: Habit): Boolean {
        val today = LocalDate.now()
        val lastCompleted = habit.lastCompletedDate ?: return true // Never completed, should be done today
        
        if (lastCompleted == today) {
            return false // Already completed today
        }
        
        val frequency = habit.notificationFrequency ?: 1
        val unit = habit.notificationFrequencyUnit ?: "days"
        
        val daysSinceCompletion = ChronoUnit.DAYS.between(lastCompleted, today)
        val frequencyLong = frequency.toLong()
        
        return when (unit) {
            "days" -> daysSinceCompletion >= frequencyLong
            "weeks" -> daysSinceCompletion >= frequencyLong * 7L
            "months" -> {
                val lastMonthStart = lastCompleted.withDayOfMonth(1)
                val currentMonthStart = today.withDayOfMonth(1)
                val monthsBetween = ChronoUnit.MONTHS.between(lastMonthStart, currentMonthStart)
                monthsBetween >= frequencyLong
            }
            else -> daysSinceCompletion >= 1L
        }
    }
}
