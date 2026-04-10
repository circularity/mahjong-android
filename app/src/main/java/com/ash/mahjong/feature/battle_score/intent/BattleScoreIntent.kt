package com.ash.mahjong.feature.battle_score.intent

sealed interface BattleScoreIntent {
    data class SelectAction(
        val actorId: Int,
        val action: BattleAction
    ) : BattleScoreIntent

    data class SelectTarget(val targetId: Int) : BattleScoreIntent

    data class SelectGangType(val gangType: GangType) : BattleScoreIntent

    data class SelectMultiplier(val multiplier: Int) : BattleScoreIntent

    data object ConfirmDraftStep : BattleScoreIntent
    data object BackEventDraftStep : BattleScoreIntent

    data object ConfirmEvent : BattleScoreIntent
    data object CancelEventDraft : BattleScoreIntent
    data object UndoLastEvent : BattleScoreIntent

    data object OnFabClick : BattleScoreIntent

    data class SelectDrawTingChoice(val isTing: Boolean) : BattleScoreIntent
    data class SelectDrawTingMultiplier(val multiplier: Int) : BattleScoreIntent
    data object ConfirmDrawSettlementSelection : BattleScoreIntent
    data object BackDrawSettlementStep : BattleScoreIntent
    data object CancelDrawSettlementDraft : BattleScoreIntent

    data object DismissSettlementPrompt : BattleScoreIntent
    data object ConfirmSettleAndNextRound : BattleScoreIntent

    data class StartHorseBinding(val horseId: Int) : BattleScoreIntent
    data class SelectHorseBindingTarget(val targetPlayerId: Int) : BattleScoreIntent
    data object CancelHorseBinding : BattleScoreIntent
}

enum class BattleAction {
    HU,
    GANG,
    ZIMO
}

enum class GangType {
    DIAN,
    BA,
    AN
}
