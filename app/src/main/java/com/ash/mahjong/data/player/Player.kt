package com.ash.mahjong.data.player

data class Player(
    val id: Int,
    val name: String,
    val score: Int,
    val createdAt: Long,
    val isActive: Boolean = true,
    val playerRole: PlayerRole = PlayerRole.ON_TABLE,
    val boundOnTablePlayerId: Int? = null,
    val avatarKey: String? = null
)
