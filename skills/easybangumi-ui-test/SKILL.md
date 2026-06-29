---
name: easybangumi-ui-test
description: Use when testing Pure Pure Bangumi (EasyBangumi) Android features through real adb-driven UI flows, including app launch, app data reset, local mock HTTP servers on Mac, device-to-host port mapping, UI hierarchy dumps, reusable page navigation, and evidence collection for manual or scripted feature verification.
---

# Easybangumi Ui Test

## Overview

This skill standardizes real-device testing for Pure Pure Bangumi with `adb`, local mock services, and repeatable UI evidence collection. Use it when the task is broader than one page or when you need reusable setup, navigation, and validation steps.

## Quick Start

1. Run the common helper script from the repo root:
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh`
2. If you need a local mock server, start it with:
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh serve /path/to/mock-dir 18080`
3. If the Android device must access a Mac-local service through `127.0.0.1`, use:
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh reverse 18080`
4. Launch and inspect the app:
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh clear com.heyanle.easybangumi4.debug`
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh launch com.heyanle.easybangumi4.debug`
   `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh dump-ui`

## Workflow

### 1. Preflight

- Confirm exactly which package to test: usually `com.heyanle.easybangumi4.debug`.
- Prefer the current workspace build when validating current code.
- Decide whether the test needs clean state:
  `clear` if onboarding, config, or repository state matters.
- Decide whether the test needs a mock service:
  use `reverse`, not `forward`, when the app on the device should reach a Mac-local service via `127.0.0.1`.

### 2. Execute the Real Flow

- Prefer explicit `am`/`monkey` launch over tapping a launcher icon when multiple app variants exist.
- Use `dump-ui` before important actions and after each state change that matters.
- Drive the UI with `tap`, `text`, and `swipe`.
- If onboarding or permissions interrupt the target flow, either complete them manually or document the exact blocking screen before continuing.

### 3. Validate Outcomes

- Validate both device-side behavior and host-side evidence.
- For network-backed flows, check the local mock server logs for requested paths.
- For install/import flows, verify both the final dialog/toast state and the downstream page state after dismissing dialogs.
- Record unexpected behavior as:
  trigger, observed result, expected result, likely code area.

### 4. Clean Up

- Stop any local mock server started for the test.
- Remove temporary `adb reverse` mappings with `unreverse`.
- Keep test artifacts only if they are useful for follow-up debugging.

## Navigation

Read [references/navigation.md](references/navigation.md) when the task depends on page entry paths, common bottom-tab routes, or source-management navigation.

## Test Strategy

Read [references/test-strategy.md](references/test-strategy.md) when you need a reusable checklist for scenario design, boundary analysis, or result reporting.

## Script

Use `scripts/easybangumi_test.sh` for common setup and adb actions. Prefer the script over retyping raw commands when the action is generic and repeatable.

Supported subcommands:

- `serve <dir> [port]`
- `stop-serve [port]`
- `reverse <port>`
- `unreverse <port>`
- `clear <package>`
- `launch <package>`
- `dump-ui [output-file]`
- `tap <x> <y>`
- `text <value>`
- `swipe <x1> <y1> <x2> <y2> [duration-ms]`
- `screenshot [output-file]`
- `current-focus`

## When To Escalate

- If multiple installed packages share the same app name and launcher icon.
- If the flow requires manual login, device-level permissions, or document picker interaction that cannot be inferred safely.
- If observed UI text conflicts with expected code behavior and you need to decide whether to trust code or runtime.
