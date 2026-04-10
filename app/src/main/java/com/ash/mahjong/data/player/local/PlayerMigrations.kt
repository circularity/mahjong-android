package com.ash.mahjong.data.player.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object PlayerMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE players ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE players ADD COLUMN player_role TEXT NOT NULL DEFAULT 'ON_TABLE'"
            )
            db.execSQL(
                "ALTER TABLE players ADD COLUMN bound_on_table_player_id INTEGER"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE players ADD COLUMN avatar_key TEXT"
            )
        }
    }
}
