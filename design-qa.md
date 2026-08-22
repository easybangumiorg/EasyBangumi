# Product Design QA — Arcana visual refresh

## Evidence

- Source visual truth: `/Users/heyanle/.codex/generated_images/019facb5-1afd-7e71-80c5-d23cf3131c7c/call_bqHEKrHoCzOsXyJBmZgzplsT.png`
- Source pixels: `853 × 1844`
- Runtime empty-state screenshot: `/private/tmp/easybangumi-product-design-audit/10-final-runtime-v2.png`
- Runtime pixels / viewport: `2340 × 1080`, Android landscape, device-native capture
- Runtime app-icon screenshot: `/private/tmp/easybangumi-product-design-audit/05-app-icon-settings.png`
- App-icon screenshot pixels / viewport: `1080 × 2340`, Android portrait, device-native capture
- Combined comparison: `/private/tmp/easybangumi-product-design-audit/brand-qa-comparison-v3.png`
- Comparison pixels: `1800 × 1400`
- CSS size / density normalization: not applicable; this is a native Android build. Source and implementation were fit without cropping into a shared comparison canvas.
- State: light Material 3 surface; empty state selected. Logo was also checked under the device's real squircle icon mask.

## Full-view comparison

The combined comparison places the selected board beside the final logo, empty, loading, and error assets, plus the device-rendered empty state and system-rendered app icon. The implementation preserves the selected warm ivory, pale lavender, butter-yellow, coral, and deep-plum direction. Character identity, staff, hair gradient, circular stitched state badges, and friendly state emotions are consistent with the source.

## Focused comparison

- Logo: face and staff gem stay identifiable at system app-icon size; the device squircle mask does not crop the focal features.
- Empty state: the selected popcorn-bucket pose, pale-lavender circular badge, cream sparkles, and stitched ring remain readable in the existing `64dp` slot.
- Loading state: `276 × 276`, 16 frames, `80ms` per frame, infinite loop, transparent corner pixels; motion is a restrained bob/turn around the selected orbit composition.
- Error state: the surprised expression, harmless magic puff, star, coral accents, and circular badge match the selected direction without introducing an alarming error symbol.
- Focused regions were required because the runtime state art is intentionally compact.

## Required fidelity surfaces

- Fonts and typography: unchanged by design. Existing app font, hierarchy, copy, wrapping, and italic state label remain intact.
- Spacing and layout rhythm: unchanged. The shared state component keeps its existing centered alignment, `64dp` image slot, `10dp` gaps, and navigation structure.
- Colors and tokens: app theme tokens are unchanged; new assets use the selected lavender/cream/yellow/coral palette and integrate cleanly with the current light pink surface.
- Image quality and asset fidelity: all four visible assets are generated raster art, not placeholders or code-drawn substitutes. PNG/GIF transparency is clean at corners; no green-screen residue or dark halo is visible in the comparison.
- Copy and content: no text is embedded in the assets. Existing localized state messages remain the source of truth.
- Icons: app icon verified in the Android system mask. Existing navigation and action icons were intentionally not changed.
- Accessibility and motion: content descriptions continue to use the existing localized state messages. The loading loop is gentle and does not add flashing; reduced-motion behavior remains the same as before this visual-only update.

## Comparison history

1. P1 — The first loading GIF export lost palette transparency and would have rendered a black square.
   - Fix: reserved a dedicated transparent palette index, restored binary alpha, reduced the loop to 16 frames, and rebuilt the APK.
   - Post-fix evidence: decoded corner pixel is `(0, 0, 0, 0)` on every checked frame; the GIF inside the APK matches the source asset SHA-256.
2. P2 — The first independent state exports omitted the pale-lavender circular badges shown in the selected board.
   - Fix: regenerated empty, loading, and error art with the circular badge, cream sparkles, and stitched edge, then rebuilt and reinstalled.
   - Post-fix evidence: `/private/tmp/easybangumi-product-design-audit/brand-qa-comparison-v3.png` and the device-rendered empty state.

## Findings

No actionable P0, P1, or P2 differences remain. The app's UX, navigation, typography, copy, and component sizing are unchanged.

## Verification

