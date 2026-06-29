## Why

EasyBangumi Next is a pure anime-watching project, but the current Gradle project graph is much more complex than the product boundary. The `shared` and `lib` roots already act as aggregation modules, yet most code still lives in many small submodules and is re-exported through `api(...)`. This makes dependency direction harder to reason about, increases build configuration surface, and makes routine feature work pay an architectural tax.

This change consolidates the module structure around the actual runtime architecture:

- `app`: thin platform shells
- `shared`: business module and business aggregation point
- `base`: low-level reusable foundation module
- special modules: `webview`, `libplayer`, and `logger`

## What Changes

- Consolidate all current `shared` submodules into the root `shared` module.
- Preserve existing Kotlin package names when a moved source set already has a package.
- Add package declarations only for files that currently lack a package.
- Consolidate current non-webview `lib` submodules into a renamed `base` module.
- Move `webview` out of the `lib` tree and keep it as a special browser module.
- Keep `libplayer` and `logger` as special modules because their responsibilities are narrow and may later become standalone libraries.
- Update Gradle settings, build scripts, project accessors, source-set configuration, KSP/Room configuration, and resource configuration to match the new module graph.
- Reduce direct dependencies on implementation-specific platform modules where an API boundary is more appropriate.

## Capabilities

### New Capabilities

- `module-architecture`: Defines and enforces the simplified module layout for app, shared, base, and special modules.

### Modified Capabilities

- Existing application features should keep behavior unchanged while their source ownership moves into fewer Gradle modules.

## Impact

- Gradle includes will change in `settings.gradle.kts`.
- Build scripts under `shared/**`, `lib/**`, and app modules will be merged or updated.
- Source files under `shared/*/src/**` will move under `shared/src/**`.
- Source files under `lib/utils`, `lib/store`, `lib/unifile`, and `lib/serialization` will move under `base/src/**`.
- WebView source files will move from `lib/webview/**` to `webview/**`.
- Project references such as `projects.lib.*`, `projects.shared.*`, and `projects.lib.webview*` will be updated to the new accessors.
- Room schema and moko-resources configuration must continue to work after consolidation.
- No user-facing behavior should change.
