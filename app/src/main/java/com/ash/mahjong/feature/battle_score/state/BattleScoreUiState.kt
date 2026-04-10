package com.ash.mahjong.feature.battle_score.state

import androidx.annotation.StringRes
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.GangType

data class BattleScoreUiState(
    val currentRound: Int,
    @param:StringRes val windLabelRes: Int,
    val players: List<PlayerCardUiModel>,
    val horses: List<HorseUiModel> = emptyList(),
    val horseBindingDraft: HorseBindingDraftUiState? = null,
    val liveLogs: List<LiveLogItemUiModel>,
    val hapticsEnabled: Boolean,
    val lastAction: String?,
    val requiresPlayerSetup: Boolean,
    val canUndo: Boolean,
    val canSettle: Boolean,
    val playerSwapDialogVisible: Boolean,
    val multiplierRange: IntRange,
    val eventDraft: EventDraftUiState?,
    val drawSettlementDraft: DrawSettlementDraftUiState?,
    val settlementPrompt: SettlementPromptUiState?,
    val resetAllConfirmStep: ResetAllConfirmStep?
)

data class EventDraftUiState(
    val step: EventDraftStep,
    val action: BattleAction,
    val actorId: Int,
    val gangType: GangType?,
    val targetId: Int?,
    val multiplier: Int?
)

enum class EventDraftStep {
    GANG_TYPE,
    TARGET,
    MULTIPLIER
}

enum class SettlementPromptType {
    AUTO_THREE_HU,
    DRAW_RESULT_CONFIRM,
    MANUAL_DRAW_PLACEHOLDER
}

data class SettlementPromptUiState(
    val type: SettlementPromptType
)

enum class DrawSettlementStep {
    CHOOSE_TING,
    CHOOSE_MULTIPLIER
}

enum class ResetAllConfirmStep {
    FIRST,
    SECOND
}

data class DrawSettlementChoiceUiState(
    val isTing: Boolean,
    val multiplier: Int?
)

data class DrawSettlementDraftUiState(
    val orderedPendingPlayerIds: List<Int>,
    val currentIndex: Int,
    val choicesByPlayerId: Map<Int, DrawSettlementChoiceUiState>,
    val step: DrawSettlementStep,
    val currentTingChoice: Boolean?,
    val currentMultiplier: Int?
)
