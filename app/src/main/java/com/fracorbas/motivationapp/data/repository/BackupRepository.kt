package com.fracorbas.motivationapp.data.repository

import androidx.room.withTransaction
import com.fracorbas.motivationapp.data.local.HabitCompletionDao
import com.fracorbas.motivationapp.data.local.HabitDao
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.local.TriggerDao
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.HabitCompletion
import com.fracorbas.motivationapp.data.model.Trigger
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Snapshot of all user data for backup/restore.
 */
data class BackupData(
    val habits: List<Habit>,
    val triggers: List<Trigger>,
    val completions: List<HabitCompletion>
)

/**
 * Exports and imports all user data (habits, triggers, completion history) as JSON.
 *
 * Serialization uses [org.json] (available on Android and the JVM) so the mapping
 * logic is unit-testable without Android instrumentation.
 */
@Singleton
class BackupRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val triggerDao: TriggerDao,
    private val completionDao: HabitCompletionDao,
    private val database: HabitDatabase
) {
    /** Read a full snapshot of the data, for export. */
    suspend fun export(): BackupData = BackupData(
        habits = habitDao.getAllHabitsList(),
        triggers = triggerDao.getAllTriggersList(),
        completions = completionDao.getAllCompletionsList()
    )

    /** Serialize a snapshot to a pretty-printed JSON string. */
    fun toJson(data: BackupData): String = BackupJson.toJson(data)

    /** Restore a snapshot, replacing all current data inside a single transaction. */
    suspend fun import(json: String) {
        val data = BackupJson.fromJson(json)
        database.withTransaction {
            completionDao.deleteAllCompletions()
            habitDao.deleteAllHabits()
            triggerDao.deleteAllTriggers()
            // Insert triggers first (habits reference triggerId), then habits, then history.
            triggerDao.insertTriggers(data.triggers)
            data.habits.forEach { habitDao.insertHabit(it) }
            completionDao.insertCompletions(data.completions)
        }
    }
}

/**
 * Pure JSON (de)serialization of [BackupData] using org.json. No Android dependencies,
 * so it can be exercised in plain JVM unit tests.
 */
object BackupJson {

    private const val KEY_VERSION = "version"
    private const val KEY_HABITS = "habits"
    private const val KEY_TRIGGERS = "triggers"
    private const val KEY_COMPLETIONS = "completions"
    private const val CURRENT_VERSION = 1

    fun toJson(data: BackupData): String {
        val root = JSONObject()
        root.put(KEY_VERSION, CURRENT_VERSION)
        root.put(KEY_HABITS, JSONArray().apply {
            data.habits.forEach { put(habitToJson(it)) }
        })
        root.put(KEY_TRIGGERS, JSONArray().apply {
            data.triggers.forEach { put(triggerToJson(it)) }
        })
        root.put(KEY_COMPLETIONS, JSONArray().apply {
            data.completions.forEach { put(completionToJson(it)) }
        })
        return root.toString(2)
    }

    fun fromJson(json: String): BackupData {
        val root = JSONObject(json)
        val habits = root.optJSONArray(KEY_HABITS)?.let { arr ->
            (0 until arr.length()).map { habitFromJson(arr.getJSONObject(it)) }
        } ?: emptyList()
        val triggers = root.optJSONArray(KEY_TRIGGERS)?.let { arr ->
            (0 until arr.length()).map { triggerFromJson(arr.getJSONObject(it)) }
        } ?: emptyList()
        val completions = root.optJSONArray(KEY_COMPLETIONS)?.let { arr ->
            (0 until arr.length()).map { completionFromJson(arr.getJSONObject(it)) }
        } ?: emptyList()
        return BackupData(habits, triggers, completions)
    }

    private fun habitToJson(h: Habit): JSONObject = JSONObject().apply {
        put("id", h.id)
        put("title", h.title)
        h.description?.let { put("description", it) }
        put("trigger", h.trigger)
        h.triggerId?.let { put("triggerId", it) }
        h.reminderTime?.let { put("reminderTime", it.toString()) }
        put("isActive", h.isActive)
        put("createdAt", h.createdAt.toString())
        put("streak", h.streak)
        h.lastCompletedDate?.let { put("lastCompletedDate", it.toString()) }
        put("notificationEnabled", h.notificationEnabled)
        h.notificationFrequency?.let { put("notificationFrequency", it) }
        h.notificationFrequencyUnit?.let { put("notificationFrequencyUnit", it) }
    }

    private fun habitFromJson(o: JSONObject): Habit = Habit(
        id = o.optInt("id", 0),
        title = o.optString("title"),
        description = o.optString("description").ifBlank { null },
        trigger = o.optString("trigger"),
        triggerId = o.optInt("triggerId", 0).takeIf { it != 0 },
        reminderTime = o.optString("reminderTime").ifBlank { null }?.let { LocalTime.parse(it) },
        isActive = o.optBoolean("isActive", true),
        createdAt = o.optString("createdAt").ifBlank { LocalDate.now().toString() }.let { LocalDate.parse(it) },
        streak = o.optInt("streak", 0),
        lastCompletedDate = o.optString("lastCompletedDate").ifBlank { null }?.let { LocalDate.parse(it) },
        notificationEnabled = o.optBoolean("notificationEnabled", false),
        notificationFrequency = o.optInt("notificationFrequency", 0).takeIf { it != 0 },
        notificationFrequencyUnit = o.optString("notificationFrequencyUnit").ifBlank { null }
    )

    private fun triggerToJson(t: Trigger): JSONObject = JSONObject().apply {
        put("id", t.id)
        put("name", t.name)
        t.description?.let { put("description", it) }
        put("isCustom", t.isCustom)
    }

    private fun triggerFromJson(o: JSONObject): Trigger = Trigger(
        id = o.optInt("id", 0),
        name = o.optString("name"),
        description = o.optString("description").ifBlank { null },
        isCustom = o.optBoolean("isCustom", true)
    )

    private fun completionToJson(c: HabitCompletion): JSONObject = JSONObject().apply {
        put("habitId", c.habitId)
        put("completedDate", c.completedDate.toString())
    }

    private fun completionFromJson(o: JSONObject): HabitCompletion = HabitCompletion(
        habitId = o.optInt("habitId", 0),
        completedDate = LocalDate.parse(o.optString("completedDate"))
    )
}
