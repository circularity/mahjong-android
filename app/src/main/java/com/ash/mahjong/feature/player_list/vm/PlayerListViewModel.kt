package com.ash.mahjong.feature.player_list.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ash.mahjong.R
import com.ash.mahjong.data.player.AddPlayerResult
import com.ash.mahjong.data.battle.BattleRecordRepository
import com.ash.mahjong.data.battle.NoOpBattleRecordRepository
import com.ash.mahjong.data.battle.PlayerStats
import com.ash.mahjong.data.player.Player
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerRepository
import com.ash.mahjong.feature.player_list.state.AddPlayerDialogUiState
import com.ash.mahjong.feature.player_list.state.PlayerDialogMode
import com.ash.mahjong.feature.player_list.state.PlayerListItemUiModel
import com.ash.mahjong.feature.player_list.state.PlayerListUiState
import com.ash.mahjong.feature.player_list.state.PlayerTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class PlayerListViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val battleRecordRepository: BattleRecordRepository = NoOpBattleRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<PlayerListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                playerRepository.observePlayers(),
                battleRecordRepository.observePlayerStats()
            ) { players, playerStats ->
                players to playerStats
            }.collect { (players, playerStats) ->
                val statsByPlayerId = playerStats.associateBy { stats -> stats.playerId }
                val monthlyBestPlayerId = playerStats
                    .filter { stats -> stats.currentMonthDelta != 0 }
                    .maxByOrNull { stats -> stats.currentMonthDelta }
                    ?.playerId
                val monthlyBestPlayerName = monthlyBestPlayerId?.let { playerId ->
                    players.firstOrNull { player -> player.id == playerId }?.name
                }.orEmpty()
                _uiState.update { state ->
                    state.copy(
                        players = players.map { player ->
                            mapToUiModel(
                                player = player,
                                stats = statsByPlayerId[player.id],
                                isMonthlyBest = player.id == monthlyBestPlayerId
                            )
                        },
                        monthlyBestPlayerName = monthlyBestPlayerName
                    )
                }
            }
        }
    }

    fun onAddPlayerClick() {
        val avatarKey = PlayerAnimalAvatarCatalog.randomAvatarKey()
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = AddPlayerDialogUiState(
                    visible = true,
                    mode = PlayerDialogMode.ADD,
                    editingPlayerId = null,
                    playerName = "",
                    initialScore = DEFAULT_INITIAL_SCORE,
                    selectedAvatarKey = avatarKey,
                    selectedAvatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(avatarKey),
                    errorMessageRes = null
                )
            )
        }
    }

    fun onPlayerLongClick(playerId: Int) {
        val currentPlayer = _uiState.value.players.firstOrNull { it.id == playerId } ?: return
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = AddPlayerDialogUiState(
                    visible = true,
                    mode = PlayerDialogMode.EDIT,
                    editingPlayerId = playerId,
                    playerName = currentPlayer.name,
                    initialScore = currentPlayer.score,
                    selectedAvatarKey = currentPlayer.avatarKey,
                    selectedAvatarEmoji = currentPlayer.avatarEmoji,
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
                    mode = PlayerDialogMode.ADD,
                    editingPlayerId = null,
                    playerName = "",
                    initialScore = DEFAULT_INITIAL_SCORE,
                    selectedAvatarKey = "",
                    selectedAvatarEmoji = "",
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

    fun onSelectDialogAvatar(avatarKey: String) {
        val normalizedAvatarKey = PlayerAnimalAvatarCatalog.normalizeAvatarKey(avatarKey) ?: return
        _uiState.update { state ->
            state.copy(
                addPlayerDialog = state.addPlayerDialog.copy(
                    selectedAvatarKey = normalizedAvatarKey,
                    selectedAvatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(normalizedAvatarKey)
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
        val dialogState = state.addPlayerDialog
        viewModelScope.launch {
            val result = when (dialogState.mode) {
                PlayerDialogMode.ADD -> {
                    playerRepository.addPlayer(
                        name = dialogState.playerName,
                        initialScore = dialogState.initialScore,
                        avatarKey = dialogState.selectedAvatarKey
                    )
                }

                PlayerDialogMode.EDIT -> {
                    val playerId = dialogState.editingPlayerId ?: return@launch
                    playerRepository.updatePlayerProfile(
                        playerId = playerId,
                        name = dialogState.playerName,
                        initialScore = dialogState.initialScore,
                        avatarKey = dialogState.selectedAvatarKey
                    )
                }
            }
            when (result) {
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
                mode = PlayerDialogMode.ADD,
                editingPlayerId = null,
                playerName = "",
                initialScore = DEFAULT_INITIAL_SCORE,
                selectedAvatarKey = "",
                selectedAvatarEmoji = "",
                errorMessageRes = null
            )
        )
    }

    private fun mapToUiModel(
        player: Player,
        stats: PlayerStats?,
        isMonthlyBest: Boolean
    ): PlayerListItemUiModel {
        val resolvedAvatarKey = PlayerAnimalAvatarCatalog.resolveAvatarKeyOrFallback(
            avatarKey = player.avatarKey,
            playerId = player.id,
            createdAt = player.createdAt
        )
        val trend = resolveTrend(stats?.recentRounds.orEmpty())
        return PlayerListItemUiModel(
            id = player.id,
            name = player.name,
            score = player.score,
            isActive = player.isActive,
            playerRole = player.playerRole,
            avatarKey = resolvedAvatarKey,
            avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(resolvedAvatarKey),
            winRatePercent = stats?.winRate?.roundToInt() ?: 0,
            totalGames = stats?.totalRounds ?: 0,
            isOnline = false,
            isMvp = isMonthlyBest,
            trend = trend
        )
    }

    private fun resolveTrend(recentRounds: List<Int>): PlayerTrend {
        val momentum = recentRounds.take(3).sum()
        return when {
            momentum > 0 -> PlayerTrend.UP
            momentum < 0 -> PlayerTrend.DOWN
            else -> PlayerTrend.FLAT
        }
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
