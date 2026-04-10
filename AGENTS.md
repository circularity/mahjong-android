# 仓库规范

## 沟通与文案规范
- 默认协作语言使用简体中文（需求沟通、评审反馈、提交说明等）。
- UI 文案必须通过资源文件管理并支持本地化，不设置“必须英文”的硬限制。
- 禁止在 UI 层硬编码用户可见文案；文案变更需同步资源文件。

## 构建、测试与开发命令
请在仓库根目录执行以下命令：

- `./gradlew assembleDebug`：构建 Debug APK。
- `./gradlew assembleRelease`：构建 Release APK。
- `./gradlew bundleRelease`：构建 Release AAB（Google Play 发布流程）。
- `./gradlew test`：运行各模块 JVM 单元测试。
- `./gradlew connectedDebugAndroidTest`：在已连接设备/模拟器上运行仪器测试。
- `./gradlew lint`：运行 Android Lint 检查。
- `./gradlew --build-cache assembleDebug -x lint -x test --configure-on-demand`：本地快速构建（可选）。该命令仅用于本地迭代，不替代 CI 或合并前质量检查。

## 代码风格与命名规范
- 语言：Kotlin（JDK 17，启用 Compose）。
- 遵循标准 Kotlin 格式：4 空格缩进、清晰的可空性处理、函数小而聚焦。
- 类 / Composable / ViewModel 命名：`PascalCase`（如 `TemplateViewModel`、`CoinPurchaseScreen`）。
- 函数 / 变量命名：`camelCase`。
- 资源 / 文件命名：`snake_case`（如 `ic_stat_notification.xml`）。
- 包结构保持以功能为导向，位于 `com.ash.mahjong...` 下。
- 依赖版本通过 `gradle/libs.versions.toml` 管理。
- 尽量使用 Jetpack Compose，并遵循单向数据流（UDF）设计。
- 导航优先使用 Navigation 组件，默认采用单 Activity 架构（第三方 SDK 强约束除外）。

## 多语言（i18n）硬性要求
- 所有 UI 文本必须使用 `stringResource(R.string.xxx)` 或等价资源引用方式，严禁硬编码。
- 修改或新增文案时，必须同步更新所有已支持语言资源，当前至少包含：`values`、`values-zh-rCN`。
- 资源命名采用小写加下划线，并按业务语义分组（如 `login_title`、`error_network`）。
- 例外范围仅限非用户可见文本（如日志、调试信息、API 字段名）。

## 日志规范（LogUtils）
- 统一使用 `LogUtils` 进行日志输出，优先使用 Lambda 延迟求值写法。
- 推荐写法：`LogUtils.d { "..." }`、`LogUtils.i { "..." }`、`LogUtils.w { "..." }`、`LogUtils.e(throwable = e) { "..." }`。
- `LogUtils.e()` 在 Release 模式也会输出，错误日志需包含足够上下文以支持排障。
- 严禁打印敏感信息（如密码、Token、密钥、用户隐私数据）。

## 架构与规模约束
- 默认目标：静态语言文件（Java/Go/Kotlin）尽量不超过 400 行；如超过，需评估并拆分。
- 默认目标：单层目录文件数尽量不超过 8 个；如超过，优先按功能拆分子目录。
- 上述为“默认上限”，允许基于业务复杂度例外，但必须在评审说明中给出理由与后续治理计划。
- 开发与评审时持续关注并主动治理坏味道：僵化、冗余、循环依赖、脆弱性、晦涩性、数据泥团、不必要的复杂性。

## 测试规范
- 单元测试：使用 `src/test/java` 下的 JUnit4。
- UI/仪器测试：使用 `src/androidTest/java` 下的 AndroidX JUnit/Espresso/Compose 测试库。
- 在合并前，针对变更的业务逻辑 关键 UI 流程新增或更新测试。

## Commit 与 Pull Request 规范
- 使用历史中已有的 Conventional Commit 风格：`feat:`、`fix:`、`refactor:`、`perf:`、`test:`、`debug:`。
- 标题保持简洁且面向动作（中文或英文均可）。
- PR 应包含清晰的变更范围与风险摘要。
- PR 应包含关联 issue/任务背景。
- PR 应包含 UI 变更截图或录屏。
- PR 应包含测试证据（Gradle命令及结果）。

## 安全与配置建议
- 严禁提交新的敏感信息（服务账号密钥、API Key、个人签名密钥库）。
- 环境相关配置值应保存在本地或 CI 密钥管理中。
