# 计划：添加番剧下载功能 + 本地源

## 概述

新增番剧下载系统和本地源，允许用户将在线番剧下载到本地，并通过本地源直接播放已下载的内容。采用 Kodi NFO 格式存储元数据，支持 Android + Desktop 跨平台。

## 目标

1. 用户可以从在线源下载番剧到本地
2. 下载的番剧通过本地源在应用内播放
3. 支持 Android（私有目录/用户自选目录）和 Desktop
4. 下载系统采用可插拔架构，支持多种下载策略

## 模块结构

```
shared/
  ├── source/inner/local/           ← 本地源
  │    ├── LocalInnerSource.kt
  │    ├── LocalPlayComponent.kt
  │    └── LocalPrefComponent.kt
  │
  ├── download/                     ← 下载模块
  │    ├── DownloadManager.kt
  │    ├── DownloadDispatcher.kt
  │    ├── DownloadReqController.kt
  │    ├── action/
  │    │    ├── DownloadAction.kt          ← 接口
  │    │    ├── ParseAction.kt             ← 跨平台
  │    │    ├── CopyAndNfoAction.kt        ← 跨平台
  │    │    ├── http/
  │    │    │    └── KtorHttpDownloadAction.kt  ← 跨平台
  │    │    └── m3u8/
  │    │         ├── M3u8Parser.kt
  │    │         ├── M3u8Decryptor.kt      ← AES-128-CBC
  │    │         └── FfmpegM3u8DownloadAction.kt  ← Desktop
  │    ├── model/
  │    └── platform/
  │         ├── android/
  │         │    ├── AriaM3u8DownloadAction.kt
  │         │    └── TransformerAction.kt
  │         └── desktop/
  │              └── FfmpegMerger.kt
  │
  ├── local/                        ← 本地文件管理
  │    ├── LocalCartoonController.kt
  │    ├── LocalItemFactory.kt
  │    └── LocalPreference.kt
  │
  └── story/                        ← Story UI
       ├── StoryController.kt
       ├── StoryViewModel.kt
       └── StoryPage.kt
```

## 关键设计决策

### 1. 本地源组件
- 实现 `PlayComponent` + `PrefComponent`
- 不需要 Search/Home/Filter（通过 Story 页面入口访问）
- 源 key: `"easybangumi_local"`

### 2. 下载系统 - 可插拔架构

```kotlin
interface DownloadAction {
    val name: String
    fun isAsync(): Boolean
    suspend fun canResume(req: DownloadReq): Boolean
    suspend fun execute(runtime: DownloadRuntime)
    suspend fun pause(runtime: DownloadRuntime): Boolean
    suspend fun resume(runtime: DownloadRuntime): Boolean
    suspend fun cancel(runtime: DownloadRuntime)
    suspend fun onTaskComplete(runtime: DownloadRuntime)
}
```

下载链根据 `PlayInfo.type` 和平台选择：
- `TYPE_NORMAL` → `["parse", "ktor_http_download", "copy_and_nfo"]`
- `TYPE_HLS` Android → `["parse", "aria_m3u8", "transcode", "copy_and_nfo"]`
- `TYPE_HLS` Desktop → `["parse", "ffmpeg_m3u8", "copy_and_nfo"]`

### 3. 平台差异化下载策略

| 场景 | Android | Desktop |
|------|---------|---------|
| 普通 HTTP | KtorHttpDownloadAction | KtorHttpDownloadAction |
| M3U8 | AriaM3u8DownloadAction | FfmpegM3u8DownloadAction |
| 转码 | TransformerAction (Media3) | 不需要（FFmpeg 直接处理） |

### 4. M3U8 处理
- M3u8Parser：解析 M3U8 播放列表
- M3u8Decryptor：AES-128-CBC 解密（纯 Kotlin，跨平台）
- Desktop: 系统 FFmpeg 合并 TS → MP4
- Android: Aria 内置 M3U8 支持 + TranscodeAction

### 5. 文件系统
- 使用项目已有的 `UniFile` / `UFD` 体系
- Android 私有目录：`pathProvider.getFilePath("local_source")` → `UFD(TYPE_JVM, ...)`
- Android 用户目录：SAF URI → `UFD(TYPE_ANDROID_UNI, uri)`
- 使用 `PreferenceStore` 存储路径配置

### 6. 路径配置（Android 特殊处理）

```kotlin
class LocalPreference(preferenceStore, pathProvider) {
    val usePrivate: Preference<Boolean>     // 是否使用私有目录
    val userFolderUFD: Preference<String>   // 用户目录 UFD (JSON)
    val noMedia: Preference<Boolean>        // .nomedia
    
    val realLocalFolderUFD: StateFlow<UFD>  // 计算实际路径
}
```

