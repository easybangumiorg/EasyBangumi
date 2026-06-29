## 1. Preparation

- [x] 1.1 Confirm the target module list in `settings.gradle.kts`: `app`, `shared`, `base`, `webview`, `libplayer`, `logger`, and any retained runtime modules.
- [x] 1.2 Capture current project references with `rg "projects\\." -g "build.gradle.kts"`.
- [x] 1.3 Establish a compile checkpoint command for Android and Desktop.

## 2. Consolidate `lib` into `base`

- [x] 2.1 Rename or replace the root `lib` module with `base`.
- [x] 2.2 Move `lib/utils` source sets into `base`.
- [x] 2.3 Move `lib/store` source sets into `base`.
- [x] 2.4 Move `lib/unifile` source sets into `base`.
- [x] 2.5 Move `lib/serialization` source sets into `base`.
- [x] 2.6 Merge required plugins and dependencies into `base/build.gradle.kts`.
- [x] 2.7 Update references from `projects.lib`, `projects.lib.utils`, `projects.lib.store`, `projects.lib.unifile`, and `projects.lib.serialization` to `projects.base`.
- [x] 2.8 Compile after the base migration checkpoint.

## 3. Move `webview` out of `lib`

- [x] 3.1 Create the top-level `webview` module structure.
- [x] 3.2 Move `lib/webview/api` to the new webview API location.
- [x] 3.3 Move `lib/webview/jcef` to the new desktop webview implementation location.
- [x] 3.4 Move `lib/webview/webkit` to the new Android webview implementation location.
- [x] 3.5 Update project accessors from `projects.lib.webview*` to the new `projects.webview*` accessors.
- [x] 3.6 Remove or relocate business-data dependencies from the webview API boundary where feasible.
- [x] 3.7 Compile after the webview migration checkpoint.

## 4. Consolidate `shared` submodules

- [x] 4.1 Move `shared/platform` source sets into `shared`.
- [x] 4.2 Move `shared/preference` source sets into `shared`.
- [x] 4.3 Move `shared/local` source sets into `shared`.
- [x] 4.4 Move `shared/scheme` source sets into `shared`.
- [x] 4.5 Move `shared/theme` source sets into `shared`.
- [x] 4.6 Move `shared/foundation` source sets into `shared`.
- [x] 4.7 Move `shared/resources` source sets and moko-resources configuration into `shared`.
- [x] 4.8 Move `shared/data` source sets, Room schemas, KSP plugin configuration, and Room compiler dependencies into `shared`.
- [x] 4.9 Move `shared/source` and nested source modules into `shared`.
- [x] 4.10 Move `shared/ktor` source sets into `shared`.
- [x] 4.11 Move `shared/playcon` source sets into `shared`.
- [x] 4.12 Preserve existing package declarations and add packages only for package-less files.

## 5. Clean Dependency Boundaries

- [x] 5.1 Update app modules so they depend on `shared` and platform-specific player implementations only where needed.
- [x] 5.2 Keep common `shared` code depending on `base`, `webview` API, `libplayer` API, `logger`, and JavaScript runtime modules as needed.
- [x] 5.3 Avoid direct common-code dependencies on concrete `webview` implementations.
- [x] 5.4 Avoid direct common-code dependencies on concrete `libplayer` implementations unless platform source sets require them.
- [x] 5.5 Remove obsolete submodule includes and empty submodule build files.

## 6. Verification

- [x] 6.1 Run Android compilation or build verification.
- [x] 6.2 Run Desktop compilation or build verification.
- [x] 6.3 Verify Room schema generation still writes to the intended schema directory.
- [x] 6.4 Verify moko-resources generated accessors still resolve.
- [x] 6.5 Search for stale project accessors and stale module paths.
- [x] 6.6 Document any deferred cleanup, especially JavaScript module placement and legacy package renames.
