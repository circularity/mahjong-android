package com.ash.mahjong.data.player.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    indices = [Index(value = ["normalized_name"], unique = true)]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "initial_score") val initialScore: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "is_active", defaultValue = "1") val isActive: Boolean = true,
    @ColumnInfo(name = "player_role", defaultValue = "'ON_TABLE'") val playerRole: String = "ON_TABLE",
    @ColumnInfo(name = "bound_on_table_player_id") val boundOnTablePlayerId: Int? = null,
    @ColumnInfo(name = "avatar_key") val avatarKey: String? = null
)
