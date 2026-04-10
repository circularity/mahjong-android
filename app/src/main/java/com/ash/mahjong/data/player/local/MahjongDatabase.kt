package com.ash.mahjong.data.player.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlayerEntity::class],
    version = 4,
    exportSchema = false
)
abstract class MahjongDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}
