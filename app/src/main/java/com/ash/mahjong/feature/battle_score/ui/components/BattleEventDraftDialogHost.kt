package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.intent.GangType
import com.ash.mahjong.feature.battle_score.state.EventDraftStep
import com.ash.mahjong.feature.battle_score.state.EventDraftUiState
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual

@Composable
fun BattleEventDraftDialogHost(
    draft: EventDraftUiState,
    players: List<PlayerCardUiModel>,
    multiplierRange: IntRange,
    onIntent: (BattleScoreIntent) -> Unit
) {
    val actor = players.firstOrNull { it.id == draft.actorId }
    val activeTargets = players.filter { it.id != draft.actorId && it.status == PlayerStatus.ACTIVE }

    when (draft.step) {
        EventDraftStep.GANG_TYPE -> {
            GangTypeDialog(
                selectedType = draft.gangType,
                onSelect = {
                    onIntent(BattleScoreIntent.SelectGangType(it))
                    onIntent(BattleScoreIntent.ConfirmDraftStep)
                },
                onCancel = { onIntent(BattleScoreIntent.CancelEventDraft) }
            )
        }

        EventDraftStep.TARGET -> {
            val isGangTargetStep = draft.action == BattleAction.GANG
            TargetDialog(
                actorName = actor?.name,
                title = if (draft.action == BattleAction.HU) {
                    stringResource(R.string.battle_draft_target_hu)
                } else {
                    stringResource(R.string.battle_draft_target_gang_dian)
                },
                targets = activeTargets,
                onSelect = { onIntent(BattleScoreIntent.SelectTarget(it)) },
                onDismiss = {
                    onIntent(
                        if (isGangTargetStep) {
                            BattleScoreIntent.BackEventDraftStep
                        } else {
                            BattleScoreIntent.CancelEventDraft
                        }
                    )
                }
            )
        }

        EventDraftStep.MULTIPLIER -> {
            if (draft.action != BattleAction.GANG) {
                MultiplierDialog(
                    range = multiplierRange,
                    selected = draft.multiplier,
                    onSelect = {
                        onIntent(BattleScoreIntent.SelectMultiplier(it))
                        onIntent(BattleScoreIntent.ConfirmEvent)
                    },
                    onBack = { onIntent(BattleScoreIntent.BackEventDraftStep) }
                )
            }
        }
    }
}

@Composable
private fun GangTypeDialog(
    selectedType: GangType?,
    onSelect: (GangType) -> Unit,
    onCancel: () -> Unit
) {
    DraftDialogContainer(
        onDismiss = onCancel,
        modifier = Modifier.testTag(BattleScoreTestTags.GANG_TYPE_DIALOG),
        shape = RoundedCornerShape(36.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        horizontalPadding = 20.dp,
        verticalPadding = 24.dp,
        contentSpacing = 14.dp
    ) {
        DraftDialogHeading(
            title = stringResource(R.string.battle_draft_gang_dialog_title),
            subtitle = stringResource(R.string.battle_draft_gang_dialog_subtitle)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GangType.entries.forEach { type ->
                StitchGangTypeCard(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onSelect(type) }
                )
            }
        }

    }
}

@Composable
private fun StitchGangTypeCard(
    type: GangType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(28.dp)
    val cardColor = when (type) {
        GangType.DIAN -> Color(0xFFF0F4F8)
        GangType.BA -> Color(0xFFE8F5E9)
        GangType.AN -> Color(0xFFEEF0F6)
    }
    val iconTint = when (type) {
        GangType.DIAN -> Color(0xFF315A45)
        GangType.BA -> Color(0xFF2F5C28)
        GangType.AN -> Color(0xFF465068)
    }
    val titleRes = when (type) {
        GangType.DIAN -> R.string.battle_gang_type_dian
        GangType.BA -> R.string.battle_gang_type_ba
        GangType.AN -> R.string.battle_gang_type_an
    }
    val descRes = when (type) {
        GangType.DIAN -> R.string.battle_gang_type_dian_desc
        GangType.BA -> R.string.battle_gang_type_ba_desc
        GangType.AN -> R.string.battle_gang_type_an_desc
    }

    Surface(
        shape = cardShape,
        color = cardColor,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0x1A233442),
                spotColor = Color(0x1A233442)
            )
            .border(
                width = if (isSelected) 1.2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else Color.White.copy(alpha = 0.45f),
                shape = cardShape
            )
            .clip(cardShape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x12000000),
                        spotColor = Color(0x12000000)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(titleRes).take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconTint
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TargetDialog(
    actorName: String?,
    title: String,
    targets: List<PlayerCardUiModel>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    DraftDialogContainer(
        onDismiss = onDismiss,
        modifier = Modifier.testTag(BattleScoreTestTags.TARGET_DIALOG),
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        horizontalPadding = 18.dp,
        verticalPadding = 22.dp,
        contentSpacing = 14.dp
    ) {
        DraftDialogHeading(
            title = title,
            subtitle = if (actorName.isNullOrBlank()) {
                stringResource(R.string.battle_draft_target_dialog_subtitle)
            } else {
                stringResource(R.string.battle_draft_target_dialog_subtitle_with_actor, actorName)
            }
        )

        if (targets.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.battle_draft_no_targets),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                targets.forEach { player ->
                    StitchTargetPlayerCard(player = player, onClick = { onSelect(player.id) })
                }
            }
        }

    }
}

