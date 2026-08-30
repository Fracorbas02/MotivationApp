package com.fracorbas.motivationapp.data.repository

import com.fracorbas.motivationapp.data.local.TriggerDao
import com.fracorbas.motivationapp.data.model.Trigger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for trigger-related operations.
 * 
 * This class abstracts the data layer and provides a clean API for triggers.
 * 
 * @property triggerDao Data Access Object for triggers
 */
class TriggerRepository @Inject constructor(
    private val triggerDao: TriggerDao
) {

    /**
     * Get all triggers as a flow (observes changes)
     */
    fun getAllTriggers(): Flow<List<Trigger>> = triggerDao.getAllTriggers()

    /**
     * Get all triggers as a list (one-time query)
     */
    suspend fun getAllTriggersList(): List<Trigger> = triggerDao.getAllTriggersList()

    /**
     * Get a trigger by its ID
     */
    suspend fun getTriggerById(id: Int): Trigger? = triggerDao.getTriggerById(id)

    /**
     * Get custom triggers only
     */
    fun getCustomTriggers(): Flow<List<Trigger>> = triggerDao.getCustomTriggers()

    /**
     * Get default triggers only
     */
    fun getDefaultTriggers(): Flow<List<Trigger>> = triggerDao.getDefaultTriggers()

    /**
     * Create a new trigger
     */
    suspend fun createTrigger(name: String, description: String? = null): Long {
        return triggerDao.insertTrigger(
            Trigger(
                name = name,
                description = description,
                isCustom = true
            )
        )
    }

    /**
     * Update an existing trigger
     */
    suspend fun updateTrigger(trigger: Trigger) = triggerDao.updateTrigger(trigger)

    /**
     * Delete a trigger
     */
    suspend fun deleteTrigger(trigger: Trigger) = triggerDao.deleteTrigger(trigger)

    /**
     * Check if a trigger exists by name
     */
    suspend fun triggerExistsByName(name: String): Boolean {
        return triggerDao.triggerExistsByName(name) > 0
    }

    /**
     * Search triggers by query
     */
    fun searchTriggers(query: String): Flow<List<Trigger>> = triggerDao.searchTriggers(query)

    /**
     * Get trigger count
     */
    fun getTriggerCount(): Flow<Int> = triggerDao.getTriggerCount()

    /**
     * Get trigger by name (for quick lookup)
     */
    suspend fun getTriggerByName(name: String): Trigger? {
        val triggers = triggerDao.getAllTriggersList()
        return triggers.find { it.name == name }
    }
}
