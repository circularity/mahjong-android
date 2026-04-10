package com.ash.mahjong.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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
        assertEquals(GameSettings.DEFAULT_CAPPING_MULTIPLIER, settings.cappingMultiplier)
        assertEquals(GameSettings.DEFAULT_HAPTICS_ENABLED, settings.hapticsEnabled)
    }

    @Test
    fun updates_andReset_workAsExpected() = runTest {
        val repository = DataStoreGameSettingsRepository(createTestDataStore(this))

        repository.updateBasePoint(10)
        repository.updateCappingMultiplier(20)
        repository.updateHapticsEnabled(true)

        val updated = repository.observeSettings().first()
        assertEquals(10, updated.basePoint)
        assertEquals(20, updated.cappingMultiplier)
        assertEquals(true, updated.hapticsEnabled)

        repository.reset()

        val reset = repository.observeSettings().first()
        assertEquals(GameSettings.DEFAULT_BASE_POINT, reset.basePoint)
        assertEquals(GameSettings.DEFAULT_CAPPING_MULTIPLIER, reset.cappingMultiplier)
        assertEquals(GameSettings.DEFAULT_HAPTICS_ENABLED, reset.hapticsEnabled)
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
