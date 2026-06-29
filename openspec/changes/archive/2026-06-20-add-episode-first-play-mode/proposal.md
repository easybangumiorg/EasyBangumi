## Why

当前播放组件（PlayComponent）采用"线路优先"模式：先获取播放线路列表，每个线路内包含剧集列表。这种模式适用于大多数番剧源，但部分源的组织方式是"剧集优先"——先有剧集列表，同一剧集可从不同播放线路获取。为了支持这类源，需要新增一种并存的播放模式。

## What Changes

- 新增 `EpisodeSimple` 和 `PlayLineSimple` 数据结构，与现有 `Episode`、`PlayerLine` 解耦（无嵌套关系）
- 在 `IPlayComponent` 接口新增可选方法：
  - `isEpisodeFirstMode()` — 判断是否为剧集优先模式（默认 false）
  - `getEpisodeList()` — 获取剧集列表
  - `getPlayLineSimpleForEpisode()` — 根据剧集获取播放线路
  - `getPlayInfoSimple()` — 获取播放信息（复用 PlayInfo）
- 扩展 `PlayLineIndexVM` 状态，支持剧集优先模式的状态管理
- 新增剧集优先模式 UI 组件（Desktop 和 Android 各一套）
- 适配播放雷达（CartoonRadarStrategyV1）支持剧集优先模式的验证逻辑
- 扩展 `CacheablePlayComponentWrapper` 支持新方法的缓存

## Capabilities

### New Capabilities
- `episode-first-play-mode`: 剧集优先播放模式，包含数据结构、接口定义、ViewModel 状态管理、UI 渲染（Desktop/Android）的完整能力

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

- **数据层**: 新增 `EpisodeSimple.kt`、`PlayLineSimple.kt`
- **接口层**: 修改 `PlayComponent.kt`，新增可选方法（向后兼容，现有源无需改动）
- **包装层**: 修改 `CacheablePlayComponentWrapper.kt`
- **ViewModel 层**: 修改 `PlayLineIndexVM.kt`，新增剧集优先模式状态和方法
- **UI 层**: 修改 `MediaPlayLineIndex.kt`（Common）、`MediaPlayLineIndex.desktop.kt`；新增 `MediaPlayLineIndex.android.kt`
- **播放雷达**: 修改 `CartoonRadarStrategyV1.kt`、`MediaFinderVM.kt`
