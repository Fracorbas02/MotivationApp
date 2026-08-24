package com.fracorbas.motivationapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.LocalDateConverter
import com.fracorbas.motivationapp.data.model.LocalTimeConverter

/**
 * Room database for the MotivationApp.
 * 
 * @property habitDao Data Access Object for habits
 */
@Database(
    entities = [Habit::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class, LocalTimeConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao
}
