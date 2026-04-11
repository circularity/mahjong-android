package com.ash.mahjong.data.player.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ash.mahjong.data.player.PlayerRole
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY created_at DESC")
    fun observeAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY created_at DESC LIMIT :limit")
    fun observeRecentPlayers(limit: Int): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlayer(entity: PlayerEntity): Long

    @Query(
        """
        UPDATE players
        SET display_name = :displayName,
            normalized_name = :normalizedName,
            initial_score = :initialScore,
            avatar_key = :avatarKey
        WHERE id = :playerId
        """
    )
    suspend fun updatePlayerProfile(
        playerId: Int,
        displayName: String,
        normalizedName: String,
        initialScore: Int,
        avatarKey: String?
    ): Int

    @Query("UPDATE players SET is_active = :isActive WHERE id = :playerId")
    suspend fun updatePlayerActiveStatus(playerId: Int, isActive: Boolean)

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getPlayerById(playerId: Int): PlayerEntity?

    @Query(
        """
        UPDATE players
        SET player_role = :playerRole,
            bound_on_table_player_id = NULL
        WHERE id = :playerId
        """
    )
    suspend fun updatePlayerRole(playerId: Int, playerRole: String)

    @Query("UPDATE players SET bound_on_table_player_id = :boundOnTablePlayerId WHERE id = :playerId")
    suspend fun updateHorseBinding(playerId: Int, boundOnTablePlayerId: Int?)

    @Query("UPDATE players SET bound_on_table_player_id = NULL WHERE bound_on_table_player_id = :targetPlayerId")
    suspend fun clearHorseBindingsByTarget(targetPlayerId: Int)

    @Query("UPDATE players SET avatar_key = :avatarKey WHERE id = :playerId")
    suspend fun updatePlayerAvatar(playerId: Int, avatarKey: String)

    @Transaction
    suspend fun swapOnTableWithHorse(
        onTablePlayerId: Int,
        horsePlayerId: Int
    ) {
        val onTablePlayer = getPlayerById(onTablePlayerId) ?: return
        val horsePlayer = getPlayerById(horsePlayerId) ?: return
        if (!onTablePlayer.isActive || !horsePlayer.isActive) {
            return
        }
        if (onTablePlayer.playerRole != PlayerRole.ON_TABLE.name ||
            horsePlayer.playerRole != PlayerRole.HORSE.name
        ) {
            return
        }
        updatePlayerRole(playerId = onTablePlayerId, playerRole = PlayerRole.HORSE.name)
        updatePlayerRole(playerId = horsePlayerId, playerRole = PlayerRole.ON_TABLE.name)
        clearHorseBindingsByTarget(targetPlayerId = onTablePlayerId)
    }
}
