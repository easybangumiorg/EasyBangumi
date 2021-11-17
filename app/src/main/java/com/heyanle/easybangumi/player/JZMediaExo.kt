package com.heyanle.easybangumi.player

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import cn.jzvd.JZMediaInterface
import cn.jzvd.Jzvd
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.easybangumi.R

/**
 * Created by HeYanLe on 2021/11/17 21:16.
 * https://github.com/heyanLE
 */
class JZMediaExo(jzvd: Jzvd) : JZMediaInterface(jzvd),Player.Listener {

    companion object{
        private const val TAG = "JZMediaExo"
    }

    private var exoPlayer: ExoPlayer? = null
    private var callback: Runnable? = null
    private var previousSeek: Long = 0

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        if(SAVED_SURFACE == null){
            SAVED_SURFACE = p0
            prepare()
        }else{
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE)
        }
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
      return false
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

    }

    override fun start() {
        exoPlayer?.playWhenReady = true
    }

    override fun prepare() {
        Log.e(TAG, "prepare")
        val context = jzvd.context

        release()
        mMediaHandlerThread = HandlerThread("JZVD")
        mMediaHandlerThread.start()

        mMediaHandler = Handler(context.mainLooper)
        handler = Handler(Looper.getMainLooper())

        mMediaHandler.post {
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            val trackSelector: TrackSelector =
                DefaultTrackSelector(context, videoTrackSelectionFactory)
            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                .setBufferDurationsMs(360000, 600000, 1000, 5000)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .build()

            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
            // 2. Create the player
            val renderersFactory: RenderersFactory = DefaultRenderersFactory(context)

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setBandwidthMeter(bandwidthMeter).build()

            // Produces DataSource instances through which media data is loaded.
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSource.Factory(context)

            val currUrl = jzvd.jzDataSource.currentUrl.toString()

            val videoSource = if (currUrl.contains(".m3u8")) {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(
                        MediaItem.Builder().setUri(Uri.parse(currUrl)).setMimeType(MimeTypes.APPLICATION_M3U8)
                            .build())
                //addEventListener 这里只有两个参数都要传入值才可以成功设置
                // 否者会被断言 Assertions.checkArgument(handler != null && eventListener != null);
                // 并且报错  IllegalArgumentException()  所以不需要添加监听器时 注释掉
                //      videoSource .addEventListener( handler, null);
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.Builder().setUri(Uri.parse(currUrl)).build())
            }

            exoPlayer?.addListener(this)
            Log.e(TAG, "URL Link = $currUrl")

            val isLoop = jzvd.jzDataSource.looping
            if (isLoop) {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }
            exoPlayer?.setMediaSource(videoSource)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
            callback = Runnable {
                exoPlayer?.let {
                    val percent = it.bufferedPercentage
                    handler.post {
                        jzvd.setBufferProgress(percent)
                    }
                    if(percent < 100){
                        handler.postDelayed(callback!!, 300)
                    }else{
                        handler.removeCallbacks(callback!!)
                    }
                }
            }
            jzvd.textureView?.let {
                val surfaceTexture = jzvd.textureView.surfaceTexture
                if(surfaceTexture != null){
                    exoPlayer?.setVideoSurface(Surface(surfaceTexture))
                }
            }
        }
    }

    override fun pause() {
        exoPlayer?.playWhenReady = false
    }

    override fun isPlaying(): Boolean {
        return exoPlayer?.playWhenReady?:false
    }

    override fun seekTo(time: Long) {
        exoPlayer?.let {
            if(time != previousSeek){
                if(time >= it.bufferedPosition){
                    jzvd.onStatePreparingPlaying()
                }
                it.seekTo(time)
                previousSeek = time
                jzvd.seekToInAdvance = time
            }
        }
    }

    override fun release() {
        if(mMediaHandler != null && mMediaHandlerThread != null && exoPlayer != null){
            val tmpHandlerThread = mMediaHandlerThread
            val exoPlayer = this.exoPlayer
            JZMediaInterface.SAVED_SURFACE = null
            mMediaHandler.post {
                exoPlayer?.release()
                tmpHandlerThread?.quit()
            }
            this.exoPlayer = null
        }
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        exoPlayer?.volume = leftVolume
        exoPlayer?.volume = rightVolume
    }

    override fun setSpeed(speed: Float) {
        val playbackParameters = PlaybackParameters(speed, 1.0f)
        exoPlayer?.playbackParameters = playbackParameters
    }

    override fun setSurface(surface: Surface?) {
        exoPlayer ?. setVideoSurface( surface)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        //super.onVideoSizeChanged(videoSize)
        handler.post {
            jzvd.onVideoSizeChanged(((videoSize.width * videoSize.pixelWidthHeightRatio).toInt()), videoSize.height)
        }
    }

    override fun onRenderedFirstFrame() {
        //super.onRenderedFirstFrame()
        Log.e(TAG, "onRenderedFirstFrame")
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        //super.onTimelineChanged(timeline, reason)
        Log.e(TAG, "onTimelineChanged")
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        //super.onPlayWhenReadyChanged(playWhenReady, reason)
        exoPlayer?.let {
            onPlaybackStateChanged(it.playbackState)
        }

    }
    override fun onPlaybackStateChanged(playbackState: Int) {
        //super.onPlaybackStateChanged(playbackState)
        Log.e(TAG, "onPlayerStateChanged$playbackState")
        handler.post {
            when(playbackState){
                Player.STATE_IDLE ->{}
                Player.STATE_BUFFERING -> {
                    jzvd.onStatePreparingPlaying()
                    callback?.let {
                        handler.post (it)
                    }
                }
                Player.STATE_READY -> {
                    exoPlayer?.let {
                        if(it.playWhenReady){
                            jzvd.onStatePlaying()
                        }
                    }
                }
                Player.STATE_ENDED -> {
                    jzvd.onCompletion()
                }
            }
        }
    }


}