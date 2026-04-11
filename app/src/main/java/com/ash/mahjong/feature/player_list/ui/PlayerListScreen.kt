package com.ash.mahjong.feature.player_list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ash.mahjong.R
import com.ash.mahjong.feature.player_list.state.PlayerListUiState

@Composable
fun PlayerListScreen(
    uiState: PlayerListUiState,
    onAddPlayerClick: () -> Unit,
    onDismissAddPlayerDialog: () -> Unit,
    onPlayerNameChange: (String) -> Unit,
    onDecreaseInitialScore: () -> Unit,
    onIncreaseInitialScore: () -> Unit,
    onSelectDialogAvatar: (String) -> Unit,
    onConfirmAddPlayer: () -> Unit,
    onTogglePlayerActiveClick: (Int) -> Unit,
    onTogglePlayerRoleClick: (Int) -> Unit,
    onChangePlayerAvatarClick: (Int) -> Unit,
    onPlayerLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerListColors.Background)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AddPlayerCard(onClick = onAddPlayerClick) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.players_active_count, uiState.activePlayerCount),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.players_icon_filter),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            if (uiState.players.isEmpty()) {
                item { EmptyPlayersState() }
            } else {
                if (uiState.activePlayers.isNotEmpty()) {
                    item {
                        PlayerSectionTitle(text = stringResource(R.string.players_active_section))
                    }
                    uiState.activePlayers.forEach { player ->
                        item(key = player.id) {
                            PlayerListCard(
                                player = player,
                                onToggleActiveClick = { onTogglePlayerActiveClick(player.id) },
                                onToggleRoleClick = { onTogglePlayerRoleClick(player.id) },
                                onAvatarClick = { onChangePlayerAvatarClick(player.id) },
                                onLongClick = { onPlayerLongClick(player.id) }
                            )
                        }
                    }
                }
                if (uiState.inactivePlayers.isNotEmpty()) {
                    item {
                        PlayerSectionTitle(
                            text = stringResource(
                                R.string.players_inactive_section,
                                uiState.inactivePlayerCount
                            )
                        )
                    }
                    uiState.inactivePlayers.forEach { player ->
                        item(key = player.id) {
                            PlayerListCard(
                                player = player,
                                onToggleActiveClick = { onTogglePlayerActiveClick(player.id) },
                                onToggleRoleClick = { onTogglePlayerRoleClick(player.id) },
                                onAvatarClick = { onChangePlayerAvatarClick(player.id) },
                                onLongClick = { onPlayerLongClick(player.id) }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.players_total_registered),
                        value = uiState.totalPlayerCount.toString(),
                        emphasized = false
                    )

                    val monthlyBestValue = uiState.monthlyBestPlayerName.ifBlank {
                        stringResource(R.string.players_stats_none)
                    }
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.players_month_best),
                        value = monthlyBestValue,
                        emphasized = true
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(6.dp)) }
        }

        if (uiState.addPlayerDialog.visible) {
            AddPlayerDialog(
                dialogState = uiState.addPlayerDialog,
                onDismissRequest = onDismissAddPlayerDialog,
                onPlayerNameChange = onPlayerNameChange,
                onDecreaseInitialScore = onDecreaseInitialScore,
                onIncreaseInitialScore = onIncreaseInitialScore,
                onSelectAvatar = onSelectDialogAvatar,
                onConfirmAddPlayer = onConfirmAddPlayer
            )
        }
    }
}

@Composable
private fun PlayerSectionTitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        text = text,
        color = PlayerListColors.OnSurfaceVariant,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
}

@Composable
private fun EmptyPlayersState() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        text = stringResource(R.string.players_empty_state),
        color = PlayerListColors.OnSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
}
