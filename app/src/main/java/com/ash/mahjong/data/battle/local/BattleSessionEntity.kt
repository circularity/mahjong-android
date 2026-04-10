package com.ash.mahjong.data.battle.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battle_session",
    indices = [
        Index(value = ["status"]),
        Index(value = ["lineup_fingerprint"])
    ]
)
data class BattleSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long? = null,
    @ColumnInfo(name = "lineup_fingerprint") val lineupFingerprint: String,
    @ColumnInfo(name = "base_point") val basePoint: Int,
    @ColumnInfo(name = "capping_multiplier") val cappingMultiplier: Int,
    @ColumnInfo(name = "current_round_no") val currentRoundNo: Int,
    @ColumnInfo(name = "last_event_seq") val lastEventSeq: Long,
    @ColumnInfo(name = "settled_round_count") val settledRoundCount: Int
)

@Entity(
    tableName = "battle_session_player",
    primaryKeys = ["session_id", "player_id"],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["session_id", "seat_index"])
    ]
)
data class BattleSessionPlayerEntity(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "player_id") val playerId: Int,
    @ColumnInfo(name = "seat_index") val seatIndex: Int,
    @ColumnInfo(name = "player_role") val playerRole: String,
    @ColumnInfo(name = "name_snapshot") val nameSnapshot: String,
    @ColumnInfo(name = "avatar_snapshot") val avatarSnapshot: String?,
    @ColumnInfo(name = "initial_score") val initialScore: Int
)
