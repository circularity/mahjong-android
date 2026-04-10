package com.ash.mahjong

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MahjongApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtils.init(this)
    }
}
