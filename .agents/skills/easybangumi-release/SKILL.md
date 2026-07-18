---
name: easybangumi-release
description: Prepare and publish an EasyBangumi Android release. Use when updating or verifying EasyBangumi versionCode/versionName, release notes, application or Room migrations, root-level inner_source packaging, Git commit/tag/push, or GitHub CI release triggers.
---

# EasyBangumi Release

Run this workflow from the repository root. Do not create a commit, tag, or push until every required confirmation has been received. Preserve unrelated worktree changes; stage explicit release files only.

## Preflight

1. Run `scripts/release_preflight.py .` to collect the version, release-note status, migration changes, Room declarations, schema changes, and `inner_source` placement.
2. Treat the report as evidence, not approval. Resolve failures before asking for confirmation.
3. Use the latest reachable SemVer tag other than the target version as the comparison baseline. Include staged and unstaged working-tree changes in the review.

## Required confirmations

Ask separately and wait for an explicit user confirmation after presenting each result.

1. **Version:** read `buildSrc/src/main/java/com/heyanle/buildsrc/Android.kt`. Require positive `versionCode`, nonblank `versionName`, and a versionCode greater than the baseline release's value. State both values.
2. **Release notes:** inspect the first nonblank entry in `app/src/main/assets/update_log.txt`. Require it to contain today's `YYYY/MM/DD` date and the exact `versionName`. If it is absent or incomplete, state that fact and require an explicit confirmation to proceed without it.
3. **Application data migration:** review the working-tree diff against the baseline for `app/src/main/java/com/heyanle/easybangumi4/Migrate.kt`. List changed `lastVersionCode < N` branches and their affected storage. If no branch changed, state that no new application-data migration was found. Ask the user to confirm the coverage decision.
4. **Room migration:** review every current `@Database`, its version, entities, auto migrations, and the matching `Migrate.*DB.getDBMigration()` registration. Build the relevant KSP task so `app/schemas` is current, then inspect schema changes against the baseline. If a schema changed, require a version increase and an unbroken migration path from the baseline version (explicit `Migration(from,to)` or `AutoMigration`). Require a human review of SQL/data preservation and any destructive fallback. Ask the user to confirm the decision even if no schema changed.
5. **Built-in sources:** require `inner_source/` at the repository root and no source files under `app/src/main/assets/inner_source/`. Ensure `app/build.gradle.kts` registers the root directory directly as a main assets source; do not add a generated-assets task or task dependency. Move duplicate source files to the root only after checking content equality; do not overwrite conflicting files without user direction.

## Validation

Run `./gradlew :app:mergeDebugAssets :app:testDebugUnitTest --tests com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest`. If Room code or schemas changed, also run the applicable Room migration tests; add or update an instrumentation migration test when no suitable coverage exists. Report failures and stop before release actions.

## Publish

After all confirmations and validation succeed:

1. Recheck `git status`, `git diff --check`, the exact staged file list, and that the target tag does not already exist.
2. Stage only the reviewed release files with explicit paths; do not use `git add -A` when unrelated changes exist.
3. Commit as `release: <versionName>`, create an annotated tag named `<versionName>`, and push the selected branch and tag to `origin`.
4. Confirm the pushed commit/tag names and explain that the tag push triggers GitHub CI. If push is rejected, leave local commit and tag intact and report the remote error.
