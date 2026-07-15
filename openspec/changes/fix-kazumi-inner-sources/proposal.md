## Why

The built-in Kazumi source files were moved to the repository-level `inner_source` directory, while runtime installation still reads `app/src/main/assets/inner_source`; new APKs therefore do not package or auto-install them. The nine enabled Kazumi rules also rely on stale, positional playlist XPath expressions and a generic playback shortcut, which can surface shuffled episodes or invalid media URLs when source sites change structure or encode their player data.

## What Changes

- Package the repository-level `inner_source` files into the Android assets consumed by `InnerSourceFileProvider`, and verify the packaged set during tests.
- Preserve the existing `block-` convention so only non-blocked source files are auto-installed.
- Revalidate the enabled Kazumi sources against their live detail and play pages; correct playlist extraction and playback-address handling where the current rules no longer match the source site.
- Add focused regression coverage for installed-source selection, playlist structure, and resolved media-address validity.

## Capabilities

### New Capabilities

- `built-in-source-packaging`: Package and validate repository-managed built-in source files for automatic runtime installation.
- `kazumi-playback-rule-validation`: Validate enabled Kazumi rules against real playlist and media-address behavior before they are automatically enabled.

### Modified Capabilities

- None.

## Impact

- Android asset/build configuration and source-installation integration tests.
- `inner_source` Kazumi JS rules, especially enabled `kazumi-*` files.
- Source loading through `InnerSourceFileProvider` and playback through `RenderHelperImpl`.
- No public API or third-party dependency change is expected.
