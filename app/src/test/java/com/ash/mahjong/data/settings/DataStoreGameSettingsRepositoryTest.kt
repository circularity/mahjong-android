package com.ash.mahjong.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ash.mahjong.test.rules.MainDispatcherRule
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreGameSettingsRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun defaults_whenNoPersistedValues() = runTest {
        val repository = DataStoreGameSettingsRepository(createTestDataStore(this))

        val settings = repository.observeSettings().first()

        assertEquals(GameSettings.DEFAULT_BASE_POINT, settings.basePoint)
        assertEquals(GameSettings.DEFAULT_CAPPING_FAN, settings.cappingFan)
        assertEquals(GameSettings.fanToMultiplier(GameSettings.DEFAULT_CAPPING_FAN), settings.cappingMultiplier)
        assertEquals(GameSettings.DEFAULT_HAPTICS_ENABLED, settings.hapticsEnabled)
    }

    @Test
    fun updates_andReset_workAsExpected() = runTest {
        val repository = DataStoreGameSettingsRepository(createTestDataStore(this))

        repository.updateBasePoint(10)
        repository.updateCappingFan(10)
        repository.updateHapticsEnabled(true)

        val updated = repository.observeSettings().first()
        assertEquals(10, updated.basePoint)
        assertEquals(10, updated.cappingFan)
        assertEquals(1024, updated.cappingMultiplier)
        assertEquals(true, updated.hapticsEnabled)

        repository.reset()

        val reset = repository.observeSettings().first()
        assertEquals(GameSettings.DEFAULT_BASE_POINT, reset.basePoint)
        assertEquals(GameSettings.DEFAULT_CAPPING_FAN, reset.cappingFan)
        assertEquals(GameSettings.fanToMultiplier(GameSettings.DEFAULT_CAPPING_FAN), reset.cappingMultiplier)
        assertEquals(GameSettings.DEFAULT_HAPTICS_ENABLED, reset.hapticsEnabled)
    }

    @Test
    fun legacyMultiplierKey_isIgnoredAndFallsBackToDefaultFan() = runTest {
        val dataStore = createTestDataStore(this)
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("capping_multiplier")] = 32
        }
        val repository = DataStoreGameSettingsRepository(dataStore)

        val settings = repository.observeSettings().first()

        assertEquals(GameSettings.DEFAULT_CAPPING_FAN, settings.cappingFan)
        assertEquals(GameSettings.fanToMultiplier(GameSettings.DEFAULT_CAPPING_FAN), settings.cappingMultiplier)
    }

    private fun createTestDataStore(testScope: TestScope) = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = {
            File.createTempFile("settings", ".preferences_pb").apply {
                deleteOnExit()
            }
        }
    )
}
