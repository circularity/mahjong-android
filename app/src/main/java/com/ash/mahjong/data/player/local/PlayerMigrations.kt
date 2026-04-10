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

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battle_session (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    status TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    ended_at INTEGER,
                    lineup_fingerprint TEXT NOT NULL,
                    base_point INTEGER NOT NULL,
                    capping_multiplier INTEGER NOT NULL,
                    current_round_no INTEGER NOT NULL,
                    last_event_seq INTEGER NOT NULL,
                    settled_round_count INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_session_status ON battle_session(status)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_session_lineup_fingerprint ON battle_session(lineup_fingerprint)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battle_session_player (
                    session_id INTEGER NOT NULL,
                    player_id INTEGER NOT NULL,
                    seat_index INTEGER NOT NULL,
                    player_role TEXT NOT NULL,
                    name_snapshot TEXT NOT NULL,
                    avatar_snapshot TEXT,
                    initial_score INTEGER NOT NULL,
                    PRIMARY KEY(session_id, player_id)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_session_player_player_id ON battle_session_player(player_id)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_session_player_session_id_seat_index ON battle_session_player(session_id, seat_index)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battle_event (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    session_id INTEGER NOT NULL,
                    round_no INTEGER NOT NULL,
                    event_seq INTEGER NOT NULL,
                    action_group_id INTEGER NOT NULL,
                    event_type TEXT NOT NULL,
                    actor_player_id INTEGER,
                    multiplier INTEGER,
                    payload_json TEXT,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_event_session_id_event_seq ON battle_event(session_id, event_seq)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_event_session_id_round_no ON battle_event(session_id, round_no)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_event_session_id_action_group_id ON battle_event(session_id, action_group_id)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battle_event_delta (
                    event_id INTEGER NOT NULL,
                    player_id INTEGER NOT NULL,
                    delta INTEGER NOT NULL,
                    PRIMARY KEY(event_id, player_id)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_event_delta_player_id ON battle_event_delta(player_id)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battle_player_state (
                    session_id INTEGER NOT NULL,
                    player_id INTEGER NOT NULL,
                    total_score INTEGER NOT NULL,
                    round_delta INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    win_order INTEGER,
                    PRIMARY KEY(session_id, player_id)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_player_state_player_id ON battle_player_state(player_id)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_battle_player_state_session_id_player_id ON battle_player_state(session_id, player_id)"
            )
        }
    }
}
