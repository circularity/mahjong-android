# Mahjong Android

一款基于 Kotlin 与 Jetpack Compose 开发的现代化 Android 麻将记分应用。

## 项目简介

该项目提供面向麻将对局的核心能力：
- 对局记分与流程管理
- 玩家管理
- 设置管理
- 底部导航与多页面切换

## 技术栈

- Kotlin (JDK 17)
- Jetpack Compose
- AndroidX Navigation
- Room
- DataStore
- Hilt
- JUnit4 / AndroidX Test

## 环境要求

- Android Studio（建议最新稳定版）
- JDK 17
- Android SDK（以项目 Gradle 配置为准）

## 快速开始

```bash
./gradlew assembleDebug
```

构建完成后可在 Android Studio 或设备上安装运行 Debug 包。

## 常用命令

在仓库根目录执行：

```bash
# Debug 构建
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Release AAB（Google Play）
./gradlew bundleRelease

# JVM 单元测试
./gradlew test

# 仪器测试（需连接设备/模拟器）
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint

# 本地快速构建（仅用于迭代）
./gradlew --build-cache assembleDebug -x lint -x test --configure-on-demand
```

## 多语言与文案规范

- 所有用户可见文案通过资源文件管理，禁止在 UI 层硬编码。
- 当前至少维护以下资源目录：
  - `app/src/main/res/values`
  - `app/src/main/res/values-zh-rCN`

## 日志规范

统一使用 `LogUtils` 输出日志，推荐写法：

```kotlin
LogUtils.d { "message" }
LogUtils.i { "message" }
LogUtils.w { "message" }
LogUtils.e(throwable = e) { "message" }
```

## 许可证

本项目使用 MIT License，详见 [LICENSE](./LICENSE)。
