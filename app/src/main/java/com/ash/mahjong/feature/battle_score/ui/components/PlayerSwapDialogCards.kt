package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ash.mahjong.R
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual

@Composable
internal fun SwapSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
internal fun <T> SwapCardsGrid(
    items: List<T>,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * 2 + columnIndex
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(item, index)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun SwapItemCard(
    title: String,
    subtitle: String,
    avatarKey: String,
    avatarEmoji: String,
    avatarFallbackText: String,
    rankLabel: String?,
    isMuted: Boolean,
    isDragging: Boolean,
    isDropTarget: Boolean,
    isSelected: Boolean,
    onBoundsChanged: (Rect) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragStop: () -> Unit,
    onClick: () -> Unit
) {
    var itemBounds by remember { mutableStateOf(Rect.Zero) }
    val shape = RoundedCornerShape(14.dp)
    val borderColor = when {
        isDragging -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        isDropTarget -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isMuted) 0.6f else 0.4f)
    }
    val cardColor = when {
        isDragging -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        isDropTarget -> MaterialTheme.colorScheme.secondaryContainer
        isMuted -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)
    val subtitleColor = when {
        isMuted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        subtitle.trim().startsWith("-") -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    }
    val avatarBackgroundColor = if (isMuted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                itemBounds = coordinates.boundsInWindow()
                onBoundsChanged(itemBounds)
            }
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragStart ->
                        onDragStart(itemBounds.topLeft + dragStart)
                    },
                    onDragCancel = onDragStop,
                    onDragEnd = onDragStop,
                    onDrag = { change, dragAmount ->
                        onDrag(dragAmount)
                        change.consume()
                    }
                )
            },
        shape = shape,
        color = cardColor
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = avatarBackgroundColor,
                            shape = CircleShape
                        )
                        .semantics { contentDescription = avatarContentDescription },
                    contentAlignment = Alignment.Center
                ) {
                    PlayerAvatarVisual(
                        avatarKey = avatarKey,
                        avatarEmoji = avatarEmoji,
                        fallbackText = avatarFallbackText,
                        textStyle = MaterialTheme.typography.labelLarge,
                        textColor = MaterialTheme.colorScheme.surface
                    )
                }

                if (rankLabel != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-6).dp, y = (-6).dp)
                            .size(18.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rankLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isMuted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
