package com.ash.mahjong.feature.settings.intent

sealed interface SettingsIntent {
    data object OnDecreaseBasePoint : SettingsIntent
    data object OnIncreaseBasePoint : SettingsIntent
    data object OnDecreaseCappingMultiplier : SettingsIntent
    data object OnIncreaseCappingMultiplier : SettingsIntent
    data class OnHapticsEnabledChange(val enabled: Boolean) : SettingsIntent
    data object OnClearCacheClick : SettingsIntent
}
