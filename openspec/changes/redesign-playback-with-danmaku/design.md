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

Introduce a `DanmakuSource` contract with source metadata, anime search, episode listing, comment load, and optional automatic resolution. An `InnerDanmakuSourceRegistry` is the only registration mechanism in this change and initially registers `DanDanPlaySource`.

DanDanPlay-returned entry provenance (for example BiliBili, Gamer, or DanDanPlay) remains a property of a comment for filtering; it is not modelled as a separately installable source. This prevents the source manager from presenting unavailable direct upstream integrations.

### 5. Match to an episode binding, not only a title

Persist a binding keyed by the actual playback identity: cartoon summary/source, selected play line, and episode identity. It stores the selected inner source and remote `episodeId`, plus match metadata. Automatic matching first reuses a binding; otherwise it resolves through available metadata and title + episode number. It binds automatically only for a unique/high-confidence result. Ambiguous, empty, or failed results surface an explicit manual search state.

Manual matching is a single stateful sheet: editable title search → anime selection → remote episode selection → bind and load. This is preferred to nested dialogs and prevents silent mismatch of specials or split-cour series.

### 6. Cache and credentials are first-class integration concerns

Cache title search, resolved remote episodes, and loaded comments by inner-source key and remote ID. Expiry follows DanDanPlay guidance with shorter lifetime for active shows and longer lifetime for stable content. Requests are cancellable with the playback session and are never allowed to overwrite a newer episode selection.

Credentials SHALL be obtained through an application-specific secure build/runtime configuration and not committed to source. The implementation will document DanDanPlay attribution and enforce a user-initiated, bounded request pattern.

### 7. DFM overlay below player controls

Host a single transparent DFM `DanmakuView` via `AndroidView` in the `EasyPlayerScaffoldBase` foreground, below Compose controls and above the video `TextureView`. A controller adapter translates normalized comments into DFM items and synchronizes prepare/start/pause/seek/hide/release with ExoPlayer. The renderer is created/released with the V2 page lifecycle and is disabled for external-player playback.

Using a DFM `SurfaceView` or `TextureView` is rejected because the app already has a texture-backed video surface and a normal transparent View keeps z-order and touch behavior predictable.

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
- What exact confidence threshold and metadata precedence should allow an automatic binding without confirmation?
- Should the V2/legacy selector be developer-only, an ordinary player setting, or a remotely controlled rollout flag during the migration period?
