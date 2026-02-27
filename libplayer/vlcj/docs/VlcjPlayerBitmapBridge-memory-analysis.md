# VlcjPlayerBitmapBridge Code Logic & Memory Analysis

## Code Logic Summary

- **Role**: Extends `BaseVlcjPlayerBridge` and attaches a `CallbackVideoSurface` so that VLC renders into a buffer. Each frame is copied into a reusable `ByteArray`, wrapped in a single reusable `Bitmap`, and delivered to an `OnFrameListener` (e.g. `VlcPlayerBitmapFrameState` for Compose UI).
- **Flow**: VLC calls `display()` on the render thread → buffer is copied into `bufferArray` → a task is posted (coroutine or EDT) → `firePendingFrame()` runs and calls `listener.onFrame(bitmap, time)`.
- **Lifecycle**: `callbackVideoSurface.attach(mediaPlayer)` in init. Listener is set/removed via `setFrameListener` / `removeFrameListener`. Bridge is held by `VlcjBridgeManager.getOrCreateBitmapBridge(tag)`; call `manager.release(tag)` to close.

## Memory-Related Points

| Area | Impact | Note |
|------|--------|------|
| **bufferArray** | Medium | Single reused `ByteArray`; resized only when frame size changes via `getBufferArray(size)`. Avoids per-frame byte array allocation. |
| **bitmap** | Low | One `Bitmap` instance, reused every frame with `installPixels()`. |
| **display() per-frame allocation** | High (before fix) | Previously each frame created a new lambda and a new `Runnable`/coroutine. At 30–60 fps this caused significant allocation churn. **Fixed** by copying the buffer synchronously in `display()` and posting a single shared `Runnable`. |
| **Dead mediaPlayerEventListener** | Medium | The class defined its own full `MediaPlayerEventListener` but never registered it; the parent’s listener in `BaseVlcjPlayerBridge` is the one used. **Fixed** by removing the duplicate listener object. |
| **bufferFormatCallback** | Low | `getBufferFormat` creates a new `ImageInfo` and `RV32BufferFormat` only when format changes. `newFormatSize` updates `VideoSize` flow; low frequency. |
| **VlcPlayerBitmapFrameState.onFrame** | Separate | `composeBitmap = bitmap.asComposeImageBitmap()` may allocate per frame on the UI side; consider throttling or caching if needed. |

## Optimizations Applied

1. **Removed unused `mediaPlayerEventListener`**  
   The entire duplicate listener (180+ lines) was never registered. Removed to save one large object and reduce code size.

2. **Reused a single Runnable in `display()`**  
   - Copy from the native buffer into `bufferArray` inside `display()` (synchronously).  
   - Set `pendingFrameTime` and post a shared `sharedFrameRunnable`.  
   - `firePendingFrame()` reads `pendingFrameTime`, `imageInfo`, `bufferArray`, and invokes `listener.onFrame(bitmap, t)`.  
   This removes per-frame lambda and Runnable allocation while keeping behavior correct (same frame data and time are delivered).

## Other Notes

- **Row bytes**: `installPixels` uses `imageInfo.width * 4` (RV32 = 4 bytes per pixel); no need to capture `bufferFormat` in the runnable.
- **Double-buffer**: Not added; if the next frame overwrites `bufferArray` before the runnable runs, that frame is shown instead (one frame drop). Acceptable for playback and keeps implementation simple.
