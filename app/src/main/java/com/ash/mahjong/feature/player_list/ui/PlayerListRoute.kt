package com.ash.mahjong.feature.player_list.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ash.mahjong.feature.player_list.vm.PlayerListViewModel

@Composable
fun PlayerListRoute(
    viewModel: PlayerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PlayerListScreen(
        uiState = uiState,
        onAddPlayerClick = viewModel::onAddPlayerClick,
        onDismissAddPlayerDialog = viewModel::onDismissAddPlayerDialog,
        onPlayerNameChange = viewModel::onPlayerNameChange,
        onDecreaseInitialScore = viewModel::onDecreaseInitialScore,
        onIncreaseInitialScore = viewModel::onIncreaseInitialScore,
        onConfirmAddPlayer = viewModel::onConfirmAddPlayer,
        onTogglePlayerActiveClick = viewModel::onTogglePlayerActiveClick,
        onTogglePlayerRoleClick = viewModel::onTogglePlayerRoleClick,
        onChangePlayerAvatarClick = viewModel::onChangePlayerAvatarClick
    )
}
