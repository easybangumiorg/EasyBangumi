## ADDED Requirements

### Requirement: 可插拔下载动作接口

系统 SHALL 定义 `DownloadAction` 接口，支持平台特定的下载策略实现。接口 MUST 包含 `name`、`isAsync()`、`canResume()`、`execute()`、`pause()`、`resume()`、`cancel()`、`onTaskComplete()` 方法。

#### Scenario: 注册下载动作
- **WHEN** 应用启动时
- **THEN** 各平台通过 Koin 模块注册本平台的 DownloadAction 实现（如 Android 注册 AriaM3u8DownloadAction，Desktop 注册 FfmpegM3u8DownloadAction）

#### Scenario: 根据 PlayInfo.type 选择下载链
- **WHEN** 用户发起下载请求且 PlayInfo.type 为 TYPE_NORMAL
- **THEN** 系统使用下载链 `[parse, ktor_http_download, copy_and_nfo]`

#### Scenario: M3U8 平台路由
- **WHEN** 用户发起下载请求且 PlayInfo.type 为 TYPE_HLS
- **THEN** Android 使用 `[parse, aria_m3u8, transcode, copy_and_nfo]`，Desktop 使用 `[parse, ffmpeg_m3u8, copy_and_nfo]`

### Requirement: 普通 HTTP 下载

系统 SHALL 支持通过 Ktor HttpClient 下载普通视频文件（TYPE_NORMAL），支持进度报告和暂停/恢复（HTTP Range header）。

#### Scenario: 成功下载普通视频
- **WHEN** 下载链执行到 KtorHttpDownloadAction 且 PlayInfo.type 为 TYPE_NORMAL
- **THEN** 系统通过 Ktor HttpClient 下载文件到缓存目录，报告下载进度

#### Scenario: 暂停和恢复下载
- **WHEN** 用户暂停一个正在进行的 HTTP 下载
- **THEN** 系统停止下载，记录已下载字节数；用户恢复时通过 Range header 继续下载

### Requirement: M3U8 播放列表解析

系统 SHALL 能解析 M3U8 播放列表，提取 TS 分片 URL、加密信息等。

#### Scenario: 解析非加密 M3U8
- **WHEN** M3U8 播放列表不包含加密信息
- **THEN** 解析器返回所有 TS 分片 URL 列表

#### Scenario: 解析加密 M3U8
- **WHEN** M3U8 播放列表包含 AES-128 加密信息
- **THEN** 解析器返回 TS 分片 URL 列表及对应的密钥 URL 和 IV

### Requirement: M3U8 AES-128-CBC 解密

系统 SHALL 支持 AES-128-CBC 解密 M3U8 TS 分片（纯 Kotlin 实现，跨平台）。

#### Scenario: 解密加密分片
- **WHEN** 下载的 TS 分片使用 AES-128-CBC 加密
- **THEN** 系统使用密钥和 IV 解密分片，输出解密后的 TS 数据

### Requirement: Desktop FFmpeg 合并

Desktop 平台 SHALL 通过系统 FFmpeg 合并 TS 分片为 MP4 文件。

#### Scenario: 检测 FFmpeg 可用性
- **WHEN** Desktop 端发起 M3U8 下载
- **THEN** 系统先检测系统 FFmpeg 是否可用，不可用时返回错误提示

#### Scenario: 合并 TS 为 MP4
- **WHEN** 所有 TS 分片下载完成
- **THEN** 系统调用 FFmpeg 的 concat demuxer 将分片合并为单个 MP4 文件

### Requirement: Android Aria M3U8 下载

Android 平台 SHALL 使用 Aria 库下载 M3U8 流媒体。

#### Scenario: Aria 下载 M3U8
- **WHEN** Android 端发起 M3U8 下载
- **THEN** 系统使用 Aria 的 M3U8VodOption 配置下载，支持进度报告和暂停/恢复

### Requirement: 下载任务调度

系统 SHALL 支持并发下载任务管理，默认最多 3 个并发任务。

#### Scenario: 提交下载任务
- **WHEN** 用户提交新的下载请求
- **THEN** 系统将请求持久化到 JSON 文件，创建 DownloadRuntime 并提交到 Dispatcher

#### Scenario: 并发限制
- **WHEN** 已有 3 个任务在运行
- **THEN** 新任务进入等待队列，有任务完成后自动启动下一个

#### Scenario: 下载完成
- **WHEN** 下载链所有步骤执行完成
- **THEN** 系统清理缓存文件、删除持久化请求、通知本地源刷新

### Requirement: 下载请求持久化

系统 SHALL 将下载请求持久化到 JSON 文件，支持应用重启后恢复。

#### Scenario: 持久化下载请求
- **WHEN** 新的下载请求创建
- **THEN** 请求被序列化为 JSON 并保存到文件

#### Scenario: 应用重启恢复
- **WHEN** 应用重启且存在未完成的下载请求
- **THEN** 系统从 JSON 文件加载请求，重新提交到 Dispatcher

### Requirement: ParseAction 解析播放地址

ParseAction SHALL 从源的 PlayComponent 获取实际播放地址（PlayInfo）。

#### Scenario: 成功解析
- **WHEN** PlayComponent 返回有效的 PlayInfo
- **THEN** 将 PlayInfo 存入 DownloadRuntime，进入下一步骤

#### Scenario: 解析超时重试
- **WHEN** PlayComponent 调用超时（50秒）
- **THEN** 系统重试最多 3 次，全部失败则报告错误

### Requirement: CopyAndNfoAction 生成本地文件

CopyAndNfoAction SHALL 将下载完成的视频文件复制到本地番剧目录，并生成 Kodi NFO 元数据文件。

#### Scenario: 复制视频并生成 NFO
- **WHEN** 视频文件下载完成
- **THEN** 系统将文件复制到 `{root}/{itemId}/` 目录，文件名为 `{itemId} {episodeTitle} S1E{episodeNum}.mp4`，同时生成对应的 `.nfo` 文件
