## Why

The project currently mixes several Kotlin and Compose version lines across the Gradle catalogs. The build plugins use Kotlin `2.1.21`, while `libs.version.toml` still references Kotlin `2.1.20`, `kotlin-reflect` `2.1.10`, and the Kotlin serialization plugin `1.9.24`. Compose is also split between the build plugin catalog at `1.10.1` and the library catalog at `1.10.3`.

This makes dependency resolution harder to reason about and increases the risk of subtle compiler, serialization, and Compose compatibility issues. Recent version exploration also showed that there are larger upgrades available, but jumping directly to Kotlin `2.4.x`, Gradle `9.x`, AGP `9.x`, and Compose `1.11.x` would combine too many moving pieces at once.

This change keeps the first upgrade pass conservative: align the current version line, apply low-risk patch updates, and document the larger upgrades as follow-up work.

## What Changes

- Align Kotlin-related versions across Gradle catalogs on the current Kotlin `2.1.21` line.
- Align Compose plugin and Compose library versions on the current `1.10.3` line.
- Keep Compose Material3 at `1.9.0`, because it is already the latest stable version for the current JetBrains Compose Material3 artifact.
- Keep `material-icons-extended` at `1.7.3`, because it is already the latest available stable version.
- Consider updating Android Material Components from `1.12.0` to the latest stable `1.14.0` only if existing Android modules use it directly and compilation remains clean.
- Consider updating the Gradle wrapper from `8.14.2` to `8.14.5` as a patch-level Gradle 8 update.
- Do not upgrade to Kotlin `2.4.x`, Compose `1.11.x`, AGP `9.x`, or Gradle `9.x` in this change.

## Capabilities

### New Capabilities

- `build-dependency-version-alignment`: Keeps build plugin and library catalog versions aligned within the selected stable version line.

### Modified Capabilities

- Build configuration uses consistent Kotlin, Compose, and Material-related versions.
- Dependency maintenance has a documented boundary between safe alignment and larger platform migrations.

## Impact

- Updates Gradle version catalog files under `gradle/`.
- May update the Gradle wrapper patch version if the wrapper task can run successfully.
- Does not change application behavior.
- Does not change source package structure or module architecture.
- Verification should focus on Gradle sync, Kotlin compilation, and representative Android/Desktop build tasks.

