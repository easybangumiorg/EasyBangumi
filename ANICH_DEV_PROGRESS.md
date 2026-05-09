# AniCh 番剧源开发进度

## 状态：进行中（已基本完成，需要测试验证）

## 已完成的工作

### 1. 创建的文件

| 文件 | 说明 |
|------|------|
| `shared/source/inner/.../anich/AniChInnerSource.kt` | 源定义、组件注册 |
| `shared/source/inner/.../anich/AniChPrefComponent.kt` | 域名配置偏好 |
| `shared/source/inner/.../anich/AniChSearchComponent.kt` | 搜索组件（分页、验证码处理） |
| `shared/source/inner/.../anich/AniChPlayComponent.kt` | 播放组件（剧集优先模式） |

### 2. 修改的文件

| 文件 | 变更 |
|------|------|
| `shared/source/.../InnerSourceProvider.kt` | 注册 AniCh 源 |
| `lib/webview/api/.../IWebView.kt` | 新增 `executeJavaScriptWithCallback` 方法 |
| `lib/webview/jcef/.../JcefWebViewProxy.kt` | 实现 `executeJavaScriptWithCallback` |
| `lib/webview/webkit/.../WebKitWebViewProxy.kt` | 实现 `executeJavaScriptWithCallback` |
| `shared/source/api/.../ComponentBusiness.kt` | 新增 `runOrNull` 方法 |
| `shared/source/.../PlayComponentExtensions.kt` | 使用 `runOrNull` 处理可空返回值 |
| `shared/src/.../CartoonRadarStrategyV1.kt` | 使用扩展函数处理剧集优先模式 |
| `shared/src/.../PlayLineIndexVM.kt` | 使用 `runOrNull` 处理可空返回值 |

### 3. 编译状态

✅ 所有模块编译成功（Desktop 和 Android）

## 待完成的工作

### 1. 测试验证
- [ ] 测试搜索功能
- [ ] 测试剧集列表获取
- [ ] 测试播放源获取
- [ ] 测试播放地址获取
- [ ] 测试 Cloudflare 验证码处理

### 2. 可能的问题
- window.$data 结构可能需要调整
- 播放地址拦截可能需要优化
- 缓存机制可能需要调优

## 技术细节

### 剧集优先模式
- `isEpisodeFirstMode()` 返回 `true`
- 先获取剧集列表 `getEpisodeList()`
- 再获取播放源 `getPlayLineSimpleForEpisode()`
- 最后获取播放地址 `getPlayInfoSimple()`

### 缓存策略
- 内存缓存，15分钟过期
- 剧集缓存：key = "anich_{bangumiId}"
- 播放源缓存：key = "anich_{bangumiId}_{episodeOrder}"

### 日志
已添加详细的日志，包括：
- URL 加载日志
- 验证码检测日志
- 播放地址拦截日志

## 相关文档

- Change: `add-anich-source`
- 位置: `openspec/changes/add-anich-source/`
