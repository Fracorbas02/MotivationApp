package com.fracorbas.motivationapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import com.fracorbas.motivationapp.data.model.LocalDateConverter
import com.fracorbas.motivationapp.data.model.LocalTimeConverter
import com.fracorbas.motivationapp.data.model.Trigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for the MotivationApp.
 * 
 * @property habitDao Data Access Object for habits
 * @property triggerDao Data Access Object for triggers
 */
@Database(
    entities = [Habit::class, Trigger::class, HabitCompletion::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class, LocalTimeConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao
    abstract val triggerDao: TriggerDao
    abstract val habitCompletionDao: HabitCompletionDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        /** Migrations, exposed for instrumented tests. */
        internal val ALL_MIGRATIONS: Array<Migration> get() = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create triggers table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS triggers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        isCustom INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Add new columns to habits table
                database.execSQL("""
                    ALTER TABLE habits 
                    ADD COLUMN triggerId INTEGER
                """)
                database.execSQL("""
                    ALTER TABLE habits 
                    ADD COLUMN notificationFrequency INTEGER
                """)
                database.execSQL("""
                    ALTER TABLE habits 
                    ADD COLUMN notificationFrequencyUnit TEXT
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the habit completion history table.
                // completedDate is stored as epoch-day (Long) via the LocalDate type converter.
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_completions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        completedDate INTEGER NOT NULL,
                        FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_habit_completions_habitId_completedDate
                    ON habit_completions(habitId, completedDate)
                """)

                // Backfill: seed one completion row per habit that already has a
                // lastCompletedDate, so existing progress is not lost.
                database.execSQL("""
                    INSERT OR IGNORE INTO habit_completions (habitId, completedDate)
                    SELECT id, lastCompletedDate FROM habits
                    WHERE lastCompletedDate IS NOT NULL
                """)
            }
        }

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "motivation_app_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Insert default triggers on first database creation
                            val defaultTriggers = listOf(
                                "Après le réveil" to "Dès que je me réveille",
                                "Après le café du matin" to "Après mon premier café",
                                "Avant de partir travailler" to "Avant de quitter la maison",
                                "Pause de midi" to "Pendant la pause déjeuner",
                                "Après le travail" to "Quand je rentre à la maison",
                                "Avant le dîner" to "Avant de manger le soir",
                                "Avant de dormir" to "Avant d'aller me coucher",
                                "Après manger" to "Après un repas",
                                "En arrivant au travail" to "Quand j'arrive au bureau",
                                "Après une réunion" to "À la fin d'une réunion"
                            )

                            defaultTriggers.forEach { (name, description) ->
                                db.execSQL(
                                    "INSERT INTO triggers (name, description, isCustom) VALUES (?, ?, 0)",
                                    arrayOf(name, description)
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
