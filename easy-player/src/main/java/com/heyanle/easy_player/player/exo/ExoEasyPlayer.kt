package com.heyanle.easy_player.player.exo

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.view.Surface
import android.view.SurfaceHolder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.easy_player.EasyPlayerManager
import com.heyanle.easy_player.constant.RealPlayerEvent
import com.heyanle.easy_player.player.IEasyPlayer

/**
 * Created by HeYanLe on 2022/10/23 15:53.
 * https://github.com/heyanLE
 */
class ExoEasyPlayer(
    private val context: Context
): IEasyPlayer, Player.Listener {

    private val mediaSourceHelper = ExoMediaSourceHelper.getInstance(context)

    private lateinit var internalPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource

    private lateinit var speedPlaybackParameters: PlaybackParameters
    private var isPreparing:Boolean = false

    private var playerEventListener: IEasyPlayer.EventListener? = null

    override fun init() {
        internalPlayer = ExoPlayer.Builder(
            context,
            DefaultRenderersFactory(context),
            DefaultMediaSourceFactory(context),
            DefaultTrackSelector(context),
            DefaultLoadControl(),
            DefaultBandwidthMeter.getSingletonInstance(context),
            DefaultAnalyticsCollector(Clock.DEFAULT)).build()

        //播放器日志

        //播放器日志
        if (EasyPlayerManager.getConfig().isEnablePlayerLog ) {
            internalPlayer.addAnalyticsListener(
                EventLogger(
                    "ExoPlayer"
                )
            )
        }

        internalPlayer.addListener(this)
    }

    override fun setVideoSource(url: String, headers: Map<String, String>?) {
        mediaSource = mediaSourceHelper.getMediaSource(url, headers)
    }

    override fun setVideoSource(fd: AssetFileDescriptor) {
        // un support
        throw UnsupportedOperationException()
    }

    override fun prepareAsync() {
        if (!::internalPlayer.isInitialized) return
        if (!::mediaSource.isInitialized) return
        if (::speedPlaybackParameters.isInitialized) {
            internalPlayer.playbackParameters = speedPlaybackParameters
        }
        isPreparing = true
        internalPlayer.setMediaSource(mediaSource)
        internalPlayer.prepare()
    }

    override fun start() {
        if (!::internalPlayer.isInitialized) return
        internalPlayer.playWhenReady = true
    }

    override fun pause() {
        if (!::internalPlayer.isInitialized) return
        internalPlayer.playWhenReady = false
    }

    override fun stop() {
        if (!::internalPlayer.isInitialized) return
        internalPlayer.stop()
    }

    override fun reset() {
        if (!::internalPlayer.isInitialized) return
        internalPlayer.stop()
        internalPlayer.clearMediaItems()
        internalPlayer.setVideoSurface(null)
        isPreparing = false
    }

    override fun isPlaying(): Boolean {
        if (!::internalPlayer.isInitialized) return false
        return when (internalPlayer.playbackState) {
            Player.STATE_BUFFERING, Player.STATE_READY -> internalPlayer.playWhenReady
            Player.STATE_IDLE, Player.STATE_ENDED -> false
            else -> false
        }
    }

    override fun seekTo(time: Long) {
        if (!::internalPlayer.isInitialized) return
        internalPlayer.seekTo(time)
    }

    override fun release() {
        if (::internalPlayer.isInitialized) {
            internalPlayer.removeListener(this)
            internalPlayer.release()
        }

        isPreparing = false
    }

    override fun getCurrentPosition(): Long {
        return if (!::internalPlayer.isInitialized) 0
        else internalPlayer.currentPosition
    }

    override fun getDuration(): Long {
        return if (!::internalPlayer.isInitialized) 0
        else internalPlayer.duration
    }

    override fun getBufferedPercentage(): Int {
        return if (!::internalPlayer.isInitialized) 0
        else internalPlayer.bufferedPercentage
    }

    override fun setSurface(surface: Surface) {
        if (::internalPlayer.isInitialized){
            internalPlayer.setVideoSurface(surface)
        }
    }

    override fun setSurfaceHolder(holder: SurfaceHolder) {
        setSurface(holder.surface)
    }

    override fun setVolume(left: Float, right: Float) {
        if (::internalPlayer.isInitialized){
            internalPlayer.volume = (left+right)/2f
        }
    }

    override fun setLooping(isLooping: Boolean) {
        if (::internalPlayer.isInitialized){
            internalPlayer.repeatMode =
                if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        }
    }

    override fun setSpeed(speed: Float) {
        if (::internalPlayer.isInitialized){
            val parameters = if(!::speedPlaybackParameters.isInitialized) PlaybackParameters(speed) else
                speedPlaybackParameters.withSpeed(speed)
            internalPlayer.playbackParameters = parameters
        }

    }

    override fun getSpeed(): Float {
        return if(!::speedPlaybackParameters.isInitialized) 1f else
            speedPlaybackParameters.speed
    }

    override fun setEventListener(eventListener: IEasyPlayer.EventListener) {
        this.playerEventListener = eventListener
    }

    override fun removeEventListener(eventListener: IEasyPlayer.EventListener) {
        this.playerEventListener = null
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playerEventListener == null) return
        if (isPreparing) {
            if (playbackState == Player.STATE_READY) {
                playerEventListener?.onPrepared()
                playerEventListener?.onRealPlayerEvent(RealPlayerEvent.REAL_PLAYER_EVENT_RENDERING_START)
                isPreparing = false
            }
            return
        }
        when (playbackState) {
            Player.STATE_BUFFERING -> playerEventListener?.onRealPlayerEvent(
                RealPlayerEvent.REAL_PLAYER_EVENT_BUFFERING_START,
                getBufferedPercentage()
            )
            Player.STATE_READY -> playerEventListener?.onRealPlayerEvent(
                RealPlayerEvent.REAL_PLAYER_EVENT_BUFFERING_END,
                getBufferedPercentage()
            )
            Player.STATE_ENDED -> playerEventListener?.onCompletion()
            Player.STATE_IDLE -> {}
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        playerEventListener?.onError()
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        playerEventListener?.onVideoSizeChanged(videoSize.width, videoSize.height)
        if (videoSize.unappliedRotationDegrees > 0) {
            playerEventListener?.onRealPlayerEvent(
                RealPlayerEvent.REAL_PLAYER_EVENT_VIDEO_ROTATION_CHANGED,
                videoSize.unappliedRotationDegrees
            )
        }
    }

}