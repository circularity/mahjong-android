package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
internal fun PlayerActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
        modifier = modifier.height(40.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = contentColor
        )
    }
}

@Composable
internal fun WinOrderBadge(
    winOrder: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(MahjongDesign.spacing.xl)
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = winOrder.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
internal fun HorseBindingWell(
    horseLabel: String,
    hasBoundHorse: Boolean
) {
    val spacing = MahjongDesign.spacing
    Box(
        modifier = Modifier
            .background(
                color = if (hasBoundHorse) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = RoundedCornerShape(999.dp)
            )
            .heightIn(min = 24.dp)
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = horseLabel,
            style = MaterialTheme.typography.labelSmall,
            fontStyle = if (hasBoundHorse) FontStyle.Normal else FontStyle.Italic,
            color = if (hasBoundHorse) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )
    }
}

@Composable
internal fun PlayerStatusStamp(
    status: PlayerStatus,
    modifier: Modifier = Modifier
) {
    val textRes = when (status) {
        PlayerStatus.HU -> R.string.player_status_hu
        PlayerStatus.ZIMO -> R.string.player_status_zimo
        else -> null
    }
    val stampColor = MaterialTheme.colorScheme.primary
    if (textRes != null) {
        Box(
            modifier = modifier
                .rotate(-12f)
                .border(
                    width = 3.dp,
                    color = stampColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = stampColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun AvatarStatusOverlay(
    status: PlayerStatus,
    modifier: Modifier = Modifier
) {
    val textRes = when (status) {
        PlayerStatus.LOCKED -> R.string.player_status_locked
        else -> null
    }

    if (textRes != null) {
        Box(
            modifier = modifier.rotate(
                when (status) {
                    PlayerStatus.HU -> 10f
                    PlayerStatus.ZIMO -> -10f
                    PlayerStatus.LOCKED -> 0f
                    PlayerStatus.ACTIVE -> 0f
                }
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = when (status) {
                    PlayerStatus.HU -> MaterialTheme.colorScheme.error
                    PlayerStatus.ZIMO -> MaterialTheme.colorScheme.primary
                    PlayerStatus.LOCKED -> MaterialTheme.colorScheme.onSurface
                    PlayerStatus.ACTIVE -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun scoreColor(delta: String) = when {
    delta.startsWith("-") -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
}
