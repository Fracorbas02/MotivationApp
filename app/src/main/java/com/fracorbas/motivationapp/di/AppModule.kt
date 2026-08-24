package com.fracorbas.motivationapp.di

import android.content.Context
import androidx.room.Room
import com.fracorbas.motivationapp.data.local.HabitDatabase
import com.fracorbas.motivationapp.data.repository.HabitRepository
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
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "motivation_app_db"
        ).build()
    }

    /**
     * Provides the HabitDao from the database
     */
    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase) = database.habitDao

    /**
     * Provides the HabitRepository
     */
    @Provides
    @Singleton
    fun provideHabitRepository(habitDao: com.fracorbas.motivationapp.data.local.HabitDao): HabitRepository {
        return HabitRepository(habitDao)
    }
}
