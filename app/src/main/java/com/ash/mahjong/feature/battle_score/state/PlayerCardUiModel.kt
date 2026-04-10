package com.ash.mahjong.feature.battle_score.state

data class PlayerCardUiModel(
    val id: Int,
    val name: String,
    val avatarKey: String,
    val avatarEmoji: String,
    val roundDelta: String,
    val totalScore: String,
    val isDealer: Boolean,
    val status: PlayerStatus,
    val winOrder: Int?,
    val boundHorseNames: List<String> = emptyList()
)
