package com.ash.mahjong.test.fake

import com.ash.mahjong.data.settings.GameSettings
import com.ash.mahjong.data.settings.GameSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeGameSettingsRepository(
    initialSettings: GameSettings = GameSettings()
) : GameSettingsRepository {

    private val settingsFlow = MutableStateFlow(initialSettings)

    override fun observeSettings(): Flow<GameSettings> = settingsFlow

    override suspend fun updateBasePoint(basePoint: Int) {
        settingsFlow.update { state ->
            state.copy(
                basePoint = basePoint.coerceIn(
                    GameSettings.MIN_BASE_POINT,
                    GameSettings.MAX_BASE_POINT
                )
            )
        }
    }

    override suspend fun updateCappingFan(fan: Int) {
        settingsFlow.update { state ->
            state.copy(
                cappingFan = fan.coerceIn(
                    GameSettings.MIN_CAPPING_FAN,
                    GameSettings.MAX_CAPPING_FAN
                )
            )
        }
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        settingsFlow.update { state ->
            state.copy(hapticsEnabled = enabled)
        }
    }

    override suspend fun reset() {
        settingsFlow.value = GameSettings()
    }
}
