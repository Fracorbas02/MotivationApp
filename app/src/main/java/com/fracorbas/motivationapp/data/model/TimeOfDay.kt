package com.fracorbas.motivationapp.data.model

import java.time.LocalTime

/**
 * Represents periods of the day for grouping habits in timeline view.
 */
enum class TimeOfDay(
    val displayName: String,
    val startHour: Int,
    val endHour: Int
) {
    MORNING("Matin", 6, 12),
    AFTERNOON("Après-midi", 12, 18),
    EVENING("Soir", 18, 24),
    NIGHT("Nuit", 0, 6);

    companion object {
        /**
         * Get the TimeOfDay for a given LocalTime
         */
        fun fromTime(time: LocalTime?): TimeOfDay {
            if (time == null) return AFTERNOON // Default to afternoon if no time set
            
            val hour = time.hour
            return when {
                hour in 0..5 -> NIGHT
                hour in 6..11 -> MORNING
                hour in 12..17 -> AFTERNOON
                else -> EVENING
            }
        }

        /**
         * Get all TimeOfDay values in order
         */
        fun inOrder(): List<TimeOfDay> = listOf(MORNING, AFTERNOON, EVENING, NIGHT)
    }
}
