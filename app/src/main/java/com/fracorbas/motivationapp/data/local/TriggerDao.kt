package com.fracorbas.motivationapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fracorbas.motivationapp.data.model.Trigger
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Trigger entities.
 */
@Dao
interface TriggerDao {

    /**
     * Get all triggers, ordered by name
     */
    @Query("SELECT * FROM triggers ORDER BY name ASC")
    fun getAllTriggers(): Flow<List<Trigger>>

    /**
     * Get all triggers (non-Flow version)
     */
    @Query("SELECT * FROM triggers ORDER BY name ASC")
    suspend fun getAllTriggersList(): List<Trigger>

    /**
     * Get a trigger by ID
     */
    @Query("SELECT * FROM triggers WHERE id = :triggerId")
    suspend fun getTriggerById(triggerId: Int): Trigger?

    /**
     * Get custom triggers only
     */
    @Query("SELECT * FROM triggers WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomTriggers(): Flow<List<Trigger>>

    /**
     * Get default triggers only
     */
    @Query("SELECT * FROM triggers WHERE isCustom = 0 ORDER BY name ASC")
    fun getDefaultTriggers(): Flow<List<Trigger>>

    /**
     * Insert a new trigger
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: Trigger): Long

    /**
     * Update an existing trigger
     */
    @Update
    suspend fun updateTrigger(trigger: Trigger)

    /**
     * Delete a trigger
     */
    @Delete
    suspend fun deleteTrigger(trigger: Trigger)

    /**
     * Check if a trigger exists by name
     */
    @Query("SELECT COUNT(*) FROM triggers WHERE name = :name")
    suspend fun triggerExistsByName(name: String): Int

    /**
     * Search triggers by name
     */
    @Query("SELECT * FROM triggers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTriggers(query: String): Flow<List<Trigger>>

    /**
     * Get trigger count
     */
    @Query("SELECT COUNT(*) FROM triggers")
    fun getTriggerCount(): Flow<Int>
}
