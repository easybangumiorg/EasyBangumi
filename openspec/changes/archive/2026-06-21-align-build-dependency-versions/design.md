## Context

The repository has two Gradle version catalogs:

- `gradle/builds.version.toml` for build plugins and included build dependencies.
- `gradle/libs.version.toml` for application/library dependencies and some plugin aliases.

Current local findings:

| Area | Current State |
|------|---------------|
| Gradle wrapper | `8.14.2` |
| Android Gradle Plugin | `8.9.0` |
| Build Kotlin plugins | `2.1.21` |
| Library Kotlin version | `2.1.20` |
| `kotlin-reflect` | `2.1.10` |
| Kotlin serialization plugin | `1.9.24` |
| Build Compose plugin | `1.10.1` |
| Compose libraries | `1.10.3` |
| Compose Material3 | `1.9.0` |
| Compose Material | `1.10.3` |
| Compose Material Icons Extended | `1.7.3` |
| Android Material Components | `1.12.0` |

Latest-version exploration showed newer major/minor lines exist, but they should be handled as a dedicated migration because they may require coordinated Kotlin, Compose compiler, AGP, Gradle, KSP, and Android toolchain changes.

## Goals / Non-Goals

**Goals:**

- Make Kotlin versions internally consistent on `2.1.21`.
- Make Compose plugin/library versions internally consistent on `1.10.3`.
- Keep Material3 and Material Icons decisions explicit rather than blindly changing every Material artifact.
- Optionally update Android Material Components to a stable version if it is directly used and does not introduce build issues.
- Optionally update Gradle wrapper to the latest Gradle 8 patch release.
- Verify Android and Desktop compilation after changes.

**Non-Goals:**

- Do not migrate to Kotlin `2.4.x`.
- Do not migrate to Compose Multiplatform `1.11.x`.
- Do not migrate to AGP `9.x`.
- Do not migrate to Gradle `9.x`.
- Do not refactor source code or module boundaries.
- Do not replace the version catalog structure.

## Version Strategy

Use a two-step upgrade philosophy:

```text
Current change:
  align within existing stable lines
  patch-level updates only
  compile verification

Future change:
  Kotlin / Compose / AGP / Gradle major-minor migration
  compatibility matrix review
  broader regression testing
```

Recommended first-pass edits:

| File | Key | Target |
|------|-----|--------|
| `gradle/libs.version.toml` | `kotlin` | `2.1.21` |
| `gradle/libs.version.toml` | `kotlin-reflect` | `2.1.21` |
| `gradle/libs.version.toml` | `jetbrains_kotlin_jvm` | `2.1.21` |
| `gradle/libs.version.toml` | `kotlin-serialization-plugin` | `2.1.21` |
| `gradle/builds.version.toml` | `compose` | `1.10.3` |

Conditional edits:

| File | Key | Target | Condition |
|------|-----|--------|-----------|
| `gradle/libs.version.toml` | `androidx-material` | `1.14.0` | Apply if Android Material Components is still directly used and compilation remains clean |
| `gradle/wrapper/gradle-wrapper.properties` | distribution URL | `8.14.5` | Apply if wrapper update can be performed safely |

Do not change:

| Key | Reason |
|-----|--------|
| `compose-material3 = "1.9.0"` | Already latest stable for the current JetBrains Compose Material3 artifact |
| `compose-material-icons-extended = "1.7.3"` | Already latest available stable |
| `agp = "8.9.0"` | AGP migration should be separate |
| `ksp = "2.1.21-2.0.2"` | Already aligned with Kotlin `2.1.21` |

## Material Version Notes

Material-related dependencies are not all released on the same version line:

- JetBrains Compose Material3 is currently stable at `1.9.0`.
- JetBrains Compose Material follows the Compose runtime version line and has newer `1.11.x` releases.
- JetBrains Compose Material Icons Extended currently remains at `1.7.3`.
- Google Android Material Components is independent from Compose Multiplatform and can be updated separately if used.

Because of this, the change should not force all Material artifacts to the same numeric version.

## Verification

Minimum verification:

- Run a Gradle help or dependency resolution task after catalog edits.
- Compile Android source sets or run the closest available Android build task.
- Compile Desktop source sets or run the closest available Desktop build task.

If Gradle wrapper is updated, verify the wrapper can start and report the expected Gradle version.

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Serialization plugin alignment changes generated code behavior | Build or runtime serialization issues | Keep plugin on same Kotlin line and run compilation |
| Android Material Components `1.14.0` changes transitive dependencies | Android build or UI behavior changes | Treat as conditional and revert/skip if compile issues appear |
| Gradle wrapper patch update changes daemon behavior | Build environment differences | Keep within Gradle 8 line and verify wrapper startup |
| Larger upgrade pressure grows during implementation | Scope creep | Defer Kotlin 2.4 / Compose 1.11 / AGP 9 / Gradle 9 to a separate proposal |

## Open Questions

- Is Android Material Components used by active Android UI code, or is the app effectively Compose-only now?
- Should Gradle wrapper `8.14.5` be included in the first implementation, or kept as a separate build-tooling task?
- Should future major/minor upgrades be split into one combined migration or separate Kotlin/Compose and AGP/Gradle changes?

