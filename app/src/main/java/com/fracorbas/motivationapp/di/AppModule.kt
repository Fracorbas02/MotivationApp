package com.fracorbas.motivationapp.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.repository.HabitRepository
import com.fracorbas.motivationapp.data.repository.TriggerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance
     */
    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return HabitDatabase.getDatabase(context)
    }

    /**
     * Provides the HabitDao from the database
     */
    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase) = database.habitDao

    /**
     * Provides the TriggerDao from the database
     */
    @Provides
    @Singleton
    fun provideTriggerDao(database: HabitDatabase) = database.triggerDao

    /**
     * Provides the HabitCompletionDao from the database
     */
    @Provides
    @Singleton
    fun provideHabitCompletionDao(database: HabitDatabase) = database.habitCompletionDao

    /**
     * Provides the HabitRepository
     */
    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: com.fracorbas.motivationapp.data.local.HabitDao,
        completionDao: com.fracorbas.motivationapp.data.local.HabitCompletionDao,
        database: HabitDatabase
    ): HabitRepository {
        return HabitRepository(habitDao, completionDao, database)
    }

    /**
     * Provides the TriggerRepository
     */
    @Provides
    @Singleton
    fun provideTriggerRepository(triggerDao: com.fracorbas.motivationapp.data.local.TriggerDao): TriggerRepository {
        return TriggerRepository(triggerDao)
    }

    /**
     * Provides the NotificationManagerCompat
     */
    @Provides
    @Singleton
    fun provideNotificationManagerCompat(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }
}
