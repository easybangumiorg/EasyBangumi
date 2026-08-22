## 1. Foundation and integration validation

- [x] 1.1 Audit the existing playback route, state ownership, and legacy page boundaries; define the V2 route and legacy fallback selection mechanism.
- [x] 1.2 Add DanmakuFlameMaster through a dependency configuration compatible with the current Android/Gradle toolchain and verify a clean build plus supported-ABI packaging.
- [x] 1.3 Obtain and configure approved DanDanPlay application credentials outside committed source, document attribution, and verify authenticated search/comment requests within quota expectations.
- [x] 1.4 Define normalized danmaku models, source metadata, result/error states, cancellation ownership, cache expiry policy, and persistent per-episode binding schema.

## 2. Built-in danmaku source domain

- [x] 2.1 Implement the built-in `DanmakuSource` contract and `InnerDanmakuSourceRegistry` with no external registration path.
- [x] 2.2 Implement the DanDanPlay source adapter for title/metadata resolution, anime search, remote episode listing, comment retrieval, and returned provenance mapping.
- [x] 2.3 Add persistent source enable/default preferences and binding/cache storage with migrations where required.
- [x] 2.4 Implement bounded, cancellable retrieval with cache reads/writes and stale playback-session result protection.
- [x] 2.5 Add a built-in danmaku-source management screen that exposes DanDanPlay metadata, enable/default controls, and non-removable status.
- [x] 2.6 Add domain tests for source registry restrictions, binding reuse, automatic-match ambiguity, cache expiry, and stale-result handling.

## 3. Danmaku matching experience

- [x] 3.1 Add V2 playback danmaku state orchestration that reuses bindings, performs high-confidence automatic matching, and exposes retryable unavailable states.
- [x] 3.2 Build the peer-level `弹幕` section for disabled, loading, matched, empty, unavailable, and failed states.
- [x] 3.3 Implement the stateful manual matching sheet: editable title query, anime candidate selection, remote episode selection, binding persistence, and comment load.
- [x] 3.4 Add playback-facing tests for binding reuse, ambiguous match confirmation, manual selection, errors, and episode changes during loading.

## 4. Danmaku rendering and settings

- [x] 4.1 Create a DFM renderer adapter that converts normalized comments to render items and owns prepare, clear, seek, visibility, and release operations.
- [x] 4.2 Mount a transparent non-intercepting DFM overlay in the V2 player foreground below Compose controls and above the video texture.
- [x] 4.3 Synchronize renderer state with ExoPlayer start, pause, seek, episode replacement, page disposal, and external-player playback.
- [x] 4.4 Add persisted user controls for enablement, category/provenance filtering, and time offset; apply changes without reopening the page.
- [x] 4.5 Verify renderer behavior on target devices for TextureView z-order, controls touchability, seek correctness, resource release, and large comment sets.

## 5. V2 playback-detail page

- [x] 5.1 Create the isolated V2 playback-detail composition using existing playback and cartoon view models without modifying the legacy page.
- [x] 5.2 Implement the media identity area with a two-line clamped synopsis and expand/collapse behavior.
- [x] 5.3 Preserve follow, search, website, download, and external-playback actions in an evenly spaced action row with one divider below the group.
- [x] 5.4 Implement the `播放源` section with horizontally scrollable source chips and existing playing-source indication.
- [x] 5.5 Implement the `选集` section with wider short horizontal episode buttons, current-episode state, episode-level sort action, and all-episodes entry point.
- [x] 5.6 Implement `EpisodePickerBottomSheet` with source switching, search, sorting, current-episode focus, grid selection, and dismissal on selection.
- [x] 5.7 Reuse existing sort/display persistence across the quick rail and picker; retain the legacy sort/display sheet behavior where it remains available.
- [x] 5.8 Add Compose UI tests for synopsis expansion, action availability, source/episode selection, picker flow, sorting consistency, and legacy fallback navigation.

## 6. Verification and rollout

- [x] 6.1 Run unit, Compose UI, and relevant instrumentation tests; add regressions for V2/legacy shared playback state.
- [x] 6.2 Manually verify the approved mobile layout at common phone widths, including long titles, long episode labels, many routes, and long episode lists.
- [x] 6.3 Manually verify automatic/manual DanDanPlay matching, error states, caching, display settings, DFM timing, and external-player behavior.
- [x] 6.4 Enable V2 as the default route only after verification, retain the documented legacy fallback, and record release/rollback validation results.

## 7. 播放页稳定性与交互整理

- [x] 7.1 以稳定的 `selectedLineId`、`playLineId` 和 `episodeId` 整理 V2 播放状态数据流，并确保排序刷新不触发重新播放或清除选中态。
- [x] 7.2 将 `DetailedViewModel.sortStateFlow` 改为从单个详情快照原子派生，并让 `tryNext` 使用最新排序投影。
- [x] 7.3 新增 V2 排序 Bottom Sheet，复用旧版 `SortColumn` 的默认/名称与正序/倒序语义，并接入快捷选集和全部选集。
- [x] 7.4 参考旧版将媒体详情改为整块点击、上下箭头及顺序淡出/淡入动画，移除独立文字按钮。
- [x] 7.5 在播放器与详情面板之间实现主题色矩形播放进度分割线，并处理未知时长。
- [x] 7.6 将自动番剧匹配改为只检查首个番剧且标题相似度严格大于 80%，成功后再按当前排序位置递进匹配选集，并保持缓存、超时与 generation 防串集。
- [x] 7.7 修正弹幕板块水平边距及手动匹配番剧/选集列表的 Bottom Sheet 背景色。
- [x] 7.8 补充标题阈值、纯位置选集解析、排序后稳定选中态及下一集顺序的单元/界面回归测试。
- [x] 7.9 运行单元测试与 Debug 构建，在连接设备上验证详情动画、四类排序、选中态、匹配面板、进度分割线及弹幕生命周期。
- [x] 7.10 将弹幕匹配拆为页面级番剧匹配与播放目标级选集匹配；无已提交番剧时手动流程依次选择番剧和选集，已有番剧时同步选择并直接进入选集步骤，后续播放切换只按当前排序位置重匹配选集。
