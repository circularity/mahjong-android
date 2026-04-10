package com.ash.mahjong.data.battle.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battle_event",
    indices = [
        Index(value = ["session_id", "event_seq"]),
        Index(value = ["session_id", "round_no"]),
        Index(value = ["session_id", "action_group_id"])
    ]
)
data class BattleEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "round_no") val roundNo: Int,
    @ColumnInfo(name = "event_seq") val eventSeq: Long,
    @ColumnInfo(name = "action_group_id") val actionGroupId: Long,
    @ColumnInfo(name = "event_type") val eventType: String,
    @ColumnInfo(name = "actor_player_id") val actorPlayerId: Int?,
    @ColumnInfo(name = "multiplier") val multiplier: Int?,
    @ColumnInfo(name = "payload_json") val payloadJson: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "battle_event_delta",
    primaryKeys = ["event_id", "player_id"],
    indices = [Index(value = ["player_id"])]
)
data class BattleEventDeltaEntity(
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "player_id") val playerId: Int,
    @ColumnInfo(name = "delta") val delta: Int
)

@Entity(
    tableName = "battle_player_state",
    primaryKeys = ["session_id", "player_id"],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["session_id", "player_id"])
    ]
)
data class BattlePlayerStateEntity(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "player_id") val playerId: Int,
    @ColumnInfo(name = "total_score") val totalScore: Int,
    @ColumnInfo(name = "round_delta") val roundDelta: Int,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "win_order") val winOrder: Int?
)
