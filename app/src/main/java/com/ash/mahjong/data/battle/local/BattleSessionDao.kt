package com.ash.mahjong.data.battle.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BattleSessionDao {
    @Query("SELECT * FROM battle_session WHERE status = 'ACTIVE' ORDER BY started_at DESC LIMIT 1")
    suspend fun getActiveSession(): BattleSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(entity: BattleSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionPlayers(entities: List<BattleSessionPlayerEntity>)

    @Update
    suspend fun updateSession(entity: BattleSessionEntity)

    @Query("UPDATE battle_session SET status = :status, ended_at = :endedAt WHERE id = :sessionId")
    suspend fun closeSession(sessionId: Long, status: String, endedAt: Long)
}
