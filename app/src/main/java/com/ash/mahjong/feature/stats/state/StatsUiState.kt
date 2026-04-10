package com.ash.mahjong.feature.stats.state

data class StatsUiState(
    val players: List<PlayerStatsUiModel> = emptyList()
)

data class PlayerStatsUiModel(
    val playerId: Int,
    val name: String,
    val avatarKey: String,
    val avatarEmoji: String,
    val zimoRounds: Int,
    val huRounds: Int,
    val gangRounds: Int,
    val totalRounds: Int,
    val winRounds: Int,
    val dianPaoRounds: Int,
    val winRateProgress: Float,
    val winRateText: String,
    val totalDelta: Int,
    val totalDeltaText: String,
    val avgDeltaText: String,
    val recentRoundsText: String
)
