package com.ash.mahjong.data.settings

import kotlinx.coroutines.flow.Flow

interface GameSettingsRepository {
    fun observeSettings(): Flow<GameSettings>

    suspend fun updateBasePoint(basePoint: Int)

    suspend fun updateCappingFan(fan: Int)

    suspend fun updateHapticsEnabled(enabled: Boolean)

    suspend fun reset()
}
