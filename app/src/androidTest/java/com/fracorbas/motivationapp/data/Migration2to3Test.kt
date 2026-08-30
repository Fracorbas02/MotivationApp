package com.fracorbas.motivationapp.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fracorbas.motivationapp.data.local.HabitDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Tests the 2→3 migration without relying on exported Room schemas
 * (exportSchema = false). A v2 database is built by hand with raw SQL, then the
 * real HabitDatabase is opened with addMigrations to run 2→3.
 */
@RunWith(AndroidJUnit4::class)
class Migration2to3Test {

    private val dbName = "migration_test_2_3.db"

    @Test
    fun migrate_2_to_3_createsCompletionsTable_and_backfills() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val today = LocalDate.now().toEpochDay()

        // 1) Create a v2 database by hand with raw SQLite (no Room schema export needed).
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS habits (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            description TEXT,
                            trigger TEXT NOT NULL,
                            triggerId INTEGER,
                            reminderTime INTEGER,
                            isActive INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            streak INTEGER NOT NULL,
                            lastCompletedDate INTEGER,
                            notificationEnabled INTEGER NOT NULL,
                            notificationFrequency INTEGER,
                            notificationFrequencyUnit TEXT
                        )"""
                    )
                    db.execSQL(
                        """CREATE TABLE IF NOT EXISTS triggers (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT,
                            isCustom INTEGER NOT NULL DEFAULT 1
                        )"""
                    )
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // No-op: we only ever create at v2 then hand off to Room.
                }
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val writable = helper.writableDatabase
        writable.execSQL(
            "INSERT INTO habits (title, trigger, isActive, createdAt, streak, lastCompletedDate, notificationEnabled) " +
                "VALUES ('Lire', 'x', 1, $today, 1, $today, 0)"
        )
        writable.close()

        // 2) Open HabitDatabase with the real migrations; Room will run 2→3.
        val db = Room.databaseBuilder(context, HabitDatabase::class.java, dbName)
            .addMigrations(*HabitDatabase.ALL_MIGRATIONS)
            .build()
        db.openHelper.writableDatabase // force migration to run

        // 3) habit_completions exists and has the backfilled row.
        val cursor = db.openHelper.writableDatabase.query(
            "SELECT habitId, completedDate FROM habit_completions"
        )
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals(today, cursor.getLong(1))

        // 4) Unique index on (habitId, completedDate) exists.
        val indexCursor = db.openHelper.writableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='habit_completions'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) indexNames.add(indexCursor.getString(0))
        assertTrue(indexNames.any { it.contains("habitId") && it.contains("completedDate") })

        db.close()
        context.deleteDatabase(dbName)
    }
}
