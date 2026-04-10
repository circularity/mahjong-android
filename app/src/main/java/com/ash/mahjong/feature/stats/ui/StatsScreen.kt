package com.ash.mahjong.feature.stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ash.mahjong.R
import com.ash.mahjong.feature.stats.state.PlayerStatsUiModel
import com.ash.mahjong.feature.stats.state.StatsUiState
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.players.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.stats_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    modifier = Modifier.padding(top = spacing.sm),
                    text = stringResource(R.string.stats_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = spacing.xl,
                end = spacing.xl,
                top = spacing.lg,
                bottom = spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item {
                Text(
                    text = stringResource(R.string.stats_players_compare_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(uiState.players, key = { player -> player.playerId }) { player ->
                PlayerStatsCard(player = player)
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(player: PlayerStatsUiModel) {
    val spacing = MahjongDesign.spacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = MahjongDesign.elevation.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(
                        avatarKey = player.avatarKey,
                        avatarEmoji = player.avatarEmoji,
                        fallbackText = player.name.take(1)
                    )
                    Column {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.stats_player_rounds_subtitle_template,
                                player.totalRounds
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.stats_total_score_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = player.totalDeltaText,
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            player.totalDelta > 0 -> MaterialTheme.colorScheme.primary
                            player.totalDelta < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.stats_win_rate_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = player.winRateText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    CircularProgressIndicator(
                        progress = { player.winRateProgress },
                        modifier = Modifier.size(42.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer,
                        strokeWidth = 4.dp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_zimo_rounds),
                        value = player.zimoRounds.toString()
                    )
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_hu_rounds),
                        value = player.huRounds.toString()
                    )
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_gang_rounds),
                        value = player.gangRounds.toString()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_dianpao_rounds),
                        value = player.dianPaoRounds.toString()
                    )
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_total_rounds),
                        value = player.totalRounds.toString()
                    )
                    StatsMetric(
                        label = stringResource(R.string.stats_metric_win_rounds),
                        value = player.winRounds.toString()
                    )
                }
            }

            Text(
                text = stringResource(
                    R.string.stats_delta_template,
                    player.totalDeltaText,
                    player.avgDeltaText
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.stats_recent_rounds_template,
                    if (player.recentRoundsText.isBlank()) {
                        stringResource(R.string.players_stats_none)
                    } else {
                        player.recentRoundsText
                    }
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AvatarBadge(
    avatarKey: String,
    avatarEmoji: String,
    fallbackText: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        PlayerAvatarVisual(
            avatarKey = avatarKey,
            avatarEmoji = avatarEmoji,
            fallbackText = fallbackText,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun StatsMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