@Composable
private fun MultiplierDialog(
    range: IntRange,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    val fanOptions = buildMultiplierOptions(range.last).mapIndexed { index, multiplier ->
        FanOptionUiModel(multiplier = multiplier, displayFan = index)
    }
    val maxDisplayFan = fanOptions.lastIndex.coerceAtLeast(0)
    DraftDialogContainer(
        onDismiss = onBack,
        modifier = Modifier.testTag(BattleScoreTestTags.MULTIPLIER_DIALOG),
        shape = RoundedCornerShape(34.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        horizontalPadding = 20.dp,
        verticalPadding = 22.dp,
        contentSpacing = 14.dp
    ) {
        DraftDialogHeading(
            title = stringResource(R.string.battle_draft_multiplier_dialog_title, 0, maxDisplayFan),
            subtitle = stringResource(R.string.battle_draft_multiplier_dialog_subtitle)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            fanOptions.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowOptions.forEach { option ->
                        MultiplierOptionCard(
                            displayFan = option.displayFan,
                            isSelected = selected == option.multiplier,
                            onClick = { onSelect(option.multiplier) },
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 0.dp)
                                .aspectRatio(1f)
                                .testTag(BattleScoreTestTags.multiplierOption(option.multiplier))
                        )
                    }
                    if (rowOptions.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

    }
}

@Composable
private fun DraftDialogHeading(
    title: String,
    subtitle: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MultiplierOptionCard(
    displayFan: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(22.dp)
    Surface(
        shape = cardShape,
        color = if (isSelected) Color(0xFFDFF2E1) else Color.White,
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0x1F233442),
                spotColor = Color(0x1F233442)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                else Color.White.copy(alpha = 0.7f),
                shape = cardShape
            )
            .clip(cardShape)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayFan.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.battle_draft_multiplier_unit),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class FanOptionUiModel(
    val multiplier: Int,
    val displayFan: Int
)

@Composable
private fun StitchTargetPlayerCard(
    player: PlayerCardUiModel,
    onClick: () -> Unit
) {
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)
    val cardShape = RoundedCornerShape(20.dp)
    Surface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                ambientColor = Color(0x1F233442),
                spotColor = Color(0x1F233442)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                shape = cardShape
            )
            .clip(cardShape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerAvatarVisual(
                        avatarKey = player.avatarKey,
                        avatarEmoji = player.avatarEmoji,
                        fallbackText = player.name.take(1),
                        contentDescription = avatarContentDescription,
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.battle_draft_target_score, player.totalScore),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.players_icon_chevron),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun buildMultiplierOptions(maxMultiplier: Int): List<Int> {
    val limit = maxMultiplier.coerceAtLeast(1)
    val options = mutableListOf<Int>()
    var current = 1
    while (current <= limit) {
        options.add(current)
        if (current > Int.MAX_VALUE / 2) break
        current *= 2
    }
    return options
}

@Composable
private fun DraftDialogContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    horizontalPadding: androidx.compose.ui.unit.Dp = 22.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    contentSpacing: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = shape,
                color = containerColor,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .then(modifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    verticalArrangement = Arrangement.spacedBy(contentSpacing)
                ) {
                    content()
                }
            }
        }
    }
}