- `./gradlew :app:assembleDebug` — passed
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` — passed
- Device launch and empty-state navigation — passed
- APK asset presence and loading GIF hash/transparency/frame metadata — passed
- `git diff --check` — passed

## Follow-up polish

- P3: capture the error state during a naturally occurring request failure in a later full regression pass; the shared component wiring and final asset itself are already verified.

Result: passed

---

# Product Design QA — Player settings section tabs

## Evidence

- Source visual truth (existing product pattern): `/private/tmp/easybangumi-player-settings-audit/05-star-filter-sort.png`
- Source pixels / viewport: `1080 × 2340`, Android portrait, device-native capture at the connected device density.
- Original player-settings baseline: `/private/tmp/easybangumi-player-settings-audit/04-settings-baseline.png`
- Baseline pixels / viewport: `2340 × 1080`, Android fullscreen landscape, device-native capture.
- Rendered implementation: `/private/tmp/easybangumi-player-settings-audit/09-settings-after-theme.png`
- Implementation pixels / viewport: `1080 × 2075`, Android portrait panel capture; the height excludes system chrome while preserving the source device width and density.
- Focused comparison: `/private/tmp/easybangumi-player-settings-audit/10-reference-implementation-tabs.png`
- Focused comparison pixels: `1120 × 220`; two unscaled `560 × 220` crops placed side by side.
- Density normalization: no scaling. The reference and implementation were captured on the same Android device with the same dynamic-light Material color scheme.
- State: light dynamic theme; the first tab is selected (`筛选` in the source pattern, `弹幕` in the implementation).

## Full-view comparison

The collection filter/sort sheet and the playback settings sheet retain their own titles and content, but now share the same navigation grammar: a left-aligned scrollable tab row, text labels, a primary-color rounded underline, and a divider immediately before the selected section content. The original full-screen baseline confirms that panel size, close action, settings content, order, and playback overlay remain outside the visual change.

## Focused comparison

The focused comparison shows the source pattern and implementation at the same density without resampling. Tab spacing, minimum touch height, left alignment, indicator thickness/radius, and the divider relationship are consistent. The implementation intentionally uses primary color and semibold weight for the selected label to strengthen the active hierarchy requested by the user; the source collection tab relies mainly on the indicator.

## Required fidelity surfaces

- Fonts and typography: existing system font and Material type scale remain. Tab labels use `titleSmall`; the active tab alone receives semibold weight, with no wrapping or truncation.
- Spacing and layout rhythm: the former full-width pill has been removed. Tabs are left aligned with the same `20dp` content inset, Material tab minimum touch height, shared indicator geometry, and a full-width divider directly below.
- Colors and visual tokens: all colors come from `MaterialTheme.colorScheme`; active label and indicator use `primary`, inactive text uses `onSurfaceVariant`, and the row itself stays transparent.
- Image quality and asset fidelity: no image, video, illustration, logo, or icon asset is changed by this task. The player image remains an independent background surface.
- Copy and content: `弹幕`, `画面`, all settings labels, ordering, and content are unchanged.
- Icons: no icons were added, matching the collection filter/sort reference and avoiding a second visual vocabulary.
- Interaction and accessibility: the controls remain Material `Tab` components with selected semantics, existing test tags, 48dp-class touch targets, and explicit `已选择` / `未选择` state descriptions.
- Responsiveness: the same selector composable is used by both the fullscreen landscape side panel and compact portrait bottom sheet; only the containing panel branch changes.

## Comparison history

1. Evidence normalization — the first isolated Compose capture used the default purple Material test theme, which did not match the app's dynamic warm theme.
   - Fix: recaptured the implementation with `dynamicLightColorScheme` on the same device and compared unscaled focused crops.
   - Post-fix evidence: `/private/tmp/easybangumi-player-settings-audit/09-settings-after-theme.png` and `/private/tmp/easybangumi-player-settings-audit/10-reference-implementation-tabs.png`.
2. No actionable P0, P1, or P2 visual difference was found after normalization.

## Findings

No actionable P0, P1, or P2 findings remain. The updated control reads as section navigation rather than a two-state switch, while preserving behavior and content.

## Verification

- `./gradlew :app:compileDebugKotlin` — passed
- `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.easybangumi4.ui.cartoon_play.PlayerPlaybackSettingsUiTest` — 8 tests passed
- `./gradlew :app:installDebug` — passed on the connected MEIZU 21
- Selected/unselected tab semantics and reopen-state persistence — passed
- Device auto-rotation restored after QA capture

## Follow-up polish

- P3: recapture the final selector over live fullscreen video after the debug playback source is configured again; the shared selector itself has already been captured and verified in the dynamic theme.

final result: passed

# Product Design QA — Overview search verification

## Evidence

- Source visual truth:
  `design-audit/overview-verification/00-selected-concept.png`
- Baseline before redesign:
  `design-audit/overview-verification/01-current-stable.png`
- First implementation capture:
  `design-audit/overview-verification/02-implementation.png`
- Final implementation capture:
  `design-audit/overview-verification/03-implementation-final.png`
- Final regenerated-asset build after the live source stopped returning verification:
  `design-audit/overview-verification/06-final-build-source-resolved.png`
- Full-view side-by-side comparison:
  `design-audit/overview-verification/04-comparison-full.png`
- Focused verification-region comparison:
  `design-audit/overview-verification/05-comparison-focused.png`
- Final accessibility hierarchy:
  `/private/tmp/easybangumi-final-ready.xml`
- Device / viewport: MEIZU 21, portrait, native app content at `1080 × 2340`.
- Source pixels: `852 × 1846`, normalized to `1080 × 2340` with matching aspect ratio.
- Implementation pixels: `1080 × 2340`; no density resampling was used for the final capture.
- State: Overview search for “孤独摇滚”, warm dynamic light theme, one source blocked by
  human verification while other source results remain visible.

## Findings

No actionable P0, P1, or P2 visual findings remain.

- Fonts and typography: the implementation retains the app's Material type scale and system
  font. Source name, state and action preserve the selected concept's hierarchy without
  truncation in the captured viewport.
- Spacing and layout rhythm: the verification prompt spans the entire result pane, sits before
  result cards, and uses an `80dp` minimum content-adaptive height. The implementation is
  intentionally slightly taller than the generated concept to preserve the existing app's type
  scale and a comfortable full-row touch target.
- Colors and visual tokens: container, outline, text and action colors are all derived from
  `MaterialTheme.colorScheme`. The warm theme therefore replaces the concept's fixed lavender
  values without changing the selected visual structure.
- Image quality and asset fidelity: the new yellow-haired Arcana verification artwork is a
  `384 × 384` RGBA `drawable-nodpi` runtime asset generated from the approved character
  references. It remains readable in the `52dp` circular treatment with no visible green fringe
  or transparency halo.
- Copy and content: the implementation uses “GiriGiriLove / 需要人机校验 / 去验证”. CAPTCHA
  variants use “需要输入验证码 / 输入验证码”. The older “效验/点击跳转” wording is no longer used
  by search verification states.
- Interaction and accessibility: the entire visible row is the only click target. The final UI
  hierarchy exposes one clickable node at `[308,433][1069,653]`; the source name, state and action
  remain readable child semantics. Multiple blocked sources are rendered as independent
  full-width rows with stable source keys and source-specific retry callbacks.

## Comparison History

1. The first implementation matched the selected composition, but the semantics modifier was
   ordered outside the click action and the verification row was not exposed as the expected
   full-width clickable node in the first hierarchy capture.
2. The semantics and click modifier order was corrected without changing the visual layout.
3. The post-fix hierarchy exposes a single full-row clickable node and the final screenshot
   retains the selected compact strip, theme-derived border/action colors and Arcana artwork.
4. The runtime artwork was then regenerated through the checked-in brand script and reinstalled.
   Pixel comparison against a fresh script output passed. During the post-install recapture the
   remote source completed without a verification exception, so the resolved-source capture is
   retained separately rather than being treated as a visual regression.

## Verification

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` — passed.
- `./gradlew :app:installDebug` — passed on the connected MEIZU 21.
- Final debug package installed and launched.
- Runtime verification asset exactly matches a fresh `save_alpha` script output at `384 × 384`
  with alpha extrema `(0, 255)`.
