package com.ash.mahjong.data.player

import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observePlayers(): Flow<List<Player>>

    fun observeRecentPlayers(limit: Int): Flow<List<Player>>

    suspend fun addPlayer(
        name: String,
        initialScore: Int,
        avatarKey: String? = null
    ): AddPlayerResult

    suspend fun updatePlayerProfile(
        playerId: Int,
        name: String,
        initialScore: Int,
        avatarKey: String? = null
    ): AddPlayerResult

    suspend fun updatePlayerActiveStatus(
        playerId: Int,
        isActive: Boolean
    )

    suspend fun updatePlayerRole(
        playerId: Int,
        role: PlayerRole
    )

    suspend fun updateHorseBinding(
        playerId: Int,
        boundOnTablePlayerId: Int?
    )

    suspend fun swapOnTableWithHorse(
        onTablePlayerId: Int,
        horsePlayerId: Int
    )

    suspend fun updatePlayerAvatar(
        playerId: Int,
        avatarKey: String
    )
}
