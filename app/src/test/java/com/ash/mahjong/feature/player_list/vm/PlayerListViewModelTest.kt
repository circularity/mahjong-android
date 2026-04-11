package com.ash.mahjong.feature.player_list.vm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.ash.mahjong.R
import com.ash.mahjong.data.battle.PlayerStats
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.feature.player_list.state.PlayerDialogMode
import com.ash.mahjong.feature.player_list.state.PlayerTrend
import com.ash.mahjong.test.fake.FakeBattleRecordRepository
import com.ash.mahjong.test.fake.FakePlayerRepository
import com.ash.mahjong.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_hasNoMockPlayersAndDialogIsHidden() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.players.isEmpty())
        assertEquals(0, state.totalPlayerCount)
        assertFalse(state.addPlayerDialog.visible)
        assertEquals(PlayerListViewModel.DEFAULT_INITIAL_SCORE, state.addPlayerDialog.initialScore)
    }

    @Test
    fun onAddPlayerClickAndConfirm_withValidName_addsPlayerAndDismissesDialog() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("阿宝")
        viewModel.onIncreaseInitialScore()
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.players.size)
        assertEquals("阿宝", state.players.first().name)
        assertEquals(
            PlayerListViewModel.DEFAULT_INITIAL_SCORE + PlayerListViewModel.SCORE_STEP,
            state.players.first().score
        )
        assertFalse(state.addPlayerDialog.visible)
    }

    @Test
    fun onConfirmAddPlayer_withBlankName_doesNotAddPlayer() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("   ")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.players.isEmpty())
        assertTrue(state.addPlayerDialog.visible)
        assertEquals(R.string.players_dialog_error_empty_name, state.addPlayerDialog.errorMessageRes)
    }

    @Test
    fun observePlayerStats_updatesPlayerDataAndMonthlyBestFromRealStats() = runTest {
        val playerRepository = FakePlayerRepository()
        val battleRepository = FakeBattleRecordRepository()
        val viewModel = PlayerListViewModel(
            playerRepository = playerRepository,
            battleRecordRepository = battleRepository
        )

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("甲")
        viewModel.onConfirmAddPlayer()
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("乙")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val playersByName = viewModel.uiState.value.players.associateBy { it.name }
        val playerA = playersByName.getValue("甲")
        val playerB = playersByName.getValue("乙")
        battleRepository.emitStats(
            listOf(
                PlayerStats(
                    playerId = playerA.id,
                    name = playerA.name,
                    avatarKey = playerA.avatarKey,
                    createdAt = 1L,
                    zimoRounds = 1,
                    huRounds = 2,
                    gangRounds = 0,
                    totalRounds = 12,
                    winRounds = 7,
                    dianPaoRounds = 1,
                    winRate = 58.3f,
                    totalDelta = 66,
                    currentMonthDelta = 38,
                    avgDelta = 5.5f,
                    recentRounds = listOf(12, 8, -3)
                ),
                PlayerStats(
                    playerId = playerB.id,
                    name = playerB.name,
                    avatarKey = playerB.avatarKey,
                    createdAt = 2L,
                    zimoRounds = 0,
                    huRounds = 1,
                    gangRounds = 0,
                    totalRounds = 8,
                    winRounds = 3,
                    dianPaoRounds = 2,
                    winRate = 37.5f,
                    totalDelta = -10,
                    currentMonthDelta = -6,
                    avgDelta = -1.2f,
                    recentRounds = listOf(-5, -2, 1)
                )
            )
        )
        advanceUntilIdle()

        val updatedA = viewModel.uiState.value.players.first { it.id == playerA.id }
        val updatedB = viewModel.uiState.value.players.first { it.id == playerB.id }
        assertEquals(58, updatedA.winRatePercent)
        assertEquals(12, updatedA.totalGames)
        assertEquals(PlayerTrend.UP, updatedA.trend)
        assertTrue(updatedA.isMvp)
        assertEquals(37, updatedB.winRatePercent)
        assertEquals(8, updatedB.totalGames)
        assertEquals(PlayerTrend.DOWN, updatedB.trend)
        assertFalse(updatedB.isMvp)
        assertEquals("甲", viewModel.uiState.value.monthlyBestPlayerName)
    }

    @Test
    fun onDecreaseInitialScore_respectsMinimumZero() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())

        viewModel.onAddPlayerClick()
        repeat(50) { viewModel.onDecreaseInitialScore() }

        val state = viewModel.uiState.value
        assertEquals(0, state.addPlayerDialog.initialScore)
    }

    @Test
    fun onConfirmAddPlayer_withDuplicateName_showsDuplicateError() = runTest {
        val repository = FakePlayerRepository()
        val viewModel = PlayerListViewModel(repository)

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("Li")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange(" li ")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.players.size)
        assertTrue(state.addPlayerDialog.visible)
        assertEquals(R.string.players_dialog_error_duplicate_name, state.addPlayerDialog.errorMessageRes)
    }

    @Test
    fun onTogglePlayerActiveClick_togglesPlayerActiveStatus() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("可可")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val insertedPlayerId = viewModel.uiState.value.players.first().id
        assertTrue(viewModel.uiState.value.players.first().isActive)

        viewModel.onTogglePlayerActiveClick(insertedPlayerId)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.players.first().isActive)
        assertEquals(0, viewModel.uiState.value.activePlayerCount)
        assertEquals(1, viewModel.uiState.value.inactivePlayerCount)
    }

    @Test
    fun onTogglePlayerRoleClick_togglesBetweenOnTableAndHorse() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("乐乐")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val playerId = viewModel.uiState.value.players.first().id
        assertEquals(PlayerRole.ON_TABLE, viewModel.uiState.value.players.first().playerRole)

        viewModel.onTogglePlayerRoleClick(playerId)
        advanceUntilIdle()
        assertEquals(PlayerRole.HORSE, viewModel.uiState.value.players.first().playerRole)

        viewModel.onTogglePlayerRoleClick(playerId)
        advanceUntilIdle()
        assertEquals(PlayerRole.ON_TABLE, viewModel.uiState.value.players.first().playerRole)
    }

    @Test
    fun onPlayerLongClick_opensEditDialogWithCurrentValues() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("可可")
        viewModel.onIncreaseInitialScore()
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val player = viewModel.uiState.value.players.first()
        viewModel.onPlayerLongClick(player.id)

        val dialog = viewModel.uiState.value.addPlayerDialog
        assertTrue(dialog.visible)
        assertEquals(PlayerDialogMode.EDIT, dialog.mode)
        assertEquals(player.id, dialog.editingPlayerId)
        assertEquals(player.name, dialog.playerName)
        assertEquals(player.score, dialog.initialScore)
        assertEquals(player.avatarKey, dialog.selectedAvatarKey)
        assertEquals(player.avatarEmoji, dialog.selectedAvatarEmoji)
    }

    @Test
    fun onSelectDialogAvatar_updatesDialogSelectedAvatar() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        advanceUntilIdle()

        val initialKey = viewModel.uiState.value.addPlayerDialog.selectedAvatarKey
        val targetKey = PlayerAnimalAvatarCatalog.nextAvatarKey(initialKey)
        viewModel.onSelectDialogAvatar(targetKey)

        val dialog = viewModel.uiState.value.addPlayerDialog
        assertEquals(targetKey, dialog.selectedAvatarKey)
        assertEquals(PlayerAnimalAvatarCatalog.emojiForKey(targetKey), dialog.selectedAvatarEmoji)
    }

    @Test
    fun onConfirmAddPlayer_inEditMode_updatesPlayer() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("阿宝")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val player = viewModel.uiState.value.players.first()
        viewModel.onPlayerLongClick(player.id)
        viewModel.onPlayerNameChange("阿宝2")
        viewModel.onIncreaseInitialScore()
        val targetAvatarKey = PlayerAnimalAvatarCatalog.nextAvatarKey(player.avatarKey)
        viewModel.onSelectDialogAvatar(targetAvatarKey)
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val updatedPlayer = viewModel.uiState.value.players.first()
        assertEquals("阿宝2", updatedPlayer.name)
        assertEquals(player.score + PlayerListViewModel.SCORE_STEP, updatedPlayer.score)
        assertEquals(targetAvatarKey, updatedPlayer.avatarKey)
        assertFalse(viewModel.uiState.value.addPlayerDialog.visible)
    }

    @Test
    fun onConfirmAddPlayer_inEditModeWithDuplicateName_showsDuplicateError() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("甲")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("乙")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val playerToEdit = viewModel.uiState.value.players.first { it.name == "乙" }
        viewModel.onPlayerLongClick(playerToEdit.id)
        viewModel.onPlayerNameChange("甲")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val dialog = viewModel.uiState.value.addPlayerDialog
        assertTrue(dialog.visible)
        assertEquals(PlayerDialogMode.EDIT, dialog.mode)
        assertEquals(R.string.players_dialog_error_duplicate_name, dialog.errorMessageRes)
    }

    @Test
    fun togglePlayerRole_doesNotChangeActiveCount() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("甲")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val playerId = viewModel.uiState.value.players.first().id
        assertEquals(1, viewModel.uiState.value.activePlayerCount)
        assertEquals(0, viewModel.uiState.value.inactivePlayerCount)

        viewModel.onTogglePlayerRoleClick(playerId)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.activePlayerCount)
        assertEquals(0, viewModel.uiState.value.inactivePlayerCount)
    }

    @Test
    fun onChangePlayerAvatarClick_switchesToNextAvatar() = runTest {
        val viewModel = PlayerListViewModel(FakePlayerRepository())
        viewModel.onAddPlayerClick()
        viewModel.onPlayerNameChange("丸子")
        viewModel.onConfirmAddPlayer()
        advanceUntilIdle()

        val player = viewModel.uiState.value.players.first()
        val oldAvatarKey = player.avatarKey

        viewModel.onChangePlayerAvatarClick(player.id)
        advanceUntilIdle()

        val updatedPlayer = viewModel.uiState.value.players.first()
        assertTrue(PlayerAnimalAvatarCatalog.normalizeAvatarKey(updatedPlayer.avatarKey) != null)
        assertTrue(
            updatedPlayer.avatarKey != oldAvatarKey
        )
    }
}
