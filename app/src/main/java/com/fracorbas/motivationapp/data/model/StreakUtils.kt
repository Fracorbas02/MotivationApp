package com.fracorbas.motivationapp.data.model

import java.time.LocalDate

/**
 * Pure streak computation, extracted for testability (no Android/Room dependencies).
 *
 * A "streak" is the number of consecutive days ending today or yesterday.
 */
object StreakUtils {

    /**
     * Compute the current streak from a sorted (ascending) list of completion dates.
     *
     * The streak is live if the most recent completion is today or yesterday; it
     * counts consecutive days backwards from there. Returns 0 if the list is empty
     * or the most recent completion is older than yesterday.
     */
    fun currentStreak(history: List<LocalDate>): Int {
        if (history.isEmpty()) return 0
        val last = history.last()
        val today = LocalDate.now()
        if (last != today && last != today.minusDays(1)) return 0

        var streak = 0
        var expected = last
        for (i in history.indices.reversed()) {
            when {
                history[i] == expected -> {
                    streak++
                    expected = expected.minusDays(1)
                }
                history[i].isAfter(expected) -> Unit // skip stray future entry
                else -> break
            }
        }
        return streak
    }

    /**
     * Longest run of consecutive days in [history], regardless of whether it is live.
     */
    fun longestStreak(history: List<LocalDate>): Int {
        if (history.isEmpty()) return 0
        val sorted = history.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].plusDays(1)) {
                run++
                if (run > best) best = run
            } else if (sorted[i] != sorted[i - 1]) {
                run = 1
            }
        }
        return best
    }
}
