package com.ash.mahjong.data.battle.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BattleEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(entity: BattleEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEventDeltas(entities: List<BattleEventDeltaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerStates(entities: List<BattlePlayerStateEntity>)

    @Query(
        """
        SELECT p.id AS playerId,
               p.display_name AS playerName,
               p.avatar_key AS avatarKey,
               p.created_at AS createdAt,
               COALESCE(rounds.totalRounds, 0) AS totalRounds,
               COALESCE(actorStats.winRounds, 0) AS winRounds,
               COALESCE(actorStats.huRounds, 0) AS huRounds,
               COALESCE(actorStats.zimoRounds, 0) AS zimoRounds,
               COALESCE(actorStats.gangRounds, 0) AS gangRounds,
               COALESCE(dianPao.dianPaoRounds, 0) AS dianPaoRounds,
               COALESCE(deltas.totalDelta, 0) AS totalDelta
        FROM players p
        LEFT JOIN (
            SELECT sp.player_id AS playerId, SUM(s.settled_round_count) AS totalRounds
            FROM battle_session_player sp
            INNER JOIN battle_session s ON s.id = sp.session_id
            GROUP BY sp.player_id
        ) rounds ON rounds.playerId = p.id
        LEFT JOIN (
            SELECT e.actor_player_id AS playerId,
                   SUM(CASE WHEN e.event_type = 'HU' THEN 1 ELSE 0 END) AS huRounds,
                   SUM(CASE WHEN e.event_type = 'ZIMO' THEN 1 ELSE 0 END) AS zimoRounds,
                   SUM(CASE WHEN e.event_type IN ('GANG_DIAN', 'GANG_BA', 'GANG_AN') THEN 1 ELSE 0 END) AS gangRounds,
                   COUNT(DISTINCT CASE
                       WHEN e.event_type IN ('HU', 'ZIMO') THEN e.session_id || '-' || e.round_no
                       ELSE NULL
                   END) AS winRounds
            FROM battle_event e
            INNER JOIN battle_session_player sp ON sp.session_id = e.session_id AND sp.player_id = e.actor_player_id
            WHERE e.actor_player_id IS NOT NULL
              AND sp.player_role = 'ON_TABLE'
            GROUP BY e.actor_player_id
        ) actorStats ON actorStats.playerId = p.id
        LEFT JOIN (
            SELECT d.player_id AS playerId,
                   COUNT(e.id) AS dianPaoRounds
            FROM battle_event e
            INNER JOIN battle_event_delta d ON d.event_id = e.id
            INNER JOIN battle_session_player sp ON sp.session_id = e.session_id AND sp.player_id = d.player_id
            WHERE e.event_type = 'HU'
              AND d.delta < 0
              AND sp.player_role = 'ON_TABLE'
            GROUP BY d.player_id
        ) dianPao ON dianPao.playerId = p.id
        LEFT JOIN (
            SELECT player_id AS playerId, SUM(delta) AS totalDelta
            FROM battle_event_delta
            GROUP BY player_id
        ) deltas ON deltas.playerId = p.id
        ORDER BY totalDelta DESC, totalRounds DESC, p.created_at ASC
        """
    )
    fun observePlayerStatsRows(): kotlinx.coroutines.flow.Flow<List<PlayerStatsRow>>

    @Query(
        """
        SELECT d.player_id AS playerId,
               e.session_id AS sessionId,
               e.round_no AS roundNo,
               SUM(d.delta) AS roundDelta,
               MAX(e.created_at) AS occurredAt
        FROM battle_event_delta d
        INNER JOIN battle_event e ON e.id = d.event_id
        GROUP BY d.player_id, e.session_id, e.round_no
        ORDER BY occurredAt DESC
        """
    )
    fun observeRecentRoundRows(): kotlinx.coroutines.flow.Flow<List<PlayerRecentRoundRow>>
}

data class PlayerStatsRow(
    val playerId: Int,
    val playerName: String,
    val avatarKey: String?,
    val createdAt: Long,
    val totalRounds: Int,
    val winRounds: Int,
    val huRounds: Int,
    val zimoRounds: Int,
    val gangRounds: Int,
    val dianPaoRounds: Int,
    val totalDelta: Int
)

data class PlayerRecentRoundRow(
    val playerId: Int,
    val sessionId: Long,
    val roundNo: Int,
    val roundDelta: Int,
    val occurredAt: Long
)
