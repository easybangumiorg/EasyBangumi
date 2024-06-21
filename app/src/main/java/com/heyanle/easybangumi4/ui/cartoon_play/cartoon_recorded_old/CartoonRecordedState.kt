package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded_old

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.view.LayoutInflater
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded_old.clip_video.ClipVideoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import loli.ball.easyplayer2.texture.EasyTextureView
import loli.ball.easyplayer2.utils.MeasureHelper

/**
 * Created by heyanlin on 2024/6/11.
 */
class CartoonRecordedState(
    val ctx: Context,
    val exoPlayer: ExoPlayer,
    val mediaSource: MediaSource,
    val scope: CoroutineScope,
    val thumbnailBuffer: ThumbnailBuffer,
    val start: Long,
    val end: Long,
    val currentPosition: Long,
):  Player.Listener, TextureView.SurfaceTextureListener {

    data class Configuration(
        val fps: Int = 15,

        // 1->gif 2->mp4
        val type: Int = 1,
    )

    data class RecordedState(
        // 0->idle 1->recording 2->saving 3->completely
        val state: Int = 0,
        val configuration: Configuration = Configuration(),
    )

    @UnstableApi
    val clipVideoState = ClipVideoState(
        ctx, mediaSource, scope, thumbnailBuffer, start, end, currentPosition
    )


    private val _stateFlow = MutableStateFlow(RecordedState())
    val stateFlow = _stateFlow.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    val layout = LayoutInflater.from(APP).inflate(R.layout.recorded_layout, null)


    @SuppressLint("StaticFieldLeak")
    val textureView: EasyTextureView = layout.findViewById<EasyTextureView?>(R.id.easy_texture)
        .apply {
            setScaleType(MeasureHelper.SCREEN_SCALE_ADAPT)
            setExtSurfaceTextureListener(this@CartoonRecordedState)
        }

    @SuppressLint("StaticFieldLeak")
    val rectSelectionView: RectSelectionView = layout.findViewById(R.id.rect_selection_view)

    fun onLaunch(){
        exoPlayer.addListener(this)
        //exoPlayer.clearVideoSurface()
        exoPlayer.clearVideoSurface()
        textureView.attachPlayer(exoPlayer)
    }

    fun onDispose(){
        exoPlayer.removeListener(this)
        textureView.detachPlayer(exoPlayer)
        //exoPlayer.clearVideoTextureView(textureView)
    }

    fun startRecord(){}

    fun stopRecord(){}

    fun makeGif(){}

    fun changeGif(){}

    fun changeMp4(){}

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        textureView.setVideoSize(videoSize.width, videoSize.height)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    @OptIn(UnstableApi::class)
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        if (exoPlayer.isPlaying && exoPlayer.playWhenReady){
            val cur = exoPlayer.currentPosition
            if (cur !in clipVideoState.selectionStart..clipVideoState.selectionEnd){
                exoPlayer.seekTo(clipVideoState.selectionStart)
                clipVideoState.currentPosition = clipVideoState.selectionStart
            }else{
                clipVideoState.currentPosition = exoPlayer.currentPosition
            }
            clipVideoState.check()
        }
    }
}
