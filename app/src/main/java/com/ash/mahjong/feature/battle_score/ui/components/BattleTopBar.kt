package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.theme.MahjongDesign
import kotlin.math.floor

@Composable
fun BattleTopBar(
    currentRound: Int,
    canSettle: Boolean,
    canSwapPlayers: Boolean,
    swapAttentionActive: Boolean,
    canReset: Boolean,
    onSettleClick: () -> Unit,
    onSwapPlayersClick: () -> Unit,
    onResetClick: () -> Unit,
    onQuickHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = MahjongDesign.elevation.bar
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MahjongDesign.spacing.topBarHeight)
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.match),
                        contentDescription = stringResource(R.string.battle_history_entry),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onQuickHistoryClick)
                    )
                    Text(
                        text = stringResource(R.string.battle_round_label, currentRound),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    TopBarOutlinedActionButton(
                        iconVector = Icons.Filled.SwapHoriz,
                        text = stringResource(R.string.battle_swap_players_action),
                        enabled = canSwapPlayers,
                        onClick = onSwapPlayersClick,
                        attentionEnabled = swapAttentionActive,
                        testTag = BattleScoreTestTags.SWAP_PLAYERS_BUTTON
                    )
                    TopBarOutlinedActionButton(
                        iconRes = R.drawable.reset,
                        text = stringResource(R.string.battle_reset_action),
                        enabled = canReset,
                        onClick = onResetClick
                    )
                    FilledTonalButton(
                        onClick = onSettleClick,
                        enabled = canSettle,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag(BattleScoreTestTags.FLOATING_ACTION)
                    ) {
                        Text(
                            text = stringResource(R.string.battle_fab_settle),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun TopBarOutlinedActionButton(
    @DrawableRes iconRes: Int? = null,
    iconVector: ImageVector? = null,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    attentionEnabled: Boolean = false,
    testTag: String? = null
) {
    val attentionActive = attentionEnabled && enabled
    val attentionTransition = if (attentionActive) {
        rememberInfiniteTransition(label = "top_bar_attention_transition")
    } else {
        null
    }
    val pulseScale = if (attentionTransition != null) {
        val animated by attentionTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1800,
                    easing = FastOutSlowInEasing
                )
            ),
            label = "top_bar_attention_scale"
        )
        animated
    } else {
        1f
    }
    val iconRotation = if (attentionTransition != null) {
        val animated by attentionTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2400
                    0f at 0
                    (-6f) at 120 using FastOutSlowInEasing
                    6f at 270 using FastOutSlowInEasing
                    0f at 420 using FastOutSlowInEasing
                    0f at 2400
                }
            ),
            label = "top_bar_attention_icon_swing"
        )
        animated
    } else {
        0f
    }
    val borderPulse = if (attentionTransition != null) {
        val animated by attentionTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1400,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "top_bar_attention_border_pulse"
        )
        animated
    } else {
        0f
    }
    val colorShift = if (attentionTransition != null) {
        val animated by attentionTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5200,
                    easing = FastOutSlowInEasing
                )
            ),
            label = "top_bar_attention_color_shift"
        )
        animated
    } else {
        0f
    }
    val borderColor = if (attentionTransition != null) {
        interpolatePaletteColor(
            progress = colorShift,
            palette = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.error.copy(alpha = 0.94f),
                MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.96f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.95f)
            )
        )
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val borderWidth = if (attentionTransition != null) {
        (1f + borderPulse * 0.8f).dp
    } else {
        1.dp
    }
    val containerColor = if (attentionTransition != null) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    }
    val contentColor = if (attentionTransition != null) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val buttonModifier = Modifier
        .graphicsLayer(
            scaleX = pulseScale,
            scaleY = pulseScale
        )
        .then(
            if (testTag != null) Modifier.testTag(testTag) else Modifier
        )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = buttonModifier,
        border = BorderStroke(
            width = borderWidth,
            color = borderColor
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer { rotationZ = iconRotation }
                )
            } else {
                val resolvedIconRes = requireNotNull(iconRes) {
                    "TopBarOutlinedActionButton requires either iconRes or iconVector."
                }
                Icon(
                    painter = painterResource(resolvedIconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer { rotationZ = iconRotation }
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

private fun interpolatePaletteColor(
    progress: Float,
    palette: List<Color>
): Color {
    if (palette.size < 2) return palette.firstOrNull() ?: Color.Transparent
    val clampedProgress = progress.coerceIn(0f, 1f)
    val segmentCount = palette.size - 1
    val segmentProgress = clampedProgress * segmentCount
    val startIndex = floor(segmentProgress).toInt().coerceIn(0, segmentCount - 1)
    val localProgress = segmentProgress - startIndex
    return lerp(
        start = palette[startIndex],
        stop = palette[startIndex + 1],
        fraction = localProgress
    )
}
