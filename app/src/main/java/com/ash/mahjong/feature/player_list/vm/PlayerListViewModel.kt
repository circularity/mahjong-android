package com.ash.mahjong.feature.player_list.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ash.mahjong.R
import com.ash.mahjong.data.player.AddPlayerResult
import com.ash.mahjong.data.player.Player
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerRepository
import com.ash.mahjong.feature.player_list.state.AddPlayerDialogUiState
import com.ash.mahjong.feature.player_list.state.PlayerListItemUiModel
import com.ash.mahjong.feature.player_list.state.PlayerListUiState
import com.ash.mahjong.feature.player_list.state.PlayerTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerListViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<PlayerListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.observePlayers().collect { players ->
                _uiState.update { state ->
                    state.copy(players = players.map(::mapToUiModel))
                }
            }
        }
    }

    fun onAddPlayerClick() {
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = AddPlayerDialogUiState(
                    visible = true,
                    playerName = "",
                    initialScore = DEFAULT_INITIAL_SCORE,
                    errorMessageRes = null
                )
            )
        }
    }

    fun onDismissAddPlayerDialog() {
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = AddPlayerDialogUiState(
                    visible = false,
                    playerName = "",
                    initialScore = DEFAULT_INITIAL_SCORE,
                    errorMessageRes = null
                )
            )
        }
    }

    fun onPlayerNameChange(name: String) {
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = state.addPlayerDialog.copy(
                    playerName = name,
                    errorMessageRes = null
                )
            )
        }
    }

    fun onIncreaseInitialScore() {
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = state.addPlayerDialog.copy(
                    initialScore = state.addPlayerDialog.initialScore + SCORE_STEP
                )
            )
        }
    }

    fun onDecreaseInitialScore() {
        _uiState.update { state ->
            val updatedScore = (state.addPlayerDialog.initialScore - SCORE_STEP).coerceAtLeast(0)
            state.copy(
                addPlayerDialog = state.addPlayerDialog.copy(initialScore = updatedScore)
            )
        }
    }

    fun onConfirmAddPlayer() {
        val state = _uiState.value
        viewModelScope.launch {
            when (
                playerRepository.addPlayer(
                    name = state.addPlayerDialog.playerName,
                    initialScore = state.addPlayerDialog.initialScore
                )
            ) {
                AddPlayerResult.Success -> onDismissAddPlayerDialog()
                AddPlayerResult.InvalidName -> showNameError(R.string.players_dialog_error_empty_name)
                AddPlayerResult.DuplicateName -> showNameError(R.string.players_dialog_error_duplicate_name)
            }
        }
    }

    fun onTogglePlayerActiveClick(playerId: Int) {
        val currentPlayer = _uiState.value.players.firstOrNull { it.id == playerId } ?: return
        viewModelScope.launch {
            playerRepository.updatePlayerActiveStatus(
                playerId = playerId,
                isActive = !currentPlayer.isActive
            )
        }
    }

    fun onTogglePlayerRoleClick(playerId: Int) {
        val currentPlayer = _uiState.value.players.firstOrNull { it.id == playerId } ?: return
        val targetRole = if (currentPlayer.playerRole == PlayerRole.ON_TABLE) {
            PlayerRole.HORSE
        } else {
            PlayerRole.ON_TABLE
        }
        viewModelScope.launch {
            playerRepository.updatePlayerRole(
                playerId = playerId,
                role = targetRole
            )
        }
    }

    fun onChangePlayerAvatarClick(playerId: Int) {
        val currentPlayer = _uiState.value.players.firstOrNull { it.id == playerId } ?: return
        val nextAvatarKey = PlayerAnimalAvatarCatalog.nextAvatarKey(currentPlayer.avatarKey)
        viewModelScope.launch {
            playerRepository.updatePlayerAvatar(
                playerId = playerId,
                avatarKey = nextAvatarKey
            )
        }
    }

    private fun createInitialState(): PlayerListUiState {
        return PlayerListUiState(
            players = emptyList(),
            addPlayerDialog = AddPlayerDialogUiState(
                visible = false,
                playerName = "",
                initialScore = DEFAULT_INITIAL_SCORE,
                errorMessageRes = null
            )
        )
    }

    private fun mapToUiModel(player: Player): PlayerListItemUiModel {
        val resolvedAvatarKey = PlayerAnimalAvatarCatalog.resolveAvatarKeyOrFallback(
            avatarKey = player.avatarKey,
            playerId = player.id,
            createdAt = player.createdAt
        )
        return PlayerListItemUiModel(
            id = player.id,
            name = player.name,
            score = player.score,
            isActive = player.isActive,
            playerRole = player.playerRole,
            avatarKey = resolvedAvatarKey,
            avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(resolvedAvatarKey),
            winRatePercent = 0,
            totalGames = 0,
            isOnline = false,
            isMvp = false,
            trend = PlayerTrend.FLAT
        )
    }

    private fun showNameError(@androidx.annotation.StringRes errorRes: Int) {
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = state.addPlayerDialog.copy(errorMessageRes = errorRes)
            )
        }
    }

    companion object {
        const val DEFAULT_INITIAL_SCORE = 100
        const val SCORE_STEP = 10
    }
}