- Whole-row click bounds and readable child semantics — passed.
- Single-source filtering and multiple-source support remain in the existing verification item
  projection; no Paging or retry state-machine changes were made.

## Follow-up Polish

- P3: add a screenshot fixture with two simultaneously blocked sources when a deterministic fake
  source is available; the current runtime data exposed one blocked source, while the multi-row
  rendering path is covered by the existing list projection and stable keys.

final result: passed

---

# Product Design QA — Player utilities, episode panel, and state art

## Evidence

- Source panel truth: `/private/tmp/easybangumi-player-settings-audit/04-settings-baseline.png`
- Source toolbar truth: `/private/tmp/control_verify.png`
- Implemented fullscreen controls and status: `/private/tmp/easybangumi-feature-audit/16-fullscreen-reenter.png`
- Implemented episode panel: `/private/tmp/easybangumi-feature-audit/17-episode-panel.png`
- Screenshot transaction states: `/private/tmp/easybangumi-feature-audit/18-capture-loading.png` and `/private/tmp/easybangumi-feature-audit/19-capture-preview.png`
- Danmaku long-press result: `/private/tmp/easybangumi-feature-audit/20-danmaku-longpress.png`
- Capture-button long-press result: `/private/tmp/easybangumi-feature-audit/21-record-longpress.png`
- MediaStore image read back from the device: `/private/tmp/easybangumi-feature-audit/22-saved-screenshot.png`
- Same-input visual comparison: `/private/tmp/easybangumi-feature-audit/24-combined-design-qa.png`
- Runtime state assets: `app/src/main/assets/loading_ryo.gif`, `app/src/main/res/drawable/empty_bocchi.png`, and `app/src/main/res/drawable/error_ikuyo.png`
- Device / viewport: MEIZU 21, Android API 16 runner target, native `2340 × 1080` landscape captures.
- State: dynamic warm light theme, DOG DAYS episode 1, fullscreen controls visible where required.

