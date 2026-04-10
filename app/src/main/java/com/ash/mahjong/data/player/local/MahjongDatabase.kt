package com.ash.mahjong.data.player.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ash.mahjong.data.battle.local.BattleEventDao
import com.ash.mahjong.data.battle.local.BattleEventDeltaEntity
import com.ash.mahjong.data.battle.local.BattleEventEntity
import com.ash.mahjong.data.battle.local.BattlePlayerStateEntity
import com.ash.mahjong.data.battle.local.BattleSessionDao
import com.ash.mahjong.data.battle.local.BattleSessionEntity
import com.ash.mahjong.data.battle.local.BattleSessionPlayerEntity

@Database(
    entities = [
        PlayerEntity::class,
        BattleSessionEntity::class,
        BattleSessionPlayerEntity::class,
        BattleEventEntity::class,
        BattleEventDeltaEntity::class,
        BattlePlayerStateEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MahjongDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun battleSessionDao(): BattleSessionDao

    abstract fun battleEventDao(): BattleEventDao
}
