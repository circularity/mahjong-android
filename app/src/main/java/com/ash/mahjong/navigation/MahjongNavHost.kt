package com.ash.mahjong.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group as FilledGroup
import androidx.compose.material.icons.filled.Leaderboard as FilledLeaderboard
import androidx.compose.material.icons.filled.Settings as FilledSettings
import androidx.compose.material.icons.filled.Style as FilledStyle
import androidx.compose.material.icons.outlined.Group as OutlinedGroup
import androidx.compose.material.icons.outlined.Leaderboard as OutlinedLeaderboard
import androidx.compose.material.icons.outlined.Settings as OutlinedSettings
import androidx.compose.material.icons.outlined.Style as OutlinedStyle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.ui.BattleScoreRoute
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.feature.battle_score.ui.components.BattleBottomBar
import com.ash.mahjong.feature.player_list.ui.PlayerListRoute
import com.ash.mahjong.feature.settings.ui.SettingsRoute
import com.ash.mahjong.feature.stats.ui.StatsRoute

@Composable
fun MahjongNavHost() {
    val backStack = rememberNavBackStack(AppRoute.BattleScore)
    val navigator = remember(backStack) { AppNavigator(backStack) }
    val currentRoute = backStack.currentAppRoute()

    Scaffold(
        bottomBar = {
            BattleBottomBar(
                items = buildTopLevelTabs(currentRoute),
                onTabClick = { tabIndex ->
                    navigator.switchTopLevel(tabRoute(tabIndex))
                },
                modifier = Modifier.testTag(BattleScoreTestTags.BOTTOM_BAR)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                backStack = backStack,
                onBack = { navigator.goBack() },
                entryProvider = { key ->
                    when (key) {
                        AppRoute.BattleScore -> NavEntry(key) {
                            BattleScoreRoute(
                                onGoToPlayers = { navigator.switchTopLevel(AppRoute.Players) }
                            )
                        }
                        AppRoute.Stats -> NavEntry(key) {
                            StatsRoute()
                        }
                        AppRoute.Players -> NavEntry(key) { PlayerListRoute() }
                        AppRoute.Settings -> NavEntry(key) {
                            SettingsRoute()
                        }
                        else -> NavEntry(AppRoute.BattleScore) {
                            BattleScoreRoute(
                                onGoToPlayers = { navigator.switchTopLevel(AppRoute.Players) }
                            )
                        }
                    }
                }
            )
        }
    }
}

private fun tabRoute(index: Int): AppRoute = when (index) {
    0 -> AppRoute.BattleScore
    1 -> AppRoute.Stats
    2 -> AppRoute.Players
    else -> AppRoute.Settings
}

private fun buildTopLevelTabs(currentRoute: AppRoute): List<TopLevelTabUiModel> {
    return listOf(
        TopLevelTabUiModel(
            labelRes = R.string.battle_nav_match,
            selectedIcon = Icons.Filled.FilledStyle,
            unselectedIcon = Icons.Outlined.OutlinedStyle,
            selected = currentRoute == AppRoute.BattleScore
        ),
        TopLevelTabUiModel(
            labelRes = R.string.battle_nav_stats,
            selectedIcon = Icons.Filled.FilledLeaderboard,
            unselectedIcon = Icons.Outlined.OutlinedLeaderboard,
            selected = currentRoute == AppRoute.Stats
        ),
        TopLevelTabUiModel(
            labelRes = R.string.battle_nav_rules,
            selectedIcon = Icons.Filled.FilledGroup,
            unselectedIcon = Icons.Outlined.OutlinedGroup,
            selected = currentRoute == AppRoute.Players
        ),
        TopLevelTabUiModel(
            labelRes = R.string.battle_nav_settings,
            selectedIcon = Icons.Filled.FilledSettings,
            unselectedIcon = Icons.Outlined.OutlinedSettings,
            selected = currentRoute == AppRoute.Settings
        )
    )
}

private fun NavBackStack<NavKey>.currentAppRoute(): AppRoute {
    if (isEmpty()) return AppRoute.BattleScore
    return this[lastIndex] as? AppRoute ?: AppRoute.BattleScore
}
