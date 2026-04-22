package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.state.DrawSettlementDraftUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementStep
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual

@Composable
fun DrawSettlementDialogHost(
    draft: DrawSettlementDraftUiState,
    players: List<PlayerCardUiModel>,
    multiplierRange: IntRange,
    onIntent: (BattleScoreIntent) -> Unit
) {
    val currentPlayerId = draft.orderedPendingPlayerIds.getOrNull(draft.currentIndex) ?: return
    val currentPlayer = players.firstOrNull { it.id == currentPlayerId } ?: return
    val progress = stringResource(
        R.string.battle_draw_progress,
        draft.currentIndex + 1,
        draft.orderedPendingPlayerIds.size
    )

    when (draft.step) {
        DrawSettlementStep.CHOOSE_TING -> {
            DrawTingChoiceDialog(
                playerName = currentPlayer.name,
                avatarKey = currentPlayer.avatarKey,
                avatarEmoji = currentPlayer.avatarEmoji,
                totalScore = currentPlayer.totalScore,
                progress = progress,
                selectedTingChoice = draft.currentTingChoice,
                canBack = draft.currentIndex > 0,
                onChooseTing = { onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true)) },
                onChooseNoTing = { onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false)) },
                onConfirm = { onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection) },
                onBack = { onIntent(BattleScoreIntent.BackDrawSettlementStep) },
                onCancel = { onIntent(BattleScoreIntent.CancelDrawSettlementDraft) }
            )
        }

        DrawSettlementStep.CHOOSE_MULTIPLIER -> {
            DrawMultiplierChoiceDialog(
                playerName = currentPlayer.name,
                avatarKey = currentPlayer.avatarKey,
                avatarEmoji = currentPlayer.avatarEmoji,
                progress = progress,
                range = multiplierRange,
                selected = draft.currentMultiplier,
                onSelect = { onIntent(BattleScoreIntent.SelectDrawTingMultiplier(it)) },
                onConfirm = { onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection) },
                onBack = { onIntent(BattleScoreIntent.BackDrawSettlementStep) },
                onCancel = { onIntent(BattleScoreIntent.CancelDrawSettlementDraft) }
            )
        }
    }
}

@Composable
private fun DrawTingChoiceDialog(
    playerName: String,
    avatarKey: String,
    avatarEmoji: String,
    totalScore: String,
    progress: String,
    selectedTingChoice: Boolean?,
    canBack: Boolean,
    onChooseTing: () -> Unit,
    onChooseNoTing: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    DrawDialogContainer(
        tag = BattleScoreTestTags.DRAW_TING_CHOICE_DIALOG,
        onDismiss = onCancel
    ) {
        DialogTitle(
            title = stringResource(R.string.battle_draw_choose_ting_title, playerName),
            subtitle = stringResource(R.string.battle_draw_choose_ting_subtitle, progress)
        )
        DrawCurrentPlayerHeader(
            playerName = playerName,
            avatarKey = avatarKey,
            avatarEmoji = avatarEmoji
        )
        Text(
            text = stringResource(R.string.battle_draw_current_score, totalScore),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DrawChoiceCard(
            title = stringResource(R.string.battle_draw_choice_ting),
            subtitle = stringResource(R.string.battle_draw_choice_ting_desc),
            selected = selectedTingChoice == true,
            onClick = onChooseTing
        )
        DrawChoiceCard(
            title = stringResource(R.string.battle_draw_choice_no_ting),
            subtitle = stringResource(R.string.battle_draw_choice_no_ting_desc),
            selected = selectedTingChoice == false,
            onClick = onChooseNoTing
        )
        DrawActionRow(
            canConfirm = selectedTingChoice != null,
            canBack = canBack,
            onConfirm = onConfirm,
            onBack = onBack,
            onCancel = onCancel
        )
    }
}

@Composable
private fun DrawMultiplierChoiceDialog(
    playerName: String,
    avatarKey: String,
    avatarEmoji: String,
    progress: String,
    range: IntRange,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    val optionsMaxHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.62f)
        .coerceIn(360.dp, 520.dp)
    val fanOptions = buildDrawMultiplierOptions(range.last).mapIndexed { index, multiplier ->
        DrawFanOptionUiModel(multiplier = multiplier, displayFan = index)
    }
    val maxDisplayFan = fanOptions.lastIndex.coerceAtLeast(0)
    DrawDialogContainer(
        tag = BattleScoreTestTags.DRAW_MULTIPLIER_DIALOG,
        onDismiss = onCancel
    ) {
        DialogTitle(
            title = stringResource(R.string.battle_draw_choose_score_title, playerName),
            subtitle = stringResource(R.string.battle_draw_choose_score_subtitle, progress)
        )
        DrawCurrentPlayerHeader(
            playerName = playerName,
            avatarKey = avatarKey,
            avatarEmoji = avatarEmoji
        )
        Text(
            text = stringResource(
                R.string.battle_draw_score_formula_hint,
                0,
                maxDisplayFan
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = optionsMaxHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            fanOptions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { option ->
                        DrawMultiplierCard(
                            displayFan = option.displayFan,
                            selected = selected == option.multiplier,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(BattleScoreTestTags.drawMultiplierOption(option.multiplier)),
                            onClick = { onSelect(option.multiplier) }
                        )
                    }
                    if (row.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        DrawActionRow(
            canConfirm = selected != null,
            canBack = true,
            onConfirm = onConfirm,
            onBack = onBack,
            onCancel = onCancel
        )
    }
}

@Composable
private fun DrawDialogContainer(
    tag: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .testTag(BattleScoreTestTags.DRAW_SETTLEMENT_DIALOG),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .testTag(tag),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun DialogTitle(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
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
private fun DrawCurrentPlayerHeader(
    playerName: String,
    avatarKey: String,
    avatarEmoji: String
) {
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                PlayerAvatarVisual(
                    avatarKey = avatarKey,
                    avatarEmoji = avatarEmoji,
                    fallbackText = playerName.take(1),
                    contentDescription = avatarContentDescription,
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DrawChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DrawMultiplierCard(
    displayFan: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val showFanUnit = displayFan != 0
    val displayFanLabel = if (displayFan == 0) {
        stringResource(R.string.battle_fan_zero_label)
    } else {
        displayFan.toString()
    }
    val shape = RoundedCornerShape(16.dp)
    Surface(
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayFanLabel,
                style = MaterialTheme.typography.titleLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            if (showFanUnit) {
                Text(
                    text = stringResource(R.string.battle_draft_multiplier_unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class DrawFanOptionUiModel(
    val multiplier: Int,
    val displayFan: Int
)

@Composable
private fun DrawActionRow(
    canConfirm: Boolean,
    canBack: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.battle_draft_cancel))
        }
        OutlinedButton(
            onClick = onBack,
            enabled = canBack,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.battle_draft_back))
        }
        Button(
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.battle_draft_confirm))
        }
    }
}

private fun buildDrawMultiplierOptions(maxMultiplier: Int): List<Int> {
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
