package com.ash.mahjong.feature.battle_score.state

data class HorseUiModel(
    val id: Int,
    val name: String,
    val avatarKey: String,
    val avatarEmoji: String,
    val boundOnTablePlayerName: String?,
    val roundDelta: String,
    val totalScore: String
)
