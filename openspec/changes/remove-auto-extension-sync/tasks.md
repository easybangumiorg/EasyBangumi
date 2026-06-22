# Tasks

## 1. Remove Startup Auto Sync

- [x] 删除 `Scheduler.runOnMainActivityCreate()` 中对 `ExtensionRepoController.fireAutoSync()` 的调用。
- [x] 清理 `Scheduler.kt` 中不再需要的 `ExtensionRepoController` import。
- [x] 删除 `ExtensionSetting()` 中的“自动同步番源”设置项。
- [x] 清理 `ExtensionSetting.kt` 中不再使用的 `SyncAlt`、`SettingPreferences`、`settingPreferences` 等引用。
- [x] 评估 `SettingPreferences.sourceAutoSync` 是否保留为兼容旧数据；已保留字段但不再使用。

## 2. Simplify ExtensionRepoController

- [x] 移除 `ExtensionRepoController.State.autoSync`。
- [x] 删除 `fireAutoSync()` 方法以及只为它服务的 imports。
- [x] 保留 `refreshRemote()`、`appendOrUpdate()`、`delete()` 的手动操作能力。
- [x] 检查 `appendOrUpdate()` 中下载、删除旧缓存、重命名失败时的返回值处理。

## 3. Harden JS Plugin Delete

- [x] 调整 `JsExtensionProviderV2.delete(key)`，覆盖状态流找不到 key 但索引仍残留的情况。
- [x] 文件不存在但索引存在时，清理索引和 `temp` 后返回成功。
- [x] 文件删除失败时返回错误，并避免提前更新索引。
- [x] 索引更新后清理缓存，确保下一次状态流不会继续展示旧插件。
- [x] 防止同一 key 的删除、安装、更新并发互相覆盖；已增加 per-key 串行保护。

## 4. Harden Install And Update

- [x] 检查 `ExtensionRepoController.appendOrUpdate(remoteInfo)` 的临时文件命名，避免同 key 并发下载互相覆盖。
- [x] 检查并处理 `renameTo()` 失败。
- [x] 失败时删除临时文件，且不更新插件索引。
- [x] 更新成功后确认索引只指向最新文件。
- [x] 评估是否清理同 key 的旧版本未引用文件；已清理被旧索引引用的旧文件，并修复批量更新索引替换逻辑。

## 5. UI State And Feedback

- [x] 删除时避免重复提交同一个 key。
- [x] 删除成功提示应与真实结果一致；失败时展示错误。
- [x] 删除完成后确认 `ExtensionV2ViewModel` 的 `list` 和 `showList` 能随状态流刷新。
- [x] 搜索过滤状态下删除插件后，结果列表不保留旧安装态。

## 6. Verification

- [x] 使用 `rg` 确认没有 `fireAutoSync` 调用点。
- [x] 使用 `rg` 确认设置页不再展示自动同步入口。
- [x] 编译项目，已运行 `.\gradlew.bat compileKotlin` 和 `.\gradlew.bat :app:compileDebugKotlin`。
- [ ] 手动验证：启动不自动同步、手动刷新、手动安装/更新、删除 JS 插件。
- [x] 如测试框架允许，补充 `JsExtensionProviderV2.delete()` 的索引/文件一致性测试；当前未发现现有可直接复用的插件 provider 单元测试框架，本次以 app Kotlin 编译和静态检查验证。
