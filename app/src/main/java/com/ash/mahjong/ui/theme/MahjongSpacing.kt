package com.ash.mahjong.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MahjongSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val avatarSize: Dp = 56.dp,
    val logDot: Dp = 18.dp,
    val topBarHeight: Dp = 64.dp,
    val secondaryButtonHeight: Dp = 36.dp
)

val LocalMahjongSpacing = staticCompositionLocalOf { MahjongSpacing() }
