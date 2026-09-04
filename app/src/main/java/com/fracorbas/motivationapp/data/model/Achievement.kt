package com.fracorbas.motivationapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A badge earned by the user for reaching a milestone.
 * Stored so we can display unlocked vs locked badges and their unlock date.
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val badgeId: String,
    val unlockedAt: java.time.LocalDate
)

/**
 * Catalog of all possible badges. The [badgeId] is the stable key stored in Room.
 */
enum class Badge(
    val badgeId: String,
    val title: String,
    val description: String,
    val icon: String
) {
    FIRST_HABIT("first_habit", "Premier pas", "Créer ta première habitude", "🌱"),
    STREAK_7("streak_7", "Une semaine", "7 jours consécutifs sur une habitude", "🔥"),
    STREAK_30("streak_30", "Un mois", "30 jours consécutifs sur une habitude", "⚡"),
    STREAK_100("streak_100", "Centenaire", "100 jours consécutifs sur une habitude", "💯"),
    COMPLETIONS_50("completions_50", "Régulier", "50 complétions au total", "📅"),
    COMPLETIONS_200("completions_200", "Assidu", "200 complétions au total", "📊"),
    PERFECT_DAY("perfect_day", "Journée parfaite", "Compléter toutes tes habitudes en un jour", "✨"),
    HABITS_5("habits_5", "Collectionneur", "Avoir 5 habitudes actives", "📚");

    companion object {
        val ALL = entries.toList()
    }
}
