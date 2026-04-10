package com.ash.mahjong.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ash.mahjong.data.settings.DataStoreGameSettingsRepository
import com.ash.mahjong.data.settings.GameSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsDataModule {

    @Provides
    @Singleton
    fun provideGameSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("game_settings.preferences_pb") }
        )
    }

    @Provides
    @Singleton
    fun provideGameSettingsRepository(
        dataStore: DataStore<Preferences>
    ): GameSettingsRepository {
        return DataStoreGameSettingsRepository(dataStore)
    }
}
