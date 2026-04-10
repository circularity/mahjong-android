package com.ash.mahjong.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppRoute : NavKey {
    @Serializable
    data object BattleScore : AppRoute

    @Serializable
    data object Stats : AppRoute

    @Serializable
    data object Players : AppRoute

    @Serializable
    data object Settings : AppRoute
}
