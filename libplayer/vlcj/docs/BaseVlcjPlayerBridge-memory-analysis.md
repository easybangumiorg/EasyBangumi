# BaseVlcjPlayerBridge Code Logic & Memory Analysis

## Code Logic Summary

- **Role**: Bridges the libplayer API with VLC's `MediaPlayer`. Holds a single `MediaPlayer` (lazy-created via `VlcjBridgeManager`), maintains play state in `StateFlow`s, and reacts to VLC events by posting updates to the EDT via `SwingUtilities.invokeLater`.
- **Lifecycle**: Listener is registered in `init`, and in `close()` the listener is removed and `mediaPlayer.release()` is called. `VlcjBridgeManager` keeps strong references to bridge instances (e.g. in `map`/`nativeMap`); call `release(tag)` to remove and close a bridge.
- **Seek handling**: `seekingTargetTime` is set on `seekTo()` and cleared when buffering reaches 100% and current time is within 15s of target (with 15s tolerance). While seeking, `positionMs` returns `seekingTargetTime` instead of actual time.

## Memory Considerations

| Area | Impact | Note |
|------|--------|------|
| **buffering() + invokeLater** | High | VLC can call `buffering()` very frequently (e.g. every 100ms). Each call allocates a `Runnable` and a lambda and posts to EDT, causing allocation churn and EDT load. |
| **Other listener callbacks** | Low | `opening`, `playing`, `paused`, `stopped`, `finished`, `mediaPlayerReady` are relatively low-frequency; their `invokeLater` usage is acceptable. |
| **mediaPlayerEventListener** | Low | Single anonymous object; captures `this`. Removed in `close()`, so no leak if bridge is released. |
| **prepareAction() / actionMap** | Low | Invoked once (guarded by CAS in `AbsPlayerBridge.action()`); one Map and one `VlcjSpeedAction` per bridge. |
| **StateFlow fields** | Low | Fixed number of `MutableStateFlow` instances from base class; no growth. |

## Recommended Optimization: Throttle buffering()

- **Goal**: Reduce allocations and EDT load when `buffering()` is called repeatedly.
- **Approach**: Only post to EDT when either (1) the desired play state would change, or (2) a minimum interval since the last post has elapsed (e.g. 150–200ms). Use a volatile “last posted state” and “last post time” to decide; run the actual state-update logic in the existing `invokeLater` block when we do post.

This keeps behavior correct (state still converges to READY/BUFFERING) while cutting the number of `invokeLater` runs and thus temporary objects.

## Other Notes

- **Empty overrides in MediaPlayerEventListener**: Many no-op overrides; they add little memory (vtable only). An adapter or default interface could reduce code size but not meaningfully change memory.
- **Closure capture**: The `buffering` lambda captures `time`, `newCache`, and `this` (for flows and `seekingTargetTime`). Throttling reduces how often that closure is created; further reducing capture would complicate the logic (e.g. calling `mediaPlayer?.controls()?.nextFrame()` and updating `seekingTargetTime` still require access to the bridge/mediaPlayer inside the posted runnable).
