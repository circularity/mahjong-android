package com.ash.mahjong.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreGameSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : GameSettingsRepository {

    override fun observeSettings(): Flow<GameSettings> {
        return dataStore.data.map { preferences ->
            val basePoint = (preferences[BASE_POINT_KEY] ?: GameSettings.DEFAULT_BASE_POINT)
                .coerceIn(GameSettings.MIN_BASE_POINT, GameSettings.MAX_BASE_POINT)
            val cappingFan =
                (preferences[CAPPING_FAN_KEY] ?: GameSettings.DEFAULT_CAPPING_FAN)
                    .coerceIn(
                        GameSettings.MIN_CAPPING_FAN,
                        GameSettings.MAX_CAPPING_FAN
                    )
            val hapticsEnabled =
                preferences[HAPTICS_ENABLED_KEY] ?: GameSettings.DEFAULT_HAPTICS_ENABLED

            GameSettings(
                basePoint = basePoint,
                cappingFan = cappingFan,
                hapticsEnabled = hapticsEnabled
            )
        }
    }

    override suspend fun updateBasePoint(basePoint: Int) {
        dataStore.edit { preferences ->
            preferences[BASE_POINT_KEY] =
                basePoint.coerceIn(GameSettings.MIN_BASE_POINT, GameSettings.MAX_BASE_POINT)
        }
    }

    override suspend fun updateCappingFan(fan: Int) {
        dataStore.edit { preferences ->
            preferences[CAPPING_FAN_KEY] = fan.coerceIn(
                GameSettings.MIN_CAPPING_FAN,
                GameSettings.MAX_CAPPING_FAN
            )
        }
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED_KEY] = enabled
        }
    }

    override suspend fun reset() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val BASE_POINT_KEY = intPreferencesKey("base_point")
        val CAPPING_FAN_KEY = intPreferencesKey("capping_fan")
        val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")
    }
}
