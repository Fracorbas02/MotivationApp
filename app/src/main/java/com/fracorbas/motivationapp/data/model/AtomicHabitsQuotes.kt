package com.fracorbas.motivationapp.data.model

/**
 * Curated quotes from James Clear's "Atomic Habits", in French.
 * Used by the home screen to surface one idea per day.
 */
object AtomicHabitsQuotes {
    val quotes: List<String> = listOf(
        "Tu ne t'élèves pas au niveau de tes objectifs, tu tombes au niveau de tes systèmes.",
        "Les habitudes sont la composante de l'amélioration continue.",
        "1 % mieux chaque jour, 37 fois mieux en un an.",
        "La réussite est le produit d'habitudes quotidiennes répétées.",
        "Tu deviens ce que tu répètes.",
        "Ne brise pas la chaîne. Ne manque jamais deux jours de suite.",
        "Les habitudes atomiques : petites améliorations, résultats remarquables.",
        "Fais-le tellement petit que tu ne puisse pas dire non.",
        "On ne décide pas son avenir, on décide ses habitudes, et ce sont les habitudes qui décident l'avenir.",
        "L'environnement est l'architecte invisible du comportement.",
        "Rends-le évident, attractif, facile, satisfaisant.",
        "Les habitudes se forment par la répétition, non par la perfection.",
        "Le but n'est pas de tout faire, mais d'éviter de rater deux fois.",
        "Un objectif sert à fixer une direction ; un système sert à progresser.",
        "L'identité précède le résultat : « je suis quelqu'un qui… »."
    )

    /** Quote of the day, deterministic from the date so it stays stable during the day. */
    fun forDay(dayOfYear: Int): String = quotes[(dayOfYear % quotes.size).coerceIn(0, quotes.lastIndex)]
}
