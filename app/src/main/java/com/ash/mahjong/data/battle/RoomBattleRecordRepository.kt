package com.ash.mahjong.data.battle

import androidx.room.withTransaction
import com.ash.mahjong.data.battle.local.BattleEventDao
import com.ash.mahjong.data.battle.local.BattleEventDeltaEntity
import com.ash.mahjong.data.battle.local.BattleEventEntity
import com.ash.mahjong.data.battle.local.BattlePlayerStateEntity
import com.ash.mahjong.data.battle.local.BattleSessionDao
import com.ash.mahjong.data.battle.local.BattleSessionEntity
import com.ash.mahjong.data.battle.local.BattleSessionPlayerEntity
import com.ash.mahjong.data.player.local.MahjongDatabase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

class RoomBattleRecordRepository @Inject constructor(
    private val database: MahjongDatabase,
    private val battleSessionDao: BattleSessionDao,
    private val battleEventDao: BattleEventDao
) : BattleRecordRepository {

    override suspend fun persistSettledRound(round: SettledBattleRound) {
        if (round.lineupPlayers.isEmpty() || round.events.isEmpty()) {
            return
        }

        database.withTransaction {
            val session = ensureActiveSession(round)
            var nextEventSeq = session.lastEventSeq + 1
            val now = System.currentTimeMillis()

            round.events.forEach { event ->
                val eventId = battleEventDao.insertEvent(
                    BattleEventEntity(
                        sessionId = session.id,
                        roundNo = round.roundNo,
                        eventSeq = nextEventSeq++,
                        actionGroupId = event.actionGroupId,
                        eventType = event.eventType.name,
                        actorPlayerId = event.actorPlayerId,
                        multiplier = event.multiplier,
                        payloadJson = event.payloadJson,
                        createdAt = now
                    )
                )
                val deltaEntities = event.deltaByPlayerId.map { (playerId, delta) ->
                    BattleEventDeltaEntity(
                        eventId = eventId,
                        playerId = playerId,
                        delta = delta
                    )
                }
                if (deltaEntities.isNotEmpty()) {
                    battleEventDao.insertEventDeltas(deltaEntities)
                }
            }

            val playerStateEntities = round.playerStates.map { state ->
                BattlePlayerStateEntity(
                    sessionId = session.id,
                    playerId = state.playerId,
                    totalScore = state.totalScore,
                    roundDelta = state.roundDelta,
                    status = state.status,
                    winOrder = state.winOrder
                )
            }
            if (playerStateEntities.isNotEmpty()) {
                battleEventDao.upsertPlayerStates(playerStateEntities)
            }

            battleSessionDao.updateSession(
                session.copy(
                    currentRoundNo = round.roundNo + 1,
                    lastEventSeq = nextEventSeq - 1,
                    settledRoundCount = session.settledRoundCount + 1
                )
            )
        }
    }

    override fun observePlayerStats(): Flow<List<PlayerStats>> {
        return combine(
            battleEventDao.observePlayerStatsRows(),
            battleEventDao.observeRecentRoundRows()
        ) { statsRows, recentRows ->
            val recentByPlayer = recentRows
                .groupBy { row -> row.playerId }
                .mapValues { (_, rows) -> rows.take(5).map { row -> row.roundDelta } }

            statsRows.map { row ->
                val totalRounds = row.totalRounds
                val winRounds = row.winRounds.coerceAtMost(totalRounds)
                val winRate = if (totalRounds == 0) {
                    0f
                } else {
                    (winRounds * 100f / totalRounds)
                }
                val avgDelta = if (totalRounds == 0) {
                    0f
                } else {
                    row.totalDelta.toFloat() / totalRounds
                }
                PlayerStats(
                    playerId = row.playerId,
                    name = row.playerName,
                    avatarKey = row.avatarKey,
                    createdAt = row.createdAt,
                    zimoRounds = row.zimoRounds,
                    huRounds = row.huRounds,
                    gangRounds = row.gangRounds,
                    totalRounds = totalRounds,
                    winRounds = winRounds,
                    dianPaoRounds = row.dianPaoRounds,
                    winRate = (winRate * 10f).roundToInt() / 10f,
                    totalDelta = row.totalDelta,
                    avgDelta = (avgDelta * 10f).roundToInt() / 10f,
                    recentRounds = recentByPlayer[row.playerId].orEmpty()
                )
            }
        }
    }

    private suspend fun ensureActiveSession(round: SettledBattleRound): BattleSessionEntity {
        val fingerprint = round.lineupPlayers
            .sortedBy { it.seatIndex }
            .joinToString(separator = "|") { player ->
                "${player.playerId}:${player.playerRole}:${player.seatIndex}"
            }
        val now = System.currentTimeMillis()
        val activeSession = battleSessionDao.getActiveSession()
        if (activeSession != null &&
            activeSession.lineupFingerprint == fingerprint &&
            activeSession.basePoint == round.basePoint &&
            activeSession.cappingMultiplier == round.cappingMultiplier
        ) {
            return activeSession
        }

        if (activeSession != null) {
            battleSessionDao.closeSession(
                sessionId = activeSession.id,
                status = SESSION_STATUS_COMPLETED,
                endedAt = now
            )
        }

        val sessionId = battleSessionDao.insertSession(
            BattleSessionEntity(
                status = SESSION_STATUS_ACTIVE,
                startedAt = now,
                endedAt = null,
                lineupFingerprint = fingerprint,
                basePoint = round.basePoint,
                cappingMultiplier = round.cappingMultiplier,
                currentRoundNo = round.roundNo,
                lastEventSeq = 0,
                settledRoundCount = 0
            )
        )
        battleSessionDao.insertSessionPlayers(
            round.lineupPlayers.map { player ->
                BattleSessionPlayerEntity(
                    sessionId = sessionId,
                    playerId = player.playerId,
                    seatIndex = player.seatIndex,
                    playerRole = player.playerRole,
                    nameSnapshot = player.nameSnapshot,
                    avatarSnapshot = player.avatarKeySnapshot,
                    initialScore = player.initialScore
                )
            }
        )
        return battleSessionDao.getActiveSession()
            ?: error("Unable to load active session after creation")
    }

    companion object {
        private const val SESSION_STATUS_ACTIVE = "ACTIVE"
        private const val SESSION_STATUS_COMPLETED = "COMPLETED"
    }
}
