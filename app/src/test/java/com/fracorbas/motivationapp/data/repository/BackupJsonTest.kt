package com.fracorbas.motivationapp.data.repository

import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import com.fracorbas.motivationapp.data.model.Trigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BackupJsonTest {

    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)

    private fun sample(): BackupData {
        val trigger = Trigger(id = 1, name = "Après le café", description = "desc", isCustom = false)
        val habit = Habit(
            id = 10,
            title = "Lire 10 pages",
            description = "Livre de dev perso",
            trigger = "Après le café",
            triggerId = 1,
            reminderTime = LocalTime.of(8, 30),
            isActive = true,
            createdAt = yesterday,
            streak = 3,
            lastCompletedDate = today,
            notificationEnabled = true,
            notificationFrequency = 2,
            notificationFrequencyUnit = "days"
        )
        val completion = HabitCompletion(id = 100, habitId = 10, completedDate = today)
        return BackupData(listOf(habit), listOf(trigger), listOf(completion))
    }

    @Test
    fun `round-trip preserves all fields`() {
        val json = BackupJson.toJson(sample())
        val parsed = BackupJson.fromJson(json)

        assertEquals(1, parsed.habits.size)
        assertEquals(1, parsed.triggers.size)
        assertEquals(1, parsed.completions.size)

        val h = parsed.habits.first()
        assertEquals(10, h.id)
        assertEquals("Lire 10 pages", h.title)
        assertEquals("Livre de dev perso", h.description)
        assertEquals("Après le café", h.trigger)
        assertEquals(1, h.triggerId)
        assertEquals(LocalTime.of(8, 30), h.reminderTime)
        assertTrue(h.isActive)
        assertEquals(3, h.streak)
        assertEquals(today, h.lastCompletedDate)
        assertTrue(h.notificationEnabled)
        assertEquals(2, h.notificationFrequency)
        assertEquals("days", h.notificationFrequencyUnit)

        val t = parsed.triggers.first()
        assertEquals(1, t.id)
        assertEquals("Après le café", t.name)
        assertEquals("desc", t.description)
        assertEquals(false, t.isCustom)

        val c = parsed.completions.first()
        assertEquals(10, c.habitId)
        assertEquals(today, c.completedDate)
    }

    @Test
    fun `nullable fields survive absence`() {
        val habit = Habit(id = 1, title = "Bare", trigger = "x")
        val json = BackupJson.toJson(BackupData(listOf(habit), emptyList(), emptyList()))
        val parsed = BackupJson.fromJson(json).habits.first()

        assertEquals(1, parsed.id)
        assertNull(parsed.description)
        assertNull(parsed.triggerId)
        assertNull(parsed.reminderTime)
        assertNull(parsed.lastCompletedDate)
        assertNull(parsed.notificationFrequency)
        assertNull(parsed.notificationFrequencyUnit)
        assertFalse(parsed.notificationEnabled)
    }

    @Test
    fun `empty backup round-trips`() {
        val json = BackupJson.toJson(BackupData(emptyList(), emptyList(), emptyList()))
        val parsed = BackupJson.fromJson(json)
        assertTrue(parsed.habits.isEmpty())
        assertTrue(parsed.triggers.isEmpty())
        assertTrue(parsed.completions.isEmpty())
    }

    private fun assertFalse(v: Boolean) = org.junit.Assert.assertFalse(v)
}