## Full-view comparison

The fullscreen episode selector now uses the same right-side surface, scrim, width constraint,
corner treatment, title row, and motion model as playback settings. Episode rows use a quiet
container hierarchy, while the current episode is expressed with `primaryContainer`, a checkmark,
and a short playback summary. The right toolbar replaces two competing capture controls with one
camera action carrying a restrained coral record affordance. The current time sits beside the
existing battery indicator without changing the player's information architecture.

The loading, empty, and error illustrations now have distinct visual signatures at runtime size:
loading retains the lavender orbital motion, empty uses a warm cream ticket/popcorn badge, and
error uses a coral starburst badge. All three preserve the Arcana chibi subject and work on both
light and dark backgrounds.

## Focused interaction comparison

- Short press capture: playback pauses, the camera action becomes a progress indicator, video and
  the enabled danmaku layer are composited without Compose controls, the PNG is saved, a compact
  side preview confirms success, and the previous playback intent is restored.
- Long press capture: opens the existing video/GIF recording screen from the same control.
- Long press danmaku: force-opens the matching sheet even from loading or unmatched states; disabled
  sources still route to the existing source-settings recovery path.
- Episode selection: remains the same player action and data flow; only its fullscreen visual shell
  and row treatment changed.

## Required fidelity surfaces

- Fonts and typography: existing Material type scale and system font are retained. Panel title,
  summary, row labels, current time, and confirmation copy remain single-line and legible.
- Spacing and layout rhythm: the side panel reuses the settings panel's `360dp` maximum width,
  navigation-bar inset, close target, separators, and row rhythm. Toolbar controls keep their
  original 40dp-class footprint.
- Colors and visual tokens: panel and selected rows use `MaterialTheme.colorScheme`; the generated
  state badges use differentiated semantic palettes without introducing a second UI palette.
- Image quality: final empty/error PNGs are `384 × 384` RGBA assets with clean transparency. The
  loading GIF remains `276 × 276`. The saved screenshot was read back through MediaStore at
  `1908 × 1080` and contains no player controls.
- Copy and content: existing episode names, recording choices, and match flow are unchanged. New
  copy is limited to panel context, capture semantics, and compact success/failure feedback.
- Icons: existing Material camera, close, check, and battery glyphs are reused. The record affordance
  is a small colored dot rather than a second full icon.
