# 当前上下文总结

项目路径：`C:\project\android\EasyBangumi\easy_bangumi_android`

当前分支：`main`

## 最近提交

- `2d0d1529 feat 1: 合并 jsengine 到 js 包 2: 抽象验证重载流程`
- `252c056c feat 1: 完善 JSON 验证码重试 2: 补充播放缓存测试`
- `11baebfd feat 1: 支持 JSON 验证码输入 2: 添加播放信息缓存`

## 已完成内容

### JS 包整理

- 已将 `plugin.source.jsengine` 全部迁入 `plugin.source.js`。
- 源码内已无 `jsengine` 引用。
- 相关组件、runtime、source、utils、entity 均迁移到 `plugin/source/js/` 下。

### 验证流程抽象

新增统一验证模型：

- `VerificationParam.WebView`
- `VerificationParam.ImageCaptcha`
- `VerificationResult.WebView`
- `VerificationResult.ImageCaptcha`
- `VerificationHelper`

整体流程：

1. 业务正常调用普通 search/play。
2. 源遇到需要验证时抛业务异常。
3. UI 进入可点击错误态。
4. 用户点击后调用 `VerificationHelper`。
5. `VerificationHelper` 返回 `VerificationResult`。
6. 业务再调用带 `VerificationResult` 的 search/play 重载继续执行。

WebView 验证结果会包装验证后的 `IWebProxy`，保证原有 JS WebView 验证链路不受影响。

### JSON 源验证码

- JSON search/play 都已支持验证码验证流程。
- 普通调用只负责抛异常，不立即弹窗。
- 图片验证码通过 `VerificationResult.ImageCaptcha(input)` 继续请求。
- search/play 都已有相关单元测试覆盖。

### PlayComponent 缓存

- 新增 `PlayComponentCacheWrapper`。
- `SourceResult.Complete` 新增 `isCache: Boolean = false`。
- `PlayComponent.getPlayInfo` 新增 `canCache` 参数。
- 播放页在播放错误时，如果当前 `PlayerInfo` 来自 cache，会禁用 cache 重试一次。
- 已补充 `PlayComponentCacheWrapperTest`。

## 已验证命令

通过：

```powershell
$env:JAVA_HOME='C:\Users\eke_l\.jdks\jbr-17.0.9'
.\gradlew.bat :app:compileDebugKotlin
```

通过：

```powershell
$env:JAVA_HOME='C:\Users\eke_l\.jdks\jbr-17.0.9'
.\gradlew.bat :app:testDebugUnitTest --tests "com.heyanle.easybangumi4.plugin.source.json.JsonSourceRuleTest"
```

此前也验证过：

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.heyanle.easybangumi4.plugin.source.bundle.PlayComponentCacheWrapperTest"
```

## 当前工作区状态

源码已提交。

剩余未跟踪生成目录：

- `inject/bin/`
- `lib_upnp/bin/`

## 注意事项

- 如果编译时 `JAVA_HOME` 指向失效路径，可临时使用：

```powershell
$env:JAVA_HOME='C:\Users\eke_l\.jdks\jbr-17.0.9'
```

- 之前批量写文件时出现过 UTF-8 BOM 导致 Java 编译失败的问题，后续修改 `.java` 文件时注意不要写入 BOM。

