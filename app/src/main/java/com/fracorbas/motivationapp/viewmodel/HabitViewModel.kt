package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for managing habits in the MotivationApp.
 * 
 * This class contains the business logic and exposes state for the UI.
 * 
 * @property repository The habit repository
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    // ==================== State ====================

    /**
     * All habits in the database
     */
    val allHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Active habits only
     */
    val activeHabits: StateFlow<List<Habit>> = repository.getActiveHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Search query
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Filtered habits based on search query
     */
    val filteredHabits: StateFlow<List<Habit>> = _searchQuery
        .debounce(300) // Wait 300ms after user stops typing
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllHabits()
            } else {
                repository.searchHabits(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Today's completed habits count
     */
    val todayCompletedCount: StateFlow<Int> = repository.getTodayCompletedHabits()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Total active habits count
     */
    val activeHabitsCount: StateFlow<Int> = activeHabits
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * UI Events (navigation, snackbar messages, etc.)
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class NavigateToAddHabit(val habitId: Int? = null) : UiEvent()
        object NavigateBack : UiEvent()
    }

    // ==================== Actions ====================

    /**
     * Update search query
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Create a new habit
     */
    fun createHabit(
        title: String,
        description: String?,
        trigger: String,
        reminderTime: LocalTime?,
        notificationEnabled: Boolean
    ) = viewModelScope.launch {
        val habit = Habit(
            title = title,
            description = description,
            trigger = trigger,
            reminderTime = reminderTime,
            notificationEnabled = notificationEnabled
        )
        repository.createHabit(habit)
        _uiEvent.emit(UiEvent.ShowSnackbar("Habitude créée avec succès !"))
        _uiEvent.emit(UiEvent.NavigateBack)
    }

    /**
     * Update an existing habit
     */
    fun updateHabit(
        id: Int,
        title: String,
        description: String?,
        trigger: String,
        reminderTime: LocalTime?,
        notificationEnabled: Boolean,
        isActive: Boolean
    ) = viewModelScope.launch {
        val habit = repository.getHabitById(id) ?: return@launch
        repository.updateHabit(
            habit.copy(
                title = title,
                description = description,
                trigger = trigger,
                reminderTime = reminderTime,
                notificationEnabled = notificationEnabled,
                isActive = isActive
            )
        )
        _uiEvent.emit(UiEvent.ShowSnackbar("Habitude mise à jour !"))
        _uiEvent.emit(UiEvent.NavigateBack)
    }

    /**
     * Delete a habit
     */
    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        repository.deleteHabit(habit)
        _uiEvent.emit(UiEvent.ShowSnackbar("Habitude supprimée"))
    }

    /**
     * Toggle habit completion status
     */
    fun toggleHabitCompletion(habitId: Int) = viewModelScope.launch {
        repository.toggleHabitCompletion(habitId)
    }

    /**
     * Toggle habit active status
     */
    fun toggleHabitActive(habitId: Int) = viewModelScope.launch {
        repository.toggleHabitActive(habitId)
    }

    /**
     * Toggle notification for a habit
     */
    fun toggleNotification(habitId: Int, enabled: Boolean) = viewModelScope.launch {
        repository.setNotificationEnabled(habitId, enabled)
    }

    /**
     * Get a habit by ID (for edit screen)
     */
    suspend fun getHabitById(id: Int): Habit? = repository.getHabitById(id)

    /**
     * Request to add a new habit
     */
    fun onAddHabitClick() = viewModelScope.launch {
        _uiEvent.emit(UiEvent.NavigateToAddHabit())
    }

    /**
     * Request to edit an existing habit
     */
    fun onEditHabitClick(habitId: Int) = viewModelScope.launch {
        _uiEvent.emit(UiEvent.NavigateToAddHabit(habitId))
    }
}
