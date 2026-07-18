## Why

The current playback detail page mixes playback routes and an unbounded episode grid, making long series difficult to browse and leaving no coherent place to match or understand danmaku. The product needs a new, visually cohesive playback experience and a reliable DanDanPlay workflow while preserving the existing playback page as a rollback path.

## What Changes

- Add a new playback-detail page and navigation path; keep the existing playback page intact and selectable for rollback during the migration.
- Redesign the playback detail hierarchy around the existing video, collapsed anime synopsis, preserved action row (follow, search, website, download, external playback), and a grouped playback-source and horizontal episode experience.
- Add an all-episodes bottom sheet with route selection, search, sorting, and current-episode focus; keep the existing sort/display controls as episode-level controls.
- Add a danmaku section that reports matching and load state, supports automatic matching, and provides a manual DanDanPlay title-search → anime selection → episode selection flow.
- Introduce a built-in-only danmaku source abstraction and management surface, initially registering DanDanPlay and disallowing external source installation.
- Integrate DanmakuFlameMaster as the in-player renderer and synchronize it with ExoPlayer playback, seek, lifecycle, and user visibility settings.

## Capabilities

### New Capabilities

- `playback-detail-redesign`: New isolated playback-detail page with grouped route/episode interaction, synopsis expansion, preserved actions, and an all-episodes bottom sheet.
- `danmaku-matching`: Automatic and manual DanDanPlay matching, per-episode bindings, match-status UI, caching, and safe failure behavior.
- `built-in-danmaku-sources`: Built-in danmaku-source abstraction, DanDanPlay registration, persisted enable/default state, and source-management UI without external installation.
- `danmaku-rendering`: DanmakuFlameMaster overlay rendering, playback synchronization, display controls, and resource lifecycle handling.

### Modified Capabilities

<!-- None. No repository-level capability specification currently defines the existing playback-detail behavior. -->

## Impact

- Affected UI: `ui/cartoon_play`, navigation, playback view models, settings, and new source-management surfaces.
- Affected data: persistent danmaku source settings, per-video-episode DanDanPlay bindings, and cache entries.
- New dependency: DanmakuFlameMaster, subject to Android/Gradle compatibility verification before integration.
- New network integration: DanDanPlay Open Danmaku Network API, including application credentials, request limits, caching, and attribution/usage requirements.
