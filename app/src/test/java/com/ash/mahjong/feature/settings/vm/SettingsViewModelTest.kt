package com.ash.mahjong.feature.settings.vm

import com.ash.mahjong.data.settings.GameSettings
import com.ash.mahjong.feature.settings.intent.SettingsIntent
import com.ash.mahjong.test.fake.FakeGameSettingsRepository
import com.ash.mahjong.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_readsRepositoryValues() = runTest {
        val repository = FakeGameSettingsRepository(
            initialSettings = GameSettings(
                basePoint = 6,
                cappingFan = 5,
                hapticsEnabled = true
            )
        )

        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(6, state.basePoint)
        assertEquals(5, state.cappingFan)
        assertEquals(true, state.hapticsEnabled)
    }

    @Test
    fun stepperBounds_respectMinAndMax() = runTest {
        val repository = FakeGameSettingsRepository(
            initialSettings = GameSettings(basePoint = 1, cappingFan = 10)
        )
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        repeat(10) { viewModel.onIntent(SettingsIntent.OnDecreaseBasePoint) }
        repeat(10) { viewModel.onIntent(SettingsIntent.OnIncreaseCappingFan) }
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.basePoint)
        assertEquals(10, viewModel.uiState.value.cappingFan)
    }

    @Test
    fun clearCache_resetsToDefaults() = runTest {
        val repository = FakeGameSettingsRepository(
            initialSettings = GameSettings(basePoint = 7, cappingFan = 9, hapticsEnabled = true)
        )
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OnClearCacheClick)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(GameSettings.DEFAULT_BASE_POINT, state.basePoint)
        assertEquals(GameSettings.DEFAULT_CAPPING_FAN, state.cappingFan)
        assertEquals(GameSettings.DEFAULT_HAPTICS_ENABLED, state.hapticsEnabled)
    }

    @Test
    fun hapticToggle_updatesRepositoryState() = runTest {
        val repository = FakeGameSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.OnHapticsEnabledChange(true))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hapticsEnabled)
    }
}
