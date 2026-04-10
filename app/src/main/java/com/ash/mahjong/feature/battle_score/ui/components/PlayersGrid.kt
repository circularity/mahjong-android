package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual
import com.ash.mahjong.ui.theme.MahjongDesign
import java.util.Locale

@Composable
fun PlayersGrid(
    players: List<PlayerCardUiModel>,
    onHuClick: (Int) -> Unit,
    onGangClick: (Int) -> Unit,
    onZimoClick: (Int) -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        players.forEach { player ->
            PlayerCard(
                player = player,
                onHuClick = onHuClick,
                onGangClick = onGangClick,
                onZimoClick = onZimoClick,
                actionsEnabled = actionsEnabled
            )
        }
    }
}

@Composable
private fun PlayerCard(
    player: PlayerCardUiModel,
    onHuClick: (Int) -> Unit,
    onGangClick: (Int) -> Unit,
    onZimoClick: (Int) -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    val isActionEnabled = actionsEnabled && player.status == PlayerStatus.ACTIVE
    val showStatusStamp = player.status == PlayerStatus.HU || player.status == PlayerStatus.ZIMO
    val showLockedOverlay = player.status == PlayerStatus.LOCKED
    val showWinOrderBadge = player.winOrder != null &&
        (player.status == PlayerStatus.HU || player.status == PlayerStatus.ZIMO)
    val overlayColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    if (showLockedOverlay) {
                        drawRect(overlayColor)
                    }
                }
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Column(
                    modifier = Modifier.size(width = 92.dp, height = 108.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(spacing.xs)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PlayerAvatarVisual(
                                avatarKey = player.avatarKey,
                                avatarEmoji = player.avatarEmoji,
                                fallbackText = player.name.take(1),
                                contentDescription = avatarContentDescription,
                                textStyle = MaterialTheme.typography.headlineMedium
                            )
                        }

                        if (player.isDealer) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .zIndex(1f)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.player_role_dealer),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        if (showStatusStamp) {
                            PlayerStatusStamp(
                                status = player.status,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .zIndex(1f)
                            )
                        }
                    }
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 98.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        PlayerHorseChips(
                            horseNames = player.boundHorseNames,
                            modifier = Modifier.weight(1f)
                        )

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            if (showWinOrderBadge) {
                                WinOrderBadge(
                                    winOrder = player.winOrder ?: 0,
                                    modifier = Modifier
                                        .testTag(BattleScoreTestTags.winOrderBadge(player.id))
                                )
                            }
                            Text(
                                text = player.roundDelta,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = scoreColor(player.roundDelta)
                            )
                            Text(
                                text = stringResource(
                                    R.string.battle_player_total_score_template,
                                    player.totalScore
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        PlayerActionButton(
                            text = stringResource(R.string.battle_action_hu),
                            onClick = { onHuClick(player.id) },
                            enabled = isActionEnabled,
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(BattleScoreTestTags.huButton(player.id))
                        )
                        PlayerActionButton(
                            text = stringResource(R.string.battle_action_zimo),
                            onClick = { onZimoClick(player.id) },
                            enabled = isActionEnabled,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(BattleScoreTestTags.zimoButton(player.id))
                        )
                        PlayerActionButton(
                            text = stringResource(R.string.battle_action_gang),
                            onClick = { onGangClick(player.id) },
                            enabled = isActionEnabled,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(BattleScoreTestTags.gangButton(player.id))
                        )
                    }
                }
            }

            if (showLockedOverlay) {
                AvatarStatusOverlay(
                    status = player.status,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(1f)
                )
            }
        }
    }
}

@Composable
private fun PlayerHorseChips(
    horseNames: List<String>,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    val noHorseText = stringResource(R.string.battle_player_bound_horses_empty_chip)
    val overflowCountFormat = stringResource(R.string.battle_horse_overflow_count)

    if (horseNames.isEmpty()) {
        HorseChip(
            text = noHorseText,
            emphasized = false,
            italic = true,
            modifier = modifier
        )
        return
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val chipSpacingPx = with(density) { spacing.xs.toPx() }
        val maxRows = 2

        val rows = remember(horseNames, maxWidthPx) {
            val chipRows = mutableListOf(mutableListOf<String>())
            val rowWidths = mutableListOf(0f)
            var overflowCount = 0

            fun estimateChipWidthPx(label: String): Float {
                val charWidth = 7.5f * density.density
                val baseWidth = 20f * density.density
                return baseWidth + (label.length * charWidth)
            }

            for (index in horseNames.indices) {
                val label = horseNames[index]
                val chipWidth = estimateChipWidthPx(label)
                val rowIndex = chipRows.lastIndex
                val row = chipRows[rowIndex]
                val projectedWidth = if (row.isEmpty()) {
                    chipWidth
                } else {
                    rowWidths[rowIndex] + chipSpacingPx + chipWidth
                }

                if (projectedWidth <= maxWidthPx || row.isEmpty()) {
                    if (row.isNotEmpty()) {
                        rowWidths[rowIndex] += chipSpacingPx
                    }
                    row.add(label)
                    rowWidths[rowIndex] += chipWidth
                } else if (chipRows.size < maxRows) {
                    chipRows.add(mutableListOf(label))
                    rowWidths.add(chipWidth)
                } else {
                    overflowCount = horseNames.size - index
                    break
                }
            }

            if (overflowCount > 0) {
                val lastRowIndex = chipRows.lastIndex
                while (true) {
                    val overflowLabel = String.format(
                        Locale.getDefault(),
                        overflowCountFormat,
                        overflowCount
                    )
                    val overflowWidth = estimateChipWidthPx(overflowLabel)
                    val lastRow = chipRows[lastRowIndex]
                    val projectedWidth = if (lastRow.isEmpty()) {
                        overflowWidth
                    } else {
                        rowWidths[lastRowIndex] + chipSpacingPx + overflowWidth
                    }
                    if (projectedWidth <= maxWidthPx || lastRow.isEmpty()) {
                        if (lastRow.isNotEmpty()) {
                            rowWidths[lastRowIndex] += chipSpacingPx
                        }
                        lastRow.add(overflowLabel)
                        break
                    }

                    val removed = lastRow.removeLastOrNull() ?: break
                    val removedWidth = estimateChipWidthPx(removed)
                    rowWidths[lastRowIndex] -= removedWidth
                    if (lastRow.isNotEmpty()) {
                        rowWidths[lastRowIndex] -= chipSpacingPx
                    }
                    overflowCount += 1
                }
            }

            chipRows.map { it.toList() }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            rows.forEach { rowLabels ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    rowLabels.forEach { label ->
                        val isOverflowLabel = label.startsWith("+")
                        HorseChip(
                            text = label,
                            emphasized = true,
                            italic = false,
                            modifier = Modifier,
                            overflow = isOverflowLabel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorseChip(
    text: String,
    emphasized: Boolean,
    italic: Boolean,
    modifier: Modifier = Modifier,
    overflow: Boolean = false
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    overflow -> MaterialTheme.colorScheme.tertiaryContainer
                    emphasized -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainer
                },
                shape = RoundedCornerShape(999.dp)
            )
            .heightIn(min = 24.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = when {
                overflow -> MaterialTheme.colorScheme.onTertiaryContainer
                emphasized -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )
    }
}
