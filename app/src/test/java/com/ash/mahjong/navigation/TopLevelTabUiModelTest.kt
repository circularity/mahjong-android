package com.ash.mahjong.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelTabUiModelTest {

    @Test
    fun model_containsIconResource() {
        val selectedIcon = ImageVector.Builder(
            name = "selected",
            defaultWidth = 1.dp,
            defaultHeight = 1.dp,
            viewportWidth = 1f,
            viewportHeight = 1f
        ).build()
        val unselectedIcon = ImageVector.Builder(
            name = "unselected",
            defaultWidth = 1.dp,
            defaultHeight = 1.dp,
            viewportWidth = 1f,
            viewportHeight = 1f
        ).build()
        val model = TopLevelTabUiModel(
            labelRes = 1,
            selectedIcon = selectedIcon,
            unselectedIcon = unselectedIcon,
            selected = true
        )

        assertEquals(1, model.labelRes)
        assertEquals(selectedIcon, model.selectedIcon)
        assertEquals(unselectedIcon, model.unselectedIcon)
        assertTrue(model.selected)
    }
}