### 7. 元数据格式（Kodi NFO）

```
{root}/local_bangumi/
  ├── {itemId}/
  │    ├── tvshow.nfo      ← <tvshow><title>...</title><plot>...</plot>...</tvshow>
  │    ├── cover.png
  │    ├── S1E1.mp4
  │    ├── S1E1.nfo        ← <episodedetails><title>...</title><episode>1</episode>...
  │    └── ...
  └── .nomedia
```

### 8. 耦合度
- 松耦合：下载完成后 `DownloadDispatcher` 通知 `LocalCartoonController.refresh()`

### 9. UI 入口
- 保持旧版 Story 页面设计（底部导航 Tab）
- 包含三个子 Tab：下载中 / 已完成 / 本地番剧

## 数据流

```
用户选择剧集
  → DownloadReqFactory.create()
  → DownloadManager.newRequest()
    → DownloadReqController.persist() (JSON)
    → DownloadDispatcher.submit()
      → Action1.execute() → stepComplete
      → Action2.execute() → stepComplete
      → Action3.execute() → stepComplete
      → SUCCESS
        → 清理缓存
        → 删除持久化请求
        → LocalCartoonController.refresh()
```

## 实现阶段

### Phase 1: 基础设施 (2-3 天)
- [ ] 创建 `shared/local/` 模块
  - [ ] LocalPreference（路径配置，支持私有/用户目录）
  - [ ] LocalItemFactory（NFO 解析/创建，使用 UniFile）
  - [ ] LocalCartoonController（文件夹扫描/索引）
- [ ] 创建 `shared/download/` 骨架
  - [ ] DownloadAction 接口
  - [ ] DownloadReq / DownloadRuntime / DownloadInfo 数据类
  - [ ] DownloadActionRegistry
- [ ] 注册 LocalInnerSource 到 InnerSourceProvider

### Phase 2: 跨平台下载核心 (3-4 天)
- [ ] ParseAction（从源获取 PlayInfo）
- [ ] KtorHttpDownloadAction（普通 HTTP 下载，支持 Range）
- [ ] M3u8Parser（M3U8 播放列表解析）
- [ ] M3u8Decryptor（AES-128-CBC 解密）
- [ ] CopyAndNfoAction（复制到本地 + 生成 NFO）
- [ ] DownloadDispatcher（任务调度器）
- [ ] DownloadReqController（请求持久化到 JSON）

### Phase 3: 平台特定实现 (2-3 天)
- [ ] Android: AriaM3u8DownloadAction
- [ ] Android: TransformerAction（Media3 转码）
- [ ] Desktop: FfmpegMerger（系统 FFmpeg 合并）
- [ ] Desktop: FfmpegM3u8DownloadAction
- [ ] DownloadChain 平台路由逻辑
- [ ] Koin 模块注册（androidModule / desktopModule）

### Phase 4: Story UI (2-3 天)
- [ ] StoryController（协调下载 + 本地源）
- [ ] StoryViewModel
- [ ] StoryPage（Compose UI）
  - [ ] 下载中列表（进度、暂停/恢复/取消）
  - [ ] 已完成列表
  - [ ] 本地番剧列表
- [ ] 集成到底部导航

### Phase 5: 完善 (1-2 天)
- [ ] 暂停/恢复/取消功能
- [ ] 错误重试（ParseAction 3 次重试）
- [ ] .nomedia 支持
- [ ] 下载完成通知
- [ ] 删除本地番剧（级联删除下载请求）

## 参考文件

### 旧版本（old/ 目录）
- `cartoon/story/local/` - 本地源实现
- `cartoon/story/download/` - 下载系统
- `cartoon/story/CartoonStoryController.kt` - 耦合控制器
- `cartoon/entity/CartoonDownloadInfo.kt` - 下载数据模型

### 新版本
- `shared/source/api/` - 源 API 接口
- `shared/source/inner/age/` - 现有源实现示例
- `lib/unifile/` - UniFile / UFD 体系
- `lib/store/preference/` - PreferenceStore
- `shared/ktor/` - KtorFactory

## 风险和注意事项

1. **Aria 仅 Android**：M3U8 下载在 Desktop 上需要用 FFmpeg 替代
2. **SAF 权限**：Android 用户自选目录需要持久化 URI 权限
3. **FFmpeg 可用性**：Desktop 端依赖系统安装 FFmpeg，需要检测和提示
4. **大文件下载**：需要考虑磁盘空间检查
5. **并发控制**：默认最多 3 个下载任务同时进行
