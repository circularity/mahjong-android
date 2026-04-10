package com.ash.mahjong.feature.battle_score.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ash.mahjong.MainActivity
import com.ash.mahjong.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BattleScoreScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun screen_showsKeySections() {
        composeRule.onNodeWithTag(BattleScoreTestTags.TOP_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.LIVE_LOG_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.BOTTOM_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.FLOATING_ACTION).assertIsDisplayed()
    }

    @Test
    fun emptyPlayers_doNotShowActionButtons() {
        composeRule.onAllNodesWithTag("player_2_hu_button").assertCountEquals(0)
        composeRule.onAllNodesWithTag("player_3_hu_button").assertCountEquals(0)
    }

    @Test
    fun strings_comeFromResources() {
        val context = composeRule.activity
        composeRule.onNodeWithText(context.getString(R.string.battle_live_log_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.battle_nav_match)).assertIsDisplayed()
    }
}
