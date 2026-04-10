package com.ash.mahjong.di

import android.content.Context
import androidx.room.Room
import com.ash.mahjong.data.player.PlayerRepository
import com.ash.mahjong.data.player.RoomPlayerRepository
import com.ash.mahjong.data.player.local.MahjongDatabase
import com.ash.mahjong.data.player.local.PlayerDao
import com.ash.mahjong.data.player.local.PlayerMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerDataModule {

    @Provides
    @Singleton
    fun provideMahjongDatabase(
        @ApplicationContext context: Context
    ): MahjongDatabase {
        return Room.databaseBuilder(
            context,
            MahjongDatabase::class.java,
            "mahjong.db"
        )
            .addMigrations(
                PlayerMigrations.MIGRATION_1_2,
                PlayerMigrations.MIGRATION_2_3,
                PlayerMigrations.MIGRATION_3_4
            )
            .build()
    }

    @Provides
    fun providePlayerDao(database: MahjongDatabase): PlayerDao {
        return database.playerDao()
    }

    @Provides
    @Singleton
    fun providePlayerRepository(playerDao: PlayerDao): PlayerRepository {
        return RoomPlayerRepository(playerDao)
    }
}
