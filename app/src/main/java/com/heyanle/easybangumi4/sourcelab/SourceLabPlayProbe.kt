package com.heyanle.easybangumi4.sourcelab

import android.view.Surface
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

interface LabPlaybackProbe {
    suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long): ProbeOutcome
}

/**
 * 与正常播放使用相同 MediaSource 和请求头，不读写媒体缓存，批量验收时静音。
 * 首帧只是起点；之后继续播放并观察真实 currentPosition 推进，不 seek、不修改速度。
 */
class SourceLabPlayProbe(private val mediaSourceFactory: CartoonMediaSourceFactory) : LabPlaybackProbe {
    private var player: ExoPlayer? = null
    private var surface: Surface? = null

    /** Surface 的生命周期归调用方；探针只借用，不 release。 */
    fun attachSurface(surface: Surface?) {
        this.surface = surface
        player?.setVideoSurface(surface)
    }

    suspend fun probe(info: PlayerInfo, timeoutMs: Long): ProbeOutcome =
        probe(info, timeoutMs, LabConfig.DEFAULT_MINIMUM_PROGRESS_MS)

    override suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long): ProbeOutcome =
        withContext(Dispatchers.Main) {
            val exo = ExoPlayer.Builder(APP).build()
            player = exo
            val done = CompletableDeferred<ProbeOutcome>()
            var firstFrame = false
            var firstFramePositionMs = -1L
            var ready = false
            var size: VideoSize? = null

            fun progress(): Long = if (firstFramePositionMs >= 0) {
                (exo.currentPosition - firstFramePositionMs).coerceAtLeast(0L)
            } else 0L

            fun outcome(error: Boolean, message: String, timedOut: Boolean = false,
                        failureKind: LabFailureKind? = null) = ProbeOutcome(
                error = error, firstFrame = firstFrame, ready = ready, videoSize = size,
                positionMs = exo.currentPosition, message = message, progressMs = progress(),
                firstFramePositionMs = firstFramePositionMs, timedOut = timedOut,
                failureKind = failureKind,
            )

            val listener = object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    // Surface 附加/替换可能产生重复回调，不重置首次计时基准。
                    if (!firstFrame && surface?.isValid == true) {
                        firstFrame = true
                        firstFramePositionMs = exo.currentPosition
                    }
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) { size = videoSize }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) ready = true
                    if (playbackState == Player.STATE_ENDED) {
                        done.complete(outcome(false, "已到结尾，首帧后推进 ${progress()}ms"))
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    val causes = generateSequence<Throwable>(error) { it.cause }.take(16).toList()
                    val kind = when {
                        causes.any { it is java.net.SocketTimeoutException } -> LabFailureKind.TIMEOUT
                        causes.any { it is java.io.IOException } -> LabFailureKind.NETWORK
                        else -> LabFailureKind.PLAYBACK
                    }
                    done.complete(outcome(true,
                        "播放失败：${error.errorCodeName} / ${error.cause?.javaClass?.simpleName.orEmpty()}", failureKind = kind))
                }
            }

            try {
                exo.addListener(listener)
                exo.volume = 0f
                exo.setMediaSource(mediaSourceFactory.getWithoutCache(info))
                surface?.let { exo.setVideoSurface(it) }
                exo.prepare()
                exo.playWhenReady = true
                withTimeoutOrNull(timeoutMs) {
                    while (!done.isCompleted) {
                        if (firstFrame && exo.isPlaying && progress() >= minimumProgressMs &&
                            (size?.width ?: 0) > 0 && (size?.height ?: 0) > 0 && surface?.isValid == true) {
                            done.complete(outcome(false, "首帧已渲染，进度推进 ${progress()}ms"))
                        }
                        if (!done.isCompleted) delay(200)
                    }
                    done.await()
                } ?: outcome(
                    false,
                    when {
                        surface?.isValid != true -> "没有有效渲染 Surface，不能证明真机首帧"
                        firstFrame -> "首帧后推进 ${progress()}ms，未达到 ${minimumProgressMs}ms（${timeoutMs / 1000}s 超时）"
                        ready -> "已就绪但未渲染首帧（${timeoutMs / 1000}s 超时）"
                        else -> "播放器未就绪（${timeoutMs / 1000}s 超时）"
                    },
                    timedOut = true,
                )
            } finally {
                runCatching { exo.stop(); exo.release() }
                if (player === exo) player = null
            }
        }

    fun release() {
        runCatching { player?.release() }
        player = null
        attachSurface(null)
    }
}

data class ProbeOutcome(
    val error: Boolean,
    val firstFrame: Boolean,
    val ready: Boolean,
    val videoSize: VideoSize?,
    val positionMs: Long,
    val message: String,
    val progressMs: Long = 0L,
    val firstFramePositionMs: Long = -1L,
    val timedOut: Boolean = false,
    val failureKind: LabFailureKind? = null,
)
