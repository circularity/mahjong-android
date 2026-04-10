package com.ash.mahjong.feature.settings.state

import com.ash.mahjong.data.settings.GameSettings

data class SettingsUiState(
    val basePoint: Int = GameSettings.DEFAULT_BASE_POINT,
    val cappingMultiplier: Int = GameSettings.DEFAULT_CAPPING_MULTIPLIER,
    val hapticsEnabled: Boolean = GameSettings.DEFAULT_HAPTICS_ENABLED,
    val versionName: String = ""
) {
    val canDecreaseBasePoint: Boolean
        get() = basePoint > GameSettings.MIN_BASE_POINT

    val canIncreaseBasePoint: Boolean
        get() = basePoint < GameSettings.MAX_BASE_POINT

    val canDecreaseCappingMultiplier: Boolean
        get() = cappingMultiplier > GameSettings.MIN_CAPPING_MULTIPLIER

    val canIncreaseCappingMultiplier: Boolean
        get() = cappingMultiplier < GameSettings.MAX_CAPPING_MULTIPLIER
}
