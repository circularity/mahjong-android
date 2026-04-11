package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.state.HorseUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.state.SettlementPromptType
import com.ash.mahjong.feature.battle_score.state.SettlementPromptUiState
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual

@Composable
fun SettlementPromptDialog(
    prompt: SettlementPromptUiState,
    players: List<PlayerCardUiModel>,
    horses: List<HorseUiModel>,
    onIntent: (BattleScoreIntent) -> Unit
) {
    when (prompt.type) {
        SettlementPromptType.AUTO_THREE_HU,
        SettlementPromptType.DRAW_RESULT_CONFIRM -> {
            RoundSettlementDialog(
                players = players,
                horses = horses,
                onDismiss = { onIntent(BattleScoreIntent.DismissSettlementPrompt) },
                onStartNextRound = { onIntent(BattleScoreIntent.ConfirmSettleAndNextRound) },
                onViewHistory = { onIntent(BattleScoreIntent.DismissSettlementPrompt) }
            )
        }

        SettlementPromptType.MANUAL_DRAW_PLACEHOLDER -> {
            AlertDialog(
                onDismissRequest = { onIntent(BattleScoreIntent.DismissSettlementPrompt) },
                title = { Text(text = stringResource(R.string.battle_draw_placeholder_title)) },
                text = { Text(text = stringResource(R.string.battle_draw_placeholder_message)) },
                confirmButton = {
                    Button(onClick = { onIntent(BattleScoreIntent.DismissSettlementPrompt) }) {
                        Text(text = stringResource(R.string.battle_draw_placeholder_action))
                    }
                }
            )
        }
    }
}

@Composable
private fun RoundSettlementDialog(
    players: List<PlayerCardUiModel>,
    horses: List<HorseUiModel>,
    onDismiss: () -> Unit,
    onStartNextRound: () -> Unit,
    onViewHistory: () -> Unit
) {
    val orderedEntries = remember(players, horses) {
        buildSettlementEntries(players = players, horses = horses)
            .sortedByDescending { it.roundDelta.toDeltaValue() }
    }
    val winnerEntryKey = orderedEntries.firstOrNull { it.roundDelta.toDeltaValue() > 0 }?.entryKey
    val lowestDelta = orderedEntries.minOfOrNull { it.roundDelta.toDeltaValue() } ?: 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettlementHeader(onDismiss = onDismiss)
                    SettlementList(
                        entries = orderedEntries,
                        winnerEntryKey = winnerEntryKey,
                        lowestDelta = lowestDelta
                    )
                    SettlementFooter(
                        onStartNextRound = onStartNextRound,
                        onViewHistory = onViewHistory
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onDismiss,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.battle_settlement_close),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.battle_settlement_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun SettlementList(
    entries: List<SettlementEntryUiModel>,
    winnerEntryKey: String?,
    lowestDelta: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        entries.forEach { entry ->
            val delta = entry.roundDelta.toDeltaValue()
            SettlementPlayerRow(
                entry = entry,
                isWinner = entry.entryKey == winnerEntryKey,
                isCriticalLoss = delta < 0 && delta == lowestDelta
            )
        }
    }
}

@Composable
private fun SettlementPlayerRow(
    entry: SettlementEntryUiModel,
    isWinner: Boolean,
    isCriticalLoss: Boolean
) {
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)
    val statusLabelRes = entry.status?.let(::settlementStatusRes)
    val delta = entry.roundDelta
    val deltaColor = when {
        delta.toDeltaValue() > 0 -> MaterialTheme.colorScheme.primary
        isCriticalLoss -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val rowColor = if (isWinner) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(rowColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerAvatarVisual(
                        avatarKey = entry.avatarKey,
                        avatarEmoji = entry.avatarEmoji,
                        fallbackText = entry.name.take(1),
                        contentDescription = avatarContentDescription,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (isWinner) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.isHorse) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = stringResource(R.string.battle_settlement_horse_tag),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    statusLabelRes?.let { statusRes ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = stringResource(statusRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.battle_settlement_total_score, entry.totalScore),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.boundOnTablePlayerName?.let { boundName ->
                    Text(
                        text = stringResource(R.string.battle_horse_binding_template, boundName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = delta,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = deltaColor
            )
            if (isWinner) {
                Text(
                    text = stringResource(R.string.battle_settlement_victory),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SettlementFooter(
    onStartNextRound: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStartNextRound,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.battle_settlement_next_round),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
        TextButton(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.battle_settlement_view_history),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun settlementStatusRes(status: PlayerStatus): Int? {
    return when (status) {
        PlayerStatus.ACTIVE -> null
        PlayerStatus.HU -> R.string.player_status_hu
        PlayerStatus.ZIMO -> R.string.player_status_zimo
        PlayerStatus.LOCKED -> R.string.player_status_locked
    }
}

private data class SettlementEntryUiModel(
    val entryKey: String,
    val name: String,
    val avatarKey: String,
    val avatarEmoji: String,
    val roundDelta: String,
    val totalScore: String,
    val status: PlayerStatus?,
    val isHorse: Boolean,
    val boundOnTablePlayerName: String?
)

private fun buildSettlementEntries(
    players: List<PlayerCardUiModel>,
    horses: List<HorseUiModel>
): List<SettlementEntryUiModel> {
    val playerEntries = players.map { player ->
        SettlementEntryUiModel(
            entryKey = "player_${player.id}",
            name = player.name,
            avatarKey = player.avatarKey,
            avatarEmoji = player.avatarEmoji,
            roundDelta = player.roundDelta,
            totalScore = player.totalScore,
            status = player.status,
            isHorse = false,
            boundOnTablePlayerName = null
        )
    }
    val horseEntries = horses.map { horse ->
        SettlementEntryUiModel(
            entryKey = "horse_${horse.id}",
            name = horse.name,
            avatarKey = horse.avatarKey,
            avatarEmoji = horse.avatarEmoji,
            roundDelta = horse.roundDelta,
            totalScore = horse.totalScore,
            status = null,
            isHorse = true,
            boundOnTablePlayerName = horse.boundOnTablePlayerName
        )
    }
    return playerEntries + horseEntries
}

private fun String.toDeltaValue(): Int {
    val normalized = filter { it.isDigit() || it == '-' }
    return normalized.toIntOrNull() ?: 0
}
