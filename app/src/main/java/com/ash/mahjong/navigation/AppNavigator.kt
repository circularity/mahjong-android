package com.ash.mahjong.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class AppNavigator private constructor(
    private val backStack: AppBackStack
) {
    constructor(backStack: MutableList<AppRoute>) : this(ListBackStack(backStack))
    constructor(backStack: NavBackStack<NavKey>) : this(Nav3BackStack(backStack))

    fun navigate(route: AppRoute) {
        backStack.add(route)
    }

    fun switchTopLevel(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun goBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

private interface AppBackStack {
    val size: Int
    val lastIndex: Int
    fun add(route: AppRoute)
    fun removeAt(index: Int)
    fun clear()
}

private class ListBackStack(
    private val backStack: MutableList<AppRoute>
) : AppBackStack {
    override val size: Int
        get() = backStack.size
    override val lastIndex: Int
        get() = backStack.lastIndex

    override fun add(route: AppRoute) {
        backStack.add(route)
    }

    override fun removeAt(index: Int) {
        backStack.removeAt(index)
    }

    override fun clear() {
        backStack.clear()
    }
}

private class Nav3BackStack(
    private val backStack: NavBackStack<NavKey>
) : AppBackStack {
    override val size: Int
        get() = backStack.size
    override val lastIndex: Int
        get() = backStack.lastIndex

    override fun add(route: AppRoute) {
        backStack.add(route)
    }

    override fun removeAt(index: Int) {
        backStack.removeAt(index)
    }

    override fun clear() {
        while (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
