## Context

新版本（Next）已有完整的源系统架构（InnerSource → ComponentBundle → Components），支持 Android + Desktop 跨平台。当前 UI 中下载按钮为空实现（`onDownload = {}`）。旧版本（old/）有完整的下载系统（Aria + Media3 Transformer）和本地源（Kodi NFO），但仅支持 Android。

本设计将旧版能力迁移到新架构，同时扩展为跨平台方案。

## Goals / Non-Goals

**Goals:**

- 用户可以从在线源选择剧集下载到本地
- 下载的番剧通过本地源在应用内播放
- 支持普通 HTTP 和 M3U8(HLS) 两种流媒体格式
- Android 支持私有目录和用户自选目录（SAF）
- 下载系统采用可插拔架构，平台可注册不同的下载策略
- 使用 Kodi NFO 格式存储元数据

**Non-Goals:**

- 不支持 BT/磁力链接下载（PlayInfo.TYPE_BT_URL / TYPE_BT_MAGNET 暂不实现）
- 不支持手动创建本地番剧条目（仅通过下载自动创建）
- 不实现视频转码/重编码（Desktop 端 FFmpeg 仅做 TS 合并，不做格式转换）
- 不实现下载限速功能

## Decisions

### 1. 可插拔 DownloadAction 接口

**决策**：定义 `DownloadAction` 接口，不同平台注册不同实现。

**理由**：
- Android 的 Aria 和 Media3 无法在 Desktop 使用
- Desktop 的系统 FFmpeg 无法在 Android 使用
- 可插拔架构允许平台独立演进

**替代方案**：
- 纯 Ktor 实现（所有平台统一）→ M3U8 合并能力不足，需要 FFmpeg
- 分平台独立模块（不共享接口）→ 代码重复，调度逻辑需重写

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

### 2. 下载链（Step Chain）模式

**决策**：每个下载任务由一系列 Action 按顺序执行，形成"下载链"。

**理由**：
- 旧版已验证此模式可行
- 步骤清晰，易于调试和扩展
- 支持断点续传（从某个步骤恢复）

**下载链配置**：
- `TYPE_NORMAL` → `[parse, ktor_http_download, copy_and_nfo]`
- `TYPE_HLS` Android → `[parse, aria_m3u8, transcode, copy_and_nfo]`
- `TYPE_HLS` Desktop → `[parse, ffmpeg_m3u8, copy_and_nfo]`

### 3. 本地源仅实现 PlayComponent + PrefComponent

**决策**：本地源不实现 SearchComponent、HomeComponent、FilterComponent。

**理由**：
- 本地番剧通过 Story 页面入口访问，不需要独立的搜索/首页
- 减少实现复杂度
- 后续可按需扩展

### 4. 松耦合设计

**决策**：下载系统和本地源通过事件/回调通信，不共享状态。

**理由**：
- 模块边界清晰，可独立测试
- 下载完成 → `LocalCartoonController.refresh()` 重新扫描文件夹
- 删除本地番剧 → 级联删除相关下载请求

### 5. 使用项目已有的 UniFile/UFD 体系

**决策**：不引入新的文件操作库，使用项目的 `UniFile` + `UFD`。

**理由**：
- 项目已有成熟的跨平台文件抽象
- Android 特殊支持：`UFD.TYPE_ANDROID_UNI` 包装 SAF URI
- `UFD.TYPE_JVM` 用于普通文件路径

### 6. Android 路径双模式

**决策**：支持私有目录和用户自选目录两种模式。

**理由**：
- 私有目录：无需权限，简单可靠
- 用户自选目录：用户可管理文件，与旧版行为一致
- 通过 `PreferenceStore` 存储 `UFD`（可序列化）

### 7. Kodi NFO 元数据格式

**决策**：沿用旧版的 Kodi NFO 格式。

**理由**：
- 旧版已验证可行
- Kodi NFO 是成熟的标准，有丰富的工具支持
- `tvshow.nfo` 存储番剧元数据，`*.nfo` 存储剧集元数据

### 8. Desktop M3U8 使用系统 FFmpeg

**决策**：Desktop 端通过 `ProcessBuilder` 调用系统 FFmpeg 合并 TS 分片。

**理由**：
- 避免引入重量级 FFmpeg 绑定库
- 系统 FFmpeg 通常已安装（开发者/高级用户）
- 可检测 FFmpeg 是否可用，不可用时提示用户

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Desktop 端 FFmpeg 未安装 | M3U8 下载不可用 | 检测 FFmpeg 可用性，不可用时提示安装；普通 HTTP 下载不受影响 |
| Android SAF 权限丢失 | 用户自选目录无法访问 | 检测权限状态，自动回退到私有目录并提示用户重新授权 |
| Aria 库仅 Android | Desktop 无法使用 Aria | Desktop 使用 Ktor + FFmpeg 替代方案 |
| 加密 M3U8 流 | 某些源使用 DRM 加密 | 支持 AES-128-CBC 解密；DRM 加密流无法处理，提示用户 |
| 大文件下载占用空间 | 磁盘空间不足 | 下载前检查可用空间（后续优化） |
| 并发下载过多 | 内存/网络压力 | 默认最多 3 个并发任务 |
