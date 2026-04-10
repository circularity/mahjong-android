package com.ash.mahjong.feature.battle_score.state

enum class LiveLogHighlight {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

enum class LiveLogActionType {
    HU,
    ZIMO,
    GANG_DIAN,
    GANG_BA,
    GANG_AN,
    GANG_REFUND,
    DRAW_SETTLEMENT
}

data class LiveLogItemUiModel(
    val id: Int,
    val actorName: String,
    val actionType: LiveLogActionType,
    val relatedPlayerNames: List<String>,
    val amount: String,
    val highlight: LiveLogHighlight
)
