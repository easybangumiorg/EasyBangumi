## Why

当前应用只能在线播放番剧，用户无法将番剧下载到本地离线观看。旧版本（EasyBangumi4）已有完整的下载系统和本地源，但新版本（Next）尚未实现。需要将这套能力迁移到新架构中，支持 Android + Desktop 跨平台。

## What Changes

- 新增**下载模块**：可插拔的下载系统，支持普通 HTTP 和 M3U8(HLS) 两种流媒体格式
- 新增**本地源**：扫描本地文件夹中的番剧，通过 Kodi NFO 格式解析元数据，在应用内播放
- 新增 **Story 页面**：统一管理下载任务和本地番剧的 UI 入口
- 新增**本地文件管理**：负责文件夹扫描、NFO 解析/创建、路径配置
- Android 平台支持**私有目录**和**用户自选目录**（SAF）两种存储模式
- Desktop 平台通过**系统 FFmpeg** 处理 M3U8 合并

## Capabilities

### New Capabilities

- `download-system`: 可插拔的番剧下载系统，支持普通 HTTP 和 M3U8(HLS) 下载，跨平台（Android 用 Aria，Desktop 用 FFmpeg）
- `local-source`: 本地番剧源，扫描本地文件夹，解析 Kodi NFO 元数据，提供播放能力
- `story-page`: Story UI 页面，包含下载管理（下载中/已完成）和本地番剧列表

### Modified Capabilities

（无已有能力需要修改）

## Impact

- **新增模块**：`shared/download/`、`shared/local/`、`shared/source/inner/local/`、`shared/story/`
- **修改文件**：`InnerSourceProvider`（注册本地源）、底部导航（添加 Story Tab）
- **新增依赖**：无新外部依赖（使用已有的 Ktor、UniFile、PreferenceStore）
- **平台差异**：Android 依赖 Aria 库（已有）、Desktop 依赖系统 FFmpeg
