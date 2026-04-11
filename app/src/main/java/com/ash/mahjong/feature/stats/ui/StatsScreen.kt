package com.ash.mahjong.feature.stats.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.ash.mahjong.ui.theme.StatsCanvasBackground
import com.ash.mahjong.ui.theme.StatsSurfaceMuted
import com.ash.mahjong.ui.theme.StatsWinRateZeroRing
import com.ash.mahjong.ui.theme.StatsZimoBackground
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StatsCanvasBackground)
            .statusBarsPadding()
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.stats_players_compare_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.stats_players_compare_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(uiState.players, key = { _, player -> player.playerId }) { index, player ->
                PlayerStatsCard(
                    player = player,
                    rank = index + 1
                )
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(
    player: PlayerStatsUiModel,
    rank: Int
) {
    val spacing = MahjongDesign.spacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 6.dp,
        border = if (rank == 1) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
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
                                R.string.stats_last_battle_time_template,
                                if (player.lastBattleTimeText.isBlank()) {
                                    stringResource(R.string.players_stats_none)
                                } else {
                                    player.lastBattleTimeText
                                }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.stats_total_score_label).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = player.totalDeltaText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = when {
                            player.totalDelta > 0 -> MaterialTheme.colorScheme.primary
                            player.totalDelta < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(0.42f),
                    color = StatsCanvasBackground,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { player.winRateProgress },
                            modifier = Modifier.size(96.dp),
                            color = if (player.winRateProgress <= 0f) {
                                StatsWinRateZeroRing
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            trackColor = if (player.winRateProgress <= 0f) {
                                StatsWinRateZeroRing
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = player.winRateText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.stats_win_rate_label),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(0.58f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickMetricTile(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp),
                            label = stringResource(R.string.stats_metric_zimo_rounds),
                            value = player.zimoRounds.toString(),
                            containerColor = StatsZimoBackground,
                            valueColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        QuickMetricTile(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp),
                            label = stringResource(R.string.stats_metric_hu_rounds),
                            value = player.huRounds.toString(),
                            containerColor = StatsCanvasBackground,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickMetricTile(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp),
                            label = stringResource(R.string.stats_metric_dianpao_rounds),
                            value = player.dianPaoRounds.toString(),
                            containerColor = StatsCanvasBackground,
                            valueColor = MaterialTheme.colorScheme.error
                        )
                        QuickMetricTile(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp),
                            label = stringResource(R.string.stats_metric_gang_rounds),
                            value = player.gangRounds.toString(),
                            containerColor = StatsCanvasBackground,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StatsSurfaceMuted,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.stats_metric_total_rounds),
                        value = player.totalRounds.toString(),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                    DividerColumn()
                    SummaryMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.stats_metric_win_rounds),
                        value = player.winRounds.toString(),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    DividerColumn()
                    SummaryMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.stats_avg_delta_label),
                        value = player.avgDeltaText,
                        valueColor = if (player.avgDeltaText.startsWith("-")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = stringResource(R.string.stats_recent_rounds_label),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (player.recentRounds.isEmpty()) {
                    Text(
                        text = stringResource(R.string.players_stats_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    RecentRoundsChart(
                        recentRounds = player.recentRounds,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
private fun QuickMetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = valueColor
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = valueColor
        )
    }
}

@Composable
private fun DividerColumn() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
    )
}

@Composable
private fun RecentRoundsChart(
    recentRounds: List<Int>,
    modifier: Modifier = Modifier
) {
    val rounds = recentRounds.reversed()
    val maxAbs = max(1, rounds.maxOf { value -> abs(value) })
    Row(
        modifier = modifier.height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        rounds.forEach { roundDelta ->
            val ratio = abs(roundDelta).toFloat() / maxAbs.toFloat()
            val barHeight = (6f + (18f * ratio)).dp
            Column(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (roundDelta >= 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (roundDelta == 0) 4.dp else barHeight)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (roundDelta == 0) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (roundDelta < 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight)
                                .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    }
}
