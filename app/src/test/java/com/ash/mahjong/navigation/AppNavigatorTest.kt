package com.ash.mahjong.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun navigate_appendsRouteToBackStack() {
        val backStack = mutableListOf<AppRoute>(AppRoute.BattleScore)
        val navigator = AppNavigator(backStack)

        navigator.navigate(AppRoute.Settings)

        assertEquals(
            listOf(AppRoute.BattleScore, AppRoute.Settings),
            backStack
        )
    }

    @Test
    fun goBack_removesTopRouteWhenBackStackHasMultipleEntries() {
        val backStack = mutableListOf<AppRoute>(AppRoute.BattleScore, AppRoute.Settings)
        val navigator = AppNavigator(backStack)

        val consumed = navigator.goBack()

        assertTrue(consumed)
        assertEquals(listOf(AppRoute.BattleScore), backStack)
    }

    @Test
    fun goBack_doesNothingWhenBackStackHasSingleEntry() {
        val backStack = mutableListOf<AppRoute>(AppRoute.BattleScore)
        val navigator = AppNavigator(backStack)

        val consumed = navigator.goBack()

        assertFalse(consumed)
        assertEquals(listOf(AppRoute.BattleScore), backStack)
    }

    @Test
    fun switchTopLevel_replacesBackStackWithSingleRoute() {
        val backStack = mutableListOf<AppRoute>(
            AppRoute.BattleScore,
            AppRoute.Players,
            AppRoute.Settings
        )
        val navigator = AppNavigator(backStack)

        navigator.switchTopLevel(AppRoute.BattleScore)

        assertEquals(listOf(AppRoute.BattleScore), backStack)
    }

    @Test
    fun switchTopLevel_sameRoute_keepsSingleEntry() {
        val backStack = mutableListOf<AppRoute>(
            AppRoute.BattleScore,
            AppRoute.Players
        )
        val navigator = AppNavigator(backStack)

        navigator.switchTopLevel(AppRoute.Players)

        assertEquals(listOf(AppRoute.Players), backStack)
    }
}
