# Test Strategy

## Scenario Design

Always include:

- One clean happy path.
- One repeated-action path, such as duplicate add or repeated refresh.
- One invalid-input path.
- One network or content failure path.
- One runtime compatibility path if the feature imports or loads external content.

## Evidence Checklist

Capture at least one of:

- `uiautomator dump` before the key action.
- `uiautomator dump` after the key action.
- Mock server request log.
- Screenshot for user-visible success or failure dialog.

## Result Reporting Format

For each case, record:

- Precondition
- Steps
- Expected result
- Actual result
- Code area likely involved

## Common EasyBangumi Pitfalls

- The device usually needs `adb reverse`, not `adb forward`, to reach a Mac-local mock service.
- Installing a downloaded script can fail because the content is incompatible even when HTTP succeeded.
- A server-side 404 may surface as downstream parse failure if the error page body is saved and parsed as content.
