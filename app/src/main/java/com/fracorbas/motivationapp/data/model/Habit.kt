package com.fracorbas.motivationapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a habit in the Atomic Habits style.
 * 
 * @param id Unique identifier
 * @param title The name of the habit (e.g., "Read 10 pages")
 * @param description Optional description
 * @param trigger The trigger moment (e.g., "After my morning coffee")
 * @param reminderTime Optional time for notification
 * @param isActive Whether the habit is active
 * @param createdAt Date when the habit was created
 * @param streak Current streak in days
 * @param lastCompletedDate Last date when the habit was completed
 */
@Entity(tableName = "habits")
@TypeConverters(LocalDateConverter::class, LocalTimeConverter::class)
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val trigger: String, // e.g., "After I brush my teeth" - kept for backward compatibility
    val triggerId: Int? = null, // Reference to Trigger entity
    val reminderTime: LocalTime? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now(),
    val streak: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val notificationEnabled: Boolean = false,
    val notificationFrequency: Int? = null,
    val notificationFrequencyUnit: String? = null, // "days", "weeks", "months"
    val targetDayOfWeek: Int? = null,  // 1-7 (Monday=1) for weekly habits, null = any day
    val targetDayOfMonth: Int? = null   // 1-31 for monthly habits, null = any day
)

/**
 * Converter for LocalDate to store in Room database
 */
class LocalDateConverter {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}

/**
 * Converter for LocalTime to store in Room database
 */
class LocalTimeConverter {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): LocalTime? {
        return value?.let { LocalTime.ofNanoOfDay(it) }
    }

    @androidx.room.TypeConverter
    fun timeToTimestamp(time: LocalTime?): Long? {
        return time?.toNanoOfDay()
    }
}
