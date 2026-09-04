package com.fracorbas.motivationapp.data.repository

import com.fracorbas.motivationapp.data.local.AchievementDao
import com.fracorbas.motivationapp.data.local.HabitCompletionDao
import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.model.Achievement
import com.fracorbas.motivationapp.data.model.Badge
import com.fracorbas.motivationapp.data.model.StreakUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    val achievements: Flow<List<Achievement>> = achievementDao.observeAll()

    /**
     * Check all badge conditions and unlock any newly earned badges.
     * Call this after habit creation, completion toggle, or app startup.
     */
    suspend fun checkAndUnlock() {
        val habits = habitDao.getAllHabitsList()
        val today = LocalDate.now()

        // FIRST_HABIT: at least one habit exists
        if (habits.isNotEmpty()) tryUnlock(Badge.FIRST_HABIT, today)

        // STREAK badges: check longest current streak across all habits
        val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
        if (maxStreak >= 7) tryUnlock(Badge.STREAK_7, today)
        if (maxStreak >= 30) tryUnlock(Badge.STREAK_30, today)
        if (maxStreak >= 100) tryUnlock(Badge.STREAK_100, today)

        // COMPLETIONS badges: total completions across all habits
        val totalCompletions = completionDao.getAllCompletionsList().size
        if (totalCompletions >= 50) tryUnlock(Badge.COMPLETIONS_50, today)
        if (totalCompletions >= 200) tryUnlock(Badge.COMPLETIONS_200, today)

        // PERFECT_DAY: all active habits completed today
        val activeHabits = habits.filter { it.isActive }
        if (activeHabits.isNotEmpty() && activeHabits.all { it.lastCompletedDate == today }) {
            tryUnlock(Badge.PERFECT_DAY, today)
        }

        // HABITS_5: 5 or more active habits
        if (activeHabits.size >= 5) tryUnlock(Badge.HABITS_5, today)
    }

    private suspend fun tryUnlock(badge: Badge, date: LocalDate) {
        if (!achievementDao.isUnlocked(badge.badgeId)) {
            achievementDao.insert(Achievement(badgeId = badge.badgeId, unlockedAt = date))
        }
    }
}
