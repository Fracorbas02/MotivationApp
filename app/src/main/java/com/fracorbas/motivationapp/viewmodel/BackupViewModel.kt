package com.fracorbas.motivationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fracorbas.motivationapp.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    sealed class Event {
        data class ShowMessage(val message: String) : Event()
    }

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /** Build the backup JSON string (run off the main thread). */
    suspend fun exportJson(): String =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            backupRepository.toJson(backupRepository.export())
        }

    /** Replace all data with the parsed snapshot. */
    fun import(json: String) = viewModelScope.launch {
        try {
            kotlinx.coroutines.withContext(Dispatchers.IO) { backupRepository.import(json) }
            _events.emit(Event.ShowMessage("Import réussi"))
        } catch (e: Exception) {
            _events.emit(Event.ShowMessage("Échec de l'import : ${e.message ?: "fichier invalide"}"))
        }
    }
}
