package com.ash.mahjong.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ash.mahjong.MainActivity
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlobalBottomBarNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBar_staysVisible_whenSwitchingTopLevelTabs() {
        val context = composeRule.activity
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.battle_nav_rules)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.players_page_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.battle_nav_stats)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.placeholder_stats_description)).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.battle_nav_settings)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()
    }

    @Test
    fun playersPage_hidesBackArrow() {
        val context = composeRule.activity

        composeRule.onNodeWithText(context.getString(R.string.battle_nav_rules)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.players_page_title)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.players_icon_back)).assertCountEquals(0)
    }

    @Test
    fun bottomBar_showsAllTabIcons() {
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()
        (0..3).forEach { tabIndex ->
            composeRule.onNodeWithTag(
                BattleScoreTestTags.bottomTabIcon(tabIndex),
                useUnmergedTree = true
            ).assertIsDisplayed()
        }
    }
}
