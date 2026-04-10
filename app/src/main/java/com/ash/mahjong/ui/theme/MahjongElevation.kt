package com.ash.mahjong.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MahjongElevation(
    val card: Dp = 4.dp,
    val logCard: Dp = 1.dp,
    val bar: Dp = 6.dp
)

val LocalMahjongElevation = staticCompositionLocalOf { MahjongElevation() }
