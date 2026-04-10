package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.HorseUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags

@Composable
fun PlayerSwapDialog(
    onTablePlayers: List<PlayerCardUiModel>,
    horses: List<HorseUiModel>,
    onSwap: (onTablePlayerId: Int, horseId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val itemBounds = remember { mutableStateMapOf<SwapItemKey, Rect>() }
    var draggingSource by remember { mutableStateOf<SwapItemKey?>(null) }
    var pointerInWindow by remember { mutableStateOf<Offset?>(null) }
    var selectedSource by remember { mutableStateOf<SwapItemKey?>(null) }
    val activeDropTarget = draggingSource?.let { source ->
        val pointer = pointerInWindow ?: return@let null
        itemBounds.entries.firstOrNull { entry ->
            entry.key.column != source.column && entry.value.contains(pointer)
        }?.key
    }

    fun performSwap(source: SwapItemKey, target: SwapItemKey) {
        val onTablePlayerId = if (source.column == SwapColumn.ON_TABLE) source.id else target.id
        val horseId = if (source.column == SwapColumn.HORSE) source.id else target.id
        onSwap(onTablePlayerId, horseId)
    }

    fun finishDrag() {
        val source = draggingSource
        val pointer = pointerInWindow
        val target = if (source != null && pointer != null) {
            itemBounds.entries.firstOrNull { entry ->
                entry.key.column != source.column && entry.value.contains(pointer)
            }?.key
        } else {
            null
        }
        if (source != null && target != null) {
            performSwap(source, target)
        }
        draggingSource = null
        pointerInWindow = null
        selectedSource = null
    }

    fun onItemClick(key: SwapItemKey) {
        val currentSelection = selectedSource
        when {
            currentSelection == null -> selectedSource = key
            currentSelection == key -> selectedSource = null
            currentSelection.column != key.column -> {
                performSwap(currentSelection, key)
                selectedSource = null
            }
            else -> selectedSource = key
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f))
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 420.dp)
                    .testTag(BattleScoreTestTags.PLAYER_SWAP_DIALOG),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.98f),
                tonalElevation = 6.dp,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.battle_swap_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.battle_swap_dialog_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.battle_swap_drag_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SwapSection(
                        title = stringResource(
                            R.string.battle_swap_column_count_template,
                            stringResource(R.string.battle_swap_column_on_table),
                            onTablePlayers.size
                        )
                    ) {
                        SwapCardsGrid(items = onTablePlayers) { player, index ->
                            val key = SwapItemKey(
                                column = SwapColumn.ON_TABLE,
                                id = player.id
                            )
                            SwapItemCard(
                                title = player.name,
                                subtitle = player.totalScore,
                                avatarText = player.avatarEmoji.ifBlank { player.name.take(1) },
                                rankLabel = (index + 1).toString(),
                                isMuted = false,
                                isDragging = draggingSource == key,
                                isDropTarget = activeDropTarget == key,
                                isSelected = selectedSource == key,
                                onBoundsChanged = { rect -> itemBounds[key] = rect },
                                onDragStart = { pointer ->
                                    draggingSource = key
                                    pointerInWindow = pointer
                                },
                                onDrag = { dragAmount ->
                                    pointerInWindow = pointerInWindow?.plus(dragAmount)
                                },
                                onDragStop = ::finishDrag,
                                onClick = { onItemClick(key) }
                            )
                        }
                    }
                    SwapSection(
                        title = stringResource(R.string.battle_swap_column_horse_candidates)
                    ) {
                        SwapCardsGrid(items = horses) { horse, _ ->
                            val key = SwapItemKey(
                                column = SwapColumn.HORSE,
                                id = horse.id
                            )
                            SwapItemCard(
                                title = horse.name,
                                subtitle = horse.totalScore,
                                avatarText = horse.avatarEmoji.ifBlank { horse.name.take(1) },
                                rankLabel = null,
                                isMuted = true,
                                isDragging = draggingSource == key,
                                isDropTarget = activeDropTarget == key,
                                isSelected = selectedSource == key,
                                onBoundsChanged = { rect -> itemBounds[key] = rect },
                                onDragStart = { pointer ->
                                    draggingSource = key
                                    pointerInWindow = pointer
                                },
                                onDrag = { dragAmount ->
                                    pointerInWindow = pointerInWindow?.plus(dragAmount)
                                },
                                onDragStop = ::finishDrag,
                                onClick = { onItemClick(key) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.battle_reset_confirm_cancel),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.battle_swap_confirm_action),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class SwapColumn {
    ON_TABLE,
    HORSE
}

private data class SwapItemKey(
    val column: SwapColumn,
    val id: Int
)
