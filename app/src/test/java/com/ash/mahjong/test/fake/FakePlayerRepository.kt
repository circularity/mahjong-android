package com.ash.mahjong.test.fake

import com.ash.mahjong.data.player.AddPlayerResult
import com.ash.mahjong.data.player.Player
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerRepository
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePlayerRepository(
    initialPlayers: List<Player> = emptyList()
) : PlayerRepository {

    private var nextId = (initialPlayers.maxOfOrNull { it.id } ?: 0) + 1
    private val playersFlow = MutableStateFlow(initialPlayers.sortedByDescending { it.createdAt })

    override fun observePlayers(): Flow<List<Player>> = playersFlow

    override fun observeRecentPlayers(limit: Int): Flow<List<Player>> {
        return playersFlow.map { players -> players.take(limit) }
    }

    override suspend fun addPlayer(name: String, initialScore: Int): AddPlayerResult {
        val displayName = name.trim()
        if (displayName.isBlank()) {
            return AddPlayerResult.InvalidName
        }

        val normalized = displayName.lowercase(Locale.ROOT)
        val duplicateExists = playersFlow.value.any { player ->
            player.name.trim().lowercase(Locale.ROOT) == normalized
        }
        if (duplicateExists) {
            return AddPlayerResult.DuplicateName
        }

        playersFlow.update { current ->
            listOf(
                Player(
                    id = nextId++,
                    name = displayName,
                    score = initialScore,
                    createdAt = System.currentTimeMillis(),
                    avatarKey = PlayerAnimalAvatarCatalog.randomAvatarKey()
                )
            ) + current
        }
        return AddPlayerResult.Success
    }

    override suspend fun updatePlayerActiveStatus(playerId: Int, isActive: Boolean) {
        val targetPlayer = playersFlow.value.firstOrNull { it.id == playerId } ?: return
        playersFlow.update { current ->
            val updatedPlayers = current.map { player ->
                if (player.id == playerId) player.copy(isActive = isActive) else player
            }
            if (!isActive && targetPlayer.playerRole == PlayerRole.ON_TABLE) {
                updatedPlayers.map { player ->
                    if (player.boundOnTablePlayerId == playerId) {
                        player.copy(boundOnTablePlayerId = null)
                    } else {
                        player
                    }
                }
            } else {
                updatedPlayers
            }
        }
    }

    override suspend fun updatePlayerRole(playerId: Int, role: PlayerRole) {
        val targetPlayer = playersFlow.value.firstOrNull { it.id == playerId } ?: return
        playersFlow.update { current ->
            val roleUpdated = current.map { player ->
                if (player.id == playerId) {
                    player.copy(playerRole = role, boundOnTablePlayerId = null)
                } else {
                    player
                }
            }
            if (role == PlayerRole.HORSE && targetPlayer.playerRole == PlayerRole.ON_TABLE) {
                roleUpdated.map { player ->
                    if (player.boundOnTablePlayerId == playerId) {
                        player.copy(boundOnTablePlayerId = null)
                    } else {
                        player
                    }
                }
            } else {
                roleUpdated
            }
        }
    }

    override suspend fun updateHorseBinding(playerId: Int, boundOnTablePlayerId: Int?) {
        val horsePlayer = playersFlow.value.firstOrNull { it.id == playerId } ?: return
        if (horsePlayer.playerRole != PlayerRole.HORSE) {
            playersFlow.update { players ->
                players.map { player ->
                    if (player.id == playerId) player.copy(boundOnTablePlayerId = null) else player
                }
            }
            return
        }

        val normalizedBinding = when {
            boundOnTablePlayerId == null -> null
            boundOnTablePlayerId == playerId -> null
            else -> {
                val target = playersFlow.value.firstOrNull { it.id == boundOnTablePlayerId }
                if (target?.playerRole == PlayerRole.ON_TABLE && target.isActive) {
                    boundOnTablePlayerId
                } else {
                    null
                }
            }
        }
        playersFlow.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(boundOnTablePlayerId = normalizedBinding)
                } else {
                    player
                }
            }
        }
    }

    override suspend fun swapOnTableWithHorse(onTablePlayerId: Int, horsePlayerId: Int) {
        val onTablePlayer = playersFlow.value.firstOrNull { it.id == onTablePlayerId } ?: return
        val horsePlayer = playersFlow.value.firstOrNull { it.id == horsePlayerId } ?: return
        if (!onTablePlayer.isActive || !horsePlayer.isActive) {
            return
        }
        if (onTablePlayer.playerRole != PlayerRole.ON_TABLE ||
            horsePlayer.playerRole != PlayerRole.HORSE
        ) {
            return
        }

        playersFlow.update { players ->
            players.map { player ->
                when {
                    player.id == onTablePlayerId -> {
                        player.copy(
                            playerRole = PlayerRole.HORSE,
                            boundOnTablePlayerId = null
                        )
                    }

                    player.id == horsePlayerId -> {
                        player.copy(
                            playerRole = PlayerRole.ON_TABLE,
                            boundOnTablePlayerId = null
                        )
                    }

                    player.boundOnTablePlayerId == onTablePlayerId -> {
                        player.copy(boundOnTablePlayerId = null)
                    }

                    else -> player
                }
            }
        }
    }

    override suspend fun updatePlayerAvatar(playerId: Int, avatarKey: String) {
        val normalizedAvatarKey = PlayerAnimalAvatarCatalog.normalizeAvatarKey(avatarKey) ?: return
        playersFlow.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(avatarKey = normalizedAvatarKey)
                } else {
                    player
                }
            }
        }
    }
}
