package com.fracorbas.motivationapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a trigger for habits.
 * Triggers are common moments or events that can trigger a habit.
 * 
 * @param id Unique identifier
 * @param name The name of the trigger (e.g., "Après le café du matin", "Pause de midi")
 * @param description Optional description
 * @param isCustom Whether this is a user-created trigger
 */
@Entity(tableName = "triggers")
data class Trigger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    val isCustom: Boolean = true
) {
    // Default triggers
    companion object {
        fun getDefaultTriggers(): List<Trigger> {
            return listOf(
                Trigger(id = 0, name = "Après le réveil", description = "Dès que je me réveille", isCustom = false),
                Trigger(id = 0, name = "Après le café du matin", description = "Après mon premier café", isCustom = false),
                Trigger(id = 0, name = "Avant de partir travailler", description = "Avant de quitter la maison", isCustom = false),
                Trigger(id = 0, name = "Pause de midi", description = "Pendant la pause déjeuner", isCustom = false),
                Trigger(id = 0, name = "Après le travail", description = "Quand je rentre à la maison", isCustom = false),
                Trigger(id = 0, name = "Avant le dîner", description = "Avant de manger le soir", isCustom = false),
                Trigger(id = 0, name = "Avant de dormir", description = "Avant d'aller me coucher", isCustom = false),
                Trigger(id = 0, name = "Après manger", description = "Après un repas", isCustom = false),
                Trigger(id = 0, name = "En arrivant au travail", description = "Quand j'arrive au bureau", isCustom = false),
                Trigger(id = 0, name = "Après une réunion", description = "À la fin d'une réunion", isCustom = false)
            )
        }
    }
}
