package com.ash.mahjong.feature.battle_score.domain

import com.ash.mahjong.data.settings.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleScoreCalculatorTest {

    private val calculator = BattleScoreCalculator()

    @Test
    fun hu_scalesByBasePointAndMultiplier() {
        val settings = GameSettings(basePoint = 2, cappingMultiplier = 8, hapticsEnabled = false)
        val deltaByPlayer = calculator.calculateDelta(
            actionType = BattleScoreActionType.HU,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2), multiplier = 3),
            settings = settings
        )

        assertEquals(6, deltaByPlayer[1])
        assertEquals(-6, deltaByPlayer[2])
    }

    @Test
    fun zimo_chargesEveryTargetAndSumsToActor() {
        val settings = GameSettings(basePoint = 1, cappingMultiplier = 8, hapticsEnabled = false)
        val deltaByPlayer = calculator.calculateDelta(
            actionType = BattleScoreActionType.ZIMO,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2, 3), multiplier = 4),
            settings = settings
        )

        assertEquals(8, deltaByPlayer[1])
        assertEquals(-4, deltaByPlayer[2])
        assertEquals(-4, deltaByPlayer[3])
    }

    @Test
    fun gangTypes_applyDifferentFactors() {
        val settings = GameSettings(basePoint = 1, cappingMultiplier = 8, hapticsEnabled = false)

        val dian = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_DIAN,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2), multiplier = 2),
            settings = settings
        )
        assertEquals(4, dian[1])
        assertEquals(-4, dian[2])

        val ba = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_BA,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2, 3), multiplier = 2),
            settings = settings
        )
        assertEquals(4, ba[1])
        assertEquals(-2, ba[2])
        assertEquals(-2, ba[3])

        val an = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_AN,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2, 3), multiplier = 2),
            settings = settings
        )
        assertEquals(8, an[1])
        assertEquals(-4, an[2])
        assertEquals(-4, an[3])
    }

    @Test
    fun multiplier_isClampedBySettingsCap() {
        val settings = GameSettings(basePoint = 2, cappingMultiplier = 3, hapticsEnabled = false)
        val deltaByPlayer = calculator.calculateDelta(
            actionType = BattleScoreActionType.HU,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2), multiplier = 99),
            settings = settings
        )

        assertEquals(6, deltaByPlayer[1])
        assertEquals(-6, deltaByPlayer[2])
    }

    @Test
    fun gang_fixedRuleCoefficients_matchDescriptionAtMultiplierOne() {
        val settings = GameSettings(basePoint = 3, cappingMultiplier = 8, hapticsEnabled = false)

        val dian = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_DIAN,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2), multiplier = 1),
            settings = settings
        )
        assertEquals(6, dian[1])
        assertEquals(-6, dian[2])

        val ba = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_BA,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2, 3), multiplier = 1),
            settings = settings
        )
        assertEquals(6, ba[1])
        assertEquals(-3, ba[2])
        assertEquals(-3, ba[3])

        val an = calculator.calculateDelta(
            actionType = BattleScoreActionType.GANG_AN,
            context = BattleScoreContext(actorId = 1, targetIds = listOf(2, 3), multiplier = 1),
            settings = settings
        )
        assertEquals(12, an[1])
        assertEquals(-6, an[2])
        assertEquals(-6, an[3])
    }
}
