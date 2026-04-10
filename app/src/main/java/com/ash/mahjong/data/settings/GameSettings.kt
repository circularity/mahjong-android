package com.ash.mahjong.data.settings

data class GameSettings(
    val basePoint: Int = DEFAULT_BASE_POINT,
    val cappingMultiplier: Int = DEFAULT_CAPPING_MULTIPLIER,
    val hapticsEnabled: Boolean = DEFAULT_HAPTICS_ENABLED
) {
    companion object {
        const val DEFAULT_BASE_POINT = 1
        const val DEFAULT_CAPPING_MULTIPLIER = 8
        const val DEFAULT_HAPTICS_ENABLED = false

        const val MIN_BASE_POINT = 1
        const val MAX_BASE_POINT = 10

        const val MIN_CAPPING_MULTIPLIER = 1
        const val MAX_CAPPING_MULTIPLIER = 32
    }
}
