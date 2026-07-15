## Context

Built-in sources are maintained in the repository-level `inner_source` directory, but Android runtime source installation recursively reads the `inner_source` asset directory. The release move left the runtime contract unchanged, so the source set is absent from produced APK assets. Source loading filters files named with the `block-` prefix before resolving the highest version per key.

The nine enabled Kazumi rules are generated from site-specific XPath metadata but share detail and playback scaffolding. Live inspection of 7sefun shows that its current play lines are semantic `.vod-play-list-container` containers while the rule uses a positional XPath; its player data is `encrypt: 2` encoded and the resolved media URL appears in a player iframe. Similar site drift can cause a valid source to expose incorrect episode groups or a non-playable address.

## Goals / Non-Goals

**Goals:**

- Make the repository-level source set the single source of truth and package it deterministically as Android assets.
- Retain the `block-` filename gate and avoid auto-enabling an unverified source.
- Validate each enabled Kazumi source's live detail-page play lines and resolved media URL before accepting rule changes.
- Make failures diagnosable with test output that identifies the source, line, episode, page URL, and resolved media URL.

**Non-Goals:**

- Restore every blocked Kazumi source or guarantee availability of third-party streaming sites.
- Replace the source plugin API, change source preferences, or redesign the player.
- Bypass DRM, anti-bot controls, CAPTCHAs, or site access controls.

## Decisions

### 1. Generate the asset directory from `inner_source` during Android asset processing

The build will copy the complete repository-level directory into the generated asset location that is included in the APK, rather than duplicating version-controlled JS files under `app/src/main/assets`.

This keeps edits, unit tests, and packaged content tied to one authoritative tree. A checked-in copy is rejected because it can silently drift; changing `InnerSourceFileProvider` to read arbitrary filesystem paths is not viable in a packaged Android app.

### 2. Verify packaged and repository source manifests

Tests will compare the relative JS-path manifest in the source tree with the packaged `inner_source` asset manifest, then retain existing metadata and syntax checks. The manifest comparison covers blocked files too, while runtime installation continues to filter `block-` only after copying.

This separates packaging correctness from enablement correctness and prevents a build that passes source-only tests but omits files from its APK.

### 3. Treat live playlist structure as a source-rule contract

For each enabled Kazumi rule, validation will select a stable representative title and assert that every returned play line contains only episode links from its actual page container, in source-page order. Rules will prefer semantic containers, classes, IDs, or stable attributes over deep positional XPath. A source that cannot provide a reliable ordered playlist will be blocked instead of auto-installed.

This protects the user-facing episode picker. It is preferred over a broad parser-side deduplication/reordering heuristic because the page's own line boundaries are source-specific and must not be guessed.

### 4. Resolve and verify the final media URL, not a player-page field

Playback validation will consider a URL successful only after it is the final direct media request or an HLS manifest, with the source's required headers applied. Encoded `player_aaaa` fields and iframe URLs are intermediate representations; rules must either decode a demonstrably direct URL or delegate to `RenderHelperImpl` and validate its final result.

This retains the existing renderer as the common path and avoids baking individual player encryption algorithms into every JS source. Direct extraction remains an optimization only when the extracted value is confirmed to be a direct media URL.

## Risks / Trade-offs

- [Live sites change or become unavailable during validation] → Use a recorded diagnostic report, distinguish infrastructure failures from selector regressions, and block unstable sources rather than promoting them.
- [Build-generated assets are not visible to existing unit tests] → Add an Android/instrumentation or packaged-asset test that reads the actual asset manager output.
- [A source requires headers/cookies for a direct URL] → Validate the final media request with the same source headers and retain renderer fallback instead of returning an unverified shortcut.
- [Site pages contain duplicate links or multiple player widgets] → Assert line-local, source-order episode URLs and scope selectors to the verified playlist container.

## Migration Plan

1. Add the generated asset synchronization and manifest assertions.
2. Rebuild/install a debug APK and confirm the runtime cache contains all packaged sources while only non-`block-` files are loaded.
3. Revalidate the enabled Kazumi rules; repair or block each failing rule.
4. Run unit, instrumentation, and live-rule validation suites before release.

Rollback consists of reverting the build synchronization and rule updates, or marking an individual regression as `block-` in a follow-up release; no persisted data migration is required.

## Open Questions

- Which representative title per enabled source has the most stable multi-line playlist for regression testing?
- Should live validation run only as a manually invoked connected-device suite, or also in scheduled CI with failures reported as non-blocking site-health signals?