- Interaction and accessibility: combined-click semantics expose `截图，长按录制`; danmaku exposes
  the `强制匹配弹幕` long-click label. Scrim dismiss, close, back handling, loading lockout, and
  duplicate-capture prevention remain explicit.
- Responsiveness and motion: fullscreen landscape uses the shared animated side panel. Settings and
  episode surfaces fade the scrim and slide from the right; compact portrait behavior is unchanged.

## Comparison history

1. Initial generated state art contained a visible checkerboard rather than actual alpha.
   - Fix: regenerated against a controlled chroma background, removed chroma locally, inspected the
     alpha output, and resized into runtime assets without stretching.
2. The first screenshot implementation retained preview and full-frame bitmaps across cancellation.
   - Fix: made full frames transaction-scoped, recycled pending previews on every error path, and
     released displayed previews only after Compose stopped drawing them.
3. The final combined comparison found no actionable P0, P1, or P2 visual mismatch.

## Findings

No actionable P0, P1, or P2 findings remain. The new controls are less crowded, the episode panel
belongs to the same surface family as settings, and the three state illustrations are identifiable
without relying on copy.

## Verification

- `./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest` — passed
- `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest` — passed
- `PlayerPlaybackSettingsUiTest` — all 9 tests passed on the connected device
- `rendererSnapshot_drawsVisibleLayerScaledIntoTargetCanvas` — passed on the connected device
- Short screenshot, progress state, side preview, MediaStore read-back, and playback resume — passed
- True DOWN/UP long press for recording and danmaku forced matching — passed
- Device auto-rotation restored after QA

## Follow-up polish

- P3: the current DOG DAYS test stream had no matched live comments, so the final device image could
  not visually demonstrate comment glyphs. The renderer-owned View-to-Canvas compositor is covered
  by a dedicated on-device test, and the forced match entry point was separately verified.

final result: passed

---

# Product Design QA — Settings hierarchy and fullscreen panel motion

## Evidence

- Source visual truth (existing settings pattern): `/private/tmp/codex-setting-reference.png`
- Source pixels / viewport: `1080 × 2340`, Android portrait, device-native capture.
- Rendered playback settings, top section: `/private/tmp/codex-player-setting.png`
- Rendered playback settings, danmaku section: `/private/tmp/codex-player-setting-danmaku.png`
- Implementation pixels / viewport: both `1080 × 2340`, Android portrait, device-native capture.
- Full-view comparison: `/private/tmp/settings-qa-full.png`
- Full-view comparison pixels: `3240 × 2340`; three unscaled device captures placed side by side.
- Focused comparison: `/private/tmp/settings-qa-focused.png`
- Focused comparison pixels: `3240 × 720`; three unscaled title-region crops placed side by side.
- CSS size / density normalization: not applicable; all captures are native Android pixels from the same device, density, dynamic-light theme, and portrait viewport.
- State: source is the existing appearance settings page. Implementations show the new `播放设置` heading at scroll start and `弹幕设置` after the playback-only controls.

## Full-view comparison

The playback settings page now follows the same information hierarchy as the appearance settings page: a primary-color chapter label precedes each related control group, while preference items retain their existing Material layout. Playback-only items, including default speed and double-tap seeking, remain together above the danmaku chapter. The page title, controls, values, switches, sliders, scroll behavior, and dynamic color treatment are unchanged.

## Focused comparison

The focused comparison was required because the task is specifically about small section-heading hierarchy. `夜间模式`, `播放设置`, and `弹幕设置` use the same system font, visual size, regular weight, primary color, `12dp` horizontal inset, and surrounding vertical rhythm. The implementation headings remain visually subordinate to the top app-bar title and clearly distinct from preference labels.

## Required fidelity surfaces

