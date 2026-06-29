## Context

The current repository already has root aggregation modules:

- `shared` exports many `shared:*` submodules and also depends on `lib`, `webview`, and `libplayer`.
- `lib` exports `lib:serialization`, `lib:unifile`, `lib:utils`, `lib:store`, and `lib:webview`.

This creates a wide Gradle project graph without giving the codebase a matching architectural benefit. Most of the submodules are not truly independently deployable libraries; they are business or foundation slices of the same product. The desired model is a smaller graph with clearer responsibility boundaries:

```text
app
  -> shared

shared
  -> base
  -> webview API
  -> libplayer API
  -> logger
  -> javascript runtime modules as needed

base
  -> logger

webview
  -> base
  -> logger

libplayer
  -> logger

logger
```

## Goals / Non-Goals

**Goals:**

- Make `app` a thin Android/Desktop shell.
- Make `shared` the single business module containing all former `shared` submodule source.
- Rename and consolidate non-webview `lib` functionality into `base`.
- Move `webview` out of `lib` into a special browser module.
- Keep `libplayer` and `logger` as special modules.
- Keep existing package declarations stable wherever possible.
- Preserve behavior and public APIs during the structural move.
- Keep multiplatform source-set behavior, Room/KSP schema generation, moko-resources generation, and platform actual/expect wiring working.

**Non-Goals:**

- Do not rewrite business logic.
- Do not redesign UI or playback behavior.
- Do not extract `webview`, `libplayer`, or `logger` into separate repositories yet.
- Do not perform broad package renames beyond adding missing package declarations where required.
- Do not remove JavaScript runtime modules unless a separate decision is made.

## Decisions

### Decision 1: Treat `shared` as the business module, not an aggregator

**Choice:** Move former `shared:*` source into `shared/src/**` and remove those Gradle subprojects.

**Rationale:**

- The existing root `shared` module already exports the business surface.
- Feature work benefits more from one coherent business module than many small Gradle boundaries.
- Kotlin packages already encode most internal organization.

**Alternatives:**

- Keep submodules and only clean dependencies. This would leave most complexity intact.
- Create more business modules by feature. This may be useful later, but it is not the current goal.

### Decision 2: Rename `lib` to `base` and exclude webview

**Choice:** Consolidate `lib`, `lib:utils`, `lib:store`, `lib:unifile`, and `lib:serialization` into `base`.

**Rationale:**

- These modules are low-level capabilities used across the product.
- The word `base` better communicates bottom-layer ownership than `lib`.
- Combining them reduces source-set and dependency duplication.

**Alternatives:**

- Keep each utility area as its own module. This preserves theoretical separability but keeps the current build graph complexity.

### Decision 3: Move webview into a special module

**Choice:** Move `lib:webview`, `lib:webview_api`, `lib:webview_jcef`, and `lib:webview_webkit` into a top-level `webview` module family.

**Rationale:**

- Browser integrations are platform-specific and narrow in responsibility.
- WebView may later become an independent library.
- Keeping it outside `base` prevents the bottom layer from pulling browser concerns.

**Important cleanup:**

- `webview` API should not depend on `shared:data` if avoidable. Business data coupling should live in `shared` adapters.
- `shared:ktor` style cookie/browser integrations should depend on webview API or injected abstractions rather than concrete webkit/jcef implementations where feasible.

### Decision 4: Keep package names stable during physical moves

**Choice:** Preserve existing `package` declarations when moving files.

**Rationale:**

- Keeps import churn low.
- Avoids mixing structural module work with semantic namespace migration.
- Reduces the chance of behavior regressions.

**Exception:**

- Files without a package declaration should receive a package that matches their new logical location.

### Decision 5: Separate structural consolidation from dependency cleanup

**Choice:** Perform the move in phases and keep each phase buildable.

**Rationale:**

- Room/KSP, moko-resources, Compose multiplatform, and platform-specific source sets all have configuration details that are easy to break in a large move.
- A phased migration makes compile errors easier to attribute.

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Room/KSP configuration is lost when `shared:data` is merged | Database code may fail to compile or schemas may stop generating | Move KSP plugin, compiler dependencies, and `room.schemaLocation` into `shared` |
| moko-resources configuration is lost when `shared:resources` is merged | Generated `Res` accessors may break | Move `mokoResources` plugin and `multiplatformResources` block into `shared` |
| Source-set collisions after moving files | Duplicate declarations or expect/actual mismatches | Move one module group at a time and compile after each group |
| WebView depends on business data | New special module boundary becomes leaky | Move business-specific webview adapters into `shared`; keep `webview` API generic |
| Player implementation leaks into shared business code | `shared` stays tied to platform implementations | Prefer `libplayer` API in common code and wire platform implementations from app/platform source sets |
| Typesafe project accessors change | Build scripts fail until all references are updated | Update `settings.gradle.kts` and all `projects.*` references in one controlled pass |

## Migration Plan

1. Prepare the new module graph in `settings.gradle.kts`.
2. Create or convert `base` from current `lib`.
3. Move non-webview `lib` source sets into `base`.
4. Merge `lib` dependency declarations into `base/build.gradle.kts`.
5. Move `webview` modules out of `lib` and update their project accessors.
6. Update app and shared references from `projects.lib` to `projects.base`.
7. Move lower-risk `shared` modules into `shared`: `platform`, `preference`, `local`, `scheme`, `theme`, and `foundation`.
8. Move configuration-sensitive `shared` modules into `shared`: `resources`, `data`, `source`, `source/api`, `source/bangumi`, `source/inner`, `source/local`, `ktor`, and `playcon`.
9. Merge Gradle plugins and dependencies needed by the moved code into `shared/build.gradle.kts`.
10. Remove obsolete module includes and empty build scripts after compilation succeeds.
11. Run Android and Desktop compilation/build verification.

## Open Questions

- Should the top-level `javascript` modules remain as special modules, or should they be considered part of `base` in a later change?
- Should `webview` be a single multiplatform module with platform source sets, or keep `webview:api`, `webview:jcef`, and `webview:webkit` as a small module family for now?
- Should package cleanup for legacy namespaces such as `com.heyanle.easy_bangumi_cm.common.theme.colors` be deferred to a separate change?
