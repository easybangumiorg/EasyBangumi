## 1. Foundation and integration validation

- [x] 1.1 Audit the existing playback route, state ownership, and legacy page boundaries; define the V2 route and legacy fallback selection mechanism.
- [x] 1.2 Add DanmakuFlameMaster through a dependency configuration compatible with the current Android/Gradle toolchain and verify a clean build plus supported-ABI packaging.
- [ ] 1.3 Obtain and configure approved DanDanPlay application credentials outside committed source, document attribution, and verify authenticated search/comment requests within quota expectations.
- [ ] 1.4 Define normalized danmaku models, source metadata, result/error states, cancellation ownership, cache expiry policy, and persistent per-episode binding schema.

## 2. Built-in danmaku source domain

- [ ] 2.1 Implement the built-in `DanmakuSource` contract and `InnerDanmakuSourceRegistry` with no external registration path.
- [ ] 2.2 Implement the DanDanPlay source adapter for title/metadata resolution, anime search, remote episode listing, comment retrieval, and returned provenance mapping.
- [ ] 2.3 Add persistent source enable/default preferences and binding/cache storage with migrations where required.
- [ ] 2.4 Implement bounded, cancellable retrieval with cache reads/writes and stale playback-session result protection.
- [ ] 2.5 Add a built-in danmaku-source management screen that exposes DanDanPlay metadata, enable/default controls, and non-removable status.
- [ ] 2.6 Add domain tests for source registry restrictions, binding reuse, automatic-match ambiguity, cache expiry, and stale-result handling.

## 3. Danmaku matching experience

- [ ] 3.1 Add V2 playback danmaku state orchestration that reuses bindings, performs high-confidence automatic matching, and exposes retryable unavailable states.
- [ ] 3.2 Build the peer-level `弹幕` section for disabled, loading, matched, empty, unavailable, and failed states.
- [ ] 3.3 Implement the stateful manual matching sheet: editable title query, anime candidate selection, remote episode selection, binding persistence, and comment load.
- [ ] 3.4 Add playback-facing tests for binding reuse, ambiguous match confirmation, manual selection, errors, and episode changes during loading.

## 4. Danmaku rendering and settings

- [ ] 4.1 Create a DFM renderer adapter that converts normalized comments to render items and owns prepare, clear, seek, visibility, and release operations.
- [ ] 4.2 Mount a transparent non-intercepting DFM overlay in the V2 player foreground below Compose controls and above the video texture.
- [ ] 4.3 Synchronize renderer state with ExoPlayer start, pause, seek, episode replacement, page disposal, and external-player playback.
- [ ] 4.4 Add persisted user controls for enablement, category/provenance filtering, and time offset; apply changes without reopening the page.
- [ ] 4.5 Verify renderer behavior on target devices for TextureView z-order, controls touchability, seek correctness, resource release, and large comment sets.

## 5. V2 playback-detail page

- [ ] 5.1 Create the isolated V2 playback-detail composition using existing playback and cartoon view models without modifying the legacy page.
- [ ] 5.2 Implement the media identity area with a two-line clamped synopsis and expand/collapse behavior.
- [ ] 5.3 Preserve follow, search, website, download, and external-playback actions in an evenly spaced action row with one divider below the group.
- [ ] 5.4 Implement the `播放源` section with horizontally scrollable source chips and existing playing-source indication.
- [ ] 5.5 Implement the `选集` section with wider short horizontal episode buttons, current-episode state, episode-level sort action, and all-episodes entry point.
- [ ] 5.6 Implement `EpisodePickerBottomSheet` with source switching, search, sorting, current-episode focus, grid selection, and dismissal on selection.
- [ ] 5.7 Reuse existing sort/display persistence across the quick rail and picker; retain the legacy sort/display sheet behavior where it remains available.
- [ ] 5.8 Add Compose UI tests for synopsis expansion, action availability, source/episode selection, picker flow, sorting consistency, and legacy fallback navigation.

## 6. Verification and rollout

- [ ] 6.1 Run unit, Compose UI, and relevant instrumentation tests; add regressions for V2/legacy shared playback state.
- [ ] 6.2 Manually verify the approved mobile layout at common phone widths, including long titles, long episode labels, many routes, and long episode lists.
- [ ] 6.3 Manually verify automatic/manual DanDanPlay matching, error states, caching, display settings, DFM timing, and external-player behavior.
- [ ] 6.4 Enable V2 as the default route only after verification, retain the documented legacy fallback, and record release/rollback validation results.
