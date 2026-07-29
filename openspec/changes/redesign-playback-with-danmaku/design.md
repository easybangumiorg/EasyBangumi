## Context

`CartoonPlayDetailed` currently combines source tabs and an unbounded `LazyVerticalGrid` of episodes. Its existing `PlayDetailedBottomSheet` only configures sort and grid display. The playback renderer is an ExoPlayer-backed `TextureView` hosted by `EasyPlayerScaffoldBase`; the foreground is available for an overlay. The product needs a more deliberate mobile playback-detail experience and DanDanPlay support without changing the legacy playback screen.

The new feature crosses Compose UI, navigation, playback state, persistent data, network access, settings, and a legacy Android View renderer. DanDanPlay now requires application credentials and places quota/caching constraints on search and comment retrieval.

## Goals / Non-Goals

**Goals:**

- Introduce an isolated V2 playback-detail composition with a deliberate Material 3 hierarchy matching the approved direction.
- Preserve the legacy playback-detail implementation unchanged and make it usable as a rollback target.
- Make route selection, horizontal quick episode navigation, all-episodes browsing, and sort/display controls coherent.
- Provide automatic, user-correctable DanDanPlay episode matching and cached comment loading.
- Keep the source model built-in-only while creating a stable abstraction and management UI.
- Render timed comments above the existing video texture while keeping controls interactive and lifecycle-safe.

**Non-Goals:**

- External or script-installed danmaku sources, user-submitted source definitions, and source repositories.
- Sending danmaku, user login, social features, or importing local comment files.
- Replacing the existing legacy playback page, its download-selection behavior, or fullscreen side episode picker.
- A broad visual redesign of unrelated detail, settings, or source-management pages.

## Decisions

### 1. Isolated V2 playback page with explicit legacy fallback

Create a new V2 playback-detail route/composition and route normal playback to it only after it is feature-complete. Keep the existing `CartoonPlay`, `CartoonPlayDetailed`, and their state behavior untouched. A persistent developer/recovery setting or a clearly scoped navigation switch SHALL select the legacy route for rollback.

This is preferred over incrementally changing the existing page because the new hierarchy changes the primary scrolling model and state ownership. Duplicating the full parsing/player pipeline is rejected; both pages will consume the existing `CartoonPlayViewModel`, `CartoonPlayingViewModel`, and source data contracts.

### 2. Section-led playback-detail hierarchy

The V2 page preserves the video, collapsed two-line synopsis, and existing five action functions. A single divider below the action row begins the playback area. `播放源`, `选集`, and `弹幕` are peer-level headings; spacing, headings, and thin separators create hierarchy rather than nested cards.

Routes remain horizontally scrollable choice chips. The quick episode rail uses wider, short rounded buttons with horizontal overflow. `排序` and `全部选集` belong to the episode heading; `全部选集` opens an episode picker sheet, while the existing sort/display behavior remains accessible as an episode-level control.

The alternative of retaining the vertical grid as the default was rejected because long shows dominate the page and separate route selection from episode navigation.

### 3. One bottom-sheet task for complete episode selection

`EpisodePickerBottomSheet` owns route switching, episode filtering, sorting, current-item focus, and episode selection. It is an expanded Material 3 sheet with a 3-column grid. Selecting an episode changes the existing play state and dismisses the sheet. The sheet reuses the persisted ordering state rather than creating a separate order model.

This avoids chained dialogs and preserves the existing sort/display sheet semantics. It also avoids replacing the fullscreen episode drawer, which serves a different in-play context.

### 4. Danmaku source versus entry provenance

Introduce a `DanmakuSource` contract with source metadata, bangumi search, episode listing, and comment loading. The page-level coordinator owns the two progressive matching stages instead of exposing one combined source-level automatic match. An `InnerDanmakuSourceRegistry` is the only registration mechanism in this change and initially registers `DanDanPlaySource`.

DanDanPlay-returned entry provenance (for example BiliBili, Gamer, or DanDanPlay) remains a property of a comment for filtering; it is not modelled as a separately installable source. This prevents the source manager from presenting unavailable direct upstream integrations.

### 5. 将页面级番剧选择与播放目标级选集 binding 分离

Persist a binding keyed by the actual playback identity: cartoon summary/source, selected play line, and episode identity. Separately retain a page-scoped bangumi selection with automatic/manual origin and its remote episode list. A playback-target change cancels only episode work, so line changes, episode changes, and next-episode transitions reuse the selected bangumi.

Manual matching is a single stateful sheet: editable title search → bangumi selection → remote episode selection → bind and load. The sheet always starts from bangumi selection, and committing the episode promotes the explicitly selected bangumi to the page-scoped selection. Manual selection has priority over late automatic work.

### 6. Cache and credentials are first-class integration concerns

Cache title search, resolved remote episodes, and loaded comments by inner-source key and remote ID. Expiry follows DanDanPlay guidance with shorter lifetime for active shows and longer lifetime for stable content. Requests are cancellable with the playback session and are never allowed to overwrite a newer episode selection.

Credentials SHALL be obtained through an application-specific secure build/runtime configuration and not committed to source. The implementation will document DanDanPlay attribution and enforce a user-initiated, bounded request pattern.

### 7. DFM overlay below player controls

Host a single transparent DFM `DanmakuView` via `AndroidView` in the `EasyPlayerScaffoldBase` foreground, below Compose controls and above the video `TextureView`. A controller adapter translates normalized comments into DFM items and synchronizes prepare/start/pause/seek/hide/release with ExoPlayer. The renderer is created/released with the V2 page lifecycle and is disabled for external-player playback.

