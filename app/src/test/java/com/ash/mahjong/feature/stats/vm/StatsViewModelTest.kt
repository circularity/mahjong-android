package com.ash.mahjong.feature.stats.vm

import com.ash.mahjong.data.battle.PlayerStats
import com.ash.mahjong.test.fake.FakeBattleRecordRepository
import com.ash.mahjong.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun observePlayerStats_mapsRepositoryDataToUiState() = runTest {
        val repository = FakeBattleRecordRepository()
        val viewModel = StatsViewModel(battleRecordRepository = repository)

        repository.emitStats(
            listOf(
                PlayerStats(
                    playerId = 1,
                    name = "A",
                    avatarKey = "dog",
                    createdAt = 1L,
                    zimoRounds = 3,
                    huRounds = 4,
                    gangRounds = 5,
                    totalRounds = 10,
                    winRounds = 4,
                    dianPaoRounds = 2,
                    winRate = 40f,
                    totalDelta = 15,
                    avgDelta = 1.5f,
                    recentRounds = listOf(4, -2, 0)
                )
            )
        )
        advanceUntilIdle()

        val player = viewModel.uiState.value.players.single()
        assertEquals("A", player.name)
        assertEquals("dog", player.avatarKey)
        assertEquals("\uD83D\uDC36", player.avatarEmoji)
        assertEquals(3, player.zimoRounds)
        assertEquals(4, player.huRounds)
        assertEquals(5, player.gangRounds)
        assertEquals(2, player.dianPaoRounds)
        assertEquals(0.4f, player.winRateProgress)
        assertEquals("40.0%", player.winRateText)
        assertEquals(15, player.totalDelta)
        assertEquals("+15", player.totalDeltaText)
        assertEquals("+1.5", player.avgDeltaText)
        assertEquals("+4  -2  +0", player.recentRoundsText)
    }

    @Test
    fun observePlayerStats_withImageAvatarKey_mapsAvatarKeyAndKeepsEmojiEmpty() = runTest {
        val repository = FakeBattleRecordRepository()
        val viewModel = StatsViewModel(battleRecordRepository = repository)

        repository.emitStats(
            listOf(
                PlayerStats(
                    playerId = 2,
                    name = "B",
                    avatarKey = "image_03",
                    createdAt = 1L,
                    zimoRounds = 1,
                    huRounds = 1,
                    gangRounds = 1,
                    totalRounds = 2,
                    winRounds = 1,
                    dianPaoRounds = 0,
                    winRate = 50f,
                    totalDelta = 8,
                    avgDelta = 4f,
                    recentRounds = listOf(8)
                )
            )
        )
        advanceUntilIdle()

        val player = viewModel.uiState.value.players.single()
        assertEquals("image_03", player.avatarKey)
        assertEquals("", player.avatarEmoji)
    }
}
