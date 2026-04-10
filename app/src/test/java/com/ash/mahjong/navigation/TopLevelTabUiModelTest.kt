package com.ash.mahjong.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelTabUiModelTest {

    @Test
    fun model_containsIconResource() {
        val model = TopLevelTabUiModel(
            labelRes = 1,
            iconRes = 2,
            selected = true
        )

        assertEquals(1, model.labelRes)
        assertEquals(2, model.iconRes)
        assertTrue(model.selected)
    }
}
