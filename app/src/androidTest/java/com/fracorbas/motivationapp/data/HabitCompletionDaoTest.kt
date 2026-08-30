package com.fracorbas.motivationapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fracorbas.motivationapp.data.local.HabitCompletionDao
import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HabitCompletionDaoTest {

    private lateinit var database: HabitDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var completionDao: HabitCompletionDao

    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HabitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao
        completionDao = database.habitCompletionDao
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun seedHabit(title: String = "Lire"): Int {
        val id = habitDao.insertHabit(Habit(title = title, trigger = "x"))
        return id.toInt()
    }

    @Test
    fun insert_and_count_completion() = runTest {
        val habitId = seedHabit()
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        assertEquals(1, completionDao.countCompletionsBetween(habitId, yesterday, today))
        assertTrue(completionDao.isCompletedOn(habitId, today))
        assertFalse(completionDao.isCompletedOn(habitId, yesterday))
    }

    @Test
    fun duplicate_same_day_is_ignored() = runTest {
        val habitId = seedHabit()
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        assertEquals(1, completionDao.countCompletionsBetween(habitId, today, today))
    }

    @Test
    fun delete_completion_removes_record() = runTest {
        val habitId = seedHabit()
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        completionDao.deleteCompletion(habitId, today)
        assertFalse(completionDao.isCompletedOn(habitId, today))
    }

    @Test
    fun per_date_counts_aggregate_across_habits() = runTest {
        val h1 = seedHabit("A")
        val h2 = seedHabit("B")
        completionDao.insertCompletion(HabitCompletion(habitId = h1, completedDate = today))
        completionDao.insertCompletion(HabitCompletion(habitId = h2, completedDate = today))
        val counts = completionDao.countCompletionsByDate(today, today).associate { it.date to it.count }
        assertEquals(2, counts[today])
    }

    @Test
    fun deleting_habit_cascades_to_completions() = runTest {
        val habitId = seedHabit()
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = today))
        val habit = habitDao.getHabitById(habitId)!!
        habitDao.deleteHabit(habit)
        assertEquals(0, completionDao.countCompletionsBetween(habitId, today.minusDays(7), today))
    }

    @Test
    fun getCompletionsBetween_respects_bounds() = runTest {
        val habitId = seedHabit()
        val d1 = today.minusDays(5)
        val d2 = today.minusDays(2)
        val d3 = today
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = d1))
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = d2))
        completionDao.insertCompletion(HabitCompletion(habitId = habitId, completedDate = d3))

        val inWindow = completionDao.getCompletionsBetween(habitId, d2, d3)
        assertEquals(listOf(d2, d3), inWindow)
    }
}
