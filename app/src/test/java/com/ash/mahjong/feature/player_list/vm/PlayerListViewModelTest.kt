package com.ash.mahjong.feature.player_list.vm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.ash.mahjong.R
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
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
