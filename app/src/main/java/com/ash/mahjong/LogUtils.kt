package com.ash.mahjong

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log

object LogUtils {

    private const val TAG = "MiraLog"

    // 应用实例，用于检测 debug 模式
    private var application: Application? = null

    // 缓存 debug 状态
    private var isDebugCached: Boolean? = null

    /**
     * 初始化 LogUtils，需要在 Application 中调用
     */
    fun init(app: Application) {
        application = app
        isDebugCached = null // 重置缓存
    }

    /**
     * 检测是否为 debug 模式
     */
    private val isDebug: Boolean
        get() {
            // 使用缓存避免重复计算
            isDebugCached?.let { return it }

            val result = application?.let { app ->
                (app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            } ?: false // 如果没有初始化，默认为 false

            isDebugCached = result
            return result
        }

    /**
     * 手动设置 debug 模式（可选，用于测试）
     */
    fun setDebugMode(debug: Boolean) {
        isDebugCached = debug
    }

    /**
     * Debug日志 - 延迟求值版本
     * 使用示例: LogUtils.d("MyTag") { "Loading image: $imageUrl" }
     * 使用示例: LogUtils.d { "Loading image: $imageUrl" }
     */
    fun d(tag: String = TAG, msgSupplier: () -> String) {
        if (isDebug) {
            Log.d(tag, msgSupplier())
        }
    }

    /**
     * Info日志 - 延迟求值版本
     */
    fun i(tag: String = TAG, msgSupplier: () -> String) {
        if (isDebug) {
            Log.i(tag, msgSupplier())
        }
    }

    /**
     * Warning日志 - 延迟求值版本
     */
    fun w(tag: String = TAG, msgSupplier: () -> String) {
        if (isDebug) {
            Log.w(tag, msgSupplier())
        }
    }

    /**
     * Warning日志（带异常）- 延迟求值版本
     */
    fun w(tag: String = TAG, throwable: Throwable, msgSupplier: () -> String) {
        if (isDebug) {
            Log.w(tag, msgSupplier(), throwable)
        }
    }

    /**
     * Error日志 - 延迟求值版本
     * 注意：错误日志在任何模式下都会显示
     */
    fun e(tag: String = TAG, throwable: Throwable? = null, msgSupplier: () -> String) {
        if (throwable != null) {
            Log.e(tag, msgSupplier(), throwable)
        } else {
            Log.e(tag, msgSupplier())
        }
    }

    /**
     * Verbose日志 - 延迟求值版本
     */
    fun v(tag: String = TAG, msgSupplier: () -> String) {
        if (isDebug) {
            Log.v(tag, msgSupplier())
        }
    }

    /**
     * WTF日志 - 延迟求值版本
     */
    fun wtf(tag: String = TAG, throwable: Throwable? = null, msgSupplier: () -> String) {
        if (isDebug) {
            if (throwable != null) {
                Log.wtf(tag, msgSupplier(), throwable)
            } else {
                Log.wtf(tag, msgSupplier())
            }
        }
    }
}
