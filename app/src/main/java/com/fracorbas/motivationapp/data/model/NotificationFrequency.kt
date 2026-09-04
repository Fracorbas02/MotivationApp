package com.fracorbas.motivationapp.data.model

/**
 * Enum for notification frequency units.
 */
enum class NotificationFrequencyUnit(
    val displayName: String,
    val pluralDisplayName: String,
    val storageKey: String
) {
    DAILY("Jour", "Jours", "days"),
    WEEKLY("Semaine", "Semaines", "weeks"),
    MONTHLY("Mois", "Mois", "months");

    companion object {
        fun fromString(value: String?): NotificationFrequencyUnit? {
            return when (value) {
                "days", "day", "DAILY" -> DAILY
                "weeks", "week", "WEEKLY" -> WEEKLY
                "months", "month", "MONTHLY" -> MONTHLY
                else -> null
            }
        }

        fun getAll(): List<NotificationFrequencyUnit> {
            return listOf(DAILY, WEEKLY, MONTHLY)
        }
    }
}

/**
 * Data class for notification frequency configuration.
 */
data class NotificationFrequency(
    val value: Int,
    val unit: NotificationFrequencyUnit
) {
    fun getDisplayText(): String {
        return if (value == 1) {
            "Tous les $value ${unit.displayName}"
        } else {
            "Tous les $value ${unit.pluralDisplayName}"
        }
    }

    companion object {
        val DAILY = NotificationFrequency(1, NotificationFrequencyUnit.DAILY)
        val EVERY_2_DAYS = NotificationFrequency(2, NotificationFrequencyUnit.DAILY)
        val EVERY_3_DAYS = NotificationFrequency(3, NotificationFrequencyUnit.DAILY)
        val WEEKLY = NotificationFrequency(1, NotificationFrequencyUnit.WEEKLY)
        val EVERY_2_WEEKS = NotificationFrequency(2, NotificationFrequencyUnit.WEEKLY)
        val MONTHLY = NotificationFrequency(1, NotificationFrequencyUnit.MONTHLY)
    }
}
