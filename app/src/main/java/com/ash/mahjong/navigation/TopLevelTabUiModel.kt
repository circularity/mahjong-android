package com.ash.mahjong.navigation

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes

data class TopLevelTabUiModel(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val selected: Boolean
)
