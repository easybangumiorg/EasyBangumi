## 1. 数据结构

- [x] 1.1 创建 `EpisodeSimple.kt` 数据结构（id, label, order, sourceName, ext）
- [x] 1.2 创建 `PlayLineSimple.kt` 数据结构（id, label, order, ext）

## 2. 接口层

- [x] 2.1 在 `IPlayComponent` 接口新增 `isEpisodeFirstMode()` 方法（默认返回 false）
- [x] 2.2 在 `IPlayComponent` 接口新增 `getEpisodeList()` 方法（默认返回 null）
- [x] 2.3 在 `IPlayComponent` 接口新增 `getPlayLineSimpleForEpisode()` 方法（默认返回 null）
- [x] 2.4 在 `IPlayComponent` 接口新增 `getPlayInfoSimple()` 方法（默认返回 null）

## 3. 包装层

- [x] 3.1 在 `ComponentBusiness` 新增 `runNoRetry()` 方法（无重试执行）
- [x] 3.2 创建 `PlayComponent` 扩展函数（isEpisodeFirstMode, getEpisodeList, getPlayLineSimpleForEpisode, getPlayInfoSimple）
- [x] 3.3 在 `CacheablePlayComponentWrapper` 实现 `getPlayInfoSimple()` 的缓存逻辑

## 4. ViewModel 层

- [x] 4.1 在 `PlayLineIndexVM.State` 新增 `isEpisodeFirst` 标记字段
- [x] 4.2 在 `PlayLineIndexVM.State` 新增剧集优先模式状态字段（episodeList, currentEpisodeIndex, episodePlayLineList, currentEpisodePlayLine）
- [x] 4.3 实现 `loadEpisodeFirst()` 方法（加载剧集列表 + 默认选中第一个）
- [x] 4.4 实现 `onEpisodeSimpleSelected()` 方法（切换剧集 + 刷新播放线路）
- [x] 4.5 实现 `onPlayLineSimpleSelected()` 方法（切换播放线路）
- [x] 4.6 实现 `onShowingEpisodeSelected()` 方法（UI 展示用）
- [x] 4.7 实现 `onShowingEpisodePlayLineSelected()` 方法（UI 展示用）
- [x] 4.8 实现剧集优先模式的 `refreshPlayInfo()` 逻辑（监听状态变化触发 getPlayInfoSimple）

## 5. UI 组件 - Common

- [x] 5.1 在 `MediaPlayLineIndex.kt` 新增 `mediaEpisodeFirstIndex()` composable 函数

## 6. UI 组件 - Desktop

- [x] 6.1 在 `MediaPlayLineIndex.desktop.kt` 新增 `mediaEpisodeFirstIndexDesktop()` composable 函数
- [x] 6.2 实现收起状态（LazyRow + 箭头按钮）
- [x] 6.3 实现展开状态（FlowRow）
- [x] 6.4 实现更多 Dialog（网格选择）

## 7. UI 组件 - Android

- [x] 7.1 创建 `MediaPlayLineIndex.android.kt` 文件
- [x] 7.2 实现 `mediaEpisodeFirstIndexAndroid()` composable 函数
- [x] 7.3 实现 LazyRow 横向滑动
- [x] 7.4 实现更多 ModalBottomSheet

## 8. 播放雷达适配

- [x] 8.1 扩展 `CartoonCoverResult` 新增 `episodes` 字段
- [x] 8.2 扩展 `MediaFinderVM.SelectionResult` 新增 `suggestEpisode` 字段
- [x] 8.3 在 `CartoonRadarStrategyV1.searchSource()` 适配剧集优先模式验证逻辑

## 9. 调用处适配

- [x] 9.1 修改 `NormalMediaCommonVM` 相关 Composable，根据 `isEpisodeFirst` 选择渲染组件
- [x] 9.2 修改 `BangumiMediaCommonVM` 相关 Composable，根据 `isEpisodeFirst` 选择渲染组件