Using a DFM `SurfaceView` or `TextureView` is rejected because the app already has a texture-backed video surface and a normal transparent View keeps z-order and touch behavior predictable.

### 8. 使用稳定身份分离播放状态与排序投影

当前播放身份由 `cartoonId + sourceId + playLineId + episodeId` 唯一表示；浏览中的播放线路由稳定的 `selectedLineId` 表示。`PlayLineWrapper` 只承载当前排序配置下的展示投影，不作为选中态身份，也不参与跨刷新相等判断。

`DetailedViewModel` 从同一个 `CartoonInfo` 快照原子派生 `SortState`。排序写入 Room 后虽然会重新创建 `PlayLineWrapper`，界面仍按稳定 ID 解析最新对象，因此不会清除播放源、选集高亮或重新加载 ExoPlayer。`tryNext` 从最新线路快照及当前稳定 ID 计算下一集。

直接以 wrapper 实例或列表下标保存播放身份的方案被拒绝，因为持久化排序、数据刷新或线路变化都会使这些值失效。

### 9. V2 复用旧版详情与排序交互语义

详情区域采用旧版的整块点击热区、底部上下箭头和 `AnimatedContent` 顺序淡出/淡入，不再使用独立的“展开详情”文字按钮。排序按钮打开只包含旧版 `SortColumn` 的 Material Bottom Sheet，支持“默认（按 `Episode.order`）”和“名称（按 `Episode.label`）”，每个维度在正序与倒序间切换。

“显示”页用于旧版网格列数，不适用于 V2 横向选集，因此 V2 不复制该页；全部选集面板与快捷选集栏共享同一持久排序状态。

### 10. 递进的番剧匹配与纯位置选集匹配

详情和当前播放目标就绪后，页面级番剧匹配至多执行一次缓存感知的 `searchBangumi`。只检查 DanDanPlay 搜索结果中的第一个番剧；标题先做 Unicode/大小写/空白和常见标点规范化，再以归一化 Levenshtein 距离计算相似度。相似度必须严格大于 `0.80` 才能加载该番剧的远端选集并进入选集匹配。

自动选集匹配的唯一业务输入是当前播放目标在当下 `sortedEpisodeList` 中的一基位置，并直接映射到远端选集列表的同一位置。它不读取本地 `Episode.id/order/label`，也不按远端集号或标题兜底。排序变化本身不触发播放或匹配；后续真实播放目标变化使用最新排序投影的位置。番剧请求与选集请求使用独立 generation，切集不会取消或重启番剧匹配，晚到结果也不能覆盖手动番剧或新播放目标。

遍历全部搜索结果取最高分、按本地 Episode 元数据猜测远端集的方案均被拒绝；产品规则明确要求只判断第一个番剧，并以用户当前选择的排序位置作为唯一选集坐标。

`DanmakuPlaybackState.bangumiSelection` 是当前页已提交番剧的唯一状态源；手动面板只持有尚未提交的编辑草稿。打开面板时，如已存在自动、绑定恢复或手动提交的番剧，草稿直接由该选择派生为选集步骤并复用已加载选集；仅在没有已提交番剧时从番剧搜索开始。自动结果只可同步尚未编辑的空白草稿，不覆盖用户已经搜索或选择的手动草稿。

匹配成功卡片使用结构化副标题行而非嵌入换行符：第一行承载来源、远端选集和缓存状态，并在空间不足时以省略号截断；第二行固定显示纯文本 `N 条弹幕`。两行独立布局，以保证弹幕数量不会被长番名或来源挤入同一行。

### 11. 面板边界与进度分割线

播放器和详情面板之间增加无圆角矩形轨道，轨道使用主题容器色，已播放部分使用 `primary`。进度读取 `ControlViewModel.position/during` 并做轻量平滑动画；时长无效时显示零进度。手机端分割线横跨详情面板顶部，平板端保留右侧面板顶部语义。

`播放源`、`选集`、`弹幕`统一使用 20dp 水平基线。弹幕匹配结果 `ListItem` 使用透明容器色继承 Bottom Sheet 的 elevation surface，避免深浅主题下出现不协调色块。

## Risks / Trade-offs

- [DFM is an older dependency and may not resolve or behave correctly with the current Android toolchain] → Validate dependency resolution, ABI packaging, lifecycle, and texture overlay on supported devices before wiring feature UI.
- [DanDanPlay credentials or quotas may block production usage] → Obtain an approved app credential, keep secrets out of source, cache aggressively, show actionable unavailable states, and gate release on API verification.
- [Automatic title matching can choose a wrong season or special] → Persist only confirmed or high-confidence bindings; require user selection for ambiguous candidates and expose replacement at all times.
- [Two playback pages can drift] → Share existing player/domain view models and isolate only presentation and danmaku adapters; maintain a focused V2 test matrix.
- [Large comment sets can cause UI jank] → Parse/cache off main thread, bound renderer insertion, reuse loaded data, and release overlay resources promptly.

## Migration Plan

1. Land V2 route, data contracts, and the legacy-route switch without changing legacy UI behavior.
2. Validate DFM and approved DanDanPlay API access behind the V2 route.
3. Enable V2 as the default playback-detail route after functional and device verification; retain the legacy switch through at least one release cycle.
4. On failure, select the legacy route; existing watch history, source selection, and playback parsing continue to use shared contracts and need no data rollback.

## Open Questions

- Which approved DanDanPlay AppId/AppSecret delivery mechanism will the release pipeline use?
- Should the V2/legacy selector be developer-only, an ordinary player setting, or a remotely controlled rollout flag during the migration period?
