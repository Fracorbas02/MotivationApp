package com.fracorbas.motivationapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakUtilsTest {

    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)

    @Test
    fun `current streak is zero when history is empty`() {
        assertEquals(0, StreakUtils.currentStreak(emptyList()))
    }

    @Test
    fun `current streak is zero when last completion is older than yesterday`() {
        val history = listOf(today.minusDays(5), today.minusDays(3))
        assertEquals(0, StreakUtils.currentStreak(history))
    }

    @Test
    fun `current streak counts consecutive days ending today`() {
        val history = listOf(today.minusDays(3), today.minusDays(2), yesterday, today)
        assertEquals(4, StreakUtils.currentStreak(history))
    }

    @Test
    fun `current streak counts consecutive days ending yesterday`() {
        // Gap before yesterday, so only 2 days (yesterday + day before)
        val history = listOf(today.minusDays(10), today.minusDays(2), yesterday)
        assertEquals(2, StreakUtils.currentStreak(history))
    }

    @Test
    fun `current streak breaks on a missing day`() {
        // today present, yesterday missing, day-before present -> streak is 1 (today only)
        val history = listOf(today.minusDays(5), today.minusDays(2), today)
        assertEquals(1, StreakUtils.currentStreak(history))
    }

    @Test
    fun `current streak is one when only today is completed`() {
        assertEquals(1, StreakUtils.currentStreak(listOf(today)))
    }

    @Test
    fun `longest streak is zero when history is empty`() {
        assertEquals(0, StreakUtils.longestStreak(emptyList()))
    }

    @Test
    fun `longest streak finds the longest run anywhere`() {
        // Two runs: 3 days, then 5 days. Best = 5.
        val run1 = (0..2).map { today.minusDays(20 - it.toLong()) }
        val run2 = (0..4).map { today.minusDays(6 - it.toLong()) }
        val history = (run1 + run2).sorted()
        assertEquals(5, StreakUtils.longestStreak(history))
    }

    @Test
    fun `longest streak ignores duplicate days`() {
        val history = listOf(yesterday, yesterday, today)
        assertEquals(2, StreakUtils.longestStreak(history))
    }
}
