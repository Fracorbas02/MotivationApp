package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.model.Trigger
import com.fracorbas.motivationapp.data.repository.TriggerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing triggers.
 * 
 * @property repository The trigger repository
 */
@HiltViewModel
class TriggerViewModel @Inject constructor(
    private val repository: TriggerRepository
) : ViewModel() {

    // ==================== State ====================

    /**
     * All triggers
     */
    val allTriggers: StateFlow<List<Trigger>> = repository.getAllTriggers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Default triggers only
     */
    val defaultTriggers: StateFlow<List<Trigger>> = repository.getDefaultTriggers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Custom triggers only
     */
    val customTriggers: StateFlow<List<Trigger>> = repository.getCustomTriggers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * UI Events (navigation, snackbar messages, etc.)
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class NavigateToAddTrigger(val triggerId: Int? = null) : UiEvent()
        object NavigateBack : UiEvent()
    }

    // ==================== Actions ====================

    /**
     * Create a new trigger
     */
    fun createTrigger(name: String, description: String? = null) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiEvent.emit(UiEvent.ShowSnackbar("Le nom du déclencheur ne peut pas être vide"))
            return@launch
        }

        val exists = repository.triggerExistsByName(name)
        if (exists) {
            _uiEvent.emit(UiEvent.ShowSnackbar("Un déclencheur avec ce nom existe déjà"))
            return@launch
        }

        repository.createTrigger(name, description)
        _uiEvent.emit(UiEvent.ShowSnackbar("Déclencheur créé avec succès !"))
        _uiEvent.emit(UiEvent.NavigateBack)
    }

    /**
     * Update an existing trigger
     */
    fun updateTrigger(triggerId: Int, name: String, description: String?) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiEvent.emit(UiEvent.ShowSnackbar("Le nom du déclencheur ne peut pas être vide"))
            return@launch
        }

        val trigger = repository.getTriggerById(triggerId) ?: return@launch
        repository.updateTrigger(trigger.copy(name = name, description = description))
        _uiEvent.emit(UiEvent.ShowSnackbar("Déclencheur mis à jour !"))
        _uiEvent.emit(UiEvent.NavigateBack)
    }

    /**
     * Delete a trigger
     */
    fun deleteTrigger(trigger: Trigger) = viewModelScope.launch {
        repository.deleteTrigger(trigger)
        _uiEvent.emit(UiEvent.ShowSnackbar("Déclencheur supprimé"))
    }

    /**
     * Get a trigger by ID
     */
    suspend fun getTriggerById(id: Int): Trigger? = repository.getTriggerById(id)

    /**
     * Request to add a new trigger
     */
    fun onAddTriggerClick() = viewModelScope.launch {
        _uiEvent.emit(UiEvent.NavigateToAddTrigger())
    }

    /**
     * Request to edit an existing trigger
     */
    fun onEditTriggerClick(triggerId: Int) = viewModelScope.launch {
        _uiEvent.emit(UiEvent.NavigateToAddTrigger(triggerId))
    }
}
