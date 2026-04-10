package com.ash.mahjong.feature.battle_score.domain

import com.ash.mahjong.data.settings.GameSettings

class BattleScoreCalculator {

    fun calculateDelta(
        actionType: BattleScoreActionType,
        context: BattleScoreContext,
        settings: GameSettings
    ): Map<Int, Int> {
        val multiplier = context.multiplier.coerceIn(
            minimumValue = 1,
            maximumValue = settings.cappingMultiplier
        )
        val unit = settings.basePoint * multiplier
        val targetIds = context.targetIds.distinct().filter { it != context.actorId }

        return when (actionType) {
            BattleScoreActionType.HU -> {
                val targetId = targetIds.firstOrNull() ?: return emptyMap()
                mapOf(
                    context.actorId to unit,
                    targetId to -unit
                )
            }

            BattleScoreActionType.ZIMO -> {
                if (targetIds.isEmpty()) {
                    return emptyMap()
                }
                buildMap {
                    put(context.actorId, unit * targetIds.size)
                    targetIds.forEach { targetId ->
                        put(targetId, -unit)
                    }
                }
            }

            BattleScoreActionType.GANG_DIAN -> {
                val targetId = targetIds.firstOrNull() ?: return emptyMap()
                val delta = unit * 2
                mapOf(
                    context.actorId to delta,
                    targetId to -delta
                )
            }

            BattleScoreActionType.GANG_BA -> {
                if (targetIds.isEmpty()) {
                    return emptyMap()
                }
                buildMap {
                    put(context.actorId, unit * targetIds.size)
                    targetIds.forEach { targetId ->
                        put(targetId, -unit)
                    }
                }
            }

            BattleScoreActionType.GANG_AN -> {
                if (targetIds.isEmpty()) {
                    return emptyMap()
                }
                val delta = unit * 2
                buildMap {
                    put(context.actorId, delta * targetIds.size)
                    targetIds.forEach { targetId ->
                        put(targetId, -delta)
                    }
                }
            }
        }
    }
}

enum class BattleScoreActionType {
    HU,
    ZIMO,
    GANG_DIAN,
    GANG_BA,
    GANG_AN
}

data class BattleScoreContext(
    val actorId: Int,
    val targetIds: List<Int>,
    val multiplier: Int
)
