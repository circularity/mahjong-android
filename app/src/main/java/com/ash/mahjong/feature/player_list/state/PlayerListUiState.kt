package com.ash.mahjong.feature.player_list.state

import androidx.annotation.StringRes
import com.ash.mahjong.data.player.PlayerRole

data class PlayerListUiState(
    val players: List<PlayerListItemUiModel> = emptyList(),
    val monthlyBestPlayerName: String = "",
    val addPlayerDialog: AddPlayerDialogUiState = AddPlayerDialogUiState()
) {
    val activePlayerCount: Int
        get() = players.count { it.isActive }

    val inactivePlayerCount: Int
        get() = players.count { !it.isActive }

    val activePlayers: List<PlayerListItemUiModel>
        get() = players.filter { it.isActive }

    val inactivePlayers: List<PlayerListItemUiModel>
        get() = players.filter { !it.isActive }

    val totalPlayerCount: Int
        get() = players.size

}

data class AddPlayerDialogUiState(
    val visible: Boolean = false,
    val mode: PlayerDialogMode = PlayerDialogMode.ADD,
    val editingPlayerId: Int? = null,
    val playerName: String = "",
    val initialScore: Int = 0,
    val selectedAvatarKey: String = "",
    val selectedAvatarEmoji: String = "",
    @param:StringRes val errorMessageRes: Int? = null
) {
    val canConfirm: Boolean
        get() = playerName.isNotBlank()
}

enum class PlayerDialogMode {
    ADD,
    EDIT
}

data class PlayerListItemUiModel(
    val id: Int,
    val name: String,
    val score: Int,
    val isActive: Boolean,
    val playerRole: PlayerRole,
    val avatarKey: String,
    val avatarEmoji: String,
    val winRatePercent: Int,
    val totalGames: Int,
    val isOnline: Boolean,
    val isMvp: Boolean,
    val trend: PlayerTrend
)

enum class PlayerTrend {
    UP,
    FLAT,
    DOWN
}