- Fonts and typography: existing system font and Material typography remain unchanged. Both new headings match the established regular-weight section labels without wrapping or truncation.
- Spacing and layout rhythm: headings use the existing `12dp` horizontal inset and the page's existing `12dp` spaced-column rhythm. Playback preferences are grouped continuously before the danmaku heading.
- Colors and visual tokens: headings use `MaterialTheme.colorScheme.primary`, exactly matching the appearance settings pattern and current dynamic-light theme.
- Image quality and asset fidelity: no image, illustration, logo, video, or raster asset is changed by this task.
- Copy and content: `播放设置` reuses the existing localized string; `弹幕设置` was added as a localized resource. Preference labels and values are unchanged.
- Icons: no icon was added or altered.
- Interaction and accessibility: both section labels expose heading semantics. The fullscreen side panel retains back, scrim-dismiss, close, tabs, and settings interactions. Its scrim now fades while the panel enters from and exits toward the right.
- Responsiveness and motion: compact portrait continues to use the existing Material modal bottom sheet. Landscape/expanded layouts keep the bounded right-side surface and now observe the real `visible` transition; the surface remains composed during the `240ms` exit and is removed only after the animation completes.

## Comparison history

1. Evidence normalization — initial captures were taken during the settings navigation transition and showed partial outgoing content.
   - Fix: waited for the UI hierarchy to settle, then recaptured the appearance reference and both playback-settings states at the same viewport and density.
   - Post-fix evidence: `/private/tmp/settings-qa-full.png` and `/private/tmp/settings-qa-focused.png`.
2. No actionable P0, P1, or P2 visual difference was found after normalization.

## Findings

No actionable P0, P1, or P2 findings remain. The requested hierarchy is clear and matches the existing settings design language; the fullscreen panel no longer jumps into or out of composition.

## Verification

- `./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin` — passed
- `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest` — passed
- `PlayerPlaybackSettingsUiTest` — all 9 tests passed on the connected MEIZU 21
- `landscapeSettingsPanel_remainsComposedDuringSlideOut` — passed with the animation clock paused and advanced through the transition
- Stable device captures and same-input full/focused comparisons — passed

## Follow-up polish

- P3: a short device recording over live fullscreen video could document the motion aesthetically once a playback session is already open; the transition lifecycle and direction are covered by implementation and automated regression.

final result: passed

---

# Product Design QA — V2 playback detail migration (2026-08-10)

## Evidence

- Source visual truth: `design/ux-redesign/apple-music-yellow-v1/04-play-detail-download.png`
- Supporting player-control truth: `design/ux-redesign/apple-music-yellow-v1/05-player-controls-settings.png`
- Source pixels: `1700 × 925` and `1672 × 941`.
- Intended implementation viewport: native Android portrait playback detail, previously connected device viewport `1080 × 2344`.
- Implementation screenshot path: unavailable; `adb devices -l` returned no connected device after the APK build.
- CSS size / density normalization: not applicable to the native Android build; pixel normalization could not be completed without an implementation capture.
- State: V2 portrait playback detail with player visible, collapsed media description, normal (non-download-selection) state.

## Full-view comparison

Blocked. Both source boards were opened successfully, but a rendered implementation screenshot could not be captured because the Android device disconnected and no local AVD is configured. Code inspection and build success are not accepted as visual comparison evidence.

## Focused comparison

Blocked for the same reason. The required focused regions are the media identity/action area, the three compact summary rows, and the light episode sheet; none can be judged without device-rendered pixels.

## Required fidelity surfaces

- Fonts and typography: blocked pending device capture.
- Spacing and layout rhythm: blocked pending device capture.
- Colors and visual tokens: source tokens are implemented, but visible output is unverified.
- Image quality and asset fidelity: poster rendering and crop are unverified.
- Copy and content: static strings were checked in code, but wrapping and truncation are unverified.
- Interaction and accessibility: unit tests and Android-test compilation passed; runtime touch, focus, rotation, and sheet behavior remain unverified.

## Findings

- [P1] Missing rendered implementation evidence.
  - Location: V2 playback detail, player controls, and episode sheet.
  - Evidence: source boards are available, but no current implementation screenshot exists.
  - Impact: visual fidelity, theme isolation, crop, density, and viewport overflow cannot be approved.
  - Fix: reconnect the Android device, install `app/build/outputs/apk/debug/app-debug.apk`, capture the same portrait playback state plus the episode sheet, and compare them in one normalized image.

## Comparison history

1. Implementation pass created a dedicated `v2/ui/playback` page, dark playback palette, compact detail rows, and isolated light sheets.
2. JVM tests, Android-test compilation, and APK assembly passed.
3. Post-fix visual evidence could not be captured because ADB reported no connected device.

