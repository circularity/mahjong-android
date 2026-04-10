package com.ash.mahjong.data.battle

import kotlinx.coroutines.flow.Flow

interface BattleRecordRepository {
    suspend fun persistSettledRound(round: SettledBattleRound)

    fun observePlayerStats(): Flow<List<PlayerStats>>
}

data class SettledBattleRound(
    val roundNo: Int,
    val lineupPlayers: List<SessionLineupPlayer>,
    val basePoint: Int,
    val cappingMultiplier: Int,
    val events: List<SettledBattleEvent>,
    val playerStates: List<SettledPlayerState>
)

data class SessionLineupPlayer(
    val playerId: Int,
    val seatIndex: Int,
    val playerRole: String,
    val nameSnapshot: String,
    val avatarKeySnapshot: String?,
    val initialScore: Int
)

data class SettledBattleEvent(
    val actionGroupId: Long,
    val eventType: PersistedBattleEventType,
    val actorPlayerId: Int?,
    val multiplier: Int?,
    val payloadJson: String?,
    val deltaByPlayerId: Map<Int, Int>
)

data class SettledPlayerState(
    val playerId: Int,
    val totalScore: Int,
    val roundDelta: Int,
    val status: String,
    val winOrder: Int?
)

data class PlayerStats(
    val playerId: Int,
    val name: String,
    val avatarKey: String?,
    val createdAt: Long,
    val zimoRounds: Int,
    val huRounds: Int,
    val gangRounds: Int,
    val totalRounds: Int,
    val winRounds: Int,
    val dianPaoRounds: Int,
    val winRate: Float,
    val totalDelta: Int,
    val avgDelta: Float,
    val recentRounds: List<Int>
)

object NoOpBattleRecordRepository : BattleRecordRepository {
    override suspend fun persistSettledRound(round: SettledBattleRound) = Unit

    override fun observePlayerStats(): Flow<List<PlayerStats>> {
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
}
