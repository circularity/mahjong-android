package com.ash.mahjong.data.player

import android.database.sqlite.SQLiteConstraintException
import com.ash.mahjong.data.player.local.PlayerDao
import com.ash.mahjong.data.player.local.PlayerEntity
import com.ash.mahjong.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomPlayerRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addPlayer_withBlankName_returnsInvalidName() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())

        val result = repository.addPlayer("   ", 100)

        assertEquals(AddPlayerResult.InvalidName, result)
        assertTrue(repository.observePlayers().first().isEmpty())
    }

    @Test
    fun addPlayer_withDuplicateName_returnsDuplicateName() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())
        repository.addPlayer("Alice", 100)

        val result = repository.addPlayer(" alice ", 110)

        assertEquals(AddPlayerResult.DuplicateName, result)
        assertEquals(1, repository.observePlayers().first().size)
    }

    @Test
    fun addPlayer_success_emitsTrimmedPlayer() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())

        val result = repository.addPlayer("  Bob  ", 120)
        val players = repository.observePlayers().first()

        assertEquals(AddPlayerResult.Success, result)
        assertEquals(1, players.size)
        assertEquals("Bob", players.first().name)
        assertEquals(120, players.first().score)
        assertNotNull(players.first().avatarKey)
    }

    @Test
    fun updatePlayerActiveStatus_updatesPlayerState() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())
        repository.addPlayer("Cindy", 100)

        val insertedPlayerId = repository.observePlayers().first().first().id
        repository.updatePlayerActiveStatus(playerId = insertedPlayerId, isActive = false)

        val players = repository.observePlayers().first()
        assertEquals(1, players.size)
        assertEquals(false, players.first().isActive)
    }

    @Test
    fun addPlayer_defaultsToOnTableAndNoBinding() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())

        repository.addPlayer("Dylan", 130)
        val insertedPlayer = repository.observePlayers().first().first()

        assertEquals(PlayerRole.ON_TABLE, insertedPlayer.playerRole)
        assertNull(insertedPlayer.boundOnTablePlayerId)
    }

    @Test
    fun updatePlayerRole_toHorseAndBackToOnTable_clearsOwnBinding() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())
        repository.addPlayer("A", 100)
        repository.addPlayer("B", 100)
        val playersByName = repository.observePlayers().first().associateBy { it.name }
        val onTablePlayerId = playersByName.getValue("A").id
        val horsePlayerId = playersByName.getValue("B").id

        repository.updatePlayerRole(horsePlayerId, PlayerRole.HORSE)
        repository.updateHorseBinding(horsePlayerId, onTablePlayerId)
        assertEquals(
            onTablePlayerId,
            repository.observePlayers().first().first { it.id == horsePlayerId }.boundOnTablePlayerId
        )

        repository.updatePlayerRole(horsePlayerId, PlayerRole.ON_TABLE)
        val playerAfterOnTable = repository.observePlayers().first().first { it.id == horsePlayerId }
        assertEquals(PlayerRole.ON_TABLE, playerAfterOnTable.playerRole)
        assertNull(playerAfterOnTable.boundOnTablePlayerId)

        repository.updatePlayerRole(horsePlayerId, PlayerRole.HORSE)
        val playerAfterHorseAgain = repository.observePlayers().first().first { it.id == horsePlayerId }
        assertEquals(PlayerRole.HORSE, playerAfterHorseAgain.playerRole)
        assertNull(playerAfterHorseAgain.boundOnTablePlayerId)
    }

    @Test
    fun targetBecomesHorseOrInactive_clearsHorseBindingsPointingToTarget() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())
        repository.addPlayer("Target", 100)
        repository.addPlayer("Horse", 100)
        val playersByName = repository.observePlayers().first().associateBy { it.name }
        val targetId = playersByName.getValue("Target").id
        val horseId = playersByName.getValue("Horse").id

        repository.updatePlayerRole(horseId, PlayerRole.HORSE)
        repository.updateHorseBinding(horseId, targetId)
        assertEquals(
            targetId,
            repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId
        )

        repository.updatePlayerRole(targetId, PlayerRole.HORSE)
        assertNull(repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId)

        repository.updatePlayerRole(targetId, PlayerRole.ON_TABLE)
        repository.updateHorseBinding(horseId, targetId)
        assertEquals(
            targetId,
            repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId
        )

        repository.updatePlayerActiveStatus(targetId, false)
        assertNull(repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId)
    }

    @Test
    fun updateHorseBinding_invalidTarget_clearsBinding() = runTest {
        val repository = RoomPlayerRepository(FakePlayerDao())
        repository.addPlayer("Target", 100)
        repository.addPlayer("Horse", 100)
        val playersByName = repository.observePlayers().first().associateBy { it.name }
        val targetId = playersByName.getValue("Target").id
        val horseId = playersByName.getValue("Horse").id

        repository.updatePlayerRole(horseId, PlayerRole.HORSE)
        repository.updateHorseBinding(horseId, targetId)
        assertEquals(
            targetId,
            repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId
        )

        repository.updateHorseBinding(horseId, horseId)
        assertNull(repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId)

        repository.updateHorseBinding(horseId, targetId)
        repository.updatePlayerRole(targetId, PlayerRole.HORSE)
        repository.updateHorseBinding(horseId, targetId)
        assertNull(repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId)

        repository.updatePlayerRole(targetId, PlayerRole.ON_TABLE)
        repository.updatePlayerActiveStatus(targetId, false)
        repository.updateHorseBinding(horseId, targetId)
        assertNull(repository.observePlayers().first().first { it.id == horseId }.boundOnTablePlayerId)
    }
}