## Implementation checklist

- Reconnect and authorize the Android device.
- Install the latest debug APK.
- Capture portrait detail, episode sheet, download selection, and landscape player controls.
- Build same-input comparison boards and fix all visible P0/P1/P2 differences.

final result: blocked

---

# Product Design QA — V2 non-playback visual rewrite (2026-08-11)

## Evidence

- Primary visual truth: `/Users/heyanle/.codex/generated_images/019fd078-9d8c-70a1-ae6d-3132ff999f8b/exec-6be242f4-8f15-4bd9-9dda-78e0ea816dab.png`.
- Supporting boards: `design/ux-redesign/apple-music-yellow-v1/03-search.png`, `07-local-download.png`, `08-source-management.png`, `09-tags-migration-backup.png`, `10-settings-general.png`, and `11-settings-support-about.png`.
- Device: MEIZU 21, native portrait viewport `1080 × 2340`.
- Final home capture: `/private/tmp/easybangumi-v2-final-home-ready.png`.
- Same-input home comparison: `/private/tmp/easybangumi-v2-home-compare.png`.
- Additional inspected captures: `/private/tmp/easybangumi-v2-star.png`, `/private/tmp/easybangumi-v2-history.png`, `/private/tmp/easybangumi-v2-more.png`, `/private/tmp/easybangumi-v2-search.png`, `/private/tmp/easybangumi-v2-tag.png`, `/private/tmp/easybangumi-v2-settings.png`, and `/private/tmp/easybangumi-v2-source-final.png`.

## Comparison result

The V2 implementation now consistently uses the approved warm-white canvas, brand-yellow semantic accent, compact source header, short left-aligned primary underline, distinct dot-style secondary tabs, fixed three-column poster grid, flat management lists, grouped settings surfaces, and a layered bottom navigation with a compact selected icon container. Source-provided page counts and labels remain data-driven, so the connected Age source exposes two primary pages instead of the four illustrative labels in the reference.

## Findings and fixes

1. [P1 fixed] Home and followed-content grids previously used adaptive columns and legacy card spacing. They now use a fixed three-column layout with bare poster cards and compact metadata.
2. [P1 fixed] The bottom navigation previously used the Material navigation pill hierarchy. It now uses a compact selected icon container, persistent labels, and a top divider.
3. [P1 fixed] Source manager and local/download tabs initially used full-width equal distribution. Final capture confirms short, left-aligned underline tabs.
4. [P2 fixed] Tag and source management lists were enclosed in rounded surfaces. They are now flat, divider-based lists with drag, switch, error, and configuration affordances retained.
5. [P2 fixed] Search landing hierarchy was too close to the result-state header. It now has a large title, outlined search field, compact mode selector, and separate search-history section.

No actionable P0, P1, or P2 visual findings remain in the inspected non-playback states. Empty states intentionally preserve their business-state illustrations and copy.

## Verification

- `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` — passed.
- Final `:app:compileDebugKotlin :app:assembleDebug` — passed after the left-aligned tab correction.
- `git diff --check` — passed.
- Final debug APK installed successfully on the connected MEIZU 21.
- Playback visual implementation was excluded from this rewrite.

final result: passed

---

# Product Design QA — V2 semantic theme colors (2026-08-10)

## Evidence

- Source visual truth: `/Users/heyanle/.codex/generated_images/019fd078-9d8c-70a1-ae6d-3132ff999f8b/exec-6be242f4-8f15-4bd9-9dda-78e0ea816dab.png`
- Source design state: warm-white V2 home, brand-yellow accent, short primary-tab underline, full-width divider, and layered bottom navigation.
- Intended implementation viewport: native Android portrait, previously connected device viewport `1080 × 2344`.
- Implementation screenshot: unavailable; `adb devices -l` returned no connected or authorized device after the verified APK build.
- CSS size / density normalization: not applicable to the native Android app; same-input pixel comparison remains blocked without a rendered capture.

## Full-view comparison

Blocked. The implementation now applies semantic theme colors across every V2 screen except playback and adds a compact selected-state container to the bottom navigation, but code and build output are not accepted as visual evidence.

## Focused comparison

