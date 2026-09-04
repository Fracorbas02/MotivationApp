package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Achievement
import com.fracorbas.motivationapp.data.model.Badge
import com.fracorbas.motivationapp.data.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BadgeUiState(
    val badge: Badge,
    val unlocked: Boolean,
    val unlockedAt: java.time.LocalDate? = null
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val repository: AchievementRepository
) : ViewModel() {

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badgeStates: StateFlow<List<BadgeUiState>> = repository.achievements
        .map { unlocked ->
            val unlockedMap = unlocked.associateBy { it.badgeId }
            Badge.ALL.map { badge ->
                val ach = unlockedMap[badge.badgeId]
                BadgeUiState(
                    badge = badge,
                    unlocked = ach != null,
                    unlockedAt = ach?.unlockedAt
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repository.checkAndUnlock() }
    }

    fun checkBadges() {
        viewModelScope.launch { repository.checkAndUnlock() }
    }
}
