package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.LiveLogActionType
import com.ash.mahjong.feature.battle_score.state.LiveLogHighlight
import com.ash.mahjong.feature.battle_score.state.LiveLogItemUiModel
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun LiveLogSection(
    items: List<LiveLogItemUiModel>,
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.battle_live_log_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.battle_live_log_recent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onUndoClick, enabled = canUndo) {
                Text(text = stringResource(R.string.battle_action_undo))
            }
        }

        items.forEach { item ->
            LiveLogItem(item = item)
        }
    }
}

@Composable
private fun LiveLogItem(item: LiveLogItemUiModel) {
    val spacing = MahjongDesign.spacing

    Card(
        shape = MahjongDesign.shapes.logCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = MahjongDesign.elevation.logCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(MahjongDesign.spacing.logDot)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MahjongDesign.shapes.logDot
                        )
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    val actionLabel = stringResource(actionLabelRes(item.actionType))
                    Text(
                        text = stringResource(
                            R.string.battle_log_title_template,
                            item.actorName,
                            actionLabel
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitleText(item),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = item.amount,
                style = MaterialTheme.typography.titleMedium,
                color = when (item.highlight) {
                    LiveLogHighlight.POSITIVE -> MaterialTheme.colorScheme.primary
                    LiveLogHighlight.NEGATIVE -> MaterialTheme.colorScheme.error
                    LiveLogHighlight.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun subtitleText(item: LiveLogItemUiModel): String {
    if (item.relatedPlayerNames.isEmpty()) {
        return stringResource(R.string.battle_log_related_none)
    }
    val separator = stringResource(R.string.battle_log_name_separator)
    val joinedNames = item.relatedPlayerNames.joinToString(separator = separator)
    return stringResource(relatedTemplateRes(item.actionType), joinedNames)
}

private fun actionLabelRes(actionType: LiveLogActionType): Int = when (actionType) {
    LiveLogActionType.HU -> R.string.battle_action_hu
    LiveLogActionType.ZIMO -> R.string.battle_action_zimo
    LiveLogActionType.GANG_DIAN -> R.string.battle_gang_type_dian
    LiveLogActionType.GANG_BA -> R.string.battle_gang_type_ba
    LiveLogActionType.GANG_AN -> R.string.battle_gang_type_an
    LiveLogActionType.GANG_REFUND -> R.string.battle_action_gang_refund
    LiveLogActionType.DRAW_SETTLEMENT -> R.string.battle_action_draw_settlement
}

private fun relatedTemplateRes(actionType: LiveLogActionType): Int = when (actionType) {
    LiveLogActionType.HU -> R.string.battle_log_related_hu_template
    LiveLogActionType.ZIMO -> R.string.battle_log_related_zimo_template
    LiveLogActionType.GANG_DIAN -> R.string.battle_log_related_gang_dian_template
    LiveLogActionType.GANG_BA -> R.string.battle_log_related_gang_ba_template
    LiveLogActionType.GANG_AN -> R.string.battle_log_related_gang_an_template
    LiveLogActionType.GANG_REFUND -> R.string.battle_log_related_gang_refund_template
    LiveLogActionType.DRAW_SETTLEMENT -> R.string.battle_log_related_draw_settlement_template
}
