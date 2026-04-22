package com.ash.mahjong.feature.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ash.mahjong.MainActivity
import com.ash.mahjong.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsPage_showsKeySections() {
        val context = composeRule.activity

        openSettings()

        composeRule.onNodeWithTag(SettingsTestTags.SCREEN_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_section_game_rules)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_section_feedback)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_section_about)).assertIsDisplayed()
    }

    @Test
    fun stepperAndClearCache_updateAndResetValues() {
        openSettings()

        composeRule.onNodeWithTag(SettingsTestTags.BASE_POINT_VALUE).assertTextEquals("1")
        composeRule.onNodeWithTag(SettingsTestTags.CAPPING_VALUE).assertTextEquals("3")

        composeRule.onNodeWithTag(SettingsTestTags.BASE_POINT_INCREASE).performClick()
        composeRule.onNodeWithTag(SettingsTestTags.CAPPING_INCREASE).performClick()

        composeRule.onNodeWithTag(SettingsTestTags.BASE_POINT_VALUE).assertTextEquals("2")
        composeRule.onNodeWithTag(SettingsTestTags.CAPPING_VALUE).assertTextEquals("4")

        composeRule.onNodeWithTag(SettingsTestTags.CLEAR_CACHE).performClick()

        composeRule.onNodeWithTag(SettingsTestTags.BASE_POINT_VALUE).assertTextEquals("1")
        composeRule.onNodeWithTag(SettingsTestTags.CAPPING_VALUE).assertTextEquals("3")
    }

    @Test
    fun hapticsSwitch_togglesState() {
        openSettings()

        composeRule.onNodeWithTag(SettingsTestTags.HAPTICS_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(SettingsTestTags.HAPTICS_SWITCH).performClick()
        composeRule.onNodeWithTag(SettingsTestTags.HAPTICS_SWITCH).assertIsOn()
    }

    private fun openSettings() {
        val context = composeRule.activity
        composeRule.onNodeWithText(context.getString(R.string.battle_nav_settings)).performClick()
    }
}
