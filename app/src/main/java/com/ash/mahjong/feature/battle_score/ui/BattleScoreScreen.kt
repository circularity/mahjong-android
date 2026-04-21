package com.ash.mahjong.feature.battle_score.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.state.BattleScoreUiState
import com.ash.mahjong.feature.battle_score.ui.components.BattleEventDraftDialogHost
import com.ash.mahjong.feature.battle_score.ui.components.BattleTopBar
import com.ash.mahjong.feature.battle_score.ui.components.DrawSettlementDialogHost
import com.ash.mahjong.feature.battle_score.ui.components.HorseBindingDialog
import com.ash.mahjong.feature.battle_score.ui.components.HorseSection
import com.ash.mahjong.feature.battle_score.ui.components.LiveLogSection
import com.ash.mahjong.feature.battle_score.ui.components.PlayerSwapDialog
import com.ash.mahjong.feature.battle_score.ui.components.PlayersGrid
import com.ash.mahjong.feature.battle_score.ui.components.ResetAllConfirmDialog
import com.ash.mahjong.feature.battle_score.ui.components.SettlementPromptDialog
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun BattleScoreScreen(
    uiState: BattleScoreUiState,
    onIntent: (BattleScoreIntent) -> Unit,
    onGoToPlayers: () -> Unit
) {
    val spacing = MahjongDesign.spacing
    val haptic = LocalHapticFeedback.current

    fun dispatchIntent(intent: BattleScoreIntent) {
        onIntent(intent)
        if (uiState.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                BattleTopBar(
                    currentRound = uiState.currentRound,
                    canSettle = uiState.canSettle,
                    canSwapPlayers = !uiState.requiresPlayerSetup && uiState.horses.isNotEmpty(),
                    swapAttentionActive = !uiState.requiresPlayerSetup && uiState.horses.isNotEmpty(),
                    canReset = !uiState.requiresPlayerSetup,
                    onSettleClick = { dispatchIntent(BattleScoreIntent.OnFabClick) },
                    onSwapPlayersClick = { dispatchIntent(BattleScoreIntent.OpenPlayerSwapDialog) },
                    onResetClick = { dispatchIntent(BattleScoreIntent.OpenResetAllConfirmDialog) },
                    onQuickHistoryClick = {},
                    modifier = Modifier
                        .statusBarsPadding()
                        .testTag(BattleScoreTestTags.TOP_BAR)
                )
                if (uiState.horses.isNotEmpty()) {
                    HorseSection(
                        horses = uiState.horses,
                        onBindClick = { horseId ->
                            dispatchIntent(BattleScoreIntent.StartHorseBinding(horseId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md, vertical = spacing.sm)
                            .testTag(BattleScoreTestTags.HORSE_SECTION)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = spacing.md),
            contentPadding = PaddingValues(vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            if (!uiState.isPlayersLoaded) {
                item {
                    BattleLoadingSkeleton(
                        modifier = Modifier.testTag(BattleScoreTestTags.LOADING_SKELETON)
                    )
                }
            } else {
                if (uiState.requiresPlayerSetup) {
                    item {
                        SetupGuideCard(onGoToPlayers = onGoToPlayers)
                    }
                }

                item {
                    PlayersGrid(
                        players = uiState.players,
                        onHuClick = {
                            dispatchIntent(
                                BattleScoreIntent.SelectAction(
                                    actorId = it,
                                    action = BattleAction.HU
                                )
                            )
                        },
                        onGangClick = {
                            dispatchIntent(
                                BattleScoreIntent.SelectAction(
                                    actorId = it,
                                    action = BattleAction.GANG
                                )
                            )
                        },
                        onZimoClick = {
                            dispatchIntent(
                                BattleScoreIntent.SelectAction(
                                    actorId = it,
                                    action = BattleAction.ZIMO
                                )
                            )
                        },
                        actionsEnabled = !uiState.requiresPlayerSetup,
                        modifier = Modifier.testTag(BattleScoreTestTags.PLAYERS_GRID)
                    )
                }
            }

            item {
                LiveLogSection(
                    items = uiState.liveLogs,
                    canUndo = uiState.canUndo,
                    onUndoClick = { dispatchIntent(BattleScoreIntent.UndoLastEvent) },
                    modifier = Modifier.testTag(BattleScoreTestTags.LIVE_LOG_SECTION)
                )
            }
        }
    }

    uiState.eventDraft?.let { draft ->
        BattleEventDraftDialogHost(
            draft = draft,
            players = uiState.players,
            multiplierRange = uiState.multiplierRange,
            onIntent = ::dispatchIntent
        )
    }

    uiState.horseBindingDraft?.let { draft ->
        HorseBindingDialog(
            draft = draft,
            players = uiState.players,
            onSelectTarget = { targetId ->
                dispatchIntent(BattleScoreIntent.SelectHorseBindingTarget(targetId))
            },
            onDismiss = { dispatchIntent(BattleScoreIntent.CancelHorseBinding) }
        )
    }

    if (uiState.playerSwapDialogVisible) {
        PlayerSwapDialog(
            onTablePlayers = uiState.players,
            horses = uiState.horses,
            onSwap = { onTablePlayerId, horseId ->
                dispatchIntent(
                    BattleScoreIntent.SwapOnTableWithHorse(
                        onTablePlayerId = onTablePlayerId,
                        horsePlayerId = horseId
                    )
                )
            },
            onDismiss = { dispatchIntent(BattleScoreIntent.DismissPlayerSwapDialog) }
        )
    }

    uiState.drawSettlementDraft?.let { draft ->
        DrawSettlementDialogHost(
            draft = draft,
            players = uiState.players,
            multiplierRange = uiState.multiplierRange,
            onIntent = ::dispatchIntent
        )
    }

    uiState.settlementPrompt?.let { prompt ->
        SettlementPromptDialog(
            prompt = prompt,
            players = uiState.players,
            horses = uiState.horses,
            onIntent = ::dispatchIntent
        )
    }

    uiState.resetAllConfirmStep?.let { step ->
        ResetAllConfirmDialog(
            step = step,
            onDismiss = { dispatchIntent(BattleScoreIntent.DismissResetAllConfirmDialog) },
            onConfirm = { dispatchIntent(BattleScoreIntent.ConfirmResetAllConfirmDialog) }
        )
    }
}

@Composable
private fun BattleLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MahjongDesign.shapes.logCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            SkeletonBar(
                widthFraction = 0.42f,
                heightDp = 20,
                color = placeholderColor
            )
            SkeletonBar(
                widthFraction = 0.86f,
                heightDp = 14,
                color = placeholderColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) {
                    SkeletonBar(
                        widthFraction = 0.32f,
                        heightDp = 36,
                        color = placeholderColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        repeat(4) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MahjongDesign.shapes.logCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = placeholderColor,
                                shape = MahjongDesign.shapes.logCard
                            )
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = spacing.xs)
                    ) {
                        SkeletonBar(
                            widthFraction = 0.5f,
                            heightDp = 14,
                            color = placeholderColor
                        )
                        SkeletonBar(
                            widthFraction = 0.78f,
                            heightDp = 12,
                            color = placeholderColor
                        )
                        SkeletonBar(
                            widthFraction = 1f,
                            heightDp = 32,
                            color = placeholderColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonBar(
    widthFraction: Float,
    heightDp: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(heightDp.dp)
            .background(
                color = color,
                shape = MahjongDesign.shapes.logCard
            )
    )
}

@Composable
private fun SetupGuideCard(
    onGoToPlayers: () -> Unit
) {
    val spacing = MahjongDesign.spacing
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MahjongDesign.shapes.logCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = stringResource(R.string.battle_setup_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.battle_setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onGoToPlayers) {
                Text(text = stringResource(R.string.battle_setup_action))
            }
        }
    }
}
