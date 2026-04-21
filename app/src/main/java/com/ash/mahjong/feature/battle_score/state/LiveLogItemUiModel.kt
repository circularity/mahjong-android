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
    val actorIsHorse: Boolean = false,
    val becausePlayerName: String? = null,
    val actionType: LiveLogActionType,
    val relatedPlayerNames: List<String>,
    val relatedPlayerDetails: List<LiveLogRelatedPlayerUiModel> = emptyList(),
    val amount: String,
    val highlight: LiveLogHighlight
)

data class LiveLogRelatedPlayerUiModel(
    val name: String,
    val delta: String
)
