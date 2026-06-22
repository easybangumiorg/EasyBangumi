# Design

## Current Behavior

V2 番源仓库同步由 `ExtensionRepoController` 聚合本地插件状态和远端仓库状态。它会把 `ExtensionCase.flowExtensionState()` 与 `ExtensionRemoteController.remote` 合并为 `ExtensionRemoteLocalInfo` 列表，供 `ExtensionV2ViewModel` 展示。

启动自动同步路径如下：

- `Scheduler.runOnMainActivityCreate()` 初始化 `IExtensionController` 后调用 `Inject.get<ExtensionRepoController>().fireAutoSync()`。
- `ExtensionRepoController.fireAutoSync()` 读取 `SettingPreferences.sourceAutoSync`。
- 当偏好为 true 时，等待本地和远端都加载完成。
- 对 `onlyRemote` 或 `hasUpdate` 的远端项并发调用 `appendOrUpdate()`。
- 同步期间更新 `State.autoSync`，并弹出开始/完成提示。

偏好入口位于 `ExtensionSetting()`，文案为“自动同步番源”。默认值在 `SettingPreferences.sourceAutoSync` 中为 true。

V2 JS 插件删除路径如下：

- `ExtensionV2ViewModel.onDelete()` 调用 `ExtensionRepoController.delete(key)`。
- `ExtensionRepoController.delete()` 透传到 `ExtensionControllerV2.deleteExtension(key)`。
- `ExtensionControllerV2.deleteExtension()` 透传到 `JsExtensionProviderV2.delete(key)`。
- `JsExtensionProviderV2.delete()` 从当前 `_flow.value.extensionMap` 找到插件，删除 `extensionFolder` 中的文件，更新 `extensionIndex`，并移除 `temp` 缓存。
- `indexHelper.flow` 再触发重新 load，最终刷新 UI。

## Proposed Behavior

### Remove Automatic Sync

移除主界面启动后的自动同步调用。`Scheduler.runOnMainActivityCreate()` 不再获取 `ExtensionRepoController` 来执行 `fireAutoSync()`。

移除设置页中的自动同步开关。`sourceAutoSync` 可以先保留在偏好定义中以兼容旧数据，但不再由 UI 暴露，也不再参与运行时流程。若代码中没有其他引用，可在实现阶段评估是否直接删除该偏好字段；为了降低存量数据迁移风险，建议本次只删除使用点。

`ExtensionRepoController.State.autoSync` 与 `fireAutoSync()` 已无业务用途，应删除或私有废弃。若删除会引发较大连锁改动，则至少保证没有调用路径，并移除自动同步提示。

### Keep Manual Repository Operations

手动刷新、手动下载、手动更新继续走现有接口：

- `ExtensionV2ViewModel.refreshRemote()` -> `ExtensionRepoController.refreshRemote()`
- `ExtensionV2ViewModel.onDownload()` -> `ExtensionRepoController.appendOrUpdate(remoteInfo)`

这些手动入口不应依赖 `sourceAutoSync` 或 `autoSync` 状态。

### Harden Delete Consistency

删除操作应以“索引与文件最终一致”为目标，并避免成功提示早于状态可观测更新。

建议实现策略：

- 删除时不要只依赖 `_flow.value.extensionMap[key]`。当状态流中找不到 key 时，应检查 `extensionIndex` 是否仍有该 key；如果索引有残留但文件不存在，应清理索引并返回成功或明确的 no-op 成功。
- 文件删除失败时，不更新索引，不返回成功。
- 文件已不存在但索引存在时，更新索引移除该 key，清理 `temp`，返回成功。
- 索引更新后立即清理 `temp[key]`，并确保下一次 `indexHelper.flow` 重新加载不会复用旧缓存。
- `ExtensionV2ViewModel.onDelete()` 在删除期间可显示进行中状态或至少避免重复触发；删除成功后应等待状态流自然刷新，或主动触发一次本地状态刷新能力。如果没有显式刷新 API，则通过索引更新驱动刷新。

### Harden Install And Update Consistency

`ExtensionRepoController.appendOrUpdate(remoteInfo)` 当前先下载到 `cache/key`，再根据加密标记重命名到 `cache/key.ebg.js` 或 `cache/key.ebg.jsc`，最后交给 `ExtensionControllerV2.appendOrUpdateExtension(file)`。

建议实现策略：

- 检查 `delete()`、`renameTo()`、`copyTo()` 的返回结果或异常，失败时返回 `DataResult.Error`。
- 临时下载文件与目标缓存文件命名应避免并发同 key 操作互相覆盖。
- 更新成功后，索引应只指向新文件。对于同 key 旧版本文件，若不再被索引引用，应考虑清理，避免磁盘残留。
- 更新失败时不修改索引，已有插件继续可用。
- 同 key 下载/删除应串行化，避免删除与安装并发导致索引指向不存在文件或旧文件重新出现。

## Edge Cases To Cover

- 自动同步偏好旧值为 true，但启动后不发生下载/更新。
- 远端仓库加载慢或失败，不触发任何自动安装。
- 已安装 JS 插件删除成功后，列表变为 onlyRemote 或从搜索结果中消失。
- 删除时文件已经不存在，但索引仍存在。
- 删除时索引不存在，但状态流仍有旧缓存。
- 文件删除失败。
- 索引更新失败或抛异常。
- 用户连续点击删除确认，或删除过程中再次长按同一项。
- 更新同 key 插件时下载失败、重命名失败、loader 校验失败。
- 更新成功后旧版本文件不再被索引引用。
- 远端仓库存在同 key 多来源覆盖时，手动更新仍按当前仓库排序规则选择最终 remoteInfo。

## Verification

- 静态检查：确认 `fireAutoSync` 没有调用点，设置页没有自动同步入口。
- 单元或集成测试：覆盖 `JsExtensionProviderV2.delete()` 的索引/文件组合场景。
- 手动验证：启动应用、进入插件管理页、刷新仓库、安装、更新、删除 JS 插件。
- 回归验证：APK 安装插件仍打开系统应用详情，不受 JS 文件删除逻辑影响。

