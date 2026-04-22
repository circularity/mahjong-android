package com.ash.mahjong.data.settings

data class GameSettings(
    val basePoint: Int = DEFAULT_BASE_POINT,
    val cappingFan: Int = DEFAULT_CAPPING_FAN,
    val hapticsEnabled: Boolean = DEFAULT_HAPTICS_ENABLED
) {
    val cappingMultiplier: Int
        get() = fanToMultiplier(cappingFan)

    companion object {
        const val DEFAULT_BASE_POINT = 1
        const val DEFAULT_CAPPING_FAN = 3
        const val DEFAULT_HAPTICS_ENABLED = false

        const val MIN_BASE_POINT = 1
        const val MAX_BASE_POINT = 10

        const val MIN_CAPPING_FAN = 1
        const val MAX_CAPPING_FAN = 10

        fun fanToMultiplier(fan: Int): Int {
            val normalizedFan = fan.coerceIn(MIN_CAPPING_FAN, MAX_CAPPING_FAN)
            return 1 shl normalizedFan
        }
    }
}
