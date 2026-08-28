package loli.ball.easyplayer2

import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer

enum class EasyPlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
}

data class EasyVideoSize(
    val width: Int,
    val height: Int,
)

/**
 * Playback contract consumed by the Compose controller layer.
 *
 * The UI deliberately depends on this small contract instead of ExoPlayer so another native
 * engine (libmpv) can reuse fullscreen, gestures, progress, speed and danmaku behavior.
 */
interface EasyPlayerController {
    interface Listener {
        fun onPlaybackStateChanged(state: EasyPlaybackState) = Unit
        fun onPlayWhenReadyChanged(playWhenReady: Boolean) = Unit
        fun onIsPlayingChanged(isPlaying: Boolean) = Unit
        fun onVideoSizeChanged(size: EasyVideoSize) = Unit
        fun onPositionDiscontinuity(positionMs: Long) = Unit
    }

    var playWhenReady: Boolean
    val isPlaying: Boolean
    val playbackState: EasyPlaybackState
    val hasMedia: Boolean
    val currentPosition: Long
    val duration: Long
    val bufferedPosition: Long
    val speed: Float

    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun seekForward()
    fun seekBack()
    fun setSpeed(speed: Float)
    fun stop()
    fun attach(view: View)
    fun detach(view: View)
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}

class ExoEasyPlayerController(
    val player: ExoPlayer,
) : EasyPlayerController, Player.Listener {
    private val listeners = linkedSetOf<EasyPlayerController.Listener>()

    override var playWhenReady: Boolean
        get() = player.playWhenReady
        set(value) {
            player.playWhenReady = value
        }

    override val isPlaying: Boolean get() = player.isPlaying
    override val playbackState: EasyPlaybackState
        get() = player.playbackState.toEasyPlaybackState()
    override val hasMedia: Boolean
        get() = player.playbackState == Player.STATE_BUFFERING || player.playbackState == Player.STATE_READY
    override val currentPosition: Long get() = player.currentPosition.coerceAtLeast(0L)
    override val duration: Long
        get() = player.duration.takeUnless { it == C.TIME_UNSET }?.coerceAtLeast(0L) ?: 0L
    override val bufferedPosition: Long get() = player.bufferedPosition.coerceAtLeast(0L)
    override val speed: Float get() = player.playbackParameters.speed

    override fun play() = player.play()
    override fun pause() = player.pause()
    override fun seekTo(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))
    override fun seekForward() = player.seekForward()
    override fun seekBack() = player.seekBack()
    override fun setSpeed(speed: Float) = player.setPlaybackSpeed(speed)
    override fun stop() = player.stop()

    override fun attach(view: View) {
        when (view) {
            is SurfaceView -> player.setVideoSurfaceView(view)
            is TextureView -> player.setVideoTextureView(view)
        }
    }

    override fun detach(view: View) {
        when (view) {
            is SurfaceView -> player.clearVideoSurfaceView(view)
            is TextureView -> player.clearVideoTextureView(view)
        }
    }

    override fun addListener(listener: EasyPlayerController.Listener) {
        if (listeners.isEmpty()) player.addListener(this)
        listeners += listener
    }

    override fun removeListener(listener: EasyPlayerController.Listener) {
        listeners -= listener
        if (listeners.isEmpty()) player.removeListener(this)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        listeners.toList().forEach { it.onPlaybackStateChanged(playbackState.toEasyPlaybackState()) }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        listeners.toList().forEach { it.onPlayWhenReadyChanged(playWhenReady) }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        listeners.toList().forEach { it.onIsPlayingChanged(isPlaying) }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        listeners.toList().forEach {
            it.onVideoSizeChanged(EasyVideoSize(videoSize.width, videoSize.height))
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        listeners.toList().forEach { it.onPositionDiscontinuity(newPosition.positionMs) }
    }

    private fun Int.toEasyPlaybackState(): EasyPlaybackState = when (this) {
        Player.STATE_BUFFERING -> EasyPlaybackState.BUFFERING
        Player.STATE_READY -> EasyPlaybackState.READY
        Player.STATE_ENDED -> EasyPlaybackState.ENDED
        else -> EasyPlaybackState.IDLE
    }
}
