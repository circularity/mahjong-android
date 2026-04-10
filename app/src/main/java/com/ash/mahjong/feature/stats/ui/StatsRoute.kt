package com.ash.mahjong.feature.stats.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ash.mahjong.feature.stats.vm.StatsViewModel

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    StatsScreen(uiState = uiState)
}