Blocked. The required focused states are the home primary tabs and divider, bottom navigation selection hierarchy, and `外观设置 → 主题色` with at least two live accent selections.

## Findings

- [P1] Missing rendered implementation evidence.
  - Location: V2 home, bottom navigation, and appearance theme-color chooser.
  - Evidence: the selected source design is available, while ADB reports no device.
  - Impact: live recoloring, text contrast, selected-state hierarchy, spacing, and clipping cannot be visually approved.
  - Fix: reconnect the device, install `app/build/outputs/apk/debug/app-debug.apk`, capture brand-yellow and one alternate theme at the same viewport, and compare each capture with the source in one normalized input.

## Automated verification

- `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` — passed.
- `V2ThemeColorTest` — passed after adding storage-key round-trip, fallback, uniqueness, and display-name coverage.
- Non-playback V2 source scan — no remaining `V2Tokens.BrandGold` or `V2Tokens.BrandGoldSoft` references.
- Playback source scan — legacy static brand-yellow references intentionally remain isolated in `PlaybackDetailV2.kt`.

final result: blocked

---

# Product Design QA — V2 tab motion and flat More list (2026-08-11)

## Evidence

- Source visual truth: `/Users/heyanle/.codex/generated_images/019fd078-9d8c-70a1-ae6d-3132ff999f8b/exec-2755765a-86f1-409b-bc39-b2dd70f81f72.png` and `/Users/heyanle/.codex/generated_images/019fd078-9d8c-70a1-ae6d-3132ff999f8b/exec-6be242f4-8f15-4bd9-9dda-78e0ea816dab.png`.
- Device implementation viewport: MEIZU 21, native `1080 × 2340` portrait pixels.
- Primary-tab end state: `/private/tmp/v2-tab-motion-final.png`.
- Primary-tab motion recording: `/private/tmp/v2-tab-motion.mp4`.
- Secondary-tab states: `/private/tmp/v2-age-schedule-before.png` and `/private/tmp/v2-age-schedule-after.png`.
- Flat More implementation: `/private/tmp/v2-more-flat.png`.
- Normalized More comparison: `/private/tmp/v2-more-flat-compare.png`; the source panel was aspect-preserving scaled to the device width and vertically padded, while the implementation remained native-density.

## Findings and comparison history

1. [P1 fixed] Tab selection changed color, weight, dot, and underline without motion. Text/dot/indicator colors and opacity now animate over `180ms`; tab font weight stays constant so the row does not remeasure during selection.
2. [P1 fixed] The selected secondary dot was conditionally inserted, moving the selected label and neighboring tabs. Every secondary tab now reserves the same `6dp` dot slot and animates only its opacity.
3. [P1 fixed] During secondary-page refresh, the tab row moved between the grid header and the loading container. The row is now a fixed `42dp` sibling above the content viewport. Before and after device semantics bounds are identical: all three labels remain at vertical bounds `[454,520]`.
4. [P2 fixed] The old clickable modifier let the ripple occupy an unintuitive rectangular region. Each tab now uses a clipped `8dp` rounded, bounded Material 3 ripple around its own stable touch target.
5. [P2 fixed] More inserted large gaps and full-width group separators between configuration clusters. All configuration rows now share one spacing rule and one continuous inset-divider rule; the debug row also uses the same single-line height.

## Required fidelity surfaces

- Typography: constant medium tab weight prevents measurement jumps; existing app typography is otherwise unchanged.
- Spacing and rhythm: primary and secondary tab bounds remain stable; More rows are continuous and uniformly spaced.
- Colors and tokens: transitions continue to use the active V2 semantic accent and neutral tokens.
- Image quality: no image assets were changed; poster and app-logo rendering remain native.
- Copy and content: no production labels or business content changed.

## Verification

- `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` — passed.
- `V2ScrollableTabsUiTest.secondaryTabSelection_keepsEveryClickableBoundsStable` — passed on the connected MEIZU 21.
- Manual secondary-tab switch from `今天` to `昨天` — fixed row bounds verified through UI hierarchy and screenshots.
- Final APK installed and interaction states checked on device.
- `git diff --check` — passed.

No actionable P0, P1, or P2 findings remain in this iteration.

final result: passed
