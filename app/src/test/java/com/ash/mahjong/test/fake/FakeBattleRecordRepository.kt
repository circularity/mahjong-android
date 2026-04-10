package com.ash.mahjong.test.fake

import com.ash.mahjong.data.battle.BattleRecordRepository
import com.ash.mahjong.data.battle.PlayerStats
import com.ash.mahjong.data.battle.SettledBattleRound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBattleRecordRepository : BattleRecordRepository {
    private val statsFlow = MutableStateFlow<List<PlayerStats>>(emptyList())
    val persistedRounds = mutableListOf<SettledBattleRound>()

    override suspend fun persistSettledRound(round: SettledBattleRound) {
        persistedRounds.add(round)
    }

    override fun observePlayerStats(): Flow<List<PlayerStats>> = statsFlow

    fun emitStats(stats: List<PlayerStats>) {
        statsFlow.value = stats
    }
}
