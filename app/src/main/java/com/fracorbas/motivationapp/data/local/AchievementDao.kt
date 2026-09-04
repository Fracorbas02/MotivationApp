package com.fracorbas.motivationapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fracorbas.motivationapp.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: Achievement)

    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE badgeId = :badgeId)")
    suspend fun isUnlocked(badgeId: String): Boolean
}
