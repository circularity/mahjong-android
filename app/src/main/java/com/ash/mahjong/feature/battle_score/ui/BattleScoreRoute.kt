package com.ash.mahjong.feature.battle_score.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ash.mahjong.feature.battle_score.vm.BattleScoreViewModel

@Composable
fun BattleScoreRoute(
    onGoToPlayers: () -> Unit,
    viewModel: BattleScoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    BattleScoreScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onGoToPlayers = onGoToPlayers
    )
}
