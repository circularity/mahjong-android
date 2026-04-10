package com.ash.mahjong.ui.theme

import androidx.compose.runtime.Composable

object MahjongDesign {
    val spacing: MahjongSpacing
        @Composable get() = LocalMahjongSpacing.current

    val shapes: MahjongShapes
        @Composable get() = LocalMahjongShapes.current

    val elevation: MahjongElevation
        @Composable get() = LocalMahjongElevation.current
}