private class FakePlayerDao : PlayerDao {
    private var nextId = 1
    private val entities = MutableStateFlow<List<PlayerEntity>>(emptyList())

    override fun observeAllPlayers(): Flow<List<PlayerEntity>> = entities

    override fun observeRecentPlayers(limit: Int): Flow<List<PlayerEntity>> {
        return entities.map { players -> players.take(limit) }
    }

    override suspend fun insertPlayer(entity: PlayerEntity): Long {
        val duplicateExists = entities.value.any { it.normalizedName == entity.normalizedName }
        if (duplicateExists) {
            throw SQLiteConstraintException("duplicate normalized_name")
        }
        val persisted = entity.copy(id = nextId++)
        entities.update { current ->
            (current + persisted).sortedByDescending { it.createdAt }
        }
        return persisted.id.toLong()
    }

    override suspend fun updatePlayerActiveStatus(playerId: Int, isActive: Boolean) {
        entities.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(isActive = isActive)
                } else {
                    player
                }
            }
        }
    }

    override suspend fun getPlayerById(playerId: Int): PlayerEntity? {
        return entities.value.firstOrNull { it.id == playerId }
    }

    override suspend fun updatePlayerRole(playerId: Int, playerRole: String) {
        entities.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(playerRole = playerRole, boundOnTablePlayerId = null)
                } else {
                    player
                }
            }
        }
    }

    override suspend fun updateHorseBinding(playerId: Int, boundOnTablePlayerId: Int?) {
        entities.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(boundOnTablePlayerId = boundOnTablePlayerId)
                } else {
                    player
                }
            }
        }
    }

    override suspend fun clearHorseBindingsByTarget(targetPlayerId: Int) {
        entities.update { players ->
            players.map { player ->
                if (player.boundOnTablePlayerId == targetPlayerId) {
                    player.copy(boundOnTablePlayerId = null)
                } else {
                    player
                }
            }
        }
    }

    override suspend fun updatePlayerAvatar(playerId: Int, avatarKey: String) {
        entities.update { players ->
            players.map { player ->
                if (player.id == playerId) {
                    player.copy(avatarKey = avatarKey)
                } else {
                    player
                }
            }
        }
    }
}
