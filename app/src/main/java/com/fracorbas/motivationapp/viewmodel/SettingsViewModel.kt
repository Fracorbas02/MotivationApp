package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.repository.Settings
import com.fracorbas.motivationapp.data.repository.SettingsRepository
import com.fracorbas.motivationapp.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<Settings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        repository.setDynamicColor(enabled)
    }

    fun setDailyReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setDailyReminderEnabled(enabled)
    }
}
