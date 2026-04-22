package com.ash.mahjong.feature.settings.state

import com.ash.mahjong.data.settings.GameSettings

data class SettingsUiState(
    val basePoint: Int = GameSettings.DEFAULT_BASE_POINT,
    val cappingFan: Int = GameSettings.DEFAULT_CAPPING_FAN,
    val hapticsEnabled: Boolean = GameSettings.DEFAULT_HAPTICS_ENABLED,
    val versionName: String = ""
) {
    val canDecreaseBasePoint: Boolean
        get() = basePoint > GameSettings.MIN_BASE_POINT

    val canIncreaseBasePoint: Boolean
        get() = basePoint < GameSettings.MAX_BASE_POINT

    val canDecreaseCappingFan: Boolean
        get() = cappingFan > GameSettings.MIN_CAPPING_FAN

    val canIncreaseCappingFan: Boolean
        get() = cappingFan < GameSettings.MAX_CAPPING_FAN
}
