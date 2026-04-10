package com.ash.mahjong.data.player

import android.database.sqlite.SQLiteConstraintException
import com.ash.mahjong.data.player.local.PlayerDao
import com.ash.mahjong.data.player.local.PlayerEntity
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlayerRepository @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun observePlayers(): Flow<List<Player>> {
        return playerDao.observeAllPlayers().map { entities ->
            entities.map(::entityToPlayer)
        }
    }

    override fun observeRecentPlayers(limit: Int): Flow<List<Player>> {
        return playerDao.observeRecentPlayers(limit).map { entities ->
            entities.map(::entityToPlayer)
        }
    }

    override suspend fun addPlayer(name: String, initialScore: Int): AddPlayerResult {
        val displayName = name.trim()
        if (displayName.isBlank()) {
            return AddPlayerResult.InvalidName
        }

        val normalizedName = normalizePlayerName(displayName)
        val entity = PlayerEntity(
            displayName = displayName,
            normalizedName = normalizedName,
            initialScore = initialScore,
            createdAt = System.currentTimeMillis(),
            isActive = true,
            avatarKey = PlayerAnimalAvatarCatalog.randomAvatarKey()
        )

        return try {
            playerDao.insertPlayer(entity)
            AddPlayerResult.Success
        } catch (_: SQLiteConstraintException) {
            AddPlayerResult.DuplicateName
        }
    }

    override suspend fun updatePlayerActiveStatus(playerId: Int, isActive: Boolean) {
        val targetPlayer = playerDao.getPlayerById(playerId) ?: return
        playerDao.updatePlayerActiveStatus(playerId = playerId, isActive = isActive)
        if (!isActive && targetPlayer.playerRole == PlayerRole.ON_TABLE.name) {
            playerDao.clearHorseBindingsByTarget(targetPlayerId = playerId)
        }
    }

    override suspend fun updatePlayerRole(playerId: Int, role: PlayerRole) {
        val targetPlayer = playerDao.getPlayerById(playerId) ?: return

        playerDao.updatePlayerRole(playerId = playerId, playerRole = role.name)
        if (role == PlayerRole.HORSE && targetPlayer.playerRole == PlayerRole.ON_TABLE.name) {
            playerDao.clearHorseBindingsByTarget(targetPlayerId = playerId)
        }
    }

    override suspend fun updateHorseBinding(playerId: Int, boundOnTablePlayerId: Int?) {
        val horsePlayer = playerDao.getPlayerById(playerId) ?: return
        if (horsePlayer.playerRole != PlayerRole.HORSE.name) {
            playerDao.updateHorseBinding(playerId = playerId, boundOnTablePlayerId = null)
            return
        }

        if (boundOnTablePlayerId == null || boundOnTablePlayerId == playerId) {
            playerDao.updateHorseBinding(playerId = playerId, boundOnTablePlayerId = null)
            return
        }

        val targetPlayer = playerDao.getPlayerById(boundOnTablePlayerId)
        val canBind = targetPlayer?.let { player ->
            player.playerRole == PlayerRole.ON_TABLE.name && player.isActive
        } ?: false
        playerDao.updateHorseBinding(
            playerId = playerId,
            boundOnTablePlayerId = if (canBind) boundOnTablePlayerId else null
        )
    }

    override suspend fun updatePlayerAvatar(playerId: Int, avatarKey: String) {
        val normalizedAvatarKey = PlayerAnimalAvatarCatalog.normalizeAvatarKey(avatarKey) ?: return
        val targetPlayer = playerDao.getPlayerById(playerId) ?: return
        if (targetPlayer.avatarKey == normalizedAvatarKey) {
            return
        }
        playerDao.updatePlayerAvatar(playerId = playerId, avatarKey = normalizedAvatarKey)
    }
}

private fun entityToPlayer(entity: PlayerEntity): Player {
    return Player(
        id = entity.id,
        name = entity.displayName,
        score = entity.initialScore,
        createdAt = entity.createdAt,
        isActive = entity.isActive,
        playerRole = parsePlayerRole(entity.playerRole),
        boundOnTablePlayerId = entity.boundOnTablePlayerId,
        avatarKey = PlayerAnimalAvatarCatalog.normalizeAvatarKey(entity.avatarKey)
    )
}

private fun parsePlayerRole(rawRole: String): PlayerRole {
    return PlayerRole.entries.firstOrNull { role -> role.name == rawRole } ?: PlayerRole.ON_TABLE
}

internal fun normalizePlayerName(name: String): String {
    return name.trim().lowercase(Locale.ROOT)
}
