package com.ash.mahjong.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class MahjongShapes(
    val playerCard: RoundedCornerShape = RoundedCornerShape(24.dp),
    val dealerBadge: RoundedCornerShape = RoundedCornerShape(50.dp),
    val primaryAction: RoundedCornerShape = RoundedCornerShape(12.dp),
    val secondaryAction: RoundedCornerShape = RoundedCornerShape(10.dp),
    val logCard: RoundedCornerShape = RoundedCornerShape(18.dp),
    val logDot: RoundedCornerShape = RoundedCornerShape(8.dp)
)

val LocalMahjongShapes = staticCompositionLocalOf { MahjongShapes() }
