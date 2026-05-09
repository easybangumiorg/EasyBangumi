## 1. 基础设施 - 本地文件管理

- [x] 1.1 创建 `shared/local/` 模块，添加 build.gradle.kts
- [x] 1.2 实现 `LocalPreference`：路径配置（usePrivate、userFolderUFD、noMedia），使用 PreferenceStore + UFD
- [x] 1.3 实现 `LocalItemFactory`：Kodi NFO 解析（tvshow.nfo、episodedetails），使用 UniFile
- [x] 1.4 实现 `LocalCartoonController`：文件夹扫描/索引，暴露 StateFlow<List<LocalCartoonItem>>

## 2. 基础设施 - 下载模块骨架

- [x] 2.1 创建 `shared/download/` 模块，添加 build.gradle.kts
- [x] 2.2 定义 `DownloadAction` 接口（name、isAsync、canResume、execute、pause、resume、cancel、onTaskComplete）
- [x] 2.3 定义数据模型：`DownloadReq`、`DownloadRuntime`、`DownloadInfo`、`DownloadState`
- [x] 2.4 实现 `DownloadActionRegistry`：Action 注册表
- [x] 2.5 实现 `DownloadChain`：根据 PlayInfo.type 和平台选择下载链

## 3. 本地源

- [x] 3.1 创建 `shared/source/inner/local/` 目录
- [x] 3.2 实现 `LocalInnerSource`：继承 InnerSource，key="easybangumi_local"，注册 PlayComponent + PrefComponent
- [x] 3.3 实现 `LocalPlayComponent`：从 LocalCartoonController 查找本地番剧，返回 PlayInfo(TYPE_NORMAL, 本地文件路径)
- [x] 3.4 实现 `LocalPrefComponent`：路径选择（私有/用户目录）、.nomedia 开关
- [x] 3.5 在 `InnerSourceProvider` 中注册 LocalInnerSource

## 4. 跨平台下载核心

- [x] 4.1 实现 `ParseAction`：从源的 PlayComponent 获取 PlayInfo，支持超时重试（50秒，3次）
- [x] 4.2 实现 `KtorHttpDownloadAction`：基于 Ktor HttpClient 的普通 HTTP 下载，支持 Range header 暂停/恢复
- [x] 4.3 实现 `M3u8Parser`：解析 M3U8 播放列表，提取 TS 分片 URL 和加密信息
- [x] 4.4 实现 `M3u8Decryptor`：AES-128-CBC 解密（纯 Kotlin，跨平台）
- [x] 4.5 实现 `CopyAndNfoAction`：复制视频到本地目录 + 生成 Kodi NFO 文件
- [x] 4.6 实现 `DownloadReqController`：下载请求持久化（JSON 文件）
- [x] 4.7 实现 `DownloadDispatcher`：任务调度器，管理并发（默认3个），驱动 Action 链执行
- [x] 4.8 实现 `DownloadManager`：对外统一接口，协调 ReqController + Dispatcher

## 5. 平台特定实现

- [x] 5.1 Android：实现 `AriaM3u8DownloadAction`，使用 Aria 库下载 M3U8
- [x] 5.2 Android：实现 `TransformerAction`，使用 Media3 转码
- [x] 5.3 Android：创建 Koin 模块，注册 AriaM3u8DownloadAction、TransformerAction
- [x] 5.4 Desktop：实现 `FfmpegMerger`，通过 ProcessBuilder 调用系统 FFmpeg 合并 TS
- [x] 5.5 Desktop：实现 `FfmpegM3u8DownloadAction`，M3U8 下载 + AES 解密 + FFmpeg 合并
- [x] 5.6 Desktop：创建 Koin 模块，注册 FfmpegM3u8DownloadAction
- [x] 5.7 实现平台通用 Koin 模块，注册 ParseAction、KtorHttpDownloadAction、CopyAndNfoAction

## 6. Story UI

- [x] 6.1 实现 `StoryController`：协调 DownloadManager + LocalCartoonController，下载完成自动刷新本地源
- [x] 6.2 实现 `StoryViewModel`：管理 UI 状态
- [x] 6.3 实现 `StoryPage`：Compose UI，包含三个子 Tab（下载中/已完成/本地番剧）
- [x] 6.4 实现下载中列表项：显示进度、状态、暂停/恢复/取消按钮
- [x] 6.5 实现本地番剧列表项：显示封面、名称、标签，支持播放和删除
- [x] 6.6 集成 Story Tab 到底部导航

## 7. 完善

- [x] 7.1 实现下载发起流程：从在线源详情页选择剧集 → 创建下载请求
- [x] 7.2 实现自动创建本地番剧条目（tvshow.nfo + cover.png）
- [x] 7.3 实现删除本地番剧时级联删除相关下载请求
- [x] 7.4 Android：SAF 权限检测与回退（权限丢失时自动切换到私有目录）
- [x] 7.5 Desktop：FFmpeg 可用性检测与提示
- [x] 7.6 .nomedia 文件自动创建逻辑
