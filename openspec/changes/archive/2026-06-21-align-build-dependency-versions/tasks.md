## 1. Preparation

- [x] 1.1 Confirm the current active versions in `gradle/builds.version.toml`, `gradle/libs.version.toml`, and `gradle/wrapper/gradle-wrapper.properties`.
- [x] 1.2 Confirm where Android Material Components is referenced and whether it is actively used.
- [x] 1.3 Confirm no active OpenSpec implementation work depends on changing Kotlin/Compose major versions.

## 2. Kotlin and Compose Alignment

- [x] 2.1 Align `libs.version.toml` Kotlin version to `2.1.21`.
- [x] 2.2 Align `kotlin-reflect` to `2.1.21`.
- [x] 2.3 Align `jetbrains_kotlin_jvm` to `2.1.21`.
- [x] 2.4 Align the Kotlin serialization plugin version to the Kotlin `2.1.21` line.
- [x] 2.5 Align the JetBrains Compose Gradle plugin version in `builds.version.toml` to `1.10.3`.

## 3. Material and Gradle Patch Updates

- [x] 3.1 Keep Compose Material3 at `1.9.0` and document that it is already the latest stable version.
- [x] 3.2 Keep Compose Material Icons Extended at `1.7.3` and document that it is already the latest available stable version.
- [x] 3.3 Decide whether to update Android Material Components from `1.12.0` to `1.14.0` based on usage and compile risk.
- [x] 3.4 Skip Android Material Components update because the current main Gradle project does not directly use the catalog alias.
- [x] 3.5 Update the Gradle wrapper from `8.14.2` to `8.14.5`.

## 4. Verification

- [x] 4.1 Run a Gradle wrapper/version or help task to verify Gradle starts.
- [x] 4.2 Run Android compilation or the closest available Android build verification.
- [x] 4.3 Run Desktop compilation or the closest available Desktop build verification.
- [x] 4.4 Review dependency/catalog diffs to ensure no large migration versions were introduced.
- [x] 4.5 Record any skipped optional upgrades and why.
