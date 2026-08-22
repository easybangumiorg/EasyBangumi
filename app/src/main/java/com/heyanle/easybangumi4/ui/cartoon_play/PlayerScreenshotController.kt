package com.heyanle.easybangumi4.ui.cartoon_play

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.utils.MediaAndroidUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

sealed interface PlayerScreenshotState {
    data object Idle : PlayerScreenshotState
    data object Capturing : PlayerScreenshotState

    data class Saved(
        val uri: Uri,
        val preview: Bitmap,
    ) : PlayerScreenshotState

    data class Failed(val message: String) : PlayerScreenshotState
}

/**
 * Coordinates a single screenshot transaction across the player, renderer and MediaStore.
 *
 * Video is captured from TextureView and the renderer-owned danmaku View is composited separately,
 * deliberately excluding Compose controls. The previous play intent is always restored.
 */
@Stable
class PlayerScreenshotController internal constructor(
    private val playingViewModel: CartoonPlayingViewModel,
    private val scope: CoroutineScope,
    private val danmakuRenderer: () -> DfmDanmakuRenderer?,
    private val includeDanmaku: () -> Boolean,
) {
    var state by mutableStateOf<PlayerScreenshotState>(PlayerScreenshotState.Idle)
        private set

    private var captureJob: Job? = null

    fun capture() {
        if (captureJob?.isActive == true) return
        captureJob = scope.launch {
            val player = playingViewModel.exoPlayer
            val previousPlayWhenReady = player.playWhenReady
            var frame: Bitmap? = null
            var pendingPreview: Bitmap? = null
            var displayedPreview: Bitmap? = null
            try {
                state = PlayerScreenshotState.Capturing
                try {
                    player.pause()
                    // Let the paused video and danmaku clocks settle on the same visible frame.
                    withFrameNanos { }
                    frame = playingViewModel.easyTextRenderer
                        .getTextureViewOrNull()
                        ?.bitmap
                        ?: error("当前画面暂时无法截图")

                    if (includeDanmaku()) {
                        danmakuRenderer()?.drawSnapshotOnto(
                            canvas = Canvas(checkNotNull(frame)),
                            targetWidth = checkNotNull(frame).width,
                            targetHeight = checkNotNull(frame).height,
                        )
                    }

                    val capturedFrame = checkNotNull(frame)
                    val previewWidth = 192
                    val previewHeight =
                        (capturedFrame.height * (previewWidth.toFloat() / capturedFrame.width))
                            .roundToInt()
                            .coerceAtLeast(1)
                    pendingPreview = Bitmap.createScaledBitmap(
                        capturedFrame,
                        previewWidth,
                        previewHeight,
                        true,
                    )
                    val displayName = "EasyBangumi_${
                        SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
                    }.png"
                    val uri = MediaAndroidUtils.saveImage(capturedFrame, displayName).getOrThrow()
                    capturedFrame.recycle()
                    frame = null

                    displayedPreview = checkNotNull(pendingPreview)
                    pendingPreview = null
                    state = PlayerScreenshotState.Saved(
                        uri = uri,
                        preview = checkNotNull(displayedPreview),
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    state = PlayerScreenshotState.Failed(
                        throwable.message ?: "截图保存失败",
                    )
                } finally {
                    frame?.recycle()
                    pendingPreview?.recycle()
                    player.playWhenReady = previousPlayWhenReady
                }

                delay(2_500)
                state = PlayerScreenshotState.Idle
                // Allow Compose to stop drawing the preview before releasing its native pixels.
                withFrameNanos { }
                displayedPreview?.recycle()
                displayedPreview = null
            } finally {
                state = PlayerScreenshotState.Idle
                displayedPreview?.recycle()
            }
        }
    }

    fun cancel() {
        captureJob?.cancel()
    }
}

@Composable
fun rememberPlayerScreenshotController(
    playingViewModel: CartoonPlayingViewModel,
    danmakuRenderer: DfmDanmakuRenderer?,
    includeDanmaku: Boolean,
): PlayerScreenshotController {
    val scope = rememberCoroutineScope()
    val currentRenderer by rememberUpdatedState(danmakuRenderer)
    val currentIncludeDanmaku by rememberUpdatedState(includeDanmaku)
    val controller = remember(playingViewModel, scope) {
        PlayerScreenshotController(
            playingViewModel = playingViewModel,
            scope = scope,
            danmakuRenderer = { currentRenderer },
            includeDanmaku = { currentIncludeDanmaku },
        )
    }
    DisposableEffect(controller) {
        onDispose(controller::cancel)
    }
    return controller
}
