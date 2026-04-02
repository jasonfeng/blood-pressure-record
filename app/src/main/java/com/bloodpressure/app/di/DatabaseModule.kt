package com.bloodpressure.app.di

import android.content.Context
import androidx.room.Room
import com.bloodpressure.app.data.local.BloodPressureDatabase
import com.bloodpressure.app.data.local.dao.BloodPressureDao
import com.bloodpressure.app.data.preferences.SettingsRepository
import com.bloodpressure.app.data.remote.FeishuService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BloodPressureDatabase {
        return Room.databaseBuilder(
            context,
            BloodPressureDatabase::class.java,
            "blood_pressure_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBloodPressureDao(database: BloodPressureDatabase): BloodPressureDao {
        return database.bloodPressureDao()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideFeishuService(): FeishuService {
        return FeishuService()
    }
}
