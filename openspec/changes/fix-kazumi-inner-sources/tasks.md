## 1. Package built-in sources

- [x] 1.1 Add Android build asset processing that copies repository-level `inner_source` into the generated `inner_source/` asset path.
- [x] 1.2 Ensure clean builds remove generated stale source assets and preserve nested relative paths.
- [x] 1.3 Add a packaged-asset manifest test comparing repository and APK/asset-manager JS source paths.
- [x] 1.4 Extend source installation tests to prove active files load while `block-` files are copied but excluded.

## 2. Validate enabled Kazumi playlist rules

- [x] 2.1 Define stable representative titles and expected source-page playlist containers for every enabled Kazumi source.
- [x] 2.2 Run connected-device/live checks that record line boundaries, episode order, and source URLs for each enabled source.
- [x] 2.3 Replace stale positional playlist XPath expressions with verified source-specific selectors; rename any unstable source to `block-`.
- [x] 2.4 Add regression assertions that reject duplicate, unrelated, or reordered episodes within validated play lines.

## 3. Validate playback address resolution

- [x] 3.1 Classify each enabled source's player page as direct media, encoded player data, iframe-based playback, or renderer-only.
- [x] 3.2 Correct direct-play shortcuts so they return only verified MP4/HLS URLs and retain renderer fallback for encoded or iframe flows.
- [x] 3.3 Extend live-rule diagnostics with source key, detail URL, line, episode URL, final URL, media type, and failure reason.
- [x] 3.4 Validate returned media URLs with the same source headers and mark non-verifiable sources `block-`.

## 4. Verify release readiness

- [x] 4.1 Run source syntax, metadata, asset-manifest, and source-controller test suites.
- [x] 4.2 Build and install a debug APK, then verify the runtime source cache and auto-installed source set on the connected device.
- [x] 4.3 Run the full enabled-Kazumi playback validation suite and review its generated report before release.
